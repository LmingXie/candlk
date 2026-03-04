package com.bojiu.webapp.user.job;

import javax.annotation.Resource;

import com.bojiu.common.redis.RedisUtil;
import com.bojiu.webapp.user.service.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/** 保存历史消息 */
@Slf4j
@Configuration
public class SaveHistoryMsgJob {

	@Resource
	MessageService messageService;

	@Scheduled(cron = "${webapp.job.cron.SaveHistoryMsgJob:0/2 * * * * ?}")
	public void run() {
		RedisUtil.fastAttemptInLock("save-history-msg-job", 5 * 1000 * 60, () -> {
			messageService.saveHistoryMsg();
			return true;
		});
	}

}