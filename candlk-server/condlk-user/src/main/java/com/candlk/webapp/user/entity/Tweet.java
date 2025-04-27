package com.candlk.webapp.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.candlk.webapp.base.entity.BaseEntity;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
* 推文信息表
* @author 
* @since 2025-04-27
*/
@Setter
@Getter
public class Tweet extends BaseEntity {

	/** 推文ID */
	String tweetId;
	/** 推文类型：0=发帖；1=回复；2=引用；3=转发 */
	Integer type;
	/** 推文来源厂商类型 */
	Integer providerType;
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
	/** 推特用户账号名 */
	String author;


	public static final String TWEETID = "tweetId";
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
	public static final String AUTHOR = "author";
}
