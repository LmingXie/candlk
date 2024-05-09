package com.candlk.webapp.job;

import java.math.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.*;

import com.alibaba.fastjson2.*;
import com.candlk.context.web.Jsons;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Slf4j
@Configuration
public class XAIPowerJob {

	@Resource
	private Web3j web3j;
	@Resource
	private Web3JConfig web3JConfig;

	public final static String poolFactoryContractAddress = "0xF9E08660223E2dbb1c0b28c82942aB6B5E38b8E5",
			file = "/mnt/xai_bot/power.json";

	public final static BigDecimal esXAIWei = new BigDecimal(10000), keysWei = BigDecimal.ONE;
	public final static NumberFormat FORMAT = DecimalFormat.getCurrencyInstance(Locale.US);

	public static PoolInfoVO getPoolInfo(String poolAddress) {
		return getPoolInfo(poolAddress, null, false);
	}

	@Nullable
	public static PoolInfoVO getPoolInfo(String poolAddress, Web3j web3j, boolean flush) {
		try {
			final JSONObject root = XAIRedemptionJob.deserialization(file);
			final Map<String, PoolInfoVO> infoMap = root.to(new TypeReference<>() {
			});
			if (flush) {
				final PoolInfoVO poolInfo = PoolInfo.getPoolInfo(web3j, poolAddress).toVO();
				log.info("正在强刷池子信息：poolAddress={}，keyCount={}", poolAddress, poolInfo.keyCount);
				infoMap.put(poolAddress, poolInfo);
				XAIRedemptionJob.serialization(file, JSON.parseObject(Jsons.encode(infoMap)));
			}
			return infoMap.get(poolAddress);
		} catch (Exception e) {
			log.error("获取池子信息失败", e);
			return null;
		}
	}

	final static RestTemplate restTemplate = new RestTemplate();

	final Cache<String, Boolean> activeCaffeine = Caffeine.newBuilder()
			.initialCapacity(256)
			.maximumSize(1024)
			.expireAfterWrite(60, TimeUnit.MINUTES)
			.build();
	final String activeCaffeineFile = "/mnt/xai_bot/active.json";

	@PostConstruct
	public void init() {
		try {
			// 加载本地缓存
			final JSONObject root = XAIRedemptionJob.deserialization(activeCaffeineFile);
			final HashMap<String, Boolean> localCache = root.to(new TypeReference<>() {
			});
			activeCaffeine.putAll(localCache);
			log.info("加载活跃度本地缓存成功！当前存在【{}】个实例。", localCache.size());
		} catch (Exception e) {
		}
	}

	public boolean getActivePool(PoolInfoVO info, RestTemplate restTemplate, BigInteger startBlockNumber, boolean flush) {
		return activeCaffeine.get(info.poolAddress, k -> info.hasActivePool(restTemplate, info.getDelegateAddress(web3j, flush), startBlockNumber));
	}

	public void flushActivePoolLocalFile() {
		// 刷新本地文件缓存
		final ConcurrentMap<String, Boolean> localCache = activeCaffeine.asMap();
		try {
			XAIRedemptionJob.serialization(activeCaffeineFile, JSON.parseObject(Jsons.encode(localCache)));
		} catch (Exception e) {
		}
		log.info("刷新本地文件缓存成功。当前存在【{}】个实例。", localCache.size());
	}

	@Scheduled(cron = "${service.cron.XAIPowerJob:0 0 1 * * ?}")
	public void run() {
		try {
			final JSONObject root = XAIRedemptionJob.deserialization(file);
			final Map<String, PoolInfoVO> infoMap = root.to(new TypeReference<>() {
			});

			final BigInteger poolsCount = PoolInfo.getPoolsCount(web3j, poolFactoryContractAddress);
			log.info("正在刷新算力当前总池子数：{}", poolsCount);

			final int size = infoMap.size(), newSize = poolsCount.intValue(), offset = newSize - infoMap.size();
			if (offset > 0) {
				for (int i = size; i < newSize; i++) {
					final String poolAddress = PoolInfo.getPoolAddress(web3j, poolFactoryContractAddress, BigInteger.valueOf(i));
					infoMap.put(poolAddress, null);
					log.info("发现新池子【{}】", poolAddress);
				}
			}

			final BigInteger endBlockNumber = web3j.ethBlockNumber().send().getBlockNumber(),
					// Arbitrum 平均 0.26s一个区块，1小时 = 14400 区块，这里取最近15000个区块，1小时内未领奖则算不活跃的池子
					startBlockNumber = endBlockNumber.subtract(new BigInteger("15000"));

			for (Map.Entry<String, PoolInfoVO> entry : infoMap.entrySet()) {
				final String poolAddress = entry.getKey();
				final PoolInfoVO poolInfo = PoolInfo.getPoolInfo(web3j, poolAddress).toVO();
				// 刷新最新的委托人地址地址
				this.getActivePool(poolInfo, restTemplate, startBlockNumber, true);
				entry.setValue(poolInfo);
				log.info("正在刷新算力【{}】 每1000EsXAI算力 -> {},每1Keys算力 -> {}", poolAddress, poolInfo.calcEsXAIPower(esXAIWei), poolInfo.calcKeysPower(BigDecimal.ONE));
			}
			flushActivePoolLocalFile();
			XAIRedemptionJob.serialization(file, JSON.parseObject(Jsons.encode(infoMap)));

			final int len = infoMap.size(), topN = web3JConfig.topN > len ? len : web3JConfig.topN;

			final List<PoolInfoVO> esXAIPowerTopN = infoMap.values().stream().sorted((o1, o2) -> o2.calcEsXAIPower(esXAIWei).compareTo(o1.calcEsXAIPower(esXAIWei))).toList();
			final StringBuilder sb = new StringBuilder("### 10000/EsXAI算力排行榜  \n  ");
			sb.append("|  排名  |  池子  |  算力  |  加成   | 总质押  | 活跃  |   \n  ");
			sb.append("|:------:|:------:|:-------:|:-----:  |  :-----:  |  :-----:  |  \n  ");
			for (int i = 0; i < topN; i++) {
				final PoolInfoVO info = esXAIPowerTopN.get(i);
				final BigDecimal totalStakedAmount = new BigDecimal(info.totalStakedAmount).movePointLeft(18).setScale(0, RoundingMode.HALF_UP);
				final String poolName = Web3JConfig.getContractName(info.poolAddress);
				sb.append("| ").append(i + 1)
						.append(" | [").append(outPoolName(poolName)).append("](https://app.xai.games/pool/").append(info.poolAddress).append("/summary)")
						.append(info.getUpdateSharesTimestamp().compareTo(BigInteger.ZERO) > 0 ? "<font color=\"red\">【变】</font> | " : " | ")
						.append(info.calcEsXAIPower(esXAIWei)).append(" | ")
						.append("×").append(info.calcStakingTier(totalStakedAmount)).append(" | ")
						.append(totalStakedAmount.movePointLeft(4).setScale(0, RoundingMode.HALF_UP)).append("w").append(" | ")
						// .append(this.getActivePool(info, restTemplate, startBlockNumber) ? "<font color=\"common_green1_color\">✔️</font>" : "<font color=\"red\">✘</font>")
						.append(this.getActivePool(info, restTemplate, startBlockNumber, false)
								? "<font color=\"common_green1_color\">[✔️](https://arbiscan.io/address/" + info.getDelegateAddress() + ")</font>"
								: "<font color=\"red\">[✘](https://arbiscan.io/address/" + info.getDelegateAddress() + ")</font>")
						.append(" |   \n  ")
				;
			}
			log.info("EsXAI算力排行榜：{}", sb);
			web3JConfig.sendWarn("EsXAI算力排行榜", sb.toString());

			final List<PoolInfoVO> keysIPowerTopN = infoMap.values().stream().sorted((o1, o2) -> o2.calcKeysPower(keysWei).compareTo(o1.calcKeysPower(keysWei))).toList();
			sb.setLength(0);
			sb.append("### 1/Keys算力排行榜  \n  ");
			sb.append("|  排名  |   池子   |   算力   |  加成  | 总质押  | 活跃  |   \n  ");
			sb.append("|:------:|:------:|:-------:|:-----:  |  :-----:  |  :-----:  |  \n  ");
			for (int i = 0; i < topN; i++) {
				final PoolInfoVO info = keysIPowerTopN.get(i);
				final BigDecimal totalStakedAmount = new BigDecimal(info.totalStakedAmount).movePointLeft(18).setScale(0, RoundingMode.HALF_UP);
				final String poolName = Web3JConfig.getContractName(info.poolAddress);
				sb.append("| ").append(i + 1)
						.append(" | [").append(outPoolName(poolName)).append("](https://app.xai.games/pool/").append(info.poolAddress).append("/summary)")
						.append(info.getUpdateSharesTimestamp().compareTo(BigInteger.ZERO) > 0 ? "<font color=\"red\">【变】</font> | " : " | ")
						.append(info.calcKeysPower(keysWei)).append(" | ")
						.append("×").append(info.calcStakingTier(totalStakedAmount)).append(" | ")
						.append(info.keyCount).append(" | ")
						.append(this.getActivePool(info, restTemplate, startBlockNumber, false)
								? "<font color=\"common_green1_color\">[✔️](https://arbiscan.io/address/" + info.getDelegateAddress() + ")</font>"
								: "<font color=\"red\">[✘](https://arbiscan.io/address/" + info.getDelegateAddress() + ")</font>")
						.append(" |   \n  ")
				;
			}
			log.info("Keys算力排行榜：{}", sb);
			web3JConfig.sendWarn("Keys算力排行榜", sb.toString());
		} catch (Exception e) {
			log.error("算力统计异常", e);
		}
	}

	private String outPoolName(String poolName) {
		final String newPoolName = poolName.replaceAll(" ", "").replaceAll("-", "").replaceAll("\\|", "").replaceAll("｜", "");
		return newPoolName.substring(0, Math.min(newPoolName.length(), 10));
	}

	public static void main(String[] args) throws Exception {
		// System.out.println(new BigDecimal("744229").movePointLeft(4).setScale(0, RoundingMode.HALF_UP));
		final Web3j web3j1 = Web3j.build(new HttpService("https://arbitrum-one.public.blastapi.io"));
		String poolFactoryContractAddress = "0xF9E08660223E2dbb1c0b28c82942aB6B5E38b8E5";

		final BigInteger poolsCount = PoolInfo.getPoolsCount(web3j1, poolFactoryContractAddress);
		System.out.println("poolsCount = " + poolsCount);

		final Map<String, PoolInfoVO> infoMap = new HashMap<>(poolsCount.intValue());

		// for (BigInteger i = BigInteger.ZERO; i.compareTo(poolsCount) < 0; i = i.add(BigInteger.ONE)) {
		// 	String poolAddress = PoolInfo.getPoolAddress(web3j1, poolFactoryContractAddress, i);
		// 	infoMap.put(poolAddress, PoolInfo.getPoolInfo(web3j1, poolAddress));
		// 	System.out.println("poolAddress = " + poolAddress);
		// }

		final PoolInfoVO poolInfo = PoolInfo.getPoolInfo(web3j1, "0xd471C63C24F5e59aFB5Bf67892A6F3B3dB9C495A").toVO();
		infoMap.put(poolInfo.poolAddress, poolInfo);

		// final PoolInfo poolInfo2 = PoolInfo.getPoolInfo(web3j1, "0x499D227EaC69C5abB22f638721661D4b2fA19C7C");
		// infoMap.put(poolInfo2.poolAddress, poolInfo2.toVO());
		//
		// for (PoolInfoVO info : infoMap.values()) {
		// 	System.out.println(info.poolAddress + " -> " + info.calcEsXAIPower(esXAIWei));
		// 	System.out.println(info.poolAddress + " -> " + info.calcKeysPower(keysWei));
		// }
		//
		// System.out.println("EsXAI 算力排名");
		// List<PoolInfoVO> collect = infoMap.values().stream().sorted((o1, o2) -> o2.calcEsXAIPower(esXAIWei).compareTo(o1.calcEsXAIPower(esXAIWei))).toList();
		// for (PoolInfoVO info : collect) {
		// 	System.out.println(info.poolAddress + " -> " + info.calcEsXAIPower(esXAIWei));
		// }
		//
		// System.out.println("Keys 算力排名");
		// List<PoolInfoVO> keys = infoMap.values().stream().sorted((o1, o2) -> o2.calcKeysPower(keysWei).compareTo(o1.calcKeysPower(keysWei))).toList();
		// for (PoolInfoVO info : keys) {
		// 	System.out.println(info.poolAddress + " -> " + info.calcKeysPower(keysWei));
		// }

		System.out.println(Jsons.encode(poolInfo));
		System.out.println("每1000EsXAI算力：" + poolInfo.calcEsXAIPower(esXAIWei));
		System.out.println("每1Keys算力：" + poolInfo.calcKeysPower(BigDecimal.ONE));
		XAIRedemptionJob.serialization(file, JSON.parseObject(Jsons.encode(infoMap)));
	}

}
