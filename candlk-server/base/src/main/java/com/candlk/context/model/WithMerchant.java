package com.candlk.context.model;

import com.candlk.context.auth.PermissionException;
import com.candlk.context.config.GlobalConfig;

/**
 * 与 商户 关联的实体标识
 */
public interface WithMerchant {

	Long getMerchantId();

	/** 商户 ID 字段名 */
	String MERCHANT_ID = "merchant_id";

	/**
	 * 检查商户ID是否与当前实体一致（否则报错）
	 */
	static void assertSame(Long merchantId, Long oMerchantId) {
		if (!merchantId.equals(oMerchantId)) {
			throw new PermissionException();
		}
	}

	/**
	 * 检查商户ID是否与当前实体一致（否则报错）
	 */
	default void assertSame(Long merchantId) {
		assertSame(getMerchantId(), merchantId);
	}

	/**
	 * 检查所属商户ID是否与当前实体一致（否则报错）
	 */
	default void assertSame(WithMerchant o) {
		assertSame(getMerchantId(), o.getMerchantId());
	}

	/**
	 * 检查指定商户ID的用户是否有权访问指定商户归属的对象（平台可以访问商户），否则报错
	 */
	static void assertCanAccess(Long merchantId, Long inputMerchantId) {
		if ((long) merchantId != GlobalConfig.PLATFORM_MERCHANT_ID && !merchantId.equals(inputMerchantId)) {
			throw new PermissionException();
		}
	}

	/**
	 * 检查指定商户ID的用户是否有权访问指定商户归属的对象（平台可以访问商户），否则报错
	 */
	default void assertCanAccess(Long merchantId) {
		assertCanAccess(getMerchantId(), merchantId);
	}

	/**
	 * 检查指定商户ID的用户是否有权访问指定商户归属的对象（平台可以访问商户），否则报错
	 */
	default void assertCanAccess(WithMerchant o) {
		assertCanAccess(getMerchantId(), o.getMerchantId());
	}

}
