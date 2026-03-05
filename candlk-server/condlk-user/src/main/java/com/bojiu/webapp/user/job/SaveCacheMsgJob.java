package com.bojiu.webapp.user.job;

import javax.annotation.Resource;

import com.bojiu.common.redis.RedisUtil;
import com.bojiu.context.model.RedisKey;
import com.bojiu.webapp.user.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/** 保存缓存消息 */
@Slf4j
@Configuration
public class SaveCacheMsgJob {

	@Resource
	MessageService messageService;

	transient long lastClearTime = 0;

	/** 持久化缓存消息（每2秒执行一次） */
	@Scheduled(cron = "${webapp.job.cron.SaveCacheMsgJob:0/2 * * * * ?}")
	public void run() {
		RedisUtil.fastAttemptInLock("save-cache-msg-job", 5 * 1000 * 60, () -> {
			// 保存历史消息（优雅停机时允许丢失2秒内的消息，尽可能的快速存档消息）
			messageService.saveCacheMsg();

			// 删除1个小时以前的去重消息
			final long now = System.currentTimeMillis();
			if (now - lastClearTime > 1000 * 60 * 2) {
				RedisUtil.opsForZSet().removeRangeByScore(RedisKey.MSG_DEDUP_KEY, 0, now - 1000 * 60 * 60);
				lastClearTime = now;
			}
			return true;
		});
	}

}