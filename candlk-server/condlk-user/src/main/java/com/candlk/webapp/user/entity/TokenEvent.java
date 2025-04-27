package com.candlk.webapp.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.candlk.webapp.base.entity.BaseEntity;
import java.util.Date;

import com.candlk.webapp.base.entity.TimeBasedEntity;
import lombok.Getter;
import lombok.Setter;

/**
* 代币事件表
* @author 
* @since 2025-04-27
*/
@Setter
@Getter
public class TokenEvent extends TimeBasedEntity {

	/** 推文ID */
	Long tweetId;
	/** 事件类型：0=特殊关注账号；1=热门推文；2=浏览量猛增； */
	Integer type;
	/** 代币名称 */
	String coin;
	/** 代币简称 */
	String symbol;
	/** 代币地址 */
	String ca;
	/** 代币简介 */
	String desc;
	/** 业务状态 */
	Integer status;


	public static final String TWEETID = "tweetId";
	public static final String TYPE = "type";
	public static final String COIN = "coin";
	public static final String SYMBOL = "symbol";
	public static final String CA = "ca";
	public static final String DESC = "desc";
	public static final String STATUS = "status";
}
