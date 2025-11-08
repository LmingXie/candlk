package com.bojiu.context.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import com.bojiu.common.context.Context;
import com.bojiu.common.context.RequestContext;
import com.bojiu.common.util.Common;
import com.bojiu.context.auth.Permission;
import org.apache.commons.lang3.StringUtils;

/**
 * 系统成员类型
 */
public enum MemberType {

	/** 未知的 */
	UNKNOWN,
	/** 任一成员类型（但需要登录） */
	ANY,
	/** 用户 */
	USER,
	/** 员工（商户 或 管理员） */
	EMP,
	/** 经销商（商户经销商成员） */
	DEALER,
	/** 代理（商户代理成员） */
	AGENT,
	/** 商户（商户成员） */
	MERCHANT,
	/** 商户集团（集团成员） */
	GROUP,
	/** 访客商户 */
	VISITOR,
	/** 管理员（平台成员） */
	ADMIN;

	/** 当前（微服务）应用所服务的成员类型 */
	public static MemberType CURRENT;

	public static MemberType parse(String appName) {
		if (StringUtils.endsWith(appName, Permission.MERCHANT)) {
			return MERCHANT;
		}
		if (StringUtils.endsWith(appName, Permission.AGENT)) {
			return AGENT;
		}
		if (StringUtils.endsWith(appName, Permission.DEALER)) {
			return DEALER;
		}
		if (StringUtils.endsWith(appName, Permission.ADMIN)) {
			return ADMIN;
		}
		return USER;
	}

	public Integer getValue() {
		return this.ordinal();
	}

	public boolean asEmp() {
		return switch (this) {
			case ADMIN, MERCHANT, AGENT, DEALER -> true;
			default -> false;
		};
	}

	public static boolean fromEmp() {
		return CURRENT.asEmp();
	}

	@Nonnull
	public static MemberType parseFrom(HttpServletRequest request) {
		if (CURRENT == null) {
			CURRENT = parse(Context.applicationName());
		}
		if (CURRENT == ADMIN) {
			String appId = RequestContext.getAppId(request);
			return StringUtils.contains(appId, Permission.ADMIN) ? ADMIN : StringUtils.contains(appId, Permission.AGENT) ? AGENT : StringUtils.contains(appId, Permission.DEALER) ? DEALER : MERCHANT;
		}
		return USER;
	}

	public static boolean fromBackstage() {
		return CURRENT == ADMIN;
	}

	public static final MemberType[] CACHE = values();

	public static MemberType of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, 0);
	}

}