package com.candlk.webapp.user.service;

import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.candlk.webapp.base.service.BaseServiceImpl;
import com.candlk.webapp.user.dao.TweetUserDao;
import com.candlk.webapp.user.entity.TweetUser;
import org.springframework.stereotype.Service;

/**
 * 推特用户表 服务实现类
 *
 * @since 2025-04-27
 */
@Service
public class TweetUserService extends BaseServiceImpl<TweetUser, TweetUserDao, Long> {

	public boolean updateTweetLastTime(String author, Date now) {
		return update(new UpdateWrapper<TweetUser>()
				.set(TweetUser.TWEET_LAST_TIME, now)
				.eq(TweetUser.AUTHOR, author)
		) > 0;
	}

}
