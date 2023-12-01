package com.candlk.common.validator;

import java.lang.annotation.*;

@Documented
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Rule {

	Class<? extends RuleValidator<? extends Annotation, ?>>[] value();

}
