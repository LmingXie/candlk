package com.bojiu.webapp.user.entity;

import com.bojiu.webapp.base.entity.TimeBasedEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 剧本聊天机器人演员表
 *
 * @author
 * @since 2025-11-27
 */
@Setter
@Getter
public class LarpBotActor extends TimeBasedEntity {

	/** 配置ID */
	Long larpId;
	/** 用户ID */
	Long userId;

	public static final String LARP_ID = "larp_id";
	public static final String USER_ID = "user_id";

}
