package com.bojiu.webapp.user.bet.impl;

import java.net.URI;
import java.net.http.*;
import java.util.*;

import com.alibaba.fastjson2.JSONObject;
import com.bojiu.common.model.Messager;
import com.bojiu.common.util.SpringUtil;
import com.bojiu.webapp.user.bet.BaseBetApiImpl;
import com.bojiu.webapp.user.dto.GameDTO;
import com.bojiu.webapp.user.dto.ScoreResult;
import com.bojiu.webapp.user.model.BetProvider;
import lombok.extern.slf4j.Slf4j;
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

	protected Messager<JSONObject> doGetLogin(String langx) {
		final Map<String, Object> params = new TreeMap<>();
		params.put("loginId", this.getConfig().username);
		params.put("password", this.getConfig().password);
		return sendRequest(HttpMethod.POST, buildURI(ver), params, FLAG);
	}

	@Override
	protected HttpRequest.Builder createRequest(HttpMethod method, URI uri, Map<String, Object> params, int flags) {
		HttpRequest.Builder builder = super.createRequest(method, uri, params, flags);
		builder.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
		builder.setHeader("Origin", "https://user-pc-new.zlshelves.com");
		builder.setHeader("Referer", "https://user-pc-new.zlshelves.com/");
		builder.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36");
		final String token = getConfig().token;
		builder.setHeader("checkId", "pc-" + token + "-" + userId + "-" + System.currentTimeMillis());
		builder.setHeader("lang", params == null ? getDefaultLanguage() : (String) params.remove("lang"));
		builder.setHeader("request-code", "{\"panda-bss-source\":\"2\"}");
		builder.setHeader("requestId", token);
		builder.setHeader("sec-ch-ua", "\"Google Chrome\";v=\"143\", \"Chromium\";v=\"143\", \"Not A(Brand\";v=\"24\"");
		builder.setHeader("sec-ch-ua-mobile", "?0");
		builder.setHeader("sec-ch-ua-platform", "\"Windows\"");
		return builder;
	}

	protected URI buildURI(String url) {
		return URI.create(this.getConfig().endPoint + url + "?t=" + System.currentTimeMillis());
	}

	@Override
	protected String mapStatus(JSONObject json, HttpResponse<String> response) {
		final String errorCode = (String) getErrorCode(json);
		if (!"0000000".equals(errorCode)) {
			if ("0401013".equals(errorCode)) { // 掉登录
				SpringUtil.logError(log, () -> "【" + getProvider() + "】掉登录，请手动补登录，errCode：0401013");
			}
			log.warn("接口响应错误：{}", json.getString("msg"));
			return null;
		}
		return Messager.OK;
	}

	@Override
	protected Object getErrorCode(JSONObject json) {
		return json.getString("code");
	}

	@Override
	protected boolean requestBodyAsJson() {
		return true;
	}

	@Override
	public Messager<Void> ping() {
		Messager<JSONObject> result = sendRequest(HttpMethod.GET, buildURI("/yewu11/v1/getSystemTime/currentTimeMillis"), null);
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