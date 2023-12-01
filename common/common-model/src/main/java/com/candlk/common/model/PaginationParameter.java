package com.candlk.common.model;

/**
 * 数据分页接口
 *

 * @date 2016年11月21日
 */
public interface PaginationParameter extends java.io.Serializable {

	/** 当属性 {@code size}、 为此值时，则表示【不】限制每页显示记录数 */
	int SIZE_UNLIMITED = -1;
	/** 当属性 {@code current} 为此值时，则表示【不】查询总记录数 */
	int TOTAL_IGNORED = -1;

	/**
	 * 获取当前分页页数（从1开始）
	 */
	int getCurrent();

	/**
	 * 设置当前分页页数（从1开始）
	 */
	void setCurrent(int current);

	/**
	 * 获取每页显示的最大记录数
	 */
	int getSize();

	/**
	 * 设置每页显示的最大记录数
	 */
	void setSize(int size);

	/**
	 * 是否不限制查询的分页记录数
	 */
	static boolean sizeUnlimited(int size) {
		return size == SIZE_UNLIMITED;
	}

	/**
	 * 是否需要查询总记录数
	 */
	static boolean skipTotal(int current) {
		return current != TOTAL_IGNORED;
	}

}
