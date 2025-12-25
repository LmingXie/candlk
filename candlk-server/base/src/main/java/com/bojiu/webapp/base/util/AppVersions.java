package com.bojiu.webapp.base.util;

import java.util.*;

import com.bojiu.common.redis.RedisUtil;
import com.bojiu.common.util.Common;
import com.bojiu.common.web.Client;
import com.bojiu.context.model.RedisKey;
import com.bojiu.context.web.ProxyRequest;
import com.bojiu.context.web.RequestContextImpl;
import me.codeplayer.util.*;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.Nullable;

/**
 * APP版本兼容 辅助工具类
 */
public abstract class AppVersions {

	static final LazyCacheLoader<Map<Client, String>> cache = new TimeBasedCacheLoader<>(EasyDate.MILLIS_OF_MINUTE * 10, AppVersions::getRedisVersion);

	public static final Integer[]
			v_1_0_0 = { 1, 0, 0 },
			v_1_1_0 = { 1, 1, 0 },
			v_1_2_0 = { 1, 2, 0 }
					//
					;

	public static boolean lt(String current, Integer[] target) {
		return compare(current, target) < 0;
	}

	public static boolean le(String current, Integer[] target) {
		return compare(current, target) <= 0;
	}

	public static boolean ge(String current, Integer[] target) {
		return compare(current, target) >= 0;
	}

	public static boolean gt(String current, Integer[] target) {
		return compare(current, target) > 0;
	}

	public static boolean eq(String current, Integer[] target) {
		return compare(current, target) == 0;
	}

	/**
	 * 获取指定客户端类型当前正在审核中的版本
	 */
	@Nullable
	public static String getVerifyVersion(Client client) {
		return cache.get().get(client);
	}

	/**
	 * 检测当前请求客户端的APP 版本是否在上架审核中
	 */
	public static boolean inVerify(@Nullable Client client, @Nullable String version) {
		if (StringUtil.notEmpty(version)) {
			final Map<Client, String> map = cache.get();
			return version.equals(map.get(client));
		}
		return false;
	}

	/**
	 * 检测当前请求客户端的APP 版本是否在上架审核中
	 */
	public static boolean inVerify(@Nullable RequestContextImpl requestContext) {
		return inVerify(requestContext, null);
	}

	/**
	 * 检测当前请求客户端的APP 版本是否在上架审核中
	 *
	 * @param clientOnly 限定必须是指定客户端类型，否则返回 false。如果为 null 表示不限定
	 */
	public static boolean inVerify(@Nullable RequestContextImpl requestContext, final @Nullable Client clientOnly) {
		final Map<Client, String> map = cache.get();
		if (!map.isEmpty()) {
			if (requestContext == null) {
				requestContext = RequestContextImpl.get();
			}
			if (requestContext.getRequest() != null) {
				final Client current = requestContext.getClientInfo().client();
				if (clientOnly == null || current == clientOnly) {
					final String version = map.get(current);
					if (StringUtil.notEmpty(version)) {
						return version.equals(requestContext.getClientInfo().version());
					}
				}
			}
		}
		return false;
	}

	/**
	 * 检测当前请求客户端的APP 版本是否在上架审核中
	 */
	public static boolean inVerify(ProxyRequest q) {
		return inVerify(q.requestContext);
	}

	/**
	 * 获取审核中的APP版本
	 */
	public static Map<Client, String> getRedisVersion() {
		List<String> list = RedisUtil.opsForValue().multiGet(Arrays.asList(
				RedisKey.APP_VERSION_PREFIX + Client.APP_IOS.value,
				RedisKey.APP_VERSION_PREFIX + Client.APP_ANDROID.value
		));
		final String iOS = list.get(0), android = list.get(1);
		if (StringUtil.isEmpty(iOS) && StringUtil.isEmpty(android)) {
			return Collections.emptyMap();
		}
		return Map.of(
				Client.APP_IOS, StringUtil.toString(iOS),
				Client.APP_ANDROID, StringUtil.toString(android)
		);
	}

	static final Map<String, Integer[]> versionMap = Map.of(
			"1.0.0", v_1_0_0,
			"1.1.0", v_1_1_0,
			"1.2.0", v_1_2_0
	);

	/**
	 * 将版本号转为分段的 整数 或 字符串数组
	 */
	public static Object[] version2Parts(String version) {
		if (StringUtil.notEmpty(version)) {
			final Integer[] versions = versionMap.get(version);
			if (versions != null) {
				return versions;
			}
			return version.split("\\.");
		}
		return ArrayUtils.EMPTY_INTEGER_OBJECT_ARRAY;
	}

	/**
	 * 比较版本号
	 */
	public static int compare(String version1, String version2) {
		return compare(version2Parts(version1), version2Parts(version2));
	}

	/**
	 * 比较版本号
	 */
	public static int compare(String version1, Object[] version2Parts) {
		return compare(version2Parts(version1), version2Parts);
	}

	/**
	 * 比较版本号
	 */
	public static int compare(Object[] version1Parts, Object[] version2Parts) {
		return Common.compareVersions(version1Parts, version2Parts);
	}

}