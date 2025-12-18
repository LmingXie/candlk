package com.bojiu.webapp.user.dto;

import java.util.*;

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
		final String aHomeId = getStandardName(aGame.teamHome), aClientId = getStandardName(aGame.teamClient);

		for (GameDTO bGame : bGames) {
			final String bHomeId = getStandardName(bGame.teamHome), bClientId = getStandardName(bGame.teamClient);

			// 只需要简单的字符串相等判断 (ID 化比对)
			final boolean isMatch = (aHomeId.equals(bHomeId) && aClientId.equals(bClientId))
					|| (aHomeId.equals(bClientId) && aClientId.equals(bHomeId));

			if (isMatch) {
				return bGame;
			}
		}
		return null;
	}

}