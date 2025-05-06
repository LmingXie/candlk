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

	@Select("SELECT * FROM x_tweet ${ew.customSqlSegment}")
	Page<TweetVO> findPage(Page<?> page, @Param("ew") Wrapper<?> wrapper);

	@Select("SELECT tweet_id FROM x_tweet ${ew.customSqlSegment}")
	List<String> lastList(@Param("ew") Wrapper<?> wrapper);

	@Select("""
			SELECT t.*, te.type AS userType
			FROM x_tweet t
			LEFT JOIN x_tweet_user tu ON t.username = tu.username
			LEFT JOIN x_token_event te ON t.id = te.tweet_id
			${ew.customSqlSegment}
			ORDER BY (t.score + tu.score) DESC
			""")
	List<Tweet> lastGenToken(@Param("ew") Wrapper<?> wrapper);

}
