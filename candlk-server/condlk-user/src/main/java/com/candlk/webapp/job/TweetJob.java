package com.candlk.webapp.job;

import javax.annotation.PostConstruct;

import com.candlk.webapp.user.model.TweetProvider;
import com.candlk.webapp.ws.TweetWsApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
public class TweetJob {

	@PostConstruct
	public void init() {
		for (TweetProvider type : TweetProvider.CACHE) {
			TweetWsApi.getInstance(type);
		}
	}

	/**
	 * 同步推文信息（生产：1 m/次；本地：15 m/次）
	 */
	@Scheduled(cron = "${service.cron.TweetJob:0 0/1 * * * ?}")
	public void run() {
		log.info("开始执行心跳任务...");
		// log.info("结束执行心跳任务。");
	}

}
