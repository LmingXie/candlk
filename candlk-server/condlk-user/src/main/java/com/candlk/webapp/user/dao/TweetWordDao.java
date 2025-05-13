package com.candlk.webapp.user.dao;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.candlk.common.web.Page;
import com.candlk.webapp.base.dao.BaseDao;
import com.candlk.webapp.user.entity.TweetWord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 推特词库 Mapper 接口
 *
 * @since 2025-04-27
 */
public interface TweetWordDao extends BaseDao<TweetWord> {

	@Select("SELECT * FROM x_tweet_word ${ew.customSqlSegment}")
	Page<TweetWord> findPage(Page<?> page, @Param("ew") Wrapper<?> wrapper);

	@Select("SELECT words FROM x_tweet_word ${ew.customSqlSegment}")
	List<String> findWords(@Param("ew") Wrapper<?> wrapper);

}
