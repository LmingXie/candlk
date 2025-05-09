package com.candlk.webapp.user.dao;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.candlk.common.web.Page;
import com.candlk.webapp.base.dao.BaseDao;
import com.candlk.webapp.user.entity.Tweet;
import com.candlk.webapp.user.vo.TweetVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 推文信息表 Mapper 接口
 *
 * @since 2025-04-27
 */
public interface TweetDao extends BaseDao<Tweet> {

	@Select("""
			SELECT t.id, t.coin, t.symbol, t.description, t.status, t.ca, t.add_time AS saveTime,
			
			tw.tweet_id, tw.type, tw.provider_type, tw.text,
			tw.retweet, tw.reply, tw.likes, tw.quote, tw.bookmark, tw.impression,
			tw.add_time, tw.update_time, tw.username, tw.images, tw.videos, tw.score, tw.words,
			
			tu.avatar, tu.type AS userType ,tu.followers, tu.score AS userScore
			
			FROM x_token_event t
			LEFT JOIN x_tweet tw ON  t.tweet_id= tw.id
			LEFT JOIN x_tweet_user tu ON tw.username = tu.username
			${ew.customSqlSegment}
			""")
	Page<TweetVO> findPage(Page<?> page, @Param("ew") Wrapper<?> wrapper);

	@Select("SELECT * FROM x_tweet ${ew.customSqlSegment}")
	List<Tweet> lastList(@Param("ew") Wrapper<?> wrapper);

	@Select("""
			SELECT t.*, tu.type AS userType
			FROM x_tweet t
			LEFT JOIN x_tweet_user tu ON t.username = tu.username
			LEFT JOIN x_token_event te ON t.id = te.tweet_id
			${ew.customSqlSegment}
			""")
	List<Tweet> lastGenToken(@Param("ew") Wrapper<?> wrapper);

	@Select("""
			SELECT t.id, t.tweet_id, t.type, t.provider_type, t.text,
			t.retweet, t.reply, t.likes, t.quote, t.bookmark, t.impression,
			t.add_time, t.update_time, t.username, t.images, t.videos, t.score,
			
			tu.nickname, tu.avatar,  tu.score AS userScore,
			te.status
			
			FROM x_tweet t
			LEFT JOIN x_tweet_user tu ON t.username = tu.username
			LEFT JOIN x_token_event te ON t.id = te.tweet_id
			${ew.customSqlSegment}
			""")
	Page<TweetVO> findPageTrackers(Page<?> page, @Param("ew") Wrapper<?> wrapper);

}
