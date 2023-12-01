package com.candlk.common.model;

public interface AnySupplier<T> {

	T get() throws Exception;

}
