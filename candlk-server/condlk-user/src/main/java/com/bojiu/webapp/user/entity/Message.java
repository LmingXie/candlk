package com.bojiu.webapp.user.entity;

import com.bojiu.webapp.base.entity.TimeBasedEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 消息记录表
 *
 * @author
 * @since 2025-11-27
 */
@Setter
@Getter
public class Message extends TimeBasedEntity {

	/** 用户ID */
	Long userId;
	/** 对话ID（群组-100开头） */
	Long chatId;
	/** 消息类型：0=消息；1=引用回复；2=转发 */
	Integer type;
	/** 对话中的消息ID */
	Long msgId;
	/** 分组ID（收到多个媒体文件时会自动分组拆分） */
	String groupId;
	/** 用户/群组/频道 ID */
	Long peerId;
	/** 发送用户ID（群组/频道） */
	Long fromId;
	/** 消息内容 */
	String message;
	/** 媒体资源JOSN数组（图片=img:url；视频=video:url；其他文件=file:<url>:<filename>:<size>l；） */
	String medias;
	/** web页预览JSON（预览图photo=url） */
	String webpage;
	/** 富文本修饰JSON（链接=MessageEntityTextUrl；加粗=MessageEntityBold；斜体=MessageEntityItalic；等） */
	String entities;
	/** 原始消息 */
	String raw;
	/** 是否为私聊消息 */
	Boolean isPrivateChat;
	/** 发送人是否为机器人 */
	Boolean isBot;
	/** 发送人是否为内部账号 */
	Boolean isInner;

	public static final String USER_ID = "user_id";
	public static final String CHAT_ID = "chat_id";
	public static final String TYPE = "type";
	public static final String MSG_ID = "msg_id";
	public static final String GROUP_ID = "group_id";
	public static final String PEER_ID = "peer_id";
	public static final String FROM_ID = "from_id";
	public static final String MESSAGE = "message";
	public static final String MEDIAS = "medias";
	public static final String WEBPAGE = "webpage";
	public static final String ENTITIES = "entities";
	public static final String RAW = "raw";
	public static final String IS_PRIVATE_CHAT = "is_private_chat";
	public static final String IS_BOT = "is_bot";
	public static final String IS_INNER = "is_inner";

}
