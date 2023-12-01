package com.candlk.webapp.base.util;

import java.lang.annotation.*;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Export {

	/** 指示不缓存 */
	String NO_CACHE = "*";
	/** 支持外部自定义扩展字段 */
	String CUSTOM_EXT = "?";

	/** 指示表达式将在方法内设置 */
	String IN_METHOD = "inMethod";

	/**
	 * 用于进行行列生成的 键值对表达式 或 特殊值 "inMethod"
	 *
	 * @see #IN_METHOD
	 */
	String value();

	/** 标题数组 */
	String[] values() default {};

	/**
	 * 缓存配置 的 key，如果为 "*" 则不缓存配置
	 *
	 * @see #NO_CACHE
	 */
	String key() default "";

	/** 响应输出的文件名（前缀） */
	String filename() default "";

}
