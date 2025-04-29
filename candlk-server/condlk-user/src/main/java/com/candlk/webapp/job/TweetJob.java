package com.candlk.webapp.job;

import java.util.List;
import java.util.Set;
import javax.annotation.Resource;

import com.candlk.common.model.Messager;
import com.candlk.common.util.Common;
import com.candlk.context.web.Jsons;
import com.candlk.webapp.api.*;
import com.candlk.webapp.user.service.TweetService;
import com.candlk.webapp.user.service.TweetUserService;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.StringUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
public class TweetJob {

	@Resource
	TweetService tweetService;
	@Resource
	TweetUserService tweetUserService;

	/**
	 * 同步推文信息（生产：1 m/次；本地：15 m/次）
	 */
	@Scheduled(cron = "${service.cron.TweetJob:0 0/15 * * * ?}")
	public void run() {
		log.info("开始同步推文数据信息...");
		List<TweetInfo> tweets = sync();

		// TODO AI 分词并提取 代币名称和简称

		// TODO ES 分词匹配

		log.info("结束同步推文数据任务。");
	}

	private List<TweetInfo> sync() {
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
			return null;
		}

		// 将最新推文数据刷新到DB
		List<TweetInfo> tweets = tweetsMsg.data();
		tweetService.sync(tweets);

		// 同步用户数据【max100】
		Set<String> authorIds = Common.toSet(tweets, tweet -> tweet.authorId);
		int diff = 100 - authorIds.size();
		if (diff > 0) {
			// 再查询最近更新过帖子的用户
			List<String> userIds = tweetUserService.lastList(authorIds, diff);
			if (!userIds.isEmpty()) {
				authorIds.addAll(userIds);
			}
		}
		// 同步用户信息数据
		Messager<List<TweetUserInfo>> usersMsg = tweetApi.users(StringUtil.joins(authorIds, ","));
		if (usersMsg.isOK()) {
			tweetUserService.sync(usersMsg.data());
		}
		return tweets;
	}

}
