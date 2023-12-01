package com.candlk.common.validator;

import java.lang.annotation.*;
import javax.validation.Constraint;
import javax.validation.Payload;

@Documented
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FormValidator.class)
public @interface CheckForm {

	String message() default ValidateError.invalid;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	boolean notNull() default true;

	String label();

}
