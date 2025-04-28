package com.candlk.common.dao;

import java.lang.annotation.*;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSource {

	/**
	 * 数据源类型
	 */
	DataSourceType value();

}
