package com.bojiu.webapp.base.entity;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import javax.annotation.Nullable;

import lombok.Getter;
import lombok.Setter;
import me.codeplayer.util.X;

@Setter
@Getter
public abstract class TimeBasedEntity extends BaseEntity {

	/** 添加时间 */
	protected Date addTime;
	/** 最后更新时间 */
	protected Date updateTime;

	public void initTime(Date now) {
		addTime = X.expectNotNull(addTime, now);
		updateTime = X.expectNotNull(now, updateTime);
	}

	public static <T> Map<String, Object> asEmbed(@Nullable T bean, Function<T, Map<String, Object>> mapper) {
		return X.map(bean, mapper);
	}

	public static final String ADD_TIME = "add_time";
	public static final String UPDATE_TIME = "update_time";

}
