package com.bojiu.webapp.user.dto;

import java.util.*;

import com.bojiu.common.redis.RedisUtil;
import com.bojiu.context.web.Jsons;
import com.bojiu.webapp.base.entity.BaseEntity;
import com.bojiu.webapp.user.dto.GameDTO.OddsInfo;
import com.bojiu.webapp.user.model.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

/** 预估对冲算法类 */
@Slf4j
@Setter
@Getter
@NoArgsConstructor
public class HedgingDTO extends BaseEntity {

	/** 串子/串关 */
	public Odds[] parlays;

	public HedgingDTO(Odds[] parlays, BaseRateConifg baseRateConifg) {
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
		/** A平台 盘口指针 */
		public Integer oddsIdx;
		/** A平台 赔率指针 */
		public Integer aIdx;
		/** A平台 赔率指针 */
		public Integer bIdx;
		/** 开赛时间 */
		@Setter
		@Getter
		public Long gameOpenTime;

		/** 串子平台赛果定义：0=全赢；1=全输；2=赢半；3=输半；4=走水(平) [建议增加4处理走水] */
		public Integer result;
		/** 全赢 */
		// static final int ALL_WIN = 0,;

		/**
		 * 结算赛果
		 *
		 * @param scoreResult 赛果
		 * @param isAResult 是否为A平台的赛果
		 */
		public boolean settle(ScoreResult scoreResult, boolean isAResult) {
			final OddsInfo oddsInfo = isAResult? aGame.getOdds().get(aIdx):aGame.getOdds().get(aIdx);
			final OddsType type = oddsInfo.getType();
			// 1. 获取对应进球数（全场或上半场）
			Integer[] currentScore = (type.type == PeriodType.FULL) ? scoreResult.getScore() : scoreResult.getScoreH();

			if (currentScore == null || currentScore.length < 2) {
				return false;
			}

			int homeGoal = currentScore[0];
			int clientGoal = currentScore[1];

			switch (type) {
				case R, HR -> {
					// 解析让球值，例如 "-0.5/1" -> -0.75
					final double r = parseRatio(oddsInfo.getRatioRate());
					// 计算净胜球 D = 主队 - 客队
					final double diff = (double) homeGoal - clientGoal;

					// 如果是投注客队，需要反转差异值（相当于客队-主队）
					// 假设 isAResult 代表当前处理的是“让球方/左侧球队”
					double finalDiff = isAResult ? diff : -diff;
					// 注意：ratioRate 的正负号通常是以主队为基准的，如果投注客队，r也要取反
					double finalRatio = isAResult ? r : -r;

					this.result = calculateHandicapResult(finalDiff, finalRatio);
					return true;
				}
				case OU, HOU -> {
					double r = parseRatio(oddsInfo.getRatioRate());
					double total = (double) homeGoal + clientGoal;

					// isAResult: true为大球(Over)，false为小球(Under)
					this.result = calculateOverUnderResult(total, r, isAResult);
					return true;
				}
				case M, HM -> {
					// 独赢盘逻辑
					if (homeGoal > clientGoal) {
						this.result = isAResult ? 0 : 1;
					} else if (homeGoal < clientGoal) {
						this.result = isAResult ? 1 : 0;
					} else {
						this.result = 4; // 走水/平
					}
					return true;
				}
				default -> {
					return false;
				}
			}
		}

		/**
		 * 将多种格式的 ratioRate 转换为 double
		 * e.g., "0.5/1" -> 0.75, "-1/1.5" -> -1.25, "0.75" -> 0.75
		 */
		private double parseRatio(String ratioRate) {
			if (ratioRate == null || ratioRate.isEmpty()) {
				return 0;
			}

			// 处理带 "/" 的情况，如 "0.5/1"
			if (ratioRate.contains("/")) {
				String[] parts = ratioRate.split("/");
				double r1 = Double.parseDouble(parts[0]);
				double r2 = Double.parseDouble(parts[1]);
				return (r1 + r2) / 2.0;
			}
			// 处理直接数值情况，如 "0.75" 或 "1.5"
			return Double.parseDouble(ratioRate);
		}

		/** 让球盘判定 */
		private int calculateHandicapResult(double diff, double ratio) {
			double score = diff - ratio; // 净胜球减去让球数

			if (score >= 0.5) {
				return 0;    // 全赢
			}
			if (score == 0.25) {
				return 2;   // 赢半
			}
			if (score == 0) {
				return 4;      // 走水
			}
			if (score == -0.25) {
				return 3;  // 输半
			}
			if (score <= -0.5) {
				return 1;   // 全输
			}
			return 1;
		}

		/** 大小盘判定 */
		private int calculateOverUnderResult(double total, double ratio, boolean isOver) {
			double score = total - ratio;
			int res;

			if (score >= 0.5) {
				res = 0;      // 全赢
			} else if (score == 0.25) {
				res = 2; // 赢半
			} else if (score == 0) {
				res = 4;    // 走水
			} else if (score == -0.25) {
				res = 3;// 输半
			} else {
				res = 1;                   // 全输
			}

			// 如果投的是小球(Under)，结果完全反转
			if (!isOver) {
				if (res == 0) {
					res = 1;
				} else if (res == 1) {
					res = 0;
				} else if (res == 2) {
					res = 3;
				} else if (res == 3) {
					res = 2;
				}
			}
			return res;
		}

		/** 是否已锁定赔率（锁定后将不再自动更新赔率） */
		public Boolean lock;

		public Odds(double aRate, double bRate) {
			this.aRate = aRate;
			this.bRate = bRate;
		}

		public Odds initGame(GameDTO aGame, GameDTO bGame, Integer oddsIdx, Integer aIdx, Integer bIdx) {
			this.aGame = aGame;
			this.bGame = bGame;
			this.oddsIdx = oddsIdx;
			this.aIdx = aIdx;
			this.bIdx = bIdx;
			this.lock = false;
			return this;
		}

		public String outInfo() {
			if (aGame != null) {
				final GameDTO game = this.aGame;
				OddsInfo oddsInfo = game.odds.get(this.oddsIdx);
				return "【" + game.league + "】" + game.teamHome + " VS " + game.teamClient
						+ " 【" + oddsInfo.getType().getLabel() + "】（" + oddsInfo.ratioRate + "）";

			}
			return null;
		}

		transient Double bWinFactor;

		/** B平台 赢的净收益系数 */
		public double bWinFactor(double bRebate) {
			return bWinFactor == null ? bWinFactor = (bRate - 1) * (1 + bRebate) : bWinFactor;
		}

	}

	transient Double bLossFactor;

	/** B平台 输的净收益系数 */
	public double bLossFactor() {
		return bLossFactor == null ? bLossFactor = baseRate.bRebate - 1 : bLossFactor;
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
	public double getAInCoin() {
		return aInCoin == null ? aInCoin = baseRate.aPrincipal * (1 + baseRate.aRechargeRate) : aInCoin;
	}

	transient Double aRebateCoin;

	/** A平台 返水金额（串子全输时） */
	public double getARebateCoin() {
		return aRebateCoin == null ? aRebateCoin = getAInCoin() * baseRate.aRebate : aRebateCoin;
	}

	transient Double loss;

	/** A平台 串关全输时的固定收益 */
	public double getLoss() {
		return loss == null ? loss = getARebateCoin() - baseRate.aPrincipal : loss;
	}

	transient Double win;

	/** A平台 串关全赢时的固定收益（按输赢金额计算返水） */
	public double getWin() {
		final double odds = overallOdds();
		return win == null ? win = (getAInCoin() * odds) + (getARebateCoin() * Math.abs(odds - 1)) - baseRate.aPrincipal : win;
	}

	protected transient double[] hedgingCoins;

	/** 根据当前的赔率估算在 B平台 对冲的每场投注金额 */
	public double[] getHedgingCoins() {
		if (hedgingCoins == null) {
			final int size = parlays.length;
			hedgingCoins = new double[size];
			int lastIdx = size - 1;
			double bLossFactor = bLossFactor();
			// 计算最后一场的对冲金额
			hedgingCoins[lastIdx] = (getWin() - getLoss()) / (parlays[lastIdx].bWinFactor(baseRate.bRebate) - bLossFactor);
			// 往前推算每场需要对冲的金额
			for (int i = lastIdx - 1; i >= 0; i--) {
				hedgingCoins[i] = (parlays[i + 1].bWinFactor(baseRate.bRebate) * hedgingCoins[i + 1]) / (parlays[i].bWinFactor(baseRate.bRebate) - bLossFactor);
			}
		}
		return hedgingCoins;
	}

	/** 计算剩余场次的对冲金额 */
	public double[] calcHedgingCoinsLock(Date now) {
		if (hedgingCoins != null) {
			boolean flag = false; // 全部锁住时不更新对冲金额
			for (Odds parlay : parlays) {
				if (!parlay.lock) {
					flag = true;
					break;
				}
			}
			if (flag) {
				final long timeNow = now.getTime();
				final boolean firstEnd = parlays[0].lock || timeNow > parlays[0].gameOpenTime, // 第一场是否结束
						twoEnd = parlays[1].lock || timeNow > parlays[1].gameOpenTime, // 第二场是否结束
						existsThree = parlays.length == 3, // 是否存在第三场比赛
						threeEnd = existsThree && (parlays[2].lock || timeNow > parlays[2].gameOpenTime); // 第三场是否结束
				// 第一场比赛结束，第二场比赛将开始：计算第二场下注，若存在第三场则继续推导第三场下注额
				if (firstEnd && !twoEnd) {
					// hedgingCoins[1] = TODO
				}

				// 第一二场比赛结束，第三场比赛将开始：结合一二场投注，计算第三场投注
				if (existsThree) {
					// TODO: 2025/12/30 继续投注
				}
			}
		}
		return hedgingCoins;
	}

	public static class Out {

		public String resultDescription;
		public double out;

		public Out(String desc, double out) {
			this.resultDescription = desc;
			this.out = out;
		}

	}

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

				result.add(new Out("串子第" + (i + 1) + "场输", loss_ + (bLossFactor_ * sumBInCoin) + (bHedgingCoin * parlays[i].bWinFactor(baseRate.bRebate))));
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

	public double calcAvgProfitAndCache(double[] bHedgingCoins) {
		// 初始成本和产出
		return avgProfit = calcAvgProfit(bHedgingCoins);
	}

	public double calcAvgProfit(double[] bHedgingCoins) {
		// 初始成本和产出
		return getLoss() + (bHedgingCoins[0] * parlays[0].bWinFactor(baseRate.bRebate));
	}

	/** 根据当前返水配置刷新方案 */
	public HedgingDTO flush(BaseRateConifg baseRate) {
		return flush(baseRate, null);
	}

	public HedgingDTO flush(@Nullable BaseRateConifg baseRate, double[] hedgingCoins) {
		this.aInCoin = null;
		this.aRebateCoin = null;
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
		json = Jsons.encode(this);
	}

}