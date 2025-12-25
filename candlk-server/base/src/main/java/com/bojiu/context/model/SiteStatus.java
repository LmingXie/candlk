package com.bojiu.context.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

import static com.bojiu.context.model.BaseI18nKey.*;

/**
 * 站点状态：-99=注销；-5=冻结；-3=结账；-1=维护；0=建设中；1=正常
 */
@Getter
public enum SiteStatus implements LabelI18nProxy<SiteStatus, Integer> {
	/** 限制商户、玩家登录 */
	CLOSED(-99, SITE_STATUS_CLOSE),
	/** 限制商户、玩家登录 */
	FROZEN(-5, SITE_STATUS_FROZEN),
	/** 限制玩家登录、后台只能访问商户信息、商户账单 */
	CLOSE_ACCOUNTS(-3, SITE_STATUS_CLOSE_ACCOUNTS),

	/** 限制玩家登录 */
	MAINTAIN(-1, SITE_STATUS_MAINTAIN),

	/** 前台也可以正常访问，但是不能登录注册、后台也不能添加会员（相当于仅允许访问首页） */
	INIT(0, SITE_STATUS_INIT),
	/** 正常使用 */
	OK(1, SITE_STATUS_OK);

	@EnumValue
	public final Integer value;
	final ValueProxy<SiteStatus, Integer> proxy;

	SiteStatus(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final SiteStatus[] CACHE = values();
	/** 总台修改商户站点状态 */
	public static final SiteStatus[] PLATFORM_GROUP = { OK, MAINTAIN, CLOSE_ACCOUNTS, FROZEN, CLOSED };
	/** 商户修改自己的站点状态 */
	public static final SiteStatus[] MERCHANT_GROUP = { OK, MAINTAIN };

	/**
	 * 冻结 状态 和 注销状态 时 无需风控
	 */
	public boolean ignoreRiskCheck() {
		return this == CLOSED || this == FROZEN;
	}

	public static SiteStatus of(@Nullable Integer value) {
		return Common.getEnum(CACHE, SiteStatus::getValue, value);
	}

}