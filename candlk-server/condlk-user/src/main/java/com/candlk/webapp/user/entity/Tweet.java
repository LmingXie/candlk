package com.candlk.webapp.user.entity;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

import com.candlk.context.web.Jsons;
import com.candlk.webapp.base.entity.BaseEntity;
import com.candlk.webapp.user.model.TweetProvider;
import com.candlk.webapp.user.model.TweetType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.dubbo.common.utils.CollectionUtils;

/**
 * 推文信息表
 *
 * @since 2025-04-27
 */
@Setter
@Getter
@Accessors(chain = true)
public class Tweet extends BaseEntity {

	/** 推文ID */
	String tweetId;
	/** 推文来源厂商类型 */
	TweetProvider providerType;
	/** 推文类型：0=发帖；1=回复；2=引用；3=转发 */
	TweetType type;
	/** 推特用户账号名 */
	String username;
	/** 推文内容 */
	String text;
	/** 推文实体：urls=引用图片/视频；mentions=提及的人；hashtags=被识别的标签文本； */
	String entities;
	/** 原始推文数据 */
	String orgMsg;
	/** 此推文被转发的次数 */
	Integer retweet;
	/** 此推文被回复的次数 */
	Integer reply;
	/** 此推文被点赞的次数 */
	Integer likes;
	/** 此推文被引用的次数 */
	Integer quote;
	/** 此推文被收藏的次数 */
	Integer bookmark;
	/** 此推文被浏览的次数 */
	Integer impression;
	/** 业务标识 */
	Long bizFlag;
	/** 推文发布时间 */
	Date addTime;
	/** 最后更新时间 */
	Date updateTime;
	/** 推文图片 */
	String images;
	/** 推文视频 */
	String videos;
	/** 业务状态：0=初始录入；1=同步推文；2=正在进行分析；3=分析结束；4=不合格推文； */
	Integer status;
	/** 推文分数 */
	BigDecimal score;
	/** 命中的关键词 */
	String words;

	public static final int INIT = 0, SYNC = 1, ANALYZING = 2, ANALYZED = 3, QUALITY_NOT_PASS = 4;

	public static Tweet of(TweetProvider providerType, TweetType tweetType, String username, String tweetId, String text, String entities, String orgMsg, Date addTime) {
		Tweet tweet = new Tweet();
		tweet.setProviderType(providerType);
		tweet.setType(tweetType);
		tweet.setUsername(username);
		tweet.setTweetId(tweetId);
		tweet.setText(text);
		tweet.setEntities(entities);
		tweet.setOrgMsg(orgMsg);
		tweet.setAddTime(addTime);
		tweet.setUpdateTime(addTime);
		return tweet;
	}

	public Tweet setVideos(Collection<?> videos) {
		this.videos = CollectionUtils.isEmpty(videos) ? null : Jsons.encode(videos);
		return this;
	}

	public Tweet setImages(Collection<?> images) {
		this.images = CollectionUtils.isEmpty(images) ? null : Jsons.encode(images);
		return this;
	}

	public static final String TWEET_ID = "tweet_id";
	public static final String TYPE = "type";
	public static final String PROVIDER_TYPE = "provider_type";
	public static final String TEXT = "text";
	public static final String ENTITIES = "entities";
	public static final String ORG_MSG = "org_msg";
	public static final String RETWEET = "retweet";
	public static final String REPLY = "reply";
	public static final String LIKES = "likes";
	public static final String QUOTE = "quote";
	public static final String BOOKMARK = "bookmark";
	public static final String IMPRESSION = "impression";
	public static final String BIZ_FLAG = "biz_flag";
	public static final String ADD_TIME = "add_time";
	public static final String UPDATE_TIME = "update_time";
	public static final String USERNAME = "username";
	public static final String STATUS = "status";
	public static final String SCORE = "score";
	public static final String WORDS = "words";

}
