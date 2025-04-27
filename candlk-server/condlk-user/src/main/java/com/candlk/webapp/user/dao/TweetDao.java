package com.candlk.webapp.user.dao;

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

}
