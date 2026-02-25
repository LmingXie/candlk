package com.bojiu.webapp.user.model;

import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import com.bojiu.context.model.LabelI18nProxy;
import lombok.Getter;

/**
 * 消息类型
 */
@Getter
public enum MsgType implements LabelI18nProxy<MsgType, Integer> {

	/** 普通消息 */
	MSG(0, "普通消息"),
	/** 引用回复 */
	REPLY(1, "引用回复"),
	/** 转发 */
	FORWARD(2, "转发"),
	;

	public final Integer value;
	final ValueProxy<MsgType, Integer> proxy;

	MsgType(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final MsgType[] CACHE = values();

	public static MsgType of(Integer value) {
		return Common.getEnum(CACHE, value, 0);
	}

}
