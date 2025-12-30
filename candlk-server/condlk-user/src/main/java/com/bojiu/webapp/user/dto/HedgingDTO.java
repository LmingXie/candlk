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
		/** A平台 赔率信息 */
		public OddsInfo aOdds;
		/** B平台 赔率信息 */
		public OddsInfo bOdds;
		/**
		 * A平台串子投注方向（对冲平台投注方向取反）
		 *
		 * <p>让球盘：0=主队(Home), 1=客队(Client)
		 * <p>大小盘：0=大球(Over), 1=小球(Under)
		 * <p>独赢盘：0=主胜, 1=客胜, 2=平局
		 *
		 * @see GameDTO.OddsInfo#getRates
		 */
		public Integer parlaysIdx;
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
			Integer[] currentScore = (type.type == PeriodType.FULL) ? scoreResult.getScore() : scoreResult.getScoreH();
			final int homeGoal = currentScore[0], clientGoal = currentScore[1];

			// 核心逻辑：所有计算最终需转化为“A平台投注方向”的胜平负
			return switch (type) {
				case R, HR -> {
					// 1. 解析原始盘口（例如 -1.25 代表主让，1.25 代表客让）
					final double r = parseRatio(oddsInfo.getRatioRate());

					// 计算主队的赢盘表现分数
					double homePerformance = (double) homeGoal + r - clientGoal;
					;

					// 3. 计算主队的赛果
					int homeResult = calcHandicapResult(homePerformance);

					// 4. 根据 A 平台投注方向输出结果
					this.result = parlaysIdx == 0/*投主队*/ ? homeResult : reversedResult(homeResult);
					yield true;
				}
				case OU, HOU -> {
					final double r = parseRatio(oddsInfo.getRatioRate());
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
		 * 将多种格式的 ratioRate 转换为 double
		 * <p>e.g., "0.5/1" -> 0.75, "-1/1.5" -> -1.25, "0.75" -> 0.75
		 */
		private double parseRatio(String ratioRate) {
			if (ratioRate == null || ratioRate.isEmpty() || "0".equals(ratioRate)) {
				return 0.0;
			}

			// 1. 判定整体正负号 (只要包含 "-" 就判定为负盘)
			double sign = ratioRate.contains("-") ? -1.0 : 1.0;

			// 2. 移除所有非数字和非分隔符的字符 (去掉 + -)
			String cleanRate = ratioRate.replaceAll("[\\-+]", "");

			// 3. 处理复合盘口 (0/0.5, 0.5/1, 1/1.5)
			if (cleanRate.contains("/")) {
				String[] parts = cleanRate.split("/");
				// 使用绝对值计算，避免 -0 干扰
				double r1 = Math.abs(Double.parseDouble(parts[0]));
				double r2 = Math.abs(Double.parseDouble(parts[1]));
				return sign * ((r1 + r2) / 2.0);
			}

			// 4. 处理单盘口 (1, -1, 0.5)
			return sign * Double.parseDouble(cleanRate);
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
			return toResult(result);
		}

		public static String toResult(Integer res) {
			return switch (res) {
				case ALL_WIN -> "全赢";
				case ALL_LOSE -> "全输";
				case WIN_HALF -> "赢半";
				case LOSE_HALF -> "输半";
				case DRAW -> "走水";
				default -> throw new IllegalArgumentException("未知的result：" + res);
			};
		}

		public String getBResult_() {
			return toResult(getBResult());
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
		for (Odds parlay : parlays) { // 移除冗余的赔率信息
			parlay.aGame.odds = null;
			parlay.bGame.odds = null;
		}
		json = Jsons.encode(this);
	}

}