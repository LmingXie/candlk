package com.candlk.webapp.job;

import java.math.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;

import com.alibaba.fastjson2.JSONObject;
import com.candlk.common.util.SpringUtil;
import com.candlk.context.web.Jsons;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.web3j.abi.*;
import org.web3j.abi.datatypes.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.http.HttpService;

import static com.candlk.webapp.job.XAIPowerJob.*;

@Slf4j
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "service.xai")
public class Web3JConfig {

	public String web3jUrl = "https://arb1.arbitrum.io/rpc";

	public List<String> web3jUrlPool = new ArrayList<>();

	public String dingTalkBotUrl = "https://oapi.dingtalk.com/robot/send?access_token=0699ae605b56910e521bf8fea6104d028a1e724d068f8261a6271b2a877c783f";
	public volatile BigInteger lastBlock = BigInteger.valueOf(203584844);

	/** XAI大额赎回的领取 */
	public BigDecimal redemptionThreshold = new BigDecimal(5000);
	/** 池子esXAI大额 */
	public BigDecimal esXAIThreshold = new BigDecimal(50000);
	/** 池子Keys大额 */
	public BigDecimal keysThreshold = new BigDecimal(5);
	/** 顶级Keys算力池解除质押成功提醒 */
	public BigDecimal unstakeKeysThreshold = new BigDecimal("2");
	/**
	 * 相对较弱的活跃度检查阈值
	 * TODO 每次减半后需要进行调整，产出参考：https://arbiscan.io/address/0x958e5cc35fd7f95c135d55c7209fa972bdb68617#tokentxns
	 */
	public BigDecimal weakActiveThreshold = new BigDecimal(2000);
	public String statFilePath = "/mnt/xai_bot/stat.json";
	/** 取排名多少 */
	public Integer topN = 20;
	@Value("${service.proxy.host}")
	private String host;
	@Value("${service.proxy.port}")
	private Integer port;

	private String tgMsgHookUrl;
	private String tgChatId;

	/** 需要监听的发送者请求（小写）地址 -> 备注 */
	public Map<String, String> spyFroms = new HashMap<>();

	private static final RestTemplate restTemplate;
	public RestTemplate proxyRestTemplate;

	public synchronized BigInteger incrLastBlock() {
		return this.lastBlock = this.lastBlock.add(BigInteger.ONE);
	}

	static {
		final SimpleClientHttpRequestFactory httpRequestFactory = new SimpleClientHttpRequestFactory();
		httpRequestFactory.setReadTimeout(10000);
		httpRequestFactory.setConnectTimeout(5000);
		restTemplate = new RestTemplate(httpRequestFactory);
	}

	private volatile int offset = 0, maxOffset;
	public List<Web3j> web3jPool = new ArrayList<>();

	@PostConstruct
	public void init() {
		this.maxOffset = web3jUrlPool.size() - 1;
		for (String url : web3jUrlPool) {
			web3jPool.add(Web3j.build(new HttpService(url)));
		}

		// 初始化RestTemplate代理
		proxyRestTemplate = new RestTemplate();
		final SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port)));
		proxyRestTemplate.setRequestFactory(factory);
	}

	public synchronized Web3j pollingGetWeb3j() {
		final Web3j result = web3jPool.get(offset);
		if (offset++ >= maxOffset) {
			offset = 0;
		}
		return result;
	}

	private transient Web3j web3jCache;

	@Bean
	public Web3j getWeb3j() {
		return web3jCache = Web3j.build(new HttpService(web3jUrl));
	}

	public void sendWarn(String title, String content, String telegramMsg) {
		if (content != null) {
			SpringUtil.asyncRun(() -> {
				final HttpEntity<JSONObject> httpEntity = new HttpEntity<>(JSONObject.of(
						"msgtype", "markdown",
						"markdown", JSONObject.of(
								"title", title,
								"text", content
						),
						"at", JSONObject.of("isAtAll", "true")), new HttpHeaders());
				final String body = restTemplate.postForEntity(dingTalkBotUrl, httpEntity, String.class)
						.getBody();
				final JSONObject resp = Jsons.parseObject(body);
				if ("0".equals(resp.getString("errcode"))) {
					log.warn("预警通知结果：{}", body);
				} else {
					log.error("预警通知异常：body={},title={},text={}", body, title, content);
				}
			});
		}
		SpringUtil.asyncRun(() -> sendTelegramMessage(telegramMsg));
	}

	final List<String> failTgMsg = new ArrayList<>();

	public void sendTelegramMessage(String content) {
		log.info("正在向Telegram推送消息：{}", content);
		final HttpHeaders headers = new HttpHeaders();
		HttpEntity<JSONObject> httpEntity = new HttpEntity<>(JSONObject.of(
				"chat_id", tgChatId,
				"parse_mode", "Markdown",
				"text", content
		), headers);
		try {
			final JSONObject body = proxyRestTemplate.postForEntity(tgMsgHookUrl, httpEntity, JSONObject.class).getBody();
			if (body == null || !body.getBoolean("ok")) {
				log.error("发送Telegram消息失败：content={}，body={}", content, Jsons.encode(body));
			}
			if (!failTgMsg.isEmpty()) {
				JSONObject data = new JSONObject();
				data.put("chat_id", tgChatId);
				data.put("parse_mode", "Markdown");
				final Iterator<String> iterator = failTgMsg.iterator();
				while (iterator.hasNext()) {
					final String msg = iterator.next();
					data.put("text", msg);
					httpEntity = new HttpEntity<>(data, headers);

					log.info("正在向Telegram推送补偿消息：{}", msg);
					final JSONObject body1 = proxyRestTemplate.postForEntity(tgMsgHookUrl, httpEntity, JSONObject.class).getBody();
					if (body1 == null || !body1.getBoolean("ok")) {
						log.error("发送Telegram消息失败：content={}，body={}", content, Jsons.encode(body1));
					}
					iterator.remove(); // 删除消息
				}
			}
		} catch (Exception e) {
			failTgMsg.add(content);
			log.error("发送Telegram消息异常：content={}", content, e);
		}
	}

	private final static Function<String, String> initContractName = k -> {
		final Web3j web3jProxy = SpringUtil.getBean(Web3j.class);
		try {
			final String contractName = getContractName(web3jProxy, k);
			if (StringUtils.isNotEmpty(contractName)) {
				return contractName.startsWith("ALPHA") ? contractName
						// 非阿尔法过滤合约非UTF8编码字符
						: filterOffUtf8Mb4(contractName).replaceAll("“", "").replaceAll("”", "")
						.replaceAll("\\?", "")
						.replaceAll("\\.", "");
			}
			return contractName;
		} catch (Exception e) {
			log.error("获取合约名称失败：" + e);
		}
		return k;
	};

	static public String filterOffUtf8Mb4(String text) {
		final byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
		final ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
		int i = 0;
		while (i < bytes.length) {
			short b = bytes[i];
			if (b > 0) {
				buffer.put(bytes[i++]);
				continue;
			}
			b += 256;
			if ((b ^ 0xC0) >> 4 == 0) {
				buffer.put(bytes, i, 2);
				i += 2;
			} else if ((b ^ 0xE0) >> 4 == 0) {
				buffer.put(bytes, i, 3);
				i += 3;
			} else if ((b ^ 0xF0) >> 4 == 0) {
				i += 4;
			}
		}
		buffer.flip();
		return new String(buffer.array(), StandardCharsets.UTF_8);
	}

	/**
	 * 根据方法解释提示内容
	 * <p>
	 * 空格格式：&nbsp 或 &#160 或 &#xA0
	 * <p>
	 * 换行格式： \n
	 * </p>
	 */
	public final static Map<String, Function<Object[], String[]>> METHOD2TIP = new HashMap<>() {
		{
			put("0x57634198", inputs -> new String[] { "普通消息：从池子中领取奖励", null });
			put("0x2f1a0b1c", inputs -> parseStake(inputs, "预警：Keys质押", () -> new BigDecimal(new BigInteger(((String) inputs[3]).substring(138, 202), 16)),
					"**  \n  ", "质押", "Staked", (BigDecimal) inputs[7]));

			put("0xa528916d", inputs -> parseStake(inputs, "预警：esXAI质押", () ->
					new BigDecimal(new BigInteger(((String) inputs[3]).substring(74), 16)).movePointLeft(18)
							.setScale(2, RoundingMode.HALF_UP), " esXAI**  \n  ", "质押", "Staked", (BigDecimal) inputs[6]));

			put("0xd4e44335", inputs -> parseStake(inputs, "通知：Keys 赎回申请", () -> new BigDecimal(new BigInteger(((String) inputs[3]).substring(74), 16)),
					"**  \n  ", "赎回", "Approve Redemption", (BigDecimal) inputs[7]));

			put("0x75710569", inputs -> parseStake(inputs, "通知：esXAI 赎回申请", () ->
					new BigDecimal(new BigInteger(((String) inputs[3]).substring(74), 16)).movePointLeft(18)
							.setScale(2, RoundingMode.HALF_UP), " esXAI**  \n  ", "赎回", "Approve Redemption", (BigDecimal) inputs[6]));

			// unstakeKeys
			put("0x95003265", inputs -> parseStake(inputs, "预警：Keys 赎回成功", () -> new BigDecimal(new BigInteger(((String) inputs[3]).substring(202, 266), 16)),
					"**  \n  ", "赎回", "Complete Redemption", (BigDecimal) inputs[7]));

			// unstakeEsXai
			put("0x68da34e6", inputs -> parseStake(inputs, "预警：esXAI 赎回成功", () ->
							new BigDecimal(new BigInteger(((String) inputs[3]).substring(138, 202), 16)).movePointLeft(18).setScale(2, RoundingMode.HALF_UP),
					" esXAI**  \n  ", "赎回", "Complete Redemption", (BigDecimal) inputs[6]));

			put("0x098e8ae7", inputs -> {
				final String from = (String) inputs[0], hash = (String) inputs[2], nickname = (String) inputs[4];
				return new String[] {
						"预警：创建池子",
						"### 预警：创建池子！  \n  "
								+ "识别到关注的【**[" + nickname + "](https://arbiscan.io/address/" + from + ")**】地址正创建新池。  \n  "
								+ "Hash：**" + hash + "**  \n  "
								+ "[点击前往查看详情](https://arbiscan.io/tx/" + hash + ")",

						"\uD83D\uDE80*Warn：Create Pool !* \n\n"
								+ "Monitored [" + nickname + "](https://arbiscan.io/address/" + from + ") address creating a new pool.  \n"
								+ "Hash：*" + hash + "*  \n"
								+ "[\uD83D\uDC49\uD83D\uDC49Click View](https://arbiscan.io/tx/" + hash + ")"
				};
			});
			put(null, inputs -> {
				final String from = (String) inputs[0], to = (String) inputs[1], hash = (String) inputs[2], nickname = (String) inputs[4], method = (String) inputs[5];
				return new String[] {
						"预警：无法识别的调用",
						"### 预警：无法识别的调用！  \n  "
								+ "识别到关注的【**[" + nickname + "](https://arbiscan.io/address/" + from + ")**】地址进行了一笔无法识别的调用。  \n  "
								+ "From：" + from + "  \n  "
								+ "To：**" + to + "**  \n  "
								+ "Method：**" + method + "**  \n  "
								+ "Hash：" + hash + "  \n  "
								+ "[点击前往查看详情](https://arbiscan.io/tx/" + hash + ")",

						"\uD83D\uDE80*Warn：Unrecognized call !* \n\n"
								+ "Monitored [" + nickname + "](https://arbiscan.io/address/" + from + ") address made an unrecognized call. \n"
								+ "From：" + from + "  \n"
								+ "To：*" + to + "*  \n"
								+ "Method：*" + method + "*  \n"
								+ "Hash：" + hash + "  \n"
								+ "[\uD83D\uDC49\uD83D\uDC49Click View](https://arbiscan.io/tx/" + hash + ")"

				};
			});
		}

		private static String[] parseStake(Object[] inputs, String x, Supplier<BigDecimal> supplier, String x1, String type, String typeEn, BigDecimal threshold) {
			final String from = (String) inputs[0], hash = (String) inputs[2], input = (String) inputs[3], nickname = inputs[4] == null ? from : (String) inputs[4];
			final BigDecimal amount = supplier.get();
			final boolean hasBigAmount = inputs.length > 9; // "1" 占位
			if (hasBigAmount) { // 大额预警
				x = "大额" + x;
			}
			if (!hasBigAmount || amount.compareTo(threshold) >= 0) {
				final String poolContractAddress = new Address(input.substring(11, 74)).getValue(), poolName = getContractName(poolContractAddress);

				final Web3JConfig configProxy = SpringUtil.getBean(Web3JConfig.class);
				final PoolInfoVO info = XAIPowerJob.getPoolInfo(poolContractAddress, configProxy.web3jCache, true);
				final BigInteger lastBlock = (BigInteger) inputs[8];
				final String outputActive = outputActive(getAndFlushActivePool(info, configProxy.proxyRestTemplate, configProxy.web3jCache, lastBlock, configProxy.weakActiveThreshold, true), info.poolAddress);
				return new String[] { x,
						"### " + x + "！  \n  "
								+ "识别到关注的【**[" + nickname + "](https://arbiscan.io/address/" + from + ")**】地址正在【**[" + poolName + "](https://app.xai.games/pool/" + poolContractAddress + "/summary)**】池进行" + type + "。  \n  "
								+ type + "数量：**" + amount + x1 + "  \n  "
								+ "**Keys算力：" + info.calcKeysPower(keysWei) + "**  \n  "
								+ "**esXAI算力：" + info.calcEsXAIPower(esXAIWei) + "**  \n  "
								+ "阶梯：**×" + info.calcStakingTier() + "**  \n  "
								+ "esXAI总质押：**" + info.outputExXAI() + "**  \n  "
								+ "Keys总质押：**" + info.keyCount + "**  \n  "
								+ "活跃状态：**" + outputActive + "**  \n  "
								+ "[点击前往查看详情](https://arbiscan.io/tx/" + hash + ")",

						"\uD83D\uDE80*BlockTrade：" + typeEn + " !* \n\n"
								+ "Monitored that address [" + nickname + "](https://arbiscan.io/address/" + from + ") in [" + poolName + "](app.xai.games/pool/" + poolContractAddress + "/summary) pool " + typeEn + " action.  \n"
								+ typeEn + " Amount：*" + amount + x1.replaceAll("\\*\\*", "*") + " \n"
								+ "*Tier：×" + info.calcStakingTier() + "* \n"
								+ "*Keys Power：" + info.calcKeysPower(keysWei) + "* \n"
								+ "*esXAI Power：" + info.calcEsXAIPower(esXAIWei) + "* \n"
								+ "*esXAI Staked：" + info.outputExXAI() + "* \n "
								+ "*Keys Staked：" + info.keyCount + "* \n"
								+ "*Active：*" + outputActive + " \n "
								+ "[\uD83D\uDC49\uD83D\uDC49Click View](https://arbiscan.io/tx/" + hash + ")"
				};
			}
			return null;
		}
	};

	public static String getContractName(String poolContractAddress) {
		return contractNames.get(poolContractAddress, initContractName);
	}

	private static final Cache<String, String> contractNames = Caffeine.newBuilder()
			.initialCapacity(256)
			.maximumSize(2048)
			.expireAfterWrite(3, TimeUnit.HOURS)
			.build();

	public static String getContractName(Web3j web3j, String contractAddress) throws Exception {
		final List<TypeReference<?>> outputReturnTypes = List.of(new org.web3j.abi.TypeReference<Utf8String>() {
		});
		final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function("name", Collections.emptyList(), outputReturnTypes);
		org.web3j.protocol.core.methods.response.EthCall response = web3j.ethCall(org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(null, contractAddress, FunctionEncoder.encode(function)), DefaultBlockParameterName.LATEST)
				.sendAsync().get();
		final List<Type> results = FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
		return ((Utf8String) results.get(0)).getValue();
	}

}
