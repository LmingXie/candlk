package com.candlk.common.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import javax.net.ssl.*;

import com.candlk.common.context.Context;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.StringUtil;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * HTTP请求相关处理工具类
 *

 * @since 2014年11月1日
 */
@Slf4j
public abstract class HttpUtil {

	static HttpClient httpClient;

	public static HttpClient getClient() {
		HttpClient client = httpClient;
		if (client == null) {
			httpClient = client = Context.getBean(HttpClient.class, HttpUtil::createAllowAllHostnameClient);
		}
		return client;
	}

	public static final RequestConfig DEFAULT_REQUEST_CONFIG = customRequestConfigBuilder().build();

	public static RequestConfig.Builder customRequestConfigBuilder() {
		return RequestConfig.custom()
				.setConnectTimeout(3_000) // 建立连接的超时时间
				.setSocketTimeout(30_000) // 客户端与服务器端交互的超时时间，两个数据包的传输间隔时间不能超过该值
				.setConnectionRequestTimeout(5_000) // 从连接池获取连接的等待超时时间
				;
	}

	/**
	 * 发起一个常规的HTTP请求，并设置相应的HTTP请求头
	 */
	public static HttpResponse doHTTP(HttpUriRequest request, @Nullable Map<String, String> headers) throws IllegalStateException {
		return doHTTP(getClient(), request, headers);
	}

	/**
	 * 发起一个常规的HTTP请求，并设置相应的HTTP请求头
	 */
	public static HttpResponse doHTTP(HttpClient client, HttpUriRequest request, @Nullable Map<String, String> headers) throws IllegalStateException {
		appendHeaders(request, headers);
		try {
			return client.execute(request);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * 读取HttpResponse的响应内容，并转换为字符串进行输出
	 */
	public static String responseToString(HttpResponse response) throws IllegalStateException {
		HttpEntity entity = response.getEntity();
		if (entity == null) {
			return null;
		}
		try {
			return EntityUtils.toString(entity, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * 发起一个常规的HTTP GET请求，并设置相应的HTTP请求头
	 */
	public static HttpResponse doGET(String url, @Nullable Map<String, String> headers) {
		return doHTTP(new HttpGet(url), headers);
	}

	/**
	 * 发起一个常规的HTTP GET请求，设置相应的HTTP请求头，并返回字符串形式的响应内容
	 */
	public static String doGet(String url, @Nullable Map<String, String> headers) throws IllegalStateException {
		return responseToString(doHTTP(new HttpGet(url), headers));
	}

	/**
	 * 发起一个常规的HTTP POST请求，设置相应的HTTP请求头，并返回对应的HttpResponse响应对象
	 */
	public static HttpResponse doPOST(String url, @Nullable HttpEntity paramEntity, @Nullable Map<String, String> headers) throws IllegalStateException {
		HttpPost post = new HttpPost(url);
		if (paramEntity != null) {
			post.setEntity(paramEntity);
		}
		return doHTTP(post, headers);
	}

	/**
	 * 发起一个常规的HTTP POST请求，设置相应的HTTP请求头，并返回对应的HttpResponse响应对象
	 */
	public static HttpResponse doPOST(String url, @Nullable String params, @Nullable Map<String, String> headers, @Nullable Charset charset) throws IllegalStateException {
		return doPOST(url, toEntityFromString(params, null, null), headers);
	}

	/**
	 * 将字符串转为 HttpEntity
	 */
	public static StringEntity toEntityFromString(@Nullable String params, @Nullable String contentType, @Nullable Charset charset) {
		if (StringUtil.notEmpty(params)) {
			StringEntity entity = new StringEntity(params, charset == null ? StandardCharsets.UTF_8 : charset);
			entity.setContentType(StringUtil.isEmpty(contentType) ? ContentType.APPLICATION_FORM_URLENCODED.getMimeType() : contentType);
			return entity;
		}
		return null;
	}

	/**
	 * 将Map集合转为 HttpEntity
	 */
	public static UrlEncodedFormEntity toEntityFromMap(@Nullable Map<String, String> paramMap, @Nullable Charset charset) {
		if (paramMap != null && paramMap.size() > 0) {
			List<NameValuePair> list = new ArrayList<>(paramMap.size());
			for (Entry<String, String> entry : paramMap.entrySet()) {
				list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
			}
			return new UrlEncodedFormEntity(list, charset == null ? StandardCharsets.UTF_8 : charset);
		}
		return null;
	}

	/**
	 * 发起一个常规的HTTP POST请求，设置相应的HTTP请求头，并返回字符串形式的响应内容
	 *
	 * @param contentType 默认为 <code>"application/x-www-form-urlencoded"</code>
	 * @param charset 默认为 {@link StandardCharsets#UTF_8}
	 */
	public static String doPost(String url, @Nullable String params, @Nullable Map<String, String> headers, @Nullable String contentType, @Nullable Charset charset) throws IllegalStateException {
		return responseToString(doPOST(url, toEntityFromString(params, contentType, charset), headers));
	}

	/**
	 * 发起一个常规的HTTP POST请求，设置相应的HTTP请求头，并返回字符串形式的响应内容
	 */
	public static String doPost(String url, @Nullable String params, @Nullable Map<String, String> headers) throws IllegalStateException {
		return responseToString(doPOST(url, toEntityFromString(params, null, null), headers));
	}

	/**
	 * 发起一个常规的HTTP POST请求，设置相应的HTTP请求头，并返回字符串形式的响应内容
	 */
	public static String doPost(String url, @Nullable Map<String, String> params, @Nullable Map<String, String> headers) throws IllegalStateException {
		return responseToString(doPOST(url, toEntityFromMap(params, null), headers));
	}

	/**
	 * 向请求对象 HttpUriRequest 中追加多个请求头
	 */
	protected static void appendHeaders(HttpUriRequest request, @Nullable Map<String, String> headers) {
		if (headers != null) {
			for (Entry<String, String> entry : headers.entrySet()) {
				request.setHeader(entry.getKey(), entry.getValue());
			}
		}
	}

	/**
	 * 创建跳过所有域名证书校验的 HttpClient
	 * <p>
	 * TODO 这样做将存在安全隐患
	 */
	public static HttpClient createAllowAllHostnameClient() {
		try {
			SSLContext ctx = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
			X509TrustManager tm = new X509TrustManager() {
				@Override
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

			};
			ctx.init(null, new TrustManager[] { tm }, null);
			final SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(ctx, null, null, NoopHostnameVerifier.INSTANCE);
			return HttpClientBuilder.create()
					.setDefaultRequestConfig(DEFAULT_REQUEST_CONFIG)
					.setSSLSocketFactory(sslSocketFactory)
					.build();
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			log.error("跳过SSL握手校验出错", e);
			return HttpClients.createDefault();
		}
	}

}
