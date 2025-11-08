package com.bojiu.context.model;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import lombok.Getter;

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
	TOTAL_RECHARGE(3, PROMOTION_COND_TOTAL_RECHARGE),
	/** 单笔充值 */
	SINGLE_RECHARGE(4, PROMOTION_COND_SINGLE_RECHARGE),
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

	PromotionCond(Integer value, String label, boolean isTimesRecharge, Long times) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
		this.isTimesRecharge = isTimesRecharge;
		this.times = times;
	}

	PromotionCond(Integer value, String label, boolean isTimesRecharge) {
		this(value, label, isTimesRecharge, null);
	}

	PromotionCond(Integer value, String label) {
		this(value, label, false);
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

}