package com.candlk.webapp.user.service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.candlk.webapp.base.service.BaseServiceImpl;
import com.candlk.webapp.user.dao.TweetWordDao;
import com.candlk.webapp.user.entity.TweetWord;
import org.springframework.stereotype.Service;

/**
 * 推特词库 服务实现类
 *
 * @since 2025-04-27
 */
@Service
public class TweetWordService extends BaseServiceImpl<TweetWord, TweetWordDao, Long> {

	public List<TweetWord> findByWords(List<String> words) {
		return selectList(new QueryWrapper<TweetWord>().in(TweetWord.WORDS, words));
	}

}
