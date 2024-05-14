package com.candlk.webapp.job;

import java.math.*;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Resource;

import com.candlk.common.util.SpringUtil;
import lombok.extern.slf4j.Slf4j;
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

import static com.candlk.webapp.job.Web3JConfig.METHOD2TIP;
import static com.candlk.webapp.job.Web3JConfig.getContractName;
import static com.candlk.webapp.job.XAIPowerJob.*;
import static com.candlk.webapp.job.XAIPowerJob.getAndFlushActivePool;

@Slf4j
@Configuration
public class XAIScanJob {

	@Resource
	private Web3j web3j;
	@Resource
	private Web3JConfig web3JConfig;
	@Resource
	XAIRedemptionJob xaiRedemptionJob;

	@Scheduled(cron = "${service.cron.XAIScanJob:0/5 * * * * ?}")
	public void run() throws Exception {
		final BigInteger lastBlock = web3j.ethBlockNumber().send().getBlockNumber();
		while (lastBlock.compareTo(web3JConfig.lastBlock) > 0) {
			final BigInteger blockNumber = web3JConfig.incrLastBlock();
			SpringUtil.asyncRun(() -> {
				int retry = 5;
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

	public static final BigInteger MAX_KEYS_CAPACITY = new BigInteger("750");

	private void exec(Web3j newWeb3j, BigInteger blockNumber, BigInteger lastBlock) throws Exception {
		final EthBlock.Block block = newWeb3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(blockNumber), true).send().getBlock();
		log.info("正在执行扫描区块：{}", blockNumber);
		final List<TransactionResult> txs = block.getTransactions();
		if (!CollectionUtils.isEmpty(txs)) {
			for (TransactionResult txR : txs) {
				final TransactionObject tx = (TransactionObject) txR.get();
				final Transaction info = tx.get();
				final String from = info.getFrom(), to = info.getTo(), hash = info.getHash(), input = info.getInput(),
						nickname = web3JConfig.spyFroms.get(from.toLowerCase()),
						method = StringUtils.length(input) > 10 ? input.substring(0, 10) : null;

				// Keys 质押或赎回成功
				if (method != null && (method.equals("0x2f1a0b1c") || method.equals("0x95003265"))) {
					final String poolContractAddress = new Address(input.substring(11, 74)).getValue(), poolName = getContractName(poolContractAddress);
					final PoolInfoVO poolInfo = XAIPowerJob.getPoolInfo(poolContractAddress, newWeb3j, true);

					// 算力大于阈值时触发提醒
					final BigDecimal power;
					if (poolInfo.keyCount.compareTo(MAX_KEYS_CAPACITY) < 0 && (power = poolInfo.calcKeysPower(BigDecimal.ONE)).compareTo(web3JConfig.unstakeKeysThreshold) >= 0) {
						web3JConfig.sendWarn("通知：满Keys池赎回提醒",
								"### 通知：满Keys池赎回提醒！  \n  "
										+ "顶级池【<font color=\"red\">**[" + poolName + "](https://app.xai.games/pool/" + poolContractAddress + "/summary)**</font>】存在空闲质押空间。  \n  "
										+ "当前质押Keys：**<font color=\"red\">" + poolInfo.keyCount + "</font>**  \n  "
										+ "当前质押EsXAI：**" + XAIRedemptionJob.formatAmount(new BigDecimal(poolInfo.totalStakedAmount)
										.movePointLeft(18).setScale(2, RoundingMode.HALF_UP)) + "**  \n  "
										+ "加成：**×" + poolInfo.calcStakingTier() + "**  \n  "
										+ "算力值：**" + power + "**  \n  "
										+ "[点击前往查看详情](https://arbiscan.io/tx/" + hash + ")",

								"\uD83D\uDCAF*||Notify||：Full Keys Pool Redemption !*\n\n"
										+ "Full pool [" + poolName + "](app.xai.games/pool/" + poolContractAddress + "/summary) a stake may be made. \n"
										+ "*Tier：×" + poolInfo.calcStakingTier() + "* \n"
										+ "*Keys Power：" + poolInfo.calcKeysPower(keysWei) + "* \n"
										+ "*esXAI Power：" + poolInfo.calcEsXAIPower(esXAIWei) + "* \n"
										+ "*esXAI Staked：" + poolInfo.outputExXAI() + "* \n "
										+ "*Keys Staked：" + poolInfo.keyCount + "* \n"
										+ "*Active：*" + outputActive(getAndFlushActivePool(poolInfo, web3JConfig.proxyRestTemplate, newWeb3j, lastBlock, web3JConfig.weakActiveThreshold, true), poolInfo.poolAddress) + " \n"
										+ "[\uD83D\uDC49\uD83D\uDC49Click View](https://arbiscan.io/tx/" + hash + ")"
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
				else if ("0xf9e08660223e2dbb1c0b28c82942ab6b5e38b8e5".equalsIgnoreCase(to)) {
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
					if ("0x840ecba0".equalsIgnoreCase(method)) { // 大额赎回的领取
						SpringUtil.asyncRun(() -> {
							final TransactionReceipt receipt = newWeb3j.ethGetTransactionReceipt(hash).send().getTransactionReceipt().get();
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
									web3JConfig.sendWarn("预警：XAI大额赎回领取事件",
											"### 预警：XAI大额赎回领取事件！  \n  "
													+ "识别到【**" + this.getPeriod(redemptionAmount, totalAmount) + "**】天期限的赎回领取事件。  \n  "
													+ "赎回数量：**" + redemptionAmount.setScale(2, RoundingMode.HALF_UP) + " XAI**  \n  "
													+ "销毁数量：**" + recycleAmount.setScale(2, RoundingMode.HALF_UP) + " XAI**  \n  "
													+ "赎回地址：**" + redemptionTo.replaceAll("0x000000000000000000000000", "0x") + "**  \n  "
													+ "[点击前往查看详情](https://arbiscan.io/tx/" + hash + ")",

											"\uD83D\uDE80*BlockTrade：XAI Complete Redemption !* \n\n"
													+ "Monitored *" + this.getPeriod(redemptionAmount, totalAmount) + "* days of complete redemption collection event.  \n"
													+ "Redemption Amount：*" + redemptionAmount.setScale(2, RoundingMode.HALF_UP) + " XAI*  \n"
													+ "Burn Amount：*" + recycleAmount.setScale(2, RoundingMode.HALF_UP) + " XAI*  \n"
													+ "Address：*" + redemptionTo.replaceAll("0x000000000000000000000000", "0x") + "*  \n"
													+ "[\uD83D\uDC49\uD83D\uDC49Click View](https://arbiscan.io/tx/" + hash + ")");
								}

								// 汇总统计
								xaiRedemptionJob.incrStat(redemptionAmount, recycleAmount);
							}
						});
					}
				}
			}
		}
	}

	private final static BigDecimal PERIOD15 = new BigDecimal("0.25"), PERIOD90 = new BigDecimal("0.625"), PERIOD180 = BigDecimal.ONE;

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

}
