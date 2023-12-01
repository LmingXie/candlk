package com.candlk.common.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.candlk.common.validator.NotEmpty.NotEmptyValidator;
import me.codeplayer.util.StringUtil;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(NotEmptyValidator.class)
public @interface NotEmpty {

	class NotEmptyValidator extends AbstractRuleValidator<NotEmpty, String> {

		@Override
		protected String validateInternal(String val, ValidateContext context) {
			return StringUtil.notEmpty(val) ? null : ValidateError.required;
		}

		@Override
		public int getOrder() {
			return ORDER_PRE_VALIDATE;
		}

	}

}
