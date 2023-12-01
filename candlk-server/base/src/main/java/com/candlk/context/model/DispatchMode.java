package com.candlk.context.model;

import javax.annotation.Nullable;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.candlk.common.model.ValueProxy;
import com.candlk.common.util.Common;
import lombok.Getter;

/**
 * 派发方式
 */
@Getter
public enum DispatchMode implements LabelI18nProxy<DispatchMode, Integer> {
	/** 玩家自领-过期作废 */
	MANUAL(1, "玩家自领-过期作废"),
	/** 自动派发 */
	AUTO(2, "自动派发"),
	;

	// 定义私有变量
	@EnumValue
	public final Integer value;
	final ValueProxy<DispatchMode, Integer> proxy;

	DispatchMode(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final DispatchMode[] CACHE = values();

	public static DispatchMode of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, 1);
	}

}