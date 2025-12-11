package com.bojiu.webapp.user.dto;

import java.util.*;

import com.bojiu.context.web.Jsons;
import lombok.extern.slf4j.Slf4j;

/** 预估对冲算法类 */
@Slf4j
public class HedgingDTO {

	/** 串子/串关 */
	public Odds[] parlays;

	public HedgingDTO(Odds[] parlays) {
		this.parlays = parlays;
	}

	/** A平台本金 */
	public double aPrincipal = 1000,
	/** A平台充值返奖（%） */
	aRechargeRate = 0,
	/** 投注返水比例（%） */
	aRebate = 0.02,
	/** B平台投注返水比例（%） */
	bRebate = 0.025;

	public void initRebate(double aPrincipal, double aRechargeRate, double aRebate, double bRebate) {
		this.aPrincipal = aPrincipal;
		this.aRechargeRate = aRechargeRate;
		this.aRebate = aRebate;
		this.bRebate = bRebate;
	}

	/** 赔率信息 */
	public static class Odds {

		public double aRate;
		public double bRate;

		public Odds(double aRate, double bRate) {
			this.aRate = aRate;
			this.bRate = bRate;
		}

		transient Double bWinFactor;

		/** B平台赢的净收益系数 */
		public double bWinFactor(double bRebate) {
			return bWinFactor == null ? bWinFactor = bRate - 1 + bRebate : bWinFactor;
		}

	}

	transient Double bLossFactor;

	/** B平台输的净收益系数 */
	public double bLossFactor() {
		return bLossFactor == null ? bLossFactor = bRebate - 1 : bLossFactor;
	}

	/** 综合赔率 */
	transient Double overallOdds;

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

	/** A平台串子投注金额 */
	public double getAInCoin() {
		return aInCoin == null ? aInCoin = aPrincipal * (1 + aRechargeRate) : aInCoin;
	}

	transient Double aRebateCoin;

	/** A平台返水金额 */
	public double getARebateCoin() {
		return aRebateCoin == null ? aRebateCoin = getAInCoin() * aRebate : aRebateCoin;
	}

	transient Double loss;

	/** A平台串关输时的固定收益 */
	public double getLoss() {
		return loss == null ? loss = getARebateCoin() - aPrincipal : loss;
	}

	transient Double win;

	/** A平台串关赢时的固定收益 */
	public double getWin() {
		return win == null ? win = getAInCoin() * overallOdds() + getARebateCoin() - aPrincipal : win;
	}

	transient double[] hedgingCoins;

	/** 根据当前的赔率估算在B平台对冲的每场投注金额 */
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

	/** 计算三串一利润 */
	public List<Out> calcProfit(double[] bHedgingCoins) {
		List<Out> outs = new ArrayList<>(4);
		final int N = parlays.length;
		// 初始成本和产出
		double aInCoin = getAInCoin(), sumInCoin = aInCoin, sumRebate = getARebateCoin();

		// 计算A平台串子输的情况
		for (int i = 0; i < N; i++) {
			double bHedgingCoin = bHedgingCoins[i];
			sumInCoin += bHedgingCoin;
			sumRebate += bHedgingCoin * bRebate;

			outs.add(new Out("串子在M" + (i + 1) + "结束", (bHedgingCoin * parlays[i].bRate) + sumRebate - sumInCoin));
		}

		// 计算A平台串子赢的情况
		outs.add(new Out("串子全中", (aInCoin * overallOdds()) + sumRebate - sumInCoin));

		// 3. 盈利排名 (按最大净收益从高到低排序)
		outs.sort(Comparator.comparingDouble(r -> r.out));
		Collections.reverse(outs);

		return outs;
	}

	public static void main(String[] args) {
		HedgingDTO dto = new HedgingDTO(new Odds[] {
				new Odds(1.95, 2),
				new Odds(2, 1.98),
				new Odds(1.95, 1.96)
		});
		double[] hedgingCoins1 = dto.getHedgingCoins();
		System.out.println("最佳对冲下注方案：" + Jsons.encode(hedgingCoins1));
		List<Out> profitResults = dto.calcProfit(hedgingCoins1);
		System.out.println(Jsons.encode(profitResults));
	}

}