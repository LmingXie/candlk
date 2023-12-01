package com.candlk.common.util;

import java.util.*;
import java.util.function.*;
import javax.annotation.Nullable;

import me.codeplayer.util.Assert;
import me.codeplayer.util.X;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;

public abstract class BeanUtil {

	/**
	 * 复制（浅复制）对象属性
	 *
	 * @param propertyFilter 属性过滤器，返回 false 的属性将不会被复制
	 * @see BeanUtils#copyProperties(Object, Object, String...)
	 */
	public static void copyProperties(Object source, Object target, @Nullable Predicate<String> propertyFilter) throws BeansException {
		BeanUtilsProxy.copyProperties(source, target, propertyFilter);
	}

	/**
	 * 复制（浅复制）对象属性
	 *
	 * @see BeanUtils#copyProperties(Object, Object)
	 */
	public static void copyProperties(Object source, Object target) throws BeansException {
		BeanUtils.copyProperties(source, target);
	}

	/**
	 * 复制（浅复制）对象属性
	 *
	 * @see BeanUtils#copyProperties(Object, Object, String...)
	 */
	public static void copyProperties(Object source, Object target, String... ignoreProperties) throws BeansException {
		BeanUtils.copyProperties(source, target, ignoreProperties);
	}

	/**
	 * 复制（浅复制）对象属性
	 *
	 * @see #copyProperties(Object, Object, Predicate)
	 */
	public static void copyPropertiesOnly(Object source, Object target, String... includeProperties) throws BeansException {
		Assert.isTrue(X.isValid(includeProperties), "Array includeProperties must not be empty");
		copyProperties(source, target, p -> ArrayUtils.contains(includeProperties, p));
	}

	/**
	 * 复制（浅复制）对象属性
	 *
	 * @see BeanUtils#copyProperties(Object, Object, Class)
	 */
	public static void copyProperties(Object source, Object target, Class<?> editable) throws BeansException {
		BeanUtils.copyProperties(source, target, editable);
	}

	/**
	 * 转换集合为指定类型，并复制属性
	 *
	 * @see BeanUtils#copyProperties(Object, Object)
	 */
	public static <R> R copy(@Nullable Object source, Supplier<R> supplier) throws BeansException {
		if (source != null) {
			R target = supplier.get();
			BeanUtils.copyProperties(source, target);
			return target;
		}
		return null;
	}

	/**
	 * 转换集合为指定类型，并复制属性
	 *
	 * @see BeanUtils#copyProperties(Object, Object)
	 */
	public static <R> R copy(@Nullable Object source, R target) throws BeansException {
		if (source != null) {
			BeanUtils.copyProperties(source, target);
		}
		return target;
	}

	/**
	 * 转换集合为指定类型，并复制属性
	 *
	 * @see BeanUtils#copyProperties(Object, Object)
	 */
	public static <T, R> List<R> convertAndCopy(@Nullable List<T> list, Supplier<R> targetSupplier) throws BeansException {
		final int size = X.size(list);
		if (size > 0) {
			ArrayList<R> results = new ArrayList<>(size);
			for (T t : list) {
				R target = targetSupplier.get();
				BeanUtils.copyProperties(t, target);
				results.add(target);
			}
			return results;
		}
		return X.castType(list);
	}

	/**
	 * 按照指定的处理方式，依次替换掉List集合中的所有元素，并返回替换后的List集合
	 */
	public static <T, R> List<R> replaceItems(@Nullable List<T> list, Function<? super T, R> converter) throws BeansException {
		final int size = X.size(list);
		if (size > 0) {
			final ListIterator<T> li = list.listIterator();
			while (li.hasNext()) {
				T old = li.next();
				R val = converter.apply(old);
				li.set(X.castType(val));
			}
		}
		return X.castType(list);
	}

	/**
	 * 按照指定的处理方式，依次替换掉List集合中的所有元素，并复制属性，然后返回替换后的List集合
	 */
	public static <T, R> List<R> replaceAndCopy(@Nullable List<T> list, Supplier<? extends R> supplier) throws BeansException {
		return replaceItems(list, t -> {
			R val = supplier.get();
			BeanUtils.copyProperties(t, val);
			return val;
		});
	}

}
