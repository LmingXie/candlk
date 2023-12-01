package com.candlk.common.mq;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.SendResult;
import com.aliyun.openservices.ons.api.bean.ProducerBean;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.EasyDate;

/**
 * MQ生产者通用封装
 */
@Slf4j
public class MqProducer extends ProducerBean {

	/**
	 * 发送消息到 MQ，【可延迟】投递给消费者
	 *
	 * @param payload 数据载体对象
	 * @param tag MQ Tag描述符
	 * @param delayOffsetInMs 延迟投递时间。如果设为正整数，将会在当前时间的指定毫秒数后投递消息给消费者（ 0 或 负数将可能会立即投递）
	 */
	public SendResult send(MqPayload payload, MqTagDescriptor tag, long delayOffsetInMs) {
		return send(payload, payload.mqUniqueKey(), tag, delayOffsetInMs);
	}

	/**
	 * 发送消息到 MQ，【可延迟】投递给消费者
	 *
	 * @param payload 数据载体对象
	 * @param tag MQ Tag描述符
	 * @param delayOffsetInMs 延迟投递时间。如果设为正整数，将会在【当前时间】的指定毫秒数后投递消息给消费者（ 0 或 负数将可能会立即投递）
	 */
	public <T> SendResult send(T payload, String uniqueKey, MqTagDescriptor tag, long delayOffsetInMs) {
		return sendDelay(payload, uniqueKey, tag, delayOffsetInMs > 0 ? System.currentTimeMillis() + delayOffsetInMs : 0);
	}

	/**
	 * 发送消息到 MQ，【可延迟】投递给消费者
	 *
	 * @param payload 数据载体对象
	 * @param tag MQ Tag描述符
	 * @param consumeTime 投递到消费者的时间（毫秒级时间戳）。这里是绝对值，直接取 {@link Date#getTime()} 即可，不是相对于当前时间的差值。如果为 0 或负数，则表示不延迟，理解投递
	 */
	public <T> SendResult sendDelay(T payload, String uniqueKey, MqTagDescriptor tag, long consumeTime) {
		Message msg = new Message(tag.getTopic(), tag.getTags(), JSON.toJSONBytes(payload));
		msg.setKey(uniqueKey);
		if (consumeTime > 0) {
			msg.setStartDeliverTime(consumeTime);
		}
		Exception ex = null;
		SendResult result = null;
		try {
			result = super.send(msg);
		} catch (Exception e) {
			ex = e;
		} finally {
			final String timeStr = consumeTime > 0 ? new EasyDate(consumeTime).toLongString() : null;
			if (result != null) {
				if (timeStr != null) {
					log.info("发送MQ消息：key={}，延迟时间={}，msgId={}", uniqueKey, timeStr, result.getMessageId());
				} else {
					log.info("发送MQ消息：key={}，msgId={}", uniqueKey, result.getMessageId());
				}
			} else {
				if (timeStr != null) {
					log.error("发送MQ消息：key=" + uniqueKey + "，延迟时间=" + timeStr + "，失败", ex);
				} else {
					log.error("发送MQ消息：key=" + uniqueKey + "，失败", ex);
				}
			}
		}

		return result;
	}

	/**
	 * 发送消息到 MQ，【可延迟】投递给消费者
	 *
	 * @param payload 数据载体对象
	 * @param tag MQ Tag描述符
	 * @param consumeTime 投递到消费者的时间（毫秒级时间戳）。这里是绝对值，直接取 {@link Date#getTime()} 即可，不是相对于当前时间的差值。如果为 0 或负数，则表示不延迟，理解投递
	 */
	public SendResult sendDelay(MqPayload payload, MqTagDescriptor tag, long consumeTime) {
		return sendDelay(payload, payload.mqUniqueKey(), tag, consumeTime);
	}

	/**
	 * 发送消息到 MQ，并允许【立即】投递
	 *
	 * @param payload 数据载体对象
	 * @param tag MQ Tag描述符
	 */
	public <T> SendResult send(T payload, String uniqueKey, MqTagDescriptor tag) {
		return sendDelay(payload, uniqueKey, tag, 0);
	}

	/**
	 * 发送消息到 MQ，并允许【立即】投递
	 *
	 * @param payload 数据载体对象
	 * @param tag MQ Tag描述符
	 */
	public SendResult send(MqPayload payload, MqTagDescriptor tag) {
		return sendDelay(payload, payload.mqUniqueKey(), tag, 0);
	}

}
