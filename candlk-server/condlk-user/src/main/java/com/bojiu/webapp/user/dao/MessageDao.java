package com.bojiu.webapp.user.dao;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.bojiu.webapp.base.dao.BaseDao;
import com.bojiu.webapp.user.entity.TgMsg;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 消息记录表 Mapper 接口
 *
 * @since 2025-11-27
 */
public interface MessageDao extends BaseDao<TgMsg> {

	@Update("""
			INSERT INTO tg_read_state (user_id, peer_id, last_read_msg_id, max_msg_id, update_time)
			VALUES (#{userId}, #{peerId}, #{lastReadMsgId}, #{maxMsgId}, NOW())
			ON DUPLICATE KEY UPDATE
				last_read_msg_id = IF(last_read_msg_id < VALUES(last_read_msg_id), VALUES(last_read_msg_id), last_read_msg_id),
				max_msg_id = IF(max_msg_id < VALUES(max_msg_id), VALUES(max_msg_id), max_msg_id),
				update_time = NOW()
			""")
	@InterceptorIgnore(tenantLine = "1")
	Integer updateReadState(@Param("userId") Long userId, @Param("peerId") Long peerId, @Param("lastReadMsgId") Long lastReadMsgId, @Param("maxMsgId") Long maxMsgId);

}
