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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;

/**
 * 基于 {@link java.net.http.HttpClient} 的请求封装工具类
 */
@Slf4j
public abstract class BaseHttpUtil {

	static {
		initNetConfig();
	}

	/**
	 * 预初始化 {@link HttpClient} 的部分参数设置
	 *
	 * @param enableProxyAuth 是否启用代理的 Basic 认证
	 * @param enableRetryAll 是否启用所有方法的自动重试行为，默认只启用 GET、HEAD 等幂等性行为
	 */
	public static void preInit(boolean enableProxyAuth, boolean enableRetryAll) {
		if (enableProxyAuth) {
			System.setProperty("jdk.http.auth.proxying.disabledSchemes", "");
			System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
		}
		if (enableRetryAll) {
			initRetryConfig();
		}
	}

	private static void initNetConfig() {
		// 参数配置参见：https://docs.oracle.com/en/java/javase/17/core/java-networking.html

		// HTTP/1.1 连接池的最大连接数
		// 参见 jdk.internal.net.http.ConnectionPool.MAX_POOL_SIZE
		// System.setProperty("jdk.httpclient.connectionPoolSize", "100"); // 默认 0 表示无限制

		// HTTP/1.1 空闲连接最大存活时间
		// 参见 jdk.internal.net.http.ConnectionPool.KEEP_ALIVE
		System.setProperty("jdk.httpclient.keepalive.timeout", "60"); // 默认 1200s

		// HTTP/2 空闲连接最大存活时间
		// System.setProperty("jdk.httpclient.keepalive.timeout.h2", "60"); // 默认与 jdk.httpclient.keepalive.timeout 保持一致

		//  HTTP/2 stream 的最大值
		// System.setProperty("jdk.httpclient.maxstreams", "100"); // 默认 100
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
		// 默认情况下，只有幂等请求（如 GET、HEAD）会在特定条件下自动重试
		// 开启此属性后，所有非幂等性请求（如 POST、PUT、DELETE）也都会自动重试
		System.setProperty("jdk.httpclient.enableAllMethodRetry", "true");

		// 请求失败时的重定向重试次数，默认是 5
		System.setProperty("jdk.httpclient.redirects.retrylimit", "3");
	}

	// 请求响应超时时间（目前统一设置为 30s，默认是不超时）
	public static Duration defaultRequestTimeout = Duration.of(30, ChronoUnit.SECONDS);

	static class Lazy {

		private static HttpClient httpClient = HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_2)
				.connectTimeout(Duration.of(5, ChronoUnit.SECONDS))
				.build();

	}

	public static HttpClient defaultClient() {
		return Lazy.httpClient;
	}

	public static void setDefaultClient(HttpClient defaultClient) {
		Lazy.httpClient = Assert.notNull(defaultClient);
	}

	public static final HttpResponse.BodyHandler<String> responseBodyHandler = HttpResponse.BodyHandlers.ofString();

	public static HttpRequest.Builder requestBuilder(HttpMethod method, String url, @Nullable String requestBody, boolean requestBodyAsJson) {
		return requestBuilder(method, URI.create(url), requestBody, requestBodyAsJson);
	}

	public static HttpRequest.Builder requestBuilder(HttpMethod method, URI uri, @Nullable String requestBody, boolean requestBodyAsJson) {
		final HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
				.timeout(defaultRequestTimeout);
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

	public static <T> HttpResponse<T> doSend(HttpClient httpClient, HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler) throws IOException, IllegalThreadStateException {
		try {
			return httpClient.send(request, bodyHandler);
		} catch (InterruptedException e) {
			throw wrapInterruptedException(e);
		}
	}

	/**
	 * HttpClient 有可能会因为服务器端连接超时关闭而抛出形如 "java.io.IOException: /10.189.0.176:51378: GOAWAY received" 的异常，该异常需要捕获并进行重试
	 * JDK 自带 HttpClient 没有提供 低级API 以精确判断该异常，因此只能通过异常 Message 进行判断
	 * <p>
	 * 参考 https://stackoverflow.com/questions/55087292/how-to-handle-http-2-goaway-with-httpclient
	 */
	public static <T> HttpResponse<T> sendWithRetryWhenIOE(HttpClient httpClient, HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler, boolean retryWhenConnReset) throws IOException, IllegalThreadStateException {
		try {
			return doSend(httpClient, request, bodyHandler);
		} catch (IOException e) {
			final String msg = e.getMessage();
			if (retryWhenConnReset && "Connection reset".equals(msg)) {
				// https://blog.csdn.net/fishjam/article/details/84259679
				log.warn("发送请求时遇到 [Connection reset] 断开响应，自动重试：{}", request.uri());
				return doSend(httpClient, request, bodyHandler);
			} else if (StringUtils.endsWith(msg, "GOAWAY received")) {
				log.warn("发送请求时遇到 [GOAWAY received] 断开响应，自动重试：{}", request.uri());
				return doSend(httpClient, request, bodyHandler);
			}
			throw e;
		}
	}

	/**
	 * HttpClient 有可能会因为服务器端连接超时关闭而抛出形如 "java.io.IOException: /10.189.0.176:51378: GOAWAY received" 的异常，该异常需要捕获并进行重试
	 * JDK 自带 HttpClient 没有提供 低级API 以精确判断该异常，因此只能通过异常 Message 进行判断
	 * <p>
	 * 参考 https://stackoverflow.com/questions/55087292/how-to-handle-http-2-goaway-with-httpclient
	 */
	public static <T> HttpResponse<T> sendWithRetryWhenGoawayReceived(HttpClient httpClient, HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler) throws IOException, IllegalThreadStateException {
		return sendWithRetryWhenIOE(httpClient, request, bodyHandler, false);
	}

	/**
	 * 将 Map 集合中的键值对转换为 URL 参数格式的字符串。
	 * 【注意】如果 {@link Map.Entry#getValue()} 为 {@code null}，则表示该键值对不会被拼接
	 *
	 * @param sb 用于拼接参数的 StringBuilder。如果为 {@code null}，则内部会在必要时创建一个新的 StringBuilder 对象
	 * @param hasParam 之前是否已拼接了参数，如果为 {@code true}，则会在后续拼接的第一个参数前追加 '&' 符号。如果为 {@code null} 表示内部自动根据 {@code sb} 中的内容进行识别
	 * @param urlSafeRequired 是否需要对参数值进行 URL 预编码（只有明确不会出现非安全字符时，才建议为  {@code false} ）
	 * @param converter 对集合元素进行预处理的转换器，如果为 null 则表示无需预处理。如果转换后的结果为 {@code null}，则表示跳过该参数（不对其进行参数拼接）
	 * @return 如果传入的 sb 为 {@code null} 且 {@code params} 为空，就会返回 {@code null}
	 */
	@Nullable
	public static StringBuilder mapToParams(@Nullable StringBuilder sb, @Nullable Boolean hasParam, final @Nullable Map<String, ?> params, final boolean urlSafeRequired, final @Nullable Function<Map.Entry<String, Object>, Map.Entry<String, Object>> converter) {
		if (params == null || params.isEmpty()) {
			return sb;
		}
		boolean notFirst;
		if (sb == null) {
			// &paramName=paramValue
			sb = new StringBuilder(params.size() * 20 + 16);
			notFirst = hasParam != null && hasParam;
		} else {
			notFirst = hasParam != null ? hasParam : sb.lastIndexOf("=") > -1;
			sb.ensureCapacity(sb.length() + params.size() * 20 + 16);
		}
		for (Map.Entry<String, ?> entry : params.entrySet()) {
			if (converter != null) {
				entry = converter.apply(X.castType(entry));
				if (entry == null) {
					continue;
				}
			}
			Object val = entry.getValue();
			if (val != null) {
				if (notFirst) {
					sb.append('&');
				} else {
					notFirst = true;
				}
				sb.append(entry.getKey()).append('=');
				if (val instanceof Number) {
					if (val instanceof Integer) {
						sb.append((int) val);
					} else if (val instanceof Long) {
						sb.append((long) val);
					} else if (val instanceof java.math.BigDecimal bd) {
						sb.append(bd.toPlainString());
					} else {
						sb.append(val);
					}
					continue;
				}
				if (urlSafeRequired) { // 如果需要对 URL 编码安全，就需要对参数进行 URL 预编码
					sb.append(URLEncoder.encode(val.toString(), StandardCharsets.UTF_8));
				} else {
					sb.append(val);
				}
			}
		}
		return sb;
	}

	/**
	 * 将 Map 集合中的键值对转换为 URL 参数格式的字符串，如果参数值中存在不安全的字符，也会预先进行 URL 编码。
	 * 【注意】如果 {@link Map.Entry#getValue()} 为 {@code null}，则表示该键值对不会被拼接
	 *
	 * @param sb 用于拼接参数的 StringBuilder。如果为 {@code null}，则内部会在必要时创建一个新的 StringBuilder 对象
	 * @param hasParam 之前是否已拼接了参数，如果为 {@code true}，则会在后续拼接的第一个参数前追加 '&' 符号。如果为 {@code null} 表示内部自动根据 {@code sb} 中的内容进行识别
	 * @param converter 对集合元素进行预处理的转换器，如果为 null 则表示无需预处理。如果转换后的结果为 {@code null}，则表示跳过该参数（不对其进行参数拼接）
	 * @return 如果传入的 sb 为 {@code null} 且 {@code params} 为空，就会返回 {@code null}
	 */
	@Nullable
	public static StringBuilder mapToParams(@Nullable StringBuilder sb, @Nullable Boolean hasParam, final @Nullable Map<String, ?> params, final @Nullable Function<Map.Entry<String, Object>, Map.Entry<String, Object>> converter) {
		return mapToParams(sb, hasParam, params, true, converter);
	}

	public static Function<Map.Entry<String, Object>, Map.Entry<String, Object>> ignoreEmptyValue() {
		return e -> {
			Object val = e.getValue();
			if (val == null || val instanceof CharSequence cs && cs.isEmpty()) {
				return null;
			}
			return e;
		};
	}

	/**
	 * 将 Map 集合中的键值对转换为 URL 参数格式的字符串，如果参数值中存在不安全的字符，也会预先进行 URL 编码。
	 * 【注意】如果参数值为 {@code null}，则表示该键值对不会被拼接
	 *
	 * @param sb 用于拼接参数的 StringBuilder。如果为 {@code null}，则内部会在必要时创建一个新的 StringBuilder 对象
	 * @param hasParam 之前是否已拼接了参数，如果为 {@code true}，则会在后续拼接的第一个参数前追加 '&' 符号。如果为 {@code null} 表示内部自动根据 {@code sb} 中的内容进行识别
	 * @return 如果传入的 sb 为 {@code null} 且 {@code params} 为空，就会返回 {@code null}
	 */
	@Nullable
	public static StringBuilder mapToParams(@Nullable StringBuilder sb, @Nullable Boolean hasParam, final boolean urlSafeRequired, final @Nullable Map<String, ?> params) {
		return mapToParams(sb, hasParam, params, urlSafeRequired, null);
	}

	/**
	 * 将 Map 集合中的键值对转换为 URL 参数格式的字符串，如果参数值中存在不安全的字符，也会预先进行 URL 编码。
	 * 【注意】如果参数值为 {@code null}，则表示该键值对不会被拼接
	 *
	 * @param sb 用于拼接参数的 StringBuilder。如果为 {@code null}，则内部会在必要时创建一个新的 StringBuilder 对象
	 * @param hasParam 之前是否已拼接了参数，如果为 {@code true}，则会在后续拼接的第一个参数前追加 '&' 符号。如果为 {@code null} 表示内部自动根据 {@code sb} 中的内容进行识别
	 * @return 如果传入的 sb 为 {@code null} 且 {@code params} 为空，就会返回 {@code null}
	 */
	@Nullable
	public static StringBuilder mapToParams(@Nullable StringBuilder sb, @Nullable Boolean hasParam, final @Nullable Map<String, ?> params) {
		return mapToParams(sb, hasParam, params, null);
	}

	/**
	 * 将 Map 集合中的键值对转换为 URL 参数格式的字符串，如果参数值中存在不安全的字符，也会预先进行 URL 编码。
	 * 【注意】如果参数值为 {@code null}，则表示该键值对不会被拼接
	 *
	 * @return 如果传入的 {@code params} 为空，则返回 ""
	 */
	@Nonnull
	public static String mapToParams(final @Nullable Map<String, ?> params, final boolean urlSafeRequired) {
		StringBuilder sb = mapToParams(null, Boolean.FALSE, params, urlSafeRequired, null);
		return sb == null ? "" : sb.toString();
	}

	/**
	 * 将 Map 集合中的键值对转换为 URL 参数格式的字符串，如果参数值中存在不安全的字符，也会预先进行 URL 编码。
	 * 【注意】如果参数值为 {@code null}，则表示该键值对不会被拼接
	 *
	 * @return 如果传入的 {@code params} 为空，则返回 ""
	 */
	@Nonnull
	public static String mapToParams(final @Nullable Map<String, ?> params) {
		return mapToParams(params, true);
	}

	/**
	 * @param apiPrefix 请求路径前缀
	 * @param hasParam sb 或 apiPrefix 是否已经附带部分参数
	 */
	@Nonnull
	public static StringBuilder concatGetUri(@Nullable StringBuilder sb, String apiPrefix, @Nullable Boolean hasParam, @Nullable Map<String, ?> params) {
		final int size = X.size(params);
		final StringBuilder uri = StringUtil.initBuilder(sb, size * 16 + apiPrefix.length());
		if (size > 0) {
			if (hasParam == null) {
				hasParam = apiPrefix.lastIndexOf('?') > -1;
			}
			if (!hasParam) {
				uri.append('?');
			}
			mapToParams(uri, hasParam, params, null);
		}
		return uri;
	}

	@Nonnull
	public static StringBuilder concatGetUri(@Nullable StringBuilder sb, String apiRoot, @Nullable Map<String, ?> params) {
		return concatGetUri(sb, apiRoot, null, params);
	}

	/**
	 * @param apiPrefix 请求路径前缀
	 * @param hasParam sb 或 apiPrefix 是否已经附带部分参数
	 */
	@Nonnull
	public static String concatGetUri(String apiPrefix, @Nullable Boolean hasParam, @Nullable Map<String, ?> params) {
		final int size = X.size(params);
		if (size > 0) {
			final StringBuilder uri = new StringBuilder(size * 16 + apiPrefix.length()).append(apiPrefix);
			if (hasParam == null) {
				hasParam = apiPrefix.lastIndexOf('?') > -1;
			}
			if (!hasParam) {
				uri.append('?');
			}
			return mapToParams(uri, hasParam, params, null).toString();
		}
		return apiPrefix;
	}

	/**
	 * @param apiPrefix 请求路径前缀
	 */
	@Nonnull
	public static String concatGetUri(String apiPrefix, @Nullable Map<String, ?> params) {
		return concatGetUri(apiPrefix, null, params);
	}

	public static IllegalThreadStateException wrapInterruptedException(InterruptedException e) {
		Thread.currentThread().interrupt();
		IllegalThreadStateException ex = new IllegalThreadStateException();
		ex.initCause(e);
		return ex;
	}

}
