package com.bojiu.context.model;

import javax.annotation.Nullable;

import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import lombok.Getter;

@Getter
public enum ServerVendor implements LabelI18nProxy<ServerVendor, Integer> {

	/** 亚马逊 */
	AWS(1, BaseI18nKey.SERVER_VENDOR_AWS),
	/** 微软云 */
	Azure(2, BaseI18nKey.SERVER_VENDOR_AZURE),
	;

	public final Integer value;
	final ValueProxy<ServerVendor, Integer> proxy;

	ServerVendor(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final ServerVendor[] CACHE = values();

	public static ServerVendor of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, +1);
	}

}
