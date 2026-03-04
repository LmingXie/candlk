package com.bojiu.webapp.user.job;

import javax.annotation.Resource;

import com.bojiu.common.redis.RedisUtil;
import com.bojiu.webapp.user.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.EasyDate;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/** 删除历史消息 */
@Slf4j
@Configuration
public class ClearHistoryMsgJob {

	@Resource
	MessageService messageService;

	/** 删除3天前的历史消息（每3时执行一次） */
	@Scheduled(cron = "${webapp.job.cron.ClearHistoryMsgJob:0 0 0/3 * * ?}")
	public void run() {
		RedisUtil.fastAttemptInLock("clear-history-msg-job", 5 * 1000 * 60, () -> {
			messageService.clearHistoryMsg(new EasyDate().addDay(-3).toDate());
			return true;
		});
	}

}