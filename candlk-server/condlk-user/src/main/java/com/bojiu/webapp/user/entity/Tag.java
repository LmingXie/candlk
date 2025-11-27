package com.bojiu.webapp.user.entity;

import com.bojiu.webapp.base.entity.TimeBasedEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 全局分组表
 *
 * @author
 * @since 2025-11-27
 */
@Setter
@Getter
public class Tag extends TimeBasedEntity {

	/** 分组名称 */
	String name;

	public static final String NAME = "name";

}
