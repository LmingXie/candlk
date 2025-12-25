package com.bojiu.context.model;

import java.util.*;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import lombok.Getter;
import me.codeplayer.util.CollectionUtil;
import org.jspecify.annotations.Nullable;

import static com.bojiu.context.i18n.UserModelI18nKey.*;

/**
 * 活动条件
 */
@Getter
public enum PromotionCond implements LabelI18nProxy<PromotionCond, Integer> {

	/** 账号首充 */
	FIRST_RECHARGE(1, PROMOTION_COND_FIRST_RECHARGE),
	/** 每日首充 */
	DAILY_FIRST_RECHARGE(2, PROMOTION_COND_DAILY_FIRST_RECHARGE),
	/** 累计充值 */
	TOTAL_RECHARGE(3, PROMOTION_COND_TOTAL_RECHARGE, false, null, false),
	/** 单笔充值 */
	SINGLE_RECHARGE(4, PROMOTION_COND_SINGLE_RECHARGE, false, null, false),
	/** 二充 */
	SECOND_RECHARGE(5, PROMOTION_COND_SECOND_RECHARGE, true, 2L),
	/** 三充 */
	THIRD_RECHARGE(6, PROMOTION_COND_THIRD_RECHARGE, true, 3L),
	/** 四充 */
	FOUR_RECHARGE(7, PROMOTION_COND_FOUR_RECHARGE, true, 4L),
	/** 五充 */
	FIVE_RECHARGE(8, PROMOTION_COND_FIVE_RECHARGE, true, 5L),
	;

	@EnumValue
	public final Integer value;
	final ValueProxy<PromotionCond, Integer> proxy;
	/** 是否为充值次数 */
	public final boolean isTimesRecharge;
	/** 充值次数 */
	public final Long times;
	/** 详情按天循环，默认按天循环 */
	public final boolean isDayCycle;

	PromotionCond(Integer value, String label, boolean isTimesRecharge, Long times, boolean isDayCycle) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
		this.isTimesRecharge = isTimesRecharge;
		this.times = times;
		this.isDayCycle = isDayCycle;
	}

	PromotionCond(Integer value, String label, boolean isTimesRecharge, Long times) {
		this(value, label, isTimesRecharge, times, true);
	}

	PromotionCond(Integer value, String label) {
		this(value, label, false, null);
	}

	public static final PromotionCond[] CACHE = values();

	public static PromotionCond of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, 1);
	}

	public static Set<PromotionCond> getTimesRechargeGroup() {
		Set<PromotionCond> set = new HashSet<>(4, 1F);
		for (PromotionCond p : CACHE) {
			if (p.isTimesRecharge) {
				set.add(p);
			}
		}
		return set;
	}

	/** 实际按天循环类型 */
	public static final List<PromotionCond> DAY_CYCLE_CACHE = CollectionUtil.filter(Arrays.asList(values()), PromotionCond::isDayCycle);
}