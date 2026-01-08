package com.bojiu.webapp.user.bet.impl;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.TreeMap;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.bojiu.common.model.ErrorMessageException;
import com.bojiu.common.model.Messager;
import com.bojiu.context.web.Jsons;
import com.bojiu.webapp.user.model.BetProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class D1ceBetImpl extends HgBetImpl {

	@Override
	public BetProvider getProvider() {
		return BetProvider.D1CE;
	}

	@Override
	public Pair<String, String> getVersion() {
		return Pair.of("-3ed5-iovation-0108-95881ae5676be3", "HDICAD");
	}

	final String clientId = "9dd3e87b3c8d466e8fd4dbaaab5be206", appId = "684bdb1fe87d097395894484", integrationId = "684bdb29e87d097395894520";
	/** D1CE登录URI */
	final URI loginUri = URI.create("https://web-api.d1ce.com/api/auth/login"),
	/** D1CE登录 zendesk 上报URI */
	zendeskLoginUri = URI.create("https://d1ce.zendesk.com/sc/sdk/v2/apps/" + appId + "/login"),
	/** 检查余额URI */
	gameCheckUri = URI.create("https://web-api.d1ce.com/api/game/check"),
	/** 皇冠游戏URI */
	gameUri = URI.create("https://live-api.d1ce.com/api/game/uyRWMwQzZATqI0mc_VQ-D?lang=zh-CN&wallet=USDT");

	transient String accessToken;

	private String getAccessToken() {
		if (accessToken == null) {
			doLogin();
		}
		return accessToken;
	}

	final JSONObject loginZendeskClient = Jsons.parseObject("{"
			+ "        \"platform\": \"web\","
			+ "        \"id\": \"" + clientId + "\","
			+ "        \"integrationId\": \"" + integrationId + "\","
			+ "        \"info\": {"
			+ "            \"vendor\": \"zendesk\","
			+ "            \"sdkVersion\": \"0.1\","
			+ "            \"URL\": \"d1ce.com\","
			+ "            \"userAgent\": \"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36\","
			+ "            \"referrer\": \"https://live.d1ce.com/\","
			+ "            \"browserLanguage\": \"zh-CN\","
			+ "            \"currentUrl\": \"https://d1ce.com/zh-Hans\","
			+ "            \"currentTitle\": \"D1CE - All your bet, gamble and crypto in one site.\""
			+ "        }"
			+ "    }");

	@Override
	protected @NonNull Messager<JSONObject> doGetLogin() {
		final Map<String, Object> params = new TreeMap<>();
		final String email = getConfig().username;
		params.put("email", email);
		params.put("password", getConfig().password);
		// 登录D1CE获取 access_token
		Messager<JSONObject> result = sendRequest(HttpMethod.POST, loginUri, params);
		if (result.isOK()) {
			JSONObject data = result.data();
			accessToken = JSONPath.of("$.result.userSession.access_token", String.class).eval(data).toString();

			params.clear(); // 清除冗余参数

			// 登录上报jwt_token
			result = sendRequest(HttpMethod.POST, gameCheckUri, params);
			if (result.isOK()) {
				params.clear(); // 清除冗余参数
				// 从D1CE 获取皇冠游戏链接
				result = sendRequest(HttpMethod.GET, gameUri, params);
				if (result.isOK()) {
					// 进入游戏页面解析版本信息以及 uid
					final Object eval = JSONPath.of("$.data.game_url", String.class).eval(result.data());
					if (eval instanceof String gameUrl) {
						result = sendRequest(HttpMethod.GET, URI.create(gameUrl), params, FLAG);
						final String html = result.getCallback();

						String beginStr = "globalTop.ver = '";
						int begin = html.indexOf(beginStr) + beginStr.length();
						final String ver = html.substring(begin, html.indexOf("';", begin));
						beginStr = "globalTop.iovationKey = '";
						begin = html.indexOf(beginStr) + beginStr.length();
						final String iovationKey = html.substring(begin, html.indexOf("';", begin));

						// 初始化版本信息的缓存
						setVersion(Pair.of(ver, iovationKey), null);

						beginStr = "globalTop.uid = '"; // 解析uid
						begin = html.indexOf(beginStr) + beginStr.length();
						final String uid = html.substring(begin, html.indexOf("';", begin));

						// 返回正确的登录信息，在父级会进行缓存
						return result.setStatus(Messager.OK).setData(JSONObject.of("uid", uid));
					}
				}
			}
		}
		throw new ErrorMessageException("D1CE 登录失败");
	}

	@Nullable
	protected String applyRequestData(HttpRequest.Builder builder, HttpMethod method, URI uri, @Nullable Map<String, Object> params) {
		if (uri == zendeskLoginUri) {
			final String body = Jsons.encodeRaw(params);
			builder.method(method.name(), HttpRequest.BodyPublishers.ofString(body));
			return body;
		} else {
			return super.applyRequestData(builder, method, uri, params);
		}
	}

	@Override
	protected HttpRequest.Builder createRequest(HttpMethod method, URI uri, Map<String, Object> params, int flags) {
		HttpRequest.Builder builder = super.createRequest(method, uri, params, flags);
		boolean isGameUri = uri == gameUri, isLogin2Uri = uri == zendeskLoginUri, isGameCheck = uri == gameCheckUri;
		if (uri == loginUri || isGameUri || isLogin2Uri || isGameCheck) {
			if (isGameUri) {
				builder.setHeader("Accept", "application/json, text/plain, */*");
				builder.setHeader("origin", "https://live.d1ce.com");
				builder.setHeader("referer", "https://live.d1ce.com/");
			} else {
				builder.setHeader("origin", "https://d1ce.com");
				builder.setHeader("referer", "https://d1ce.com/");
				if (isLogin2Uri) {
					builder.setHeader("Content-Type", "application/json");
					builder.setHeader("authorization", "Bearer " + params.remove("jwtToken"));
					builder.setHeader("x-smooch-appid", appId);
					builder.setHeader("x-smooch-clientid", clientId);
					builder.setHeader("x-smooch-sdk", "web/zendesk/1.0.0+sha.f2a00db");
					builder.setHeader("x-zendesk-integrationid", integrationId);
				} else if (isGameCheck) {
					builder.setHeader("Content-Type", "application/json");
					builder.setHeader("access-token", accessToken);
				}
			}
			builder.setHeader("accept-language", "zh-CN,zh;q=0.9");
			builder.setHeader("locale", "en");
			builder.setHeader("priority", "u=1, i");
			builder.setHeader("sec-ch-ua", "\"Google Chrome\";v=\"143\", \"Chromium\";v=\"143\", \"Not A(Brand\";v=\"24\"");
			builder.setHeader("sec-ch-ua-mobile", "?0");
			builder.setHeader("sec-ch-ua-platform", "\"Windows\"");
			builder.setHeader("sec-fetch-dest", "empty");
			builder.setHeader("sec-fetch-mode", "cors");
			builder.setHeader("sec-fetch-site", "same-site");
			builder.setHeader("userAgent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36");
			final String cookie = isGameUri
					? "_ga=GA1.1.57847954.1765177541; iorChgSw=Y; cu=N; cuipv6=N; ipv6=N; myGameVer_1643=_211228; ft_myGame_1643={}; protocolstr=https; connect.sid=s%3Aj1-xedAIzsAs-cekWdkGKyK4-ucLd7qt.plBzW3IhISTCnByHmAw%2BfNlDmCf0cN15bhPjnMeXVpA; _ga_PV8PN1PMTY=GS2.1.s1765782172$o7$g1$t"
					+ (System.currentTimeMillis() / 1000) + "$j2$l0$h0; access_token=" + getAccessToken()
					: "_ga=GA1.1.57847954.1765177541; iorChgSw=Y; cu=N; cuipv6=N; ipv6=N; myGameVer_1643=_211228; ft_myGame_1643={}; protocolstr=https; connect.sid=s%3AGs3BerPyVaqJI62cbzi-NWrxebbPp6M6.mUyh8d3%2F3Tblp23PP9OmqIPww8mX6JbQ13pZqpUntDE; _ga_PV8PN1PMTY=GS2.1.s1765782172$o7$g1$t"
					+ (System.currentTimeMillis() / 1000) + "$j60$l0$h0";
			builder.setHeader("Cookie", cookie);
		}
		return builder;
	}

	@Override
	protected String postHandleResult(Messager<JSONObject> result, String responseBody, HttpResponse<String> response) {
		// C1CE返回 JSON 数据结构，皇冠数据则按 XML 进行解析
		return responseBody.startsWith("{") ? responseBody : super.postHandleResult(result, responseBody, response);
	}

	@Override
	protected boolean extErrVerify(JSONObject json, HttpResponse<String> response) {
		URI uried = response.uri();
		if (uried == loginUri || uried == gameUri || uried == gameCheckUri) {
			return ((uried == loginUri || uried == gameCheckUri) && "1".equals(json.getString("status")))
					|| (uried == gameUri && "true".equals(json.getString("success")))
					|| (uried == zendeskLoginUri && json.getJSONObject("appUser") != null);
		}
		return true;
	}

	@Override
	public String getSourceResult() {
		return "xktkruvxm39824030l120308b0"; // TODO: 2026/1/8 暂时固定死
	}

	@Override
	protected int getTimeoutDay() {
		return 6;
	}

}