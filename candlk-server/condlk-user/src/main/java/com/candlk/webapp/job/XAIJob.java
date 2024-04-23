package com.candlk.webapp.job;

import java.math.BigInteger;
import java.util.List;
import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.X;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionResult;
import org.web3j.protocol.core.methods.response.Transaction;

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
		while (lastBlock.compareTo(web3JConfig.lastBlock) > 0) {
			final EthBlock.Block block = web3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(web3JConfig.lastBlock), true).send().getBlock();
			log.info("正在执行扫描区块：{}", web3JConfig.lastBlock);
			final List<TransactionResult> txs = block.getTransactions();
			if (!CollectionUtils.isEmpty(txs)) {
				for (TransactionResult txR : txs) {
					final TransactionObject tx = (TransactionObject) txR.get();
					final Transaction info = tx.get();
					final String from = info.getFrom(), to = info.getTo(), hash = info.getHash(), input = info.getInput(),
							nickname = web3JConfig.spyFroms.get(from.toLowerCase());
					if (nickname != null) {
						final String method = StringUtils.isNoneEmpty(input) ? input.substring(0, 10) : null;
						final String[] msg = METHOD2TIP.getOrDefault(method, METHOD2TIP.get(null))
								.apply(new String[] { from, to, hash, input, nickname, method });
						X.use(msg, m -> web3JConfig.sendWarn(msg[0], msg[1]));
					}
					// 只识别大额交易
					else if (to.equalsIgnoreCase("0xf9e08660223e2dbb1c0b28c82942ab6b5e38b8e5")) {
						final String method = StringUtils.isNoneEmpty(input) ? input.substring(0, 10) : null;
						final String[] msg = METHOD2TIP.get(method).apply(new String[] { from, to, hash, input, nickname, method, "1" });
						X.use(msg, m -> web3JConfig.sendWarn(msg[0], msg[1]));
					}
				}
			}
			web3JConfig.lastBlock = web3JConfig.lastBlock.add(BigInteger.ONE);
		}
		log.info("结束本次扫描，最后区块：{}", web3JConfig.lastBlock);
	}

}
