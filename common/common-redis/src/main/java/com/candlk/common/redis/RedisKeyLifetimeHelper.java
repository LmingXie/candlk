package com.candlk.common.redis;

import java.util.Calendar;
import java.util.Date;

import lombok.Getter;
import me.codeplayer.util.EasyDate;
import org.springframework.data.redis.core.RedisOperations;

/**
 * RedisKey 生命周期设置辅助类
 */
@Getter
public class RedisKeyLifetimeHelper {

	transient volatile long nextUpdateTime;
	/** 额外的延迟过期时间（单位：毫秒） */
	final int calendarField;
	/** 额外的延迟过期时间（单位：秒） */
	final int extraDelay;

	public RedisKeyLifetimeHelper(int calendarField, int extraDelaySecs) {
		this.calendarField = calendarField;
		this.extraDelay = extraDelaySecs;
	}

	/** 默认是以 天 作为单位过期时间 */
	public RedisKeyLifetimeHelper() {
		this(Calendar.DATE, 0);
	}

	/**
	 * 在 Hash、Set、ZSet 已完成初始化的情况调用该方法可以确保指定的 Redis key 在有效天数后自动过期
	 * <p>
	 * 【注意】：不可用于连续创建的 Redis key 生命周期存在重合的场景
	 */
	public void handleAfterOps(final RedisOperations<String, ?> redisOps, final String redisKey, final long nowMs, final EasyDate now) {
		if (nowMs > nextUpdateTime) {
			// 多节点并发没关系，无非是在瞬时间内多设置几次过期时间
			synchronized (this) {
				if (nowMs > nextUpdateTime) {
					final long old = now.getTime();
					final long endTime = now.endOf(calendarField).getTime();
					if (old != endTime) {
						now.setTime(old);
					}
					redisOps.expireAt(redisKey, new Date(endTime + 1 + extraDelay * 1000L + EasyDate.MILLIS_OF_MINUTE * 3));
					nextUpdateTime = endTime;
				}
			}
		}
	}

	/**
	 * 在 Hash、Set、ZSet 已完成初始化的情况调用该方法可以确保指定的 Redis key 在有效天数后自动过期
	 * <p>
	 * 【注意】：不可用于连续创建的 Redis key 生命周期存在重合的场景
	 */
	public void handleAfterOps(final String redisKey, final long nowMs, final EasyDate now) {
		handleAfterOps(RedisUtil.getStringRedisTemplate(), redisKey, nowMs, now);
	}

}
