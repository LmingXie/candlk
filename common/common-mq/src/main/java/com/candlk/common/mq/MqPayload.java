package com.candlk.common.mq;

/**
 * MQ 数据荷载抽象标识
 */
public interface MqPayload {

	/**
	 * 唯一标识该消息的唯一 key
	 */
	String mqUniqueKey();

}
