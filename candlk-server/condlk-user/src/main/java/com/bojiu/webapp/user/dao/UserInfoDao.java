package com.bojiu.webapp.user.dao;

import java.util.List;

import com.bojiu.webapp.base.dao.BaseDao;
import com.bojiu.webapp.user.entity.UserInfo;
import org.apache.ibatis.annotations.*;

/**
 * Telegram用户基础信息表 Mapper 接口
 *
 * @since 2025-11-27
 */
public interface UserInfoDao extends BaseDao<UserInfo> {

	@Update("""
			INSERT INTO tg_user_info (
			 user_id, phone, username, nickname, avatar, json_entity, add_time, update_time
			) VALUES (#{user.userId}, #{user.phone}, #{user.username}, #{user.nickname}, #{user.avatar}, #{user.jsonEntity}, #{user.addTime}, #{user.updateTime})
			ON DUPLICATE KEY UPDATE
			 phone = VALUES(phone),
			 username = VALUES(username),
			 nickname = VALUES(nickname),
			 avatar = VALUES(avatar),
			 json_entity = VALUES(json_entity),
			 update_time = VALUES(update_time)
			""")
	void updateUserInfo(@Param("user") UserInfo userInfo);

	@Select("SELECT user_id FROM tg_user_info WHERE is_bot = 1")
	List<Long> findAllBotIds();
}
