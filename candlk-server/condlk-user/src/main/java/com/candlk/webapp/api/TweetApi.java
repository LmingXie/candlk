package com.candlk.webapp.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import javax.annotation.Nullable;

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
public class TweetApi extends BaseHttpUtil {

	public static final String baseURI = "https://api.twitter.com/2";
	final String authToken;

	public TweetApi(String authToken) {
		Assert.notBlank(authToken);
		this.authToken = authToken;
	}

	/**
	 * @return 返回的 data 属性 是 JSONObject，返回的 ext 响应的原始文本
	 */
	public Messager<JSONObject> doRequest(HttpMethod method, URI uri, String body) {
		HttpRequest.Builder builder = requestBuilder(method, uri, body, true);
		if (authToken != null) {
			builder.setHeader("TRON-PRO-API-KEY", authToken);
		}
		builder.setHeader("Authorization", "Bearer " + authToken);
		String responseBody = null;
		Exception ex = null;
		try {
			HttpResponse<String> response = doSend(defaultClient(), builder.build(), responseBodyHandler);
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
				log.info("【Twitter】请求地址：{}\n请求参数：{}\n返回数据：{}", uri, body, responseBody);
			} else {
				log.error("【Twitter】请求地址：" + uri + "\n请求参数：" + body, ex);
			}
		}
	}

	/**
	 * @return 返回的 data 属性 是 JSONObject，返回的 ext 响应的原始文本
	 */
	public Messager<JSONObject> doGet(URI uri) {
		return doRequest(HttpMethod.GET, uri, null);
	}

	@Nullable
	protected Long tweets(List<String> ids) {
		final StringBuilder params = new StringBuilder()
				.append("?ids=")
				.append(StringUtil.joins(ids, ","));
		Messager<JSONObject> resp = doGet(URI.create(baseURI + "/tweets" + params));

		// Messager<JSONObject> resp = doRequest(HttpMethod.POST, serverNode.getBlockApi, "{\"detail\":false}"); // 只查询头信息，不查询区块详情
		// if (resp.isOK()) {
		// 	JSONObject root = resp.data(); // 响应数据结构参考：https://developers.tron.network/reference/getblock-1
		// 	if (root != null && root.containsKey("blockID")) {
		// 		return root.getJSONObject("block_header").getJSONObject("raw_data").getLong("number"); // int64 （不知道有没有可能是无符号的）
		// 	}
		// }
		return null;
	}

}
