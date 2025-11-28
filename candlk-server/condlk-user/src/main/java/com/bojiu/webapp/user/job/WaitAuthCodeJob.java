package com.bojiu.webapp.user.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/** 等待授权码任务 */
@Slf4j
@Configuration
public class WaitAuthCodeJob {

	@Scheduled(cron = "${webapp.job.cron.waitAuthCodeJob:0/10 * * * * ?}")
	public void run() {

	}

}