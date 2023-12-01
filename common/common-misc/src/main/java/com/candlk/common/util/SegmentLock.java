package com.candlk.common.util;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * 分段锁工具类，用于获取分段锁所需的监视器对象
 */
public abstract class SegmentLock {

	static Cache<ImmutablePair<String, Object>, Object> cache = Caffeine.newBuilder()
			.initialCapacity(128)
			.expireAfterAccess(1, TimeUnit.MINUTES)
			.build();

	public static Object get(String prefix, Object id) {
		return cache.get(new ImmutablePair<>(prefix, id), k -> id);
	}

	@SuppressWarnings("unchecked")
	public static <T> T get(String prefix, Object id, Function<? super ImmutablePair<String, Object>, ? extends T> function) {
		return (T) cache.get(new ImmutablePair<>(prefix, id), function);
	}

}
