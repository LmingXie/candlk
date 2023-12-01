package com.candlk.common.context;

import java.util.function.*;

import me.codeplayer.util.*;

public interface ContextEntry {

	String getKey();

	default String getLabel() {
		return null;
	}

	default boolean isRequired() {
		return true;
	}

	default Class<?> getType() {
		return String.class;
	}

	default boolean testValue(Object val) {
		if (getType() != Supplier.class) {
			val = X.tryUnwrap(val);
		}
		if (val == null) {
			return !isRequired();
		}
		return getType().isInstance(val);
	}

	default <T> T getDefaultValue() {
		return null;
	}

	default <T> T get() {
		return Context.get().get(getKey());
	}

	default <T> T getOrDefault(T defaultVal) {
		return Context.get().get(getKey(), defaultVal);
	}

	default <T> T getOrDefault() {
		return Context.get().get(getKey(), getDefaultValue());
	}

	default void set(Object val) {
		Context.get().put(getKey(), val);
	}

	default void set(Object val, boolean override) {
		Context.get().put(getKey(), val, override);
	}

	default void putIfAbsent(Object val) {
		Context.get().putIfAbsent(getKey(), val);
	}

	default void computeIfAbsent(Function<String, ?> mappingFunction) {
		Context.get().computeIfAbsent(getKey(), mappingFunction);
	}

}
