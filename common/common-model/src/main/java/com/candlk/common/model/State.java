package com.candlk.common.model;

import javax.annotation.Nullable;

/**
 * 标识属性或枚举的可见性
 *
 * @date 2015年10月19日
 * @since 1.0
 */
public enum State implements ValueEnum<State, Integer> {
	/** 私有的，所有人均不可见 */
	PRIVATE,
	/** 内部的，仅后台可见 */
	INTERNAL,
	/** 受保护的，仅前台用户自身 + 后台可见 */
	PROTECTED,
	/** 公开的，所有人可见 */
	PUBLIC;

	public final Integer value;

	State() {
		this.value = ordinal();
	}

	@Override
	public Integer getValue() {
		return value;
	}

	@Override
	public State getValueOf(Integer value) {
		if (value == null) {
			return null;
		}
		return getValues()[value];
	}

	static State[] values;

	public static State[] getValues() {
		State[] array = values;
		if (array == null) {
			values = array = values();
		}
		return array;
	}

	public static State of(@Nullable Integer value) {
		return PRIVATE.getValueOf(value);
	}

}
