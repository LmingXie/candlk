package com.candlk.webapp.base.service;

/**
 * 缓存同步服务
 */
public interface CacheSyncService {

	default String getCacheId() {
		Class<?> clazz = getClass();
		// 由于可能存在AOP代理对象，因此可能需要基于后缀来获取类名
		do {
			String className = clazz.getSimpleName();
			if (className.endsWith("Service") || className.endsWith("Impl")) {
				return className;
			}
			clazz = clazz.getSuperclass();
		} while (clazz != Object.class);
		return getClass().getSimpleName();
	}

	/**
	 * 刷新缓存
	 */
	void flushCache(Object... args);

}
