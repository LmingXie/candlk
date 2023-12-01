package com.candlk.context.model;

import javax.annotation.Nullable;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.candlk.common.model.ValueProxy;
import com.candlk.common.util.Common;
import lombok.Getter;

/**
 * 活动类型
 */
@Getter
public enum PromotionType implements LabelI18nProxy<PromotionType, Integer> {
	/** 充值 */
	RECHARGE(1, "充值"),
	/** 打码 */
	PLAY(2, "打码"),
	/** 签到 */
	SIGN(3, "签到"),
	;

	// 定义私有变量
	@EnumValue
	public final Integer value;
	final ValueProxy<PromotionType, Integer> proxy;

	PromotionType(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final PromotionType[] CACHE = values();

	public static PromotionType of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, 1);
	}

}