package com.candlk.common.util;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

/**
 * 高性能的对象方法获取封装类
 *

 * @date 2015年4月22日
 * @since 1.0
 */
public class PropertyBean {

	/** 是否启用严格模式：如果是严格模式，将启用与JDK完全兼容的 根据 属性名称 查找对应 getter 方法的算法 */
	public static boolean STRICT_MODE = false;

	static final Map<Class<?>, PropertyBean> beanMap = new ConcurrentHashMap<>(32);
	protected Class<?> clazz;
	protected Map<String, Method[]> getterMap = new ConcurrentHashMap<>();

	public PropertyBean(Class<?> clazz) {
		this.clazz = clazz;
	}

	static Function<Object, Class<?>> classHandler;

	public static void setClassHandler(Function<Object, Class<?>> clazzGetter) {
		PropertyBean.classHandler = clazzGetter;
	}

	public static Class<?> getClass(Object entity) {
		Function<Object, Class<?>> getter = classHandler;
		return getter == null ? entity.getClass() : getter.apply(entity);
	}

	/**
	 * 快速获取指定对象的属性值：支持嵌套属性，例如："user.id"、"user.inviteUser.id"
	 *
	 * @since 1.0
	 */
	public static Object getFastProperty(Object bean, String property) {
		return getInstance(getClass(bean)).getProperty(bean, property);
	}

	/**
	 * 获取指定类的 PropertyBean 实例
	 *
	 * @since 1.0
	 */
	public static PropertyBean getInstance(Class<?> clazz) {
		PropertyBean bean = beanMap.get(clazz);
		if (bean == null) {
			bean = new PropertyBean(clazz);
			beanMap.put(clazz, bean);
		}
		return bean;
	}

	/**
	 * 获取指定对象的指定属性值。支持嵌套属性，例如："user.id"、"user.inviteUser.id"
	 *
	 * @since 1.0
	 */
	public Object getProperty(Object bean, String property) throws IllegalArgumentException {
		Method[] getters;
		try {
			getters = getGetters(clazz, property);
		} catch (NoSuchFieldException e) {
			throw new IllegalArgumentException(e);
		}
		if (bean != null) {
			try {
				for (Method getter : getters) {
					bean = getter.invoke(bean);
					if (bean == null) {
						return null;
					}
				}
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new IllegalStateException(e);
			}
		}
		return bean;
	}

	/**
	 * 获取指定类用于获取指定属性名称的 getter 方法名称数组(需要支持嵌套属性，因此返回字符串数组)，例如：<br>
	 * ( clazz, "user.id" ) 将返回 [ getUser(), getId() ]
	 *
	 * @since 1.0
	 */
	protected Method[] getGetters(Class<?> clazz, String property) throws NoSuchFieldException {
		Method[] getters = getterMap.get(property);
		if (getters == null) {
			String[] getterNames = property.split("\\.");
			getters = new Method[getterNames.length];

			for (int i = 0; i < getterNames.length; i++) {
				Method method;
				try {
					method = STRICT_MODE
							? findReadMethodStrictly(clazz, getterNames[i])
							: findReadMethod(clazz, getterNames[i]);
				} catch (NoSuchMethodException | SecurityException e) {
					throw new NoSuchFieldException(property);
				}
				clazz = method.getReturnType();
				getters[i] = method;
			}
			getterMap.put(property, getters);
		}
		return getters;
	}

	/**
	 * 快速的用于查找 JavaBean 属性 getter 的方法。
	 * <p>
	 * 本方法不完全兼容Java SE自带的算法：当 isXxx() 和 getXxx() 同时存在时，本方法只会优先取 getXxx，而不是 isXxx。
	 */
	public static Method findReadMethod(Class<?> clazz, String property) throws NoSuchMethodException, SecurityException {
		final String methodNameSuffix = capitalize(property);
		String getterName = "get".concat(methodNameSuffix);
		try {
			return clazz.getMethod(getterName);
		} catch (NoSuchMethodException | SecurityException e) {
			String isGetterName = "is".concat(methodNameSuffix);
			Method method = clazz.getMethod(isGetterName);
			if (!isBooleanGetter(method)) {
				throw new NoSuchMethodException(clazz.getName() + "." + getterName + "()");
			}
			return method;
		}

	}

	/**
	 * 标准（完全兼容Java自身的算法）的用于查找 JavaBean 属性 getter 的方法
	 */
	public static Method findReadMethodStrictly(Class<?> clazz, String property) throws NoSuchMethodException, SecurityException {
		final String methodNameSuffix = capitalize(property);
		String getterName = "is".concat(methodNameSuffix);
		Method method;
		try {
			method = clazz.getMethod(getterName);
		} catch (NoSuchMethodException | SecurityException e) {
			method = null;
		}
		if (method == null || !isBooleanGetter(method)) {
			getterName = "get".concat(methodNameSuffix);
			method = clazz.getMethod(getterName);
		}
		return method;
	}

	public static boolean isBooleanGetter(Method method) {
		return method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class;
	}

	public static String capitalize(String name) {
		if (name == null || name.length() == 0) {
			return name;
		}
		char first = name.charAt(0);
		char upper = Character.toUpperCase(first);
		if (first == upper) {
			return name;
		}
		final char[] chars = name.toCharArray();
		chars[0] = upper;
		return new String(chars);
	}
}
