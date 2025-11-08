package com.bojiu.context.model;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.alibaba.fastjson2.TypeReference;
import com.bojiu.context.web.Jsons;
import me.codeplayer.util.StringUtil;
import me.codeplayer.util.X;

public interface SerializedConfig {

	/** 获取配置序列化后的原始字符串 */
	String rawConfig();

	/** 获取已解析的 Java 配置对象（如果尚未解析，将返回 null ） */
	@Nullable
	Object parsedConfig();

	/** 初始化设置 已解析的 Java 配置对象 */
	void initParsedConfig(Object parsedConfig);

	default void clearIfFront(boolean canClear) {
	}

	@Nonnull
	static String serializeValue(Object value) {
		if (value == null) {
			return "";
		} else if (value instanceof String s) {
			return s;
		} else if (value instanceof Number || value instanceof Boolean) {
			return value.toString();
		}
		return Jsons.encodeRaw(value);
	}

	static <S> BiFunction<String, S, String> identity() {
		return (str, s) -> str;
	}

	/**
	 * 将 value 转为 整数
	 */
	default Integer toInteger() {
		return toType(Integer.class, Integer::valueOf);
	}

	/**
	 * 将 value 转为 整数
	 */
	default Long toLong() {
		return toType(Long.class, Long::valueOf);
	}

	/**
	 * 将 value 转为 BigDecimal
	 */
	default BigDecimal toBigDecimal() {
		return toType(BigDecimal.class, BigDecimal::new);
	}

	/**
	 * 将 value 转为 boolean
	 */
	default boolean toBoolean() {
		return toType(Boolean.class, SerializedConfig::parseBoolean);
	}

	static Boolean parseBoolean(String value) {
		if (value == null) {
			return null;
		}
		return switch (value.length()) {
			case 1 -> "1".equals(value) || "T".equals(value) || "Y".equals(value);
			case 2 -> "on".equalsIgnoreCase(value);
			case 3 -> "yes".equalsIgnoreCase(value);
			case 4 -> "true".equalsIgnoreCase(value);
			default -> Boolean.FALSE;
		};
	}

	/**
	 * 使用转换器将字符串形式的原始 value 转为 指定类型的对象
	 */
	default <R> R toType(Class<R> resultType, Function<? super String, R> converter) {
		return parseValue(null, resultType, SerializedConfig.identity(), converter, true);
	}

	@SuppressWarnings("unchecked")
	private <S, T, R> R parseValue(@Nullable S valueRawType, @Nullable Class<R> resultType, BiFunction<String, S, T> parser, @Nullable Function<? super T, R> converter, boolean clearIfFront) {
		final Object val = parsedConfig();
		if (val != null && (resultType == null || resultType.isAssignableFrom(val.getClass()))) {
			return (R) val;
		}
		synchronized (this) {
			final Object newVal = parsedConfig();
			if (newVal != val && newVal != null && (resultType == null || resultType.isAssignableFrom(newVal.getClass()))) {
				return (R) newVal;
			}
			T source = parser.apply(rawConfig(), valueRawType);
			R result = converter == null ? (R) source : converter.apply(source);
			initParsedConfig(result);
			clearIfFront(clearIfFront);
			return result;
		}
	}

	default <T, R> R getParsedValue(Class<T> sourceType, Class<R> targetType, @Nullable Function<? super T, R> converter, boolean clearIfFront) {
		return parseValue(sourceType, targetType, Jsons::parseObject, converter, clearIfFront);
	}

	default <T, R> R getParsedValue(TypeReference<T> typeReference, @Nullable Function<? super T, R> converter, boolean clearIfFront) {
		return parseValue(typeReference, null, Jsons::parseObject, converter, clearIfFront);
	}

	default <T> T getParsedValue(TypeReference<T> typeReference, boolean clearIfFront) {
		return getParsedValue(typeReference, null, clearIfFront);
	}

	default <T, R> R getParsedValue(Class<T> sourceType, Class<R> targetType, @Nullable Function<? super T, R> converter) {
		return getParsedValue(sourceType, targetType, converter, true);
	}

	default <T> T getParsedValue(Class<T> sourceType, @Nullable Function<? super T, T> converter) {
		return getParsedValue(sourceType, sourceType, converter, true);
	}

	default <R> R getParsedValue(Class<R> clazz) {
		return getParsedValue(clazz, clazz, null);
	}

	default <T, R> R getParsedValuesAndConvert(Class<T> sourceType, Class<R> targetType, @Nullable Function<List<T>, R> converter, boolean clearIfFront) {
		return parseValue(sourceType, targetType, SerializedConfig::parseArray, converter, clearIfFront);
	}

	static <T> List<T> parseArray(String json, Class<T> type) {
		return StringUtil.isEmpty(json) ? Collections.emptyList() : Jsons.parseArray(json, type);
	}

	@SuppressWarnings("rawtypes")
	default <T, R, C extends Collection<R>> C getParsedValues(Class<T> sourceType, Class<? extends Collection> targetType, @Nullable Function<List<T>, C> converter, boolean clearIfFront) {
		return getParsedValuesAndConvert(sourceType, X.castType(targetType), converter, clearIfFront);
	}

	static <T> Function<T, T> toFunction(Consumer<T> consumer) {
		return t -> {
			consumer.accept(t);
			return t;
		};
	}

	default <T> List<T> getSortedValues(Class<T> clazz, @Nullable Comparator<T> comparator, boolean clearIfFront) {
		return getParsedValues(clazz, List.class, comparator == null ? null : list -> {
			list.sort(comparator);
			return list;
		}, clearIfFront);
	}

	default <T> List<T> getParsedValues(Class<T> clazz, @Nullable Comparator<T> comparator) {
		return getSortedValues(clazz, comparator, true);
	}

	default <T, R, C extends Collection<R>> C getParsedValues(Class<T> clazz, @Nullable Function<List<T>, C> converter, boolean clearIfFront) {
		return getParsedValues(clazz, Collection.class, converter, clearIfFront);
	}

	default <T> List<T> getParsedValues(Class<T> clazz, boolean clearIfFront) {
		return getParsedValues(clazz, List.class, null, clearIfFront);
	}

	default <T> List<T> getParsedValues(Class<T> clazz) {
		return getParsedValues(clazz, true);
	}

}