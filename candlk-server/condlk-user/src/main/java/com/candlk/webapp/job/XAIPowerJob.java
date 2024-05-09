package com.candlk.webapp.job;

import java.math.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import javax.annotation.Nullable;
import javax.annotation.Resource;

import com.alibaba.fastjson2.*;
import com.candlk.context.web.Jsons;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.web3j.protocol.Web3j;

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

			for (Map.Entry<String, PoolInfoVO> entry : infoMap.entrySet()) {
				final String poolAddress = entry.getKey();
				final PoolInfoVO poolInfo = PoolInfo.getPoolInfo(web3j, poolAddress).toVO();
				entry.setValue(poolInfo);
				log.info("正在刷新算力【{}】 每1000EsXAI算力 -> {},每1Keys算力 -> {}", poolAddress, poolInfo.calcEsXAIPower(esXAIWei), poolInfo.calcKeysPower(BigDecimal.ONE));
			}
			XAIRedemptionJob.serialization(file, JSON.parseObject(Jsons.encode(infoMap)));

			final int len = infoMap.size(), topN = web3JConfig.topN > len ? len : web3JConfig.topN;

			final List<PoolInfoVO> esXAIPowerTopN = infoMap.values().stream().sorted((o1, o2) -> o2.calcEsXAIPower(esXAIWei).compareTo(o1.calcEsXAIPower(esXAIWei))).toList();
			final StringBuilder sb = new StringBuilder("### 10000/EsXAI算力排行榜  \n  ");
			sb.append("|  排名  |  池子  |  算力  |  加成   | 总质押  |   \n  ");
			sb.append("|:------:|:------:|:-------:|:-----:  |  :-----:  |  \n  ");
			for (int i = 0; i < topN; i++) {
				final PoolInfoVO info = esXAIPowerTopN.get(i);
				final BigDecimal totalStakedAmount = new BigDecimal(info.totalStakedAmount).movePointLeft(18).setScale(0, RoundingMode.HALF_UP);
				final String poolName = Web3JConfig.getContractName(info.poolAddress);
				sb.append("| ").append(i + 1)
						.append(" | [").append(poolName.replaceAll("\\|", "\\\\|")).append("](https://app.xai.games/pool/").append(info.poolAddress).append("/summary)")
						.append(info.getUpdateSharesTimestamp().compareTo(BigInteger.ZERO) > 0 ? "<font color=\"red\">【变】</font> | " : " | ")
						.append(info.calcEsXAIPower(esXAIWei)).append(" | ")
						.append("×").append(info.calcStakingTier(totalStakedAmount)).append(" | ")
						.append(totalStakedAmount.movePointLeft(4).setScale(0, RoundingMode.HALF_UP)).append("w |   \n  ")
				;
			}
			log.info("EsXAI算力排行榜：{}", sb);
			web3JConfig.sendWarn("EsXAI算力排行榜", sb.toString());

			final List<PoolInfoVO> keysIPowerTopN = infoMap.values().stream().sorted((o1, o2) -> o2.calcKeysPower(keysWei).compareTo(o1.calcKeysPower(keysWei))).toList();
			sb.setLength(0);
			sb.append("### 1/Keys算力排行榜  \n  ");
			sb.append("|  排名  |   池子   |   算力   |  加成  | 总质押  |   \n  ");
			sb.append("|:------:|:------:|:-------:|:-----:  |  :-----:  |  \n  ");
			for (int i = 0; i < topN; i++) {
				final PoolInfoVO info = keysIPowerTopN.get(i);
				final BigDecimal totalStakedAmount = new BigDecimal(info.totalStakedAmount).movePointLeft(18).setScale(0, RoundingMode.HALF_UP);
				final String poolName = Web3JConfig.getContractName(info.poolAddress);
				sb.append("| ").append(i + 1)
						.append(" | [").append(poolName.replaceAll("\\|", "\\\\|")).append("](https://app.xai.games/pool/").append(info.poolAddress).append("/summary)")
						.append(info.getUpdateSharesTimestamp().compareTo(BigInteger.ZERO) > 0 ? "<font color=\"red\">【变】</font> | " : " | ")
						.append(info.calcKeysPower(keysWei)).append(" | ")
						.append("×").append(info.calcStakingTier(totalStakedAmount)).append(" | ")
						.append(info.keyCount).append(" |   \n  ")
				;
			}
			log.info("Keys算力排行榜：{}", sb);
			web3JConfig.sendWarn("Keys算力排行榜", sb.toString());
		} catch (Exception e) {
			log.error("算力统计异常", e);
		}
	}

	public static void main(String[] args) throws Exception {
		System.out.println(new BigDecimal("744229").movePointLeft(4).setScale(0, RoundingMode.HALF_UP));
		// final Web3j web3j1 = Web3j.build(new HttpService("https://arbitrum.llamarpc.com"));
		// String poolFactoryContractAddress = "0xF9E08660223E2dbb1c0b28c82942aB6B5E38b8E5";
		//
		// final BigInteger poolsCount = PoolInfo.getPoolsCount(web3j1, poolFactoryContractAddress);
		// System.out.println("poolsCount = " + poolsCount);
		//
		// final Map<String, PoolInfoVO> infoMap = new HashMap<>(poolsCount.intValue());
		//
		// // for (BigInteger i = BigInteger.ZERO; i.compareTo(poolsCount) < 0; i = i.add(BigInteger.ONE)) {
		// // 	String poolAddress = PoolInfo.getPoolAddress(web3j1, poolFactoryContractAddress, i);
		// // 	infoMap.put(poolAddress, PoolInfo.getPoolInfo(web3j1, poolAddress));
		// // 	System.out.println("poolAddress = " + poolAddress);
		// // }
		//
		// final PoolInfoVO poolInfo = PoolInfo.getPoolInfo(web3j1, "0xd471C63C24F5e59aFB5Bf67892A6F3B3dB9C495A").toVO();
		// infoMap.put(poolInfo.poolAddress, poolInfo);
		//
		// // final PoolInfo poolInfo2 = PoolInfo.getPoolInfo(web3j1, "0x499D227EaC69C5abB22f638721661D4b2fA19C7C");
		// // infoMap.put(poolInfo2.poolAddress, poolInfo2.toVO());
		// //
		// // for (PoolInfoVO info : infoMap.values()) {
		// // 	System.out.println(info.poolAddress + " -> " + info.calcEsXAIPower(esXAIWei));
		// // 	System.out.println(info.poolAddress + " -> " + info.calcKeysPower(keysWei));
		// // }
		// //
		// // System.out.println("EsXAI 算力排名");
		// // List<PoolInfoVO> collect = infoMap.values().stream().sorted((o1, o2) -> o2.calcEsXAIPower(esXAIWei).compareTo(o1.calcEsXAIPower(esXAIWei))).toList();
		// // for (PoolInfoVO info : collect) {
		// // 	System.out.println(info.poolAddress + " -> " + info.calcEsXAIPower(esXAIWei));
		// // }
		// //
		// // System.out.println("Keys 算力排名");
		// // List<PoolInfoVO> keys = infoMap.values().stream().sorted((o1, o2) -> o2.calcKeysPower(keysWei).compareTo(o1.calcKeysPower(keysWei))).toList();
		// // for (PoolInfoVO info : keys) {
		// // 	System.out.println(info.poolAddress + " -> " + info.calcKeysPower(keysWei));
		// // }
		//
		// System.out.println(Jsons.encode(poolInfo));
		// System.out.println("每1000EsXAI算力：" + poolInfo.calcEsXAIPower(esXAIWei));
		// System.out.println("每1Keys算力：" + poolInfo.calcKeysPower(BigDecimal.ONE));
		// // XAIRedemptionJob.serialization(file, JSON.parseObject(Jsons.encode(infoMap)));
	}

}
