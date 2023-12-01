package com.candlk.common.model;

import javax.annotation.Nullable;

public interface ValueProxyImpl<E extends Enum<E>, V> extends ValueEnum<E, V> {

	ValueProxy<E, V> getProxy();

	@Override
	default E getValueOf(V value) {
		return getProxy().getValueOf(value);
	}

	default boolean eq(V val) {
		return getProxy().getValue().equals(val);
	}

	@Override
	default V getValue() {
		return getProxy().value;
	}

	default String getLabel() {
		return getProxy().label;
	}

	static String label(@Nullable ValueProxyImpl<?, ?> t) {
		return t == null ? null : t.getLabel();
	}

	static <V> V value(@Nullable ValueEnum<?, V> t) {
		return t == null ? null : t.getValue();
	}

}
