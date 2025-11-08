package com.bojiu.context.model;

import javax.annotation.Nullable;

import com.bojiu.common.context.I18N;
import com.bojiu.common.model.ValueProxy;
import lombok.Getter;

@Getter
public enum RiskType implements LabelI18nProxy<RiskType, Integer> {
	/** 站点余额 */
	SITE_BALANCE(BaseI18nKey.RISK_TYPE_MERCHANT_BALANCE, BaseI18nKey.RISK_TYPE_MERCHANT_BALANCE_DESC),
	/** 最大透支倍数 */
	MAX_OVERDRAFT(BaseI18nKey.RISK_TYPE_MAX_OVERDRAFT, BaseI18nKey.RISK_TYPE_MAX_OVERDRAFT_DESC),
	/** 正常状态 */
	OK(BaseI18nKey.RISK_TYPE_OK, BaseI18nKey.RISK_TYPE_OK_DESC),
	/** 预警状态 */
	WARN(BaseI18nKey.RISK_TYPE_WARN, BaseI18nKey.RISK_TYPE_WARN_DESC),
	/** 后台限制 */
	BACKGROUND_LIMIT(BaseI18nKey.RISK_TYPE_BACKGROUND_LIMIT, BaseI18nKey.RISK_TYPE_BACKGROUND_LIMIT_DESC),
	/** 禁止游戏 */
	PROHIBIT_GAMES(BaseI18nKey.RISK_TYPE_PROHIBIT_GAMES, BaseI18nKey.RISK_TYPE_PROHIBIT_GAMES_DESC);

	public final Integer value;
	public final String desc;
	final ValueProxy<RiskType, Integer> proxy;

	// 【注意】这里的枚举值不能随便添加或删除，否则风控检测可能会出现异常，一定要同步检查、测试 MerchantRiskService 及相关元数据配置！！！

	RiskType(String label, String desc) {
		this.value = ordinal();
		this.desc = desc;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final RiskType[] CACHE = values();

	public String renderDesc(@Nullable Object val) {
		return I18N.msg(desc).replace("{}", val == null ? "0" : val.toString());
	}

	public static RiskType of(@Nullable Integer value) {
		return SITE_BALANCE.getValueOf(value);
	}

	public RiskStatus convertRiskStatus() {
		return switch (this) { // 风控类型 对应的 目标站点状态
			case PROHIBIT_GAMES -> RiskStatus.PROHIBIT_GAMES;
			case BACKGROUND_LIMIT -> RiskStatus.BACKGROUND_LIMIT;
			case WARN -> RiskStatus.WARN;
			case OK -> RiskStatus.OK;
			case SITE_BALANCE, MAX_OVERDRAFT -> null;
		};
	}

}