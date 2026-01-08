package com.bojiu.webapp.user.dto;

import java.util.*;

import com.bojiu.webapp.base.entity.TimeBasedEntity;
import com.bojiu.webapp.user.model.BetProvider;
import com.bojiu.webapp.user.model.OddsType;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

@Slf4j
@Setter
@Getter
@NoArgsConstructor
public class GameDTO extends TimeBasedEntity {

	/** 投注厂家 */
	public BetProvider betProvider;
	/** 开赛时间 */
	public Date openTime;
	/** 联赛名称 */
	public String league;
	/** 主队名称 */
	public String teamHome;
	/** 客队名称 */
	public String teamClient;
	/** 联赛名称 */
	public String leagueZh;
	/** 主队中文名 */
	public String teamHomeZh;
	/** 客队中文名 */
	public String teamClientZh;
	/** 赔率信息 */
	public List<OddsInfo> odds;

	/** 拓展数据信息 */
	public transient Object ext;

	public GameDTO(Long id, BetProvider betProvider, Date openTime, String league, String teamHome, String teamClient, List<OddsInfo> odds, Date now) {
		this.id = id;
		this.betProvider = betProvider;
		this.openTime = openTime;
		this.league = league;
		this.teamHome = teamHome;
		this.teamClient = teamClient;
		this.odds = odds;
		this.initTime(now);
	}

	public boolean initZh(Map<String, String> getEnToZhCacheMap) {
		this.leagueZh = getEnToZhCacheMap.get(league);
		this.teamHomeZh = getEnToZhCacheMap.get(teamHome);
		this.teamClientZh = getEnToZhCacheMap.get(teamClient);
		boolean b = this.leagueZh == null || this.teamHomeZh == null || this.teamClientZh == null;
		if(b){
			System.out.println(1111);
		}
		return b;
	}

	/** 赔率信息 */
	@Setter
	@Getter
	@NoArgsConstructor
	public static class OddsInfo {

		/** 盘口类型 */
		public OddsType type;
		/**
		 * <h3>赔率盘口值/交易盘口值（Ratio Rate）</h3>
		 * <p>
		 * 该字段用于表示让球盘（让分盘）或大小盘（总进球数盘）的具体盘口数值。
		 * 盘口值的结算结果取决于比赛结果与盘口值的差异，结果可能包括全赢、全输、赢一半、输一半或走水。
		 * </p>
		 *
		 * <h3>一、让球盘 (Handicap / Runline)：</h3>
		 * <p>
		 * 盘口值R，假设主队让球。计算结果差异 D = (主队得分 - 客队得分)。
		 * </p>
		 * <p>让球方（-）为强队，受让方（+）为弱队</p>
		 * <p>可以同时存在（主队+0/0.5，客队-0/0.5）和（主队-0/0.5，客队+0/0.5）</p>
		 * <h4>让球盘前缀：主队（teamHome）让球方时“-”，否则为“+”</h4>
		 * <ul>
		 * <li><b>整数盘 (e.g., 1.0)</b>：R = 1。
		 * <ul>
		 * <li>若 D > R (D > 1)，投注让球方（主队）<b>【全赢】</b>，投注受让方（客队）全输。</li>
		 * <li>若 D = R (D = 1)，<b>【走水】</b>（双方本金全退）。</li>
		 * <li>若 D < R (D < 1)，投注让球方（主队）<b>【全输】</b>，投注受让方（客队）全赢。</li>
		 * </ul>
		 * </li>
		 * <li><b>半球盘 (e.g., 1.5)</b>：R = 1.5。
		 * <ul>
		 * <li>若 D > R (D > 1.5)，投注让球方<b>【全赢】</b>，投注受让方全输。</li>
		 * <li>若 D < R (D < 1.5)，投注让球方<b>【全输】</b>，投注受让方全赢。</li>
		 * </ul>
		 * </li>
		 * <li><b>四分之一盘：半球/一球（俗称“半一盘”）(e.g., 0.5/1 或 0.75)</b>：盘口分为 R1=0.5 和 R2=1.0。
		 * <ul>
		 * <li>若 D > 1.0，投注让球方<b>【全赢】</b>。</li>
		 * <li>若 D = 1.0，投注让球方 R1盘全赢/R2盘走水，<b>【总计赢一半】</b>。</li>
		 * <li>若 0.5 ≤ D < 1.0，投注让球方 R1盘全赢/R2盘全输，<b>【总计赢一半】</b>。(此结算与 D=1.0 逻辑一致，但实际操作中仅看关键点)</li>
		 * <li>若 D = 0.5，投注让球方 R1盘走水/R2盘全输，**【总计输一半】**。（此点罕见，取决于盘口设计）</li>
		 * <li>若 D < 0.5，投注让球方<b>【全输】</b>。</li>
		 * </ul>
		 * </li>
		 * <li><b>四分之一盘：一球/球半（俗称“球半盘”）(e.g., 1/1.5 或 1.25)</b>：盘口分为 R1=1.0 和 R2=1.5。
		 * <ul>
		 * <li>若 D > 1.5，投注让球方<b>【全赢】</b>。</li>
		 * <li>若 D = 1.5，**【不可能结果】**。</li>
		 * <li>若 D = 1.0，投注让球方 R1盘走水/R2盘全输，**【总计输一半】**。</li>
		 * <li>若 1.0 < D < 1.5，投注让球方 R1盘全赢/R2盘全输，<b>【总计赢一半】</b>。</li>
		 * <li>若 D < 1.0，投注让球方<b>【全输】</b>。</li>
		 * </ul>
		 * </li>
		 * </ul>
		 *
		 * <h3>二、大小盘 (Over/Under / Total Goals)：</h3>
		 * <p>
		 * 盘口值R，T为最终总进球数。以下结算以投注“大球”方为视角。
		 * </p>
		 * <ul>
		 * <li><b>整数盘 (e.g., 2.0)</b>：R = 2。
		 * <ul>
		 * <li>若 T > R (T > 2)，投注大球<b>【全赢】</b>，投注小球全输。</li>
		 * <li>若 T = R (T = 2)，<b>【走水】</b>（双方本金全退）。</li>
		 * <li>若 T < R (T < 2)，投注大球<b>【全输】</b>，投注小球全赢。</li>
		 * </ul>
		 * </li>
		 * <li><b>半球盘 (e.g., 2.5)</b>：R = 2.5。
		 * <ul>
		 * <li>若 T > R (T ≥ 3)，投注大球<b>【全赢】</b>，投注小球全输。</li>
		 * <li>若 T < R (T ≤ 2)，投注大球<b>【全输】</b>，投注小球全赢。</li>
		 * </ul>
		 * </li>
		 * <li><b>四分之一盘（低盘）(e.g., 2/2.5 或 2.25)</b>：盘口分为 R1=2.0 和 R2=2.5。
		 * <ul>
		 * <li>若 T ≥ 3，投注大球<b>【全赢】</b>。</li>
		 * <li>若 T = 2，投注大球 R1盘走水/R2盘全输，**【总计输一半】**。</li>
		 * <li>若 T < 2，投注大球<b>【全输】</b>。</li>
		 * </ul>
		 * </li>
		 * <li><b>四分之一盘（高盘）(e.g., 2.5/3 或 2.75)</b>：盘口分为 R1=2.5 和 R2=3.0。
		 * <ul>
		 * <li>若 T ≥ 4，投注大球<b>【全赢】</b>。</li>
		 * <li>若 T = 3，投注大球 R1盘全赢/R2盘走水，**【总计赢一半】**。</li>
		 * <li>若 T ≤ 2，投注大球<b>【全输】</b>。</li>
		 * </ul>
		 * </li>
		 * </ul>
		 */
		public String ratioRate;
		/** 主队/是/单 赔率（包含本金） */
		public Double hRate;
		/** 客队/否/双 赔率（包含本金） */
		public Double cRate;
		/** 平局赔率（包含本金） */
		public Double nRate;

		/** 赔率列表（包含本金）：[ 主队/是/单, 客队/否/双, 平局赔率 ] */
		public transient Double[] rates;

		/** 获取赔率对子 */
		public Double[] getRates() {
			return rates == null ? (rates = switch (type) {
				case R, HR, OU, HOU, TS, EO -> new Double[] { hRate, cRate };
				case M, HM -> new Double[] { hRate, cRate, nRate };
			}) : rates;
		}

		public OddsInfo(OddsType type, String ratioRate, Double hRate, Double cRate) {
			this(type, ratioRate, hRate, cRate, null);
		}

		public OddsInfo(OddsType type, Double hRate, Double cRate, Double nRate) {
			this(type, null, hRate, cRate, nRate);
		}

		public OddsInfo(OddsType type, Double hRate, Double cRate) {
			this(type, null, hRate, cRate, null);
		}

		public OddsInfo(OddsType type, String ratioRate, Double hRate, Double cRate, Double nRate) {
			this.type = type;
			this.ratioRate = ratioRate;
			this.hRate = hRate;
			this.cRate = cRate;
			this.nRate = nRate;
		}

	}

	transient Long openTimeMs;

	public Long openTimeMs() {
		return openTimeMs == null ? openTimeMs = openTime.getTime() : openTimeMs;
	}

	/** 根据A平台的赔率信息，查找对应B平台的赔率信息 */
	@Nullable
	public OddsInfo findOdds(OddsInfo aOdd) {
		for (OddsInfo odds : odds) {
			if (odds.type == aOdd.type && odds.ratioRate.equals(aOdd.ratioRate)) {
				return odds;
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object o) { // 累计充值金额=阶梯，必须唯一
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		GameDTO tier = (GameDTO) o;
		return tier.eqId(id);
	}

}
