package com.candlk.common.redis;

import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import lombok.Getter;
import me.codeplayer.util.EasyDate;
import org.springframework.data.redis.core.RedisOperations;

/**
 * RedisKey 生命周期设置辅助类
 */
@Getter
public class MultipleRedisKeyLifetimeHelper {

	final ConcurrentHashMap<String, RedisKeyLifetimeHelper> cache = new ConcurrentHashMap<>(4);
	/** 额外的延迟过期时间（单位：毫秒） */
	final int calendarField;
	/** 额外的延迟过期时间（单位：秒） */
	final int extraDelay;

	final transient Function<String, RedisKeyLifetimeHelper> helperBuilder;

	public MultipleRedisKeyLifetimeHelper(int calendarField, int extraDelaySecs) {
		this.calendarField = calendarField;
		this.extraDelay = extraDelaySecs;
		this.helperBuilder = k -> new RedisKeyLifetimeHelper(calendarField, extraDelay);
	}

	/** 默认是以 天 作为单位过期时间 */
	public MultipleRedisKeyLifetimeHelper() {
		this(Calendar.DATE, 0);
	}

	public RedisKeyLifetimeHelper getHelper(String redisKeyPrefix) {
		return cache.get(redisKeyPrefix);
	}

	public RedisKeyLifetimeHelper setHelper(String redisKeyPrefix, RedisKeyLifetimeHelper helper) {
		return cache.put(redisKeyPrefix, helper);
	}

	/**
	 * 在 Hash、Set、ZSet 已完成初始化的情况调用该方法可以确保指定的 Redis key 在有效天数后自动过期
	 * <p>
	 * 【注意】：不可用于连续创建的 Redis key 生命周期存在重合的场景
	 */
	public void handleAfterOps(final RedisOperations<String, ?> redisOps, final String redisKeyPrefix, final String redisKey, final long nowMs, final EasyDate now) {
		final RedisKeyLifetimeHelper helper = cache.computeIfAbsent(redisKeyPrefix, helperBuilder);
		helper.handleAfterOps(redisOps, redisKey, nowMs, now);
	}

	/**
	 * 在 Hash、Set、ZSet 已完成初始化的情况调用该方法可以确保指定的 Redis key 在有效天数后自动过期
	 * <p>
	 * 【注意】：不可用于连续创建的 Redis key 生命周期存在重合的场景
	 */
	public void handleAfterOps(final String redisKeyPrefix, final String redisKey, final long nowMs, final EasyDate now) {
		handleAfterOps(RedisUtil.getStringRedisTemplate(), redisKeyPrefix, redisKey, nowMs, now);
	}

}
