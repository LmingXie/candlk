package com.candlk.webapp.user.service;

import java.math.BigDecimal;
import java.util.*;
import javax.annotation.Resource;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.candlk.common.dao.SmartQueryWrapper;
import com.candlk.common.model.Messager;
import com.candlk.common.web.Page;
import com.candlk.context.web.Jsons;
import com.candlk.webapp.api.TweetUserInfo;
import com.candlk.webapp.base.service.BaseServiceImpl;
import com.candlk.webapp.es.ESEngineClient;
import com.candlk.webapp.user.dao.TweetUserDao;
import com.candlk.webapp.user.entity.TweetUser;
import com.candlk.webapp.user.form.TweetUserQuery;
import com.candlk.webapp.user.model.TweetProvider;
import com.candlk.webapp.user.model.TweetUserType;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.*;
import org.springframework.dao.DuplicateKeyException;
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

	public Page<TweetUser> findPage(Page<?> page, TweetUserQuery query) {
		return baseDao.findPage(page, new SmartQueryWrapper<TweetUser>()
				.like(TweetUser.USERNAME, query.username)
				.eq(TweetUser.TYPE, query.type)
				.orderByDesc(TweetUser.FOLLOWERS)
		);
	}

	@Transactional
	public void edit(TweetUser user, TweetUserType type) throws Exception {
		super.update(new UpdateWrapper<TweetUser>().set(TweetUser.TYPE, type.value).eq(TweetUser.ID, user.getId()));
	}

	public boolean updateTweetLastTime(String username, Date now) {
		return super.update(new UpdateWrapper<TweetUser>()
				.set(TweetUser.TWEET_LAST_TIME, now)
				.eq(TweetUser.USERNAME, username)
		) > 0;
	}

	@Transactional
	public void updateStat(TweetUser tweetUser) {
		final String username = tweetUser.getUsername();
		if (tweetUser.getFollowers() != null) {
			tweetUser.setScore(BigDecimal.valueOf(calcScore(tweetUser.getFollowers())));
		}
		if (tweetUser.getUpdateTime() == null) {
			tweetUser.setUpdateTime(new Date());
		}
		int num = super.update(tweetUser, new UpdateWrapper<TweetUser>().eq(TweetUser.USERNAME, username));
		if (num < 1) {
			log.info("【推特用户】新增用户数据：{}", username);
			tweetUser.setType(TweetUserType.SPECIAL);
			// 记录为前一天，用于更新用户信息数据
			final Date yesterday = new EasyDate().addDay(-1).toDate();
			tweetUser.initTime(yesterday);
			try {
				super.save(tweetUser);
			} catch (DuplicateKeyException e) { // 违反唯一约束
				log.warn("【推特用户】违反唯一约束，入库失败：{}", username);
			}
		}/* else {
			log.info("【推特用户】更新用户数据：{}", username);
		}*/
	}

	public List<String> findByUsername(Collection<?> usernames, boolean limitLastUpdate) {
		return baseDao.lastList(new SmartQueryWrapper<TweetUser>()
				.in(TweetUser.USERNAME, usernames)
				// 更新间隔 < 1 小时 的拥挤
				.apply(limitLastUpdate, " TIMESTAMPDIFF(HOUR, update_time, NOW()) < {0}", 1)
		);
	}

	public List<String> lastList(Collection<?> excludeUsernames, Integer limit, boolean limitUpdateTime) {
		return baseDao.lastList(new SmartQueryWrapper<TweetUser>()
				.notIn(!CollectionUtils.isEmpty(excludeUsernames), TweetUser.USERNAME, excludeUsernames)
				// 更新间隔必须 > 10小时
				.apply(limitUpdateTime, " TIMESTAMPDIFF(HOUR, update_time, NOW()) > {0}", 10)
				.orderByDesc(TweetUser.TYPE)
				.orderByDesc(TweetUser.TWEET_LAST_TIME)
				.last("LIMIT " + limit)
		);
	}

	@Transactional
	public void sync(List<TweetUserInfo> users, Date now) {
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
						.set(TweetUser.UPDATE_TIME, now)
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
							.set(X.isValid(user.publicMetrics.likeCount), TweetUser.LIKES, user.publicMetrics.likeCount)
							.set(TweetUser.UPDATE_TIME, now);
				} else {
					log.warn("【{}】用户无统计数据：{}", user.id, Jsons.encode(user));
				}
				wrappers.add(wrapper);
			}
			super.updateBatchByWrappers(wrappers);
		}
	}

	public static int calcScore(Integer followersCount) {
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

	@Transactional
	public void batchSyncUserInfo(Messager<List<TweetUserInfo>> usersMsg, Date now, int retry) {
		final List<TweetUserInfo> allUser = usersMsg.data();
		final HashSet<String> allUsername = CollectionUtil.toSet(allUser, TweetUserInfo::getUsername);
		// 已经存在的用户
		final List<String> existsUsernames = findByUsername(allUsername, false);

		int size = existsUsernames.size();
		List<TweetUserInfo> existsUsers = new ArrayList<>(size);
		List<TweetUser> newUsers = new ArrayList<>(allUser.size() - size);
		for (TweetUserInfo user : allUser) {
			if (CollectionUtil.findFirst(existsUsernames, u -> u.equalsIgnoreCase(user.username)) != null) {
				// 添加到已存在用户列表
				existsUsers.add(user);
			} else {
				TweetUser tweetUser = new TweetUser()
						.setProviderType(TweetProvider.TWEET)
						.setUserId(user.id)
						.setUsername(user.username)
						.setNickname(user.name)
						.setAvatar(user.profileImageUrl)
						.setBanner(user.profileBannerUrl)
						.setPinned(StringUtil.length(user.pinnedTweetId) > 0 ? ("[\"" + user.pinnedTweetId + "\"]") : null)
						.setLocation(user.location)
						.setDescription(Jsons.encode(JSONObject.of("text", user.description)));
				if (user.publicMetrics != null) {
					final Integer followersCount = NumberUtil.getInteger(user.publicMetrics.followersCount, 0);
					int score = calcScore(followersCount);
					tweetUser.setFollowers(user.publicMetrics.followersCount)
							.setTweets(user.publicMetrics.tweetCount)
							.setFollowing(user.publicMetrics.followingCount)
							.setMedia(user.publicMetrics.mediaCount)
							.setListed(user.publicMetrics.listedCount)
							.setLikes(user.publicMetrics.likeCount)
							.setTweetLastTime(now)
							.setType(TweetUserType.SPECIAL)
							.setScore(BigDecimal.valueOf(score))
							.initTime(now);
				}
				newUsers.add(tweetUser);
			}
		}

		try {
			super.saveBatch(newUsers);
		} catch (DuplicateKeyException e) { // 违反唯一约束
			if (--retry > 0) {
				batchSyncUserInfo(usersMsg, now, retry);
				return;
			}
			log.warn("【同步用户】违反唯一约束，入库失败！");
		}
		this.sync(existsUsers, now);
		log.info("【同步用户】：新增{}个，更新{}个", newUsers.size(), existsUsers.size());
	}

}
