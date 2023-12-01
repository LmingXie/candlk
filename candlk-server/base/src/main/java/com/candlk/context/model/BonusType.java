package com.candlk.context.model;

import javax.annotation.Nullable;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.candlk.common.model.ValueProxy;
import com.candlk.common.util.Common;
import lombok.Getter;

/**
 * 奖金方式
 */
@Getter
public enum BonusType implements LabelI18nProxy<BonusType, Integer> {
	/** 固定 */
	FIX(1, "固定"),
	/** 随机 */
	RANDOM(2, "随机"),
	/** 比例 */
	RATIO(3, "比例"),
	;
	// 定义私有变量
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

}