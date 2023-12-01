package com.candlk.common.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.candlk.common.validator.Optional.OptionalValidator;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(OptionalValidator.class)
public @interface Optional {

	class OptionalValidator implements RuleValidator<Optional, Object> {

		@Override
		public Result validate(Object val, ValidateContext context) {
			if (val == null || (val instanceof String && val.toString().isEmpty())) {
				return Result.YES_BREAK;
			}
			return Result.YES;
		}

		@Override
		public int getOrder() {
			return ORDER_PRE_VALIDATE - 10;
		}

		@Override
		public boolean notNull() {
			return false;
		}
	}

}
