package com.bojiu.webapp.user.entity;

import java.util.Date;

import com.bojiu.webapp.base.entity.TimeBasedEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * Telegram用户基础信息表
 *
 * @since 2025-11-27
 */
@Setter
@Getter
public class UserInfo extends TimeBasedEntity {

	/** 用户ID */
	Long userId;
	/** 手机号 */
	String phone;
	/** 用户账号名 */
	String username;
	/** 昵称 */
	String nickname;
	/** 头像 */
	String avatar;
	/** to_json结果（User, Chat, Channel类型） */
	String jsonEntity;
	/** 业务标识（FLAG_BOT=机器人） */
	Integer bizFlag;
	/** 是否为机器人 */
	Integer isBot;

	public static UserInfo of(Long userId, String phone, String username, String nickname, String avatar, String jsonEntity, Date now, Integer isBot) {
		UserInfo info = new UserInfo();
		info.setUserId(userId);
		info.setPhone(phone);
		info.setUsername(username);
		info.setNickname(nickname);
		info.setAvatar(avatar);
		info.setJsonEntity(jsonEntity);
		info.setAddTime(now);
		info.setUpdateTime(now);
		info.setBizFlag(0);
		info.setIsBot(isBot);
		info.initTime(now);
		return info;
	}

	/** 业务标记：是否为机器人 */
	public static final int BIZ_FLAG_BOT = 1;

	public static final String USER_ID = "user_id";
	public static final String PHONE = "phone";
	public static final String USERNAME = "username";
	public static final String NICKNAME = "nickname";
	public static final String AVATAR = "avatar";
	public static final String JSON_ENTITY = "json_entity";
	public static final String ADD_TIME = "add_time";
	public static final String UPDATE_TIME = "update_time";
	public static final String BIZ_FLAG = "biz_flag";
	public static final String IS_BOT = "is_bot";

}
