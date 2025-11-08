package com.bojiu.context.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import lombok.Getter;

import static com.bojiu.context.i18n.UserModelI18nKey.GENDER_FEMALE;
import static com.bojiu.context.i18n.UserModelI18nKey.GENDER_MALE;

@Getter
public enum Gender implements LabelI18nProxy<Gender, Integer> {

	/** 女 */
	FEMALE(0, GENDER_FEMALE),
	/** 男 */
	MALE(1, GENDER_MALE);

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
