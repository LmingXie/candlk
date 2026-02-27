package com.bojiu.webapp.user.model;

import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import com.bojiu.context.model.LabelI18nProxy;
import lombok.Getter;

/**
 * 用户类型
 */
@Getter
public enum ChatType implements LabelI18nProxy<ChatType, Integer> {

	/** 私聊 */
	PRIVATE(0, "私聊"),
	/** 群组 */
	GROUP(1, "群组"),
	/** 频道 */
	CHANNEL(2, "频道"),
	;

	public final Integer value;
	final ValueProxy<ChatType, Integer> proxy;

	ChatType(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final ChatType[] CACHE = values();

	public static ChatType of(Integer value) {
		return Common.getEnum(CACHE, value, 0);
	}

}
