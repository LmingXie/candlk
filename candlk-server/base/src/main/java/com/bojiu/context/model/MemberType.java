package com.bojiu.context.model;

import javax.servlet.http.HttpServletRequest;

import com.bojiu.common.context.Context;
import com.bojiu.common.context.RequestContext;
import com.bojiu.common.util.Common;
import com.bojiu.context.auth.Permission;
import com.bojiu.context.web.ClientInfo;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
	/** 【代打平台】用户 */
	WORKER,
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
		if (appName != null) {
			if (appName.endsWith(Permission.MERCHANT)) {
				return MERCHANT;
			}
			if (appName.endsWith(Permission.AGENT)) {
				return AGENT;
			}
			if (appName.endsWith(Permission.DEALER)) {
				return DEALER;
			}
			if (appName.endsWith("work")) {
				return WORKER;
			}
			if (appName.endsWith(Permission.ADMIN)) {
				return ADMIN;
			}
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

	@NonNull
	public static MemberType parseFrom(HttpServletRequest request) {
		if (CURRENT == null) {
			CURRENT = parse(Context.applicationName());
		}
		if (CURRENT == ADMIN) {
			String name = ClientInfo.of(RequestContext.getAppId(request)).name();
			if (name != null) {
				if (name.lastIndexOf(Permission.MERCHANT) != -1) {
					return MERCHANT;
				} else if (name.lastIndexOf(Permission.ADMIN) != -1) {
					return ADMIN;
				} else if (name.lastIndexOf(Permission.AGENT) != -1) {
					return AGENT;
				} else if (name.lastIndexOf(Permission.DEALER) != -1) {
					return DEALER;
				}
			}
			return MERCHANT;
		}
		return CURRENT;
	}

	/** 返回当前用户类型所归属的终端家族：前台游戏用户（ USER ）、代打平台用户（ WORKER ）、后台用户（ ADMIN ） */
	public final MemberType family() {
		return switch (this) {
			case USER -> USER;
			case WORKER -> WORKER;
			default -> ADMIN;
		};
	}

	public static boolean fromBackstage() {
		return CURRENT == ADMIN;
	}

	/** 是否是 【代打平台】服务 */
	public static boolean worker() {
		return CURRENT == WORKER;
	}

	public static final MemberType[] CACHE = values();

	public static MemberType of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, 0);
	}

}