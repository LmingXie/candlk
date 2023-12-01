package com.candlk.common.model;

import lombok.Getter;

@Getter
public enum AuditStatus implements ValueProxyImpl<AuditStatus, Integer> {
	/** 不需要审核 */
	NONE(-99, "不需要审核"),
	/** 终审不通过 */
	REVIEW_FALSE(-2, "复审不通过"),
	/** 审核不通过 */
	VERIFY_FALSE(-1, "初审不通过"),
	/** 待审核 */
	WAIT_VERIFY(0, "待审核"),
	/** 审核通过 */
	VERIFY_TRUE(1, "初审通过"),
	/** 终审通过 */
	REVIEW_TRUE(2, "复审通过"),
	//
	;

	public final Integer value;
	public final ValueProxy<AuditStatus, Integer> proxy;

	AuditStatus(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static AuditStatus[] CACHE = values();

	public static AuditStatus of(Integer value) {
		return WAIT_VERIFY.getValueOf(value);
	}

	public static AuditStatus ofVerify(boolean yesOrNo) {
		return yesOrNo ? VERIFY_TRUE : VERIFY_FALSE;
	}

	public static AuditStatus ofReview(boolean yesOrNo) {
		return yesOrNo ? REVIEW_TRUE : REVIEW_FALSE;
	}

	public static boolean hasReviewed(int status) {
		return status == REVIEW_FALSE.value || status == REVIEW_TRUE.value;
	}

}
