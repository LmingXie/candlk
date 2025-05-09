package com.candlk.webapp.job;

import java.math.BigDecimal;
import java.util.*;
import javax.annotation.Resource;

import com.alibaba.fastjson2.JSONObject;
import com.candlk.common.model.Messager;
import com.candlk.common.redis.RedisUtil;
import com.candlk.common.util.Common;
import com.candlk.context.model.RedisKey;
import com.candlk.context.web.Jsons;
import com.candlk.webapp.api.TweetApi;
import com.candlk.webapp.api.TweetUserInfo;
import com.candlk.webapp.user.entity.Tweet;
import com.candlk.webapp.user.entity.TweetUser;
import com.candlk.webapp.user.model.TweetProvider;
import com.candlk.webapp.user.model.TweetUserType;
import com.candlk.webapp.user.service.TweetService;
import com.candlk.webapp.user.service.TweetUserService;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import static com.candlk.webapp.user.service.TweetUserService.calcScore;

@Slf4j
@Configuration
public class TweetSyncJob {

	@Resource
	TweetService tweetService;
	@Resource
	TweetUserService tweetUserService;

	static final TweetApi tweetApi = new TweetApi("AAAAAAAAAAAAAAAAAAAAAK450wEAAAAAc6mb7tVQF3%2B6ssbH2borX%2F0jQpI%3DrxKSmb3okKK49o5Z1P4RFZXgIK08FXXhc1XjSbVrJd1y0bo1sp",
			"http://127.0.0.1:10809");

	/**
	 * 同步推文信息（生产：1 m/次；本地：15 m/次）
	 */
	@Scheduled(cron = "${service.cron.TweetSyncJob:0 0/1 * * * ?}")
	public void run() {
		if (!RedisUtil.getStringRedisTemplate().opsForSet().isMember(RedisKey.SYS_SWITCH, RedisKey.TWEET_SCORE_FLAG)) {
			log.info("【推文评分】开关关闭，跳过执行...");
			return;
		}
		log.info("开始同步推文数据信息...");// 查询前100条推文
		final List<Tweet> oldTweets = tweetService.lastList(100);
		// final String tweetIds = StringUtil.join(oldTweets, Tweet::getTweetId, ",");
		//
		// log.info("推文ID：{}", tweetIds);
		// Messager<List<TweetInfo>> tweetsMsg = tweetApi.tweets(tweetIds);
		//
		// log.info("推文：{}", Jsons.encode(tweetIds));
		// if (tweetsMsg == null || !tweetsMsg.isOK()) {
		// 	log.warn("推文获取失败！");
		// 	return null;
		// }
		//
		// // 将最新推文数据刷新到DB
		// List<TweetInfo> tweets = tweetsMsg.data();
		// tweetService.sync(tweets, false);
		tweetService.syncStatus(oldTweets);

		// 同步用户数据【max100】
		final Set<String> usernames = Common.toSet(oldTweets, Tweet::getUsername);
		// 排除 1 小时内更新过的用户
		final List<String> filterLastUser = tweetUserService.findByUsername(usernames, true);
		filterLastUser.forEach(usernames::remove);

		int diff = 100 - usernames.size();
		if (diff > 0) {
			// 查询最近更新过帖子的用户【更新时间10】
			List<String> userIds = tweetUserService.lastList(usernames, diff, true);
			if (!userIds.isEmpty()) {
				usernames.addAll(userIds);
			}
			diff = 100 - usernames.size();
			if (diff > 0) {
				// 查询最近更新过帖子的用户【不限制更新时间】
				final List<String> userIds2 = tweetUserService.lastList(usernames, diff, false);
				if (!userIds2.isEmpty()) {
					usernames.addAll(userIds2);
				}
			}
		}
		final Date now = new Date();
		// 同步用户信息数据
		final Messager<List<TweetUserInfo>> usersMsg = tweetApi.usersByUsernames(StringUtil.joins(usernames, ","));
		if (usersMsg.isOK()) {
			tweetUserService.batchSyncUserInfo(usersMsg, now, 3);
		}
		log.info("结束同步推文数据任务。");
	}

}
