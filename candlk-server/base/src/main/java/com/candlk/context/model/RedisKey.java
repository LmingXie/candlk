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
	/**
	 * 推特新用户账号 < Set < $username > >
	 */
	String TWEET_NEW_USERS = "tweetNewUsers";

}
