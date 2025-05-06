package com.candlk.webapp.user.model;

import com.candlk.context.model.RedisKey;

public interface UserRedisKey extends RedisKey {

	/**
	 * 浏览量猛增推文 API 限流 ZSet
	 * <pre>
	 * 限流：< $api, $lastTime >
	 * 端点： < point, $counter % $pointTime >
	 *     计数器（6位）： 取余 1_000_00
	 *     秒级时间戳（10位）： 除以 1_000_00 取整
	 * </pre>
	 */
	String SURGE_TWEET_API_LIMIT = "surgeTweetApiLimit";

}
