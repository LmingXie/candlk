package com.candlk.common.model;

import java.io.Serializable;

/**
 * 标识具有范围区间特性的实体接口<br>
 * 该接口是否遵循Java源代码通行的开闭原则（即：包含起始点，不包含结束点）取决于具体实现类的 {@link #endClosed()} 返回值。
 *

 * @date 2015年9月17日
 * @since 1.0
 */
public interface Interval<E> extends Serializable {

	/**
	 * 获取区间起始点(包含)的原始值
	 *
	 * @since 1.0
	 */
	E getBegin();

	/**
	 * 获取区间结束点(不包含)的原始值
	 *
	 * @since 1.0
	 */
	E getEnd();

	/**
	 * 获取处理后的区间起始点(包含)的最终值
	 *
	 * @since 1.0
	 */
	E getFinalBegin();

	/**
	 * 获取处理后的区间结束点的最终值
	 *
	 * @since 1.0
	 */
	E getFinalEnd();

	/**
	 * 指示该区间范围是否包含结束点的边界值
	 *
	 * @since 1.0
	 */
	boolean endClosed();

}
