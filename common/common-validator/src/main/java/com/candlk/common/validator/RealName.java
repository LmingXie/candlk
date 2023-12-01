package com.candlk.common.validator;

import java.lang.annotation.*;

import com.candlk.common.validator.RealName.RealNameValidator;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(RealNameValidator.class)
public @interface RealName {

	class RealNameValidator extends AbstractRuleValidator<RealName, String> {

		@Override
		protected String validateInternal(String val, ValidateContext context) {
			boolean result = ValidateHelper.REAL_NAME_MATCHER.test(val);
			return result ? null : ValidateError.real_name_invalid;
		}

	}

}
