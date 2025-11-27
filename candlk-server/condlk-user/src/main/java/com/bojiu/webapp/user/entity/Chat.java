package com.bojiu.webapp.user.entity;

import com.bojiu.webapp.base.entity.TimeBasedEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 账号对话表
 *
 * @author
 * @since 2025-11-27
 */
@Setter
@Getter
public class Chat extends TimeBasedEntity {

	/** 对话类型：0=私聊；1=群组；2=频道；4=加密私聊（暂时不区别） */
	Integer type;
	/** 对话ID（群组-100开头） */
	Long chatId;
	/** 用户ID */
	Long userId;
	/** 用户/群组/频道 ID */
	Long peerId;
	/** 对话标题 */
	String title;
	/** 头像 */
	String avatar;
	/** 未读消息数 */
	Long unreadCount;
	/** 未读提及次数 */
	Long unreadMentionsCount;
	/** to_json结果（User, Chat, Channel类型） */
	String jsonEntity;
	/** 业务标识，整数，标记特定状态（如禁言），默认0 */
	Integer bizFlag;

	public static final String TYPE = "type";
	public static final String CHAT_ID = "chat_id";
	public static final String USER_ID = "user_id";
	public static final String PEER_ID = "peer_id";
	public static final String TITLE = "title";
	public static final String AVATAR = "avatar";
	public static final String UNREAD_COUNT = "unread_count";
	public static final String UNREAD_MENTIONS_COUNT = "unread_mentions_count";
	public static final String JSON_ENTITY = "json_entity";
	public static final String BIZ_FLAG = "biz_flag";

}
