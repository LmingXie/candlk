package com.bojiu.context.model;

import com.bojiu.common.redis.Permanent;

/***
 * 全局共享的 Redis 键
 */
public interface RedisKey {

	/** APP审核版本 */
	@Permanent
	String APP_VERSION_PREFIX = "appVersion:";

	/** 用户操作分布式锁前缀 */
	String USER_OP_LOCK_PREFIX = "user:lock:";
	/**
	 * 商户站点域名 Hash
	 * <p> < "域名", 商户ID >
	 * 【注意】域名存入之前一定要先转小写
	 */
	@Permanent
	String MERCHANT_DOMAINS = "merchantDomains";
	/**
	 * 商户后台域名 Hash
	 * <p> < "域名", 商户ID >
	 * 【注意】域名存入之前一定要先转小写
	 */
	@Permanent
	String MERCHANT_BG_DOMAINS = "merchantBgDomains";

	String dailySep = "-";
	String weeklySep = "W-";
	String monthlySep = "M-";

	/**
	 * 用户【前一】 日 | 周 | 月 的VIP等级 Hash【注意$yyyyMMdd是当前周期！！】
	 * <li> < "userLevel-$yyyyMM", $userId, level >
	 * <li> < "userLevel-$yyyyMMdd", $userId, level >
	 * <li> < "userLevel", $userId, level > level 前两位为更新人工调整等级，后两位为实际升级等级
	 */
	String USER_LEVEL = "userLevel";

	/** 冻结商户名单 Set < $merchantId > */
	String FROZEN_MERCHANT_LIST = "frozen_merchant";
	/**
	 * 渠道统计前缀
	 * ZSet < "channelStat"-$yyyyMMdd-$suffix-$channelId, $value >
	 * Hash < "channelStat"-$yyyyMMdd-$channelId, <$suffix, $value >>
	 */
	String CHANNEL_STAT_PREFIX = "channelStat";

	/** 自研游戏控制taskId计数器 String < "$merchantId",$counter >（单用户唯一，批量操作可以使用相同ID） */
	String GAME_CTRL_TASK_ID_COUNTER = "gameCtrlTaskIdCounter";
	/**
	 * 商户白名单 Hash < $merchantId,  $IPS >
	 */
	String MERCHANT_WHITELIST = "merchant:whitelist";

	/**
	 * 不验证IP白名单的商户ID
	 * Set < "whitelistNotCheck", $merchantId >
	 */
	String WHITELIST_NOT_CHECK = "whitelistNotCheck";

	/** 元数据配置前缀*/
	String META_PREFIX = "meta:";

}