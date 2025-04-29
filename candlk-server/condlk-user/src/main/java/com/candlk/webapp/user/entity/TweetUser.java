package com.candlk.webapp.user.entity;

import java.util.Date;

import com.candlk.webapp.base.entity.TimeBasedEntity;
import com.candlk.webapp.user.model.TweetProvider;
import com.candlk.webapp.user.model.TweetUserType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 推特用户表
 *
 * @since 2025-04-27
 */
@Setter
@Getter
@Accessors(chain = true)
public class TweetUser extends TimeBasedEntity {

	/** 推文来源厂商类型 */
	TweetProvider providerType;
	/** 推特用户ID */
	String userId;
	/** 推特用户账号名 */
	String username;
	/** 推特昵称 */
	String nickname;
	/** 推特头像 */
	String avatar;
	/** 推特横幅 */
	String banner;
	/** 置顶推文（JSON） */
	String pinned;
	/** 地区 */
	String location;
	/** 推特简介 */
	String description;
	/** 关注该用户的用户数 */
	Integer followers;
	/** 该用户发布的帖子数（包括转推） */
	Integer tweets;
	/** 该用户关注的用户数 */
	Integer following;
	/** 该用户发布的媒体数 */
	Integer media;
	/** 包含该用户的列表数量 */
	Integer listed;
	/** 该用户创建的赞数 */
	Integer likes;
	/** 最后一次发帖时间 */
	Date tweetLastTime;
	/** 账号类型：0=普通账号；1=特殊关注账号；2=二级账号； */
	TweetUserType type;
	/** 业务标识 */
	Integer bizFlag;

	public static final String LOCATION = "location";
	public static final String BANNER = "banner";
	public static final String PINNED = "pinned";
	public static final String USERID = "user_id";
	public static final String USERNAME = "username";
	public static final String NICKNAME = "nickname";
	public static final String AVATAR = "avatar";
	public static final String DESCRIPTION = "description";
	public static final String FOLLOWERS = "followers";
	public static final String TWEETS = "tweets";
	public static final String FOLLOWING = "following";
	public static final String MEDIA = "media";
	public static final String LISTED = "listed";
	public static final String LIKES = "likes";
	public static final String TWEET_LAST_TIME = "tweet_last_time";
	public static final String TYPE = "type";
	public static final String BIZ_FLAG = "biz_flag";

}
