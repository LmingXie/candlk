package com.candlk.common.context;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import javax.annotation.*;

public interface ExternalConfigProxy {

	ConcurrentMap<String, Object> getExternal();

	@Nullable
	default Object getRaw(String key) {
		return getExternal().get(key);
	}

	@Nullable
	default Object getRaw(ContextEntry key) {
		return getRaw(key.getKey());
	}

	default Object getRaw(String key, Object defaultVal) {
		Object v = getRaw(key);
		return v == null ? defaultVal : v;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	default <T> T get(String key) {
		return (T) getRaw(key);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	default <T> T get(ContextEntry key) {
		return (T) getRaw(key);
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	default <T> T getRequired(String key) {
		return (T) Objects.requireNonNull(getRaw(key));
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	default <T> T getRequired(ContextEntry key) {
		return (T) Objects.requireNonNull(getRaw(key));
	}

	@SuppressWarnings("unchecked")
	default <T> T get(String key, T defaultVal) {
		return (T) getRaw(key, defaultVal);
	}

	@SuppressWarnings("unchecked")
	default <T> T get(ContextEntry key, T defaultVal) {
		return (T) getRaw(key.getKey(), defaultVal);
	}

	default void put(String key, Object val) {
		getExternal().put(key, val);
	}

	default void put(ContextEntry key, Object val) {
		put(key.getKey(), val);
	}

	default void putIfAbsent(String key, Object val) {
		getExternal().putIfAbsent(key, val);
	}

	default void putIfAbsent(ContextEntry key, Object val) {
		putIfAbsent(key.getKey(), val);
	}

	default void put(String key, Object val, boolean override) {
		if (override) {
			put(key, val);
		} else {
			putIfAbsent(key, val);
		}
	}

	default void put(ContextEntry key, Object val, boolean override) {
		put(key.getKey(), val, override);
	}


	default void computeIfAbsent(String key, Function<String, ?> mappingFunction) {
		getExternal().computeIfAbsent(key, mappingFunction);
	}

	default void computeIfAbsent(ContextEntry key, Function<String, ?> mappingFunction) {
		computeIfAbsent(key.getKey(), mappingFunction);
	}

}
