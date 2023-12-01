package com.candlk.webapp.base.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 数据访问层的公共抽象父接口，用于封装通用的数据访问工具方法<br>
 */
public interface BaseDao<T> extends BaseMapper<T> {

	/** 获取 wrapper 自定义 WHERE 子句 的表达式 */
	String CUSTOM_WHERE_SQL = "${ew.customSqlSegment}";

}
