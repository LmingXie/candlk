package com.bojiu.webapp.user.job;

import java.io.IOException;
import java.math.*;
import java.util.*;
import javax.annotation.Resource;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.bojiu.common.model.ErrorMessageException;
import com.bojiu.common.redis.RedisUtil;
import com.bojiu.common.util.SpringUtil;
import com.bojiu.webapp.user.config.Web3JConfig;
import com.bojiu.webapp.user.model.UserRedisKey;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.CollectionUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionResult;
import org.web3j.utils.Convert;

@Slf4j
@Configuration
public class StatProfitJob {

	@Resource
	private Web3j web3j;
	@Resource
	Web3JConfig web3JConfig;

	// 目标合约地址 (A合约)
	final String contractAddress = "0x231bdd87ade0feb934a981f9a4442cc2bac110c8";
	// 批量投放 transfer(address[] token,address[] from,address[] to,uint256[] amount)
	final String multTransferMethodId = "0xe19c2253";
	// ERC-20转账：
	final String erc20TransferMethodId = "0xa9059cbb";

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
	public static final Map<String, String> tokenMap = CollectionUtil.asLinkedHashMap(
			"USDT", "0x55d398326f99059fF775485246999027B3197955",
			"USDC", "0x8AC76a51cc950d9822D68b83fE1Ad97B32Cd580d",
			"BUSD", "0xe9e7CEA3DedcA5984780Bafc599bD69ADd087D56",
			"FDUSD", "0xc5f0f7b66764F6ec8C8Dff7BA683102295E16409",
			"ETH", "0x2170ed0880ac9a755fd29b2688956bd959f933f8"
	);
	public final String[] tokenName = tokenMap.keySet().toArray(new String[0]);
	public final List<String> tokenContracts = new ArrayList<>(StatProfitJob.tokenMap.values());

	public String getTokenName(String token) {
		for (int i = 0, len = tokenContracts.size(); i < len; i++) {
			if (tokenContracts.get(i).equals(token)) {
				return tokenName[i];
			}
		}
		return null;
	}

	@Scheduled(cron = "${service.cron.StatProfitJob:0/3 * * * * ?}")
	public void run() {
		RedisUtil.fastAttemptInLock("statProfitJob", 30 * 60 * 1000, () -> {
			try {
				final BigInteger fromBlock = web3JConfig.lastBlock,
						toBlock = web3j.ethBlockNumber().send().getBlockNumber();
				log.warn("初始区块：{} -> {}", fromBlock, toBlock);
				while (toBlock.compareTo(web3JConfig.lastBlock) > 0) {
					final BigInteger blockNumber = web3JConfig.incrLastBlock();
					SpringUtil.asyncRun(() -> web3JConfig.exec(10, newWeb3j -> this.exec(newWeb3j, blockNumber)));
				}

				SpringUtil.asyncRun(() -> web3JConfig.exec(10, newWeb3j -> this.transferStatLogs(fromBlock, toBlock, newWeb3j)));

				log.info("结束本次扫描，最后区块：{}", fromBlock);
			} catch (IOException ignore) {
			}
			return true;
		});
	}

	final String INCR_IF_EXISTS_LUA =
			"if redis.call('ZSCORE', KEYS[1], ARGV[2]) then " +
					"   return redis.call('ZINCRBY', KEYS[1], ARGV[1], ARGV[2]) " +
					"else " +
					"   return nil " +
					"end";

	private void transferStatLogs(BigInteger fromBlock, BigInteger toBlock, Web3j newWeb3j) {
		try {
			fromBlock = fromBlock.add(BigInteger.ONE);
			final EthFilter filter = new EthFilter(
					new DefaultBlockParameterNumber(fromBlock),
					new DefaultBlockParameterNumber(toBlock),
					tokenContracts
			);
			filter.addSingleTopic("0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef");
			// 读取事件日志
			final EthLog ethLog = newWeb3j.ethGetLogs(filter).send();
			if (ethLog.hasError()) {
				throw new ErrorMessageException(ethLog.getError().getMessage());
			}
			log.warn("查询转账事件：{} -> {}", fromBlock, toBlock);
			final List<EthLog.LogResult> logs = ethLog.getLogs();
			if (CollectionUtils.isEmpty(logs)) {
				return;
			}
			final Map<String, Map<String, BigDecimal>> tokenBalanceStat = new HashMap<>();
			for (EthLog.LogResult logResult : logs) {
				final Log l = (Log) logResult.get();
				final String contractAddress = l.getAddress();
				final List<String> topics = l.getTopics();
				final int size = topics.size();
				if (size == 3) {
					final BigDecimal rawAmount = new BigDecimal(new BigInteger(l.getData().substring(2), 16));
					final BigDecimal amount = Convert.fromWei(rawAmount, Convert.Unit.ETHER);
					if (amount.compareTo(BigDecimal.ZERO) <= 0) {
						continue;
					}
					final String toAddress = "0x" + topics.get(2).substring(26);
					tokenBalanceStat.computeIfAbsent(contractAddress, k -> new HashMap<>())
							.merge(toAddress, amount, BigDecimal::add);
					// log.info("转账：To: {} ,amount: {} ,block: {}, Hash: {}", toAddress, amount, l.getBlockNumber(), l.getTransactionHash());
				} else {
					log.info("跳过：Size: {}, Topics: {}, Hash: {}", size, topics, l.getTransactionHash());
				}
			}
			if (!tokenBalanceStat.isEmpty()) {
				for (Map.Entry<String, Map<String, BigDecimal>> entry : tokenBalanceStat.entrySet()) {
					final String tokenAddress = getTokenName(entry.getKey());
					if (tokenAddress != null) {
						final Map<String, BigDecimal> addressAmountMap = entry.getValue();
						final String redisKey = UserRedisKey.ADDRESS_BALANCE_STAT_PERIFX + tokenAddress;

						RedisUtil.doInTransaction(redisOps -> {
							for (Map.Entry<String, BigDecimal> addressAmountEntry : addressAmountMap.entrySet()) {
								final String address = addressAmountEntry.getKey();
								final BigDecimal amount = addressAmountEntry.getValue();

								redisOps.execute(new DefaultRedisScript<>(INCR_IF_EXISTS_LUA, Double.class),
										Collections.singletonList(redisKey),
										amount.setScale(6, RoundingMode.HALF_UP).toPlainString(),
										address
								);
							}
						});
					}
				}
			}
		} catch (Exception e) {
			if (!(e instanceof IOException || e instanceof ErrorMessageException)
					&& !e.getMessage().startsWith("Invalid response received: 403")) {
				log.error("查询转账事件失败:{}", e.getMessage());
			}
			throw new RuntimeException("查询转账事件失败");
		}
	}

	private void exec(Web3j newWeb3j, BigInteger blockNumber) {
		try {
			EthBlock ethBlock = newWeb3j.ethGetBlockByNumber(new DefaultBlockParameterNumber(blockNumber), true).send();
			if (ethBlock.hasError()) {
				throw new ErrorMessageException(ethBlock.getError().getMessage());
			}
			final EthBlock.Block block = ethBlock.getBlock();

			final List<TransactionResult> txs = block.getTransactions();
			if (CollectionUtils.isEmpty(txs)) {
				return;
			}

			log.info("正在执行扫描区块：{}", blockNumber);
			for (TransactionResult txR : txs) {
				final EthBlock.TransactionObject tx = (EthBlock.TransactionObject) txR.get();
				final String txTo = tx.getTo();

				// 过滤：目标合约且 MethodID 匹配
				if (tx.getInput() != null && tx.getInput().startsWith(multTransferMethodId) && contractAddress.equals(txTo)) {
					// 截取 Data（去掉前 10 位的 0x 和 MethodID）
					final String inputData = tx.getInput().substring(10);
					// 使用 Web3j 解码器解析
					final List<Type> results = FunctionReturnDecoder.decode(inputData, outputParameters);
					if (results.isEmpty()) {
						continue;
					}

					final List<Address> froms = (List<Address>) results.get(1).getValue(), tos = (List<Address>) results.get(2).getValue();
					final List<Uint256> amounts = (List<Uint256>) results.get(3).getValue();
					// Token类型标记，一共40位，-1后等于tokenName数组下标，与froms倒序对应，使用size-i的方式解析
					final String bizFlag = amounts.getLast().getValue().toString();

					RedisUtil.doInTransaction(redisOps -> {
						final ZSetOperations<String, String> opsForZSet = redisOps.opsForZSet();
						final HashOperations<String, Object, Object> opsForHash = redisOps.opsForHash();
						for (int i = 0, size = froms.size(); i < size; i++) {
							final int offset = Integer.parseUnsignedInt(bizFlag, size - i - 1, size - i, 10) - 1;
							if (offset < tokenName.length) {
								// 记录靓号地址
								final String tokenName = this.tokenName[offset];
								opsForZSet.addIfAbsent(UserRedisKey.ADDRESS_BALANCE_STAT_PERIFX + tokenName, tos.get(i).getValue(), 0);
								opsForHash.put(UserRedisKey.TRANSFER_KEY + tokenName, froms.get(i).getValue(), tos.get(i).getValue());
								// log.info("序号 [{}]: Token: {}, From: {}, To: {}, Amount: {}", i, tokenName,
								// 		froms.get(i).getValue(), tos.get(i).getValue(), amounts.get(i).getValue());
							}
						}
					});
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
