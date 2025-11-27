package com.bojiu.webapp.user.entity;

import java.util.Date;

import com.bojiu.webapp.base.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 剧本聊天机器人消息发送记录
 *
 * @author
 * @since 2025-11-27
 */
@Setter
@Getter
public class LarpBotLogs extends BaseEntity {

	/** 配置ID */
	Long larpId;
	/** 用户ID */
	Long userId;
	/** 对话中的消息ID */
	Long msgId;
	/** 当前话术索引位 */
	Long offset;
	/** 消息内容 */
	String message;
	/** 创建时间 */
	Date addTime;

	public static final String LARP_ID = "larp_id";
	public static final String USER_ID = "user_id";
	public static final String MSG_ID = "msg_id";
	public static final String OFFSET = "offset";
	public static final String MESSAGE = "message";
	public static final String ADD_TIME = "add_time";

}
