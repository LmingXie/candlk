package com.bojiu.webapp.user.bet.impl;

import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bojiu.common.model.ErrorMessageException;
import com.bojiu.common.model.Messager;
import com.bojiu.common.util.Common;
import com.bojiu.context.web.Jsons;
import com.bojiu.webapp.user.bet.LoginBaseBetApiImpl;
import com.bojiu.webapp.user.dto.*;
import com.bojiu.webapp.user.dto.GameDTO.OddsInfo;
import com.bojiu.webapp.user.model.*;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.*;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KyBetImpl extends LoginBaseBetApiImpl {

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

	@Override
	public String getLanguage(String lang) {
		return switch (lang) {
			case LANG_EN -> "en";
			case LANG_ZH -> "zh";
			default -> throw new IllegalArgumentException("未知的语言：" + lang);
		};
	}

	/*
		可用域名：
			api.q7stajv.com
			api.togav85.com
		 */
	@Override
	public Set<GameDTO> getGameBets(String lang) {
		getLoginToken();
		final Map<String, Object> params = new TreeMap<>();
		final StringBuilder sb = new StringBuilder();
		Set<GameDTO> gameDTOs = new HashSet<>();
		BetProvider provider = getProvider();
		Date now = new Date();
		// 比赛的最小开赛时间
		final long startTimeThreshold = getStartTimeThreshold(now.getTime());
		for (Map.Entry<String, String> entry : euidToTidMap.entrySet()) {
			params.put("lang", lang);
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
							handlerBatch(entry, params, sb, gameDTOs, provider, now, lang);
							sb.setLength(0);
						}
					}
				}
				if (!sb.isEmpty()) {
					handlerBatch(entry, params, sb, gameDTOs, provider, now, lang);
				}
			}
			params.clear();
		}
		// log.info("获取全部赛事名称完成，大小{}：{}", leagueSet.size(), Jsons.encode(leagueSet));
		return gameDTOs;
	}

	private void handlerBatch(Map.Entry<String, String> entry, Map<String, Object> params, StringBuilder sb, Set<GameDTO> gameDTOs, BetProvider provider, Date now
			, String lang) {
		int size;
		Messager<JSONObject> result;
		// 根据ID查询赛事信息列表
		params.clear();
		params.put("lang", lang);
		params.put("mids", sb.substring(0, sb.length() - 1));
		params.put("cuid", userId);
		params.put("cos", 0);
		params.put("euid", entry.getKey());
		result = sendRequest(HttpMethod.POST, buildURI("/yewu11/v1/w/structureMatchBaseInfoByMids"), params, FLAG);
		if (!result.isOK()) {
			params.clear();
			final String msg = result.data().getString("msg");
			// The current number of visitors is too high. Please try again later
			if (msg != null && (msg.startsWith("The current number of visitors") || msg.startsWith("当前访问人数过多"))) {
				try {
					Thread.sleep(543);
				} catch (InterruptedException ignore) {
				}
				handlerBatch(entry, params, sb, gameDTOs, provider, now, lang);
			}
			return;
		}
		final boolean isZh = "zh".equals(lang);
		final JSONArray games = result.data().getJSONObject("data").getJSONArray("data");
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
					final OddsType oddsType = parseOddsType(oddsData.getString("hpid"));
					final JSONArray hls = oddsData.getJSONArray("hl"); // hpsAdd中是 Array
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
					final String league = isZh ? game.getString("tn").replaceAll(" ", "") : game.getString("tn");
					if (!"Fantasy Matches".equals(league) && !"梦幻对垒".equals(league)) {  // 排除虚拟球赛
						// leagueSet.add(league);
						gameDTOs.add(new GameDTO(game.getLong("mid"), provider, new Date(game.getLong("mgt")), convertLeague(league),
								game.getString("mhn"), game.getString("man"), odds, now));
					}
				}
			}
		}
	}

	// Set<String> leagueSet = new HashSet<>();

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
	protected HttpClient currentClient() {
		HttpClient client = getProxyClient();
		return client == null ? defaultClient() : client;
	}

	@Override
	protected HttpRequest.Builder createRequest(HttpMethod method, URI uri, Map<String, Object> params, int flags) {
		HttpRequest.Builder builder = super.createRequest(method, uri, params, flags);
		if (uri != v4URI) {
			builder.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36");
			final Object apiSite = params == null ? null : params.remove("x-api-site");
			if (apiSite == null) {
				builder.setHeader("lang", params == null ? getDefaultLanguage() : (String) params.remove("lang"));
				builder.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
				builder.setHeader("Origin", "https://user-pc-new.zlshelves.com");
				builder.setHeader("Referer", "https://user-pc-new.zlshelves.com/");
				final String token = loginInfo != null ? loginInfo.getString("token") : getConfig().token;
				builder.setHeader("checkId", "pc-" + token + "-" + userId + "-" + System.currentTimeMillis());
				builder.setHeader("request-code", "{\"panda-bss-source\":\"2\"}");
				builder.setHeader("requestId", token);
				builder.setHeader("sec-ch-ua", "\"Google Chrome\";v=\"143\", \"Chromium\";v=\"143\", \"Not A(Brand\";v=\"24\"");
				builder.setHeader("sec-ch-ua-mobile", "?0");
				builder.setHeader("sec-ch-ua-platform", "\"Windows\"");
			} else {
				builder.setHeader("x-api-client", "web");
				builder.setHeader("x-api-site", (String) apiSite);
				builder.setHeader("x-api-uuid", UUID.randomUUID().toString());
				builder.setHeader("x-api-version", "2.0.0");
				if (loginInfo != null) {
					builder.setHeader("x-api-token", loginInfo.getString("apiToken"));
				}
				Object apiToken = params.remove("apiToken");
				if (apiToken != null) {
					builder.setHeader("x-api-token", (String) apiToken);
				}
				if (uri.getPath().endsWith("/launch")) {
					/*
					测试发现 x-api-xxx 前面为时间戳，后面为根据请求URL生成的签名，由于其使用了 WASM 进行计算，暂不进行逆向
					源码生成 X-API-XXX：
					 1、null === (n = t.match(/\/\w+\/\w+/i)) || void 0 === n ? void 0
					 2、return (r = t).includes("/component") && (r = "/site
					 */
					builder.setHeader("x-api-xxx", (System.currentTimeMillis() / 1000) + "561704683abf661728c1ce4d4c374be7ac97acb8b321897cec3c31");
				}
			}
		}
		return builder;
	}

	protected URI buildURI(String url) {
		return URI.create(this.getConfig().endPoint + url + "?t=" + System.currentTimeMillis());
	}

	protected URI buildWebsiteURI(String basePath, String url) {
		return URI.create(basePath + url);
	}

	final URI v4URI = URI.create("http://127.0.0.1:5008/word/verify");
	final Map<String, Object> v4Params = new TreeMap<>() {{
		put("captcha_id", "283ed0bd78efd3d7899888027e9a851f");
	}};

	@Override
	protected JSONObject doLogin(String lang) {
		final BetApiConfig config = this.getConfig();
		return doLogin(config.ext.getString("siteId"), config.ext.getString("website"), config.username, config.password);
	}

	@Override
	public int getTokenTimeout() {
		return 1;
	}

	public JSONObject doLogin(String siteId, String basePath, String username, String password) {
		// 获取极验V4验证码信息
		final Messager<JSONObject> v4Msg = super.sendRequest(HttpMethod.POST, v4URI, v4Params);
		if (!v4Msg.isOK()) {
			throw new ErrorMessageException("获取极验V4验证码失败：" + v4Msg.data().getString("msg"));
		}
		JSONObject data = v4Msg.data().getJSONObject("data");
		final JSONObject seccode = data.getJSONObject("seccode");
		if (seccode == null) {
			throw new ErrorMessageException("获取极验V4验证码失败：lotNumber为空");
		}

		// 开云校验验证码
		final Map<String, Object> params = new TreeMap<>();
		params.put("x-api-site", siteId); // 站点ID
		params.put("validate_way", 1);
		params.put("lot_number", seccode.getString("lot_number"));
		params.put("captcha_output", seccode.getString("captcha_output"));
		params.put("gen_time", seccode.getString("gen_time"));
		params.put("pass_token", seccode.getString("pass_token"));
		final Messager<JSONObject> verifyMsg = super.sendRequest(HttpMethod.POST, buildWebsiteURI(basePath, "/site/api/v1/user/member/validateGeeCheckV2"), params);
		final String newLotNumber = verifyMsg.data().getJSONObject("data").getJSONObject("captcha_args").getString("lot_number");
		if (StringUtil.isEmpty(newLotNumber)) {
			throw new ErrorMessageException("极验V4验证码校验失败：" + verifyMsg.data().getJSONObject("data").getString("reason"));
		}

		// 获取登录Token
		params.clear();
		params.put("x-api-site", siteId); // 站点ID
		params.put("name", username);
		params.put("password", Encrypter.md5(password));
		params.put("Kaptchcate", 0);
		params.put("codeId", newLotNumber);
		final Messager<JSONObject> loginMsg = super.sendRequest(HttpMethod.POST, buildWebsiteURI(basePath, "/site/api/v1/user/login"), params);
		data = loginMsg.data().getJSONObject("data");
		final String apiToken = data.getString("token");
		data.put("apiToken", apiToken);

		params.clear();
		params.put("x-api-site", siteId); // 站点ID
		params.put("apiToken", apiToken);
		params.put("name", username);
		params.put("enName", "YBTY");
		params.put("isApp", 0);
		params.put("https", 1);
		params.put("isManualLaunch", false);
		params.put("clientType", "web");
		params.put("isActivity", false);
		params.put("isPandaAct", "0");

		final Messager<JSONObject> launchMsg = super.sendRequest(HttpMethod.POST, buildWebsiteURI(basePath, "/game/api/v1/venue/launch"), params);
		final String gameUrl = launchMsg.data().getJSONObject("data").getString("url");
		int beginIndex = gameUrl.indexOf("?token=") + 7;
		String token = gameUrl.substring(beginIndex, gameUrl.indexOf("&", beginIndex));
		data.put("token", token);
		return data;
	}

	@Override
	protected String mapStatus(JSONObject json, HttpResponse<String> response) {
		final URI uri = response.request().uri();
		if (uri == v4URI) {
			if (!"true".equals(json.getString("success"))) {
				log.warn("【{}-极验】获取验证码失败：{}", getProvider(), json.getString("msg"));
				return null;
			}
		} else {
			final String errorCode = (String) getErrorCode(json);
			if (!"0000000".equals(errorCode)/*游戏状态码*/ && !"6000".equals(json.getString("status_code"))/*官网状态码*/) {
				if ("0401013".equals(errorCode)) { // 掉登录
					clearLoginToken();
					// SpringUtil.logError(log, () -> "【" + getProvider() + "】掉登录，请手动补登录，errCode：0401013");
				}
				log.warn("接口响应错误：{}", json.getString("msg"));
				return null;
			}
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

	@Override
	public String convertLeague(String league) {
		league = league.replaceFirst("（", "(").replaceFirst("）", ")");
		// 优先处理完全不规则的、或需要特殊映射的 KY 联赛名称
		return switch (league) {
			case "England Premier League U21" -> League.EnglandPremierLeagueU21;
			case "AFC U23 Asian Cup 2026 (in Saudi Arabia)" -> League.AFCU23AsianCup2026InSaudiArabia;
			case "England Super League Women" -> League.EnglandSuperLeagueWomen;
			case "Africa Cup of Nations 2025 (In Morocco)" -> League.AfricaCupOfNations2025InMorocco2En;
			case "Israel Premier League Women" -> League.IsraelWomenPremierLeague;
			case "Uruguay Copa de la Liga AUF" -> League.UruguayCopadelaLigaAUF;
			case "Spain Primera Division Women" -> League.SpainWomenPrimeraDivision;
			case "France Division 1 Women" -> League.FranceWomenPremiereLigue;
			case "Club Friendly" -> League.ClubFriendlies;
			case "Mexico Liga MX U21" -> League.MexicoLigaMX;
			case "Premier League International Cup U21 (In England)" -> League.PremierLeagueInternationalCupInEngland;
			case "Portugal League Cup Women" -> League.PortugalWomenLeagueCup;
			case "Vietnam Championship U19 Qualifiers", "Vietnam Championship U19 - Playoff" -> League.VietnamU19Championship;
			case "Colombia Superliga" -> League.ColombiaSuperCup;
			case "Winter League (In Czech Republic & Slovakia)" -> League.CzechRepublicTipsportLiga;
			case "Oman Professional League" -> League.OmanProLeague;
			case "Indonesia Liga 3" -> League.IndonesiaLigaNusantara;
			case "Spain Tercera Division" -> League.SpainTerceraFederacion;
			case "Panama Liga Panamena de Futbol - Playoff", "Panama Liga Panamena de Futbol" -> League.PanamaPrimeraDivision;
			case "Belgium Super League Women" -> League.BelgiumWomenSuperLeague;
			case "UAE Emirates Cup U21" -> League.UAECupU21;
			case "England FA Cup Women" -> League.EnglandFAWomenCup;
			case "Ireland Munster Senior League Senior Premier Division" -> League.IrelandLeinsterSeniorLeagueSeniorDivision;
			case "Reykjavik Football Tournament(In Iceland)" -> League.ReykjavikWomenFootballTournamentInIceland;
			case "Cambodian Premier League" -> League.CambodiaPremierLeague;
			case "Turkey League U19" -> League.TurkeyPAFLeagueU19;
			case "Italy Serie A Women" -> League.ItalyWomenSerieA;
			case "Kenya National Super League" -> League.KenyaPremierLeague;
			case "Cyprus Division 2 - Playoff" -> League.CyprusDivision2;
			case "Macau 1st Division" -> League.MacauPrimeiraDivisao;

			// 默认处理：如果无法精准匹配，尝试通用的字符替换逻辑（注意：这依然返回字符串）
			default -> {
				String temp = league;
				if (temp.contains("Qualifier")) {
					temp = temp.replace("Qualifier", "Qualifiers");
				}
				yield temp;
			}
		};
	}

	transient String tournamentId;
	transient long tournamentIdCacheTime = 0;

	private String doQueryTournament(long startTime, long endTime, String lang) {
		getLoginToken();
		if (tournamentId == null || System.currentTimeMillis() > tournamentIdCacheTime) {
			final Map<String, Object> params = new TreeMap<>();
			params.put("lang", lang);
			params.put("sportType", "1");
			params.put("endTime", endTime);
			params.put("startTime", startTime);
			params.put("langType", lang);
			params.put("nameStr", "");
			params.put("isVirtualSport", "");
			params.put("runningBar", "0"); // 是否包含滚球
			params.put("champion", 0);
			params.put("showem", 1);
			params.put("orderByHot", 1);
			final Messager<JSONObject> result = sendRequest(HttpMethod.POST, buildURI("/yewu11/v1/orderScoreResult/queryTournament"), params, FLAG);
			if (result.isOK()) {
				JSONArray array = result.data().getJSONArray("data");
				if (!array.isEmpty()) {
					final StringBuilder sb = new StringBuilder();
					for (Object o : array) {
						sb.append(((JSONObject) o).getString("id")).append(",");
					}
					tournamentId = sb.substring(0, sb.length() - 1);
					tournamentIdCacheTime = System.currentTimeMillis() + (1000 * 60 * 30); // 缓存30分钟
				}
			}
		}
		return tournamentId;
	}

	@Override
	public Map<Object, ScoreResult> getScoreResult() {
		getLoginToken();
		EasyDate d = new EasyDate().beginOf(Calendar.DATE);
		final long endTime = d.getTime(), startTime = d.addDay(-1).getTime();
		final String lang = getDefaultLanguage();
		final String tournamentId = doQueryTournament(startTime, endTime, lang);
		if (tournamentId != null) {
			final Map<String, Object> params = new TreeMap<>();
			params.put("lang", lang);
			params.put("tournamentId", tournamentId);
			params.put("runningBar", "0"); // 是否包含滚球
			params.put("isPlayBack", 0);
			params.put("orderBy", 0);
			params.put("sportType", "1");
			params.put("startTime", startTime);
			params.put("endTime", endTime);
			params.put("langType", lang);
			params.put("page", JSONObject.of("size", 200, "current", 1));
			params.put("isVirtualSport", "");
			params.put("matchNameStr", "");
			params.put("isNew", 1);
			params.put("champion", 0);
			params.put("isESport", "");
			params.put("orderByHot", 0);
			final Messager<JSONObject> result = sendRequest(HttpMethod.POST, buildURI("/yewu11/v1/orderScoreResult/queryTournamentScoreResult"), params, FLAG);
			if (result.isOK()) {
				final JSONArray array = result.data().getJSONObject("data").getJSONArray("records");
				if (array != null && !array.isEmpty()) {
					final Map<Object, ScoreResult> results = new HashMap<>(array.size(), 1F);
					for (Object o : array) {
						final JSONObject game = (JSONObject) o;
						final String scoreResultJson = game.getString("scoreResult");
						if (!scoreResultJson.isEmpty()) {
							try {
								// ["S1|2:4","S2|0:2","S3|2:2", "S15|1:3"] => S1=全场进球；S2=上半场进球；S3=下半场进球
								final List<String> scores = Jsons.parseArray(scoreResultJson, String.class);
								if (scores.isEmpty()) {
									LOGGER.warn("【{}】未读取到进球数据,score={}，详细信息={}", getProvider(), scoreResultJson, Jsons.encode(game));
									continue;
								}
								final ScoreResult scoreResult = new ScoreResult();
								for (int i = 0, size = scores.size(); i < 2; i++) {
									if (i >= size) {
										continue;
									}
									final String score = scores.get(i);
									if (score.startsWith("S1")) {
										// 解析全场进球
										final int pos = score.indexOf("|", 2) + 1, pos2 = score.lastIndexOf(":");
										scoreResult.setScore(new Integer[] { Integer.parseUnsignedInt(score, pos, pos2, 10),
												Integer.parseUnsignedInt(score, pos2 + 1, score.length(), 10) });
									} else if (score.startsWith("S2")) {
										final int pos = score.indexOf("|", 2) + 1, pos2 = score.lastIndexOf(":");
										// 解析上半场进球
										scoreResult.setScoreH(new Integer[] { Integer.parseUnsignedInt(score, pos, pos2, 10),
												Integer.parseUnsignedInt(score, pos2 + 1, score.length(), 10) });
									}
								}
								if (scoreResult.score == null && scoreResult.scoreH == null) {
									LOGGER.warn("【{}】发现无法解析的分数格式,score={}，详细信息={}", getProvider(), scoreResultJson, Jsons.encode(game));
									continue;
								}

								scoreResult.setTeamHome(game.getString("homeName"));
								scoreResult.setTeamClient(game.getString("awayName"));
								results.put(game.getLong("matchId"), scoreResult);
							} catch (Exception e) {
								log.error("【{}】无法解析的分数格式,score={}，详细信息={}", getProvider(), scoreResultJson, Jsons.encode(game), e);
							}
						}
					}
					return results;
				}
			}
		}
		return Collections.emptyMap();
	}

}