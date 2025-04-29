package com.candlk.webapp.job;

import java.util.List;
import javax.annotation.Resource;

import com.candlk.common.model.Messager;
import com.candlk.context.web.Jsons;
import com.candlk.webapp.api.TweetApi;
import com.candlk.webapp.api.TweetInfo;
import com.candlk.webapp.user.service.TweetService;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.StringUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
public class TweetJob {

	@Resource
	TweetService tweetService;

	/**
	 * 同步推文信息（生产：1 m/次；本地：15 m/次）
	 */
	@Scheduled(cron = "${service.cron.TweetJob:0 0/15 * * * ?}")
	public void run() {
		log.info("开始同步推文数据信息...");
		// 查询前100条推文
		List<String> oldTweets = tweetService.lastList(100);
		final String tweetIds = StringUtil.joins(oldTweets, ",");

		// TODO 从数据库查询配置
		TweetApi tweetApi = new TweetApi("AAAAAAAAAAAAAAAAAAAAAK450wEAAAAAGq8cOrQ4HTVBBn9Z24umOk8kmik%3DkjB0pGI1V3v3c9WkcQCRVjbfa4DPxJdeTxsF0hWVnIuXrOPVVv",
				"http://127.0.0.1:10809");

		log.info("推文ID：{}", tweetIds);
		Messager<List<TweetInfo>> tweetsMsg = tweetApi.tweets(tweetIds);
		log.info("推文：{}", Jsons.encode(tweetsMsg));
		if (!tweetsMsg.isOK()) {
			log.warn("推文获取失败：{}", tweetsMsg.getMsg());
			return;
		}

		// 将最新推文数据刷新到DB
		List<TweetInfo> tweets = tweetsMsg.data();
		tweetService.sync(tweets);

		// 同步用户数据

		// TODO AI 分词并提取 代币名称和简称

		// TODO ES 分词匹配

		log.info("结束同步推文数据任务。");
	}

}
