package com.candlk.webapp.job;

import java.math.*;
import java.util.*;
import java.util.function.Function;
import javax.annotation.Resource;

import com.alibaba.fastjson2.*;
import com.candlk.common.util.Formats;
import com.candlk.common.util.SpringUtil;
import com.candlk.context.web.Jsons;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.EasyDate;
import me.codeplayer.util.X;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;
import org.web3j.abi.datatypes.Address;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionResult;

import static com.candlk.webapp.job.PoolInfoVO.parsePercent;
import static com.candlk.webapp.job.Web3JConfig.METHOD2TIP;
import static com.candlk.webapp.job.Web3JConfig.getContractName;
import static com.candlk.webapp.job.XAIPowerJob.*;

@Slf4j
@Configuration
public class XAIScanJob {

	@Resource
	private Web3j web3j;
	@Resource
	private Web3JConfig web3JConfig;
	@Resource
	XAIRedemptionJob xaiRedemptionJob;

	public static Map<String, Map<Integer, BigInteger>> yieldStatCache;
	public static final Function<String, Map<Integer, BigInteger>> yieldStatBuilder = k -> new HashMap<>();
	private static final String yieldStatFile = "/mnt/xai_bot/yieldStatCache";
	public static final BigInteger pow18 = BigInteger.TEN.pow(18), SecondsDaily = BigInteger.valueOf(24 * 60 * 60L);

	public static synchronized Map<String, Map<Integer, BigInteger>> getYieldStatCache() {
		if (yieldStatCache == null) {
			synchronized (yieldStatFile) {
				try {
					final JSONObject cache = XAIRedemptionJob.deserialization(yieldStatFile);
					yieldStatCache = cache == null ? new HashMap<>() : cache.to(new TypeReference<>() {
					});
					log.info("加载【产量统计】本地缓存成功！当前存在【{}】个实例。", yieldStatCache.size());
				} catch (Exception e) {
					log.error("加载【产量统计】本地缓存失败！", e);
				}
			}
		}
		return yieldStatCache;
	}

	public static synchronized void flushYieldStatLocalFile() {
		synchronized (yieldStatFile) {
			// 刷新本地文件缓存
			try {
				XAIRedemptionJob.serialization(yieldStatFile, JSON.parseObject(Jsons.encode(yieldStatCache)));
			} catch (Exception ignored) {
			}
			log.info("【产量统计】刷新本地文件缓存成功。当前存在【{}】个实例。", yieldStatCache.size());
		}
	}

	@Scheduled(cron = "${service.cron.XAIScanJob:0/5 * * * * ?}")
	public void run() throws Exception {
		getYieldStatCache();
		final BigInteger lastBlock = web3j.ethBlockNumber().send().getBlockNumber();
		while (lastBlock.compareTo(web3JConfig.lastBlock) > 0) {
			final BigInteger blockNumber = web3JConfig.incrLastBlock();
			SpringUtil.asyncRun(() -> {
				int retry = 10;
				while (retry-- > 0) {
					try {
						final Web3j newWeb3j = web3JConfig.pollingGetWeb3j();
						this.exec(newWeb3j, blockNumber, lastBlock);
						break;
					} catch (Exception e) {
						log.info("接口被限制，进行重试");
					}
				}
			});
		}
		log.info("结束本次扫描，最后区块：{}", web3JConfig.lastBlock);
	}

	public TransactionReceipt getTransactionReceipt(String hash) {
		int retry = 20;
		TransactionReceipt receipt = null;
		while (retry-- > 0) {
			try {
				final Web3j newWeb3j = web3JConfig.pollingGetWeb3j();
				receipt = newWeb3j.ethGetTransactionReceipt(hash).send().getTransactionReceipt().get();
				break;
			} catch (Exception e) {
				log.warn("查询交易详情接口被限制，进行重试");
			}
		}
		if (receipt == null) {
			log.warn("查询交易详情接口失败：{}", hash);
		}
		return receipt;
	}

	public static final BigInteger MAX_KEYS_CAPACITY = new BigInteger("1000");

	private void exec(Web3j newWeb3j, BigInteger blockNumber, BigInteger lastBlock) throws Exception {
		final EthBlock.Block block = newWeb3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(blockNumber), true).send().getBlock();
		log.info("正在执行扫描区块：{}", blockNumber);
		final List<TransactionResult> txs = block.getTransactions();
		if (!CollectionUtils.isEmpty(txs)) {
			final EasyDate now = new EasyDate();
			final int yyyyMMdd = Formats.getYyyyMMdd(now);
			for (TransactionResult txR : txs) {
				final TransactionObject tx = (TransactionObject) txR.get();
				final Transaction info = tx.get();
				final String from = info.getFrom(), to = info.getTo(), hash = info.getHash(), input = info.getInput(),
						nickname = web3JConfig.spyFroms.get(from.toLowerCase()),
						method = StringUtils.length(input) > 10 ? input.substring(0, 10) : null;

				if (method != null && (method.equals("0xb4d6b7df")/*池子批量领取奖励*/ || method.equals("0x86bb8f37")/*池子领取单个奖励*/)) {
					final TransactionReceipt receipt = getTransactionReceipt(hash);
					final List<Log> logs = receipt.getLogs();
					for (Log l : logs) {
						final List<String> topics = l.getTopics();
						if (topics.size() == 3 && l.getAddress().equalsIgnoreCase("0x4C749d097832DE2FEcc989ce18fDc5f1BD76700c")) {
							final String poolContractAddress = new Address(topics.get(2)).getValue();
							final BigInteger reward = new BigInteger(l.getData().substring(2), 16);
							// 分池子 分天统计池子的实际产出
							syncUpdate(poolContractAddress, yyyyMMdd, reward);
							log.info("扫描到池子领取奖励。poolContractAddress={}，reward={}，hash={}", poolContractAddress, reward, hash);
							break;
						}
					}
					continue;
				}

				// Keys 质押或赎回成功
				if (method != null && (method.equals("0x2f1a0b1c") || method.equals("0x95003265"))) {
					final String poolContractAddress = new Address(input.substring(11, 74)).getValue(), poolName = getContractName(poolContractAddress);
					final PoolInfoVO poolInfo = XAIPowerJob.getPoolInfo(poolContractAddress, newWeb3j, true);

					// 算力大于阈值时触发提醒
					final BigDecimal power;
					if (poolInfo.keyCount.compareTo(MAX_KEYS_CAPACITY) < 0 && (power = poolInfo.calcKeysPower(BigDecimal.ONE)).compareTo(web3JConfig.unstakeKeysThreshold) >= 0) {
						final String opType = method.equals("0x2f1a0b1c") ? "质押" : "赎回", opTypeEn = method.equals("0x2f1a0b1c") ? "质押" : "赎回";
						web3JConfig.sendWarn("通知：满Keys池" + opType + "提醒",
								"### 通知：满Keys池" + opType + "提醒！  \n  "
										+ "顶级池【<font color=\"red\">**[" + poolName + "](https://app.xai.games/pool/" + poolContractAddress + "/summary)**</font>】存在空闲质押空间。  \n  "
										+ "当前Keys：**<font color=\"red\">" + poolInfo.keyCount + "</font>**  \n  "
										+ "当前EsXAI：**" + XAIRedemptionJob.formatAmount(new BigDecimal(poolInfo.totalStakedAmount)
										.movePointLeft(18).setScale(2, RoundingMode.HALF_UP)) + "**  \n  "
										+ "加成：**×" + poolInfo.calcStakingTier() + "**  \n  "
										+ "算力值：**" + power + "**  \n  "
										+ "[点击前往查看详情](https://arbiscan.io/tx/" + hash + ")",

								"\uD83D\uDCAF*||通知||：满Keys池 " + opTypeEn + "! *\n\n"
										+ "满Keys池 [" + poolName + "](app.xai.games/pool/" + poolContractAddress + "/summary) 存在空闲质押空间。 \n"
										+ "*加成：×" + poolInfo.calcStakingTier() + "* \n"
										+ "*Keys总质押：" + poolInfo.keyCount + "* \n"
										+ "*Keys算力：" + poolInfo.calcKeysPower(keysWei) + "* \n"
										+ "esXAI算力：" + poolInfo.calcEsXAIPower(esXAIWei) + " \n"
										+ "esXAI总质押：" + poolInfo.outputExXAI() + " \n "
										+ "*活跃状态：*" + outputActive(getAndFlushActivePool(poolInfo, web3JConfig.proxyRestTemplate, newWeb3j, lastBlock, web3JConfig.weakActiveThreshold, true), poolInfo.poolAddress) + " \n"
										+ "[\uD83D\uDC49\uD83D\uDC49前往查看详情](https://arbiscan.io/tx/" + hash + ")"
						);
					}
				}
				if (nickname != null) {
					final String[] msg = METHOD2TIP.getOrDefault(method, METHOD2TIP.get(null))
							.apply(new Object[] { from, to, hash, input, nickname, method, web3JConfig.esXAIThreshold, web3JConfig.keysThreshold, lastBlock });
					if (msg != null && msg[1] != null) {
						web3JConfig.sendWarn(msg[0], msg[1], msg[2]);
					}
				}
				// 只识别大额质押与赎回
				if ("0xf9e08660223e2dbb1c0b28c82942ab6b5e38b8e5".equalsIgnoreCase(to)) {
					final Function<Object[], String[]> function = METHOD2TIP.get(method);
					if (function != null) {
						final String[] msg = function.apply(new Object[] { from, to, hash, input, from, method, web3JConfig.esXAIThreshold, web3JConfig.keysThreshold, lastBlock, "1" });
						X.use(msg, m -> {
							if (msg[1] != null) {
								web3JConfig.sendWarn(msg[0], msg[1], msg[2]);
							} else {
								log.info("识别到普通调用 -> {}：from={},to={},hash={}", msg[0], from, to, hash);
							}
						});
					} else {
						log.info("无法识别的调用：from={},to={},hash={}", from, to, hash);
					}
				}
				// esXAI 合约监控
				if ("0x4c749d097832de2fecc989ce18fdc5f1bd76700c".equalsIgnoreCase(to)) {
					if ("0x840ecba0".equalsIgnoreCase(method)) { // XAI大额赎回的领取
						SpringUtil.asyncRun(() -> {
							final TransactionReceipt receipt = getTransactionReceipt(hash);
							final List<Log> logs = receipt.getLogs();
							if (logs.size() == 4) {
								final Log redemption = logs.get(1), recycle = logs.get(2);
								final BigDecimal redemptionAmount = new BigDecimal(new BigInteger(redemption.getData().substring(2), 16))
										.movePointLeft(18).setScale(2, RoundingMode.HALF_UP),
										recycleAmount = new BigDecimal(new BigInteger(recycle.getData().substring(2), 16))
												.movePointLeft(18).setScale(2, RoundingMode.HALF_UP);

								if (redemptionAmount.compareTo(web3JConfig.redemptionThreshold) >= 0) {
									final BigDecimal totalAmount = new BigDecimal(new BigInteger(logs.get(0).getData().substring(2), 16)).movePointLeft(18);
									final String redemptionTo = redemption.getTopics().get(2);
									web3JConfig.sendWarn("大宗交易：XAI大额赎回成功",
											"### 大宗交易：XAI大额赎回成功！  \n  "
													+ "识别到【**" + this.getPeriod(redemptionAmount, totalAmount) + "**】天期限的赎回领取事件。  \n  "
													+ "赎回数量：**" + redemptionAmount + " XAI**  \n  "
													+ "销毁数量：**" + recycleAmount + " XAI**  \n  "
													+ "赎回地址：**" + redemptionTo.replaceAll("0x000000000000000000000000", "0x") + "**  \n  "
													+ "[点击前往查看详情](https://arbiscan.io/tx/" + hash + ")",

											"\uD83D\uDE80*大宗交易：XAI 大额赎回成功 !* \n\n"
													+ "识别到*【" + this.getPeriod(redemptionAmount, totalAmount) + "】*天大额XAI赎回交易.  \n"
													+ "赎回数量：*" + redemptionAmount + " XAI*  \n"
													+ "销毁数量：*" + recycleAmount + " XAI*  \n"
													+ "地址：*" + redemptionTo.replaceAll("0x000000000000000000000000", "0x") + "*  \n"
													+ "[\uD83D\uDC49\uD83D\uDC49前往查看详情](https://arbiscan.io/tx/" + hash + ")");
								}

								// 汇总统计
								xaiRedemptionJob.incrStat(redemptionAmount, recycleAmount);
							}
						});
					} else if ("0x1ef306e3".equalsIgnoreCase(method)) { // XAI大额赎回
						if (input.length() == 138) {
							final BigInteger amount = new BigInteger(input.substring(11, 74), 16).divide(pow18);
							if (amount.compareTo(web3JConfig.startRedemptionThreshold) >= 0) {
								final BigInteger duration = new BigInteger(input.substring(74), 16).divide(SecondsDaily);
								final BigDecimal amountBig = new BigDecimal(amount), redemptionAmount = amountBig.multiply(duration.compareTo(PERIOD15D) == 0
										? PERIOD15 : duration.compareTo(PERIOD90D) == 0 ? PERIOD90 : PERIOD180).setScale(0, RoundingMode.DOWN);
								final BigDecimal recycleAmount = amountBig.subtract(redemptionAmount).setScale(0, RoundingMode.DOWN);
								final int day = duration.intValue();
								final String dateTimeString = now.addDay(day).toDateTimeString();
								now.addDay(-day);
								web3JConfig.sendWarn("预警：大额XAI赎回开始",
										"### ⛑⛑预警：大额XAI赎回开始！  \n  "
												+ "识别到【**" + duration + "**】天期限的XAI大额赎回开始。  \n  "
												+ "赎回数量：**" + redemptionAmount + " XAI**  \n  "
												+ "销毁数量：**" + recycleAmount + " XAI**  \n  "
												+ "到期时间：**" + dateTimeString + "**  \n  "
												+ "赎回地址：**[" + from + "](https://arbiscan.io/address/" + from + ")**  \n  "
												+ "[点击前往查看详情](https://arbiscan.io/tx/" + hash + ")",

										"⛑⛑*预警：大额XAI赎回开始！* \n\n"
												+ "识别到 *【" + duration + "】*天期限的XAI大额赎回开始。  \n"
												+ "赎回地址：[" + from + "](https://arbiscan.io/address/" + from + ")  \n"
												+ "赎回数量：*" + redemptionAmount + " XAI*  \n"
												+ "销毁数量：*" + recycleAmount + " XAI*  \n"
												+ "到期时间：*" + dateTimeString + "*  \n"
												+ "[\uD83D\uDC49\uD83D\uDC49点击前往查看详情](https://arbiscan.io/tx/" + hash + ")");
							}
						}
					}
				}
			}
		}
	}

	private static synchronized void syncUpdate(String poolContractAddress, int yyyyMMdd, BigInteger reward) {
		getYieldStatCache().computeIfAbsent(poolContractAddress, yieldStatBuilder).merge(yyyyMMdd, reward, BigInteger::add);
		flushYieldStatLocalFile();
	}

	private final static BigDecimal PERIOD15 = new BigDecimal("0.25"), PERIOD90 = new BigDecimal("0.625"), PERIOD180 = BigDecimal.ONE;

	public final static BigInteger PERIOD15D = BigInteger.valueOf(15L), PERIOD90D = BigInteger.valueOf(90L), PERIOD180D = BigInteger.valueOf(180L);

	private String getPeriod(BigDecimal redemptionAmount, BigDecimal totalAmount) {
		final BigDecimal div = redemptionAmount.divide(totalAmount);
		if (div.compareTo(PERIOD15) == 0) {
			return "15（25%）";
		} else if (div.compareTo(PERIOD90) == 0) {
			return "90（62.5%）";
		} else if (div.compareTo(PERIOD180) == 0) {
			return "180（100%）";
		}
		return "无法识别的期限" + div;
	}

	public static String[] yieldRank(Map<String, PoolInfoVO> infoMap, int topN, int len, BigDecimal wei) {
		final EasyDate now = new EasyDate();
		final Map<String, Map<Integer, BigInteger>> yieldStat = getYieldStatCache();
		// 必须先计算产出
		for (Map.Entry<String, PoolInfoVO> entry : infoMap.entrySet()) {
			entry.getValue().calcYield(yieldStat, now, len);
		}

		String[] result = new String[2];
		List<PoolInfoVO> esXAIPowerTopN = infoMap.values().stream().sorted((o1, o2) -> o2.calcKeysYield().compareTo(o1.calcKeysYield())).toList();
		StringBuilder tgMsg = new StringBuilder("*\uD83D\uDCB9 Keys 【").append(len).append("】日平均产出排行榜 *\n\n");
		tgMsg.append("*     1Keys时产     总产出        配比 / 池子 / 变动 * \n");

		for (int i = 1; i <= topN; i++) {
			final PoolInfoVO info = esXAIPowerTopN.get(i - 1);
			final String poolName = Web3JConfig.getContractName(info.poolAddress);
			final long updateSharesTimestamp = info.getUpdateSharesTimestamp().longValue();
			final boolean hasUpdateSharesTimestamp = info.hasUpdateSharesTimestamp();

			final String yieldStr = info.yield.setScale(0, RoundingMode.DOWN).toPlainString(),
					hourKeyYieldStr = info.keysYield.toPlainString();
			tgMsg.append("*").append(i).append("    ").append(i < 10 ? "  *" : "*")
					// Keys时产
					.append(hourKeyYieldStr).append(switch (hourKeyYieldStr.length()) {
						case 5 -> "           ";
						case 6 -> "         ";
						case 7 -> "      ";
						default -> " 	  ";
					})
					// 单位时间总产出
					.append("[").append(yieldStr).append("](https://arbiscan.io/address/").append(info.getPoolAddress()).append("#tokentxns)")
					.append(switch (yieldStr.length()) {
						case 2 -> "           ";
						case 3 -> "         ";
						case 4 -> "       ";
						case 5 -> "     ";
						case 6 -> "    ";
						default -> " 	  ";
					})
					.append("     \\[").append(parsePercent(info.ownerShare)).append("/").append(parsePercent(info.keyBucketShare))
					.append("/").append(parsePercent(info.stakedBucketShare)).append("]").append(" [")
					.append(poolName.length() > 13 ? poolName.substring(0, 13) : poolName).append("](app.xai.games/pool/")
					.append(info.poolAddress).append("/summary)")
					.append(hasUpdateSharesTimestamp ? "‼\uFE0F" + new EasyDate(updateSharesTimestamp * 1000).toDateTimeString().replaceAll("2024-", "") : "")
					.append("\n");
		}
		result[0] = tgMsg.toString();

		esXAIPowerTopN = infoMap.values().stream().sorted((o1, o2) -> o2.calcEsXAIYield(wei).compareTo(o1.calcEsXAIYield(wei))).toList();
		tgMsg.setLength(0);
		tgMsg.append("*\uD83D\uDCB9 10K EsXAI【").append(len).append("】日平均产出排行榜 *\n\n");
		tgMsg.append("*     10K时产     总产出        配比 / 池子 / 变动 * \n");

		for (int i = 1; i <= topN; i++) {
			final PoolInfoVO info = esXAIPowerTopN.get(i - 1);
			final String poolName = Web3JConfig.getContractName(info.poolAddress);
			final long updateSharesTimestamp = info.getUpdateSharesTimestamp().longValue();
			final boolean hasUpdateSharesTimestamp = info.hasUpdateSharesTimestamp();

			final String yieldStr = info.yield.setScale(0, RoundingMode.DOWN).toPlainString(),
					hourEsXAIYieldStr = info.esXAIYield.toPlainString();
			tgMsg.append("*").append(i).append("    ").append(i < 10 ? "  *" : "*")
					// 10K EsXAI 试产
					.append(hourEsXAIYieldStr).append(switch (hourEsXAIYieldStr.length()) {
						case 5 -> "           ";
						case 6 -> "         ";
						case 7 -> "      ";
						default -> " 	  ";
					})
					// 单位时间总产出
					.append("[").append(yieldStr).append("](https://arbiscan.io/address/").append(info.getPoolAddress()).append("#tokentxns)")
					.append(switch (yieldStr.length()) {
						case 2 -> "           ";
						case 3 -> "         ";
						case 4 -> "       ";
						case 5 -> "     ";
						case 6 -> "    ";
						default -> " 	  ";
					})
					.append("     \\[").append(parsePercent(info.ownerShare)).append("/").append(parsePercent(info.keyBucketShare))
					.append("/").append(parsePercent(info.stakedBucketShare)).append("]").append(" [")
					.append(poolName.length() > 13 ? poolName.substring(0, 13) : poolName).append("](app.xai.games/pool/")
					.append(info.poolAddress).append("/summary)")
					.append(hasUpdateSharesTimestamp ? "‼\uFE0F" + new EasyDate(updateSharesTimestamp * 1000).toDateTimeString().replaceAll("2024-", "") : "")
					.append("\n");
		}
		result[1] = tgMsg.toString();
		return result;
	}

}
