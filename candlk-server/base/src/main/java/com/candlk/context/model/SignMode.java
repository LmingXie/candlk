package com.candlk.context.model;

import javax.annotation.Nullable;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.candlk.common.model.ValueProxy;
import com.candlk.common.util.Common;
import lombok.Getter;

/**
 * 签到方式
 */
@Getter
public enum SignMode implements LabelI18nProxy<SignMode, Integer> {
	/** 连续签到 */
	SUSTAIN(1, "连续签到"),
	/** 累计签到 */
	ACCUMULATE(2, "累计签到"),
	;
	// 定义私有变量
	@EnumValue
	public final Integer value;
	final ValueProxy<SignMode, Integer> proxy;

	SignMode(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final SignMode[] CACHE = values();

	public static SignMode of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, 1);
	}

}