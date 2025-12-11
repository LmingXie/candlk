package com.bojiu.context.model;

import java.util.Set;

import com.bojiu.common.context.Env;
import com.bojiu.common.model.Bean;
import com.bojiu.context.AppRegion;
import com.bojiu.context.auth.MemberRole;
import com.bojiu.context.auth.PermissionException;

public interface Member extends Bean<Long>, MemberRole, WithMerchant {

	Gender getGender();

	Long getTopUserId();

	Long getDealerId();

	default Set<Long> merchantIds() {
		return Set.of(getMerchantId());
	}

	/**
	 * 是否有效
	 */
	boolean valid();

	/**
	 * 获取会话ID
	 */
	default String getSessionId() {
		return null;
	}

	void setSessionId(String sessionId);

	String getUsername();

	String getPassword();

	void setPassword(String password);

	/** 是否是 （商户 或 平台）员工 */
	default boolean asEmp() {
		return false;
	}

	/** 是否所属商户 */
	default boolean asGroup() {
		return false;
	}

	/** 是否是 平台员工 */
	default boolean asAdmin() {
		return false;
	}

	/** 是否可作为商户访客 */
	default boolean asVisitor() {
		return false;
	}

	default void assertSame(Long memberId) {
		if (!getId().equals(memberId)) {
			throw new PermissionException();
		}
	}

	default boolean canAccess(Long merchantId) {
		return getMerchantId().equals(merchantId);
	}

	/**
	 * 检查指定商户ID的用户是否有权访问指定商户归属的对象（平台可以访问商户），否则报错
	 */
	default void assertCanAccess(Long merchantId) {
		if (!canAccess(merchantId)) {
			throw new PermissionException();
		}
	}

	/**
	 * 检查指定商户ID的用户是否有权访问指定商户归属的对象（平台可以访问商户），否则报错
	 */
	default Long assertCanAccess(WithMerchant o) {
		Long merchantId = o.getMerchantId();
		assertCanAccess(merchantId);
		return merchantId;
	}

	/**
	 * 请【不要】直接引用该常量
	 * 请使用如下方法替代：
	 *
	 * @see #idPrefix()
	 */
	@Deprecated
	String ID_PREFIX = AppRegion.inBr() ? "bg" : "ag";

	static String idPrefix() {
		return idPrefix(Env.CURRENT);
	}

	static String idPrefix(final Env current) {
		return switch (current) {
			case PROD -> ID_PREFIX;
			case TEST -> AppRegion.inBr() ? "bt" : "at";
			case UAT -> AppRegion.inBr() ? "bu" : "au";
			case DEV -> "bd";
			default -> "bc"; // 本地环境
		};
	}

	static Env envByIdPrefix(String prefix) {
		return switch (prefix) {
			case "bg", "ag" -> Env.PROD;
			case "bu", "au" -> Env.UAT;
			case "bt", "at" -> Env.TEST;
			case "bd" -> Env.DEV;
			case "bc" -> Env.LOCAL;
			default -> throw new UnsupportedOperationException();
		};
	}

}