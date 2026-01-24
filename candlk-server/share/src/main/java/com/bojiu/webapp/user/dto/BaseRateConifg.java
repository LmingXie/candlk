package com.bojiu.webapp.user.dto;

import java.util.HashMap;
import java.util.Map;

import com.bojiu.common.context.I18N;
import com.bojiu.context.i18n.AdminI18nKey;
import com.bojiu.webapp.base.entity.MetaValue;
import com.bojiu.webapp.user.model.BetProvider;
import lombok.Getter;
import lombok.Setter;

/** 基础返点配置 */
@Setter
@Getter
public class BaseRateConifg implements MetaValue<BaseRateConifg> {

	/** A平台 本金 */
	public Double aPrincipal = 1000D,
	/** A平台充值返奖（%） */
	aRechargeRate = 0D;
	public Map<BetProvider, Double> rebate = new HashMap<>(BetProvider.CACHE.length);

	public BaseRateConifg() {
		for (BetProvider provider : BetProvider.CACHE) {
			rebate.put(provider, 0.02);
		}
	}

	@Override
	public void validate() {
		I18N.assertTrue(aPrincipal >= 1 && aPrincipal <= 100_000_000, AdminI18nKey.VALIDATE_LE, "本金", 100_000_000);
		I18N.assertTrue(aRechargeRate >= 0 && aRechargeRate < 100, AdminI18nKey.VALIDATE_LE, "平台充值返水", 100);
		for (BetProvider provider : BetProvider.CACHE) {
			final Double rate = rebate.get(provider);
			I18N.assertTrue(rate != null && rate >= 0 && rate < 100, AdminI18nKey.VALIDATE_LE, provider + "平台返水", 100);
		}
	}

	@Override
	public BaseRateConifg init(BaseRateConifg value) {
		return value;
	}

	public static BaseRateConifg defaultCfg() {
		return new BaseRateConifg();
	}

}
