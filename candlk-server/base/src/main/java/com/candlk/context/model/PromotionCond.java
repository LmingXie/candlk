package com.candlk.context.model;

import javax.annotation.Nullable;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.candlk.common.model.ValueProxy;
import com.candlk.common.util.Common;
import lombok.Getter;

/**
 * 活动条件
 */
@Getter
public enum PromotionCond implements LabelI18nProxy<PromotionCond, Integer> {
	/** 账号首充 */
	FIRST_RECHARGE(1, "账号首充"),
	/** 累计充值 */
	TOTAL_RECHARGE(2, "累计充值"),
	/** 单笔充值 */
	SINGLE_RECHARGE(2, "单笔充值"),
	;
	// 定义私有变量
	@EnumValue
	public final Integer value;
	final ValueProxy<PromotionCond, Integer> proxy;

	PromotionCond(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final PromotionCond[] CACHE = values();

	public static PromotionCond of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, 1);
	}

}