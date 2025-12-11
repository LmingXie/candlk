package com.bojiu;

import com.bojiu.webapp.user.utils.HGOddsConverter;
import org.junit.jupiter.api.Test;

public class JavaTest {

	@Test
	public void oddsTest() {
		double[] result = HGOddsConverter.convertOddsRatio(0.560, 1.340, 2, null);
		System.out.println("H=" + result[0] + ", C=" + result[1]);
	}

	static class HedgingDTO {

		public double aRate;
		public double bRate;

		public HedgingDTO(double aRate, double bRate) {
			this.aRate = aRate;
			this.bRate = bRate;
		}

		transient Double bWinFactor;

		/** B平台净盈利系数 */
		public double bWinFactor(double bRebate) {
			return bWinFactor == null ? bWinFactor = bRate - 1 + bRebate : bWinFactor;
		}

		transient Double bLossFactor;

		/** B平台净亏损系数 */
		public double bLossFactor(double bRebate) {
			return bLossFactor == null ? bLossFactor = 1 - bRate : bLossFactor;
		}

	}

	@Test
	public void hedgingTest() {
		double aPrincipal = 1000, // 本金
				aRechargeRate = 0,  // 充值返奖（% 不参与计算）
				aRebate = 0.02, // 投注返水比例（%）
				bRebate = 0.025; // B平台投注返水比例（%）
		// List<HedgingDTO>
		/*
		1、任何赔率低于0.85将被过滤不参与组串子
		2、串子之间的间隔必须大于1小时（保留一定对冲操作时间）
		 */
	}

}