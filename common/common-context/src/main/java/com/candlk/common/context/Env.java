package com.candlk.common.context;

import javax.annotation.Nullable;

import lombok.Getter;
import org.apache.commons.lang3.*;

/**
 * 系统运行环境的区分枚举
 *

 * @date 2017年3月23日
 */
@Getter
public enum Env {
	/** 生产环境 */
	PROD(Env.prod, "生产环境"),
	/** 预发布环境 */
	UAT(Env.uat, "预发布环境"),
	/** 测试环境 */
	TEST(Env.test, "测试环境"),
	/** 开发环境（共用开发环境） */
	DEV(Env.dev, "开发环境"),
	/** 本地环境（本地开发环境） */
	LOCAL(Env.local, "本地环境");

	public final String value;
	public final String label;

	Env(String value, String label) {
		this.value = value;
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public static Env of(String value) {
		return EnumUtils.getEnumIgnoreCase(Env.class, value);
	}

	/** 生产环境 */
	public static final String prod = "prod";
	/** 测试环境 */
	public static final String uat = "uat";
	/** 测试环境 */
	public static final String test = "test";
	/** 公共开发环境 */
	public static final String dev = "dev";
	/** 本地开发环境 */
	public static final String local = "local";
	/** 单元测试环境 */
	public static final String unitTest = "unitTest";

	/** 当前的环境类型（默认为生产环境） */
	public static Env CURRENT = PROD;

	public static boolean inLocal() {
		return CURRENT == LOCAL;
	}

	public static boolean inDev() {
		return CURRENT == DEV;
	}

	public static boolean inTest() {
		return CURRENT == TEST;
	}

	/**
	 * 在企业内部环境（非UAT、非正式环境）
	 */
	public static boolean inner() {
		return !outer();
	}

	public static boolean inUat() {
		return CURRENT == UAT;
	}

	public static boolean inProduction() {
		return CURRENT == PROD;
	}

	/**
	 * 在企业外部环境（UAT、正式环境）
	 */
	public static boolean outer() {
		return CURRENT == PROD || CURRENT == UAT;
	}

	/**
	 * 基于指定的环境 profile 配置初始化 Env
	 */
	public static Env init(@Nullable final String[] activeProfiles) {
		Env current = CURRENT;
		if (ArrayUtils.isNotEmpty(activeProfiles)) {
			Env[] envs = Env.values();
			for (Env env : envs) {
				if (StringUtils.containsAnyIgnoreCase(env.value, activeProfiles)) {
					CURRENT = current = env;
					break;
				}
			}
		}
		System.err.println("自动检测到的当前环境为：" + current);
		return current;
	}

}
