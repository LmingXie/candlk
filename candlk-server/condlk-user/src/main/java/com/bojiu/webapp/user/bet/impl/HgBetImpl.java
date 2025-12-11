package com.bojiu.webapp.user.bet.impl;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.bojiu.webapp.user.bet.BetApi;
import com.bojiu.webapp.user.model.BetProvider;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.CollectionUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HgBetImpl implements BetApi {

	@Override
	public BetProvider getProvider() {
		return BetProvider.HG;
	}

	/*
	联赛列表：
	get_league_count
		FT  足球
		BK  篮球
		TN  网球
		ES  电子竞技
		VB  排球
		SK  台球/斯诺克
		OP  其他
		RB_count    滚球
		FT_count    今日
		FS_FU_count + P3_FU_count   早盘

	 */
	// final HttpClient proxyHttpClient;

	// public HgBetImpl(@Value("${service.proxy-conf}") String proxyConfig) {
	// 	// proxyHttpClient = BaseHttpUtil.getProxyOrDefaultClient(proxyConfig);
	// }

	@Override
	public Set<String> pull() throws IOException {
		// return googleTrendingKeywords;
		return null;
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