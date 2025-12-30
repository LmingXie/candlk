package com.bojiu.webapp.user.dto;

import java.util.*;

import com.bojiu.common.redis.RedisUtil;
import com.bojiu.context.web.Jsons;
import com.bojiu.webapp.base.entity.BaseEntity;
import com.bojiu.webapp.user.dto.GameDTO.OddsInfo;
import com.bojiu.webapp.user.model.UserRedisKey;
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

		/** 串子平台已结束赛事的赛果（0=全赢；1=赢半；2=输半；3=全输）对冲平台的盈亏基于此计算 */
		public int[] hedgingResult;

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