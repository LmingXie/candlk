package com.bojiu.webapp.user.job;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import javax.annotation.Resource;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.bojiu.common.redis.RedisUtil;
import com.bojiu.common.util.SpringUtil;
import com.bojiu.webapp.user.config.Web3JConfig;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.CollectionUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionResult;

@Slf4j
@Configuration
public class StatProfitJob {

	@Resource
	private Web3j web3j;
	@Resource
	Web3JConfig web3JConfig;

	// 目标合约地址 (A合约)
	final String contractAddress = "0x231bdd87ade0feb934a981f9a4442cc2bac110c8";
	// 目标方法的 Method ID
	final String targetMethodId = "0xe19c2253";

	// 定义函数参数结构（对应 address[], address[], address[], uint256[]）
	final List outputParameters = Arrays.asList(
			new TypeReference<DynamicArray<Address>>() {
			},  // token
			new TypeReference<DynamicArray<Address>>() {
			},  // from
			new TypeReference<DynamicArray<Address>>() {
			},  // to
			new TypeReference<DynamicArray<Uint256>>() {
			}   // amount
	);
	final Map<String, String> tokenMap = CollectionUtil.asLinkedHashMap(
			"USDT", "0x55d398326f99059fF775485246999027B3197955",
			"USDC", "0x8AC76a51cc950d9822D68b83fE1Ad97B32Cd580d",
			"BUSD", "0xe9e7CEA3DedcA5984780Bafc599bD69ADd087D56",
			"FDUSD", "0xc5f0f7b66764F6ec8C8Dff7BA683102295E16409",
			"ETH", "0x2170ed0880ac9a755fd29b2688956bd959f933f8"
	);
	final String[] tokenName = tokenMap.keySet().toArray(new String[0]);

	@Scheduled(cron = "${service.cron.StatProfitJob:0/3 * * * * ?}")
	public void run() {
		RedisUtil.fastAttemptInLock("statProfitJob", 30 * 60 * 1000, () -> {
			try {
				final BigInteger lastBlock = web3j.ethBlockNumber().send().getBlockNumber();
				log.warn("初始区块：{} -> {}", web3JConfig.lastBlock, lastBlock);
				while (lastBlock.compareTo(web3JConfig.lastBlock) > 0) {
					final BigInteger blockNumber = web3JConfig.incrLastBlock();
					SpringUtil.asyncRun(() -> {
						int retry = 5;
						while (retry-- > 0) {
							final Web3j newWeb3j = web3JConfig.pollingGetWeb3j();
							try {
								this.exec(newWeb3j, blockNumber);
								break;
							} catch (Exception e) {
								log.info("接口被限制，进行重试");
							}
						}
					});
				}
				log.info("结束本次扫描，最后区块：{}", web3JConfig.lastBlock);
			} catch (IOException ignore) {
			}
			return true;
		});
	}

	private void exec(Web3j newWeb3j, BigInteger blockNumber) throws Exception {
		final EthBlock.Block block = newWeb3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(blockNumber), true).send().getBlock();
		final List<TransactionResult> txs = block.getTransactions();
		if (CollectionUtils.isEmpty(txs)) {
			return;
		}

		log.info("正在执行扫描区块：{}", blockNumber);
		for (TransactionResult txR : txs) {
			final EthBlock.TransactionObject tx = (EthBlock.TransactionObject) txR.get();

			// 过滤：目标合约且 MethodID 匹配
			if (contractAddress.equalsIgnoreCase(tx.getTo()) && tx.getInput() != null && tx.getInput().startsWith(targetMethodId)) {
				// 截取 Data（去掉前 10 位的 0x 和 MethodID）
				final String inputData = tx.getInput().substring(10);
				// 使用 Web3j 解码器解析
				final List<Type> results = FunctionReturnDecoder.decode(inputData, outputParameters);
				if (results.isEmpty()) {
					continue;
				}
				log.info("--------------------------------------------------");
				log.info("发现目标调用! Hash: {}", tx.getHash());

				final List<Address> froms = (List<Address>) results.get(1).getValue(), tos = (List<Address>) results.get(2).getValue();
				final List<Uint256> amounts = (List<Uint256>) results.get(3).getValue();
				// Token类型标记，一共40位，-1后等于tokenName数组下标，与froms倒序对应，使用size-i的方式解析
				final String bizFlag = amounts.getLast().getValue().toString();

				// 打印解析出的具体数据
				for (int i = 0, size = froms.size(); i < size; i++) {
					final int offset = Integer.parseUnsignedInt(bizFlag, size - i - 1, size - i, 10) - 1;
					if (offset < tokenName.length) {
						log.info("序号 [{}]: Token: {}, From: {}, To: {}, Amount: {}", i, tokenName[offset],
								froms.get(i).getValue(), tos.get(i).getValue(), amounts.get(i).getValue());
					} else {
						log.warn("【无法解析】序号 [{}]: Token: {}, From: {}, To: {}, Amount: {}", i, offset,
								froms.get(i).getValue(), tos.get(i).getValue(), amounts.get(i).getValue());
					}
				}
			}
		}
	}

}
