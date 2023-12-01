package com.candlk.common.alarm.dingtalk;

import java.util.concurrent.TimeUnit;

import lombok.Setter;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 基于 Redis 防止消息连续重复发送
 */
@Setter
public class RedisBugWarnExpiredService implements BugWarnExpiredService {

	final RedisTemplate<String, Object> redisTemplate;
	/** 多少s过期 */
	int expireSecond = 60;

	public RedisBugWarnExpiredService(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	public RedisBugWarnExpiredService(RedisTemplate<String, Object> redisTemplate, int expireSecond) {
		this.redisTemplate = redisTemplate;
		this.expireSecond = expireSecond;
		if (expireSecond <= 0) {
			throw new RuntimeException("超时时间设置错误");
		}
	}

	@Override
	public boolean canSend(String uniqueKey) {
		final Boolean can = redisTemplate.opsForValue().setIfAbsent(CACHE_NAME + uniqueKey, uniqueKey, expireSecond, TimeUnit.SECONDS);
		return can != null && can;
	}

}
