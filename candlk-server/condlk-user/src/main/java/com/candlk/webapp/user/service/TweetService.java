package com.candlk.webapp.user.service;

import com.candlk.common.dao.SmartQueryWrapper;
import com.candlk.common.model.TimeInterval;
import com.candlk.common.web.Page;
import com.candlk.webapp.base.service.BaseServiceImpl;
import com.candlk.webapp.user.dao.TweetDao;
import com.candlk.webapp.user.entity.Tweet;
import com.candlk.webapp.user.form.TweetQuery;
import com.candlk.webapp.user.vo.TweetVO;
import org.springframework.stereotype.Service;

/**
 * 推文信息表 服务实现类
 *
 * @since 2025-04-27
 */
@Service
public class TweetService extends BaseServiceImpl<Tweet, TweetDao, Long> {

	public Page<TweetVO> findPage(Page<?> page, TweetQuery query, TimeInterval interval) {
		return baseDao.findPage(page, new SmartQueryWrapper<Tweet>()
		);
	}

}
