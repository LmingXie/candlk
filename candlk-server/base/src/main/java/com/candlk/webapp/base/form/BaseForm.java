package com.candlk.webapp.base.form;

import java.util.function.Supplier;

import com.candlk.common.util.BeanUtil;
import com.candlk.common.validator.Form;

public abstract class BaseForm<E> implements Form {

	/** 将数据复制到指定的实体属性中 */
	public E copyTo(E entity) {
		BeanUtil.copyProperties(this, entity);
		return entity;
	}

	public E copyTo(Supplier<E> supplier) {
		return copyTo(supplier.get());
	}

}
