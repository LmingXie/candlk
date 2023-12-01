package com.candlk.common.model;

public interface VisibleProxyImpl<E extends Enum<E>, V> extends ValueProxyImpl<E, V>, Visible {

	@Override
	default State getState() {
		return getProxy().state;
	}
}
