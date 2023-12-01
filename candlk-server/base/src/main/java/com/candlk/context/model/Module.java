package com.candlk.context.model;

import lombok.Getter;
import lombok.Setter;
import me.codeplayer.util.Assert;

/**
 * 系统模块 & 功能特性
 */
public enum Module {
	/** 【首页】模块 */
	INDEX("首页", true),
	/** 【优惠】模块 */
	PROMOTION("优惠", true),
	/** 【VIP】模块 */
	VIP("VIP", true),
	/** 【商城】模块 */
	SHOP("SHOP", true),
	/** 我的 */
	ME("我的", false),
	//
	;

	Module(String name, boolean enabled) {
		this.name = name;
		this.enabled = enabled;
	}

	/** 模块或功能特性名称 */
	public final String name;
	/** 指示启用该模块或功能特性 */
	@Getter
	@Setter
	boolean enabled;

	public void checkEnabled() {
		Assert.isTrue(isEnabled(), "该功能暂未开放");
	}

}
