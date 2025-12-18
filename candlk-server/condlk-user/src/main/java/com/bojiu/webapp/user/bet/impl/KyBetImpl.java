package com.bojiu.webapp.user.bet.impl;

import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bojiu.common.model.Messager;
import com.bojiu.common.util.Common;
import com.bojiu.common.util.SpringUtil;
import com.bojiu.webapp.user.bet.BaseBetApiImpl;
import com.bojiu.webapp.user.dto.GameDTO;
import com.bojiu.webapp.user.dto.GameDTO.OddsInfo;
import com.bojiu.webapp.user.model.BetProvider;
import com.bojiu.webapp.user.model.OddsType;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.StringUtil;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KyBetImpl extends BaseBetApiImpl {

	@Override
	public BetProvider getProvider() {
		return BetProvider.KY;
	}

	public OddsType parseOddsType(String hpid) {
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

	/**
	 * 固定拉取的赛事信息
	 * <p>tid的映射写死在 index-BhTHtuiB.js</p>
	 *
	 */
	final Map<String, String> euidToTidMap = Map.of(
			// "6107", "180,239,276,320,79", // 五大联赛
			// "6110", "41837,166,230,37106,10821,17755,7722,5425,3170,3169", // 世界杯-2026
			"3020101", "", // 全部今日赛事列表
			"3020201", "" // 全部早盘赛事
	);

	/** 返回结果为文档，且屏蔽Body输出 */
	final int FLAG = FLAG_LOG_OUT_BRIEF_BODY;

	/*
	可用域名：
		api.q7stajv.com
		api.togav85.com
	 */
	// https://api.togav85.com/yewu11/v2/w/getAllMatchesOddsPB?t=1766047765942
	@Override
	public Set<GameDTO> getGameBets() {
		final Map<String, Object> params = new TreeMap<>();
		final StringBuilder sb = new StringBuilder();
		Set<GameDTO> gameDTOs = new HashSet<>();
		BetProvider provider = getProvider();
		Date now = new Date();
		// 比赛的最小开赛时间
		final long startTimeThreshold = getStartTimeThreshold(now.getTime());
		for (Map.Entry<String, String> entry : euidToTidMap.entrySet()) {
			params.put("cuid", userId);
			params.put("sort", 1);
			params.put("tid", entry.getValue());
			params.put("apiType", 1);
			params.put("category", 1);
			params.put("euid", entry.getKey());

			// 联赛ID列表
			Messager<JSONObject> result = sendRequest(HttpMethod.POST, buildURI("/yewu11/v2/w/structureTournamentMatches"), params, FLAG);
			if (!result.isOK()) {
				params.clear();
				continue;
			}
			final JSONArray nolivedata = result.data().getJSONObject("data").getJSONArray("nolivedata");
			int size;
			if (nolivedata != null && (size = nolivedata.size()) > 0) {
				for (int i = 0; i < size; i++) { // 最多查40条数据，多余将会被截断
					JSONObject game = nolivedata.getJSONObject(i);
					long mgt = game.getLongValue("mgt"); // 赛事开始时间
					if (mgt > startTimeThreshold) {
						sb.append(game.getString("mids")).append(",");
						if (i > 0 && i % 40 == 0) {
							handlerBatch(entry, params, sb, gameDTOs, provider, now);
							sb.setLength(0);
						}
					}
				}
				if (!sb.isEmpty()) {
					handlerBatch(entry, params, sb, gameDTOs, provider, now);
				}
			}
			params.clear();
		}
		return gameDTOs;
	}

	private void handlerBatch(Map.Entry<String, String> entry, Map<String, Object> params, StringBuilder sb, Set<GameDTO> gameDTOs, BetProvider provider, Date now) {
		int size;
		Messager<JSONObject> result;
		// 根据ID查询赛事信息列表
		params.clear();
		params.put("mids", sb.substring(0, sb.length() - 1));
		params.put("cuid", userId);
		params.put("cos", 0);
		params.put("euid", entry.getKey());
		result = sendRequest(HttpMethod.POST, buildURI("/yewu11/v1/w/structureMatchBaseInfoByMids"), params, FLAG);
		if (!result.isOK()) {
			params.clear();
			final String msg = result.data().getString("msg");
			if (msg != null && msg.startsWith("当前访问人数过多")) {
				try {
					Thread.sleep(2100);
				} catch (InterruptedException ignore) {
				}
				handlerBatch(entry, params, sb, gameDTOs, provider, now);
			}
			return;
		}
		JSONArray games = result.data().getJSONObject("data").getJSONArray("data");
		if (games != null && (size = games.size()) > 0) {
			for (int i = 0; i < size; i++) {
				// 解析赛事赔率信息（开云赔率包含本金无需二次转换）
				JSONObject game = (JSONObject) games.get(i);

				final JSONObject hpsData = (JSONObject) game.getJSONArray("hpsData").get(0);

				// 主队：mhn = T1 = 第一支队伍，man = T2 = 第二支队伍，st（STRONG） = T1/T2 = 主队（不存在时默认为 T2）
				List<OddsInfo> odds = new ArrayList<>();
				// 解析主盘信息
				for (Object hp : hpsData.getJSONArray("hps")) {
					final JSONObject oddsData = (JSONObject) hp;
					OddsType oddsType = parseOddsType(oddsData.getString("hpid"));
					JSONObject hl = oddsData.getJSONObject("hl");
					parseOdds(hl, oddsType, odds);
				}
				// 解析拓展盘口信息
				for (Object hpsAdd : hpsData.getJSONArray("hpsAdd")) {
					final JSONObject oddsData = (JSONObject) hpsAdd;
					OddsType oddsType = parseOddsType(oddsData.getString("hpid"));
					JSONArray hls = oddsData.getJSONArray("hl"); // hpsAdd中是 Array
					if (hls != null && !hls.isEmpty()) {
						int offset = switch (oddsType) {
							case R, HR, OU, HOU -> 2; // 2个为一组，可能存在多组
							case M, HM -> 3;
							default -> throw new IllegalArgumentException("未知的盘口类型：" + oddsData.getString("hpid"));
						};
						for (Object hlObj : hls) {
							JSONObject hl = ((JSONObject) hlObj);
							// 拓展数据中的ol会包含多组赔率数据
							for (int j = 0; j < hl.getJSONArray("ol").size(); j += offset) {
								parseOdds(hl, oddsType, odds);
							}
						}
					}
				}
				if (!odds.isEmpty()) {
					String league = game.getString("tn").replaceAll(" ", "");
					if (!"梦幻对垒".equals(game.getString("tn"))) {  // 排除虚拟球赛
						if (league.endsWith("级联赛") && !league.contains("超级联赛")) {
							league = league.replace("级联赛", "组联赛");
						} else if ("玻利维亚杯".equals(league)) {
							league = "玻利维亚职业联赛杯";
						}
						gameDTOs.add(new GameDTO(game.getLong("mid"), provider, new Date(game.getLong("mgt")), league,
								game.getString("mhn"), game.getString("man"), odds, now));
					}
				}
			}
		}
	}

	private void parseOdds(JSONObject hl, OddsType oddsType, List<OddsInfo> odds) {
		JSONArray ol = hl.getJSONArray("ol");
		if (oddsType != null && ol != null && !ol.isEmpty()) {
			switch (oddsType) {
				case R, HR -> odds.add(new OddsInfo(oddsType, ((JSONObject) ol.get(0)).getString("onb"),
						parseOdds(ol, 0), parseOdds(ol, 1)));
				case OU, HOU -> odds.add(new OddsInfo(oddsType, hl.getString("hv"), parseOdds(ol, 0), parseOdds(ol, 1)));
				case M, HM -> odds.add(new OddsInfo(oddsType, parseOdds(ol, 0), parseOdds(ol, 1), parseOdds(ol, 2)));
			}
		}
	}

	public double parseOdds(JSONArray ol, int idx) {
		final long odds = ((JSONObject) ol.get(idx)).getLongValue("ov");
		return odds / 100_000D; // 赔率（包含本金）
	}

	/** 账号ID并未跟token强制关联验证 */
	final String userId = "529740290458888888";

	@Override
	protected HttpRequest.Builder createRequest(HttpMethod method, URI uri, Map<String, Object> params, int flags) {
		HttpRequest.Builder builder = super.createRequest(method, uri, params, flags);
		builder.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
		builder.setHeader("Origin", "https://user-pc-new.zlshelves.com");
		builder.setHeader("Referer", "https://user-pc-new.zlshelves.com/");
		builder.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36");
		final String token = getConfig().token;
		builder.setHeader("checkId", "pc-" + token + "-" + userId + "-" + System.currentTimeMillis());
		builder.setHeader("lang", "zh");
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

	/**
	 * 对 PB 格式数据进行解密
	 * 流程：Base64解码 -> Gzip解压 -> UTF-8转换 -> URL解码
	 */
	public static String decryptPBData(String encodedData) throws Exception {
		if (StringUtil.isEmpty(encodedData)) {
			return null;
		}

		// 自动处理换行符
		final byte[] compressedBytes = Common.decodeBase64(encodedData.replaceAll("\\\\n", ""));

		// Gzip 解压 (对应 pako.inflate)
		// 使用 try-with-resources 自动管理资源
		try (
				var bis = new ByteArrayInputStream(compressedBytes);
				final var reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(bis), StandardCharsets.UTF_8))
		) {

			// 读取解压后的内容
			return URLDecoder.decode(reader.lines().collect(Collectors.joining()), StandardCharsets.UTF_8);
		}
	}

}