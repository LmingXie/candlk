package com.bojiu.context.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nullable;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.context.I18N;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import com.bojiu.context.i18n.AdminI18nKey;
import lombok.*;
import me.codeplayer.util.Arith;
import me.codeplayer.util.RandomUtil;

/* 奖金方式  */
@Getter
public enum BonusType implements LabelI18nProxy<BonusType, Integer> {
	/** 固定 */
	FIXED(1, AdminI18nKey.BONUS_TYPE_FIXED),
	/** 随机 */
	RANDOM(2, AdminI18nKey.BONUS_TYPE_RANDOM),
	/** 比例 */
	RATIO(3, AdminI18nKey.BONUS_TYPE_RATIO),
	;

	@EnumValue
	public final Integer value;
	final ValueProxy<BonusType, Integer> proxy;

	BonusType(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final BonusType[] CACHE = values();

	public static BonusType of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, 1);
	}

	public static BigDecimal calcReward(BonusType type, BigDecimal amount, BigDecimal reward, BigDecimal ratio, BigDecimal min, BigDecimal max, @Nullable List<LimitOddsVO> limitOdds) {
		return switch (type) {
			case FIXED -> reward;
			case RATIO -> amount.multiply(ratio).movePointLeft(2).setScale(2, RoundingMode.FLOOR);
			case RANDOM -> calcRandomReward(min, max, limitOdds);
		};
	}

	public static BigDecimal calcRandomReward(BigDecimal min, BigDecimal max, @Nullable List<LimitOddsVO> limitOdds) {
		if (min == null || max == null) {
			return BigDecimal.ZERO;
		}
		double confirmMin = min.doubleValue(), confirmMax = max.doubleValue();
		if (limitOdds != null) {
			final long odds = ThreadLocalRandom.current().nextLong(1, 10000); // 生成概率
			long cp = 0L;
			for (int i = 0, size = limitOdds.size(); i < size; i++) {
				final LimitOddsVO vo = limitOdds.get(i);
				if (odds <= (cp += vo.odds.movePointRight(2).longValue())) { // 掉落在当前概率范围内
					confirmMax = vo.limit.doubleValue();
					final int prev = i - 1;
					if (prev > -1) { // 获取前一阶梯金额作为 min，否则等于初始min
						confirmMin = limitOdds.get(prev).limit.doubleValue();
					}
					break;
				}
			}
		}
		return Common.floor(ThreadLocalRandom.current().nextDouble(confirmMin, confirmMax), 2);
	}

	@Getter
	@Setter
	public static class Config {

		/** 奖励金额 */
		public BigDecimal reward;
		/** 最小奖励金额 */
		public BigDecimal min;
		/** 最大奖励金额 */
		public BigDecimal max;
		/** 期望奖金/平均值（平均值上下浮动avgFactor%，minFactor%概率触发最小值范围随机，maxFactor%概率触发最大值范围随机） */
		public BigDecimal avg;
		/** 前端展示金额（最小值） */
		public BigDecimal showMin;
		/** 前端展示金额（最大值） */
		public BigDecimal showMax;
		/** 额度概率配置 */
		public List<LimitOddsVO> limitOdds;

		public static final double AVG_FACTOR = 0.5d, MIN_FACTOR = 0.05d, MAX_FACTOR = 0.05d;

		public BigDecimal randomAvg() {
			return randomAvg(AVG_FACTOR, MIN_FACTOR, MAX_FACTOR);
		}

		public BigDecimal randomAvg(BigDecimal avgFactor, BigDecimal minFactor, BigDecimal maxFactor) {
			return randomAvg(avgFactor.doubleValue(), minFactor.doubleValue(), maxFactor.doubleValue());
		}

		public BigDecimal randomAvg(double avgFactor, double minFactor, double maxFactor) {
			return randomAvg(avgFactor, minFactor, maxFactor, null);
		}

		/**
		 * 生成随机奖励金额
		 *
		 * @param avgFactor 平均值上下浮动因子，例如0.2表示±20%
		 * @param minFactor 触发min区间概率，例如0.05表示5%
		 * @param maxFactor 触发max区间概率
		 * @return 随机金额，保留两位小数
		 */
		public BigDecimal randomAvg(double avgFactor, double minFactor, double maxFactor, Double max) {
			double rand = ThreadLocalRandom.current().nextDouble();
			final double avgValue = cacheAvg(), minValue = cacheMin(), maxValue = max == null ? cacheMax() : max;
			BigDecimal result;
			if (rand < minFactor) {
				// 触发 min ~ avg 随机
				result = Common.floor(ThreadLocalRandom.current().nextDouble(minValue, avgValue), 2);
			} else if (rand < minFactor + maxFactor) {
				// 触发 avg ~ max 随机
				result = Common.floor(ThreadLocalRandom.current().nextDouble(avgValue, maxValue), 2);
			} else {
				// 默认在 avg ± avgFactor% 区间内，但限定在 [min, max] 范围
				double rawLow = avgValue * (1 - avgFactor);
				double rawHigh = avgValue * (1 + avgFactor);

				// 限制在 [min, max]
				double finalLow = Math.max(rawLow, minValue), finalHigh = Math.min(rawHigh, maxValue);

				// 避免非法范围
				if (finalLow > finalHigh) {
					finalLow = finalHigh;
				}

				result = Common.floor(ThreadLocalRandom.current().nextDouble(finalLow, finalHigh), 2);
			}

			return result;
		}

		public BigDecimal outMax() {
			return showMax == null ? max : showMax;
		}

		public BigDecimal outMin() {
			return showMin == null ? min : showMin;
		}

		public void checkAvg() {
			I18N.assertTrue(min != null && min.compareTo(BigDecimal.ZERO) > 0, AdminI18nKey.PROMOTION_AWARD_MIN_INVALID); // 最小金额错误
			I18N.assertTrue(max != null && max.compareTo(min) > 0, AdminI18nKey.PROMOTION_AWARD_MAX_INVALID); // 最大金额错误
			I18N.assertTrue(avg != null && avg.compareTo(min) > 0 && avg.compareTo(max) < 0, AdminI18nKey.PROMOTION_AWARD_AVG_INVALID);
			I18N.assertTrue((showMin == null && showMax == null) // 要么不填，要么都必须填写
							|| (showMin != null && showMin.compareTo(BigDecimal.ZERO) >= 0 && showMax != null && showMax.compareTo(showMin) >= 0)
					, AdminI18nKey.PROMOTION_AWARD_SHOW_AMOUNT_INVALID);
		}

		/**
		 * 在 min/max 范围内基于 avg 和概率逻辑生成一个随机 Integer
		 *
		 * @param min 最小值（包含）
		 * @param max 最大值（包含）
		 * @param avg 平均值
		 * @param avgFactor 平均值上下浮动比例，例如 0.2 表示 ±20%
		 * @param minFactor 触发 [min, avg) 区间的概率，例如 0.05 表示 5%
		 * @param maxFactor 触发 [avg, max] 区间的概率
		 * @return 随机整数值，范围在 [min, max]
		 */
		public static int randomAvgInt(int min, int max, int avg, BigDecimal avgFactor, BigDecimal minFactor, BigDecimal maxFactor) {

			if (avg < min || avg > max) {
				throw new IllegalArgumentException("参数范围不合法");
			}

			double rand = ThreadLocalRandom.current().nextDouble();

			int result;

			final double minFactorVal = minFactor.doubleValue();
			if (rand < minFactorVal) {
				// min ~ avg 区间
				result = RandomUtil.getInt(min, avg);
			} else if (rand < minFactorVal + maxFactor.doubleValue()) {
				// avg ~ max 区间
				result = RandomUtil.getInt(avg, max);
			} else {
				// 在 avg ± avgFactor% 区间内随机
				double factor = avgFactor.doubleValue();
				double lower = avg * (1 - factor);
				double upper = avg * (1 + factor);

				// 限制范围不能超过 min/max
				int floorMin = Math.max(min, (int) Math.floor(lower));
				int ceilMax = Math.min(max, (int) Math.ceil(upper));

				result = RandomUtil.getInt(floorMin, ceilMax);
			}
			return result;
		}

		public BigDecimal calcReward(Integer bonusType) {
			return calcReward(bonusType, null);
		}

		public BigDecimal calcReward(Integer bonusType, BigDecimal amount) {
			return RANDOM.eq(bonusType) && avg != null
					? randomAvg() // 期望奖金算法
					: BonusType.calcReward(BonusType.of(bonusType), amount, reward, null, min, max, limitOdds); // 随机概率算法
		}

		public void check(BonusType bt) {
			if (bt == BonusType.FIXED) {
				// 固定金额错误
				I18N.assertTrue(reward != null && reward.compareTo(BigDecimal.ZERO) > 0, AdminI18nKey.PROMOTION_AWARD_FIX_INVALID);
			} else {
				if (avg != null) {
					checkAvg();
				} else {
					I18N.assertTrue(min != null && min.compareTo(BigDecimal.ZERO) > 0, AdminI18nKey.PROMOTION_AWARD_MIN_INVALID); // 最小金额错误
					I18N.assertTrue(max != null && max.compareTo(min) > 0, AdminI18nKey.PROMOTION_AWARD_MAX_INVALID); // 最大金额错误
				}
			}
			LimitOddsVO.validate(limitOdds, min, max);
		}

		transient Double avgCache;

		public double cacheAvg() {
			return avgCache != null ? avgCache : (avgCache = avg.doubleValue());
		}

		transient Double minCache;

		public double cacheMin() {
			return minCache != null ? minCache : (minCache = min.doubleValue());
		}

		transient Double maxCache;

		public double cacheMax() {
			return maxCache != null ? maxCache : (maxCache = max.doubleValue());
		}

	}

	/** 限制随机掉落概率：第一阶梯应该大于最低奖励额，最后阶梯应该等于最大奖励额！ */
	@Setter
	@Getter
	@AllArgsConstructor
	public static class LimitOddsVO {

		/** 额度 */
		BigDecimal limit;
		/** 概率 */
		BigDecimal odds;

		public static void validate(List<LimitOddsVO> limitOddsVOS, BigDecimal min, BigDecimal max) {
			if (limitOddsVOS != null) {
				final int size = limitOddsVOS.size();
				I18N.assertTrue(size > 1, AdminI18nKey.PROMOTION_AWARD_RATIO_TIERS_REQUIRED);  // 请至少配置两个概率阶梯
				I18N.assertTrue(size <= 10, AdminI18nKey.PROMOTION_AWARD_RATIO_TIERS_INVALID); // 最多10个额度比例
				I18N.assertTrue(limitOddsVOS.get(0).limit.compareTo(min) > 0, AdminI18nKey.PROMOTION_RATIO_TIERS_FIRST_INVALID, min); // 第一阶梯应该大于最低奖励额
				I18N.assertTrue(limitOddsVOS.get(size - 1).limit.compareTo(max) == 0, AdminI18nKey.PROMOTION_RATIO_TIERS_LAST_INVALID, max); // 最后阶梯应该等于最大奖励额

				BigDecimal total = BigDecimal.ZERO;
				for (int i = 0; i < size; i++) {
					final LimitOddsVO t = limitOddsVOS.get(i);
					total = total.add(t.odds);

					final int prev = i - 1;
					if (prev > -1) { // 存在上一阶梯
						// 请保证阶梯金额是递增的，问题金额在 {0} 附近
						I18N.assertTrue(t.limit.compareTo(limitOddsVOS.get(prev).limit) > 0, AdminI18nKey.PROMOTION_TIERS_AMOUNT_INVALID, t.getLimit());
					}
				}
				// 概率总和必须等于100
				I18N.assertTrue(total.compareTo(Arith.HUNDRED) == 0, AdminI18nKey.PROMOTION_RATIO_TIERS_SUM_INVALID);
			}
		}

	}

}