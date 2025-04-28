package com.candlk.webapp.user.service;

import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.candlk.webapp.base.service.BaseServiceImpl;
import com.candlk.webapp.user.dao.TweetUserDao;
import com.candlk.webapp.user.entity.TweetUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 推特用户表 服务实现类
 *
 * @since 2025-04-27
 */
@Slf4j
@Service
public class TweetUserService extends BaseServiceImpl<TweetUser, TweetUserDao, Long> {

	public boolean updateTweetLastTime(String username, Date now) {
		return update(new UpdateWrapper<TweetUser>()
				.set(TweetUser.TWEET_LAST_TIME, now)
				.eq(TweetUser.USERNAME, username)
		) > 0;
	}

	@Transactional
	public void updateStat(TweetUser tweetUser) {
		final String userId = tweetUser.getUserId();
		int num = update(tweetUser, new UpdateWrapper<TweetUser>().eq(TweetUser.USERID, userId));
		if (num < 1) {
			log.info("【推特用户】新增用户数据：{}", userId);
			super.save(tweetUser);
		} else {
			log.info("【推特用户】更新用户数据：{}", userId);
		}
	}

}
