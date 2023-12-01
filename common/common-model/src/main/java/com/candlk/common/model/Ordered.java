package com.candlk.common.model;

/**
 * 标识可通过 order 字段进行常规排序（按照 <b>降序</b> 排列）
 */
public interface Ordered extends ID {

	Long getOrder();

	void setOrder(Long order);

	/**
	 * 默认的排序值
	 */
	Long ORDER_DEFAULT = 0L;

	/**
	 * 用于标识对应实例一般通过 order 字段进行 <b>升序</b> 排列
	 */
	interface OrderedAsc extends Ordered {

	}

}
