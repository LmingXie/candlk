package com.bojiu.context.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.context.I18N;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import com.bojiu.context.i18n.AdminI18nKey;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

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
	REVIEW(3, AdminI18nKey.DISPATCH_MODE_REVIEW),
	;

	@EnumValue
	public final Integer value;
	public final String label;
	final ValueProxy<DispatchMode, Integer> proxy;

	DispatchMode(Integer value, String label) {
		this.value = value;
		this.label = label;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	/** 手动领取、自动派发 */
	public String asModeLabel() {
		return I18N.msg(this == AUTO ? "auto.desc" : "manual.desc");
	}

	public String asDispatchModeDesc() {
		return I18N.msg(this == MANUAL_EXPIRED || this == MANUAL ? label + ".desc" : label);
	}

	public static final DispatchMode[] CACHE = values();

	public static DispatchMode of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, 0);
	}

}