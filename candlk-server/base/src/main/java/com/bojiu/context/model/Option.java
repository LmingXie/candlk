package com.bojiu.context.model;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import com.bojiu.common.model.ValueEnum;
import com.bojiu.common.model.ValueProxyImpl;
import com.bojiu.common.util.Common;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Option<String> asString() {
		Option me = this;
		if (!(value instanceof String)) {
			me.value = asString(value);
		}
		return me;
	}

	public static Option<String> ofString(Integer value, String label) {
		return new Option<>(Common.toString(value), label);
	}

	public static Option<String> ofString(Long value, String label) {
		return new Option<>(Common.toString(value), label);
	}

	public static Option<String> ofString(Object value, String label) {
		return new Option<>(asString(value), label);
	}

	public static <V> Option<V> of(V value, String label) {
		return new Option<>(value, label);
	}

	public static Option<String> ofAnyString(Object value, Object label) {
		return new Option<>(asString(value), asString(label));
	}

	public static List<Option<String>> toMetas(ValueProxyImpl<?, ?>... values) {
		return toMetas(values, null);
	}

	public static List<Option<String>> toMetas(ValueProxyImpl<?, ?>[] values, @Nullable Language language) {
		return Common.toList(Arrays.asList(values), t -> toOption(t, language));
	}

	public static List<Option<String>> toMetas(Collection<ValueProxyImpl<?, ?>> values, @Nullable Language language) {
		return Common.toList(values, t -> toOption(t, language));
	}

	public static <E extends Enum<E> & ValueProxyImpl<E, V>, V extends Serializable> List<Option<String>>
	toMetas(Collection<E> values, @Nullable Predicate<E> filter, @Nullable Language language) {
		final Function<E, Option<String>> mapper = t -> toOption(t, language);
		return filter != null ? Common.filterAndMap(values, filter, mapper)
				: Common.toList(values, mapper);
	}

	public static List<Option<String>> toMetas(Collection<ValueProxyImpl<?, ?>> values) {
		return toMetas(values, null);
	}

	public static <V extends Serializable> Option<V> of(ValueEnum<?, V> t) {
		return new Option<>(t.getValue(), t.getLabel());
	}

	public static <V extends Serializable> Option<String> ofString(ValueEnum<?, V> t) {
		return new Option<>(asString(t.getValue()), t.getLabel());
	}

	public static <T extends ValueProxyImpl<?, ?>> List<Option<String>> toMetas(T[] values, @NonNull Predicate<? super T> filter, @Nullable Language language) {
		return Common.toList(Arrays.asList(values), t -> filter.test(t) ? toOption(t, language) : null, false);
	}

	private static Option<String> toOption(ValueProxyImpl<?, ?> t, @Nullable Language language) {
		return new Option<>(asString(t.getValue()), language == null || !(t instanceof LabelI18nProxy<?, ?>) ? t.getLabel() : language.msg(t.getProxy().label));
	}

	public static String asString(Object value) {
		if (value instanceof Integer val) {
			return Common.toString(val);
		}
		return value == null ? "" : value.toString();
	}

	public static <E extends Enum<E>, V extends Serializable> Option<V> valueOf(ValueEnum<E, V> t) {
		return new Option<>(t.getValue(), t.getLabel());
	}

	public static <E extends Enum<E>> Option<String> nameOf(ValueProxyImpl<E, ?> t) {
		return new Option<>(t.getProxy().instance.name(), t.getLabel());
	}

	public static <E extends Enum<E>> Option<Integer> ordinalOf(ValueProxyImpl<E, ?> t) {
		return new Option<>(t.getProxy().instance.ordinal(), t.getLabel());
	}

	public static <E extends Enum<E>> Option<String> ordinalAsStringOf(ValueProxyImpl<E, ?> t) {
		return new Option<>(Common.toString(t.getProxy().instance.ordinal()), t.getLabel());
	}

	public static <V> Option<V> fromEntry(Map.Entry<V, String> entry) {
		return new Option<>(entry.getKey(), entry.getValue());
	}

	public static <E, V> List<Option<V>> toOptions(Function<? super E, Option<V>> mapper, List<E> items) {
		return Common.toList(items, mapper);
	}

	public static <E, V> List<Option<V>> toOptions(Function<? super E, Option<V>> mapper, E... items) {
		return Common.toList(Arrays.asList(items), mapper);
	}

}