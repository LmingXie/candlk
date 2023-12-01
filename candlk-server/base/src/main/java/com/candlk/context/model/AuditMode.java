package com.candlk.context.model;

import javax.annotation.Nullable;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.candlk.common.model.ValueProxy;
import com.candlk.common.util.Common;
import lombok.Getter;

/**
 * 稽核方式
 */
@Getter
public enum AuditMode implements LabelI18nProxy<AuditMode, Integer> {
	/** 稽核奖金 */
	BONUS(1, "稽核奖金"),
	/** 稽核本金+奖金 */
	CAPITAL_BONUS(2, "稽核本金+奖金"),
	;
	// 定义私有变量
	@EnumValue
	public final Integer value;
	final ValueProxy<AuditMode, Integer> proxy;

	AuditMode(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final AuditMode[] CACHE = values();

	public static AuditMode of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, 1);
	}

}