package com.candlk.context.web;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.IntFunction;
import javax.annotation.Nullable;

import com.candlk.common.model.ValueEnum;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;

/**
 * 增强的 枚举参数 通用转换器工厂：
 * 1. null safe
 * 2. value mismatch safe
 * 3. 如果传入数字，则自动基于 ValueEnum.value （如果实现了 ValueEnum 接口） 或 Enum.ordinal 转换
 * 4. 如果基于字符串，则自动基于 Enum.name 转换
 * <p>
 * 参考 <code> org.springframework.core.convert.support.StringToEnumConverterFactory </code>
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class EnhanceEnumConverterFactory implements ConverterFactory<String, Enum> {

	static final ConcurrentMap<Class<?>, Converter> cache = new ConcurrentHashMap<>();
	static final Function<Class<?>, Converter> mappingFunction = k -> new StringToEnum(getEnumType(k));

	@Override
	public <T extends Enum> Converter<String, T> getConverter(Class<T> targetType) {
		return cache.computeIfAbsent(targetType, mappingFunction);
	}

	public static Class<?> getEnumType(Class<?> targetType) {
		Class<?> enumType = targetType;
		while (enumType != null && !enumType.isEnum()) {
			enumType = enumType.getSuperclass();
		}
		if (enumType == null) {
			throw new IllegalArgumentException("The target type " + targetType.getName() + " does not refer to an enum");
		}
		return enumType;
	}

	static class StringToEnum<T extends Enum> implements Converter<String, T> {

		final Class<T> enumType;
		@Nullable
		final IntFunction<T> intToEnum;

		StringToEnum(Class<T> enumType) {
			this.enumType = enumType;
			if (ValueEnum.class.isAssignableFrom(enumType)) {
				final ValueEnum ve = (ValueEnum) enumType.getEnumConstants()[0];
				intToEnum = i -> (T) ve.getValueOf(i);
			} else {
				final T[] values = enumType.getEnumConstants();
				intToEnum = i -> values.length > i ? values[i] : null;
			}
		}

		@Override
		@Nullable
		public T convert(String source) {
			if (source.isEmpty()) {
				// It's an empty enum identifier: reset the enum value to null.
				return null;
			}
			if (StringUtils.isNumeric(source)) {
				int val = Integer.parseInt(source);
				return intToEnum.apply(val);
			}
			return (T) EnumUtils.getEnum(this.enumType, source);
		}

	}

}
