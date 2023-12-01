package com.candlk.context.auth;

import java.lang.annotation.*;

/**
 * 菜单注解
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Menu {

	/**
	 * 菜单名称
	 */
	String name();

	/**
	 * 菜单参数。例如：<code> {"status", "1", "type", "1"} </code>
	 */
	String[] args() default {};

}
