package com.bojiu.webapp.user.entity;

import com.bojiu.webapp.base.entity.TimeBasedEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * TG群组消息采集记录表
 *
 * @author
 * @since 2025-11-27
 */
@Setter
@Getter
public class CollectMsg extends TimeBasedEntity {

	/** 用户ID */
	Long userId;
	/** 来源群组链接 */
	String link;
	/** 消息ID */
	Long msgId;
	/** 原始消息 */
	String raw;
	/** 状态：0=未邀请；1=已邀请；2=邀请失败 */
	Integer status;
	/** 失败原因 */
	String issue;

	public static final String USER_ID = "user_id";
	public static final String LINK = "link";
	public static final String MSG_ID = "msg_id";
	public static final String RAW = "raw";
	public static final String STATUS = "status";
	public static final String ISSUE = "issue";

}
