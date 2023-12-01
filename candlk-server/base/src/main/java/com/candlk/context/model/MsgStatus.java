package com.candlk.context.model;

import javax.annotation.Nullable;

import com.candlk.common.model.ValueProxy;
import com.candlk.common.util.Common;
import lombok.Getter;

@Getter
public enum MsgStatus implements LabelI18nProxy<MsgStatus, Integer> {
	/** 已撤回 */
	CANCELED(-1, "已撤回"),
	/** 待发送 */
	PENDING(0, "待发送"),
	/** 已发送 */
	SENT(1, "已发送"),
	;

	public final Integer value;
	final ValueProxy<MsgStatus, Integer> proxy;

	MsgStatus(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final MsgStatus[] CACHE = values();

	public static MsgStatus of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, -1);
	}

}
