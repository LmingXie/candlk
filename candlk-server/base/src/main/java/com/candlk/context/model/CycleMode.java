package com.candlk.context.model;

import javax.annotation.Nullable;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.candlk.common.model.ValueProxy;
import com.candlk.common.util.Common;
import lombok.Getter;

/**
 * 循环方式
 */
@Getter
public enum CycleMode implements LabelI18nProxy<CycleMode, Integer> {
	/** 单次活动 */
	SINGLE(1, "单次活动"),
	/** 每日循环 */
	DAILY_CYCLE(2, "每日循环"),
	/** 每周循环 */
	WEEKLY_CYCLE(3, "每周循环"),
	;
	// 定义私有变量
	@EnumValue
	public final Integer value;
	final ValueProxy<CycleMode, Integer> proxy;

	CycleMode(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final CycleMode[] CACHE = values();

	public static CycleMode of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, 1);
	}

}