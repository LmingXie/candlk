package com.bojiu.context.model;

import javax.annotation.Nullable;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import com.bojiu.context.i18n.AdminI18nKey;
import lombok.Getter;

/**
 * 稽核方式
 */
@Getter
public enum AuditMode implements LabelI18nProxy<AuditMode, Integer> {
	/** 稽核奖金 */
	BONUS(1, BaseI18nKey.AUDIT_MODE_BONUS),
	/** 稽核本金+奖金 */
	CAPITAL_BONUS(2, AdminI18nKey.AUDIT_MODE_CAPITAL_BONUS),
	/** 稽核余额+奖金 */
	BALANCE_BONUS(3, AdminI18nKey.AUDIT_MODE_BALANCE_BONUS);

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
