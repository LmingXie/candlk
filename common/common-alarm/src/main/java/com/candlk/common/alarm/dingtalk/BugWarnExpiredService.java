package com.candlk.common.alarm.dingtalk;

/**
 * 超时检测服务
 */
public interface BugWarnExpiredService {

	String CACHE_NAME = "ALARM_";

	/**
	 * 是否可以发送
	 *
	 * @param uniqueKey 唯一标识消息内容的 key
	 */
	boolean canSend(String uniqueKey);

}
