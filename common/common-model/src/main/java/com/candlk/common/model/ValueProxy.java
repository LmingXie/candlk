package com.candlk.common.model;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import me.codeplayer.util.EnumUtil;

public class ValueProxy<E extends Enum<E>, V> implements ValueEnum<E, V>, Visible {

	public final State state;
	public final V value;
	public final String label;
	public final E instance;
	/** 初始化后只读 */
	private Map<V, E> valueMapper;
	/** 读写并发，内部Object初始化时为List&lt;ValueProxy&gt，获取时重新设置为ValueProxy[]，并且索引index = ordinal() */
	private static final ConcurrentMap<Class<? extends Enum<?>>, Map<?, ? extends Enum<?>>> mapperCache = new ConcurrentHashMap<>(32, 0.75f, 32);

	@SuppressWarnings("unchecked")
	public ValueProxy(E self, State state, V value, String label) {
		this.instance = self;
		this.state = state;
		this.value = value;
		this.label = label;
		Class<E> clazz = getEnumType();
		Map<V, E> mapper = (Map<V, E>) mapperCache.get(clazz);
		if (mapper == null) {
			mapper = new LinkedHashMap<>();
			mapperCache.put(clazz, mapper);
		}
		mapper.put(value, self);
		valueMapper = mapper;
	}

	public ValueProxy(E self, V value, String label) {
		this(self, State.PUBLIC, value, label);
	}

	public ValueProxy(E self, State state, V value) {
		this(self, state, value, null);
	}

	public ValueProxy(E self, V value) {
		this(self, value, null);
	}

	public static <E extends Enum<E>> ValueProxy<E, Integer> ofOrdinal(E self, State state, String label) {
		return new ValueProxy<>(self, state, self.ordinal(), label);
	}

	public static <E extends Enum<E>> ValueProxy<E, Integer> ofOrdinal(E self, String label) {
		return new ValueProxy<>(self, self.ordinal(), label);
	}

	public static <E extends Enum<E>> ValueProxy<E, Integer> ofOrdinal(E self, State state) {
		return new ValueProxy<>(self, state, self.ordinal(), null);
	}

	public static <E extends Enum<E>> ValueProxy<E, Integer> ofOrdinal(E self) {
		return new ValueProxy<>(self, self.ordinal(), null);
	}

	public V getValue() {
		return value;
	}

	public E getValueOf(V value) {
		return valueMapper.get(value);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T extends Enum<T>> Class<T> getEnumType(Enum<T> instance) {
		// 枚举实例 可能是一个匿名子类实现，因此需要获取实际的枚举类型
		Class<? extends Enum> clazz = instance.getClass();
		while (!clazz.isEnum()) {
			clazz = (Class<? extends Enum>) clazz.getSuperclass();
		}
		return (Class<T>) clazz;
	}

	public Class<E> getEnumType() {
		return getEnumType(instance);
	}

	public State getState() {
		return state;
	}

	public String getLabel() {
		return label;
	}

	/**
	 * 获取指定枚举类中 不低于指定可见性的枚举数组
	 *
	 * @param values 所有的枚举值，如果为null，内部将会自动获取所有的枚举值
	 */
	public static <E extends Enum<? extends Visible>> E[] getVisibleEnums(final Class<E> enumClass, final E[] values, final State minVisibility) {
		return EnumUtil.getMatched(enumClass, values, val -> ((Visible) val).getState().compareTo(minVisibility) >= 0);
	}

	static final Map<Class<?>, Object> arrayCache = new ConcurrentHashMap<>();

	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> T[] getCachedArray(Class<T> clazz, final Supplier<T[]> supplier) {
		T[] cached = (T[]) arrayCache.get(clazz);
		if (cached == null && supplier != null) {
			arrayCache.put(clazz, cached = supplier.get());
		}
		return cached;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Enum<T>> T[] getCachedAll(Class<T> clazz) {
		T[] cached = (T[]) arrayCache.get(clazz);
		if (cached == null) {
			arrayCache.put(clazz, cached = clazz.getEnumConstants());
		}
		return cached;
	}

	public static <T extends Enum<T>> T[] cacheArray(Class<T> clazz, T[] array) {
		arrayCache.put(clazz, array);
		return array;
	}

}
