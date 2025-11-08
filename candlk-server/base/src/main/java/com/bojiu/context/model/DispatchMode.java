package com.bojiu.context.model;

import javax.annotation.Nullable;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import com.bojiu.context.i18n.AdminI18nKey;
import lombok.Getter;

/**
 * 派发方式
 */
@Getter
public enum DispatchMode implements LabelI18nProxy<DispatchMode, Integer> {
	/** 玩家自领-过期自动派发 */
	MANUAL_EXPIRED(0, AdminI18nKey.DISPATCH_MODE_MANUAL_EXPIRED),
	/** 玩家自领-过期作废 */
	MANUAL(1, BaseI18nKey.DISPATCH_MODE_MANUAL),
	/** 自动派发 */
	AUTO(2, AdminI18nKey.DISPATCH_MODE_AUTO),
	/** 玩家申请-人工派发 */
	ARTIFICIAL(3, AdminI18nKey.DISPATCH_MODE_ARTIFICIAL),
	;

	@EnumValue
	public final Integer value;
	final ValueProxy<DispatchMode, Integer> proxy;

	DispatchMode(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final DispatchMode[] CACHE = values();

	public static DispatchMode of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, 0);
	}

}
