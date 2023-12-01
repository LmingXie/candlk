package com.candlk.common.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.util.function.Supplier;

import com.candlk.common.validator.Range.RangeValidator;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(RangeValidator.class)
public @interface Range {

	double min() default Double.MIN_VALUE;

	double max() default Double.MAX_VALUE;

	boolean canEqMin() default true;

	boolean canEqMax() default true;

	class RangeValidator extends AbstractRuleValidator<Range, Number> {

		protected Range rule;
		protected Number min, max;

		@Override
		public void init(Range rule) {
			this.rule = rule;
		}

		@Override
		protected String validateInternal(Number val, ValidateContext context) {
			return null;
		}

		@Override
		protected Supplier<String> validateInternal(Number val, String label, ValidateContext context) {
			Class<? extends Number> clazz = val.getClass();
			if (clazz == Integer.class || clazz == Long.class || clazz == Short.class || clazz == Byte.class) {
				if (min == null) {
					min = (long) rule.min();
					max = (long) rule.max();
				}
				final Long v = clazz == Long.class ? (Long) val : val.longValue();
				return checkRange(label, v, (Long) min, (Long) max, rule.canEqMin(), rule.canEqMax());
			}
			if (min == null) {
				min = BigDecimal.valueOf(rule.min());
				max = BigDecimal.valueOf(rule.max());
			}
			BigDecimal v = clazz == BigDecimal.class ? (BigDecimal) val : BigDecimal.valueOf(val.doubleValue());
			return checkRange(label, v, (BigDecimal) min, (BigDecimal) max, rule.canEqMin(), rule.canEqMax());
		}

		public static <T extends Comparable<T>> Supplier<String> checkRange(final String label, final T val, final T min, final T max,
		                                                                    final boolean canEqMin, final boolean canEqMax) {
			int cmp = val.compareTo(min);
			// min
			if (cmp < 0 || (cmp == 0 && !canEqMin)) {
				// return () -> label + (canEqMin ? "不能小于 " : "必须大于 ") + min;
				return new ValidateError(canEqMin ? ValidateError.range_ge_min_invalid : ValidateError.range_gt_min_invalid, label, min);
			}
			// max
			cmp = val.compareTo(max);
			if (cmp > 0 || (cmp == 0 && !canEqMax)) {
				// return () -> label + (canEqMax ? "不能大于 " : "必须小于 ") + max;
				return new ValidateError(canEqMax ? ValidateError.range_le_max_invalid : ValidateError.range_lt_max_invalid, label, max);
			}
			return null;
		}

	}

}
