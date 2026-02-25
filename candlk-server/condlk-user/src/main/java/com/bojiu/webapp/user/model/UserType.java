package com.bojiu.webapp.user.model;

import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import com.bojiu.context.model.LabelI18nProxy;
import lombok.Getter;

/**
 * 用户类型
 */
@Getter
public enum UserType implements LabelI18nProxy<UserType, Integer> {

	/** 协议号 */
	SESSION(0, "协议号"),
	/** 直登号 */
	DIRECT(1, "直登号"),
	/** TD号 */
	TD_LIB(2, "TD号"),
	;

	public final Integer value;
	final ValueProxy<UserType, Integer> proxy;

	UserType(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final UserType[] CACHE = values();

	public static UserType of(Integer value) {
		return Common.getEnum(CACHE, value, 0);
	}

}
