package com.candlk.common.util;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;

import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.StringUtil;
import me.codeplayer.util.X;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;

/**
 * 基于 {@link java.net.http.HttpClient} 的请求封装工具类
 */
@Slf4j
public abstract class BaseHttpUtil {

	static {
		initRetryConfig();
	}

	/**
	 * 初始化 JDK HttpClient 的重试机制
	 * <p>
	 * https://segmentfault.com/a/1190000016579536
	 * <p>
	 * https://docs.oracle.com/en/java/javase/17/core/java-networking.html#GUID-E6C82625-7C02-4AB3-B15D-0DF8A249CD73
	 *
	 * @see jdk.internal.net.http.MultiExchange
	 * @see sun.net.NetProperties#get(String)
	 */
	protected static void initRetryConfig() {
		System.setProperty("jdk.httpclient.enableAllMethodRetry", "true");
		System.setProperty("jdk.httpclient.redirects.retrylimit", "3");
	}

	// 请求响应超时时间（目前统一设置为 30s，默认是不超时）
	public static Duration defaultRequestTimeout = Duration.of(30, ChronoUnit.SECONDS);

	public static final HttpClient httpClient = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_2)
			.connectTimeout(Duration.of(5, ChronoUnit.SECONDS))
			.build();

	public static final HttpResponse.BodyHandler<String> responseBodyHandler = HttpResponse.BodyHandlers.ofString();

	public static HttpRequest.Builder requestBuilder(HttpMethod method, String url, @Nullable String requestBody, boolean requestBodyAsJson) {
		return requestBuilder(method, URI.create(url), requestBody, requestBodyAsJson);
	}

	public static HttpRequest.Builder requestBuilder(HttpMethod method, URI uri, @Nullable String requestBody, boolean requestBodyAsJson) {
		final HttpRequest.Builder builder = HttpRequest.newBuilder().uri(uri);
		builder.timeout(defaultRequestTimeout);
		if (method == HttpMethod.GET) {
			builder.GET();
		} else if (method == HttpMethod.DELETE) {
			builder.DELETE();
		} else {
			builder.method(method.name(), HttpRequest.BodyPublishers.ofString(StringUtil.toString(requestBody)));
		}
		return builder
				.setHeader("Content-Type", requestBodyAsJson && method != HttpMethod.GET ? "application/json" : "application/x-www-form-urlencoded")
				.setHeader("Accept", "application/json");
	}

	/**
	 * HttpClient 有可能会因为服务器端连接超时关闭而抛出形如 "java.io.IOException: /10.189.0.176:51378: GOAWAY received" 的异常，该异常需要捕获并进行重试
	 * JDK 自带 HttpClient 没有提供 低级API 以精确判断该异常，因此只能通过异常 Message 进行判断
	 * <p>
	 * 参考 https://stackoverflow.com/questions/55087292/how-to-handle-http-2-goaway-with-httpclient
	 */
	public static <T> HttpResponse<T> sendWithRetryWhenGoawayReceived(HttpClient httpClient, HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler) throws IOException, InterruptedException {
		try {
			return httpClient.send(request, bodyHandler);
		} catch (IOException e) {
			if (StringUtils.endsWith(e.getMessage(), "GOAWAY received")) {
				log.warn("发送请求时遇到 [GOAWAY received] 断开响应，自动重试：{}", request.uri());
				return httpClient.send(request, bodyHandler);
			}
			throw e;
		}
	}

	@Nullable
	public static StringBuilder mapToParams(@Nullable StringBuilder sb, final @Nullable Map<String, ?> params, final @Nullable Function<String, String> keyConverter) {
		if (!X.isValid(params)) {
			return sb;
		}
		if (sb == null) {
			sb = new StringBuilder(params.size() * 16);
		} else {
			sb.ensureCapacity(sb.length() + params.size() * 16);
		}
		for (Map.Entry<String, ?> entry : params.entrySet()) {
			Object val = entry.getValue();
			if (val != null) {
				if (!sb.isEmpty()) {
					sb.append('&');
				}
				String key = entry.getKey();
				if (keyConverter != null) {
					key = keyConverter.apply(key);
				}
				sb.append(key).append('=').append(URLEncoder.encode(val.toString(), StandardCharsets.UTF_8));
			}
		}
		return sb;
	}

	@Nullable
	public static String mapToParams(@Nullable Map<String, ?> params, @Nullable Function<String, String> keyConverter) {
		StringBuilder sb = mapToParams(null, params, keyConverter);
		return sb == null ? null : sb.toString();
	}

	@Nullable
	public static String mapToParams(@Nullable Map<String, ?> params) {
		return mapToParams(params, null);
	}

}
