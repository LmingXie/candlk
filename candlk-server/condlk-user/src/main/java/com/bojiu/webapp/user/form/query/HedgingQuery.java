package com.bojiu.webapp.user.form.query;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HedgingQuery {

	/** 查询平台对 */
	public String pair;

	/** 方案类型（0=推荐方案；1=存档方案） */
	public Integer type;

	/** 排序类型（0=平均利润；1=第一场时间） */
	public Integer sortType;

	/** 串子大小 */
	public Integer parlaySize = 3;

}
