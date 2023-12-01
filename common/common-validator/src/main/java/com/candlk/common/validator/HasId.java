package com.candlk.common.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.candlk.common.model.ID;
import com.candlk.common.validator.HasId.HasIdValidator;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(HasIdValidator.class)
public @interface HasId {

	class HasIdValidator extends AbstractRuleValidator<HasId, ID> {

		@Override
		protected String validateInternal(ID val, ValidateContext context) {
			return val != null && val.hasValidId() ? null : ValidateError.required;
		}

	}

}
