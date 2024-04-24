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
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionResult;

import static com.candlk.webapp.job.Web3JConfig.METHOD2TIP;

@Slf4j
@Configuration
public class XAIJob {

	@Resource
	private Web3j web3j;
	@Resource
	private Web3JConfig web3JConfig;

	@Scheduled(cron = "${service.cron.xai:0/5 * * * * ?}")
	public void run() throws Exception {
		final BigInteger lastBlock = web3j.ethBlockNumber().send().getBlockNumber();
		do {
			final BigInteger blockNumber = web3JConfig.incrLastBlock();
			SpringUtil.asyncRun(() -> {
				final EthBlock.Block block = web3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(blockNumber), true).send().getBlock();
				log.info("正在执行扫描区块：{}", blockNumber);
				final List<TransactionResult> txs = block.getTransactions();
				if (!CollectionUtils.isEmpty(txs)) {
					for (TransactionResult txR : txs) {
						final TransactionObject tx = (TransactionObject) txR.get();
						final Transaction info = tx.get();
						final String from = info.getFrom(), to = info.getTo(), hash = info.getHash(), input = info.getInput(),
								nickname = web3JConfig.spyFroms.get(from.toLowerCase()),
								method = StringUtils.length(input) > 10 ? input.substring(0, 10) : null;
						if (nickname != null) {
							final String[] msg = METHOD2TIP.getOrDefault(method, METHOD2TIP.get(null))
									.apply(new String[] { from, to, hash, input, nickname, method });
							X.use(msg, m -> web3JConfig.sendWarn(msg[0], msg[1]));
						}
						// 只识别大额质押与赎回
						else if ("0xf9e08660223e2dbb1c0b28c82942ab6b5e38b8e5".equalsIgnoreCase(to)) {
							final Function<String[], String[]> function = METHOD2TIP.get(method);
							if (function != null) {
								final String[] msg = function.apply(new String[] { from, to, hash, input, from, method, "1" });
								X.use(msg, m -> {
									if (msg[1] != null) {
										web3JConfig.sendWarn(msg[0], msg[1]);
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
									final TransactionReceipt receipt = web3j.ethGetTransactionReceipt(hash).send().getTransactionReceipt().get();
									final List<Log> logs = receipt.getLogs();
									if (logs.size() == 4) {
										final Log redemption = logs.get(1), recycle = logs.get(2);
										final BigDecimal redemptionAmount = new BigDecimal(new BigInteger(redemption.getData().substring(2), 16)).movePointLeft(18);
										if (redemptionAmount.compareTo(web3JConfig.redemptionThreshold) >= 0) {
											final BigDecimal totalAmount = new BigDecimal(new BigInteger(logs.get(0).getData().substring(2), 16)).movePointLeft(18),
													recycleAmount = new BigDecimal(new BigInteger(recycle.getData().substring(2), 16)).movePointLeft(18)
															.setScale(2, RoundingMode.HALF_UP);
											final String redemptionTo = redemption.getTopics().get(2);
											web3JConfig.sendWarn("预警：XAI大额赎回领取事件",
													"### 预警：XAI大额赎回领取事件！  \n  "
															+ "识别到【**" + this.getPeriod(redemptionAmount, totalAmount) + "**】天期限的赎回领取事件。  \n  "
															+ "赎回数量：**" + redemptionAmount.setScale(2, RoundingMode.HALF_UP) + " XAI**  \n  "
															+ "销毁数量：**" + recycleAmount.setScale(2, RoundingMode.HALF_UP) + " XAI**  \n  "
															+ "赎回地址：**" + redemptionTo.replaceAll("0x000000000000000000000000", "0x") + "**  \n  "
															+ "[点击前往查看详情](https://arbiscan.io/tx/" + hash + ")");
										}
									}
								});
							}
						}
					}
				}
			});
		} while (lastBlock.compareTo(web3JConfig.lastBlock) > 0);
		log.info("结束本次扫描，最后区块：{}", web3JConfig.lastBlock);
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
