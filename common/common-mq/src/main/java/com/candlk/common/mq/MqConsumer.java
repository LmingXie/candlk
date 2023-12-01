package com.candlk.common.mq;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MQ消费者（消息监听器）
 */
public interface MqConsumer extends MessageListener {

	Logger LOGGER = LoggerFactory.getLogger(MqConsumer.class);

	MqTagDescriptor getSubscription();

	@Override
	default Action consume(Message message, ConsumeContext context) {
		Action result = null;
		Exception ex = null;
		try {
			result = doConsume(message, context);
			return result;
		} catch (Exception e) {
			ex = e;
			return Action.ReconsumeLater;
		} finally {
			if (result != null) {
				LOGGER.info("收到MQ消息：key={}，msgId={}，结果={}", message.getKey(), message.getMsgID(), result);
			} else {
				LOGGER.error("收到MQ消息：key=" + message.getKey() + "，msgId=" + message.getMsgID() + "，处理时出错", ex);
			}
		}
	}

	Action doConsume(Message message, ConsumeContext context) throws Exception;

}
