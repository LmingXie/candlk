package com.candlk.webapp.user.entity;

import java.util.Date;

import com.candlk.webapp.base.entity.TimeBasedEntity;
import lombok.Getter;
import lombok.Setter;

/**
* 推特用户表
* @author 
* @since 2025-04-27
*/
@Setter
@Getter
public class TweetUser extends TimeBasedEntity {

	/** 推特用户ID */
	String userId;
	/** 推特用户账号名 */
	String author;
	/** 推特昵称 */
	String nickname;
	/** 推特头像 */
	String avatar;
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
	/** 最后一次发帖时间 */
	Date tweetLastTime;
	/** 事件类型：0=特殊关注账号；1=普通账号 */
	Integer type;
	/** 业务标识 */
	Integer bizFlag;


	public static final String USERID = "userId";
	public static final String AUTHOR = "author";
	public static final String NICKNAME = "nickname";
	public static final String AVATAR = "avatar";
	public static final String DESCRIPTION = "description";
	public static final String FOLLOWERS = "followers";
	public static final String TWEETS = "tweets";
	public static final String FOLLOWING = "following";
	public static final String MEDIA = "media";
	public static final String TWEET_LAST_TIME = "tweet_last_time";
	public static final String TYPE = "type";
	public static final String BIZ_FLAG = "biz_flag";
}
