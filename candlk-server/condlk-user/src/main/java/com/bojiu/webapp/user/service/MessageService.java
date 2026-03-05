package com.bojiu.webapp.user.service;

import java.util.*;

import javax.annotation.Resource;

import com.bojiu.common.redis.RedisUtil;
import com.bojiu.context.model.RedisKey;
import com.bojiu.webapp.base.service.BaseServiceImpl;
import com.bojiu.webapp.user.dao.MessageDao;
import com.bojiu.webapp.user.entity.TgMsg;
import org.apache.dubbo.common.utils.ConcurrentHashSet;
import org.apache.dubbo.common.utils.Pair;
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

	@Resource
	UserInfoService userInfoService;

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
		// 进行群组消息去重（私聊无需去重）
		if (msg.getChatId() > 0 || RedisUtil.opsForZSet().add(RedisKey.MSG_DEDUP_KEY, msg.getChatId() + "-" + msg.getMsgId(), msg.getAddTime().getTime())) {
			cachedMsgs.add(msg);
		}
	}

	/** 保存缓存消息 */
	@Transactional
	public void saveCacheMsg() {
		if (!cachedMsgs.isEmpty()) {
			final Map<Pair<Long, Long>, Long> statMaxMsgId = new HashMap<>();
			for (TgMsg msg : cachedMsgs) {
				msg.setIsBot(userInfoService.hasBot(msg.getUserId()));
				msg.setIsInner(userInfoService.hasInner(msg.getUserId()));
				statMaxMsgId.merge(Pair.of(msg.getUserId(), msg.getChatId()), msg.getMsgId(), Long::max);
			}

			saveBatch(cachedMsgs, 2048);

			// 更新未读消息数
			if (!statMaxMsgId.isEmpty()) {
				for (Map.Entry<Pair<Long, Long>, Long> entry : statMaxMsgId.entrySet()) {
					final Pair<Long, Long> pair = entry.getKey();
					final Long msgId = entry.getValue();
					baseDao.updateReadState(pair.getLeft(), pair.getRight(), msgId, msgId);
				}
			}
			cachedMsgs.clear();
		}
	}

	/** 删除N天以前的历史消息 */
	@Transactional
	public void clearHistoryMsg(Date endTime) {
		delete(updateWrapper().lt(ADD_TIME, endTime));
	}

}
