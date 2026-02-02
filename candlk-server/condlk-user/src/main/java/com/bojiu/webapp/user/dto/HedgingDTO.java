package com.bojiu.webapp.user.dto;

import java.util.*;

import com.bojiu.common.redis.RedisUtil;
import com.bojiu.context.web.Jsons;
import com.bojiu.webapp.base.entity.BaseEntity;
import com.bojiu.webapp.user.dto.GameDTO.OddsInfo;
import com.bojiu.webapp.user.model.*;
import com.bojiu.webapp.user.vo.HedgingVO;
import com.google.common.collect.ImmutableMap;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.ArrayUtil;
import me.codeplayer.util.X;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;

import static com.bojiu.webapp.user.dto.HedgingDTO.Odds.*;

/** 预估对冲算法类 */
@Slf4j
@Setter
@Getter
@NoArgsConstructor
public class HedgingDTO extends BaseEntity {

	public BetProvider[] pair;
	/** 串子/串关 */
	public Odds[] parlays;

	public HedgingDTO(Pair<BetProvider, BetProvider> pair, Odds[] parlays, BaseRateConifg baseRateConifg) {
		this.pair = new BetProvider[] { pair.getKey(), pair.getValue() };
		this.parlays = parlays;
		this.baseRate = baseRateConifg;
	}

	@Override
	public Long getId() {
		if (id == null) { // 全局自增ID
			this.id = RedisUtil.opsForValue().increment(UserRedisKey.HEDGING_ID_INCR_KEY, 1);
		}
		return id;
	}

	/** 基础返水配置 */
	public BaseRateConifg baseRate;

	/** 赔率信息 */
	@NoArgsConstructor
	public static class Odds {

		/** A平台 赔率 */
		public double aRate;
		/** B平台 赔率 */
		public double bRate;
		/** A平台 游戏信息 */
		public GameDTO aGame;
		/** B平台 游戏信息 */
		public GameDTO bGame;
		/** A平台 赔率信息 */
		public OddsInfo aOdds;
		/** B平台 赔率信息 */
		public OddsInfo bOdds;
		/** 拓展其他B平台 赔率信息 */
		public List<HedgingVO> extBOdds;
		/**
		 * A平台串子投注方向（对冲平台投注方向取反）
		 *
		 * <p>让球盘：0=主队(Home), 1=客队(Client)
		 * <p>大小盘：0=大球(Over), 1=小球(Under)
		 * <p>独赢盘：0=主胜, 1=客胜, 2=平局
		 * <p>两队都得分：0=否, 1=是
		 * <p>单双盘：0=双, 1=单
		 *
		 * @see GameDTO.OddsInfo#getRates
		 */
		public Integer parlaysIdx;
		/** 缓存对冲投注方向指针 */
		transient Integer bIdx;

		public Integer getBIdx() {
			return bIdx == null ? bIdx = parlaysIdx == 0 ? 1 : 0 : bIdx;
		}

		/** A平台串子 投注方向 */
		public String getBetADirection() {
			return parseBetDirection(true);
		}

		public String getBetBDirection() {
			return parseBetDirection(false);
		}

		private @Nullable String parseBetDirection(boolean isParlays) {
			if (bOdds.type == null) {
				return null;
			}
			// true = A平台投注方向; false = B平台投注方向
			final boolean direction = isParlays ? parlaysIdx == 0 : parlaysIdx == 1;
			return switch (bOdds.type) {
				case R, HR -> direction ? "让主胜" : "让客胜";
				case OU, HOU -> direction ? "大" + bOdds.ratioRate + "球" : "小" + bOdds.ratioRate + "球";
				case M, HM -> direction ? "主胜" : parlaysIdx == 0 ? "客胜" : "平局"; // TODO: 2026/1/15 暂未确定
				case TS -> direction ? "都不得分" : "都得分";
				case EO -> direction ? "双" : "单";
			};
		}

		/** 开赛时间 */
		@Setter
		@Getter
		public Long gameOpenTime;
		/** 是否已锁定赔率（锁定后将不再自动更新赔率） */
		public Boolean lock;
		/** 串子平台赛果定义：0=全赢；1=全输；2=赢半；3=输半；4=走水(平) [建议增加4处理走水] */
		public Integer result;

		public Odds(double aRate, double bRate, Integer parlaysIdx) {
			this.aRate = aRate;
			this.bRate = bRate;
			this.parlaysIdx = parlaysIdx;
		}

		public Odds initGame(GameDTO aGame, GameDTO bGame, OddsInfo aOdds, OddsInfo bOdds) {
			this.aGame = aGame;
			this.bGame = bGame;
			this.aOdds = aOdds;
			this.bOdds = bOdds;
			this.lock = false;
			return this;
		}

		public String outInfo() {
			if (aGame != null) {
				final GameDTO game = this.aGame;
				return "【" + game.league + "】" + game.teamHome + " VS " + game.teamClient
						+ " 【" + aOdds.getType().getLabel() + "】（" + aOdds.ratioRate + "）";

			}
			return null;
		}

		transient Double bWinFactor;

		/** B平台 赢的净收益系数 */
		public double bWinFactor(double bRebate) {
			return bWinFactor == null ? bWinFactor = (bRate - 1) * (1 + bRebate) : bWinFactor;
		}

		public void setNewBRateOdds(OddsInfo newOdds) {
			if (!Objects.equals(bOdds.hRate, newOdds.hRate) || !Objects.equals(bOdds.cRate, newOdds.cRate)) {
				log.info("更新赔率【{}】{} VS {} 盘口={}（{}） hRate：{}->{} cRate：{}->{} ", aGame.league, aGame.teamHome, aGame.teamClient, newOdds.getType()
								.getLabel(), newOdds.ratioRate
						, bOdds.hRate, newOdds.hRate, bOdds.cRate, newOdds.cRate);
				this.bOdds.cRate = newOdds.cRate;
				this.bOdds.hRate = newOdds.hRate;
				final Double[] rates = this.bOdds.getRates();
				this.bRate = parlaysIdx == 1/*对冲B平台与串子A平台相反*/ ? rates[0] : rates[1];
			}
		}

		/** 全赢 */
		public static final int ALL_WIN = 0,
		/** 全输 */
		ALL_LOSE = 1,
		/** 赢半 */
		WIN_HALF = 2,
		/** 输半 */
		LOSE_HALF = 3,
		/** 走水/平局 */
		DRAW = 4;

		/**
		 * 结算赛果（计算在A平台投注的输赢结果）
		 *
		 * @param scoreResult 赛果数据
		 * @param isAResult scoreResult是否来自A平台（用于决定读取aOdds还是bOdds的盘口值）
		 */
		public boolean settle(ScoreResult scoreResult, boolean isAResult) {
			// 根据来源获取对应的赔率盘口信息
			final OddsInfo oddsInfo = isAResult ? aOdds : bOdds;
			final OddsType type = oddsInfo.getType();
			// 获取对应时段进球数
			final Integer[] currentScore = (type.type == PeriodType.FULL) ? scoreResult.getScore() : scoreResult.getScoreH();
			if (currentScore == null) { // 可能在没有赛果
				return false;
			}
			final int homeGoal = currentScore[0], clientGoal = currentScore[1];

			// 核心逻辑：所有计算最终需转化为“A平台投注方向”的胜平负
			return switch (type) {
				case R, HR -> {
					// 解析原始盘口（例如 -1.25 代表主让，1.25 代表客让）
					final double r = convertRatioRate(oddsInfo.getRatioRate());

					// 计算主队的赢盘表现分数
					final double homePerformance = (double) homeGoal + r - clientGoal;
					// 计算主队的赛果
					final int homeResult = calcHandicapResult(homePerformance);

					// 根据 A 平台投注方向输出结果
					this.result = parlaysIdx == 0/*投主队*/ ? homeResult : reversedResult(homeResult);
					yield true;
				}
				case OU, HOU -> {
					final double r = convertRatioRate(oddsInfo.getRatioRate());
					final double total = (double) homeGoal + clientGoal;

					// 大小盘：parlaysIdx 0=大球(Over), 1=小球(Under)
					final boolean isOver = (parlaysIdx == 0);
					this.result = calculateOverUnderResult(total, r, isOver);
					yield true;
				}
				case M, HM -> {
					// 独赢盘：0=主胜, 1=客胜, 2=平
					if (homeGoal > clientGoal) {
						this.result = (parlaysIdx == 0) ? ALL_WIN : ALL_LOSE;
					} else if (homeGoal < clientGoal) {
						this.result = (parlaysIdx == 1) ? ALL_WIN : ALL_LOSE;
					} else {
						// 如果A平台投的是平局(2)，则全赢，否则全输（独赢盘通常没有走水，除非特定规则）
						this.result = parlaysIdx == 2 ? ALL_WIN : ALL_LOSE;
					}
					yield true;
				}
				default -> false;
			};
		}

		/**
		 * 将 交易盘口值 转换为 等效盘口值
		 * <p>e.g., "0.5/1" -> 0.75, "-1/1.5" -> -1.25, "0.75" -> 0.75
		 */
		private double convertRatioRate(String ratioRate) {
			if (ratioRate == null || ratioRate.isEmpty() || "0".equals(ratioRate)) {
				return 0.0;
			}

			// 处理复合盘口 (0/0.5, 0.5/1, 1/1.5)
			final int pos = ratioRate.indexOf("/");
			if (pos > 0) {
				// 整体正负号 e.g：-1/1.5 => 一半投让 -1 球，一半投让 -1.5 球，实际让球值为 (1 + 1.5) / 2 = 1.25
				final double sign = ratioRate.startsWith("-") ? -1.0 : 1.0;
				final double r1 = Math.abs(Double.parseDouble(ratioRate.substring(0, pos)));
				final double r2 = Math.abs(Double.parseDouble(ratioRate.substring(pos + 1)));
				return sign * ((r1 + r2) / 2.0);
			}

			// 处理单盘口 (1, -1, 0.5)
			return Double.parseDouble(ratioRate);
		}

		/**
		 * 计算让球盘结果
		 * 0.25: 赢一半 (e.g. 0 + 0.25)
		 * 0.5: 全赢
		 */
		private int calcHandicapResult(double performance) {
			if (performance >= 0.5) {
				return ALL_WIN;
			} else if (performance == 0.25) {
				return WIN_HALF;
			} else if (performance == 0) {
				return DRAW;
			} else if (performance == -0.25) {
				return LOSE_HALF;
			}
			return ALL_LOSE;
		}

		/** 计算大小盘结果 */
		private int calculateOverUnderResult(double total, double ratio, boolean isOver) {
			final double score = total - ratio;
			int res;
			if (score >= 0.5) {
				res = ALL_WIN;
			} else if (score == 0.25) {
				res = WIN_HALF;
			} else if (score == 0) {
				res = DRAW;
			} else if (score == -0.25) {
				res = LOSE_HALF;
			} else {
				res = ALL_LOSE;
			}

			// 如果投注方向是小球，结果完全对调
			return isOver ? res : reversedResult(res);
		}

		/** 获取对冲平台的投注结果 */
		public Integer getBResult() {
			return result == null ? null : reversedResult(result);
		}

		public Integer reversedResult(Integer result) {
			return switch (result) {
				case ALL_WIN -> ALL_LOSE;
				case ALL_LOSE -> ALL_WIN;
				case WIN_HALF -> LOSE_HALF;
				case LOSE_HALF -> WIN_HALF;
				case DRAW -> DRAW;
				default -> throw new IllegalArgumentException("Invalid result: " + result);
			};
		}

		public String getResult_() {
			return ALL_RESULT.get(result);
		}

		public static final Map<Integer, String> ALL_RESULT = ImmutableMap.of(
				ALL_WIN, "全赢",
				ALL_LOSE, "全输",
				WIN_HALF, "赢半",
				LOSE_HALF, "输半",
				DRAW, "走水"
		);

		public String getBResult_() {
			return ALL_RESULT.get(getBResult());
		}

		/** 计算A平台的赛果赔率 */
		public double calcAResultRate() {
			return switch (result == null ? ALL_WIN : result) { // 默认当做全赢处理
				case ALL_WIN -> aRate;
				case DRAW -> 1;
				case WIN_HALF -> (1 + aRate) / 2;
				case LOSE_HALF -> 0.5;
				case ALL_LOSE -> 0;
				default -> throw new IllegalArgumentException("Invalid result: " + result);
			};
		}

		/** 计算A平台的赛果赔率 */
		public double calcBResultRate(Double bRebate) {
			return switch (result == null ? ALL_WIN : result) { // 默认当做全赢处理
				case ALL_WIN -> bRebate - 1;
				case DRAW -> 0; // 走水
				case WIN_HALF -> (bRebate - 1) / 2; // 赢半
				case LOSE_HALF -> 0.5 * (bRate - 1) * (1 + bRebate); // 输半
				case ALL_LOSE -> (bRate - 1) * (1 + bRebate); // 全输
				default -> throw new IllegalArgumentException("Invalid result: " + result);
			};
		}

	}

	transient Double bLossFactor;

	/** B平台 输的净收益系数 */
	public double bLossFactor() {
		return bLossFactor == null ? bLossFactor = getBRebate() - 1 : bLossFactor;
	}

	transient Double overallOdds;

	/** 综合赔率 */
	public double overallOdds() {
		if (overallOdds == null) {
			double odds = parlays[0].aRate;
			for (int i = 1, size = parlays.length; i < size; i++) {
				odds *= parlays[i].aRate;
			}
			overallOdds = odds;
		}
		return overallOdds;
	}

	transient Double aInCoin;

	/** A平台 串子投注金额 */
	public double aInCoin() {
		return aInCoin == null ? aInCoin = baseRate.aPrincipal * (1 + baseRate.aRechargeRate) : aInCoin;
	}

	public transient Double aRebate;

	public double getARebate() {
		return aRebate == null ? aRebate = baseRate.rebate.get(pair[0]) : aRebate;
	}

	/** 是否需要重新计算投注 */
	public transient Boolean reset;

	public transient Double bRebate;

	public double getBRebate() {
		return bRebate == null ? bRebate = baseRate.rebate.get(pair[1]) : bRebate;
	}

	transient Double aRebateCoin;

	/** A平台 返水金额（串子全输时） */
	public double aRebateCoin() {
		return aRebateCoin == null ? aRebateCoin = aInCoin() * getARebate() : aRebateCoin;
	}

	transient Double loss;

	/** A平台 串关全输时的固定收益 */
	public double getLoss() {
		return loss == null ? loss = aRebateCoin() - baseRate.aPrincipal : loss;
	}

	transient Double win;

	/** A平台 串关全赢时的固定收益（按输赢金额计算返水） */
	public double getWin() {
		final double odds = overallOdds();
		return win == null ? win = (aInCoin() * odds) + (aRebateCoin() * Math.abs(odds - 1)) - baseRate.aPrincipal : win;
	}

	public double[] hedgingCoins;

	/** 根据当前的赔率估算在 B平台 对冲的每场投注金额 */
	public double[] getHedgingCoins() {
		if (hedgingCoins == null) {
			final double bRebate = getBRebate();
			final int size = parlays.length;
			hedgingCoins = new double[size];
			int lastIdx = size - 1;
			double bLossFactor = bLossFactor();
			// 计算最后一场的对冲金额
			hedgingCoins[lastIdx] = (getWin() - getLoss()) / (parlays[lastIdx].bWinFactor(bRebate) - bLossFactor);
			// 往前推算每场需要对冲的金额
			for (int i = lastIdx - 1; i >= 0; i--) {
				hedgingCoins[i] = (parlays[i + 1].bWinFactor(bRebate) * hedgingCoins[i + 1]) / (parlays[i].bWinFactor(bRebate) - bLossFactor);
			}
		}
		return hedgingCoins;
	}

	public void calcHedgingCoinsLock(Date now) {
		try {
			calcHedgingCoinsLock(now, null);
		} catch (Exception e) {
			log.error("计算对冲金额时出错：", e);
		}
	}

	/** 计算剩余场次的对冲金额（多阶段成本回收模型） */
	public void calcHedgingCoinsLock(Date now, Boolean threeEnd) {
		if (hedgingCoins != null) {
			// 全部锁住时不更新对冲金额
			if (ArrayUtil.matchAny(p -> !p.lock, parlays)) {
				if (parlays.length == 2) { // 二串一
					calcParlay2(now);
				} else { // 三串一
					calcParlay3(now, threeEnd);
				}
			}
		}
	}

	/** 计算二串一 */
	private void calcParlay2(Date now) {
		final long timeNow = now.getTime();
		final double bRebate = getBRebate();
		if (X.isValid(reset)) { // 需要重新计算此前的全部赛事
			reset();
			// 计算第一场
			calcFirstGame2(timeNow, bRebate);
			// 计算第二场
			if (timeNow > parlays[0].gameOpenTime) {
				calcTwoGame2(bRebate);
			}
		} else {
			final boolean firstEnd = parlays[0].lock || timeNow > parlays[0].gameOpenTime, // 第一场是否结束
					twoEnd = parlays[1].lock || timeNow > parlays[1].gameOpenTime; // 第二场是否结束
			// 第一场比赛未开始 || 第二场比赛未开始：计算第二场下注，若存在第三场则继续推导第三场下注额
			if (!firstEnd || !twoEnd) {
				calcFirstGame2(timeNow, bRebate);
			}
			// 第二场比赛开始（赔率不再发生变化）
			else {
				calcTwoGame2(bRebate);
			}
		}
	}

	/** 计算三串一 */
	private void calcParlay3(Date now, Boolean threeEnd) {
		final long timeNow = now.getTime();
		final double bRebate = getBRebate();
		if (X.isValid(reset)) { // 需要重新计算此前的全部赛事
			reset();
			// 计算第一场
			calcFirstGame(timeNow, bRebate);
			// 计算第二场
			if (timeNow > parlays[0].gameOpenTime) {
				calcTwoGame(bRebate);
			}
			// 计算第三场
			if (timeNow > parlays[1].gameOpenTime) {
				calcThreeGame(bRebate);
			}
		} else {
			final boolean firstEnd = parlays[0].lock || timeNow > parlays[0].gameOpenTime, // 第一场是否结束
					twoEnd = parlays[1].lock || timeNow > parlays[1].gameOpenTime; // 第二场是否结束
			if (threeEnd == null) {
				threeEnd = (parlays[2].lock || timeNow > parlays[2].gameOpenTime); // 第三场是否结束
			}
			// 第一场比赛未开始 || 第二场比赛未开始：计算第二场下注，若存在第三场则继续推导第三场下注额
			if (!firstEnd || !twoEnd) {
				calcFirstGame(timeNow, bRebate);
			}
			// 第三场比赛开始 || 手动模拟第三场赛果
			else if (threeEnd) {
				calcThreeGame(bRebate);
			}
			// 第二场比赛开始（赔率不再发生变化）
			else {
				calcTwoGame(bRebate);
			}
		}
	}

	/** 二串一串关  估算第一局收益情况和第二场所需投注 */
	private void calcFirstGame2(long timeNow, double bRebate) {
		// 若第一场还未开始，需要刷新拓展的对冲投注金额
		if (reset != null && timeNow < parlays[0].gameOpenTime) {
			reset();
		}
		// 计算A平台串子第一场赛果赔率（第一场赛果系数）
		final double firstRate = parlays[0].calcAResultRate();
		// 计算当前的净输赢，A平台串子“后两场全赢”时净结果 W_A_当前（按输赢金额算返水）：
		//      (串子投注 * 第一场赛果赔率 * 串子第二场赔率 * 串子第三场赔率) + A平台返水金额 * abs（第一场赛果赔率 * 第二场赔率 - 1）- 本金
		final double ratePct = firstRate * parlays[1].aRate;
		final double aWinLoss = aInCoin() * ratePct + aRebateCoin() * Math.abs(ratePct - 1) - baseRate.aPrincipal;
		final double winLossDiff = aWinLoss - getLoss();
		// 第二场投注额： (净输赢 - 串子输光损失) / 对冲第二场赔率
		hedgingCoins[1] = winLossDiff / parlays[1].bRate;

		// 第一场B平台已实现净盈亏：(B对冲第一场投注 * B平台赛果赔率)
		final double bWin = hedgingCoins[0] * parlays[0].calcBResultRate(bRebate);
		// 计算可能的情况
		factor1 = new Double[4];
		// 当前第二场净赢系数：（B第二场赔率 -1）*（1+B返水）
		factor1[0] = (parlays[1].bRate - 1) * (1 + bRebate);
		// 当前输一注净亏系数：-1+B返水
		factor1[1] = -1 + bRebate;
		// 第一场B平台已实现净盈亏
		factor1[2] = bWin;
		// A平台串子“后两场全赢”时净结果
		factor1[3] = aWinLoss;

		// 在当前赔率和下注计划下，后三种可能路径的总盈亏
		oneOuts = new ArrayList<>(2);
		final double sumWin = getLoss() + bWin;
		// 串子第二场输：输光净结果 + 第一场B平台已实现净盈亏 + B平台第二场投注 * 当前第二场净赢系数
		oneOuts.add(new Out("第二场挂", sumWin + (hedgingCoins[1] * factor1[0])));
		// 串子全赢：串子当前净输赢 + 第一场B平台已实现净盈亏 + 当前输一注净亏系数 *（B平台第二场投注 + B平台第三场投注）
		final double out2 = aWinLoss + bWin + (hedgingCoins[1] * factor1[1]);
		oneOuts.add(new Out("串子全赢", out2));
		log.info("推演一期对冲方案：ID={} 第二场投注={} 串子全赢={}", getId(), hedgingCoins[1], out2);
	}

	/** 二串一结束后的最终收益情况 */
	private void calcTwoGame2(double bRebate) {

		if (factor1 != null) { // 避免已过期赛事导致的影响
			factor3 = new Double[6];

			// 第二场A结果系数
			factor3[0] = parlays[1].calcAResultRate();

			// 第二场B结果系数
			factor3[1] = switch (parlays[1].result == null ? ALL_WIN : parlays[1].result) {
				case ALL_WIN -> 0.0;
				case DRAW -> 1.0;
				case WIN_HALF -> 0.5;
				case LOSE_HALF -> (1 + parlays[1].bRate) / 2;
				case ALL_LOSE -> parlays[1].bRate;
				default -> throw new IllegalArgumentException("Invalid result: " + parlays[1].result);
			};

			// 第二场B平台最终盈亏
			final double bSecondWinLoss = hedgingCoins[1] * parlays[1].calcBResultRate(bRebate);

			// 第一场A结果系数
			final double firstRate = parlays[0].calcAResultRate();

			// A平台最终输光净结果
			final double aWinLoss = aInCoin() * firstRate * factor3[0]
					+ aRebateCoin() * Math.abs(firstRate * factor3[0] - 1)
					- baseRate.aPrincipal;

			// 第一场B平台已实现净盈亏
			final Double bWin = factor1[2];
			// B平台最终净结果（第一场 + 第二场）
			final double bWinLoss = bWin + bSecondWinLoss;

			factor3[2] = bSecondWinLoss;
			factor3[3] = aWinLoss;
			factor3[4] = bWinLoss;

			// 总最终盈亏
			factor3[5] = aWinLoss + bWinLoss;

			// 在当前赔率和下注计划下，可能路径的总盈亏
			twoOuts = new ArrayList<>(2);
			final double lossBase = getLoss() + bWin;

			// 第二场挂
			twoOuts.add(new Out("第二场挂", lossBase + hedgingCoins[1] * factor1[0]));

			// A平台串子“全赢”时净结果
			final Double aWin = factor1[3];
			// 串子全赢
			twoOuts.add(new Out("串子全赢", aWin
					+ bWin/*B第一场投注成本（负数）*/
					+ (hedgingCoins[1] * factor1[1])/*B平台第二场投注成本（负数）*/));

			log.info("推演二串一最终方案！ID={}，最终总盈亏={}", getId(), factor3[5]);
		}
	}

	/** 三串一串关  估算第一局收益情况和第二场所需投注 */
	private void calcFirstGame(long timeNow, double bRebate) {
		// 若第一场还未开始，需要刷新拓展的对冲投注金额
		if (reset != null && timeNow < parlays[0].gameOpenTime) {
			reset();
		}
		// 计算A平台串子第一场赛果赔率（第一场赛果系数）
		final double firstRate = parlays[0].calcAResultRate();
		// 计算当前的净输赢，A平台串子“后两场全赢”时净结果 W_A_当前（按输赢金额算返水）：
		//      (串子投注 * 第一场赛果赔率 * 串子第二场赔率 * 串子第三场赔率) + A平台返水金额 * abs（第一场赛果赔率 * 第二场赔率 - 1）- 本金
		final double ratePct = firstRate * parlays[1].aRate * parlays[2].aRate;
		final double aWinLoss = aInCoin() * ratePct + aRebateCoin() * Math.abs(ratePct - 1) - baseRate.aPrincipal;
		final double winLossDiff = aWinLoss - getLoss();
		// 第二场投注额：(对冲第三场赔率 - 1 + B平台返水) * (净输赢 - 串子输光损失) / (对冲第二场赔率 * 对冲第三场赔率)
		hedgingCoins[1] = (parlays[2].bRate - 1 + bRebate) * winLossDiff / (parlays[1].bRate * parlays[2].bRate);

		// 第三场投注额：(净输赢 - 串子输光损失) / 对冲第三场赔率
		hedgingCoins[2] = winLossDiff / parlays[2].bRate;

		// 第一场B平台已实现净盈亏：(B对冲第一场投注 * B平台赛果赔率)
		final double bWin = hedgingCoins[0] * parlays[0].calcBResultRate(bRebate);
		// 计算可能的情况
		factor1 = new Double[5];
		// 当前第二场净赢系数：（B第二场赔率 -1）*（1+B返水）
		factor1[0] = (parlays[1].bRate - 1) * (1 + bRebate);
		// 当前第三场净赢系数：（B第三场赔率 -1）*（1+B返水）
		factor1[1] = (parlays[2].bRate - 1) * (1 + bRebate);
		// 当前输一注净亏系数：-1+B返水
		factor1[2] = -1 + bRebate;
		// 第一场B平台已实现净盈亏
		factor1[3] = bWin;
		// A平台串子“后两场全赢”时净结果
		factor1[4] = aWinLoss;

		// 在当前赔率和下注计划下，后三种可能路径的总盈亏
		oneOuts = new ArrayList<>(3);
		final double sumWin = getLoss() + bWin;
		// 串子第二场输：输光净结果 + 第一场B平台已实现净盈亏 + B平台第二场投注 * 当前第二场净赢系数
		oneOuts.add(new Out("第二场挂", sumWin + (hedgingCoins[1] * factor1[0])));
		// 串子第三场输：输光净结果 + 第一场B平台已实现净盈亏 + B平台第二场投注*当前输一注净亏系数 + B平台第三“场投注*当前第三场净赢系数
		final double out = sumWin + (hedgingCoins[1] * factor1[2]) + (hedgingCoins[2] * factor1[1]);
		oneOuts.add(new Out("第三场挂", out));
		// 串子全赢：串子当前净输赢 + 第一场B平台已实现净盈亏 + 当前输一注净亏系数 *（B平台第二场投注 + B平台第三场投注）
		final double out2 = aWinLoss + bWin + (factor1[2] * (hedgingCoins[1] + hedgingCoins[2]));
		oneOuts.add(new Out("串子全赢", out2));
		log.info("推演一期对冲方案：ID={} 第二场投注={} 第三场投注={} 串子第三场输={} 串子全赢={}", getId(), hedgingCoins[1], hedgingCoins[2], out, out2);
	}

	/** 估算第二局收益情况 */
	private void calcTwoGame(double bRebate) {
		// 计算A平台串子前两场赛果赔率（第一场系数和第二场系数）
		final double firstRate = parlays[0].calcAResultRate(), twoRate = parlays[1].calcAResultRate();
		// A平台串子全红净结果：串子投注 * 第一场系数 * 第二场系数 * A串子第三场赔率 + A平台返水金额 * abs（第一场系数 * 第二场系数 * A平台第三场赔率 - 1）- 本金
		final double aWinLoss = aInCoin() * firstRate * twoRate * parlays[2].aRate + aRebateCoin()
				* Math.abs(firstRate * twoRate * parlays[2].aRate - 1) - baseRate.aPrincipal;
		// 第三场投注额：(净输赢 - 串子输光损失) / 对冲第三场赔率
		hedgingCoins[2] = (aWinLoss - getLoss()) / parlays[2].bRate;

		// 在当前赔率和下注计划下，两种可能路径的总盈亏
		twoOuts = new ArrayList<>(2);

		// 前两场B平台已实现净盈亏
		double firstBWin = factor1[3], twoBWin = hedgingCoins[1] * parlays[1].calcBResultRate(bRebate);
		factor2 = new Double[6];
		// 当前第三场净赢系数：（B第三场赔率 -1）*（1+B返水）
		factor2[0] = (parlays[2].bRate - 1) * (1 + bRebate);
		// 当前输一注净亏系数：-1+B返水
		factor2[1] = -1 + bRebate;
		// A平台串子全红净结果
		factor2[2] = aWinLoss;
		// 第二场B平台已实现净盈亏
		factor2[3] = twoBWin;
		// 第一场系数
		factor2[4] = firstRate;
		// 第二场系数
		factor2[5] = twoRate;
		// 串子第三场输：输光净结果 + 第一场B平台已实现净盈亏 + 第二场B平台已实现净盈亏 + B平台第三“场投注*当前第三场净赢系数
		final double out = getLoss() + firstBWin + twoBWin + (hedgingCoins[2] * factor2[0]);
		twoOuts.add(new Out("第三场挂", out));
		// 串子全赢：串子当前净输赢 + 第一场B平台已实现净盈亏 + B平台第三场投注 * 当前输一注净亏系数
		twoOuts.add(new Out("串子全赢", aWinLoss + firstBWin + twoBWin + (hedgingCoins[2] * factor2[1])));
		log.info("推演二期对冲方案：ID={} 第二场投注={} 第三场投注={} 串子第三场输={}", getId(), hedgingCoins[1], hedgingCoins[2], out);
	}

	/** 估算第三局收益情况 */
	private void calcThreeGame(double bRebate) {
		if (factor2 != null) { // 避免已过期赛事导致的影响
			factor3 = new Double[6];
			// 第三场A结果系数
			factor3[0] = parlays[2].calcAResultRate();
			// 第三场B结果系数
			factor3[1] = switch (parlays[2].result == null ? ALL_WIN : parlays[2].result) { // 默认当做全赢处理
				case ALL_WIN -> 0.0;
				case DRAW -> 1.0;
				case WIN_HALF -> 0.5;
				case LOSE_HALF -> (1 + parlays[2].bRate) / 2;
				case ALL_LOSE -> parlays[2].bRate;
				default -> throw new IllegalArgumentException("Invalid result: " + parlays[2].result);
			};
			// 第三场B平台最终净盈亏
			final double bThirdWinLoss = hedgingCoins[2] * parlays[2].calcBResultRate(bRebate);
			// 计算A平台串子前两场赛果赔率（第一场系数和第二场系数）
			final double firstRate = parlays[0].calcAResultRate(), twoRate = parlays[1].calcAResultRate();
			// A平台最终净结果 P_A_最终（按输赢金额算返水）
			final double aWinLoss = aInCoin() * firstRate * twoRate * factor3[0] + aRebateCoin()
					* Math.abs(firstRate * twoRate * factor3[0] - 1) - baseRate.aPrincipal;
			// B平台最终净结果 P_B_最终
			final double bWinLoss = factor1[3] + factor2[3] + bThirdWinLoss;
			factor3[2] = bThirdWinLoss;
			factor3[3] = aWinLoss;
			factor3[4] = bWinLoss;
			// 总最终盈亏
			factor3[5] = aWinLoss + bWinLoss;
			log.info("推演三期对冲方案！ID={}，最终总盈亏={}", getId(), factor3[5]);
		}
	}

	/** 第一场结束时的计算系数 */
	public Double[] factor1;
	/** 第二场结束时的计算系数 */
	public Double[] factor2;
	/** 第三场结束时的计算系数 */
	public Double[] factor3;

	public static class Out {

		public String resultDescription;
		public double out;

		public Out(String desc, double out) {
			this.resultDescription = desc;
			this.out = out;
		}

	}

	/** 第一场结束时的输出 */
	public List<Out> oneOuts;
	/** 第二场结束时的输出 */
	public List<Out> twoOuts;

	@Getter
	transient List<Out> outs;

	/** 计算三串一利润 */
	public List<Out> calcProfit(double[] bHedgingCoins) {
		if (outs == null) {
			final int N = parlays.length;
			final List<Out> result = new ArrayList<>(N + 1);
			// 初始成本和产出
			double loss_ = getLoss(), bLossFactor_ = bLossFactor(), sumBInCoin = 0;
			// 计算 A平台 串子输的情况
			for (int i = 0; i < N; i++) {
				double bHedgingCoin = bHedgingCoins[i];

				result.add(new Out("串子第" + (i + 1) + "场输", loss_ + (bLossFactor_ * sumBInCoin)
						+ (bHedgingCoin * parlays[i].bWinFactor(getBRebate()))));
				sumBInCoin += bHedgingCoin;
			}
			// 计算 A平台 串子赢的情况
			result.add(new Out("串子全中", getWin() + (bLossFactor_ * sumBInCoin)));
			outs = result;
		}
		return outs;
	}

	/** 平均利润 */
	@Getter
	public double avgProfit;

	/** 计算平均利润并缓存（静态锁利模型） */
	public double calcAvgProfitAndCache(double[] bHedgingCoins) {
		return avgProfit = calcAvgProfit(bHedgingCoins);
	}

	public double calcAvgProfit(double[] bHedgingCoins) {
		// 初始成本和产出
		return getLoss() + (bHedgingCoins[0] * parlays[0].bWinFactor(getBRebate()));
	}

	/** 根据当前返水配置刷新方案 */
	public HedgingDTO flush(BaseRateConifg baseRate) {
		return flush(baseRate, null);
	}

	public HedgingDTO flush(@Nullable BaseRateConifg baseRate, double[] hedgingCoins) {
		this.aInCoin = null;
		this.aRebateCoin = null;
		this.bRebate = null;
		this.aRebate = null;
		if (baseRate != null) {
			this.baseRate = baseRate;
		}
		this.loss = null;
		this.win = null;
		this.hedgingCoins = null;
		calcAvgProfitAndCache(hedgingCoins == null ? getHedgingCoins() : hedgingCoins);
		return this;
	}

	public transient String json;

	public void toJson() {
		this.getId(); // 初始化ID
		int len = parlays.length;
		List<OddsInfo>[] odds = new ArrayList[len * 2];
		for (int i = 0, j = 0; i < len; i++, j += 2) {  // 移除冗余的赔率信息
			final Odds parlay = parlays[i];
			odds[j] = parlay.aGame.odds;
			odds[j + 1] = parlay.bGame.odds;
			parlay.aGame.odds = null;
			parlay.bGame.odds = null;
		}
		json = Jsons.encode(this);

		// odds 引用传递，避免后续业务逻辑NPE
		for (int i = 0, j = 0; i < len; i++, j += 2) {
			final Odds parlay = parlays[i];
			parlay.aGame.odds = odds[j];
			parlay.bGame.odds = odds[j + 1];
		}
	}

	public void reset() {
		this.loss = null;
		this.win = null;
		this.bRebate = null;
		this.hedgingCoins = null;
		// 重新计算串子投注
		calcAvgProfitAndCache(getHedgingCoins());
		reset = null;
	}

}