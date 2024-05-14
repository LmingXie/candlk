package com.candlk.webapp.job;

import java.math.*;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.annotation.*;

import com.alibaba.fastjson2.*;
import com.candlk.context.web.Jsons;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.PolyNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;
import org.web3j.protocol.Web3j;

import static com.candlk.webapp.job.PoolInfoVO.parsePercent;

@Slf4j
@Configuration
public class XAIPowerJob {

	@Resource
	private Web3j web3j;
	@Resource
	private Web3JConfig web3JConfig;

	public final static String poolFactoryContractAddress = "0xF9E08660223E2dbb1c0b28c82942aB6B5E38b8E5", PowerCachePath = "/mnt/xai_bot/power.json";

	public final static BigDecimal esXAIWei = new BigDecimal(10000), keysWei = BigDecimal.ONE;

	@Nonnull
	public static PoolInfoVO getPoolInfo(String poolAddress, Web3j web3j, boolean flush) {
		final Map<String, PoolInfoVO> infoMap = readLocalCache();
		try {
			if (flush) {
				final PoolInfoVO poolInfo = PoolInfo.getPoolInfo(web3j, poolAddress).toVO();
				log.info("正在强刷池子信息：poolAddress={}，keyCount={}", poolAddress, poolInfo.keyCount);
				infoMap.put(poolAddress, poolInfo);
				XAIRedemptionJob.serialization(PowerCachePath, JSON.parseObject(Jsons.encode(infoMap)));
			}
		} catch (Exception e) {
			log.error("获取池子信息失败", e);
		}
		return infoMap.get(poolAddress);
	}

	private static final Cache<String, Boolean> activeCaffeine = Caffeine.newBuilder()
			.initialCapacity(256)
			.maximumSize(1024)
			.expireAfterWrite(30, TimeUnit.MINUTES)
			.build();
	private static final String activeCaffeineFile = "/mnt/xai_bot/active.json";

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
			log.error("加载活跃度本地缓存失败！", e);
		}
	}

	public static boolean nonFlushActivePool(PoolInfoVO info, Web3j web3j) {
		return getAndFlushActivePool(info, null, web3j, null, null, false);
	}

	public static final BigInteger weakActiveKeysThreshold = new BigInteger("50");

	public static boolean getAndFlushActivePool(PoolInfoVO info, RestTemplate restTemplate, Web3j web3j, BigInteger endBlockNumber, BigDecimal weakActiveThreshold, boolean flush) {
		if (info.delegateAddress == null) {
			info.getDelegateAddress(web3j, flush);
		}
		final Function<String, @PolyNull Boolean> function = k -> {
			// Arbitrum 平均 0.26s一个区块，1小时 = 14400 区块，这里取最近15000个区块，1小时内未领奖则算不活跃的池子
			BigInteger startBlockNumber = endBlockNumber.subtract(new BigInteger("15000"));
			boolean active = info.hasActivePool(restTemplate, info.getDelegateAddress(web3j, flush), startBlockNumber);
			log.info("刷新池子的活跃度结果：poolAddress={}，active={}，KeyCount={}，weakActiveThreshold={}", info.poolAddress, active, info.keyCount, weakActiveThreshold);
			if (!active && info.keyCount.compareTo(weakActiveKeysThreshold) > 0) { // 针对大池进行弱检测
				active = info.weakActiveCheck(restTemplate, info.poolAddress, startBlockNumber, weakActiveThreshold);
				log.info("针对大池进行活跃度的弱检测结果：poolAddress={}，active={}，KeyCount={}，weakActiveThreshold={}", info.poolAddress, active, info.keyCount, weakActiveThreshold);
			}
			return active;
		};
		return activeCaffeine.get(info.poolAddress, function);
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

	public static Map<String, PoolInfoVO> readLocalCache() {
		try {
			final JSONObject root = XAIRedemptionJob.deserialization(PowerCachePath);
			return root.to(new TypeReference<>() {
			});
		} catch (Exception ignored) {
		}
		return null;
	}

	@Scheduled(cron = "${service.cron.XAIPowerJob:0 0 1 * * ?}")
	public void run() {
		try {
			final BigInteger poolsCount = PoolInfo.getPoolsCount(web3j, poolFactoryContractAddress);
			log.info("正在刷新算力当前总池子数：{}", poolsCount);
			final Map<String, PoolInfoVO> infoMap = readLocalCache();
			final int size = infoMap.size(), newSize = poolsCount.intValue(), offset = newSize - infoMap.size();
			if (offset > 0) {
				for (int i = size; i < newSize; i++) {
					final String poolAddress = PoolInfo.getPoolAddress(web3j, poolFactoryContractAddress, BigInteger.valueOf(i));
					infoMap.put(poolAddress.toLowerCase(), null);
					log.info("发现新池子【{}】", poolAddress);
				}
			}

			final BigInteger endBlockNumber = web3j.ethBlockNumber().send().getBlockNumber();

			for (Map.Entry<String, PoolInfoVO> entry : infoMap.entrySet()) {
				final String poolAddress = entry.getKey();
				final PoolInfoVO oldPoolInfoVO = entry.getValue(), newPoolInfo = PoolInfo.getPoolInfo(web3j, poolAddress).toVO();
				final String oldDelegateAddress = oldPoolInfoVO == null ? null : oldPoolInfoVO.getDelegateAddress();
				newPoolInfo.setDelegateAddress(oldDelegateAddress);
				entry.setValue(newPoolInfo);
				log.info("正在刷新算力【{}】 每1000EsXAI算力 -> {},每1Keys算力 -> {}", poolAddress, newPoolInfo.calcEsXAIPower(esXAIWei), newPoolInfo.calcKeysPower(keysWei));
			}

			final int len = infoMap.size(), topN = web3JConfig.topN > len ? len : web3JConfig.topN;

			final List<PoolInfoVO> esXAIPowerTopN = infoMap.values().stream().sorted((o1, o2) -> o2.calcEsXAIPower(esXAIWei).compareTo(o1.calcEsXAIPower(esXAIWei))).toList();
			final StringBuilder sb = new StringBuilder("### 10000/EsXAI算力排行榜  \n  ");
			sb.append("|  排名  |  池子  |  算力  |  加成   | EsXAI  | Keys  | 活跃  |   \n  ");
			sb.append("|:------:|:------|:-------:|:-----:|:-----:|:-----:|:-----:|  \n  ");
			final StringBuilder tgMsg = new StringBuilder("*\uD83D\uDCB910000/EsXAI Stake Computing Power Rank *\n\n*   Power    Tier   EsXAI      Keys    Active           Reward/Pool* \n");
			for (int i = 1; i <= topN; i++) {
				final PoolInfoVO info = esXAIPowerTopN.get(i - 1);
				// 只刷新排行榜上池子的活跃状态，并更新委托人地址
				final BigDecimal totalStakedAmount = info.parseTotalStakedAmount();
				final String poolName = Web3JConfig.getContractName(info.poolAddress),
						total = totalStakedAmount.movePointLeft(4).setScale(0, RoundingMode.HALF_UP).toPlainString() + "w",
						keyCount = info.keyCount.toString();
				sb.append("| ").append(i)
						.append(" | [").append(outPoolName(poolName)).append("](https://app.xai.games/pool/").append(info.poolAddress).append("/summary)")
						.append(info.getUpdateSharesTimestamp().compareTo(BigInteger.ZERO) > 0 ? "<font color=\"red\">变</font> | " : " | ")
						.append(info.calcEsXAIPower(esXAIWei)).append(" | ")
						.append("×").append(info.calcStakingTier()).append(" | ")
						.append(total).append(" | ")
						.append(keyCount).append(" | ")
						.append(getAndFlushActivePool(info, web3JConfig.proxyRestTemplate, web3j, endBlockNumber, web3JConfig.weakActiveThreshold, true)
								// .append(nonFlushActivePool(info, web3j)
								? "[<font color=\"green\">✔️</font>](https://arbiscan.io/address/" + info.getPoolAddress() + "#tokentxns)"
								: "[<font color=\"red\">✘</font>](https://arbiscan.io/address/" + info.getPoolAddress() + "#tokentxns)"
						)
						.append(" |   \n  ")
				;
				buildTgMsg(tgMsg, i, info, poolName, keyCount, true);
			}
			log.info("EsXAI算力排行榜：{}", sb);
			web3JConfig.sendWarn("EsXAI算力排行榜", sb.toString(), tgMsg.toString());

			final List<PoolInfoVO> keysIPowerTopN = infoMap.values().stream().sorted((o1, o2) -> o2.calcKeysPower(keysWei).compareTo(o1.calcKeysPower(keysWei))).toList();
			sb.setLength(0);
			sb.append("### 1/Keys算力排行榜  \n  ");
			sb.append("|  排名  |   池子   |   算力   |  加成  | 总质押  | 活跃  |   \n  ");
			sb.append("|:------:|:------|:-------:|:-----:|:-----:|:-----:|  \n  ");

			tgMsg.setLength(0);
			tgMsg.append("*\uD83D\uDCB91/Keys Stake Computing Power Rank *\n\n*   Power    Tier   EsXAI      Keys    Active           Reward/Pool* \n");

			for (int i = 1; i <= topN; i++) {
				final PoolInfoVO info = keysIPowerTopN.get(i - 1);
				// 只刷新排行榜上池子的活跃状态，并更新委托人地址
				final String poolName = Web3JConfig.getContractName(info.poolAddress), keyCount = info.keyCount.toString();
				sb.append("| ").append(i)
						.append(" | [").append(outPoolName(poolName)).append("](https://app.xai.games/pool/").append(info.poolAddress).append("/summary)")
						.append(info.getUpdateSharesTimestamp().compareTo(BigInteger.ZERO) > 0 ? "<font color=\"red\">变</font> | " : " | ")
						.append(info.calcKeysPower(keysWei)).append(" | ")
						.append("×").append(info.calcStakingTier()).append(" | ")
						.append(info.keyCount).append(" | ")
						.append(getAndFlushActivePool(info, web3JConfig.proxyRestTemplate, web3j, endBlockNumber, web3JConfig.weakActiveThreshold, true)
								// .append(nonFlushActivePool(info, web3j)
								? "[<font color=\"green\">✔️</font>](https://arbiscan.io/address/" + info.getPoolAddress() + "#tokentxns)"
								: "[<font color=\"red\">✘</font>](https://arbiscan.io/address/" + info.getPoolAddress() + "#tokentxns)")
						.append(" |   \n  ")
				;

				buildTgMsg(tgMsg, i, info, poolName, keyCount, false);
			}
			// 持久化本地缓存文件
			flushActivePoolLocalFile();
			XAIRedemptionJob.serialization(PowerCachePath, JSON.parseObject(Jsons.encode(infoMap)));
			log.info("Keys算力排行榜：{}", sb);
			web3JConfig.sendWarn("Keys算力排行榜", sb.toString(), tgMsg.toString());
		} catch (Exception e) {
			log.error("算力统计异常", e);
		}
	}

	public static String outputActive(boolean active, String poolAddress) {
		return active ? "[✅](https://arbiscan.io/address/" + poolAddress + ")"
				: "[❌](https://arbiscan.io/address/" + poolAddress + "#tokentxns)";
	}

	private void buildTgMsg(StringBuilder tgMsg, int i, PoolInfoVO info, String poolName, String keyCount, boolean esXAIRank) {
		final String total = info.outputExXAI();
		tgMsg.append("*").append(i).append("  ").append(i < 10 ? "  " : "")
				.append("  ").append(esXAIRank ? info.calcEsXAIPower(esXAIWei) : info.calcKeysPower(keysWei))
				.append("* 	   ×").append(info.calcStakingTier())
				.append(" 	   ").append(total).append(total.length() > 4 ? "        " : "          ")
				.append(keyCount).append(keyCount.length() > 2 ? "      " : "        ")
				.append(outputActive(nonFlushActivePool(info, web3j), info.poolAddress)).append("     \\[")
				.append(parsePercent(info.ownerShare)).append("/").append(parsePercent(info.keyBucketShare)).append("/").append(parsePercent(info.stakedBucketShare))
				.append("][")
				.append(poolName.length() > 14 ? poolName.substring(0, 14) : poolName).append("](app.xai.games/pool/").append(info.poolAddress).append("/summary)")
				.append(info.getUpdateSharesTimestamp().compareTo(BigInteger.ZERO) > 0 ? "‼\uFE0F" : "")
				.append("\n")
		;
	}

	private static String outPoolName(String poolName) {
		final String newPoolName = poolName.replaceAll(" ", "").replaceAll("-", "").replaceAll("\\|", "").replaceAll("｜", "");
		final int len = newPoolName.length();
		return newPoolName.substring(0, Math.min(len, 12));
	}

	public static void main(String[] args) throws Exception {
		System.out.println(outPoolName("ALPHA 5 - Franchisee"));
		// System.out.println(new BigDecimal("744229").movePointLeft(4).setScale(0, RoundingMode.HALF_UP));

		// final Web3j web3j1 = Web3j.build(new HttpService("https://arbitrum-one.public.blastapi.io"));
		// initLocalFile(web3j1);
	}

	private static void initLocalFile(Web3j web3j1) throws Exception {
		String poolFactoryContractAddress = "0xF9E08660223E2dbb1c0b28c82942aB6B5E38b8E5";
		final BigInteger poolsCount = PoolInfo.getPoolsCount(web3j1, poolFactoryContractAddress);
		System.out.println("poolsCount = " + poolsCount);

		final Map<String, PoolInfoVO> infoMap = new HashMap<>(poolsCount.intValue());

		final PoolInfoVO poolInfo = PoolInfo.getPoolInfo(web3j1, "0xd471C63C24F5e59aFB5Bf67892A6F3B3dB9C495A").toVO();
		infoMap.put(poolInfo.poolAddress, poolInfo);

		System.out.println(Jsons.encode(poolInfo));
		System.out.println("每1000EsXAI算力：" + poolInfo.calcEsXAIPower(esXAIWei));
		System.out.println("每1Keys算力：" + poolInfo.calcKeysPower(keysWei));
		XAIRedemptionJob.serialization(PowerCachePath, JSON.parseObject(Jsons.encode(infoMap)));
	}

}
