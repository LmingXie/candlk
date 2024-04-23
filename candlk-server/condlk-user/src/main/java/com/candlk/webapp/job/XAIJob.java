package com.candlk.webapp.job;

import java.util.List;
import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionResult;
import org.web3j.protocol.core.methods.response.Transaction;

@Slf4j
@Configuration
public class XAIJob {

	@Resource
	private Web3j web3j;
	@Resource
	private Web3JConfig web3JConfig;

	@Scheduled(cron = "${service.cron.xai:0/30 * * * * ?}")
	public void run() throws Exception {
		log.info("正在执行扫描任务");
		final EthBlock.Block block = web3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(web3JConfig.lastBlock), true).send().getBlock();
		final List<TransactionResult> txs = block.getTransactions();
		if (!CollectionUtils.isEmpty(txs)) {
			for (TransactionResult txR : txs) {
				final TransactionObject tx = (TransactionObject) txR.get();
				final Transaction info = tx.get();
				final String from = info.getFrom(), to = info.getTo(), hash = info.getHash(), input = info.getInput(),
						nickname = web3JConfig.spyFroms.get(from.toLowerCase());
				if (nickname != null) {
					final String method = input.substring(0, 10);
					final String[] msg = Web3JConfig.METHOD2TIP.get(method).apply(new String[] { from, to, hash, input, nickname });
					web3JConfig.sendWarn(msg[0], msg[1]);
				}
			}
		}
	}

}
