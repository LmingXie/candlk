package com.bojiu;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;

@Slf4j
public class JavaTest {

	@Test
	public void loadContractTest() {
		final HttpService service = new HttpService("https://bsc-mainnet.public.blastapi.io/");
		service.addHeader("referer", "https://four.meme/");
		service.addHeader("origin", "https://four.meme");
		// 目标合约地址 (A合约)
		final String contractAddress = "0x231bdd87ade0feb934a981f9a4442cc2bac110c8";
		// 目标方法的 Method ID
		final String targetMethodId = "0xe19c2253";

		try (Web3j web3j = Web3j.build(service)) {
			log.info("开始监听 BSC 区块...");

			// 2. 订阅新区块（replayPastBlocks 从最新块开始同步）
			web3j.replayPastBlocksFlowable(DefaultBlockParameterName.LATEST, true)
					.subscribe(ethBlock -> ethBlock.getBlock().getTransactions().forEach(txResult -> {
						if (txResult.get() instanceof EthBlock.TransactionObject tx) {
							// 3. 过滤：发送给目标合约 且 输入数据以 MethodID 开头
							if (contractAddress.equals(tx.getTo()) && tx.getInput() != null && tx.getInput().startsWith(targetMethodId)) {

								log.info("检测到目标交易: {}", tx.getHash());

								try {
									// 4. 获取交易收据以读取 Event Logs
									Optional<TransactionReceipt> receiptOptional = web3j.ethGetTransactionReceipt(tx.getHash()).send().getTransactionReceipt();

									receiptOptional.ifPresent(receipt -> {
										log.info("交易执行状态: {}", receipt.isStatusOK() ? "成功" : "失败");

										// 5. 遍历并解析所有事件
										for (Log eventLog : receipt.getLogs()) {
											// 这里会打印所有释放的事件，包括 ERC20 的 Transfer 或自定义事件
											final List<String> topics = eventLog.getTopics();
											// Transfer：0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef
											if (topics.size() == 3 && topics.get(0).startsWith("0xddf252ad")) {
												log.info("Transfer 事件：{} -> {} -> {}", topics.get(1), topics.get(2), eventLog.getData());
											}
										}
									});
								} catch (IOException e) {
									log.error("Error occurred while processing transaction: ", e);
								}
							}
						}
					}), throwable -> {
						log.error("监听异常: ", throwable);
					});

			// 为了让测试用例不立即结束，挂起主线程（实际生产中由框架管理声明周期）
			Thread.sleep(120000);
		} catch (Exception e) {
			log.error("Error occurred while loading contract: ", e);
		}
	}

}