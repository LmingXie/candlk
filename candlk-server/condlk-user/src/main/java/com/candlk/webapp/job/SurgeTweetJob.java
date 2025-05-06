package com.candlk.webapp.job;

import java.util.List;
import javax.annotation.Resource;

import com.candlk.webapp.user.entity.Tweet;
import com.candlk.webapp.user.service.TweetService;
import com.candlk.webapp.user.service.TweetUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
// @Configuration
public class SurgeTweetJob {

	@Resource
	TweetService tweetService;
	@Resource
	TweetUserService tweetUserService;

	/**
	 * 同步推文信息（生产：1 m/次；本地：15 m/次）
	 */
	@Scheduled(cron = "${service.cron.TweetJob:0 0/15 * * * ?}")
	public void run() {
		log.info("开始【刷新浏览量】定时任务...");
		List<Tweet> tweets = tweetService.surgeToken(100);

		log.info("结束【刷新浏览量】定时任务...");
	}

}
