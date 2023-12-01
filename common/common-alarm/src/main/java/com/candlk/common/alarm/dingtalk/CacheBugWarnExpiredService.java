package com.candlk.common.alarm.dingtalk;

import org.springframework.cache.CacheManager;

/**
 * 基于 Cache 内存防重复
 */
public class CacheBugWarnExpiredService implements BugWarnExpiredService {

	final CacheManager cacheManager;

	public CacheBugWarnExpiredService(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Override
	public boolean canSend(String uniqueKey) {
		return cacheManager.getCache(CACHE_NAME).putIfAbsent(uniqueKey, uniqueKey) == null;
	}

}
