package com.bojiu.context.web;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import javax.annotation.Nullable;

import com.bojiu.common.model.ValueEnum;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.core.GenericTypeResolver;
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
		return getCachedConverter(targetType);
	}

	public static <T extends Enum> Converter<String, T> getCachedConverter(Class<T> targetType) {
		return cache.computeIfAbsent(targetType, mappingFunction);
	}

	public static <T extends Enum> T convert(Class<T> targetType, String source) {
		return getCachedConverter(targetType).convert(source);
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

		@Nullable
		final Function<String, T> valueToEnum;

		StringToEnum(final Class<T> enumType) {
			if (ValueEnum.class.isAssignableFrom(enumType)) {  // 根据 ValueEnum.of()下标 或 枚举名称进行映射
				final ValueEnum first = (ValueEnum) enumType.getEnumConstants()[0];
				final Class<?>[] types = GenericTypeResolver.resolveTypeArguments(enumType, ValueEnum.class);
				final Class<?> type = types != null && types.length > 1 ? types[1] : null;
				valueToEnum = str -> {
					if (type == Integer.class) {
						final Integer val = tryParseInt(str);
						if (val != null) {
							return (T) first.getValueOf(val);
						}
					} else if (type == String.class) {
						return (T) first.getValueOf(str);
					}
					return (T) EnumUtils.getEnum(enumType, str);
				};
			} else { // 根据数字下标 或 枚举名称进行映射
				final T[] values = enumType.getEnumConstants();
				valueToEnum = str -> {
					final Integer intVal = tryParseInt(str);
					if (intVal != null) {
						final int i = intVal;
						return values.length > i ? values[i] : null;
					}
					return (T) EnumUtils.getEnum(enumType, str);
				};
			}
		}

		@Nullable
		static Integer tryParseInt(final String source) {
			char ch = source.charAt(0);
			if (ch == '-' && source.length() > 1) {
				ch = source.charAt(1);
			}
			if (ch >= '0' && ch <= '9') {
				try {
					return Integer.parseInt(source);
				} catch (NumberFormatException ignored) {
				}
			}
			return null;
		}

		@Override
		@Nullable
		public T convert(String source) {
			final int length = source.length();
			if (length == 0) {
				// It's an empty enum identifier: reset the enum value to null.
				return null;
			}
			return valueToEnum.apply(source);
		}

	}

}