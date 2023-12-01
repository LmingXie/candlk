package com.candlk.common.alarm.dingtalk;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 基于 Redis 或 Local Cache，二选一
 */
@Configuration(proxyBeanMethods = false)
public class AlarmConfig {

	@Bean
	public BugSendService bugSendService(@Value("${warn.service.url}") String url,
	                                     @Value("${warn.service.package:}") String packageName,
	                                     BugWarnExpiredService bugWarnExpiredService) {
		return BugSendFactory.createInstance(url, packageName, bugWarnExpiredService);
	}

	@Configuration(proxyBeanMethods = false)
	@ConditionalOnProperty(name = "warn.service.impl", havingValue = "redis", matchIfMissing = true)
	@ConditionalOnClass(RedisTemplate.class)
	static class Redis {

		@Bean
		public BugWarnExpiredService redisBugWarnExpiredService(org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate) {
			return new RedisBugWarnExpiredService(redisTemplate);
		}

	}

	@ConditionalOnBean(CacheManager.class)
	@ConditionalOnMissingBean(type = "org.springframework.data.redis.core.RedisTemplate")
	static class LocalCache {

		@Bean
		public BugWarnExpiredService cacheBugWarnExpiredService(CacheManager cacheManager) {
			System.err.println("初始化 Ehcache");
			return new CacheBugWarnExpiredService(cacheManager);
		}

	}

}
