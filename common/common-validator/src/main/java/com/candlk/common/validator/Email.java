package com.candlk.common.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(Email.EmailValidator.class)
public @interface Email {

	class EmailValidator extends AbstractRuleValidator<Email, String> {

		@Override
		protected String validateInternal(String val, ValidateContext context) {
			return ValidateHelper.EMAIL_MATCHER.test(val) ? null : ValidateError.email_invalid;
		}

	}

}
