package com.candlk.context.model;

import com.candlk.context.auth.Permission;
import org.apache.commons.lang3.StringUtils;

/**
 * 系统成员类型
 */
public enum MemberType {

	/** 用户 */
	NONE,
	/** 用户 */
	USER,
	/** 员工（商户 或 管理员） */
	EMP,
	/** 商户（商户成员） */
	MERCHANT,
	/** 管理员（平台成员） */
	ADMIN;

	/** 当前（微服务）应用所服务的成员类型 */
	public static MemberType CURRENT;

	public static MemberType parse(String appName) {
		if (StringUtils.endsWith(appName, Permission.ADMIN)) {
			return MemberType.ADMIN;
		}
		if (StringUtils.endsWith(appName, Permission.MERCHANT)) {
			return MemberType.MERCHANT;
		}
		return MemberType.USER;
	}

	public boolean asEmp() {
		return this == MemberType.MERCHANT || this == MemberType.ADMIN;
	}

}
