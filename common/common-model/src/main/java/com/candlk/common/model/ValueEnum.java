package com.candlk.common.model;

/**
 * 用于存储指定类型指定值的枚举所需要实现的接口
 *
 * @param <E> 指定值的类型

 * @date 2015年07月25日
 * @since 1.0
 */
public interface ValueEnum<E extends Enum<E>, V> {

	/**
	 * 获取枚举的 value 值
	 */
	V getValue();

	/**
	 * 根据指定的枚举value值转换为对应的枚举实例
	 */
	Enum<E> getValueOf(V value);
}
