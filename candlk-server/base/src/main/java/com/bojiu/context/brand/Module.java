package com.bojiu.context.brand;

import lombok.Getter;

/**
 * 系统模块（一级功能）
 */
public enum Module {
	/** 后台皮肤 */
	BG_SKIN("后台皮肤", true),
	/** 语言 */
	LANGUAGE("语言", true),
	/** 厂商游戏 */
	GAME("厂商游戏", true),
	/** 支付通道 */
	PAYMENT("支付通道", true),
	/** 站点配置 */
	SITE_CONFIG("站点配置", true),
	/** 运营中心 */
	OPERATION("运营中心", false),
	/** 用户管理 */
	USER("用户管理", false),
	/** 代理中心 */
	AGENT("代理中心", false),
	/** 财务中心 */
	FINANCIAL("财务中心", false),
	/** 风控中心 */
	RISK_CONTROL("风控中心", false),
	/** 抽成模式 */
	COMMISSION_MODE("抽成模式", true),
	//
	;

	Module(String label, boolean required) {
		this.label = label;
		this.required = required;
	}

	/** 模块或功能特性名称 */
	public final String label;
	/** 是否是必需启用的模块（至少勾选其中一个功能特性，哪些是必选的由其下的 {@link Feature} 决定） */
	@Getter
	public final boolean required;

}