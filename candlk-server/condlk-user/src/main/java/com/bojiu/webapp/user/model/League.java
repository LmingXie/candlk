package com.bojiu.webapp.user.model;

public interface League {

	// --- 原有英格兰部分补充 ---
	String EnglishPremierLeague = "英格兰超级联赛";
	String EnglishLeagueCup = "英格兰联赛杯";
	String EnglishLeagueChampionship = "英格兰冠军联赛";
	String EnglishLeagueChampionshipSpecials = "英格兰冠军联赛-特别投注";
	String EnglishFootballLeagueTrophy = "英格兰联赛锦标赛";
	String EnglishLeague1 = "英格兰甲组联赛";
	String EnglishLeague2 = "英格兰乙组联赛";
	String EnglishNationalLeague = "英格兰足球协会全国联赛";
	String EnglishNationalLeagueNorth = "英格兰足球协会北部全国联赛";
	String EnglishNationalLeagueSouth = "英格兰足球协会南部全国联赛";
	String PremierLeagueCupU21 = "英格兰超级联赛杯U21";
	String EnglandNorthernPremierLeague = "英格兰北部超级联赛";
	String EnglandNorthernEastLeague1 = "英格兰北部东部甲组联赛";

	// --- 原有德国部分补充 ---
	String GermanyBundesliga1 = "德国甲组联赛";
	String GermanyBundesliga2 = "德国乙组联赛";
	String GermanyBundesliga2Specials = "德国乙组联赛-特别投注";
	String Germany3rdLiga = "德国丙组联赛";
	String GermanyWomenBundesliga1 = "德国女子甲组联赛";

	// --- 原有西班牙部分补充 ---
	String SpainPrimeraDivision = "西班牙甲组联赛";
	String SpainSegundaDivision = "西班牙乙组联赛";
	String SpainPrimeraDivisionU19 = "西班牙甲组联赛U19";
	String SpainFederationLeague1 = "西班牙足协甲组联赛";
	String SpainQueensCup = "西班牙皇后杯";

	// --- 原有意大利部分补充 ---
	String ItalySerieA = "意大利甲组联赛";
	String ItalySerieB = "意大利乙组联赛";
	String ItalySerieC = "意大利丙组联赛";
	String ItalyPrimavera2U19 = "意大利青年春季乙组联赛U19";
	String ItalySuperCup = "意大利超级杯";

	// --- 原有法国部分补充 ---
	String FranceLigue1 = "法国甲组联赛";
	String FranceLigue2 = "法国乙组联赛";
	String FranceCup = "法国杯";

	// --- 原有荷兰部分补充 ---
	String NetherlandsEredivisie = "荷兰甲组联赛";
	String NetherlandsEersteDivisie = "荷兰乙组联赛";
	String NetherlandsDivision1U21 = "荷兰甲组联赛1 U21";

	// --- 原有比利时部分补充 ---
	String BelgiumFirstDivisionA = "比利时甲组联赛A";
	String BelgiumChallengerProLeague = "比利时乙组联赛";
	String BelgiumWomenSuperLeague = "比利时女子超级联赛";
	String BelgiumDivision3 = "比利时丙组联赛";

	// --- 原有土耳其部分 ---
	String TurkeySuperLeague = "土耳其超级联赛";
	String TurkeyTFFFirstLeague = "土耳其甲组联赛";

	// --- 原有北爱尔兰部分 ---
	String NorthernIrelandPremiership = "北爱尔兰超级联赛";

	// --- 原有葡萄牙部分补充 ---
	String PortugalPrimeiraLiga = "葡萄牙超级联赛";
	String PortugalLiga2 = "葡萄牙甲组联赛";
	String PortugalLiga3 = "葡萄牙丙组联赛";
	String PortugalLigaRevelacaoU23 = "葡萄牙联赛U23";
	// 希腊
	String GreeceSuperLeague1 = "希腊超级联赛甲组";
	String GreeceSuperLeague2 = "希腊超级联赛乙组";

	// 欧洲
	String UefaChampionsLeague = "欧洲冠军联赛";
	String UefaChampionsLeagueSpecials = "欧洲冠军联赛-特别投注";
	String UefaEuropaLeague = "欧洲联赛";
	String UefaConferenceLeague = "欧洲协会联赛";
	String UefaWomenChampionsLeague = "欧洲女子冠军联赛";
	String PremierLeagueInternationalCupInEngland = "超级联赛国际杯(在英格兰)";
	String UefaYouthLeagueU19 = "欧洲青年联赛U19";

	// 国际
	String WorldCup2026EuropeQualifiersPlayOff = "世界杯2026欧洲外围赛-附加赛";
	String AfricaCupOfNations2025InMorocco = "非洲国家杯2025(在摩洛哥)";
	String IntercontinentalCup2025 = "洲际杯2025";
	String ArabCup2025InQatar = "阿拉伯杯2025(在卡塔尔)";

	// --- 原有其他国家补充 ---
	String CroatiaHNLLeague = "克罗地亚足球联赛";
	String EcuadorSerieAPlayOff = "厄瓜多尔甲组联赛-附加赛";
	String EgyptLeagueCup = "埃及联赛杯";
	String BulgariaFirstProfessionalFootballLeague = "保加利亚甲组联赛";
	String RomaniaLiga1 = "罗马尼亚甲组联赛";
	String AustraliaALeagueWomen = "澳大利亚女子甲组联赛";
	String SwitzerlandChallengeLeague = "瑞士甲组联赛";
	String CyprusDivision2 = "塞浦路斯乙组联赛";
	String OmanSuperLeague = "阿曼超级联赛";

	// --- 完全新增国家/地区 ---
	String OmanProfessionalLeague = "阿曼超级联赛";

	String KuwaitPremierLeague = "科威特超级联赛";

	String QatarOlympicLeagueU23 = "卡塔尔奥林匹克联赛U23";

	String AlgeriaLigue1 = "阿尔及利亚甲组联赛";
	String AlgeriaLigue2 = "阿尔及利亚乙组联赛";

	String MaltaPremierLeaguePlayOff = "马耳他超级联赛-附加赛";

	String HungaryLeague1 = "匈牙利甲组联赛";

	String AlbaniaSuperliga = "阿尔巴尼亚超级联赛";

	String BahrainPremierLeague = "巴林超级联赛";

	// KY 平台特有/补充常量
	String IsraelLeague1 = "以色列甲组联赛";
	String UnitedArabEmiratesPremierLeague = "阿联酋超级联赛";
	String SaudiArabiaPremierLeague = "沙特超级联赛";
	String SaudiArabiaLeague1 = "沙特甲组联赛";
	String AlbaniaLeague1 = "阿尔巴尼亚甲组联赛";
	String EnglandSouthernLeagueSouth = "英格兰南部超级联赛(南部)";
	String EnglandSouthernLeagueCentral = "英格兰南部中区超级联赛";

	String JapanWomenSUniversityFootballChampionship = "日本女子大学足球锦标赛";

}