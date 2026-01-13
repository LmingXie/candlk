package com.bojiu.webapp.user.dto;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.bojiu.common.redis.RedisUtil;
import com.bojiu.webapp.user.bet.BetApi;
import com.bojiu.webapp.user.model.BetProvider;
import com.bojiu.webapp.user.model.UserRedisKey;
import me.codeplayer.util.CollectionUtil;
import me.codeplayer.util.X;
import org.springframework.data.redis.core.HashOperations;

/** 球队匹配器 */
public class TeamMatcher {

	// 1. 定义原始别名常量
	private static final String[] ALIAS_CONFIG = {
			"林肯红魔,林肯瑞德因普斯",
			"莫斯塔尔辛尼斯基,瑞尼斯基",
			"维也纳迅速,维也纳快速",
			"波兹南莱希,波兹南莱赫",
			"奥洛穆茨西格玛,奥洛莫茨",
			"斯蒂文尼奇,斯蒂夫尼奇",
			"保顿艾尔宾,伯顿",
			"赫拉克勒斯,阿尔梅罗大力神",
			"海伦维恩,海伦芬",
			"莫拉松,马拉松",
			"普拉腾斯科尔特斯港,普拉坦斯",
			"喜柏尼恩斯波拉,希伯尼恩斯",
			"圣塔克莱拉,圣克拉拉",
			"扎巴尔圣巴特克,扎巴尔圣帕特里克",
			"华伦西亚,瓦伦西亚",
			"马略卡,马洛卡",
			"克雷莫内塞,克雷莫纳",
			"利云特,莱万特",
			"皇家苏斯达,皇家社会",
			"奥维多,皇家奥维耶多",
			"维戈塞尔塔,塞尔塔",
			"比雷亚雷亚尔,比利亚雷亚尔",
			"斯帕肯堡,斯巴肯堡",
			"布里斯托城,布里斯托尔城",
			"南安普敦,南安普顿",
			"西布朗,西布罗姆维奇",
			"纽卡斯尔,纽卡斯尔联",
			"奥斯堡,奥格斯堡",
			"柏林联,柏林联合",
			"爱斯宾奴,西班牙人",
			"皇家贝迪斯,皇家贝蒂斯",
			"海登海默,海登海姆",
			"艾尔切,埃尔切",
			"欧伦塞,奥伦塞",
			"奧摩尼亚,奥莫尼亚",
			"沃伦丹,福伦丹",
			"咸仑斯巴坦,哈姆伦斯巴达",
			"达他,德里塔",
			"伊斯坦堡巴萨希尔,伊斯坦布尔BFK",
			"锡塔德命运,福图纳锡塔德",
			"科木,科莫",
			"瑞尼斯基,莫斯塔尔辛尼斯基",
			"北安普敦,北安普顿",
			"温布尔登,AFC温布尔登",
			"采列,佩利根",
			"贝雷达比历克,布列达布利克",
			"艾斯特城,埃克塞特城",
			"斯肯迪加,斯肯迪亚",
			"RB莱比锡,莱比锡红牛",
			"波兹南莱希,波兹南莱赫",
			"奥洛穆茨西格玛,奥洛莫茨",
			"唐卡斯特流浪者,唐卡斯特",
			"普利茅夫,普利茅斯",
			"CS卡拉奥华大学,克拉约瓦大学",
			"布拉德福德,布拉德福德城",
			"费马利卡奥,法马利康",
			"鹿特丹精英,SBV精英",
			"马鲁特联,马鲁北联",
			"马卡萨,望加锡",
			"阿舒多,阿什杜德",
			"沙克尼,萨赫宁比尼",
			"奥林匹亚科斯B队,奥林匹亚科斯II队",
			"帕纳尔基亚高斯,帕纳吉亚高斯",
			"阿鲁卡,阿罗",
			"圣塔克莱,圣克拉拉",
			"吉奥利,吉奧利",
			"普斯卡斯学院,普斯卡什学院",
			"海法夏普尔,海法工人",
			"耶路撒冷夏普尔,耶路撒冷哈普尔",
			"埃尔祖鲁姆士邦,埃尔祖鲁姆体育",
			"班迪马士邦,班德尔马体育",
			"帕纳多里高斯,帕纳托利克斯",
			"沃罗斯,沃洛斯",
			"苏达迪罗尔,苏迪路",
			"祖华史塔比亚,尤维斯塔比亚",
			"CS康桑汰,CS君士坦丁",
			"USM罕西拉,肯彻莱",
			"阿尔萨德U23,萨德U23",
			"夏马尔U23,北部区体育U23",
			"泰拉克托,大不里士拖拉机",
			"加兹温,沙姆斯阿扎尔卡兹温",
			"霍拉馬巴德,霍拉马巴德",
			"派肯,培坎",
			"查多玛路,查多尔·马鲁·亚兹德",
			"沙巴罕,塞帕汉",
			"阿尔泰,塔伊",
			"亚哈,阿布哈",
			"贝罗,巴罗",
			"艾宁顿,阿克灵顿",
			"阿尔艾利吉达,吉达国民",
			"阿尔菲斯,哈萨征服",
			"巴里纳马拉德联,巴里纳马勒联",
			"沙巴柏利雅德,利雅得沙巴布",
			"伊蒂哈德吉达,吉达联合",
			"婆罗洲三马林达,婆罗洲",
			"马鲁特联,马鲁北联",
			"甘马雷斯,吉马良斯",
			"卡莎皮亚,卡萨皮亚",
			"埃斯特雷拉达阿马多拉,阿马多拉",
			"费马利卡奥,法马利康",
			"阿尔萨德,萨德",
			"夏马尔,北部区体育",
			"艾尔雷恩,赖扬",
			"阿拉毕多哈,多哈阿拉伯人",
			"吉尔维森特,吉维森特",
			"阿鲁卡,阿罗卡",
			"哈洛贾特,哈罗盖特",
			"莎尔福德城,索尔福德城",
			"MSP巴特拿,巴特纳",
			"MO康桑汰,MO康士坦丁",
			"艾尔雷恩U23,赖扬U23",
			"阿拉毕多哈U23,多哈阿拉伯人U23",
			"迈奈耶勒堡,博尔德梅奈尔",
			"NRB特哈玛,NRB电报",
			"拉尔,莱尔",
			"荷利赫德,霍利海德热刺",
			"哈塔斯堡,哈塔伊体育",
			"凡斯珀尔,贝拉迪亚士邦",
			"士登后斯莫亚,斯坦豪斯摩尔",
			"阿洛厄,艾洛亚竞技",
			"USM阿尔格,USM阿尔及尔",
			"奥德阿库,奥林匹克阿克布",
			"艾积尔奈恩斯,艾尔德里联",
			"亚布岳夫,阿布洛斯",
			"富力特城,弗利特伍德",
			"莎尔福德城,索尔福德城",
			"卡巴阿斯玛利,卡赫拉巴伊斯梅利亚",
			"塔拉雅,陆军先锋",
			"柯尔恩,夸恩",
			"Harborough Town,哈伯勒镇",
			"胡森伊尔比德,阿尔胡森",
			"蓝慕沙,拉姆塔",
			"史蒂愛文斯城,史蒂爱文斯城",
			"戴拿模城,迪纳摩城",
			"埃格纳提亚,伊纳迪亚",
			"深水埗体育会,深水埗",
			"Supreme,至尊",
			"伊蒂哈德卡尔巴U23,卡巴联合U23",
			"阿尔贾兹拉U23,阿布扎比贾兹拉U23",
			"德哈法U23,达弗拉U23",
			"般尼亚斯U23,班尼亚斯U23",
			"瓜达席亚,胡拜尔库迪西亚",
			"沙巴柏利雅德,利雅得沙巴布",
			"玛拉宛,马拉云",
			"泰拉克托,大不里士拖拉机",
			"纳撒利雅德,利雅得胜利",
			"伊地法格,达曼协作",
			"皇家贝迪斯U19,皇家贝蒂斯U19",
			"沙巴柏阿尔艾利杜拜U23,迪拜阿赫利沙巴布U23",
			"阿基曼U23,阿积曼U23",
			"帕庄内格洛,佩西博博波尼格罗",
			"Persika Karanganyar,佩西卡卡兰甘雅尔",
			"耶路撒冷夏普尔,耶路撒冷哈普尔",
			"比尔舒华夏普尔,贝尔谢巴工人",
			"科威特运动,斯波蒂",
			"阿尔贾兹拉艾哈迈迪,科威特詹辛拉",
			"祖文图德拉古纳U19,尤文图德U19",
			"马力诺U19,马利诺U19",
			"鲁思因城,拉辛城",
			"佩林科奇,佩莱恩科奇",
			"奥林比俱乐部,奥林比克",
			"马主俱乐部,埃及马主",
			"帕哈尔科,佩哈亚克",
			"切拉米卡克里奥帕特拉,克莉奥帕特拉",
			"雷乌斯雷迪斯,罗伊",
			"利尔达竞技,CE莱里达竞技",
			"艾宾奴列夫,阿尔比诺莱费",
			"贝卢诺多洛米蒂,多洛米蒂贝卢诺",
			"阿苏尔(女),蓝十字(女)",
			"利昂(女),莱昂(女)",
			"提华纳(女),蒂华纳(女)",
			"托拉卡(女),托卢卡体育(女)",
			"PAOK,塞萨洛尼基",
			"帕纳多里高斯,帕纳托利克斯",
			"泰利普利斯,特里波利斯",
			"OFI克雷迪,OFI克里特",
			"UD欧雲斯,UD欧云斯",
			"萨玛诺,萨曼诺",
			"FCSB,布加勒斯特星",
			"萨格勒布戴拿模,萨格勒布迪纳摩",
			"博幹蒂罗,博干蒂罗斯",
			"萨里亚纳,施萃娜",
			"卢多格德斯,卢多戈雷茨",
			"格拉斯哥流浪,流浪者",
			"泰利普利斯,特里波利斯",
			"OFI克雷迪,OFI克里特",
			"森多利亚,桑普多利亚",
			"阿维利诺,阿韦利诺",
			"亚摩勒比塔,阿莫雷维耶塔",
			"施斯达奥,塞斯陶河",
			"维多利科隆,科隆胜利",
			"斯科维夫,施韦因富特",
			"皇家贝迪斯,皇家贝蒂斯",
			"托德拉,通德拉",
			"莫雷伦斯,莫雷拉人",
			"诺夫哈加里夏普尔,纳扎雷斯伊利特",
			"奇亚亚蒙,基里亚特亚姆",
			"阿苏尔,蓝十字",
			"利昂,莱昂",
			"伯基纳法索,布基纳法索",
			"象牙海岸,科特迪瓦",
			"阿美利加会,美洲足球俱乐部",
			"提华纳,蒂华纳",
			"米迪兰特,中日德兰",
			"白兰恩,布兰",
			"托拉卡,托卢卡体育",
			"蒙特瑞,蒙特雷",
			"玛贝亚,托卢卡体育",
			"比雷亚雷亚尔B队,蒙特雷",
			"克努托内,克罗托内",
			"班尼文托,贝内文托",
			"乌德勒支青年队,乌德勒支(青年)",
			"瓦尔维克,瓦尔韦克",
			"Qus,El Qusiya",
			"Al Madina Al Monawara SC,El Madina El Monowara",
			"Al-Khaleej,Al Khaleej Saihat",
			"Al-Ettifaq,Al Ettifaq",
			"Al-Nassr,Al Nassr Riyadh",
			"Al-Hilal,Al Hilal Riyadh",
			"Al-Kholood,Al Kholood",
			"Al-Akhdoud,Al Okhdood",
			"Al Khalidiya,Al Khaldiya SC U21",
			"Al-Najma,Al Najma Manama U21",
	};

	// 2. 预处理后的倒排索引 Map
	private static final Map<String, String> ALIAS_MAP = new HashMap<>();

	static {
		for (String group : ALIAS_CONFIG) {
			final String[] names = group.split(",");
			final String standardName = names[0]; // 以第一个作为标准名
			for (String name : names) {
				ALIAS_MAP.put(name.trim(), standardName);
			}
		}
	}

	/**
	 * 获取队伍的标准身份
	 */
	private static String getStandardName(String teamName) {
		if (teamName == null) {
			return null;
		}
		// 先查别名映射表，查不到则返回原名（归一化处理）
		return ALIAS_MAP.getOrDefault(teamName, teamName);
	}

	/**
	 * 优化后的匹配方法
	 */
	public static GameDTO findMatchedGame(GameDTO aGame, List<GameDTO> bGames) {
		// 预先计算出 A 平台的主客队标准标识
		final String aHome = getStandardName(aGame.teamHome), aClient = getStandardName(aGame.teamClient);

		for (GameDTO bGame : bGames) {
			final String bHome = getStandardName(bGame.teamHome), bClient = getStandardName(bGame.teamClient);

			// 只需要简单的字符串相等判断 (ID 化比对)
			final boolean isMatch = (aHome.equals(bHome) && aClient.equals(bClient))
					|| (aHome.equals(bClient) && aClient.equals(bHome));

			if (isMatch) {
				return bGame;
			}
		}
		return null;
	}

	/** 团队与联赛名的英中文映射（） */
	private static final Map<BetProvider, Map<String, String>> enToZhCache = new ConcurrentHashMap<>(BetProvider.CACHE.length, 1F);

	public static Map<String, String> getEnToZhCacheMap(BetProvider betProvider, Set<GameDTO> gameEnDTOs) {
		return getEnToZhCacheMap(betProvider, gameEnDTOs, false);
	}

	public static Map<String, String> getEnToZhCacheMap(BetProvider betProvider, Set<GameDTO> gameEnDTOs, boolean flush) {
		if (flush) {
			return flushEnToZhCache(betProvider, gameEnDTOs, false);
		}
		return enToZhCache.computeIfAbsent(betProvider, k -> {
			final Map<String, String> redisCache = X.castType(RedisUtil.template().opsForHash().entries(UserRedisKey.TEAM_NAME_EN2ZH_CACHE + betProvider.name()));
			if (redisCache.isEmpty()) {
				return flushEnToZhCache(betProvider, gameEnDTOs, true);
			}
			return redisCache;
		});
	}

	/** 刷新缓存数据 */
	private static Map<String, String> flushEnToZhCache(BetProvider betProvider, Set<GameDTO> gameEnDTOs, boolean isCompute) {
		final Set<GameDTO> gameZhBets = BetApi.getInstance(betProvider).getGameBets(BetApi.LANG_ZH);
		final Map<String, String> cache = isCompute ? new HashMap<>() : enToZhCache.computeIfAbsent(betProvider, k -> new HashMap<>());
		final boolean empty = cache.isEmpty();
		final Map<String, String> newMap = new HashMap<>();
		final HashMap<Long, GameDTO> enGameMap = CollectionUtil.toHashMap(gameEnDTOs, GameDTO::getId);
		for (GameDTO zhDto : gameZhBets) {
			final GameDTO enDto = enGameMap.get(zhDto.getId());
			if (enDto != null) {
				if (empty) {
					newMap.put(enDto.getLeague(), zhDto.getLeague());
					newMap.put(enDto.getTeamHome(), zhDto.getTeamHome());
					newMap.put(enDto.getTeamClient(), zhDto.getTeamClient());
					cache.put(enDto.getLeague(), zhDto.getLeague());
					cache.put(enDto.getTeamHome(), zhDto.getTeamHome());
					cache.put(enDto.getTeamClient(), zhDto.getTeamClient());
				} else {
					if (!cache.containsKey(enDto.getLeague())) {
						newMap.put(enDto.getLeague(), zhDto.getLeague());
						cache.put(enDto.getLeague(), zhDto.getLeague());
					}
					if (!cache.containsKey(enDto.getTeamHome())) {
						newMap.put(enDto.getTeamHome(), zhDto.getTeamHome());
						cache.put(enDto.getTeamHome(), zhDto.getTeamHome());
					}
					if (!cache.containsKey(enDto.getTeamClient())) {
						newMap.put(enDto.getTeamClient(), zhDto.getTeamClient());
						cache.put(enDto.getTeamClient(), zhDto.getTeamClient());
					}
				}
			}
		}
		if (!newMap.isEmpty()) {
			RedisUtil.doInTransaction(redisOps -> {
				final HashOperations<String, Object, Object> opsForHash = redisOps.opsForHash();
				opsForHash.putAll(UserRedisKey.TEAM_NAME_EN2ZH_CACHE + betProvider.name(), newMap);
			});
		}
		return cache;
	}

}