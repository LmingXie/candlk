package com.candlk.common.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import com.candlk.common.validator.Size.SizeValidator;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(SizeValidator.class)
public @interface Size {

	int min() default 0;

	int max() default Integer.MAX_VALUE;

	class SizeValidator extends AbstractRuleValidator<Size, Object> {

		protected Size rule;

		@Override
		public void init(Size rule) {
			this.rule = rule;
		}

		@Override
		protected String validateInternal(Object val, ValidateContext context) {
			return null;
		}

		@Override
		protected Supplier<String> validateInternal(Object val, String label, ValidateContext context) {
			if (val instanceof String) {
				int length = ((String) val).length();
				return makeError(length, label, true);
			} else if (val instanceof Collection) {
				return makeError(((Collection<?>) val).size(), label, false);
			} else if (val instanceof Map) {
				return makeError(((Map<?, ?>) val).size(), label, false);
			} else if (val.getClass().isArray()) {
				return makeError(Array.getLength(val), label, false);
			} else {
				throw new UnsupportedOperationException("参数校验出错，请重新输入！");
			}
		}

		protected Supplier<String> makeError(int val, String label, boolean charOrItem) {
			if (val < rule.min()) {
				// return () -> label + noun + "不能小于 " + rule.min();
				return new ValidateError(charOrItem ? ValidateError.size_string_min_invalid : ValidateError.size_items_min_invalid, label, rule.min());
			}
			if (val > rule.max()) {
				// return () -> label + noun + "不能大于 " + rule.max();
				return new ValidateError(charOrItem ? ValidateError.size_string_max_invalid : ValidateError.size_items_max_invalid, label, rule.max());
			}
			return null;
		}

	}

}
