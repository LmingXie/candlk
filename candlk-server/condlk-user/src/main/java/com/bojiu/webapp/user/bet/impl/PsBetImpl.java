package com.bojiu.webapp.user.bet.impl;

import java.net.URI;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.*;

import com.alibaba.fastjson2.*;
import com.bojiu.common.model.ErrorMessageException;
import com.bojiu.common.model.Messager;
import com.bojiu.context.web.Jsons;
import com.bojiu.webapp.user.bet.WsBaseBetApiImpl;
import com.bojiu.webapp.user.dto.*;
import com.bojiu.webapp.user.dto.GameDTO.OddsInfo;
import com.bojiu.webapp.user.model.*;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.EasyDate;
import me.codeplayer.util.StringUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PsBetImpl extends WsBaseBetApiImpl {

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

	@Override
	public Set<GameDTO> getGameBets(String lang) {
		final WebSocket webSocket = getWsConnection();
		if (ws != null) {
			try {
				final Date now = new Date();
				final Set<GameDTO> gameDTOS = new HashSet<>();
				final JSONObject todayBets = getGameBets(webSocket, lang, true);
				if (todayBets != null) {
					// 解析滚球赛事
					parseBlock(todayBets, JSONPath.of("$.odds.l[0][2]", JSONArray.class), gameDTOS, now);
					// 解析今日赛事
					parseBlock(todayBets, JSONPath.of("$.odds.n[0][2]", JSONArray.class), gameDTOS, now);
				}

				final JSONObject earlyBets = getGameBets(webSocket, lang, false);
				if (earlyBets != null) {
					// 解析早盘赛事
					parseBlock(earlyBets, JSONPath.of("$.odds.n[0][2]", JSONArray.class), gameDTOS, now);
					// 解析亮点赛事
					parseBlock(earlyBets, JSONPath.of("$.odds.hle[0][2]", JSONArray.class), gameDTOS, now);
				}
				return gameDTOS;
			} finally {
				// 每30秒一次心跳
				ws.sendText(pingMsg, true);
			}
		}
		return Collections.emptySet();
	}

	public String getDefaultLanguage() {
		return getLanguage(LANG_ZH);
	}

	private void parseBlock(JSONObject bets, JSONPath jsonPath, Set<GameDTO> gameDTOS, Date now) {
		for (Object o : (JSONArray) bets.eval(jsonPath)) {
			parseGames((JSONArray) o, gameDTOS, now);
		}
	}

	private void parseGames(JSONArray leagueGroup, Set<GameDTO> gameDTOS, Date now) {
		final JSONArray games = leagueGroup.getJSONArray(2);
		if (!games.isEmpty()) {
			final String leagueZh = leagueGroup.getString(1), leagueEn = leagueGroup.getString(4);
			for (Object gameObj : games) {
				final JSONArray game = (JSONArray) gameObj;
				final JSONObject odds = game.getJSONObject(8);
				// 全场赔率
				final JSONArray fullOdds = odds.getJSONArray("0");
				final JSONArray halfOdds = odds.getJSONArray("1");
				int fullSize = fullOdds == null ? 0 : fullOdds.size(), halfSize = halfOdds == null ? 0 : halfOdds.size();
				final List<OddsInfo> oddsInfos = new ArrayList<>(fullSize + halfSize);
				if (fullSize > 0) {
					// 0=让球盘；
					parseRAndOu(fullOdds, 0, oddsInfos, OddsType.R);
					// 1=大小盘；
					parseRAndOu(fullOdds, 1, oddsInfos, OddsType.OU);
					// 2=主客平
					parseM(fullOdds, oddsInfos, OddsType.M);
				}

				// 上半场赔率
				if (halfSize > 0) {
					// 0=让球盘
					parseRAndOu(halfOdds, 0, oddsInfos, OddsType.HR);
					// 1=大小盘
					parseRAndOu(halfOdds, 1, oddsInfos, OddsType.HOU);
					// 2=主客平
					parseM(halfOdds, oddsInfos, OddsType.HM);
				}
				final Long id = game.getLong(0);
				final String teamHomeZh = parseTeamName(game.getString(1)), teamClientZh = parseTeamName(game.getString(2)),
						teamHomeEn = parseTeamName(game.getString(24)), teamClientEn = parseTeamName(game.getString(25));
				final Date openTime = game.getDate(4);
				gameDTOS.add(new GameDTO(id, getProvider(), openTime, convertLeague(leagueEn), teamHomeEn, teamClientEn, oddsInfos, now)
						.initZh(leagueZh, teamHomeZh, teamClientZh));
			}
		}
	}

	/**
	 * 解析独赢盘
	 */
	private static void parseM(JSONArray fullOdds, List<OddsInfo> oddsInfos, OddsType oddsType) {
		final JSONArray ms = fullOdds.getJSONArray(2);
		if (ms != null && !ms.isEmpty()) {
			oddsInfos.add(new OddsInfo(oddsType, ms.getDouble(1),
					ms.getDouble(0), ms.getDouble(2)));
		}
	}

	/**
	 * 解析让球盘和大小盘
	 */
	private static void parseRAndOu(JSONArray fullOdds, int index, List<OddsInfo> oddsInfos, OddsType oddsType) {
		final JSONArray rs = fullOdds.getJSONArray(index);
		if (rs != null && !rs.isEmpty()) {
			for (Object r : rs) {
				JSONArray row = (JSONArray) r;
				oddsInfos.add(new OddsInfo(oddsType, parseRatioRate(row.getString(2)),
						row.getDouble(3), row.getDouble(4)));
			}
		}
	}

	/**
	 * 解析赔率盘口值/比率
	 *
	 * @see OddsInfo#ratioRate
	 */
	protected static String parseRatioRate(String ratioRate) {
		return "0.0".equals(ratioRate) ? "0" : ratioRate.replaceFirst("-", "/");
	}

	public static String parseTeamName(String teamName) {
		final int idx = teamName.indexOf("\\r\\n");
		if (teamName.startsWith("Al-")) {
			teamName = teamName.replaceFirst("Al-", "Al ");
		}
		return (idx > 0 ? teamName.substring(0, idx) : teamName).trim();
	}

	/**
	 * 等待响应的 UUID 映射（订阅消息 -> 响应 Future）
	 */
	final Map<String, Pair<String, CompletableFuture<JSONObject>>> pendingMap = new ConcurrentHashMap<>(100, 1F);

	public JSONObject getGameBets(WebSocket webSocket, String lang, boolean today) {
		final String uuid = genUuid();
		final String msg = "{\"type\":\"SUBSCRIBE\",\"destination\":\"ODDS\",\"body\":{\"sp\":29,\"lg\":\"\",\"ev\":\"\","
				+ "\"mk\":" + (today ? 1 : 0) + ","
				+ "\"btg\":\"1\",\"ot\":1,\"d\":\"\",\"o\":1,\"l\":3,\"v\":\"\",\"lv\":\"\",\"me\":0,\"more\":false,\"lang\":\"\",\"tm\":0,\"pa\":0,"
				+ "\"c\":\"\",\"g\":\"QQ==\",\"pn\":-1,\"ec\":\"\",\"cl\":3,"
				+ "\"hle\":" + !today + ","
				+ "\"pimo\":\"0,1,8,39,2,3,6,7,4,5\",\"inl\":false,\"pv\":1,\"ic\":false,\"ice\":false,\"dpVXz\":\"ZDfaFZUP9\","
				+ "\"locale\":\"" + lang + "\"},"
				+ "\"id\":\"" + uuid + "\"}";
		return syncRequestWs(webSocket, uuid, msg);
	}

	public String genUuid() {
		while (true) {
			final String uuid = UUID.randomUUID().toString();
			if (!pendingMap.containsKey(uuid)) {
				return uuid;
			}
		}
	}

	public JSONObject syncRequestWs(WebSocket webSocket, String uuid, String msg) {
		final CompletableFuture<JSONObject> future = new CompletableFuture<>();
		// 🚩 只有发送成功（或失败）之后才注册 future 到 pendingMap
		webSocket.sendText(msg, true)
				.thenRun(() -> {
					// 发送成功后允许监听响应
					pendingMap.put(uuid, Pair.of(
							msg.replaceFirst("SUBSCRIBE", "UNSUBSCRIBE"),
							future
					));
				})
				.exceptionally(ex -> {
					// 发送失败，直接 fail 掉 future
					future.completeExceptionally(ex);
					return null;
				});
		try {
			return future.get(5, TimeUnit.SECONDS);
		} catch (Exception e) {
			log.warn("获取数据失败或超时，UUID: {}", uuid);
			return null;
		} finally {
			pendingMap.remove(uuid);
		}
	}

	private final StringBuilder buffer = new StringBuilder();
	/**
	 * 是否处于丢弃模式
	 */
	private volatile boolean discarding = false;

	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
		// 第一帧必然调用（buffer 空 + discarding=false 的状态下进入）
		if (!discarding && buffer.isEmpty()) {
			final String chunk = data.toString();

			// 第一帧不包含 FULL_ODDS → 进入丢弃模式
			if (!chunk.contains("\"type\":\"FULL_ODDS\"")) {
				// 单帧即结束，直接退出丢弃状态
				discarding = !last;
				return super.onText(webSocket, data, last);
			}
		}

		// 非丢弃模式才拼接
		if (!discarding) {
			buffer.append(data);
		}

		// 包尾处理
		if (last) {
			if (!discarding && !buffer.isEmpty()) {
				final String jsonData = buffer.toString();
				// log.debug("收到 FULL_ODDS 数据: {}", jsonData);

				// 处理 JSON
				if (jsonData.contains("\"type\":\"FULL_ODDS\"")) {
					final JSONObject event = Jsons.parseObject(jsonData);
					final String id = event.getString("id");
					final Pair<String, CompletableFuture<JSONObject>> pair = pendingMap.remove(id);
					if (pair != null) {
						webSocket.sendText(pair.getLeft(), true); // 取消订阅
						pair.getRight().complete(event);
					}
				}
			}
			// 无论是正常还是丢弃，结束后都要 reset
			buffer.setLength(0);
			discarding = false;
		}
		// 回调父级获取下一帧数据
		return super.onText(webSocket, data, last);
	}

	@Override
	protected HttpClient currentClient() {
		HttpClient client = getProxyClient();
		return client == null ? defaultClient() : client;
	}

	@Override
	protected String getWsUrl() {
		final String wsToken = getWsToken();
		if (wsToken == null) {
			throw new ErrorMessageException("获取WS Token失败");
		}

		final JSONObject login = getLoginToken();
		final String ulp = login.getString("_ulp");
		// 建立连接
		final BetApiConfig config = this.getConfig();
		return "wss://" + config.domain + "/sports-websocket/ws?token=" + wsToken + "&ulp=" + ulp;
	}

	final String pingMsg = "{\"type\":\"PONG\",\"destination\":\"ALL\"}";

	@Override
	protected String getPingMsg() {
		return pingMsg;
	}

	@Override
	protected JSONObject doLogin(String lang) {
		final Map<String, Object> params = new TreeMap<>();
		final BetApiConfig config = this.getConfig();
		params.put("loginId", config.username);
		params.put("password", config.password);
		params.put("Referer", this.getConfig().endPoint + "/" + lang.toLowerCase() + "/sports/soccer");
		final Messager<JSONObject> result = sendRequest(HttpMethod.POST, buildURI("/member-auth/v2/authenticate", lang), params);
		return result.data().getJSONObject("tokens");
	}

	@Nullable
	public String getWsToken() {
		getLoginToken();
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

	@Override
	protected HttpRequest.Builder createRequest(HttpMethod method, URI uri, Map<String, Object> params, int flags) {
		HttpRequest.Builder builder = super.createRequest(method, uri, params, flags);
		final String endPoint = this.getConfig().endPoint;
		builder.setHeader("origin", endPoint);
		builder.setHeader("referer", (String) params.remove("Referer"));
		builder.setHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36");
		builder.setHeader("x-trust-client", "false");
		if (loginInfo != null) {
			final String XBrowserSessionId = loginInfo.getString("X-Browser-Session-Id");
			builder.setHeader("x-app-data", loginInfo.getString("X-App-Data"));
			builder.setHeader("x-custid", loginInfo.getString("custid"));
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
	protected JSONObject responseBodyToJSON(String responseBody) {
		return StringUtil.isEmpty(responseBody) ? null
				: responseBody.startsWith("[") ? JSONObject.of("data", Jsons.parseArray(responseBody))
				: Jsons.parseObject(responseBody);
	}

	@Override
	protected String mapStatus(JSONObject json, HttpResponse<String> response) {
		if (json == null) {
			// 兼容赛果不返回数据 -> 掉登录
			if (response.request().uri().getPath().endsWith("/results/events")) {
				clearLoginToken();
			}
			return null;
		}
		final String errorCode = (String) getErrorCode(json);
		if (errorCode != null && !"1".equals(errorCode)) {
			if ("403".equals(errorCode)) {
				clearLoginToken();
			}
			return null;
		}
		return Messager.OK;
	}

	@Override
	protected Object getErrorCode(JSONObject json) {
		final String code = json.getString("code");
		return code == null ? json.getString("error") : code;
	}

	@Override
	protected String postHandleResult(final Messager<JSONObject> result, String responseBody, HttpResponse<String> response) {
		if (response.statusCode() == 403) {
			clearLoginToken();
			return responseBody;
		}
		if (response.request().uri().getPath().endsWith("/authenticate")) { // 登录响应头信息
			Map<String, List<String>> headers = response.headers().map();
			final List<String> list = headers.get("X-App-Data");
			final JSONObject data = result.data();
			if (data != null) {
				final JSONObject tokens = data.getJSONObject("tokens");
				if (list != null && !list.isEmpty()) {
					tokens.put("X-App-Data", list.get(0));
				}
				List<String> setCookies = headers.get("set-cookie");
				if (setCookies != null) {
					for (String cookie : setCookies) {
						// _ulp=azZlNWJKMlVrUG9WSlpZSThvUS9Ua3o1UWRjQngrUG5ENHpVcFB0YU95bWJFaHE5c0VzYVRiaE5aQkh1ZnQyeUdMMXJJOWQ4dVhWdWNkYzBCbVVsY2c9PXw5MjljMDgxZmQ2NDdiYTIyYjQ5NWY4NGYwZDAwMzVjOQ==; Path=/; Domain=.ps3838.com; HttpOnly; SameSite=None; Secure
						if (cookie.startsWith("_ulp=")) {
							tokens.put("_ulp", cookie.substring(5, cookie.indexOf(";")));
						} else if (cookie.startsWith("custid=")) {
							// custid=id=ATLUBCP004&login=202601090027&roundTrip=202601090027&hash=6B19901E568660C19D41145DDF0F2669; Path=/; Domain=.ps3838.com; Expires=Fri, 09-Jan-2026 06:27:42 GMT; SameSite=None; Secure
							tokens.put("custid", cookie.substring(7, cookie.indexOf(";")));
						}
					}
				}
			}
		}
		return responseBody;
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

	@Override
	public Map<Object, ScoreResult> getScoreResult() {
		getLoginToken();
		final String lang = getLanguage(LANG_EN); // 需要确保联赛名称一定是英文
		final EasyDate d = new EasyDate();
		final Map<Object, ScoreResult> scoreMap = new HashMap<>();
		final TreeMap<String, Object> params = new TreeMap<>();
		final String referer = this.getConfig().endPoint + "/" + lang.toLowerCase() + "/account/results";
		for (int i = 0; i < 2; i++) { // 只查询近两天的数据
			d.addDay(-i);
			params.put("Referer", referer);
			final Messager<JSONObject> result = sendRequest(HttpMethod.GET, URI.create(this.getConfig().endPoint
					+ "/member-service/v2/results/events?sp=29&lg=0&o=LEAGUE&d=" + d
					+ "&locale=" + lang + "&_" + System.currentTimeMillis()), params, FLAG_LOG_OUT_BRIEF_BODY);
			if (result.isOK()) {
				final JSONArray scoreResults = result.data().getJSONArray("data").getJSONArray(2);
				if (scoreResults != null && !scoreResults.isEmpty()) {
					for (Object o : scoreResults) {
						final JSONArray leagueGroup = (JSONArray) o;
						final String leagueName = convertLeague(leagueGroup.getString(0));
						JSONArray scores = leagueGroup.getJSONArray(1);
						if (scores != null && !scores.isEmpty()) {
							for (Object s : scores) {
								final JSONArray gameScore = (JSONArray) s;
								JSONObject score = gameScore.getJSONObject(6);
								if (score != null) {
									final ScoreResult scoreResult = new ScoreResult();
									scoreResult.setLeagueName(leagueName);
									scoreResult.setTeamHome(gameScore.getString(8));
									scoreResult.setTeamClient(gameScore.getString(9));
									scoreResult.setOpenTime(gameScore.getDate(2));
									scoreResult.setScore(parseScore(score.getString("0")));
									scoreResult.setScoreH(parseScore(score.getString("1")));
									scoreMap.put(scoreResult, scoreResult);
								}
							}
						}
					}
				}
			}
		}
		return scoreMap;
	}

	@Override
	public ScoreResult scoreGetter(Map<Object, ScoreResult> scoreResult, GameDTO game) {
		return scoreResult.get(game);
	}

	private static Integer[] parseScore(String scoreVal) {
		if (StringUtil.notEmpty(scoreVal)) {
			int idx = scoreVal.indexOf("-");
			if (idx > 0) { // 不能等于0，等于0代表还没出结果
				return new Integer[] { Integer.parseUnsignedInt(scoreVal, 0, idx, 10),
						Integer.parseUnsignedInt(scoreVal, idx + 1, scoreVal.length(), 10) };
			}
		}
		return null;
	}

	@Override
	public String convertLeague(String league) {
		league = league.replaceFirst(" -", "")
				// 处理形如 "Egypt 2nd Division" → "Egypt Division 2"
				.replaceFirst("(\\d+)nd Division", "Division $1")
				.replaceFirst("National (\\d+)", "Championnat National $1");
		return switch (league) {
			case "Egypt 2nd Division A" -> League.EgyptDivision1;
			case "Bahrain 2nd Division" -> League.BahrainDivision2;
			case "Cyprus 2nd Division", "Cyprus Division 2" -> League.CyprusDivision2;
			case "Saudi Arabia Division 2" -> League.EgyptDivision2;
			case "CAF Africa Cup of Nations" -> League.AfricaCupOfNations2025InMorocco2En;
			case "England EFL Trophy" -> League.EnglandFootballLeagueTrophy;
			case "Spain La Liga" -> League.SpainPrimeraDivision;
			case "Belgium Pro League" -> League.BelgiumFirstDivisionA;
			case "Argentina Liga Pro" -> League.ArgentinaLigaProfesional;
			case "Saudi Arabia Pro League", "Saudi Pro League" -> League.SaudiProLeague;
			case "Germany 3. Liga" -> League.Germany3rdLiga;
			case "Cyprus 1st Division" -> League.CyprusDivision1;
			case "Turkey 1st League" -> League.TurkeyTFFFirstLeague;
			case "England Isthmian Premier League" -> League.EnglandIsthmianPremierDivision;
			case "Egypt 2nd Division B" -> League.EgyptDivision2B;
			case "AFC U23 Asian Cup" -> League.AFCU23AsianCup2026InSaudiArabia;
			case "Saudi Arabia Division 1" -> League.SaudiDivision1;
			case "Mexico Liga de Expansión MX" -> League.MexicoLigaExpansionMX;
			case "Bahrain Kings Cup" -> League.BahrainKingCup;
			case "Germany Bundesliga" -> League.GermanyBundesliga1;
			case "Israel Ligat Leumit" -> League.IsraelLeague1;
			case "England Championship" -> League.EnglandLeagueChampionship;
			case "Greece Super League" -> League.GreeceSuperLeague1;
			case "Italy Serie C Group A", "Italy Serie C Group B", "Italy Serie C Group C",
			     "Italy Serie C Group D" -> League.ItalySerieC;
			case "Netherlands Cup" -> League.NetherlandsKNVBCup;
			case "France National" -> League.FranceChampionnatNational;
			case "England EFL Cup" -> League.EnglandLeagueCup;
			case "Italy Primavera U20" -> League.ItalyCampionatoPrimavera1U20;
			case "Colombia Superliga" -> League.ColombiaSuperCup;
			case "England Southern League Division 1" -> League.EnglandSouthernLeagueDivisionOneSouth;
			case "FIFA World Cup Qualifiers Europe" -> League.FIFAWorldCup2026EuropeQualifiersPlayOff;
			case "UAE Reserve League U23" -> League.UAEProLeagueU23;
			case "Brazil Sao Paulo Cup U20" -> League.BrazilCopaSaoPauloJuniorU20;
			case "FIFA World Cup" -> League.FIFAWorldCup2026InCanadaMexico_USA;
			case "Mauritania Ligue 1" -> League.MauritaniaSuperD1;
			case "Oman Cup" -> League.OmanSultanCup;
			case "Uruguay Copa de la Liga AUF" -> League.UruguayCopadelaLigaAUF;
			case "Uruguay - League Cup" -> "乌拉圭足协联赛杯";
			case "Brazil Paulista" -> League.BrazilCampeonatoPaulistaSerieA1;
			case "Portugal U23 Championship" -> League.PortugalLigaRevelacaoU23Playoff;
			case "International Finalissima" -> League.FinalissimaCupofChampions2026InQatar;
			case "Israel Premier League Women" -> League.IsraelWomenPremierLeague;
			case "Northern Ireland Reserve League" -> League.NorthernIrelandPremiershipDevelopmentLeagueU20;
			case "Bahrain U21 League" -> League.BahrainLeagueU21;
			case "Colombia Super Cup" -> League.ColombiaSuperliga;
			case "Spain Primera Division Women" -> League.SpainWomenPrimeraDivision;
			case "Portugal League Cup Women" -> League.PortugalWomenLeagueCup;
			case "Germany Cup" -> League.GermanyDFBCup;
			case "Brazil Mineiro" -> League.BrazilCampeonatoMineiroDivision1;
			case "Brazil Paulista A2" -> League.BrazilCampeonatoPaulistaSerieA2;
			case "Brazil Gaucho" -> League.BrazilCampeonatoGauchoSerieA1;
			case "France Feminine Division 1" -> League.FranceWomenPremiereLigue;
			case "Brazil Paranaense" -> League.BrazilCampeonatoParanaenseDivision1;
			case "Spain Copa Catalunya" -> League.BrazilCopaCatalunya;
			case "Brazil Baiano" -> League.BrazilCampeonatoBaianoSerieA;
			case "Brazil Carioca" -> League.BrazilCampeonatoCariocaSerieA;
			case "International Premier League Cup U21" -> League.PremierLeagueInternationalCupInEngland;
			case "International Friendlies U19" -> League.InternationalFriendlyU19;
			case "Brazil Catarinense" -> League.BrazilCampeonatoCatarinenseSerieA;
			case "England Southern Premier League Central" -> League.EnglandSouthernPremierDivisionCentral;
			case "Brazil Alagoano" -> League.BrazilCampeonatoAlagoanoDivision1;
			case "South Africa PSL" -> League.SouthAfricaPremiership;
			case "Israel U19 Elite Division" -> League.IsraelPremierLeagueU19;
			case "Brazil Pernambucano" -> League.BrazilCampeonatoPernambucanoSerieA1;
			case "Italy Serie D Group A", "Italy Serie D Group B", "Italy Serie D Group C",
			     "Italy Serie D Group D", "Italy Serie D Group E" -> League.ItalySerieD;
			case "Vietnam U19 Womens Championship" -> League.VietnamChampionshipWomenU19;
			case "Club Friendlies Women" -> League.ClubFriendlyWomen;
			case "Wales Premier Womens League" -> League.WalesPremierLeagueWomen;
			case "Tunisia League 1" -> League.TunisiaLigue1;
			case "Scotland Cup" -> League.ScotlandFACup;
			case "Spain Primera RFEF Group 1", "Spain Primera RFEF Group 2", "Spain Primera RFEF Group 3" -> League.SpainPrimeraFederacion;
			case "Philippines UAAP Championship" -> League.PhilippinesUAAPFootballChampionship;
			case "Spain Segunda RFEF Group 1", "Spain Segunda RFEF Group 2", "Spain Segunda RFEF Group 3",
			     "Spain Segunda RFEF Group 4", "Spain Segunda RFEF Group 5" -> League.SpainSegundaFederacion;
			case "Qatar U19 League" -> League.QatarLeagueU19;
			case "Algeria U20 League" -> League.AlgeriaLeagueU20;
			case "Malaysia Liga A1" -> League.MalaysiaA1SemiProLeague;
			case "Belgium Super League Women" -> League.BelgiumWomenSuperLeague;
			case "UAE U21 Cup" -> League.UAECupU21;
			case "France National 2" -> League.FranceChampionnatNational2;
			case "Ireland Leinster Senior League", "Ireland Leinster Senior League Senior Division",
			     "Ireland Munster Senior League" -> League.IrelandLeinsterSeniorLeagueSeniorDivision;
			case "Belgium Reserve Pro League U21" -> League.BelgiumProLeagueU21;
			case "England FA Cup Women" -> League.EnglandFAWomenCup;
			case "Iceland Reykjavik Cup Women" -> League.ReykjavikWomenFootballTournamentInIceland;
			case "England Southern Premier League South" -> League.EnglandSouthernPremierDivisionSouth;
			case "Philippines PFL" -> League.PhilippinesFootballLeague;
			case "England Isthmian Division 1 North" -> League.EnglandIsthmianLeagueDivisionOneNorth;
			case "England Northern Premier League" -> League.EnglandNorthernPremierDivision;
			case "Brazil Brasiliense" -> League.BrazilCampeonatoBrasilienseDivision1;
			case "France U19 League" -> League.FranceChampionnatNationalU19;
			case "South Africa Diski Challenge U23" -> League.SouthAfricaPremierLeagueReserve;
			case "Italy Primavera 2 U19" -> League.ItalyCampionatoPrimavera2U19;
			case "Brazil Acreano" -> League.BrazilCampeonatoAcreanoDivision1;
			case "Turkey Super Lig U19" -> League.TurkeyPAFLeagueU19;
			case "Tunisia League 2" -> League.TunisiaLigue2;
			case "Myanmar Women League" -> League.MyanmarMFFLeagueWomen;
			case "France National 3" -> League.FranceChampionnatNational3;
			case "Turkey 3rd League Group 1", "Turkey 3rd League Group 2", "Turkey 3rd League Group 3",
			     "Turkey 3rd League Group 4" -> League.TurkeyTFFThirdLeague;
			case "Spain Tercera Division" -> League.SpainTerceraFederacion;
			case "Portugal Campeonato de Portugal Prio" -> League.PortugalDeCampeonato;
			case "Hong Kong HKFA 1. Division" -> League.HongKongDivision1;
			case "Italy Serie A Women" -> League.ItalyWomenSerieA;
			case "Kenya Super League" -> League.KenyaPremierLeague;
			case "Turkey 2nd League" -> League.TurkeyTFFSecondLeague;
			case "Albania 1st Division" -> League.AlbaniaDivision1;
			case "Brazil Sergipano" -> League.BrazilCampeonatoSergipanoSerieA1;
			case "Brazil Goiano" -> League.BrazilCampeonatoGoianoDivision1;
			case "Portugal Cup Women" -> League.PortugalWomenCup;

			default -> league;
		};
	}

}