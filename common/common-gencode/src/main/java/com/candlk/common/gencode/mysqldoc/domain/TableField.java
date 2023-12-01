package com.candlk.common.gencode.mysqldoc.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @since 2019/2/21 16:48
 */
@Getter
@Setter
@ToString
public class TableField {

	/** 字段名 */
	private String field;
	/** 类型 */
	private String type;
	/** 是否为空 */
	private String Null;
	/** 主键 */
	private String key;
	/** 字段说明 */
	private String comment;
	/** 类属性名称 */
	private String property;

}
