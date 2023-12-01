package com.candlk.common.util;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.candlk.common.context.Context;
import com.candlk.common.model.*;
import me.codeplayer.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class Common {

	public static final String SEP = ",";
	public static final char CHAR_SEP = ',';
	public static final String LINE_SEP = "-";

	static Predicate<?> nonNull = Objects::nonNull;

	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> nonNull() {
		return (Predicate<T>) nonNull;
	}

	/**
	 * 将指定的URL尽可能解析为相对根目录的URL
	 */
	public static String parseToRelativeURL(@Nullable String url) {
		if (StringUtil.isEmpty(url)) {
			return "#";
		}
		return StringUtils.removeStart(url, Context.nav().getSiteURL());
	}

	/**
	 * 按照指定的处理方式，依次替换掉List集合中的所有元素，并返回替换后的List集合
	 */
	public static <E, T> List<T> replaceListValues(@Nullable List<E> list, final Function<? super E, ? extends T> handler) {
		if (list != null && !list.isEmpty()) {
			Objects.requireNonNull(handler);
			final ListIterator<E> li = list.listIterator();
			while (li.hasNext()) {
				E old = li.next();
				T val = handler.apply(old);
				li.set(X.castType(val));
			}
		}
		return X.castType(list);
	}

	/**
	 * 将以指定分隔字符分隔字符串，并将每个部分转换为数字
	 */
	public static <E> List<E> split(@Nullable final String ids, final char sep, Function<? super String, E> mapper, boolean ignoreEmpty) {
		if (StringUtil.notEmpty(ids)) {
			final List<E> list = new ArrayList<>();
			int pos, start = 0;
			// ",,"
			while ((pos = ids.indexOf(sep, start)) != -1) {
				String part = start == pos ? "" : ids.substring(start, pos);
				start = pos + 1;
				if (ignoreEmpty && part.isEmpty()) {
					continue;
				}
				final E val = mapper.apply(part);
				if (val != null || !ignoreEmpty) {
					list.add(val);
				}
			}
			if (start <= ids.length()) {
				String part = start == ids.length() ? "" : ids.substring(start);
				if (!ignoreEmpty || StringUtil.notEmpty(part)) {
					final E val = mapper.apply(part);
					if (val != null || !ignoreEmpty) {
						list.add(val);
					}
				}
			}
			return list;
		}
		return null;
	}

	/**
	 * 将以指定分隔字符','分隔字符串，并将每个部分转换为数字
	 */
	public static List<Long> splitAsLongList(final String ids) {
		return split(ids, CHAR_SEP, Long::valueOf, true);
	}

	/**
	 * 将以指定分隔字符','分隔字符串，并将每个部分转换为数字
	 */
	public static List<Integer> splitAsIntList(final String ids) {
		return split(ids, CHAR_SEP, Integer::valueOf, true);
	}

	/**
	 * 将以指定分隔字符','分隔字符串，并将每个部分转换为字符串
	 */
	public static List<String> splitAsStringList(@Nullable final String strs) {
		return split(strs, CHAR_SEP, String::valueOf, true);
	}

	/**
	 * 将以指定分隔字符','分隔字符串，并将每个部分转换为数字
	 */
	public static Long[] splitAsLongs(@Nullable final String ids) {
		return ArrayUtil.toArray(split(ids, CHAR_SEP, Long::valueOf, true), Long.class);
	}

	/**
	 * 将以指定分隔字符','分隔字符串，并将每个部分转换为数字
	 */
	public static Integer[] splitAsInts(@Nullable final String ids) {
		return ArrayUtil.toArray(split(ids, CHAR_SEP, Integer::valueOf, true), Integer.class);
	}

	public static Long[] toLongIds(Collection<? extends Bean<Long>> list) {
		return ArrayUtil.toArray(list, Long.class, Bean::getId);
	}

	/**
	 * 返回从指定集合过滤并映射后的新集合
	 */
	public static <T, R> List<R> filterAndMap(@Nullable Collection<T> list, final Predicate<? super T> filter, final Function<? super T, R> mapper) {
		final int size = X.size(list);
		if (size > 0) {
			List<R> result = null;
			for (T t : list) {
				if (filter.test(t)) {
					if (result == null) {
						result = size > 10 ? new ArrayList<>() : new ArrayList<>(size);
					}
					result.add(mapper.apply(t));
				}
			}
			if (result != null) {
				return result;
			}
		}
		return Collections.emptyList();
	}

	public static Integer[] toIntIds(Collection<? extends Bean<Integer>> list) {
		return ArrayUtil.toArray(list, Integer.class, Bean::getId);
	}

	/**
	 * 检测实体的指定属性是否发生了变更（如果该实体是新增的，也直接视为变更）
	 */
	public static <T extends ID, R> boolean isFieldChanged(@Nullable T old, Function<T, R> getter, R newVal) {
		return old == null || old.getId() == null || !Objects.equals(newVal, getter.apply(old));
	}

	/**
	 * 校验数值是否在指定的范围区间之内
	 *
	 * @param d 指定的数值
	 * @param begin '(' 或 '['
	 * @param min 最小值
	 * @param max ')' 或 ']'
	 * @param end 最大值
	 */
	public static boolean checkRange(BigDecimal d, char begin, BigDecimal min, BigDecimal max, char end) {
		if (d == null) {
			return false;
		}
		// begin
		int cmp = d.compareTo(min);
		boolean match = switch (begin) {
			case '[' -> cmp >= 0; // 期望 d > min
			case '(' -> cmp > 0; // 期望 d >= min
			default -> throw new UnsupportedOperationException();
		};
		if (!match) {
			return false;
		}
		// end
		cmp = d.compareTo(max);
		match = switch (end) {
			case ']' -> cmp <= 0; // 期望 d <= max
			case ')' -> cmp < 0; // 期望 d < max
			default -> throw new UnsupportedOperationException();
		};
		return match;
	}

	/**
	 * 校验数值是否在指定的范围区间之内
	 *
	 * @param d 指定的数值
	 * @param begin '(' 或 '['
	 * @param min 最小值
	 * @param max ')' 或 ']'
	 * @param end 最大值
	 */
	public static boolean checkRange(BigDecimal d, char begin, int min, int max, char end) {
		if (d == null) {
			return false;
		}
		return checkRange(d, begin, Arith.toBigDecimal(min), Arith.toBigDecimal(max), end);
	}

	public static <E> List<E> toList(Collection<E> collection) {
		if (collection == null) {
			return null;
		}
		if (collection instanceof List) {
			return (List<E>) collection;
		}
		return new ArrayList<>(collection);
	}

	public static <E, R> List<R> toList(Collection<E> c, Function<? super E, R> converter, boolean allowNull) {
		if (c == null) {
			return null;
		}
		List<R> list = new ArrayList<>(c.size());
		for (E t : c) {
			R val = converter.apply(t);
			if (allowNull || val != null) {
				list.add(val);
			}
		}
		return list;
	}

	public static <E, R> List<R> toList(Collection<E> c, Function<? super E, R> converter) {
		return toList(c, converter, true);
	}

	public static <K extends Serializable, T extends Bean<K>> T findAnyById(final Collection<T> range, final K idToFind) {
		return CollectionUtil.findFirst(range, t -> t.getId().equals(idToFind));
	}

	public static <K extends Serializable, T extends Bean<K>> boolean containsById(final Collection<T> range, final K idToFind) {
		return findAnyById(range, idToFind) != null;
	}

	/**
	 * 指示是否匹配指定的条件
	 *
	 * @return {@code base && a || !base && b }
	 */
	public static boolean matchOr(final boolean base, Supplier<Boolean> a, Supplier<Boolean> b) {
		return base && a.get() || !base && b.get();
	}

	/**
	 * 指示是否匹配指定的条件
	 *
	 * @return {@code base && a || !base && b }
	 */
	public static boolean matchOr(final boolean base, boolean a, boolean b) {
		return base && a || !base && b;
	}

	/**
	 * 把参数解析为map的形式<br>
	 * 如：<br>
	 * name=cs_file&phone=13987654321<br>
	 * 解析为：<br>
	 * {name=cs_file, phone=13987654321}
	 */
	@Nonnull
	public static Map<String, String> decodeParamAsMap(String queryString) {
		if (StringUtil.isEmpty(queryString)) {
			return Collections.emptyMap();
		}
		String[] params = StringUtils.split(queryString, '&');
		Map<String, String> map = CollectionUtil.newHashMap(params.length);
		for (String param : params) {
			int index = param.indexOf('=');
			map.put(param.substring(0, index), param.substring(index + 1));
		}
		return map;
	}

	/**
	 * 将集合的指定属性或输出拼接为字符串
	 *
	 * @param delimiter 分隔符
	 */
	public static <E> String join(Collection<E> c, Function<E, Object> getter, String delimiter) {
		return StringUtil.join(c, (sb, t) -> sb.append(getter.apply(t)), delimiter);
	}

	/**
	 * 将 整数集合 拼接为字符串
	 *
	 * @param delimiter 分隔符
	 */
	public static String join(Collection<? extends Number> c, String delimiter) {
		return StringUtil.join(c, (sb, t) -> sb.append(t.longValue()), delimiter);
	}

	/**
	 * 将集合的指定属性或输出拼接为字符串
	 *
	 * @param delimiter 分隔符
	 */
	public static <T extends Bean<? extends Number>> String joinId(Collection<T> c, String delimiter) {
		return StringUtil.join(c, (sb, t) -> sb.append(t.getId().longValue()), delimiter);
	}

	/**
	 * 将集合的指定属性或输出拼接为字符串
	 *
	 * @param delimiter 分隔符
	 */
	public static <T> String joinId(Collection<T> c, ToLongFunction<? super T> idMapper, String delimiter) {
		return StringUtil.join(c, (sb, t) -> sb.append(idMapper.applyAsLong(t)), delimiter);
	}

	/**
	 * 将集合的指定属性或输出拼接为字符串
	 *
	 * @param delimiter 分隔符
	 */
	public static <T extends Bean<? extends Number>> String joinId(@Nullable T[] array, String delimiter) {
		if (!X.isValid(array)) {
			return "";
		}
		return joinId(Arrays.asList(array), delimiter);
	}

	/**
	 * 比较新旧集合的差异，并分别返回【新增】和【删除】的差异部分
	 */
	public static <T> Pair<LinkedList<T>, LinkedList<T>> diff(@Nonnull Set<T> oldRange, final @Nullable Collection<T> newRange) {
		final LinkedList<T> addList = new LinkedList<>();
		if (X.isValid(newRange)) {
			for (T one : newRange) {
				if (!oldRange.remove(one)) {
					addList.add(one);
				}
			}
		}
		return MutablePair.of(addList, new LinkedList<>(oldRange));
	}

	/**
	 * 从集合中随机取出一个元素
	 */
	@Nullable
	public static <T> T randomOne(@Nullable List<T> list) {
		if (list != null) {
			final int size = list.size();
			if (size > 0) {
				if (size == 1) {
					return list.get(0);
				}
				return list.get(ThreadLocalRandom.current().nextInt(size));
			}
		}
		return null;
	}

	/**
	 * 从集合中随机返回最多 {@code max } 个元素
	 */
	@Nullable
	public static <T> List<T> randomSubList(@Nullable List<T> list, int max, boolean create) {
		if (list != null) {
			final int size = list.size();
			if (size <= max) {
				return create ? new ArrayList<>(list) : list;
			}
			final List<T> result = new ArrayList<>(max);
			final boolean[] indexes = size <= 64 ? new boolean[size] : null;
			final Set<Integer> matched = indexes == null ? new HashSet<>(max, 1F) : null;
			while (result.size() < max) {
				final int index = (int) (Math.random() * size);
				if (indexes != null) {
					if (indexes[index]) {
						continue;
					}
					indexes[index] = true;
				} else {
					if (!matched.add(index)) {
						continue;
					}
				}
				result.add(list.get(index));
			}
			return result;
		}
		return null;
	}

	/**
	 * 返回指定枚举数组中查找其属性为指定值的枚举，如果找不到则返回 null
	 */
	@Nullable
	public static <E extends Enum<?>> E getEnum(E[] range, ToIntFunction<E> propertyGetter, @Nullable Integer value) {
		if (value != null) {
			final int val = value;
			for (E e : range) {
				if (propertyGetter.applyAsInt(e) == val) {
					return e;
				}
			}
		}
		return null;
	}

	/**
	 * 基于 value 值 相对 ordinal 的偏移，获取对应的枚举
	 * 这要求枚举的 value 必须是连续的整数值
	 *
	 * @param ordinalOffset 传入 value 相对 ordinal 的偏移值 <code> (value - ordinal)</code>
	 */
	@Nullable
	public static <E extends Enum<?>> E getEnum(E[] range, @Nullable Integer value, int ordinalOffset) {
		if (value != null) {
			int ordinal = value - ordinalOffset;
			if (ordinal >= 0 && ordinal < range.length) {
				return range[ordinal];
			}
		}
		return null;
	}

	/**
	 * 返回指定枚举数组中查找其属性为指定值的枚举，如果找不到则返回 null
	 */
	@Nullable
	public static <V, E extends ValueEnum<?, V>> E getEnum(E[] range, @Nullable V value) {
		if (value != null) {
			for (E e : range) {
				if (value.equals(e.getValue())) {
					return e;
				}
			}
		}
		return null;
	}

	/**
	 * 比较值 Pair < lowUserId, highUserId >
	 */
	public static Pair<Long, Long> sort(@Nonnull Long userId, @Nonnull Long toUserId) {
		final boolean asc = userId < toUserId;
		return Pair.of(asc ? userId : toUserId, asc ? toUserId : userId);
	}

	public static String sortKey(@Nonnull Long userId, @Nonnull Long toUserId, String sep) {
		if (StringUtil.isEmpty(sep)) {
			sep = LINE_SEP;
		}
		final boolean asc = userId < toUserId;
		return (asc ? userId : toUserId) + sep + (asc ? toUserId : userId);
	}

	/**
	 * 比较版本号
	 *
	 * @param version1 支持 <code>"x.y"</code>、<code>"x.y.z"</code>、<code>"x.y.z.b"</code>
	 * @param version2 支持 <code>"x.y"</code>、<code>"x.y.z"</code>、<code>"x.y.z.b"</code>
	 */
	public static int compareVersions(String version1, String version2) {
		return compareVersions(version1, version2.split("\\."));
	}

	/**
	 * 比较版本号
	 *
	 * @param version1 支持 <code>"x.y"</code>、<code>"x.y.z"</code>、<code>"x.y.z.b"</code>
	 * @param version2Parts 支持 <code> String[]、Integer[] </code> 数组
	 */
	public static int compareVersions(String version1, Object[] version2Parts) {
		return compareVersions(version1.split("\\."), version2Parts);
	}

	/**
	 * 比较版本号
	 *
	 * @param version1Parts 支持 <code> String[]、Integer[] </code> 数组
	 * @param version2Parts 支持 <code> String[]、Integer[] </code> 数组
	 */
	public static int compareVersions(Object[] version1Parts, Object[] version2Parts) {
		int maxLengthOfVersionSplits = Math.max(version1Parts.length, version2Parts.length);

		final Integer[] version2IntParts = version2Parts instanceof Integer[] ? (Integer[]) version2Parts : null;

		for (int i = 0; i < maxLengthOfVersionSplits; i++) {
			int v1 = i < version1Parts.length ? NumberUtil.getInt(version1Parts[i], 0) : 0;
			int v2 = i < version2Parts.length ? version2IntParts == null ? NumberUtil.getInt(version2Parts[i], 0) : version2IntParts[i] : 0;
			int compare = Integer.compare(v1, v2);
			if (compare != 0) {
				return compare;
			}
		}
		return 0;
	}

	public static BigDecimal convertOut(long val, final int scale) {
		return val == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(val).movePointLeft(scale);
	}

	public static BigDecimal convertOut(@Nullable Long val, final int scale) {
		return val == null ? BigDecimal.ZERO : convertOut(val.longValue(), scale);
	}

	public static BigDecimal convertOut(@Nullable Long val, final int scale, BigDecimal defaultVal) {
		return val == null ? null : convertOut(val.longValue(), scale);
	}

	public static long convertIn(@Nullable BigDecimal val, final int scale) {
		return val == null ? 0 : val.movePointRight(scale).longValueExact();
	}

	public static long convertIn(@Nullable Long val, final int scale) {
		if (val == null) {
			return 0L;
		}
		return convertIn(val.longValue(), scale);
	}

	public static long convertIn(long val, int scale) {
		return switch (scale) {
			case 2 -> val * 100L;
			case 0 -> val;
			case 4 -> val * 10000L;
			case 6 -> val * 100_0000L;
			default -> {
				while (scale > 0) {
					val *= 10;
					scale--;
				}
				while (scale < 0) {
					val /= 10;
					scale++;
				}
				yield val;
			}
		};
	}

}
