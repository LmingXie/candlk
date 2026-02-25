package com.bojiu.webapp.user.service;

import java.util.concurrent.TimeUnit;

import com.bojiu.webapp.base.service.BaseServiceImpl;
import com.bojiu.webapp.user.dao.ChatDao;
import com.bojiu.webapp.user.entity.TgChat;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import static com.bojiu.webapp.user.entity.TgChat.PEER_ID;
import static com.bojiu.webapp.user.entity.TgChat.USER_ID;

/**
 * 账号对话表 服务实现类
 *
 * @since 2025-11-27
 */
@Service
public class ChatService extends BaseServiceImpl<TgChat, ChatDao, Long> {

	/** 缓存：<用户ID, 对话ID> -> $chat */
	static final Cache<Pair<Long, Long>, TgChat> cache = Caffeine.newBuilder()
			.initialCapacity(100)
			.maximumSize(2048)
			.expireAfterWrite(1, TimeUnit.HOURS)
			.build();

	public TgChat getCache(Long userId, Long peerId) {
		return cache.get(Pair.of(userId, peerId), pair -> get(userId, peerId));
	}

	public TgChat get(Long userId, Long peerId) {
		return selectOne(smartEq(USER_ID, userId, PEER_ID, peerId));
	}

}
