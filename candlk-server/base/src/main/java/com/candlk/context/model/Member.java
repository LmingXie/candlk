package com.candlk.context.model;

import com.candlk.common.context.Env;
import com.candlk.common.model.Bean;
import com.candlk.context.auth.MemberRole;
import com.candlk.context.auth.PermissionException;

public interface Member extends Bean<Long>, MemberRole, WithMerchant {

	Gender getGender();

	/**
	 * 是否有效
	 */
	boolean isValid();

	/**
	 * 获取会话ID
	 */
	default String getSessionId() {
		return null;
	}

	void setSessionId(String sessionId);

	String getUsername();

	String getPassword();

	/** 是否是 （商户 或 平台）员工 */
	default boolean asEmp() {
		return false;
	}

	/** 是否是 平台员工 */
	default boolean asAdmin() {
		return false;
	}

	default void assertSame(Long memberId) {
		if (!getId().equals(memberId)) {
			throw new PermissionException();
		}
	}

	/**
	 * 请【不要】直接引用该常量
	 * 请使用如下方法替代：
	 *
	 * @see #idPrefix()
	 */
	@Deprecated
	String ID_PREFIX = "g";

	static String idPrefix() {
		return idPrefix(Env.CURRENT);
	}

	static String idPrefix(final Env current) {
		return switch (current) {
			case PROD -> ID_PREFIX;
			case TEST -> "t";
			case UAT -> "u";
			case DEV -> "d";
			default -> "c"; // 本地环境
		};
	}

	static Env envByIdPrefix(String prefix) {
		return switch (prefix) {
			case ID_PREFIX -> Env.PROD;
			case "u" -> Env.UAT;
			case "t" -> Env.TEST;
			case "d" -> Env.DEV;
			case "c" -> Env.LOCAL;
			default -> throw new UnsupportedOperationException();
		};
	}

}
