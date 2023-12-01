package com.candlk.common.validator;

import java.lang.annotation.*;

@Documented
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Check {

	String value() default "";

	boolean required() default true;

	boolean i18n() default true;

}
