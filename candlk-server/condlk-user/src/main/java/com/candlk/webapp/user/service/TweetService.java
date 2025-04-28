package com.candlk.webapp.user.service;

import java.util.List;
import javax.annotation.Resource;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.candlk.common.dao.SmartQueryWrapper;
import com.candlk.common.model.TimeInterval;
import com.candlk.common.redis.RedisUtil;
import com.candlk.common.web.Page;
import com.candlk.context.model.RedisKey;
import com.candlk.webapp.base.service.BaseServiceImpl;
import com.candlk.webapp.user.dao.TweetDao;
import com.candlk.webapp.user.entity.Tweet;
import com.candlk.webapp.user.form.TweetQuery;
import com.candlk.webapp.user.model.TweetProvider;
import com.candlk.webapp.user.vo.TweetVO;
import lombok.extern.slf4j.Slf4j;
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

			// TODO AI 分词并提取 代币名称和简称

			// TODO 分词匹配
		} catch (DuplicateKeyException e) { // 违反唯一约束
			log.warn("【{}】推文已存在：{}", provider, tweetId);
		} catch (Exception e) {
			log.error("【{}】保存推文失败：{}", provider, tweetId, e);
		}
	}

	public List<Tweet> lastList(Integer limit) {
		return selectList(new QueryWrapper<Tweet>()
				.orderByDesc(Tweet.ADD_TIME)
				.last("LIMIT " + 100)
		);
	}

}
