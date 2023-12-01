package com.candlk.common.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.candlk.common.validator.IntSet.IntSetValidator;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(IntSetValidator.class)
public @interface IntSet {

	int[] value();

	class IntSetValidator extends AbstractRuleValidator<IntSet, Number> {

		protected Set<Integer> range;

		@Override
		public void init(IntSet rule) {
			int[] values = rule.value();
			range = Arrays.stream(values).boxed().collect(Collectors.toSet());
			if (values.length != range.size()) {
				throw new ValidateException("@IntSet 注解定义有误，不能出现重复的枚举值！");
			}
		}

		@Override
		protected String validateInternal(Number val, ValidateContext context) {
			boolean inSet = false;
			if (val instanceof Integer) {
				inSet = range.contains(val);
			} else if (val instanceof Long || val instanceof Short) {
				inSet = range.contains(val.intValue());
			}
			return inSet ? null : ValidateError.invalid;
		}

	}

}
