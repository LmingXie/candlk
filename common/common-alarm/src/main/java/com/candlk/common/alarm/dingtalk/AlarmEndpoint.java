package com.candlk.common.alarm.dingtalk;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import javax.annotation.Nullable;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.candlk.common.model.Messager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 钉钉&企业微信 告警服务
 */
@Slf4j
public class AlarmEndpoint {

	/** 钉钉机器人URl */
	final String url;
	/** 秘钥，可以没有 */
	@Nullable
	private String secret;

	/** 没有秘钥的方式 */
	public AlarmEndpoint(String url) {
		this.url = url;
	}

	/** 有私钥的方式 */
	public AlarmEndpoint(String url, @Nullable String secret) {
		this.url = url;
		this.secret = secret;
	}

	static HttpClient client = HttpClient.newBuilder()
			.connectTimeout(Duration.of(5, ChronoUnit.SECONDS))
			.build();

	public Messager<String> sendMsg(String msg) {
		return sendMsg(msg, false);
	}

	public Messager<String> sendCardMsg(String cardMsg) {
		return sendMsg(cardMsg, true);
	}

	/**
	 * 解析 钉钉配置，并返回对应的 AlarmEndpoint 集合，如果参数为空，则返回 null
	 */
	@Nullable
	public static List<AlarmEndpoint> parse(final String urlAndSecrets) {
		String[] urls = urlAndSecrets.split(",");
		if (urls.length == 0) {
			return null;
		}
		List<AlarmEndpoint> endpoints = new ArrayList<>(urls.length);
		for (String url : urls) {
			String[] urlAndKey = url.split("#");
			String uri = urlAndKey[0],
					key = urlAndKey.length == 2 && StringUtils.isNotBlank(urlAndKey[1]) ? urlAndKey[1].trim() : null;
			endpoints.add(new AlarmEndpoint(uri, key));
		}
		return endpoints;
	}

	static final HttpResponse.BodyHandler<String> responseBodyHandler = HttpResponse.BodyHandlers.ofString();

	Messager<String> sendMsg(String msg, boolean asCard) {
		String fullUrl = url;
		if (StringUtils.isNotEmpty(secret)) { // 目前只有钉钉机器人才需要密钥
			final String algorithm = "HmacSHA256";
			long timestamp = System.currentTimeMillis();
			String source = timestamp + "\n" + secret;
			String sign;
			try {
				Mac mac = Mac.getInstance(algorithm);
				mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), algorithm));
				byte[] signData = mac.doFinal(source.getBytes(StandardCharsets.UTF_8));
				sign = URLEncoder.encode(new String(Base64.getEncoder().encode(signData)), StandardCharsets.UTF_8);
			} catch (NoSuchAlgorithmException | InvalidKeyException e) {
				log.error("======【告警通知】消息签名处理失败======", e);
				return Messager.error(null);
			}
			fullUrl = url + "&timestamp=" + timestamp + "&sign=" + sign;
		}
		String result;
		int code;
		// 发送 bug 消息
		try {
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(fullUrl))
					.setHeader("Content-Type", "application/json")
					.POST(HttpRequest.BodyPublishers.ofString(msg)).build();
			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			code = response.statusCode();
			result = response.body();
			if (code == 400 && client.version() == HttpClient.Version.HTTP_2 && StringUtils.contains(result, "405 Not Allowed")) {
				// Windows 版的 JDK HttpClient 在 HTTP/2 协议下疑似存在 bug，所以可能需要降级为 HTTP/1.1
				synchronized (AlarmEndpoint.class) {
					if (client.version() == HttpClient.Version.HTTP_2) {
						log.warn("【告警通知】 HTTP/2 遇到兼容问题，自动切换为 HTTP/1.1");
						client = HttpClient.newBuilder()
								.version(HttpClient.Version.HTTP_1_1)
								.connectTimeout(Duration.of(5, ChronoUnit.SECONDS))
								.build();
					}
				}
				response = client.send(request, responseBodyHandler);
				code = response.statusCode();
				result = response.body();
			}
		} catch (Exception e) {
			final String label = getLabel(asCard);
			log.error("【告警通知】" + label + "信息发送失败：" + fullUrl + " \n" + msg, e);
			return Messager.error(null);
		}
		if (code != 200) {
			log.warn("【告警通知】信息发送失败：{} => {}", msg, result);
		} else if (log.isDebugEnabled()) {
			final String label = getLabel(asCard);
			log.debug("========【告警通知】{}通知结果========|{}", label, result);
		}
		return code == 200 ? Messager.hideData(result) : Messager.hideData(result, "发送异常！").setStatus(Messager.ERROR).setCode(code);
	}

	String getLabel(final boolean asCard) {
		return asCard ? "卡片" : "业务异常bug";
	}

}
