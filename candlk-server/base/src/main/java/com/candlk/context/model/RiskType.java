package com.candlk.context.model;

import javax.annotation.Nullable;

import com.candlk.common.model.ValueProxy;
import lombok.Getter;

@Getter
public enum RiskType implements LabelI18nProxy<RiskType, Integer> {
	/** 站点余额 */
	SITE_BALANCE("站点余额", "商户站点U币余额，充值多少就是多少"),
	/** 最大透支额 */
	MAX_OVERDRAFT("最大透支额", "最大透支额=（站点余额*透支倍数）+（站点余额*商户等级加成比例）"),
	/** 正常状态 */
	NORMAL("正常状态", "透支比例 ≤ {}%\n透支比例 =(未结账单-站点余额)/ 最大透支额度 * 100%"),
	/** 预警状态 */
	WARN("预警状态", "透支比例 >{}% 时，站点状态为预警"),
	/** 后台限制 */
	BACKGROUND_LIMIT("后台限制", "透支比例 >{}% 时，站点状态为后台限制，将无法导出会员资料和无法提现审核（含暂停自动出款）"),
	/** 禁止游戏 */
	PROHIBIT_GAMES("禁止游戏", "透支比例 >{}% 时，站点状态为禁止游戏，前台所有会员都无法进入三方游戏，后台所有账号都将踢出，停用全部功能。");

	// 定义私有变量
	public final Integer value;
	public final String desc;
	final ValueProxy<RiskType, Integer> proxy;

	// 构造函数，枚举类型只能为私有
	RiskType(String label, String desc) {
		this.value = ordinal();
		this.desc = desc;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final RiskType[] CACHE = values();

	public String renderDesc(@Nullable Object val) {
		return desc.replace("{}", val == null ? "0" : val.toString());
	}

	public static RiskType of(@Nullable Integer value) {
		return value == null ? null : CACHE[value];
	}

}
