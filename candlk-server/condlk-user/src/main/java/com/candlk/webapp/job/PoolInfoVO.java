package com.candlk.webapp.job;

import java.math.*;
import java.util.*;

import com.alibaba.fastjson2.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.web3j.abi.datatypes.StaticStruct;
import org.web3j.protocol.Web3j;

@Slf4j
@Getter
@Setter
public class PoolInfoVO extends StaticStruct {

	/** 代理地址 */
	public String delegateAddress;
	public String poolAddress;
	public String owner;
	public String keyBucketTracker;
	public String esXaiBucketTracker;
	public BigInteger keyCount;
	public BigInteger totalStakedAmount;
	public BigInteger updateSharesTimestamp;
	public BigInteger ownerShare;
	public BigInteger keyBucketShare;
	public BigInteger stakedBucketShare;

	public BigInteger v1;
	public BigInteger v2;
	public BigInteger v3;
	public BigInteger v4;

	public BigInteger v5;
	public BigInteger _ownerStakedKeys;
	public BigInteger _ownerRequestedUnstakeKeyAmount;
	public BigInteger _ownerLatestUnstakeRequestLockTime;

	public BigInteger v6;
	public String _name;

	public transient volatile BigDecimal esXAIPower;

	/**
	 * esXAI算力 = 10000/esXAI总质押 * (keys总质押 * 阶梯加成 * esXAI分成比例 = esXAI池总算力)
	 */
	public synchronized BigDecimal calcEsXAIPower(BigDecimal wei) {
		if (esXAIPower != null) {
			return esXAIPower;
		}
		final BigDecimal totalStakedAmount = new BigDecimal(this.totalStakedAmount).movePointLeft(18);
		final BigDecimal tier = calcStakingTier(totalStakedAmount);
		if (stakedBucketShare.compareTo(BigInteger.ZERO) <= 0 || wei.compareTo(totalStakedAmount) >= 0 || tier.compareTo(new BigDecimal(2)) < 0) { // 无配比
			return esXAIPower = BigDecimal.ZERO;
		}
		final BigDecimal esXAIPoolTotalPower = new BigDecimal(keyCount).multiply(tier)
				.multiply(new BigDecimal(stakedBucketShare).divide(percent, 18, RoundingMode.HALF_UP));
		return esXAIPower = (wei.divide(totalStakedAmount, 18, RoundingMode.HALF_UP)).multiply(esXAIPoolTotalPower).setScale(2, RoundingMode.HALF_UP);
	}

	public transient volatile BigDecimal keysPower;

	/**
	 * keys算力 = 1 / keys总质押 * ( keys总质押 * 阶梯加成 * keys分成比例 = keys池总算力)
	 */
	public synchronized BigDecimal calcKeysPower(BigDecimal wei) {
		if (keysPower != null) {
			return keysPower;
		}
		final BigDecimal totalStakedAmount = new BigDecimal(this.totalStakedAmount).movePointLeft(18);
		if (keyBucketShare.compareTo(BigInteger.ZERO) <= 0) { // 无配比
			return keysPower = BigDecimal.ZERO;
		}
		final BigDecimal keyCount = new BigDecimal(this.keyCount);
		final BigDecimal keysPoolTotalPower = keyCount.multiply(calcStakingTier(totalStakedAmount))
				.multiply(new BigDecimal(keyBucketShare).divide(percent, 18, RoundingMode.HALF_UP));
		return keysPower = wei.divide(keyCount, 18, RoundingMode.HALF_UP)
				.multiply(keysPoolTotalPower).setScale(2, RoundingMode.HALF_UP);
	}

	public final static BigDecimal Silver = new BigDecimal(10000),// Silver 白银
			Gold = new BigDecimal(100000), // Gold 黄金
			Platinum = new BigDecimal(500000), // Platinum 铂金
			Diamond = new BigDecimal(5500000), // Diamond 钻石
			percent = new BigDecimal(1000_000);

	public transient volatile BigDecimal tierCache;

	public synchronized BigDecimal calcStakingTier(BigDecimal totalStakedAmount) {
		if (tierCache != null) {
			return tierCache;
		}
		final BigDecimal tier; // Bronze 青铜
		if (totalStakedAmount.compareTo(Diamond) >= 0) {
			tier = BigDecimal.valueOf(6);
		} else if (totalStakedAmount.compareTo(Platinum) >= 0) {
			tier = BigDecimal.valueOf(3);
		} else if (totalStakedAmount.compareTo(Gold) >= 0) {
			tier = BigDecimal.valueOf(2);
		} else if (totalStakedAmount.compareTo(Silver) >= 0) {
			tier = BigDecimal.valueOf(1.5);
		} else {
			tier = BigDecimal.ONE;
		}
		return tierCache = tier;
	}

	public String getDelegateAddress(Web3j web3j, boolean flush) {
		int retry = 3;
		while (retry-- > 0) {
			try {
				if (flush || StringUtils.isEmpty(this.delegateAddress)) {
					this.delegateAddress = PoolInfo.getDelegateOwner(web3j, poolAddress);
					// 无委托代理地址，所有人则是 owner
					if ("0x0000000000000000000000000000000000000000".equals(this.delegateAddress)) {
						this.delegateAddress = this.owner;
					}
				}
				return this.delegateAddress;
			} catch (Exception e) {
				log.error("获取委托人地址异常：", e);
			}
		}
		return this.delegateAddress;
	}

	public boolean weakActiveCheck(RestTemplate restTemplate, String poolAddress, BigInteger startBlockNumber, BigDecimal weakActiveThreshold) {
		// arbiscan API 1s 5次请求
		final String url = "https://api.arbiscan.io/api?module=account&action=tokentx&contractaddress=0x4c749d097832de2fecc989ce18fdc5f1bd76700c&address=" + poolAddress + "&page=1&offset=100&startblock=" + startBlockNumber + "&endblock=latest&sort=desc&apikey=J63CM8DDT4J4PMBZEVGQ47Z9ZHWAUXIYEX";
		int retry = 3;
		while (retry-- > 0) {
			try {
				final JSONObject resp = restTemplate.getForEntity(url, JSONObject.class).getBody();
				if (resp != null && "1".equals(resp.getString("status"))) {
					final List<ScanTx> result = resp.getList("result", ScanTx.class);
					int counter = 0;
					for (ScanTx tx : result) {
						if ("0x0000000000000000000000000000000000000000".equals(tx.from) && poolAddress.equals(tx.to) && "deprecated".equals(tx.input)
								&& ((System.currentTimeMillis() / 1000) - tx.timeStamp < 3600)) {
							// 1小时内，超过两笔交易 零地址 交易，或者单笔 2000+（此规则需结合质押的Keys数量进行使用）
							// TODO 每次减半后需要进行调整，需结合质押的Keys数进行
							if (++counter >= 2 || new BigDecimal(tx.value).movePointLeft(18).compareTo(weakActiveThreshold) >= 0) {
								return true;
							}
						}
					}
				}
			} catch (Exception e) {
				final String message = e.getMessage();
				log.error("根据合约交易历史验证是否活跃失败：poolAddress={},startBlockNumber={},weakActiveThreshold={},url={},message={}", poolAddress, startBlockNumber, weakActiveThreshold, url, message == null ? e : message);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ignored) {
				}
			}
		}
		return false;
	}

	public boolean hasActivePool(RestTemplate restTemplate, String delegateOwner, BigInteger startBlockNumber) {
		// arbiscan API 1s 5次请求
		final String url = "https://api.arbiscan.io/api?module=account&action=txlist&address=" + delegateOwner + "&startblock=" + startBlockNumber + "&endblock=latest&page=1&offset=10&sort=desc&apikey=J63CM8DDT4J4PMBZEVGQ47Z9ZHWAUXIYEX";
		int retry = 3;
		while (retry-- > 0) {
			try {
				final JSONObject resp = restTemplate.getForEntity(url, JSONObject.class).getBody();
				if (resp != null && "1".equals(resp.getString("status"))) {
					final List<ScanTx> result = resp.getList("result", ScanTx.class);
					for (ScanTx tx : result) {
						if ("0xb4d6b7df".equals(tx.methodId) || "0x86bb8f37".equals(tx.methodId)) {
							// 最近一笔领取交易，时间距离小于1小时，则算活跃
							return ((System.currentTimeMillis() / 1000) - tx.timeStamp < 3600);
						}
					}
				}
			} catch (Exception e) {
				final String message = e.getMessage();
				log.error("验证委托人地址是否活跃失败：delegateOwner={},startBlockNumber={},url={},message={}", delegateOwner, startBlockNumber, url, message == null ? e : message);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ignored) {
				}
			}
		}
		return false;
	}

	@Setter
	@Getter
	public static class ScanTx {

		public String blockNumber;
		public Long timeStamp;
		public String hash;
		public String nonce;
		public String blockHash;
		public String transactionIndex;
		public String from;
		public String to;
		public String value;
		public String gas;
		public String gasPrice;
		public String gasPriceBid;
		public String isError;
		public String txreceiptStatus;
		public String input;
		public String contractAddress;
		public String cumulativeGasUsed;
		public String gasUsed;
		public String confirmations;
		public String methodId;
		public String functionName;

	}

}