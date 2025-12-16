package com.bojiu.webapp.user.bet.impl;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.List;

import com.alibaba.fastjson2.JSONObject;
import com.bojiu.common.model.Messager;
import com.bojiu.common.util.SpringUtil;
import com.bojiu.webapp.user.bet.BaseBetApiImpl;
import com.bojiu.webapp.user.dto.GameDTO;
import com.bojiu.webapp.user.model.BetProvider;
import com.bojiu.webapp.user.model.OddsType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KyBetImpl extends BaseBetApiImpl {

	@Override
	public BetProvider getProvider() {
		return BetProvider.KY;
	}

	public OddsType getOddsType(String hpid) {
		return switch (hpid) {
			case "4" -> OddsType.R; // 全场让球
			case "2" -> OddsType.OU; // 全场大小
			case "1" -> OddsType.M; // 全场独赢
			case "19" -> OddsType.HR; // 上半场让球
			case "18" -> OddsType.HOU; // 上半场大小
			case "17" -> OddsType.HM; // 上半场独赢
			case "12" -> OddsType.TS; // 两队都进球
			case "15" -> OddsType.EO; // 全场单双
			default -> throw new IllegalArgumentException("未知的盘口类型：" + hpid);
		};
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
	public List<GameDTO> getGameBets() {
		// TODO 查询五大联赛ID列表

		// TODO 根据ID查询赛事信息列表
		return List.of();
	}

	protected URI buildURI(String url) {
		return URI.create(this.getConfig().endPoint + url + "?t=" + System.currentTimeMillis());
	}

	@Override
	public Messager<Void> ping() {
		Messager<JSONObject> result = sendRequest(HttpMethod.GET, buildURI("/yewu11/v1/getSystemTime/currentTimeMillis"), null);
		if (!result.isOK()) { // 标记为维护状态
			result.setStatus(STATUS_MAINTAIN);
		}
		return result.castDataType(null);
	}

}