package com.bojiu.context.model;

import javax.servlet.http.HttpServletRequest;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.context.I18N;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import com.bojiu.context.web.RequestContextImpl;
import com.bojiu.webapp.base.dto.MerchantContext;
import com.bojiu.webapp.base.entity.Merchant;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * 风控状态：-2=禁止游戏；-1=后台限制；0=预警状态；1=正常
 */
@Getter
public enum RiskStatus implements LabelI18nProxy<RiskStatus, Integer> {
	/**
	 * 禁止进入游戏
	 *
	 * @see RiskType#PROHIBIT_GAMES
	 */
	PROHIBIT_GAMES(-2, BaseI18nKey.RISK_STATUS_PROHIBIT, BaseI18nKey.RISK_PROMPT_PROHIBIT_GAMES),
	/**
	 * 目前后台限制，就是商户可以登录，但是不能导出、不能审核提现
	 *
	 * @see RiskType#BACKGROUND_LIMIT
	 */
	BACKGROUND_LIMIT(-1, BaseI18nKey.RISK_STATUS_LIMIT, BaseI18nKey.RISK_PROMPT_BACKGROUND_LIMIT),
	/**
	 * TODO 透支比例超过指定阈值时，站点状态为预警（用不同的颜色显示）
	 *
	 * @see RiskType#WARN
	 */
	WARN(0, BaseI18nKey.RISK_STATUS_WARN, BaseI18nKey.RISK_PROMPT_WARN),
	OK(1, BaseI18nKey.RISK_STATUS_OK);

	@EnumValue
	public final Integer value;
	/** 提示 */
	private final String prompt;
	final ValueProxy<RiskStatus, Integer> proxy;

	RiskStatus(Integer value, String label, String prompt) {
		this.value = value;
		this.prompt = prompt;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	RiskStatus(Integer value, String label) {
		this(value, label, "");
	}

	public static final RiskStatus[] CACHE = values();

	public static RiskStatus of(@Nullable Integer value) {
		return Common.getEnum(CACHE, RiskStatus::getValue, value);
	}

	public boolean assertUnlimited(HttpServletRequest request) {
		return assertUnlimited(RequestContextImpl.getMerchantId(request));
	}

	public boolean assertUnlimited(Long merchantId) {
		I18N.assertTrue(Merchant.isPlatform(merchantId) || this.compareTo(MerchantContext.get(merchantId).getRiskStatus()) < 0, BaseI18nKey.OPS_LIMITED);
		return true;
	}

	public String getPrompt() {
		return I18N.msg(prompt);
	}

	public RiskType convertRiskType() {
		return switch (this) {
			case PROHIBIT_GAMES -> RiskType.PROHIBIT_GAMES;
			case BACKGROUND_LIMIT -> RiskType.BACKGROUND_LIMIT;
			case WARN -> RiskType.WARN;
			case OK -> RiskType.OK;
		};
	}

}