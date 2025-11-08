package com.bojiu.context.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.model.ValueProxy;
import lombok.Getter;

@Getter
public enum VerifyStatus implements LabelI18nProxy<VerifyStatus, Integer> {

	/** 已驳回 */
	VERIFY_FALSE(-1, BaseI18nKey.VERIFY_FALSE),
	/** 审核中 */
	WAIT_VERIFY(0, BaseI18nKey.WAIT_VERIFY),
	/** 已通过 */
	VERIFY_TRUE(1, BaseI18nKey.VERIFY_TRUE),
	//
	;

	@EnumValue
	public final Integer value;
	public final ValueProxy<VerifyStatus, Integer> proxy;

	VerifyStatus(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static VerifyStatus[] CACHE = values();

	public static VerifyStatus of(Integer value) {
		return WAIT_VERIFY.getValueOf(value);
	}

	public static VerifyStatus ofVerify(boolean yesOrNo) {
		return yesOrNo ? VERIFY_TRUE : VERIFY_FALSE;
	}
}

