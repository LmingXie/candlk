package com.candlk.context.model;

/***
 * 全局共享的 Redis 键
 */
public interface RedisKey {

	/** APP审核版本 */
	String APP_VERSION = "app_version:";

	/** 用户操作分布式锁前缀 */
	String USER_OP_LOCK_PREFIX = "user:lock:";
	/** 商户站点状态 ZSet < merchantId,status > */
	String MERCHANT_SITE_STATUS = "merchant:site:status";
	/** 系统开关 < Set < $开关名称 > > */
	String SYS_SWITCH = "sysSwitch";
	/** 推文评分开关 */
	String TWEET_SCORE_FLAG = "tweetScoreFlag";

}
