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

}
