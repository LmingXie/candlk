package com.bojiu.webapp.user.service;

import java.util.Date;

import com.bojiu.webapp.base.service.BaseServiceImpl;
import com.bojiu.webapp.user.dao.MessageDao;
import com.bojiu.webapp.user.entity.Message;
import org.springframework.stereotype.Service;

import static com.bojiu.webapp.user.entity.Message.*;

/**
 * 消息记录表 服务实现类
 *
 * @author
 * @since 2025-11-27
 */
@Service
public class MessageService extends BaseServiceImpl<Message, MessageDao, Long> {

	public static final Long TELEGRAM_PEER_ID = 777000L;

	public Message getTgMsg(Long userId, Date beginTime) {
		return selectOne(smartEq(PEER_ID, userId)
				.eq(FROM_ID, TELEGRAM_PEER_ID)
				.eq(USER_ID, userId)
				.ge(ADD_TIME, beginTime)

		);
	}

}
