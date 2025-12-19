package com.bojiu.webapp.user.dto;

import java.util.ArrayList;
import java.util.List;

import com.bojiu.webapp.user.dto.GameDTO.OddsInfo;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

/** 预估对冲算法类 */
@Slf4j
public class HedgingDTO {

	/** 串子/串关 */
	public Odds[] parlays;

	public HedgingDTO(Odds[] parlays) {
		this.parlays = parlays;
	}

	/** A平台 本金 */
	public double aPrincipal = 1000,
	/** A平台充值返奖（%） */
	aRechargeRate = 0,
	/** 投注返水比例（%） */
	aRebate = 0.015,
	/** B平台投注返水比例（%） */
	bRebate = 0.025;

	public void initRebate(double aPrincipal, double aRechargeRate, double aRebate, double bRebate) {
		this.aPrincipal = aPrincipal;
		this.aRechargeRate = aRechargeRate;
		this.aRebate = aRebate;
		this.bRebate = bRebate;
	}

	public record GameRate(GameDTO game, Integer oddsIdx, Integer rateIdx) {

	}

	/** 赔率信息 */
	@NoArgsConstructor
	public static class Odds {

		/** A平台 赔率 */
		public double aRate;
		/** B平台 赔率 */
		public double bRate;
		/** A平台 游戏信息 */
		public GameRate aGame;
		/** B平台 游戏信息 */
		public GameRate bGame;
		@Setter
		@Getter
		public Long gameOpenTime;

		public Odds(double aRate, double bRate) {
			this.aRate = aRate;
			this.bRate = bRate;
		}

		public Odds initGame(GameRate aGame, GameRate bGame) {
			this.aGame = aGame;
			this.bGame = bGame;
			return this;
		}

		public String getInfo() {
			if (aGame != null) {
				final GameDTO game = this.aGame.game;
				OddsInfo oddsInfo = game.odds.get(this.aGame.oddsIdx);
				return "【" + game.league + "】" + game.teamHome + " VS " + game.teamClient
						+ " 【" + oddsInfo.getType().getLabel() + "】（" + oddsInfo.ratioRate + "）";

			}
			return null;
		}

		public void clear() {
			this.aGame = null;
			this.bGame = null;
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
		return bLossFactor == null ? bLossFactor = bRebate - 1 : bLossFactor;
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
		return aInCoin == null ? aInCoin = aPrincipal * (1 + aRechargeRate) : aInCoin;
	}

	transient Double aRebateCoin;

	/** A平台 返水金额（串子全输时） */
	public double getARebateCoin() {
		return aRebateCoin == null ? aRebateCoin = getAInCoin() * aRebate : aRebateCoin;
	}

	transient Double loss;

	/** A平台 串关全输时的固定收益 */
	public double getLoss() {
		return loss == null ? loss = getARebateCoin() - aPrincipal : loss;
	}

	transient Double win;

	/** A平台 串关全赢时的固定收益（按输赢金额计算返水） */
	public double getWin() {
		final double odds = overallOdds();
		return win == null ? win = (getAInCoin() * odds) + (getARebateCoin() * Math.abs(odds - 1)) - aPrincipal : win;
	}

	transient double[] hedgingCoins;

	/** 根据当前的赔率估算在 B平台 对冲的每场投注金额 */
	public double[] getHedgingCoins() {
		if (hedgingCoins == null) {
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

				result.add(new Out("串子第" + (i + 1) + "场输", loss_ + (bLossFactor_ * sumBInCoin) + (bHedgingCoin * parlays[i].bWinFactor(bRebate))));
				sumBInCoin += bHedgingCoin;
			}
			// 计算 A平台 串子赢的情况
			result.add(new Out("串子全中", getWin() + (bLossFactor_ * sumBInCoin)));
			outs = result;
		}
		return outs;
	}

	public double calcAvgProfit(double[] bHedgingCoins) {
		// 初始成本和产出
		return getLoss() + (bHedgingCoins[0] * parlays[0].bWinFactor(bRebate));
	}

}