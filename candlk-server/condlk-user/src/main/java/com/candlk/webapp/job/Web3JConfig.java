package com.candlk.webapp.job;

import java.math.*;
import java.util.*;
import java.util.function.Function;

import com.alibaba.fastjson2.JSONObject;
import com.candlk.common.util.SpringUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "service.xai")
public class Web3JConfig {

	public String web3jUrl = "https://arb1.arbitrum.io/rpc";
	public String accessToken = "0699ae605b56910e521bf8fea6104d028a1e724d068f8261a6271b2a877c783f";
	public String webhookId = "1029d9449a1f0b5238500004";
	public BigInteger lastBlock = BigInteger.valueOf(203584844);

	/** 需要监听的发送者请求（小写）地址 -> 备注 */
	public Map<String, String> spyFroms = new HashMap<>();

	private static final RestTemplate restTemplate;

	static {
		final SimpleClientHttpRequestFactory httpRequestFactory = new SimpleClientHttpRequestFactory();
		httpRequestFactory.setReadTimeout(10000);
		httpRequestFactory.setConnectTimeout(5000);
		restTemplate = new RestTemplate();
	}

	@Bean
	public Web3j getWeb3j() {
		return Web3j.build(new HttpService(web3jUrl));
	}

	public void sendWarn(String title, String content) {
		final HttpEntity<JSONObject> httpEntity = new HttpEntity<>(JSONObject.of(
				"accessToken", accessToken,
				"title", title,
				"content", content), new HttpHeaders());
		final String body = restTemplate.postForEntity("https://connector.dingtalk.com/webhook/trigger/data/sync?webhookId=" + webhookId, httpEntity, String.class)
				.getBody();
		log.warn("预警通知结果：{}", body);
	}

	private final static Function<String, String> initContractName = k -> {
		final Web3j web3jProxy = SpringUtil.getBean(Web3j.class);
		try {
			return getContractName(web3jProxy, k);
		} catch (Exception e) {
			log.error("获取合约名称失败：" + e.getMessage());
		}
		return k;
	};
	/** 根据方法解释提示内容 */
	public final static Map<String, Function<String[], String[]>> METHOD2TIP = new HashMap<>() {{
		put("0x75710569", inputs -> {
			String from = inputs[0], to = inputs[1], hash = inputs[2], input = inputs[3], nickname = inputs[4];
			final String poolContractAddress = new Address(input.substring(11, 74)).getValue(), poolName = contractNames.computeIfAbsent(poolContractAddress, initContractName);
			final BigDecimal amount = new BigDecimal(new BigInteger(input.substring(74), 16)).movePointLeft(18).setScale(2, RoundingMode.HALF_UP);
			return new String[] {
					"预警：esXAI赎回",
					"### XAI预警！  \n<br>" +
							"**识别到关注的【" + nickname + "】地址正在" + "从【" + poolName + "】池赎回。**\n<br>"
							+ "赎回数量：**" + amount + " esXAI** \n<br/><br/>"
							+ "[点击前往查看](https://arbiscan.io/tx/" + hash + ")"
			};
		});
	}};

	private final static Map<String, String> contractNames = new HashMap<>();

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
