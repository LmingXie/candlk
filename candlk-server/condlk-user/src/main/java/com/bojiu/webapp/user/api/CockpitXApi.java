package com.bojiu.webapp.user.api;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.util.TreeMap;

import com.alibaba.fastjson2.JSONObject;
import com.bojiu.common.model.Messager;
import com.bojiu.common.util.BaseHttpUtil;
import com.bojiu.context.web.Jsons;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CockpitXApi extends BaseHttpUtil {

	public static final String baseURI = "http://127.0.0.1:8985";
	public static final URI loadUri = URI.create(baseURI + "/user/j/load"),
			closeUri = URI.create(baseURI + "/user/j/close");

	final HttpClient proxyHttpClient;

	public CockpitXApi() {
		this.proxyHttpClient = defaultClient();
	}

	/**
	 * @return 返回的 data 属性 是 JSONObject，返回的 ext 响应的原始文本
	 */
	public Messager<JSONObject> doRequest(HttpMethod method, URI uri, String body) {
		HttpRequest.Builder builder = requestBuilder(method, uri, body, true);
		String responseBody = null;
		Exception ex = null;
		try {
			HttpResponse<String> response = doSend(this.proxyHttpClient, builder.build(), responseBodyHandler);
			responseBody = response.body();
			if (response.statusCode() == 200) {
				final JSONObject json = Jsons.parseObject(responseBody);
				return Messager.hideData(json).setExt(responseBody);
			}
			return new Messager<JSONObject>().setExt(responseBody);
		} catch (IOException | IllegalThreadStateException e) {
			ex = e;
			return Messager.status(Messager.ERROR);
		} finally {
			if (ex == null) {
				log.info("【CockpitX】请求地址：{}\n请求参数：{}\n返回数据：{}", uri, body, responseBody);
			} else {
				log.error("【CockpitX】请求地址：" + uri + "\n请求参数：" + body, ex);
			}
		}
	}

	/**
	 * @return 返回的 data 属性 是 JSONObject，返回的 ext 响应的原始文本
	 */
	public Messager<JSONObject> doGet(URI uri) {
		return doRequest(HttpMethod.GET, uri, null);
	}

	public Messager<String> load(Long id) {
		return sendReq(id, loadUri);
	}

	public Messager<String> close(Long id) {
		return sendReq(id, closeUri);
	}

	private Messager<String> sendReq(Long id, URI uri) {
		TreeMap<String, Object> params = new TreeMap<>();
		params.put("id", id);
		Messager<JSONObject> resp = doRequest(HttpMethod.POST, uri, Jsons.encodeRaw(params));
		if (resp.isOK()) {
			return resp.castDataType(null);
		}
		return null;
	}

}