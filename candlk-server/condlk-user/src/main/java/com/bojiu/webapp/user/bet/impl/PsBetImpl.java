package com.bojiu.webapp.user.bet.impl;

import java.net.URI;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.bojiu.common.model.Messager;
import com.bojiu.common.redis.RedisUtil;
import com.bojiu.context.web.Jsons;
import com.bojiu.webapp.user.bet.BaseBetApiImpl;
import com.bojiu.webapp.user.dto.*;
import com.bojiu.webapp.user.model.BetProvider;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.EasyDate;
import org.apache.commons.lang3.time.FastDateFormat;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PsBetImpl extends BaseBetApiImpl {

	@Override
	public BetProvider getProvider() {
		return BetProvider.PS;
	}

	@Override
	public String getLanguage(String lang) {
		return switch (lang) {
			case LANG_EN -> "en_US";
			case LANG_ZH -> "zh_CN";
			default -> throw new IllegalArgumentException("未知的语言：" + lang);
		};
	}

	protected WebSocket ws;

	@Override
	public Set<GameDTO> getGameBets(String lang) {
		if (ws == null) {

		}
		// TODO: 2026/1/8 登录获得Token
		// TODO: 2026/1/8 兑换wsToken
		// TODO: 2026/1/8 建立WSS链接（使用wsToken）
		/*
		获取数据的方式：
			赛果分数：HTTP
			今日、早盘： WSS
			1、发送订阅消息（并阻塞等待响应 生成随机UUID 映射当前请求线程）
			2、Socket 收到消息后进行解除阻塞（收到g应后解除阻塞的线程并设置响应数据）
		wss://www.ps3838.com/sports-websocket/ws?token=AAAAAARwR7AAAAGbnT-ygWQ6PedV1SFW-mstAcOFXPIrXue-jvfMeei3PUetz66B&ulp=azZlNWJKMlVrUG9WSlpZSThvUS9Ua3o1UWRjQngrUG5ENHpVcFB0YU95bWJFaHE5c0VzYVRiaE5aQkh1ZnQyeUdMMXJJOWQ4dVhWdWNkYzBCbVVsY2c9PXw5MjljMDgxZmQ2NDdiYTIyYjQ5NWY4NGYwZDAwMzVjOQ==
		 TODO: 2026/1/8 查询今日赛事赔率：
		{"type":"UNSUBSCRIBE","destination":"ODDS","body":{
			"sp":29,"lg":"","ev":"","mk":1,"btg":"1","ot":1,
			"d":"","o":1,"l":3,"v":"","lv":"","me":0,"more":false,
			"lang":"","tm":0,"pa":0,"c":"","g":"QQ==","pn":-1,"ec":"",
			"cl":3,"hle":false,"pimo":"0,1,8,39,2,3,6,7,4,5","inl":false,
			"pv":1,"ic":false,"ice":false,"dpVXz":"ZDfaFZUP9","locale":"zh_CN"
			},"id":"015b18d9-b0aa-741d-89e6-7e91307843b9"}
		 */
		return Collections.emptySet();
	}

	transient JSONObject loginInfo;

	protected JSONObject doLogin() {
		if (loginInfo == null) {
			ValueOperations<String, String> opsForValue = RedisUtil.template().opsForValue();
			final String key = getProvider() + "_login", loginJson = opsForValue.get(key);
			if (loginJson != null) {
				loginInfo = Jsons.parseObject(loginJson, new TypeReference<>() {
				});
			} else {
				final Messager<JSONObject> result = doGetLogin(getDefaultLanguage());
				if (result.isOK()) {
					loginInfo = result.data().getJSONObject("tokens");
					opsForValue.set(key, Jsons.encode(loginInfo), 3, TimeUnit.DAYS);
				}
			}
		}
		return loginInfo;
	}

	protected Messager<JSONObject> doGetLogin(String lang) {
		final Map<String, Object> params = new TreeMap<>();
		final BetApiConfig config = this.getConfig();
		params.put("loginId", config.username);
		params.put("password", config.password);
		params.put("Referer", this.getConfig().endPoint + "/" + lang.toLowerCase() + "/sports/soccer");
		return sendRequest(HttpMethod.POST, buildURI("/member-auth/v2/authenticate", lang), params);
	}

	@Nullable
	public String doWsToken() {
		doLogin();
		if (loginInfo != null) {
			final Map<String, Object> params = new TreeMap<>();
			final String lang = getDefaultLanguage();
			params.put("Referer", this.getConfig().endPoint + "/" + lang.toLowerCase() + "/compact/sports");
			Messager<JSONObject> result = sendRequest(HttpMethod.GET, buildURI("/member-auth/v2/wstoken", lang), params);
			if (result.isOK()) {
				return result.data().getString("token");
			}
		}
		return null;
	}

	final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("YYYYMMddhhmm");

	@Override
	protected HttpRequest.Builder createRequest(HttpMethod method, URI uri, Map<String, Object> params, int flags) {
		HttpRequest.Builder builder = super.createRequest(method, uri, params, flags);
		final String endPoint = this.getConfig().endPoint;
		builder.setHeader("origin", endPoint);
		builder.setHeader("referer", (String) params.remove("Referer"));
		builder.setHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36");
		builder.setHeader("x-trust-client", "false");
		if (loginInfo != null) {
			final String nowTime = new EasyDate().toString(DATE_FORMAT);
			final String XCustid = loginInfo.getString("X-Custid");
			final String custid = XCustid != null ? XCustid : "id=ATLUBCP004&login=" + nowTime + "&roundTrip=" + nowTime + "&hash=F9345FC1F820D6B1281512F08F0A88F0";
			final String XBrowserSessionId = loginInfo.getString("X-Browser-Session-Id");

			builder.setHeader("x-app-data", "dpVXz=ZDfaFZUP9;"
					+ "pctag=7cb0d652-ae60-47f2-bf0e-07322c19d9c7;"
					+ "directusToken=TwEdnphtyxsfMpXoJkCkWaPsL2KJJ3lo;"
					+ "BrowserSessionId=" + XBrowserSessionId + ";"
					+ "PCTR=1925630200704;_og=QQ%3D%3D;"
					+ "_ulp=azZlNWJKMlVrUG9WSlpZSThvUS9Ua3o1UWRjQngrUG5ENHpVcFB0YU95bWJFaHE5c0VzYVRiaE5aQkh1ZnQyeUdMMXJJOWQ4dVhWdWNkYzBCbVVsY2c9PXw5MjljMDgxZmQ2NDdiYTIyYjQ5NWY4NGYwZDAwMzVjOQ==;"
					+ "custid=" + custid + ";"
					+ "_userDefaultView=COMPACT;"
					+ "__prefs=W251bGwsMiwxLDAsMSxudWxsLGZhbHNlLDAuMDAwMCx0cnVlLHRydWUsIl8zTElORVMiLDEsbnVsbCx0cnVlLGZhbHNlLGZhbHNlLGZhbHNlLG51bGwsbnVsbCx0cnVlXQ==");
			builder.setHeader("x-custid", custid);
			builder.setHeader("x-lcu", loginInfo.getString("X-Lcu"));
			builder.setHeader("x-u", loginInfo.getString("X-U"));
			builder.setHeader("x-slid", loginInfo.getString("X-SLID"));
			builder.setHeader("x-browser-session-id", XBrowserSessionId);
		}
		return builder;
	}

	protected URI buildURI(String url, String lang) {
		return URI.create(this.getConfig().endPoint + url + "?locale=" + lang + "&_" + System.currentTimeMillis() + "&withCredentials=true");
	}

	@Override
	protected String mapStatus(JSONObject json, HttpResponse<String> response) {
		final String errorCode = (String) getErrorCode(json);
		if (errorCode != null && !"1".equals(errorCode)) {
			return null;
		}
		return Messager.OK;
	}

	@Override
	protected Object getErrorCode(JSONObject json) {
		return json.getString("code");
	}

	@Override
	public Messager<Void> ping() {
		final Messager<JSONObject> result = sendRequest(HttpMethod.GET, buildURI("/member-service/v2/system/status",
				getDefaultLanguage()), new TreeMap<>());
		if (!result.isOK()) { // 标记为维护状态
			result.setStatus(STATUS_MAINTAIN);
		}
		return result.castDataType(null);
	}

	/** 赛果最后一场比赛的结束时间 */
	transient Long lastTime;

	@Override
	public Map<Long, ScoreResult> getScoreResult() {
		return Collections.emptyMap();
	}

	@Override
	public String convertLeague(String league) {
		return league;
	}

}