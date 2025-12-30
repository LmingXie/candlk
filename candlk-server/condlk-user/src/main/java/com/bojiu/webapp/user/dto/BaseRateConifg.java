package com.bojiu.webapp.user.dto;

import com.bojiu.common.context.I18N;
import com.bojiu.context.i18n.AdminI18nKey;
import com.bojiu.webapp.base.entity.MetaValue;
import lombok.Getter;
import lombok.Setter;

/** 基础返点配置 */
@Setter
@Getter
public class BaseRateConifg implements MetaValue<BaseRateConifg> {

	/** A平台 本金 */
	public Double aPrincipal = 1000D,
	/** A平台充值返奖（%） */
	aRechargeRate = 0D,
	/** 投注返水比例（%） */
	aRebate = 0.015,
	/** B平台投注返水比例（%） */
	bRebate = 0.025;

	@Override
	public void validate() {
		I18N.assertTrue(aPrincipal >= 1000 && aPrincipal <= 100_000_000, AdminI18nKey.VALIDATE_LE, "本金", 100_000_000);
		I18N.assertTrue(aRechargeRate >= 0 && aRechargeRate < 100, AdminI18nKey.VALIDATE_LE, "平台充值返水", 100);
		I18N.assertTrue(aRebate >= 0 && aRebate < 100, AdminI18nKey.VALIDATE_LE, "串子平台返水", 100);
		I18N.assertTrue(bRebate >= 0 && bRebate < 100, AdminI18nKey.VALIDATE_LE, "对冲平台返水", 100);
	}

	@Override
	public BaseRateConifg init(BaseRateConifg value) {
		return value;
	}

	public static BaseRateConifg defaultCfg() {
		return new BaseRateConifg();
	}

}
