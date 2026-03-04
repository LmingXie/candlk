package com.bojiu.webapp.user.entity;

import java.util.Objects;

import com.baomidou.mybatisplus.annotation.TableName;
import com.bojiu.webapp.base.entity.TimeBasedEntity;
import com.bojiu.webapp.user.model.MsgType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 消息记录表
 *
 * @since 2025-11-27
 */
@Setter
@Getter
@Accessors(chain = true)
@TableName("tg_message")
public class TgMsg extends TimeBasedEntity {

	/** 用户ID */
	Long userId;
	/** 对话ID（群组-100开头） */
	Long chatId;
	/** 消息类型：0=消息；1=引用回复；2=转发 */
	MsgType type;
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
	/** 媒体资源JOSN数组（图片=img:url；视频=video:url；其他文件=file:<url>:<filename>:<size>；） */
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

	/** 用户私聊消息 */
	public boolean asUserMsg() {
		return peerId.equals(fromId);
	}

	/// ### ChatId 数值区间
	/// | 类型             | chatId 数值范围          | 例子               | peerId 获取方式               |
	/// | -------------- | -------------------- | ---------------- | ------------------------- |
	/// | 👤 用户私聊        | `> 0`                | `7586662663`     | `peerId = chatId`         |
	/// | 👥 basic group | `-1 … -999999999999` | `-123456789`     | `peerId = -chatId`        |
	/// | 📢 supergroup  | `≤ -1000000000000`   | `-1002535681520` | `peerId = -chatId - 1e12` |
	/// | 📡 channel     | 同 supergroup         | `-1009876543210` | 同上                        |
	/// ### PeerId / ChatId / UserId 关系
	/// | 场景          | chatId                | peerId  | fromId     |
	/// | ----------- | --------------------- | ------- | ---------- |
	/// | 私聊          | userId                | userId  | userId     |
	/// | basic group | -groupId              | groupId | 发送者 userId |
	/// | supergroup  | -1000000000000 - sgId | sgId    | 发送者 userId |
	/// | channel 帖子  | 同上                    | sgId    | channelId  |
	public static long toPeerId(long chatId) {
		if (chatId <= -1_000_000_000_000L) {
			return -chatId - 1_000_000_000_000L;
		}
		return chatId;
	}

	/** 转换文件大小为MB或KB */
	public static String convertSize(long sizeBytes) {
		return sizeBytes >= 1024 * 1024
				? String.format("%.2f MB", sizeBytes / (1024.0 * 1024))
				: String.format("%.2f KB", sizeBytes / 1024.0);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final TgMsg tgMsg = (TgMsg) o;
		return Objects.equals(getChatId(), tgMsg.getChatId()) && Objects.equals(getMsgId(), tgMsg.getMsgId());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getChatId(), getMsgId());
	}

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
