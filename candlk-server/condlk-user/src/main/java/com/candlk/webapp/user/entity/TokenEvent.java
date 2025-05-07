package com.candlk.webapp.user.entity;

import com.candlk.webapp.base.entity.TimeBasedEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * 代币事件表
 *
 * @since 2025-04-27
 */
@Setter
@Getter
@Accessors(chain = true)
public class TokenEvent extends TimeBasedEntity {

	/** 推文ID */
	Long tweetId;
	/** 事件类型：0=特殊关注推；1=热门评分推文；2=浏览猛增推文； */
	Integer type;
	/** 代币名称 */
	String coin;
	/** 代币简称 */
	String symbol;
	/** 代币地址 */
	String ca;
	/** 代币简介 */
	String desc;
	/** 业务状态（0=待创建；1=已创建） */
	Integer status;

	public static final int TYPE_SPECIAL = 0, TYPE_HOT = 1, TYPE_SURGE = 2;
	public static final int CREATE = 0, CREATED = 1;

	public static final String TWEET_ID = "tweet_id";
	public static final String TYPE = "type";
	public static final String COIN = "coin";
	public static final String SYMBOL = "symbol";
	public static final String CA = "ca";
	public static final String DESC = "desc";
	public static final String STATUS = "status";

}
