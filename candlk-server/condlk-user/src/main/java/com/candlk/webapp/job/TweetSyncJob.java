package com.candlk.webapp.job;

import java.math.BigDecimal;
import java.util.*;
import javax.annotation.Resource;

import com.alibaba.fastjson2.JSONObject;
import com.candlk.common.model.Messager;
import com.candlk.common.util.Common;
import com.candlk.context.web.Jsons;
import com.candlk.webapp.api.TweetApi;
import com.candlk.webapp.api.TweetUserInfo;
import com.candlk.webapp.user.entity.Tweet;
import com.candlk.webapp.user.entity.TweetUser;
import com.candlk.webapp.user.model.TweetProvider;
import com.candlk.webapp.user.model.TweetUserType;
import com.candlk.webapp.user.service.TweetService;
import com.candlk.webapp.user.service.TweetUserService;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.*;
import org.springframework.scheduling.annotation.Scheduled;

import static com.candlk.webapp.user.service.TweetUserService.calcScore;

@Slf4j
// @Configuration
public class TweetSyncJob {

	@Resource
	TweetService tweetService;
	@Resource
	TweetUserService tweetUserService;

	static final TweetApi tweetApi = new TweetApi("AAAAAAAAAAAAAAAAAAAAAK450wEAAAAAc6mb7tVQF3%2B6ssbH2borX%2F0jQpI%3DrxKSmb3okKK49o5Z1P4RFZXgIK08FXXhc1XjSbVrJd1y0bo1sp",
			"http://127.0.0.1:10809");

	/**
	 * 同步推文信息（生产：1 m/次；本地：15 m/次）
	 */
	@Scheduled(cron = "${service.cron.TweetSyncJob:0 0/1 * * * ?}")
	public void run() {
		log.info("开始同步推文数据信息...");// 查询前100条推文
		List<Tweet> oldTweets = tweetService.lastList(100);
		// final String tweetIds = StringUtil.join(oldTweets, Tweet::getTweetId, ",");
		//
		// log.info("推文ID：{}", tweetIds);
		// Messager<List<TweetInfo>> tweetsMsg = tweetApi.tweets(tweetIds);
		//
		// log.info("推文：{}", Jsons.encode(tweetIds));
		// if (tweetsMsg == null || !tweetsMsg.isOK()) {
		// 	log.warn("推文获取失败！");
		// 	return null;
		// }
		//
		// // 将最新推文数据刷新到DB
		// List<TweetInfo> tweets = tweetsMsg.data();
		// tweetService.sync(tweets, false);
		tweetService.syncStatus(oldTweets);

		// 同步用户数据【max100】
		Set<String> usernames = Common.toSet(oldTweets, Tweet::getUsername);
		// 排除 1 小时内更新过的用户
		List<String> filterLastUser = tweetUserService.findByUsername(usernames, true);
		filterLastUser.forEach(usernames::remove);

		int diff = 100 - usernames.size();
		if (diff > 0) {
			// 查询最近更新过帖子的用户【更新时间10】
			List<String> userIds = tweetUserService.lastList(usernames, diff, true);
			if (!userIds.isEmpty()) {
				usernames.addAll(userIds);
			}
			diff = 100 - usernames.size();
			if (diff > 0) {
				// 查询最近更新过帖子的用户【不限制更新时间】
				List<String> userIds2 = tweetUserService.lastList(usernames, diff, false);
				if (!userIds2.isEmpty()) {
					usernames.addAll(userIds2);
				}
			}
		}
		final Date now = new Date();
		// 同步用户信息数据
		Messager<List<TweetUserInfo>> usersMsg = tweetApi.usersByUsernames(StringUtil.joins(usernames, ","));
		if (usersMsg.isOK()) {
			final List<TweetUserInfo> allUser = usersMsg.data();
			final HashSet<String> allUsername = CollectionUtil.toSet(allUser, TweetUserInfo::getUsername);
			// 已经存在的用户
			final List<String> existsUsernames = tweetUserService.findByUsername(allUsername, false);

			int size = existsUsernames.size();
			List<TweetUserInfo> existsUsers = new ArrayList<>(size);
			List<TweetUser> newUsers = new ArrayList<>(allUser.size() - size);
			for (TweetUserInfo user : allUser) {
				if (existsUsernames.contains(user.getUsername())) {
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

			tweetUserService.saveBatch(newUsers);
			tweetUserService.sync(existsUsers, now);
			log.info("同步用户：新增{}个，更新{}个", newUsers.size(), existsUsers.size());
		}
		log.info("结束同步推文数据任务。");
	}

}
