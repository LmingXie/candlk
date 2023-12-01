package com.candlk.common.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.candlk.common.validator.IntBool.IntBoolValidator;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(IntBoolValidator.class)
public @interface IntBool {

	class IntBoolValidator extends AbstractRuleValidator<IntBool, Number> {

		@Override
		protected String validateInternal(Number val, ValidateContext context) {
			if (val instanceof Integer || val instanceof Long || val instanceof Short || val instanceof Byte) {
				int v = val.intValue();
				if (v == 1 || v == 0) {
					return null;
				}
			}
			return ValidateError.invalid;
		}

	}

}
