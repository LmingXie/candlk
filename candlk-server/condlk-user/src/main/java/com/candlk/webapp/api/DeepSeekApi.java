package com.candlk.webapp.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.List;
import java.util.TreeMap;

import com.alibaba.fastjson2.JSONObject;
import com.candlk.common.model.Messager;
import com.candlk.common.util.BaseHttpUtil;
import com.candlk.context.web.Jsons;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.Assert;
import me.codeplayer.util.StringUtil;
import org.springframework.http.HttpMethod;

/**
 * Twitter API
 */
@Getter
@Setter
@Slf4j
public class DeepSeekApi extends BaseHttpUtil {

	public static final String baseURI = "https://api.deepseek.com";
	public static final URI chatUri = URI.create(baseURI + "/chat/completions");

	final String authToken;
	final HttpClient proxyHttpClient;

	public DeepSeekApi(String authToken) {
		Assert.notBlank(authToken);
		this.authToken = authToken;

		this.proxyHttpClient = defaultClient();
	}

	/**
	 * @return 返回的 data 属性 是 JSONObject，返回的 ext 响应的原始文本
	 */
	public Messager<JSONObject> doRequest(HttpMethod method, URI uri, String body) {
		HttpRequest.Builder builder = requestBuilder(method, uri, body, true);
		builder.setHeader("Authorization", "Bearer " + authToken);
		String responseBody = null;
		Exception ex = null;
		try {
			HttpResponse<String> response = doSend(this.proxyHttpClient, builder.build(), responseBodyHandler);
			responseBody = response.body();
			if (response.statusCode() == 200) {
				JSONObject json = Jsons.parseObject(responseBody);
				return Messager.hideData(json).setExt(responseBody);
			}
			return new Messager<JSONObject>().setExt(responseBody);
		} catch (IOException | IllegalThreadStateException e) {
			ex = e;
			return Messager.status(Messager.ERROR);
		} finally {
			if (ex == null) {
				log.info("【DeepSeek】请求地址：{}\n请求参数：{}\n返回数据：{}", uri, body, responseBody);
			} else {
				log.error("【DeepSeek】请求地址：" + uri + "\n请求参数：" + body, ex);
			}
		}
	}

	/**
	 * @return 返回的 data 属性 是 JSONObject，返回的 ext 响应的原始文本
	 */
	public Messager<JSONObject> doGet(URI uri) {
		return doRequest(HttpMethod.GET, uri, null);
	}

	public Messager<DeepSeekChat> chat(String content) {
		TreeMap<String, Object> params = new TreeMap<>();
		params.put("model", "deepseek-chat");
		params.put("messages", List.of(JSONObject.of(
				"role", "user",
				"content", StringUtil.limitChars(content, 1000, "")
		)));
		params.put("stream", false);
		Messager<JSONObject> resp = doRequest(HttpMethod.POST, chatUri, Jsons.encodeRaw(params));
		if (resp.isOK()) {
			final DeepSeekChat chat = resp.data().toJavaObject(DeepSeekChat.class);
			return resp.castDataType(chat);
		}
		return null;
	}

}
