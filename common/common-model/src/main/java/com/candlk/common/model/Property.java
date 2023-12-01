package com.candlk.common.model;

import java.lang.annotation.*;

@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Property {

	/** 属性（中文）名称 */
	String name() default "";

	/** 描述说明 */
	Class<?> type() default Void.class;

	/** 描述说明 */
	String value() default "";

}
