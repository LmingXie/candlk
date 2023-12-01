package com.candlk.common.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.candlk.common.validator.Phone.PhoneValidator;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(PhoneValidator.class)
public @interface Phone {

	class PhoneValidator extends AbstractRuleValidator<Phone, String> {

		@Override
		protected String validateInternal(String val, ValidateContext context) {
			return ValidateHelper.PHONE_MATCHER.test(val) ? null : ValidateError.format_invalid;
		}

	}

}
