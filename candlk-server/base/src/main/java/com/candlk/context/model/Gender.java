package com.candlk.context.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.candlk.common.model.ValueProxy;
import com.candlk.common.util.Common;
import lombok.Getter;

@Getter
public enum Gender implements LabelI18nProxy<Gender, Integer> {

	/** 女 */
	FEMALE(0, "female"),
	/** 男 */
	MALE(1, "male");

	// 定义私有变量
	public final Integer value;
	final ValueProxy<Gender, Integer> proxy;

	// 构造函数，枚举类型只能为私有
	Gender(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final Gender[] CACHE = values();

	public static Gender of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, 0);
	}

	public Gender other() {
		return this == MALE ? FEMALE : MALE;
	}

	@Nonnull
	public static Gender getOrDefault(@Nullable Gender gender) {
		return gender == null ? MALE : gender;
	}

}
