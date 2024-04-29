package com.candlk.webapp.job;

import java.math.*;

import lombok.Getter;
import lombok.Setter;
import org.web3j.abi.datatypes.StaticStruct;

@Getter
@Setter
public class PoolInfoVO extends StaticStruct {

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
			return BigDecimal.ZERO;
		}
		final BigDecimal esXAIPoolTotalPower = new BigDecimal(keyCount).multiply(tier)
				.multiply(new BigDecimal(stakedBucketShare).divide(percent, 18, RoundingMode.HALF_UP));
		return (wei.divide(totalStakedAmount, 18, RoundingMode.HALF_UP)).multiply(esXAIPoolTotalPower).setScale(2, RoundingMode.HALF_UP);
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
			return BigDecimal.ZERO;
		}
		final BigDecimal keyCount = new BigDecimal(this.keyCount);
		final BigDecimal keysPoolTotalPower = keyCount.multiply(calcStakingTier(totalStakedAmount))
				.multiply(new BigDecimal(keyBucketShare).divide(percent, 18, RoundingMode.HALF_UP));
		return wei.divide(keyCount, 18, RoundingMode.HALF_UP)
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
		return tier;
	}

}