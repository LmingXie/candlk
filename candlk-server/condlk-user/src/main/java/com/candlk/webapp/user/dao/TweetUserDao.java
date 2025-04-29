package com.candlk.webapp.user.dao;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.candlk.webapp.base.dao.BaseDao;
import com.candlk.webapp.user.entity.TweetUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 推特用户表 Mapper 接口
 *
 * @since 2025-04-27
 */
public interface TweetUserDao extends BaseDao<TweetUser> {

	@Select("SELECT user_id FROM x_tweet_user ${ew.customSqlSegment}")
	List<String> lastList(@Param("ew") Wrapper<?> wrapper);

}
