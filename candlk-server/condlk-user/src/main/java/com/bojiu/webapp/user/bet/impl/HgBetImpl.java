package com.bojiu.webapp.user.bet.impl;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bojiu.common.model.Messager;
import com.bojiu.webapp.user.bet.BaseBetApiImpl;
import com.bojiu.webapp.user.model.BetProvider;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.CollectionUtil;
import me.codeplayer.util.StringUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Slf4j
@Service
public class HgBetImpl extends BaseBetApiImpl {

	@Override
	public BetProvider getProvider() {
		return BetProvider.HG;
	}

	@Override
	protected HttpClient currentClient() {
		HttpClient client = proxyClient;
		return client == null ? defaultClient() : client;
	}

	public HgBetImpl() {
		super();
		// 初始化代理客户端以及配置信息 TODO 元数据更新时需要同步刷新此配置
		initProxyClientAndConfig();
	}

	protected URI buildURI() {
		return URI.create(this.config.endPoint);
	}

	transient URI uri;

	protected URI buildURI(String version) {
		if (version == null || System.currentTimeMillis() - this.lastUpdateTime > flushInterval) {
			this.uri = URI.create(this.config.endPoint + "/transform.php?ver=" + version);
		}
		return uri;
	}

	/** 更新间隔 */
	static final long flushInterval = 1000 * 60 * 60 * 10;
	/** 版本信息（< $version, $iovationKey >） */
	transient Pair<String, String> version;
	transient long lastUpdateTime = 0;

	Pair<String, String> getVersion() {
		if (version == null || System.currentTimeMillis() - lastUpdateTime > flushInterval) {
			Messager<JSONObject> result = doVersion();
			if (result.isOK()) {
				// 该接口返回的是 HTML
				final String html = result.getCallback();
				if (StringUtil.notEmpty(html)) {
					String beginStr = "top.ver = '";
					int begin = html.indexOf(beginStr) + beginStr.length();
					final String ver = html.substring(begin, html.indexOf("';", begin));
					beginStr = "top.iovationKey = '";
					begin = html.indexOf(beginStr) + beginStr.length();
					final String iovationKey = html.substring(begin, html.indexOf("';", begin));
					version = Pair.of(ver, iovationKey);
					lastUpdateTime = System.currentTimeMillis();
					LOGGER.info("【{}游戏】获取版本信息：{}", getProvider(), version);
					return version;
				}
			}
		}
		return version;
	}

	private @NonNull Messager<JSONObject> doVersion() {
		final Map<String, Object> params = new TreeMap<>();
		params.put("detection", "Y");
		params.put("sub_doubleLogin", "");
		params.put("isapp", "");
		params.put("q", "");
		params.put("appversion", "");
		return sendRequest(HttpMethod.POST, buildURI(), params, FLAG_RETURN_TEXT);
	}

	transient JSONObject loginInfo;

	protected JSONObject doLogin() {
		if (loginInfo == null) {
			final Map<String, Object> params = new TreeMap<>();
			Pair<String, String> pair = getVersion();
			final String ver = pair.getKey();
			params.put("ver", ver);
			params.put("p", "chk_login");
			params.put("langx", "zh-cn");
			params.put("username", this.config.username);
			params.put("password", this.config.password);
			params.put("app", "N");
			params.put("auto", pair.getValue());
			params.put("blackbox", "");
			// Base64加密 Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36
			params.put("userAgent", "TW96aWxsYS81LjAgKFdpbmRvd3MgTlQgMTAuMDsgV2luNjQ7IHg2NCkgQXBwbGVXZWJLaXQvNTM3LjM2IChLSFRNTCwgbGlrZSBHZWNrbykgQ2hyb21lLzE0Mi4wLjAuMCBTYWZhcmkvNTM3LjM2");
			Messager<JSONObject> result = sendRequest(HttpMethod.POST, buildURI(ver), params, FLAG_RETURN_TEXT);
			if (result.isOK()) {
				loginInfo = result.data();
			}
		}
		return loginInfo;
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

	@Override
	protected String mapStatus(JSONObject json, HttpResponse<String> response) {
		return getErrorCode(json) == null ? Messager.OK : null;
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

	@Override
	public Set<String> pull() throws IOException {
		JSONObject login = doLogin();
		final String uid = login.getString("uid");
		// 查询赛事统计数据
		Messager<JSONObject> result = doGetLeagueCount(uid);
		if (result.isOK()) {
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
			// 若存在今日则读取今日赛事赔率，若存在早盘赛事才读取早盘赛事赔率
			for (int i = 0; i < stat.length; i++) {
				int count = stat[i];
				// 查询场次赔率
				if (count > 0) {
					/*
					让球盘：
						RATIO_R = 0.5
						1/1.5：
							主队 = IOR_RH = -1/1.5
							客队 = IOR_RC = +1/1.5

						RATIO_R = 1：
							主队 = IOR_RH = -1（此时应该+1）
							客队 = IOR_RC = +1（取原始IOR_RC）
					 */
					switch (i) {
						case 0 -> { // 今日盘
							result = doGetGameList(uid, true, null);
							if (result.isOK()) {
								data = result.data();
								JSONArray ecs = data.getJSONArray("ec");
								for (int j = 0, size = ecs.size(); j < size; j++) {
									JSONObject game = ecs.getJSONObject(j).getJSONObject("game");
								}
							}
						}
						case 1 -> { // 早盘
							for (Integer leagueId : leagueMap.keySet()) {
								result = doGetGameList(uid, false, leagueId);
								if (result.isOK()) {

								}
							}
						}
					}
				}
			}

		}
		return null;
	}

	/** 赛事统计数据 */
	protected Messager<JSONObject> doGetLeagueCount(String uid) {
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
		params.put("langx", "zh-cn");
		params.put("sorttype", "league");
		params.put("date", "ALL");
		params.put("ltype", "3");
		params.put("mode", "home");
		params.put("ts", System.currentTimeMillis());
		return sendRequest(HttpMethod.POST, buildURI(ver), params, FLAG_RETURN_TEXT);
	}

	/** 查询游戏场次列表 */
	protected Messager<JSONObject> doGetGameList(String uid, boolean isToday, Integer leagueId) {
		final Map<String, Object> params = new TreeMap<>();
		Pair<String, String> pair = getVersion();
		final String ver = pair.getKey();
		params.put("uid", uid);
		params.put("ver", ver);
		params.put("langx", "zh-cn");
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
			params.put("lid", leagueId);
			params.put("action", "click_league");
		}
		params.put("rtype", "r");
		params.put("ltype", "3");
		params.put("cupFantasy", "N");
		params.put("sorttype", "L");
		params.put("specialClick", "");
		params.put("isFantasy", "N");
		params.put("ts", System.currentTimeMillis());
		return sendRequest(HttpMethod.POST, buildURI(ver), params, FLAG_RETURN_TEXT);
	}

	/** 抓取的联赛赔率数据 */
	static final Map<Integer, String> leagueMap = CollectionUtil.asHashMap(
			100021, "英格兰超级联赛",
			100794, "英格兰联赛杯",
			// 100235, "英格兰冠军联赛",
			// 100127, "英格兰甲组联赛",
			// 100128, "英格兰乙组联赛",
			// 100283, "英格兰足球协会全国联赛",
			// 101912, "英格兰足球协会北部全国联赛",

			100028, "德国甲组联赛",
			// 100117, "德国乙组联赛",
			// 102529, "德国丙组联赛",

			100030, "西班牙甲组联赛",
			// 100064, "西班牙乙组联赛",

			100026, "意大利甲组联赛",
			// 100065, "意大利乙组联赛",

			100032, "法国甲组联赛",
			// 100078, "法国乙组联赛",

			100049, "荷兰甲组联赛"
			// 100265, "荷兰乙组联赛",

			// 100111, "比利时甲组联赛A",
			// 100286, "比利时乙组联赛",

			// 100123, "土耳其超级联赛",
			// 101743, "土耳其甲组联赛",

			// --- 新增区域和联赛 ---

			// // 阿根廷
			// 101182, "阿根廷职业联赛-附加赛",
			//
			// // 丹麦
			// 100789, "丹麦杯",
			//
			// // 亚洲
			// 100845, "亚足联冠军精英联赛",
			//
			// // 澳大利亚
			// 100710, "澳大利亚甲组联赛",
			// 102737, "澳大利亚女子甲组联赛",
			//
			// // 奥地利
			// 100110, "奥地利甲组联赛",
			// 100267, "奥地利乙组联赛",
			//
			// // 巴西
			// 100798, "巴西杯",
			//
			// // 智利
			// 102731, "智利杯",
			//
			// // 克罗地亚
			// 100182, "克罗地亚足球联赛",
			//
			// // 塞浦路斯
			// 100116, "塞浦路斯甲组联赛",
			//
			// // 捷克
			// 100083, "捷克甲组联赛",
			//
			// // 厄瓜多尔
			// 103370, "厄瓜多尔甲组联赛-附加赛",
			//
			// // 欧洲
			// 100822, "欧洲冠军联赛",
			// 100824, "欧洲联赛",
			// 109306, "欧洲协会联赛",
			// 101371, "欧洲女子冠军联赛",
			//
			// // 希腊
			// 100142, "希腊超级联赛甲组",
			//
			// // 印尼
			// 104523, "印尼超级联赛",
			//
			// // 国际
			// 109377, "世界杯2026欧洲外围赛-附加赛",
			// 103389, "非洲国家杯2025(在摩洛哥)",
			// 109816, "洲际杯2025",
			//
			// // 日本
			// 102798, "日本J2联赛-附加赛",
			//
			// // 墨西哥
			// 101142, "墨西哥超级联赛-附加赛",
			//
			// // 挪威
			// 101127, "挪威甲组联赛-附加赛",
			//
			// // 波兰
			// 100076, "波兰超级联赛",
			//
			// // 葡萄牙
			// 100173, "葡萄牙超级联赛",
			// 100047, "葡萄牙甲组联赛",
			//
			// // 罗马尼亚
			// 100103, "罗马尼亚甲组联赛",
			//
			// // 苏格兰
			// 100038, "苏格兰超级联赛",
			// 100797, "苏格兰联赛杯",
			// 100099, "苏格兰冠军联赛",
			//
			// // 斯洛伐克
			// 100085, "斯洛伐克超级联赛",
			//
			// // 瑞士
			// 100189, "瑞士超级联赛",
			// 100279, "瑞士甲组联赛"
	);

}