package com.candlk.webapp.job;

import java.util.*;
import javax.annotation.Resource;

import com.candlk.common.model.Messager;
import com.candlk.common.redis.RedisUtil;
import com.candlk.common.util.Common;
import com.candlk.context.web.Jsons;
import com.candlk.webapp.api.TweetApi;
import com.candlk.webapp.api.TweetInfo;
import com.candlk.webapp.user.entity.Tweet;
import com.candlk.webapp.user.model.UserRedisKey;
import com.candlk.webapp.user.service.TweetService;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.NumberUtil;
import me.codeplayer.util.StringUtil;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
// @Configuration
public class SurgeTweetJob {

	@Resource
	TweetService tweetService;

	static final String[] SURGE_TWEET_KEYS = new String[] {
			"AAAAAAAAAAAAAAAAAAAAAK450wEAAAAAc6mb7tVQF3%2B6ssbH2borX%2F0jQpI%3DrxKSmb3okKK49o5Z1P4RFZXgIK08FXXhc1XjSbVrJd1y0bo1sp"
	};
	/** 往前追溯10倍的推文（3个秘钥 = 3000条推文） */
	static final int COUNTER_MAX = SURGE_TWEET_KEYS.length * 100 * 10;

	public List<String> findSurgeTweetApi() {
		// 查询API使用情况
		List<Double> score = RedisUtil.score(UserRedisKey.SURGE_TWEET_API_LIMIT, SURGE_TWEET_KEYS);
		long now = System.currentTimeMillis() / 1000;
		final int len = SURGE_TWEET_KEYS.length;
		List<String> apiKeys = new ArrayList<>(len);
		for (int i = 0; i < len; i++) {
			final String apiKey = SURGE_TWEET_KEYS[i];
			Double v = score.get(i);
			// 推文接口：15 个请求 / 15 分钟
			if (v == null || now - v > 60) {
				apiKeys.add(apiKey);
			}
		}
		return apiKeys;
	}

	/**
	 * 同步推文信息（生产：1 m/次）
	 */
	@Scheduled(cron = "${service.cron.SurgeTweetJob:0 0 0/1 * * ?}")
	public void run() {
		log.info("开始【刷新浏览量】定时任务...");
		final List<String> surgeTweetApis = findSurgeTweetApi();
		if (surgeTweetApis.isEmpty()) {
			log.warn("全部推文接口触发限流！");
			return;
		}
		long score = NumberUtil.getLong(RedisUtil.score(UserRedisKey.SURGE_TWEET_API_LIMIT, "point"), 0);
		int limit = surgeTweetApis.size() * 100;

		long counter = score % 1_000_000L;
		// 无需再乘以1000转化为毫秒级
		Date prevTime = score > 1_000_000_000L ? new Date((score / 1_000L) - 1/*包括最后一毫秒*/) : null;
		ZSetOperations<String, String> opsForZSet = RedisUtil.getStringRedisTemplate().opsForZSet();
		if (counter > COUNTER_MAX) {
			// 重置计数器
			counter = 0;
			prevTime = null;
			opsForZSet.remove(UserRedisKey.SURGE_TWEET_API_LIMIT, "point");
		}
		final List<Tweet> oldTweets = tweetService.surgeToken(limit, prevTime);
		if (oldTweets.isEmpty()) {
			// 重置计数器
			opsForZSet.remove(UserRedisKey.SURGE_TWEET_API_LIMIT, "point");
			log.info("跳过，没有可用推文数据！");
			return;
		}
		int i = 0, apiOffset = 0, initialCapacity = 100;
		List<Tweet> batch = new ArrayList<>(initialCapacity);
		for (Tweet oldTweet : oldTweets) {
			batch.add(oldTweet);
			i++;
			if (i >= initialCapacity) {
				sync(oldTweets, surgeTweetApis.get(apiOffset++));
				i = 0;
				batch.clear();
			}
		}
		if (!batch.isEmpty()) {
			sync(batch, surgeTweetApis.get(apiOffset));
		}

		int size = oldTweets.size();
		Date updateTime = oldTweets.get(size - 1).getUpdateTime();
		long newPoint = (updateTime.getTime() / 1000L) * 1_000_000L + counter + size;
		// 更新断点
		opsForZSet.add(UserRedisKey.SURGE_TWEET_API_LIMIT, "point", newPoint);

		log.info("结束【刷新浏览量】定时任务...");
	}

	private void sync(List<Tweet> oldTweets, String surgeTweetKey) {
		final TweetApi tweetApi = new TweetApi(surgeTweetKey, "http://127.0.0.1:10809");

		final String tweetIds = StringUtil.join(oldTweets, Tweet::getTweetId, Common.SEP);

		log.info("推文ID：{}", tweetIds);
		// 记录API使用情况
		RedisUtil.getStringRedisTemplate().opsForZSet().add(UserRedisKey.SURGE_TWEET_API_LIMIT, surgeTweetKey,
				NumberUtil.getDouble(System.currentTimeMillis() / 1000L));

		Messager<List<TweetInfo>> tweetsMsg = tweetApi.tweets(tweetIds);
		log.info("推文：{}", Jsons.encode(tweetsMsg));
		if (tweetsMsg == null || !tweetsMsg.isOK()) {
			log.warn("推文获取失败！");
			return;
		}
		// 将最新推文数据刷新到DB
		List<TweetInfo> tweets = tweetsMsg.data();
		tweetService.sync(tweets, true);
	}

}
