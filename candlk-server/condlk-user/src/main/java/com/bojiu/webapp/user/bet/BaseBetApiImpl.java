package com.bojiu.webapp.user.bet;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import javax.annotation.*;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.util.JDKUtils;
import com.bojiu.common.context.I18N;
import com.bojiu.common.model.*;
import com.bojiu.common.util.BaseHttpUtil;
import com.bojiu.common.util.EnhanceHttpResponseBodyHandler;
import com.bojiu.context.model.*;
import com.bojiu.context.web.Jsons;
import com.bojiu.webapp.base.entity.Merchant;
import com.bojiu.webapp.base.util.LocalScheduler;
import com.bojiu.webapp.user.dto.BetApiConfig;
import com.bojiu.webapp.user.model.BetProvider;
import com.bojiu.webapp.user.model.MetaType;
import com.bojiu.webapp.user.service.MetaService;
import me.codeplayer.util.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.http.HttpMethod;

public abstract class BaseBetApiImpl extends BaseHttpUtil implements BetApi {

	/** 使用 30s 的超时时间（默认是 10s） */
	protected static final int FLAG_LONG_TIMEOUT = 1;
	/** 通过 callback 属性返回【响应文本】，而不是通过 data 属性返回 JSONObject */
	protected static final int FLAG_RETURN_TEXT = 1 << 1;
	/** 在日志中记录 URL编码前请求地址 */
	protected static final int FLAG_LOG_DECODED_URI = 1 << 2;

	/** 增加超时时间 + 解码URL */
	protected static final int FLAG_QUERY_PLAY_LOG_BY_TIME = FLAG_LONG_TIMEOUT | FLAG_LOG_DECODED_URI;

	/** 标准的最大查询延迟（毫秒值），目前是 3分钟 */
	protected static final long stdMaxQueryInterval = EasyDate.MILLIS_OF_MINUTE * 3;

	/** 当 {@link Messager#getCallback()}  } 为该值时，表示登入游戏时第三方接口返回的是 html 页面代码 */
	public static final String CALLBACK_LOGIN_BY_HTML = "html";

	@Resource
	MetaService metaService;

	protected BetApiConfig config;

	@Nullable
	protected static volatile HttpClient proxyClient;

	/** 初始化代理客户端 */
	protected void initProxyClientAndConfig() {
		if (proxyClient == null) {
			synchronized (BetApi.class) {
				if (proxyClient == null) {
					config = metaService.getCachedParsedValue(Merchant.PLATFORM_ID, MetaType.bet_config, BetProvider.HG.name(), BetApiConfig.class);
					proxyClient = getProxyOrDefaultClient(config.proxy);
				}
			}
		}
	}

	protected boolean requestBodyAsJson() {
		return false;
	}

	/**
	 * 判断响应数据是否指示业务处理成功
	 *
	 * @see Messager#OK
	 * @see #STATUS_MAINTAIN
	 */
	protected abstract String mapStatus(JSONObject json, HttpResponse<String> response);

	/**
	 * 判断响应数据是否指示业务处理成功
	 *
	 * @see Messager#OK
	 * @see #STATUS_MAINTAIN
	 */
	protected String mapStatus(JSONObject json, HttpResponse<String> response, Object okCode, Object maintainCode) {
		final Object code = getErrorCode(json);
		if (code != null) {
			if (code.equals(okCode)) {
				return Messager.OK;
			} else if (code.equals(maintainCode)) {
				return STATUS_MAINTAIN;
			}
		}
		return null;
	}

	protected abstract Object getErrorCode(JSONObject json);

	/**
	 * 解析赔率盘口值/比率
	 *
	 * @see com.bojiu.webapp.user.dto.GameDTO.OddsInfo#ratioRate
	 */
	protected String handleRatioRate(String ratioRate) {
		return ratioRate.replace(" ", "");
	}

	protected static Date getDate(JSONObject node, FastDateFormat format, String field) {
		String dateStr = node.getString(field);
		if (dateStr != null) {
			try {
				return format.parse(dateStr);
			} catch (ParseException e) {
				throw new IllegalArgumentException(e);
			}
		}
		return null;
	}

	protected void handleConsiderAsOK(Messager<JSONObject> result, Object code) {
		handleConsiderAs(result, Messager.OK, code);
	}

	protected void handleConsiderAs(Messager<JSONObject> result, String asStatus, Object code) {
		if (!result.isOK()) {
			JSONObject json = result.data();
			if (json != null && code.equals(getErrorCode(json))) {
				result.setStatus(asStatus).setMsg(null);
			}
		}
	}

	protected void handleConsiderAsOK(Messager<JSONObject> result, Object... codes) {
		if (!result.isOK()) {
			JSONObject json = result.data();
			if (json != null && ArrayUtils.contains(codes, getErrorCode(json))) {
				result.setOK().setMsg(null);
			}
		}
	}

	protected Map<String, Object> preInit(Map<String, Object> params) {
		return params;
	}

	/**
	 * 内部执行链路：{@link #preInit(Map) }
	 * <p> =>  paramCallback()
	 * <p> => {@link #createRequest(HttpMethod, URI, Map, int)}
	 * <p> => {@link #applyRequestData(HttpRequest.Builder, HttpMethod, URI, Map)}
	 * <p> => 执行请求  }
	 *
	 * @param method 请求方式
	 * @param uri 请求地址
	 * @param params 请求参数
	 */
	@Nonnull
	protected final Messager<JSONObject> sendRequest(HttpMethod method, URI uri, Map<String, Object> params, int flags) {

		params = preInit(params);

		final HttpRequest.Builder builder = createRequest(method, uri, params, flags);

		final String body = applyRequestData(builder, method, uri, params);

		return doRequest(builder.build(), body, flags);
	}

	// 请求响应超时时间
	protected static Duration shortRequestTimeout = Duration.of(10, ChronoUnit.SECONDS);

	/** 返回当前API请求对应的 "Content-Type" 请求头值 */
	protected String contentType(HttpMethod method) {
		return method != HttpMethod.GET && requestBodyAsJson() ? "application/json" : "application/x-www-form-urlencoded";
	}

	/**
	 * 创建 请求Builder 对象（但尚未应用 请求方式和数据）
	 */
	protected HttpRequest.Builder createRequest(HttpMethod method, URI uri, Map<String, Object> params, int flags) {
		final Duration timeout = BizFlag.hasFlag(flags, FLAG_LONG_TIMEOUT) ? defaultRequestTimeout : shortRequestTimeout;
		return HttpRequest.newBuilder(uri)
				.timeout(timeout)
				.setHeader("Content-Type", contentType(method))
				.setHeader("Accept", "application/json")
				.setHeader("Accept-Encoding", "gzip");
	}

	/**
	 * 应用请求方式 以及 请求数据
	 *
	 * @return 返回请求主体（明文），仅用于日志记录（因此不一定要返回全部报文）
	 */
	@Nullable
	protected String applyRequestData(HttpRequest.Builder builder, HttpMethod method, URI uri, @Nullable Map<String, Object> params) {
		if (method == HttpMethod.GET) {
			if (X.isValid(params)) {
				uri = URI.create(mergeGetUri(uri.toString(), params));
				builder.uri(uri);
			}
			return null;
		}
		return attachRequestBody(builder, method, params);
	}

	/**
	 * 应用请求方式 以及 请求数据
	 *
	 * @return 返回请求主体（明文），仅用于日志记录（因此不一定要返回全部报文）
	 */
	protected String attachRequestBody(HttpRequest.Builder builder, HttpMethod method, @Nullable Map<String, Object> params) {
		String body = requestBodyAsJson() ? Jsons.encodeRaw(params) : mapToParams(params);
		builder.method(method.name(), HttpRequest.BodyPublishers.ofString(body));
		return body;
	}

	/**
	 * @param method 请求方式
	 * @param uri 请求地址
	 * @param params 请求参数
	 */
	@Nonnull
	protected final Messager<JSONObject> sendRequest(HttpMethod method, URI uri, @Nullable Map<String, Object> params) {
		return sendRequest(method, uri, params, 0);
	}

	@Nonnull
	protected static String mergeGetUri(String apiRoot, @Nullable Map<String, ?> requiredParams) {
		if (X.isValid(requiredParams)) {
			StringBuilder sb = clipGetUri(apiRoot);
			return mapToParams(sb.append('?'), false, requiredParams, null).toString();
		}
		return apiRoot;
	}

	@Nonnull
	protected static StringBuilder clipGetUri(String apiRoot) {
		return new StringBuilder(128).append(apiRoot);
	}

	/**
	 * 指示本次响应是否正常
	 */
	protected boolean responseStatusAsOK(final HttpResponse<String> response) {
		return response.statusCode() == 200;
	}

	/**
	 * 指示本次响应的内容是否为 JSON 格式
	 *
	 * @see #responseStatusAsOK(HttpResponse)
	 */
	protected boolean responseJson(final HttpResponse<String> response) {
		return responseStatusAsOK(response);
	}

	/**
	 * 本次响应的响应类型是否为 JSON 格式
	 */
	protected boolean contentTypeIsJson(final HttpResponse<String> response) {
		return response.headers().firstValue("content-type").orElse("application/json").contains("/json");
	}

	protected <T> HttpResponse<T> sendWithRetry(HttpClient httpClient, HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler) throws IOException, IllegalThreadStateException {
		return sendWithRetryWhenIOE(httpClient, request, bodyHandler, true);
	}

	protected static HttpRequest degradeHttpVersion(HttpRequest request) {
		return HttpRequest.newBuilder(request, (n, v) -> true).version(HttpClient.Version.HTTP_1_1).build();
	}

	protected HttpClient currentClient() {
		return defaultClient();
	}

	protected JSONObject responseBodyToJSON(String responseBody) {
		return Jsons.parseObject(responseBody);
	}

	/**
	 * 对已经装配好的响应结果进行后置处理
	 *
	 * @param result 基于引用传递，可以直接修改
	 * @return 响应主体数据
	 */
	protected String postHandleResult(final Messager<JSONObject> result, String responseBody, HttpResponse<String> response) {
		return responseBody;
	}

	/**
	 * @param request 请求对象
	 * @param body 将传入 request 的请求数据作为独立参数传递进来，便于内部进行统一日志记录
	 * @param flags 参考 {@link #FLAG_LONG_TIMEOUT}、{@link #FLAG_RETURN_TEXT}
	 * @return 属性 status == "OK" 表示响应正常（响应码 200）。返回的 data 属性是响应内容，callback 可能是响应内容对应的结构化数据（例如：JSONObject）
	 * @throws UncheckedIOException 网络IO异常（请求连接超时、响应超时等）
	 * @throws ErrorMessageException 线程中断异常（服务被打断，对外提示网络异常）
	 */
	@Nonnull
	protected Messager<JSONObject> doRequest(HttpRequest request, String body, final int flags) throws UncheckedIOException, ErrorMessageException {
		String responseBody = null;
		Exception ex = null;
		int statusCode = 200;
		try {
			HttpResponse<String> response = sendWithRetry(currentClient(), request, EnhanceHttpResponseBodyHandler.instance);
			statusCode = response.statusCode();
			responseBody = response.body();
			final Messager<JSONObject> result = new Messager<>();
			if (!BizFlag.hasFlag(flags, FLAG_RETURN_TEXT) && responseJson(response)) { // 如果响应内容为 JSON，则装配为 data
				JSONObject json = responseBodyToJSON(responseBody);
				result.setPayload(json);
				String status = mapStatus(json, response); // 有可能为 null
				result.setStatus(status);
			} else {
				result.setCallback(responseBody);
				if (responseStatusAsOK(response)) {
					result.setOK();
				}
			}
			if (!result.isOK()) {
				result.setCode(statusCode);
				switch (statusCode) {
					case 502, 503 -> result.setStatus(STATUS_MAINTAIN);
					case 404 -> result.setStatus("404");
				}
			}
			responseBody = postHandleResult(result, responseBody, response);
			return result;
		} catch (IOException e) {
			ex = e;
			raiseTimeout(getProvider(), e);
			throw new UncheckedIOException(e);
		} catch (IllegalThreadStateException e) {
			ex = e;
			throw new ErrorMessageException(Messager.status(MessagerStatus.BUSY, I18N.msg(BaseI18nKey.NETWORK_ABORT)), e);
		} finally {
			final String uri = uriToString(request.uri(), BizFlag.hasFlag(flags, FLAG_LOG_DECODED_URI));
			if (ex == null) {
				if (statusCode == 200) {
					if (shouldSplit(responseBody)) {
						// 如果返回的数据太多，上报到 ES 将会报错，因此需要拆分处理
						final int length = responseBody.length();
						int startPos = 0, endPos;
						do {
							endPos = StringUtils.indexOf(responseBody, '}', startPos + allowMaxByteSize - 1000);
							if (endPos == -1) {
								endPos = length - 1;
							}
							LOGGER.info("【{}游戏】请求地址：{}\n请求参数：{}\n返回数据：{}", getProvider(), uri, body, StringUtils.substring(responseBody, startPos, endPos + 1));
							startPos = endPos + 1;
						} while (startPos < length);
					} else {
						LOGGER.info("【{}游戏】请求地址：{}\n请求参数：{}\n返回数据：{}", getProvider(), uri, body, responseBody);
					}
				} else { // 非正常响应时记录 响应码
					LOGGER.warn("【{}游戏】请求地址：{}\n请求参数：{}\n响应码={}，返回数据：{}", getProvider(), uri, body, statusCode, responseBody);
				}
			} else {
				LOGGER.error("【" + getProvider() + "游戏】请求地址：" + uri + "\n请求参数：" + body, ex);
			}
		}
	}

	/**
	 * 将 URI 转为字符串
	 * <code>https://example.com/api/path?from=2025-01-08%2007:20:01&to=2025-01-08%2007:21:01</code>
	 * <ul>
	 * <li>{@link URI#getPath() } => <code>"/api/path"</code>
	 * <li>{@link URI#getQuery()} () } => <code>"from=2025-01-08 07:20:01&to=2025-01-08 07:21:01"</code>
	 * <li>{@link URI#getRawQuery()} () } => <code>"from=2025-01-08%2007:20:01&to=2025-01-08%2007:21:01"</code>
	 * </ul>
	 *
	 * @param decodeRequired 是否进行URL解码处理，以提高可读性。如果为 true，将会对特殊字符进行URL解码 => <code>https://example.com/api/path?from=2025-01-08 07:20:01&to=2025-01-08 07:21:01</code>
	 */
	private static String uriToString(URI uri, boolean decodeRequired) {
		if (decodeRequired) {
			final String rawQuery = uri.getRawQuery();
			if (StringUtil.notEmpty(rawQuery)) {
				final String query = uri.getQuery();
				//noinspection StringEquality
				if (rawQuery != query) {
					final String origin = uri.toString();
					final int originLength = origin.length();
					//noinspection StringBufferReplaceableByString
					return new StringBuilder(originLength)
							.append(origin, 0, originLength - rawQuery.length())
							.append(query)
							.toString();
				}
			}
		}
		return uri.toString();
	}

	/** 厂商API请求超时计数器：-N=相对正常次数；0=初始化；N=相对超时次数 */
	static final long[] apiTimeoutCounter = new long[BetProvider.values().length];
	private transient ScheduledFuture<?> scheduledFuture;
	/** API请求 健康检查、超时熔断 阈值 */
	static final int timeoutHealthCheckThreshold = 5, timeoutBreakThreshold = 20;

	/** 请求超时监视器 */
	private void raiseTimeout(BetProvider provider, IOException e) {
		if (e instanceof HttpTimeoutException) {
			final int index = provider.ordinal();
			long count = apiTimeoutCounter[index];
			if (count < timeoutBreakThreshold + 15) { // 低于阈值就自增
				count = ++apiTimeoutCounter[index];
				if (count >= timeoutHealthCheckThreshold && scheduledFuture == null) { // 超过检查阈值就启动健康检查
					smartHealthCheck(provider);
				}
			}
		}
	}

	private synchronized void smartHealthCheck(BetProvider provider) {
		if (scheduledFuture == null) {
			final int index = provider.ordinal();
			LOGGER.info("【{}游戏】开启健康检查", provider);
			scheduledFuture = LocalScheduler.getScheduler().scheduleAtFixedRate(() -> {
				boolean success = false;
				try {
					ping();
					success = true;
				} catch (Exception e) {
					LOGGER.error("【{}游戏】健康检查出现异常", provider, e);
				} finally {
					LOGGER.info("【{}游戏】本次健康检查{}：{}", provider, success ? "通过" : "失败", apiTimeoutCounter[index]);
				}
				if (--apiTimeoutCounter[index] <= 0) {
					LOGGER.info("【{}游戏】结束健康检查", provider);
					final ScheduledFuture<?> future = scheduledFuture;
					if (future != null) {
						future.cancel(true);
						scheduledFuture = null;
					}
				}
			}, 6000);
		}
	}

	static final int allowMaxByteSize = 100_0000;

	static boolean shouldSplit(String text) {
		if (text.length() < allowMaxByteSize - 1000) {
			return false;
		}
		byte[] bytes;
		try {
			bytes = JDKUtils.STRING_VALUE.apply(text);
		} catch (Throwable e) {
			LOGGER.warn("反射获取游戏API响应字节数组时出错：" + e.getMessage());
			bytes = text.getBytes(StandardCharsets.ISO_8859_1);
		}
		return bytes.length > allowMaxByteSize;
	}

	/**
	 * 基于代理配置信息创建HTTP客户端
	 */
	public static HttpClient.Builder prepareProxyClient(String host, int port, @Nullable String username, @Nullable String password) {
		final HttpClient.Builder builder = HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_1_1)
				.connectTimeout(Duration.of(5, ChronoUnit.SECONDS))
				.proxy(ProxySelector.of(new InetSocketAddress(host, port)));

		if (StringUtil.notEmpty(username)) {
			final PasswordAuthentication authentication = new PasswordAuthentication(username, password.toCharArray());
			builder.authenticator(new Authenticator() {
				@Override
				protected PasswordAuthentication getPasswordAuthentication() {

					if (getRequestorType() == RequestorType.PROXY) {
						return authentication;
					}
					return null;
				}
			});
		}

		return builder;
	}

	/**
	 * 基于代理配置信息创建HTTP客户端
	 *
	 * @param proxyConfig 形如 <code> "proxy://username:password@host:port" </code>
	 */
	public static HttpClient.Builder prepareProxyClient(@Nonnull String proxyConfig) {
		// "proxy://username:password@host:port"
		final URI uri = URI.create(proxyConfig);
		String username = null, password = null;
		final String userInfo = uri.getUserInfo();
		if (StringUtil.notEmpty(userInfo)) {
			final String[] parts = userInfo.split(":", 2);
			username = parts[0];
			password = parts[1];
		}
		return prepareProxyClient(uri.getHost(), uri.getPort(), username, password);
	}

	/**
	 * 基于代理配置信息创建HTTP客户端，如果没有配置代理，则使用默认的HTTP客户端
	 *
	 * @param proxyConfig 形如 <code> "proxy://username:password@host:port" </code>
	 */
	public static HttpClient getProxyOrDefaultClient(@Nullable String proxyConfig) {
		return StringUtil.isBlank(proxyConfig) ? defaultClient() : prepareProxyClient(proxyConfig).build();
	}

}
