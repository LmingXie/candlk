package com.bojiu.context.model;

import java.util.*;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import com.bojiu.context.brand.FeatureItem;
import com.bojiu.context.i18n.AdminI18nKey;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

/**
 * 登录方式配置：0=账号+密码；1=手机号码+验证码；2=手机号码+密码
 */
@Getter
public enum LoginWayConfig implements LabelI18nProxy<LoginWayConfig, Integer>, FeatureItem<Integer> {

	/** 账号+密码 */
	usernamePwd(0, AdminI18nKey.LOGIN_WAY_CONFIG_REGISTER),
	/** 手机号码+验证码 */
	phoneCaptcha(1, AdminI18nKey.LOGIN_WAY_CONFIG_SMS_CAPTCHA),
	/** 手机号码+密码 */
	phonePwd(2, AdminI18nKey.LOGIN_WAY_CONFIG_PHONE_PWD),
	;
	@EnumValue
	public final Integer value;
	final ValueProxy<LoginWayConfig, Integer> proxy;

	LoginWayConfig(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final LoginWayConfig[] CACHE = values();
	public static final List<Integer> CACHE_VALUE = Common.toList(Arrays.asList(CACHE), LoginWayConfig::getValue);

	public static LoginWayConfig of(Integer value) {
		return phoneCaptcha.getValueOf(value);
	}

	public boolean in(@NonNull Set<Integer> values) {
		return values.contains(this.value);
	}

	@Override
	public String getLabel() {
		return LabelI18nProxy.super.getLabel();
	}

}