package com.bojiu.webapp.user.entity;

import java.util.Date;

import com.bojiu.webapp.base.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 对话已读状态表
 *
 * @author
 * @since 2025-11-27
 */
@Setter
@Getter
public class ReadState extends BaseEntity {

	/** 用户ID */
	Long userId;
	/** 群组/频道ID */
	Long peerId;
	/** 最后已读（tg_message表的id字段） */
	Long lastReadMsgId;
	/** 最大消息ID */
	Integer maxMsgId;
	/** 最后更新时间 */
	Date updateTime;

	public static final String USER_ID = "user_id";
	public static final String PEER_ID = "peer_id";
	public static final String LAST_READ_MSG_ID = "last_read_msg_id";
	public static final String MAX_MSG_ID = "max_msg_id";
	public static final String UPDATE_TIME = "update_time";

}
