package com.bojiu.webapp.user.service;

import java.util.*;

import com.bojiu.webapp.base.service.BaseServiceImpl;
import com.bojiu.webapp.user.dao.MessageDao;
import com.bojiu.webapp.user.entity.TgMsg;
import org.apache.dubbo.common.utils.ConcurrentHashSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.bojiu.webapp.user.entity.TgMsg.*;

/**
 * 消息记录表 服务实现类
 *
 * @author
 * @since 2025-11-27
 */
@Service
public class MessageService extends BaseServiceImpl<TgMsg, MessageDao, Long> {

	public static final Long TELEGRAM_PEER_ID = 777000L;

	public List<TgMsg> getAuthMsg(Long userId, Date beginTime) {
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

	/** 缓存消息 */
	private final Set<TgMsg> cachedMsgs = new ConcurrentHashSet<>(4096);

	public void addMsg(TgMsg msg) {
		cachedMsgs.add(msg);
	}

	@Transactional
	public void saveHistoryMsg() {
		if (!cachedMsgs.isEmpty()) {
			saveBatch(cachedMsgs, 2048);
			cachedMsgs.clear();
		}
	}

}
