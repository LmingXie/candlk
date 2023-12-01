package com.candlk.common.web;

import java.util.Collections;
import java.util.List;
import java.util.function.*;
import javax.annotation.Nullable;

import com.alibaba.fastjson2.annotation.JSONField;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.candlk.common.util.BeanUtil;
import com.candlk.common.util.Common;
import me.codeplayer.util.X;

public class Page<T> extends com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO<T> {

	/** 当属性 <code>size</code> 为此值时，则表示不限制每页显示记录数 */
	public static int SIZE_SKIP_LIMIT = -1;

	public Long offset;

	public Page() {
		maxLimit = 100L;
	}

	public Page(long current, long size) {
		super(current, size);
	}

	public Page(long current, long size, long total) {
		super(current, size, total);
	}

	public List<T> getList() {
		return super.getRecords();
	}

	public Page<T> setList(List<T> list) {
		super.setRecords(list);
		return this;
	}

	public static long[] adjustWithOffsetAndLimit(long current, long size, long defaultSize) {
		long adjustCurrent = Math.max(current, 1L);
		long adjustSize = size < 1 ? defaultSize : size;
		long offset;
		if (size == SIZE_SKIP_LIMIT) {
			offset = 0;
			adjustSize = size;
		} else {
			offset = (adjustCurrent - 1) * adjustSize;
		}
		return new long[] { adjustCurrent, offset, adjustSize };
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <E> Page<E> castList(List<E> list) {
		super.setRecords((List) list);
		return X.castType(this);
	}

	/**
	 * 手动初始化 总记录数
	 */
	public void initTotal(LongSupplier totalCounter) {
		final int listSize = X.size(getList());
		// 当本页记录数 小于 分页数时，满足某些条件，可以直接计算出总数
		if (listSize < size && (current == 1 || listSize > 0)) {
			total = listSize + size * (current - 1);
		} else {
			total = totalCounter.getAsLong();
		}
	}

	@Override
	public boolean hasNext() {
		final int listSize = X.size(getList());
		final long total = getTotal();
		return listSize > 0 && listSize == size && (total == 0 || Math.max(current - 1, 0) * size + listSize < total);
	}

	/** 用于对外输出 */
	public boolean isHasNext() {
		return hasNext();
	}

	@Override
	public long offset() {
		return offset != null ? offset : super.offset();
	}

	public <E> Page<E> transform(final Function<? super T, ? extends E> handler) {
		Common.replaceListValues(getList(), handler);
		return X.castType(this);
	}

	public <E> Page<E> transformAndCopy(final Supplier<? extends E> handler) {
		BeanUtil.replaceAndCopy(getList(), handler);
		return X.castType(this);
	}

	/**
	 * 基于 包含全部数据的集合 构造一个 指定分页 的分页对象
	 */
	public static <T> Page<T> ofAll(@Nullable List<T> all, long current, long size) {
		final int total = X.size(all);
		Page<T> page = new Page<>(current, size, total);
		if (size == SIZE_SKIP_LIMIT || total == 0) {
			page.records = all;
		} else {
			final int offset = (int) (Math.max(current - 1, 0) * (size = size < 1 ? 10 : size));
			page.records = total > offset
					? all.subList(offset, (int) Math.min(offset + size, total))
					: Collections.emptyList();
		}
		return page;
	}

	@JSONField(serialize = false)
	@Override
	public List<T> getRecords() {
		return super.getRecords();
	}

	@JSONField(serialize = false)
	@Override
	public boolean isSearchCount() {
		return super.isSearchCount();
	}

	@Override
	public boolean searchCount() {
		return size != SIZE_SKIP_LIMIT && super.searchCount();
	}

	@JSONField(serialize = false)
	@Override
	public boolean isOptimizeCountSql() {
		return super.isOptimizeCountSql();
	}

	@JSONField(serialize = false)
	@Override
	public boolean isOptimizeJoinOfCountSql() {
		return super.isOptimizeJoinOfCountSql();
	}

	@JSONField(serialize = false)
	@Override
	public List<OrderItem> getOrders() {
		return super.getOrders();
	}

	@JSONField(serialize = false)
	@Override
	public Long getMaxLimit() {
		return super.getMaxLimit();
	}

	@Override
	public Long maxLimit() {
		return size == SIZE_SKIP_LIMIT ? null : maxLimit;
	}

	@Override
	public long getTotal() {
		return total == 0L && size == SIZE_SKIP_LIMIT ? X.size(records) : total;
	}

}
