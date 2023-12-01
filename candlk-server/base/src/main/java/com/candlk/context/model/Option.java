package com.candlk.context.model;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.candlk.common.model.Bean;
import com.candlk.common.model.ValueProxyImpl;
import com.candlk.common.util.Common;
import lombok.Getter;
import me.codeplayer.util.StringUtil;

@Getter
public class Option<V> implements Serializable {

	public V value;
	public String label;

	public Option() {
	}

	public Option(V value, String label) {
		this.value = value;
		this.label = label;
	}

	public static <T, E> Function<T, Option<E>> converter(Function<? super T, E> valueMapper, Function<? super T, String> labelMapper) {
		return t -> new Option<>(valueMapper.apply(t), labelMapper.apply(t));
	}

	public static <T extends Bean<?>> Function<T, Option<Long>> entityIdConverter(Function<T, String> labelMapper) {
		return t -> new Option<>((Long) t.getId(), labelMapper.apply(t));
	}

	public static List<Option<String>> toMetas(ValueProxyImpl<?, ?>... values) {
		return toMetas(values, null);
	}

	public static List<Option<String>> toMetas(ValueProxyImpl<?, ?>[] values, @Nullable Language language) {
		return Common.toList(Arrays.asList(values), t -> toOption(t, language));
	}

	public static Option<String> of(ValueProxyImpl<?, ?> t) {
		return new Option<>(StringUtil.toString(t.getValue()), t.getLabel());
	}

	public static <T extends ValueProxyImpl<?, ?>> List<Option<String>> toMetas(T[] values, @Nonnull Predicate<? super T> predicate, @Nullable Language language) {
		return Common.toList(Arrays.asList(values), t -> predicate.test(t) ? toOption(t, language) : null, false);
	}

	private static Option<String> toOption(ValueProxyImpl<?, ?> t, @Nullable Language language) {
		return new Option<>(StringUtil.toString(t.getValue()), language == null ? t.getLabel() : language.msg(t.getProxy().label));
	}

	public static List<Option<String>> forCountries() {
		return Common.toList(Arrays.asList(Country.CACHE), t -> {
			final String value = StringUtil.toString(t.getValue());
			return new Option<>(value, t.getLabel() + "/+" + Country.areaCodeOf(value));
		});
	}

	public static Option<String> fromEntry(Map.Entry<?, String> entry) {
		return new Option<>(entry.getKey().toString(), entry.getValue());
	}

}
