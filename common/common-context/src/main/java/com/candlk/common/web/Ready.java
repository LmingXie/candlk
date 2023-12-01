package com.candlk.common.web;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Ready {

	/**
	 * 页面的标题
	 */
	String value() default "";

	/**
	 * 额外的信息字符串
	 */
	String extra() default "";

	/**
	 * 是否对该接口的输出进行加密
	 */
	boolean encrypt() default false;

}
