package com.candlk.common.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.candlk.common.context.Context;
import me.codeplayer.util.CollectionUtil;
import me.codeplayer.util.X;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Logs {

	public static final String REQUEST_BODY = Logs.class.getName() + ".requestBody";
	public static final String RESPONSE = Logs.class.getName() + ".response";
	public static final String EXCEPTION = Logs.class.getName() + ".exception";

	public static final Logger LOGGER = LoggerFactory.getLogger(Logs.class);

	public static void logJSON(@Nullable Long merchantId, @Nullable Long memberId, String appName, String requestMethod, String requestURI, JSONObject headersJson,
	                           String params, String ip, long time, long useTimeMs, @Nullable Object retVal, @Nullable Throwable e) {

		JSONObject json = new JSONObject();
		json.put("app", appName);
		if (memberId != null) {
			json.put("mid", merchantId);
			json.put("userId", memberId);
		}
		json.put("method", requestMethod);
		json.put("uri", requestURI);
		json.put("headers", headersJson);
		json.put("params", params);
		json.put("ip", ip);
		json.put("time", time);
		json.put("useTimeMs", useTimeMs);
		json.put("response", retVal instanceof String ? retVal : JSON.toJSONString(retVal));
		if (e != null) {
			json.put("ex", e.toString());
		}
		LOGGER.info(json.toJSONString());
	}

	public static String toRequestBody(HttpServletRequest request) throws IOException {
		String body = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
		request.setAttribute(REQUEST_BODY, body);
		return body;
	}

	/**
	 * 记录指定请求的参数数据，格式为：
	 *
	 * <pre>
	 * $prefix[userId=$userId，IP=$IP]
	 * 参数：a=aValue&b=bValue
	 * </pre>
	 */
	public static StringBuilder logRequest(final HttpServletRequest request, @Nullable StringBuilder sb, @Nullable String prefix,
	                                       @Nullable final Consumer<String> jsonBodySetter,
	                                       @Nullable final Consumer<Map<String, String>> paramMapSetter,
	                                       @Nullable String... headerNames) {
		final Map<String, String[]> paramMap = request.getParameterMap();
		if (sb == null) {
			sb = new StringBuilder(32 + (paramMap.size() << 4));
		}
		if (prefix != null) {
			sb.append(prefix);
		}
		sb.append("[IP=").append(Context.get().getClientIP(request)).append("] ").append(request.getMethod()).append(' ').append(request.getRequestURL());
		if (X.isValid(headerNames)) {
			boolean first = true;
			for (String name : headerNames) {
				String value = request.getHeader(name);
				if (value == null) {
					continue;
				}
				if (first) {
					sb.append("\n请求头：");
					first = false;
				}
				sb.append('\n').append(name).append(": ").append(value);
			}
		}
		sb.append("\n请求数据：");
		final String contentType = request.getContentType();
		if (StringUtils.contains(contentType, "json")) {
			String requestData;
			try {
				requestData = IOUtils.toString(request.getReader());
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
			if (jsonBodySetter != null) {
				jsonBodySetter.accept(requestData);
			}
			sb.append('\n').append(requestData);
		} else {
			final Map<String, String> map = CollectionUtil.newHashMap(paramMap.size());
			boolean notFirst = false;
			for (Entry<String, String[]> entry : paramMap.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue()[0];
				map.put(key, value);
				if (notFirst) {
					sb.append('&');
				} else {
					notFirst = true;
				}
				sb.append(key).append('=').append(value);
			}
			if (paramMapSetter != null) {
				paramMapSetter.accept(map);
			}
			// 如果是文件上传，追加结尾
			final boolean uploadFile = "POST".equals(request.getMethod()) && StringUtils.startsWith(contentType, "multipart/");
			if (uploadFile) {
				if (!paramMap.isEmpty()) {
					sb.append('&');
				}
				sb.append("[[文件]]");
			}
		}
		return sb.append('\n');
	}

}
