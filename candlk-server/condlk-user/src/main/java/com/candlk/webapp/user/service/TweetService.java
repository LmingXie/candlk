package com.candlk.webapp.user.service;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Resource;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.candlk.common.dao.SmartQueryWrapper;
import com.candlk.common.model.TimeInterval;
import com.candlk.common.redis.RedisUtil;
import com.candlk.common.web.Page;
import com.candlk.context.model.RedisKey;
import com.candlk.context.web.Jsons;
import com.candlk.webapp.api.TweetInfo;
import com.candlk.webapp.base.service.BaseServiceImpl;
import com.candlk.webapp.user.dao.TweetDao;
import com.candlk.webapp.user.entity.Tweet;
import com.candlk.webapp.user.form.TweetQuery;
import com.candlk.webapp.user.model.TweetProvider;
import com.candlk.webapp.user.vo.TweetVO;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.X;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 推文信息表 服务实现类
 *
 * @since 2025-04-27
 */
@Slf4j
@Service
public class TweetService extends BaseServiceImpl<Tweet, TweetDao, Long> {

	@Resource
	TweetUserService tweetUserService;

	public Page<TweetVO> findPage(Page<?> page, TweetQuery query, TimeInterval interval) {
		return baseDao.findPage(page, new SmartQueryWrapper<Tweet>()
		);
	}

	@Transactional
	public void saveTweet(Tweet tweetInfo, String author, TweetProvider provider, String tweetId) {
		try {
			// 添加推文
			super.save(tweetInfo);

			if (!tweetUserService.updateTweetLastTime(author, tweetInfo.getAddTime())) {
				// Redis 记录新用户
				RedisUtil.getStringRedisTemplate().opsForSet().add(RedisKey.TWEET_NEW_USERS, author);
			}

		} catch (DuplicateKeyException e) { // 违反唯一约束
			log.warn("【{}】推文已存在：{}", provider, tweetId);
		} catch (Exception e) {
			log.error("【{}】保存推文失败：{}", provider, tweetId, e);
		}
	}

	public List<String> lastList(Integer limit) {
		return baseDao.lastList(new QueryWrapper<Tweet>()
				.orderByDesc(Tweet.ADD_TIME)
				.last("LIMIT " + limit)
		);
	}

	@Transactional
	public void sync(List<TweetInfo> tweets) {
		if (!tweets.isEmpty()) {
			List<UpdateWrapper<Tweet>> wrappers = new ArrayList<>(tweets.size());
			for (TweetInfo tweet : tweets) {
				UpdateWrapper<Tweet> wrapper = new UpdateWrapper<Tweet>()
						.set(Tweet.ORG_MSG, Jsons.encode(tweet))
						.set(Tweet.STATUS, Tweet.SYNC)
						.eq(Tweet.TWEET_ID, tweet.id)
						.eq(Tweet.STATUS, Tweet.INIT);
				if (tweet.noteTweet != null && X.isValid(tweet.noteTweet.text)) {
					wrapper.set(Tweet.TEXT, tweet.noteTweet.text);
				} else if (X.isValid(tweet.text)) {
					wrapper.set(Tweet.TEXT, tweet.text);
				} else {
					log.warn("【{}】推文无内容：{}", tweet.id, Jsons.encode(tweet));
				}
				if (tweet.publicMetrics != null) {
					wrapper.set(X.isValid(tweet.publicMetrics.retweetCount), Tweet.RETWEET, tweet.publicMetrics.retweetCount)
							.set(X.isValid(tweet.publicMetrics.replyCount), Tweet.REPLY, tweet.publicMetrics.replyCount)
							.set(X.isValid(tweet.publicMetrics.likeCount), Tweet.LIKES, tweet.publicMetrics.likeCount)
							.set(X.isValid(tweet.publicMetrics.quoteCount), Tweet.QUOTE, tweet.publicMetrics.quoteCount)
							.set(X.isValid(tweet.publicMetrics.bookmarkCount), Tweet.BOOKMARK, tweet.publicMetrics.bookmarkCount)
							.set(X.isValid(tweet.publicMetrics.impressionCount), Tweet.IMPRESSION, tweet.publicMetrics.impressionCount);
				} else {
					log.warn("【{}】推文无统计数据：{}", tweet.id, Jsons.encode(tweet));
				}
				wrappers.add(wrapper);
			}
			super.updateBatchByWrappers(wrappers);
		}
	}

}
