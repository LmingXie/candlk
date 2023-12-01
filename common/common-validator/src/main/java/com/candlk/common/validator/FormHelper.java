package com.candlk.common.validator;

import java.util.Collection;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import me.codeplayer.util.Assert;
import me.codeplayer.util.X;

public interface FormHelper {

	/**
	 * 如果参数为 0 则转为 null
	 */
	@Nullable
	static Integer zeroAsNull(@Nullable Integer val) {
		return val != null && val == 0 ? null : val;
	}

	/**
	 * 如果参数为 0 则转为 null
	 */
	@Nullable
	default <T extends Collection<?>> T emptyAsNull(@Nullable T c) {
		return c != null && c.isEmpty() ? null : c;
	}

	/**
	 * 如果参数为 null 则转为 0
	 */
	static Integer nullAsZero(@Nullable Integer val) {
		return val == null ? 0 : val;
	}

	/**
	 * 如果参数为 null 则转为 0
	 */
	static Long nullAsZero(@Nullable Long val) {
		return val == null ? 0L : val;
	}

	/**
	 * 断言指定的参数 > 0
	 */
	default void assertGtZero(Integer val) {
		Assert.isTrue(val > 0);
	}

	/**
	 * 断言指定的参数都 > 0
	 */
	default void assertGtZero(Integer a, Integer b) {
		Assert.isTrue(a > 0 && b > 0);
	}

	/**
	 * 断言指定的参数都 > 0
	 */
	default void assertGtZero(Integer a, Integer b, Integer c) {
		Assert.isTrue(a > 0 && b > 0 && c > 0);
	}

	/**
	 * 断言指定的参数都符合指定的 {@code matcher} 条件
	 */
	default <T> void assertMatchAll(Predicate<T> matcher, T a, T b) {
		Assert.isTrue(matcher.test(a) && matcher.test(b));
	}

	/**
	 * 断言指定的参数都符合指定的 {@code matcher} 条件
	 */
	default <T> void assertMatchAll(Predicate<T> matcher, T a, T b, T c) {
		Assert.isTrue(matcher.test(a) && matcher.test(b) && matcher.test(c));
	}

	/**
	 * 断言两个等价于 boolean 值的条件是互斥（若一个为 true，则另一个必定为 false）的
	 */
	default void assertMutex(boolean a, boolean b, Object error) {
		if (!X.isMutex(a, b)) {
			throw new IllegalArgumentException((String) X.tryUnwrap(error));
		}
	}

}
