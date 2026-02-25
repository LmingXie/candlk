package com.bojiu.webapp.user.service;

import java.util.*;

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

	public List<Message> getAuthMsg(Long userId, Date beginTime) {
		return selectList(smartQueryWrapper()
				.select(ID, MESSAGE)
				.eq(PEER_ID, TELEGRAM_PEER_ID)
				.eq(FROM_ID, TELEGRAM_PEER_ID)
				.eq(USER_ID, userId)
				.ge(ADD_TIME, beginTime)
				.orderByDesc(ID)
				.last("LIMIT 5")

		);
	}

}
