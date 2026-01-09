package com.bojiu.webapp.user.bet.impl;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.text.ParseException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.alibaba.fastjson2.*;
import com.bojiu.common.model.Messager;
import com.bojiu.common.redis.RedisUtil;
import com.bojiu.context.web.Jsons;
import com.bojiu.webapp.user.bet.BaseBetApiImpl;
import com.bojiu.webapp.user.dto.GameDTO;
import com.bojiu.webapp.user.dto.GameDTO.OddsInfo;
import com.bojiu.webapp.user.dto.ScoreResult;
import com.bojiu.webapp.user.model.BetProvider;
import com.bojiu.webapp.user.model.OddsType;
import me.codeplayer.util.*;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static com.bojiu.webapp.user.utils.HGOddsConverter.convertOddsRatio;

@Service
public class HgBetImpl extends BaseBetApiImpl {

	@Override
	public BetProvider getProvider() {
		return BetProvider.HG;
	}

	@Override
	protected HttpClient currentClient() {
		HttpClient client = getProxyClient();
		return client == null ? defaultClient() : client;
	}

	public HgBetImpl() {
		super();
	}

	protected URI buildURI() {
		return URI.create(this.getConfig().endPoint);
	}

	transient URI uri;

	protected URI buildURI(String version) {
		if (this.uri == null || version == null || System.currentTimeMillis() - this.lastUpdateTime > flushInterval) {
			this.uri = URI.create(this.getConfig().endPoint + "/transform.php?ver=" + version);
		}
		return uri;
	}

	/** 返回结果为文档，且屏蔽Body输出 */
	final int FLAG = FLAG_RETURN_TEXT | FLAG_LOG_OUT_BRIEF_BODY;

	/** 更新间隔 */
	static final long flushInterval = 1000 * 60 * 60 * 10;
	/** 版本信息（< $version, $iovationKey >） */
	transient Pair<String, String> version;
	transient long lastUpdateTime = 0;

	protected Pair<String, String> getVersion() {
		if (version == null) {
			ValueOperations<String, String> opsForValue = RedisUtil.template().opsForValue();
			final String key = getProvider() + "_version", versionJson = opsForValue.get(key);
			if (versionJson != null) {
				version = Jsons.parseObject(versionJson, new TypeReference<>() {
				});
			} else {
				final String html = doVersion().getCallback();
				// 该接口返回的是 HTML
				if (StringUtil.notEmpty(html)) {
					version = doGetVersion(html);
					setVersion(version, key);
					return version;
				}
			}
		}
		return version;
	}

	protected int getTimeoutDay() {
		return 1;
	}

	protected void setVersion(Pair<String, String> version, String key) {
		RedisUtil.template().opsForValue().set(key == null ? (getProvider() + "_version") : key, Jsons.encode(version), getTimeoutDay(), TimeUnit.DAYS);
		lastUpdateTime = System.currentTimeMillis();
		LOGGER.info("【{}游戏】获取版本信息：{}", getProvider(), version);
	}

	protected Pair<String, String> doGetVersion(String html) {
		String beginStr = "top.ver = '";
		int begin = html.indexOf(beginStr) + beginStr.length();
		final String ver = html.substring(begin, html.indexOf("';", begin));
		beginStr = "top.iovationKey = '";
		begin = html.indexOf(beginStr) + beginStr.length();
		final String iovationKey = html.substring(begin, html.indexOf("';", begin));
		return Pair.of(ver, iovationKey);
	}

	private @NonNull Messager<JSONObject> doVersion() {
		final Map<String, Object> params = new TreeMap<>();
		params.put("detection", "Y");
		params.put("sub_doubleLogin", "");
		params.put("isapp", "");
		params.put("q", "");
		params.put("appversion", "");
		return sendRequest(HttpMethod.POST, buildURI(), params, FLAG);
	}

	transient JSONObject loginInfo;

	@Override
	public String getLanguage(String lang) {
		return lang;
	}

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
					loginInfo = result.data();
					opsForValue.set(key, Jsons.encode(loginInfo), getTimeoutDay(), TimeUnit.DAYS);
				}
			}
		}
		return loginInfo;
	}

	protected @NonNull Messager<JSONObject> doGetLogin(String langx) {
		final Map<String, Object> params = new TreeMap<>();
		Pair<String, String> pair = getVersion();
		final String ver = pair.getKey();
		params.put("ver", ver);
		params.put("p", "chk_login");
		params.put("langx", langx);
		params.put("username", this.getConfig().username);
		params.put("password", this.getConfig().password);
		params.put("app", "N");
		params.put("auto", pair.getValue());
		params.put("blackbox", "");
		// Base64加密 Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36
		params.put("userAgent", "TW96aWxsYS81LjAgKFdpbmRvd3MgTlQgMTAuMDsgV2luNjQ7IHg2NCkgQXBwbGVXZWJLaXQvNTM3LjM2IChLSFRNTCwgbGlrZSBHZWNrbykgQ2hyb21lLzE0Mi4wLjAuMCBTYWZhcmkvNTM3LjM2");
		return sendRequest(HttpMethod.POST, buildURI(ver), params, FLAG);
	}

	private void clearLoginToken() {
		loginInfo = null;
		RedisUtil.template().delete(getProvider() + "_login");
	}

	final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

	@Override
	protected String postHandleResult(Messager<JSONObject> result, String responseBody, HttpResponse<String> response) {
		super.postHandleResult(result, responseBody, response);
		final JSONObject json;
		if (!responseBody.startsWith("<?xml")) {
			json = JSONObject.of("msg", responseBody);
		} else {
			final Document doc;
			try {
				doc = factory.newDocumentBuilder().parse(new InputSource(new StringReader(responseBody)));
			} catch (SAXException | IOException | ParserConfigurationException e) {
				throw new IllegalArgumentException(e);
			}
			final Element root = doc.getDocumentElement();
			json = (JSONObject) xmlNodeToJson(root);
		}
		result.setData(json);
		result.setStatus(mapStatus(json, response));
		return responseBody;
	}

	/**
	 * 递归方法将 XML 元素转换为 JSONObject 或 JSONArray
	 *
	 * @return {@link JSONObject} 或 null 或 字符串
	 */
	static Object xmlNodeToJson(Element element) {
		final NodeList children = element.getChildNodes();
		final int length = children.getLength();

		if (length == 1 && children.item(0).getNodeType() == Node.TEXT_NODE) {
			// 如果元素只有一个文本节点，直接返回文本内容
			return element.getTextContent();
		}

		final JSONObject jsonObject = new JSONObject();
		for (int i = 0; i < length; i++) {
			Node node = children.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element child = (Element) node;
				final String tagName = child.getTagName();

				final Object childNode = xmlNodeToJson(child);
				final Object old = jsonObject.put(tagName, childNode);
				if (old != null) { // 之前已存在该 tagName，将已有的元素添加到 JSONArray 中
					final JSONArray list;
					if (old instanceof JSONArray array) {
						list = array;
					} else {
						list = new JSONArray(length);
						list.add(old);
					}
					list.add(childNode);
					jsonObject.put(tagName, list);
				}
			}
		}

		return jsonObject.isEmpty() ? null : jsonObject;
	}

	protected boolean extErrVerify(JSONObject json, HttpResponse<String> response) {
		return true;
	}

	@Override
	protected String mapStatus(JSONObject json, HttpResponse<String> response) {
		final String errorCode = (String) getErrorCode(json);
		if ((errorCode != null && !"100".equals(errorCode))
				|| !extErrVerify(json, response)
		) {
			if ("doubleLogin".equals(errorCode)) { // 掉登录状态，清除登录信息
				clearLoginToken();
			}
			return null;
		}
		return Messager.OK;
	}

	@Override
	protected Object getErrorCode(JSONObject json) {
		return json.getString("msg");
	}

	@Override
	public Messager<Void> ping() {
		Messager<JSONObject> result = doVersion();
		if (!result.isOK()) { // 标记为维护状态
			result.setStatus(STATUS_MAINTAIN);
		}
		return result.castDataType(null);
	}

	/** 需要查询的非主场赔率类型（OU|MIX=让球&大小；CN=角球；RN=罚牌数；PD=波胆；SFS=进球球员） */
	static final List<String> betObtType = List.of("OU|MIX");

	@Override
	public Set<GameDTO> getGameBets(String lang) {
		return getGameDTOS(lang);
	}

	private @NonNull Set<GameDTO> getGameDTOS(String langx) {
		JSONObject login = doLogin();
		final String uid = login.getString("uid");
		// 查询赛事统计数据
		Date now = new Date();
		Set<GameDTO> gameDTOs = new HashSet<>();
		Messager<JSONObject> result = doGetLeagueCount(uid, langx);
		if (!result.isOK()) {
			final String callback = result.getCallback();
			if (callback != null && callback.startsWith("无可执行的采集UID")) {
				LOGGER.warn("获取赔率数据失败，厂商【{}】维护，返回数据：{}", getProvider(), callback);
				return gameDTOs;
			}
			clearLoginToken();
			LOGGER.warn("获取赛事统计数据失败：{}", Jsons.encodeRaw(result));
			return gameDTOs;
		}
		JSONObject data = result.data();
		JSONArray games = data.getJSONArray("game");
		int[] stat = { 0, 0 };
		if (!games.isEmpty()) {
				/*
				查询足球统计数据：
					RB_count    滚球
					FT_count    今日
					FS_FU_count + P3_FU_count   早盘
				 */
			JSONObject ft = (JSONObject) CollectionUtil.findFirst(games, g -> "FT".equals(((JSONObject) g).getString("gtype")));
			if (ft != null) {
				stat[0] = ft.getIntValue("FT_count", 0);
				stat[1] = ft.getIntValue("FS_FU_count", 0) + ft.getIntValue("P3_FU_count", 0);
			}
		}

		final long startTimeThreshold = getStartTimeThreshold(now.getTime());

		// 若存在今日则读取今日赛事赔率，若存在早盘赛事才读取早盘赛事赔率
		for (int i = 0; i < stat.length; i++) {
			int count = stat[i];
			if (count > 0) { // 存在赛事
				List<GameDTO> dtos = switch (i) {
					// 今日赛事
					case 0 -> parseGames(doGetGameList(uid, true, langx), startTimeThreshold, true, now);
					// 早盘赛事
					case 1 -> parseGames(doGetGameList(uid, false, langx), startTimeThreshold, false, now);
					default -> throw new IllegalArgumentException("未知状态：" + i);
				};
				if (dtos != null) {
					gameDTOs.addAll(dtos);
				}
			}
		}
		// 拉取非主场赔率数据 TODO 暂时不拉取更多赔率数据
		// if (!gameDTOs.isEmpty() && !betObtType.isEmpty()) {
		// 	int size = gameDTOs.size();
		// 	for (String model : betObtType) {
		// 		for (int i = 0; i < size; i++) {
		// 			GameDTO dto = gameDTOs.get(i);
		// 			List<GameDTO> dtos = parseGamesOBT(doGetGameOBT(uid, dto, model), (Boolean) dto.ext, now);
		// 			if (dtos != null) {
		// 				gameDTOs.addAll(dtos);
		// 			}
		// 		}
		// 	}
		// }
		return gameDTOs;
	}

	private List<GameDTO> parseGamesOBT(Messager<JSONObject> result, boolean isToday, Date openTime, Date now) {
		if (result.isOK()) {
			JSONObject data = result.data();
			JSONArray ecs = data.getJSONArray("ec");
			if (ecs != null) {
				int size = ecs.size();
				List<GameDTO> gameDTOs = new ArrayList<>(size);
				for (int j = 0; j < size; j++) {
					JSONObject dto = ecs.getJSONObject(j);
					JSONArray games = dto.getJSONArray("game");
					if (games != null) { // 存在多个game标签时将被解析为 JSONArray
						for (int k = 0, len = games.size(); k < len; k++) {
							JSONObject game = games.getJSONObject(k);
							parseGameSingle(isToday, game, gameDTOs, openTime, now);
						}
					} else { // 只存在一个game标签是被解析为 JSONObject
						JSONObject game = dto.getJSONObject("game");
						if (game != null) {
							parseGameSingle(isToday, game, gameDTOs, openTime, now);
						}
					}
				}
				return gameDTOs;
			}
		}
		return null;
	}

	private void parseGameSingle(boolean isToday, JSONObject game, List<GameDTO> gameDTOs, Date openTime, Date now) {
		if ("Y".equals(game.getString("ISMASTER"))) {
			return;
		}
		gameDTOs.add(parseGameDTO(isToday, game, openTime, now));
	}

	private List<GameDTO> parseGames(Messager<JSONObject> result, long startTimeThreshold, boolean isToday, Date now) {
		if (result.isOK()) {
			JSONObject data = result.data();
			JSONArray ecs = data.getJSONArray("ec");
			int size = ecs.size();
			List<GameDTO> gameDTOs = new ArrayList<>(size);
			for (int j = 0; j < size; j++) {
				JSONObject game = ecs.getJSONObject(j).getJSONObject("game");
				final Date openTime = parseTime(game.getString("DATETIME"));
				if (openTime.getTime() >= startTimeThreshold &&
						!"奇幻赛事".equals(game.getString("LEAGUE")) // 排除虚拟球赛
				) {
					gameDTOs.add(parseGameDTO(isToday, game, openTime, now));
				}
			}
			return gameDTOs;
		}
		return null;
	}

	private @NonNull GameDTO parseGameDTO(boolean isToday, JSONObject game, Date openTime, Date now) {
		List<OddsInfo> odds = new ArrayList<>(OddsType.CACHE.length);
		// 以第一只队伍为准（H表示强队与第一支队伍相同，此时 第一支队伍为 让球方 ）
		boolean homeIsStrong = "H".equals(game.getString("STRONG")), homeIsHStrong = "H".equals(game.getString("HSTRONG"));
		final String strongPrefix = homeIsStrong ? "-" : "+", // 全场
				strongHPrefix = "H".equals(game.getString("HSTRONG")) ? "-" : "+"; // 上半场
		for (OddsType oddsType : OddsType.CACHE) {
			switch (oddsType) {
				case R -> {
					Double iorRh = game.getDouble("IOR_RH");
					if (iorRh != null) {
						double[] values = convertOddsRatio(iorRh, game.getDouble("IOR_RC"), 2, null);
						odds.add(new OddsInfo(oddsType, parseRatioRate(strongPrefix, game.getString("RATIO_R")), values[0] + 1, values[1] + 1));
					}
				}
				case OU -> {
					Double iorOuh = game.getDouble("IOR_OUH");
					if (iorOuh != null) {
						double[] values = convertOddsRatio(iorOuh, game.getDouble("IOR_OUC"), 2, null);
						odds.add(new OddsInfo(oddsType, parseRatioRate(null, game.getString("RATIO_OUO")), values[1] + 1,/*大小的结果取反*/ values[0] + 1));
					}
				}
				case M -> {
					Double iorMh = game.getDouble("IOR_MH");
					if (iorMh != null) {
						odds.add(new OddsInfo(oddsType, iorMh, game.getDouble("IOR_MC"), game.getDouble("IOR_MN")));
					}
				}
				case HR -> {
					Double iorHrh = game.getDouble("IOR_HRH");
					if (iorHrh != null) {
						double[] values = convertOddsRatio(iorHrh, game.getDouble("IOR_HRC"), 2, null);
						odds.add(new OddsInfo(oddsType, parseRatioRate(strongHPrefix, game.getString("RATIO_HR")), values[0] + 1, values[1] + 1));
					}
				}
				case HOU -> {
					Double iorHouh = game.getDouble("IOR_HOUH");
					if (iorHouh != null) {
						double[] values = convertOddsRatio(iorHouh, game.getDouble("IOR_HOUC"), 2, null);
						odds.add(new OddsInfo(oddsType, parseRatioRate(null, game.getString("RATIO_HOUO")), values[1] + 1,/*大小的结果取反*/ values[0] + 1));
					}
				}
				case HM -> {
					Double iorHmh = game.getDouble("IOR_HMH");
					if (iorHmh != null) {
						odds.add(new OddsInfo(oddsType, iorHmh, game.getDouble("IOR_HMC"), game.getDouble("IOR_HMN")));
					}
				}
				case TS -> {
					Double iorTsy = game.getDouble("IOR_TSY");
					if (iorTsy != null) { // 已经算入本金无需转换
						odds.add(new OddsInfo(oddsType, iorTsy, game.getDouble("IOR_TSN")));
					}
				}
				case EO -> {
					Double iorEoo = game.getDouble("IOR_EOO");
					if (iorEoo != null) { // 已经算入本金无需转换
						odds.add(new OddsInfo(oddsType, iorEoo, game.getDouble("IOR_EOE")));
					}
				}
			}
		}
		final GameDTO dto = new GameDTO(game.getLong("ECID"), getProvider(), openTime, convertLeague(game.getString("LEAGUE")),
				game.getString("TEAM_H"), game.getString("TEAM_C"), odds, now);
		dto.setExt(isToday);
		return dto;
	}

	/**
	 * 解析赔率盘口值/比率
	 *
	 * @see com.bojiu.webapp.user.dto.GameDTO.OddsInfo#ratioRate
	 */
	protected String parseRatioRate(String strongPrefix, String ratioRate) {
		return ("0".equals(ratioRate) ? "0" : (strongPrefix == null ? "" : strongPrefix) + ratioRate.replace(" ", ""));
	}

	final ZoneId zoneId = ZoneId.of("GMT-4");
	final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("YYYY-MM-dd hh:mma", TimeZone.getTimeZone(zoneId), Locale.ENGLISH);

	private Date parseTime(String time) {
		try {
			// 获取当前年份 (在 GMT-4 时区下的当前年份)
			int currentYear = ZonedDateTime.now(zoneId).getYear();
			return DATE_FORMAT.parse(currentYear + "-" + time);
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/** 赛事统计数据 */
	protected Messager<JSONObject> doGetLeagueCount(String uid, String langx) {
		/*
		联赛统计数据：
		get_league_count
			FT  足球
			BK  篮球
			TN  网球
			ES  电子竞技
			VB  排球
			SK  台球/斯诺克
			OP  其他
		 */
		final Map<String, Object> params = new TreeMap<>();
		Pair<String, String> pair = getVersion();
		final String ver = pair.getKey();
		params.put("p", "get_league_count");
		params.put("uid", uid);
		params.put("ver", ver);
		params.put("langx", langx);
		params.put("sorttype", "league");
		params.put("date", "ALL");
		params.put("ltype", getProvider() == BetProvider.HG ? "3" : "4");
		params.put("mode", "home");
		params.put("ts", System.currentTimeMillis());
		return sendRequest(HttpMethod.POST, buildURI(ver), params, FLAG);
	}

	/** 查询游戏场次列表 */
	protected Messager<JSONObject> doGetGameList(String uid, boolean isToday, String langx) {
		final Map<String, Object> params = new TreeMap<>();
		Pair<String, String> pair = getVersion();
		final String ver = pair.getKey();
		params.put("uid", uid);
		params.put("ver", ver);
		params.put("langx", langx);
		params.put("p", "get_game_list");
		params.put("p3type", "");
		params.put("gtype", "ft"); // 足球
		if (isToday) {
			params.put("date", "");
			params.put("filter", "FT");
			params.put("showtype", "today");
		} else {
			params.put("date", "all");
			params.put("showtype", "early");
			params.put("filter", "FU");
			// params.put("lid", leagueId);
			params.put("action", "click_league");
		}
		params.put("rtype", "r");
		params.put("ltype", "3");
		params.put("cupFantasy", "N");
		params.put("sorttype", "L");
		params.put("specialClick", "");
		params.put("isFantasy", "N");
		params.put("ts", System.currentTimeMillis());
		return sendRequest(HttpMethod.POST, buildURI(ver), params, FLAG);
	}

	protected Messager<JSONObject> doGetGameOBT(String uid, GameDTO dto, String model, String langx) {
		final Map<String, Object> params = new TreeMap<>();
		Pair<String, String> pair = getVersion();
		final String ver = pair.getKey();
		params.put("uid", uid);
		params.put("ver", ver);
		params.put("langx", langx);
		params.put("p", "get_game_OBT");
		params.put("gtype", "ft"); // 足球
		params.put("isSpecial", "");
		params.put("isEarly", "N");
		params.put("model", model); // OU|MIX=让球&大小；CN=角球；RN=罚牌数；PD=波胆；SFS=进球球员
		params.put("ecid", dto.getId());
		final boolean isToday = (boolean) dto.ext;
		params.put("showtype", (isToday ? "today" : "early"));
		params.put("isETWI", "N");
		params.put("ltype", "4");
		params.put("is_rb", "N");
		params.put("ts", System.currentTimeMillis());
		params.put("isClick", "Y");
		try {
			return sendRequest(HttpMethod.POST, buildURI(ver), params, FLAG);
		} catch (Exception e) {
			return Messager.error(e.getMessage());
		}
	}

	@Override
	public String convertLeague(String league) {
		return league.equals("埃及乙组联赛B") ? "埃及乙组联赛" : league;
	}

	public String getSourceResult() {
		JSONObject login = doLogin();
		return login.getString("uid");
	}

	@Override
	public Map<Long, ScoreResult> getScoreResult() {
		final String uid = getSourceResult();
		EasyDate d = new EasyDate();
		final Map<Long, ScoreResult> results = new HashMap<>();
		for (int i = 0; i < 2; i++) { // 只查询近两天的数据
			d.addDay(-i);
			final Messager<JSONObject> result = sendRequest(HttpMethod.GET, URI.create(this.getConfig().scoreResultUrl + "/app/member/account/result/result.php?game_type=FT"
					+ "&list_date=" + d + "&uid=" + uid + "&langx=zh-cn"), null, FLAG);
			final String html = result.getCallback();
			var root = Jsoup.parse(html);
			Elements homeTrs = root.getElementsByClass("acc_result_tr_top");
			if (!homeTrs.isEmpty()) {
				Elements clientTrs = root.getElementsByClass("acc_result_tr_other");
				final HashMap<String, org.jsoup.nodes.Element> homeTrMap = CollectionUtil.toHashMap(homeTrs, h -> h.attr("id")),
						clientTrMap = CollectionUtil.toHashMap(clientTrs, h -> h.attr("id"));
				int size = homeTrs.size();
				if (size != clientTrs.size()) {
					results.putAll(parseScore(homeTrMap, clientTrMap, size));
					LOGGER.warn("【{}】游戏 查询赛果，进行差值映射", getProvider());
					continue;
				}
				results.putAll(parseScore(homeTrs, clientTrs));
			}

		}
		return results;
	}

	private Map<Long, ScoreResult> parseScore(HashMap<String, org.jsoup.nodes.Element> homeTrMap, HashMap<String, org.jsoup.nodes.Element> clientTrMap, int size) {
		final Map<Long, ScoreResult> results = new HashMap<>(size, 1F);
		for (Map.Entry<String, org.jsoup.nodes.Element> entry : homeTrMap.entrySet()) {
			var homeTr = entry.getValue();
			final String id = entry.getKey(); // 格式：TR_100710_8407017
			org.jsoup.nodes.Element clientEmt = clientTrMap.get(id);
			if (clientEmt == null) {
				continue;
			}
			builderScoreResult(clientEmt, id, homeTr, results);
		}
		return results;
	}

	private Map<Long, ScoreResult> parseScore(Elements homeTrs, Elements clientTrs) {
		final int size = clientTrs.size();
		final Map<Long, ScoreResult> results = new HashMap<>(size, 1F);
		for (int j = 0; j < size; j++) {
			var homeTr = homeTrs.get(j);
			final String id = homeTr.attr("id"); // 格式：TR_100710_8407017
			builderScoreResult(clientTrs.get(j), id, homeTr, results);
		}
		return results;
	}

	/** 赛果最后一场比赛的结束时间 */
	transient Long lastTime;

	private void builderScoreResult(org.jsoup.nodes.Element clientEmt, String id, org.jsoup.nodes.Element homeTr, Map<Long, ScoreResult> results) {
		var client = clientEmt.getElementsByTag("td");
		var homeTds = homeTr.getElementsByTag("td");
		final long dateTime = parseTime(homeTds.get(0).text()).getTime();
		if (lastTime == null || dateTime >= lastTime) { // 只处理后续赛事结果
			final ScoreResult scoreResult = new ScoreResult();
			// 主队名：<td class="acc_result_team">墨尔本胜利 &nbsp;&nbsp;</td>
			scoreResult.setTeamHome(homeTds.get(1).text());
			scoreResult.setTeamClient(client.get(0).text()); // 客队名

			// 全场进球数：<td class="acc_result_full"><span class="acc_cont_bold">5</span></td>
			final String text = homeTds.get(2).getElementsByTag("span").get(0).text(),
					text2 = client.get(2).getElementsByTag("span").get(0).text(),
					text3 = homeTds.get(3).getElementsByTag("span").get(0).text();
			if (!NumberUtil.isNumber(text) || !NumberUtil.isNumber(text2) || !NumberUtil.isNumber(text3)) {
				LOGGER.warn("解析进球数异常：{}", text);
				return;
			}
			scoreResult.setScore(new Integer[] { Integer.parseInt(text),
					Integer.parseInt(client.get(1).getElementsByTag("span").get(0).text()) });
			// 上半场进球数：<td class="acc_result_bg"><span class="acc_cont_bold">2</span></td>
			scoreResult.setScoreH(new Integer[] { Integer.parseInt(text3), Integer.parseInt(text2) });
			// 格式：TR_100710_8407017
			results.put(Long.parseLong(id.substring(id.lastIndexOf("_") + 1)), scoreResult);
			lastTime = dateTime;
		}
	}

}