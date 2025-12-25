package com.bojiu.webapp.user.dto;

import java.util.Date;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.*;
import org.jspecify.annotations.Nullable;

@Getter
@Setter
@NoArgsConstructor
public class GameBetQueryDTO {

	/** 上次查询时间 */
	@Nullable
	@JSONField(format = "yyyy-MM-dd HH:mm:ss.SSS")
	public Date lastTime;

}
