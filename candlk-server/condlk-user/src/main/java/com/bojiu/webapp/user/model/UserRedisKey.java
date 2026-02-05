package com.bojiu.webapp.user.model;

import com.bojiu.context.model.RedisKey;

public interface UserRedisKey extends RedisKey {

	/** 用户信息 */
	String USER_INFO = "users";
	/** 靓号地址余额统计 ZSet < $address, $score > */
	String ADDRESS_BALANCE_STAT_PERIFX = "addressBalanceStat:";

	/** 用户转账记录 From -> To ZSet < $address, $score > */
	String TRANSFER_KEY = "transfer:";

}