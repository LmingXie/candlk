package com.candlk.common.model;

import java.util.List;

/**
 * 数据分页接口
 *

 * @date 2016年11月21日
 */
public interface Pagination<E> extends PaginationParameter {

	/**
	 * 获取总的记录数
	 */
	int getTotal();

	/**
	 * 设置总的记录数
	 */
	void setTotal(int total);

	/**
	 * 获取分页的总页数
	 */
	int getPageCount();

	/**
	 * 获取当前分页的数据集合
	 */
	List<E> getList();

	/**
	 * 设置当前分页的数据集合
	 */
	void setList(List<E> list);

	/**
	 * 获取修正后的分页参数，即：{@code [ current, size ] }
	 */
	static int[] adjust(int current, int size, final int defaultSize) {
		if (current < 1 && !PaginationParameter.skipTotal(current)) {
			current = 1;
		}
		if (size < 1 && !PaginationParameter.sizeUnlimited(size)) {
			size = defaultSize;
		}
		return new int[]{current, size};
	}

	/**
	 * 获取用于数据库查询所需的分页参数，即：{@code [ current, offset, size (also limit) ] }
	 */
	static int[] adjustForDbQuery(int current, int size, final int defaultSize) {
		int adjustCurrent = Math.max(current, 1);
		int adjustSize = size < 1 ? defaultSize : size;
		int offset;
		if (PaginationParameter.sizeUnlimited(size)) {
			offset = 0;
			adjustSize = size;
		} else {
			offset = (adjustCurrent - 1) * adjustSize;
		}
		return new int[]{adjustCurrent, offset, adjustSize};
	}

}
