package com.candlk.common.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.springframework.lang.Nullable;

public class ClassUtil {

	public static final Type[] EMPTY_TYPE = new Type[0];

	/**
	 * 获取当前类 {@code clazz } 上基于指定接口或超类 {@code target } 声明的泛型类型
	 *
	 * @param clazz 指定的当前类
	 * @param target 指定泛型声明所属的的接口或超类类型
	
	 */
	public static Type[] getParameterizedType(Class<?> clazz, Class<?> target) {
		if (clazz == target || !target.isAssignableFrom(clazz)) {
			throw new IllegalArgumentException("Unable to get parameterized type which based [" + target + "] from [" + clazz + ']');
		}
		if (target.isInterface()) { // 如果泛型类型声明在接口上
			Type[] types = clazz.getGenericInterfaces();
			for (Type value : types) {
				ParameterizedType type = (ParameterizedType) value;
				if (type.getRawType() == target) {
					return type.getActualTypeArguments();
				}
			}
			return EMPTY_TYPE;
		} else {
			Class<?> parent = clazz.getSuperclass();
			while (parent != target) {
				parent = (clazz = parent).getSuperclass();
			}
			return ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments();
		}
	}

	public static Object newInstance(Class<?> clazz) throws IllegalArgumentException {
		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public static <T> Class<T> loadClass(String className, boolean errorAsNull) {
		try {
			return (Class<T>) Class.forName(className);
		} catch (ClassNotFoundException e) {
			if (errorAsNull) {
				return null;
			}
			throw new IllegalArgumentException("Unable to load class: " + className, e);
		}
	}

	@Nullable
	public static <T> Class<T> tryLoadClass(String className) {
		return loadClass(className, true);
	}

}
