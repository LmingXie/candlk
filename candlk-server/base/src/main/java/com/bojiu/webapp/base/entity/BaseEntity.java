package com.bojiu.webapp.base.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.bojiu.common.model.Bean;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class BaseEntity implements Bean<Long> {

	// 必须要有，否则 RedisTemplate 反序列化会因为数据结构变更而报错
	@java.io.Serial
	private static final long serialVersionUID = 1L;

	@TableId
	protected Long id;

	public static String id2Alias(long id) {
		return Long.toString(id, 32);
	}

	public static Long alias2Id(String alias) {
		return Long.parseLong(alias, 32);
	}

	/** 主键列 */
	public static final String ID = "id";

}
