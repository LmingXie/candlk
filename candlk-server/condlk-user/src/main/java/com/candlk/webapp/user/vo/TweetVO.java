package com.candlk.webapp.user.vo;

import java.math.BigDecimal;
import java.util.Date;

import com.candlk.webapp.base.vo.AbstractVO;
import com.candlk.webapp.user.entity.Tweet;
import com.candlk.webapp.user.model.TweetProvider;
import com.candlk.webapp.user.model.TweetUserType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TweetVO extends AbstractVO<Tweet> {

	/** 业务状态（0=待创建；1=已创建） */
	Integer status;
	/** 推文ID */
	String tweetId;
	/** 类型：0=特殊关注推；1=热门评分推文；2=浏览猛增推文； */
	Integer type;
	/** 代币名称 */
	String coin;
	/** 代币简称 */
	String symbol;
	/** 代币地址 */
	String ca;
	/** 代币简介 */
	String desc;

	/** 推文来源厂商类型 */
	TweetProvider providerType;
	/** 推特用户账号名 */
	String username;
	/** 推文内容 */
	String text;
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
	/** 推文发布时间 */
	Date addTime;
	/** 最后更新时间 */
	Date updateTime;
	/** 推文图片 */
	String images;
	/** 推文视频 */
	String videos;
	/** 推文分数 */
	BigDecimal score;
	/** 命中的关键词 */
	String words;

	/** 推特头像 */
	String avatar;
	/** 账号类型：0=普通账号；1=二级账号；2=特殊关注账号； */
	TweetUserType userType;
	/** 关注该用户的用户数 */
	Integer followers;
	/** 用户分数 */
	BigDecimal userScore;

}