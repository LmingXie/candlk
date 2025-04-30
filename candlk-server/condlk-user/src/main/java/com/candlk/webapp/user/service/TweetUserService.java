package com.candlk.webapp.user.service;

import java.math.BigDecimal;
import java.util.*;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.candlk.common.dao.SmartQueryWrapper;
import com.candlk.context.web.Jsons;
import com.candlk.webapp.api.TweetUserInfo;
import com.candlk.webapp.base.service.BaseServiceImpl;
import com.candlk.webapp.user.dao.TweetUserDao;
import com.candlk.webapp.user.entity.TweetUser;
import com.candlk.webapp.user.model.TweetUserType;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.NumberUtil;
import me.codeplayer.util.X;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * 推特用户表 服务实现类
 *
 * @since 2025-04-27
 */
@Slf4j
@Service
public class TweetUserService extends BaseServiceImpl<TweetUser, TweetUserDao, Long> {

	public boolean updateTweetLastTime(String username, Date now) {
		return super.update(new UpdateWrapper<TweetUser>()
				.set(TweetUser.TWEET_LAST_TIME, now)
				.eq(TweetUser.USERNAME, username)
		) > 0;
	}

	@Transactional
	public void updateStat(TweetUser tweetUser) {
		final String userId = tweetUser.getUserId();
		if (tweetUser.getFollowers() != null) {
			tweetUser.setScore(BigDecimal.valueOf(calcScore(tweetUser.getFollowers())));
		}
		int num = super.update(tweetUser, new UpdateWrapper<TweetUser>().eq(TweetUser.USERID, userId));
		if (num < 1) {
			log.info("【推特用户】新增用户数据：{}", userId);
			tweetUser.setType(TweetUserType.SPECIAL);
			super.save(tweetUser);
		} else {
			log.info("【推特用户】更新用户数据：{}", userId);
		}
	}

	public List<String> lastList(Collection<?> excludeUserIds, Integer limit) {
		return baseDao.lastList(new SmartQueryWrapper<TweetUser>()
				.notIn(!CollectionUtils.isEmpty(excludeUserIds), TweetUser.USERID, excludeUserIds)
				.orderByDesc(TweetUser.TWEET_LAST_TIME)
				.last("LIMIT " + limit)
		);
	}

	@Transactional
	public void sync(List<TweetUserInfo> users) {
		if (!users.isEmpty()) {
			List<UpdateWrapper<TweetUser>> wrappers = new ArrayList<>(users.size());
			for (TweetUserInfo user : users) {
				UpdateWrapper<TweetUser> wrapper = new UpdateWrapper<TweetUser>()
						.set(X.isValid(user.username), TweetUser.USERNAME, user.username)
						.set(TweetUser.NICKNAME, user.name)
						.set(X.isValid(user.profileImageUrl), TweetUser.AVATAR, user.profileImageUrl)
						.set(X.isValid(user.profileBannerUrl), TweetUser.BANNER, user.profileBannerUrl)
						.set(X.isValid(user.pinnedTweetId), TweetUser.PINNED, "[\"" + user.pinnedTweetId + "\"]")
						.set(X.isValid(user.location), TweetUser.LOCATION, user.location)
						.set(X.isValid(user.description), TweetUser.DESCRIPTION, user.toDescription().toJSONString())
						.eq(TweetUser.USERID, user.id);

				if (user.publicMetrics != null) {
					final Integer followersCount = NumberUtil.getInteger(user.publicMetrics.followersCount, 0);
					int score = calcScore(followersCount);
					wrapper.set(TweetUser.FOLLOWERS, followersCount)
							.set(TweetUser.SCORE, BigDecimal.valueOf(score))
							.set(X.isValid(user.publicMetrics.followingCount), TweetUser.FOLLOWING, user.publicMetrics.followingCount)
							.set(X.isValid(user.publicMetrics.tweetCount), TweetUser.TWEETS, user.publicMetrics.tweetCount)
							.set(X.isValid(user.publicMetrics.mediaCount), TweetUser.MEDIA, user.publicMetrics.mediaCount)
							.set(X.isValid(user.publicMetrics.listedCount), TweetUser.LISTED, user.publicMetrics.listedCount)
							.set(X.isValid(user.publicMetrics.likeCount), TweetUser.LIKES, user.publicMetrics.likeCount);
				} else {
					log.warn("【{}】用户无统计数据：{}", user.id, Jsons.encode(user));
				}
				wrappers.add(wrapper);
			}
			super.updateBatchByWrappers(wrappers);
		}
	}

	private static int calcScore(Integer followersCount) {
		if (followersCount == null) {
			return 1;
		}
	    /*
	    对用户进行评分 根据账号粉丝数量进行评分：
		     <10 万粉丝：1分
		     10万-50万：2分
		     50万-100万：3分
		     >100万：4分
	     */
		int score = 1;
		if (followersCount >= 10_000 && followersCount < 50_000) {
			score = 2;
		} else if (followersCount >= 50_000 && followersCount < 100_000) {
			score = 3;
		} else if (followersCount >= 100_000) {
			score = 4;
		}
		return score;
	}

}
