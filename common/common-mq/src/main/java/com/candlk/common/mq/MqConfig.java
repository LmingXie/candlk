package com.candlk.common.mq;

import java.util.*;
import java.util.function.Function;

import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import com.aliyun.openservices.ons.api.bean.ConsumerBean;
import com.aliyun.openservices.ons.api.bean.Subscription;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.X;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.*;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "rocketmq")
@Slf4j
public class MqConfig {

	String accessKey;
	String secretKey;
	String nameSrvAddr;

	public Properties toProperties() {
		Properties props = new Properties();
		props.setProperty(PropertyKeyConst.AccessKey, this.accessKey);
		props.setProperty(PropertyKeyConst.SecretKey, this.secretKey);
		props.setProperty(PropertyKeyConst.NAMESRV_ADDR, this.nameSrvAddr);
		return props;
	}

	public Properties getProperties() {
		return toProperties();
	}

	@Bean(initMethod = "start", destroyMethod = "shutdown")
	@Lazy
	public MqProducer producerBean() {
		MqProducer producer = new MqProducer();
		producer.setProperties(toProperties());
		log.info("启动MQ生产者");
		return producer;
	}

	/**
	 * 初始化 RocketMQ 消费者【要符合订阅关系一致原则】
	 * <p>
	 * 【订阅关系一致】 https://help.aliyun.com/document_detail/43523.html
	 * 【使用消息队列RocketMQ版实例时订阅关系不一致】 https://help.aliyun.com/document_detail/29641.html
	 */
	@Bean(initMethod = "start", destroyMethod = "shutdown")
	public ConsumerBean consumerBean(ConfigurableApplicationContext applicationContext, @Autowired(required = false) List<MqConsumer> mqConsumers) {
		final int size = X.size(mqConsumers);
		if (size == 0) {
			return null;
		}
		// 目前 只有 相同的 groupId 才能共用同一个 ConsumerBean
		Map<String, Map<Subscription, MessageListener>> groupSubscriptionTableMap = new LinkedHashMap<>(mqConsumers.size(), 1F);
		final Function<String, Map<Subscription, MessageListener>> mapBuilder = k -> new HashMap<>(2, 1F);
		for (MqConsumer c : mqConsumers) {
			final MqTagDescriptor descriptor = c.getSubscription();
			final Map<Subscription, MessageListener> subscriptionTable = groupSubscriptionTableMap.computeIfAbsent(descriptor.getGroupId(), mapBuilder);
			Subscription subscription = new Subscription();
			subscription.setTopic(descriptor.getTopic());
			subscription.setExpression(descriptor.getTags());
			subscriptionTable.put(subscription, c);
		}

		ConsumerBean primary = null;
		for (Map.Entry<String, Map<Subscription, MessageListener>> entry : groupSubscriptionTableMap.entrySet()) {
			final String groupId = entry.getKey();
			final Map<Subscription, MessageListener> table = entry.getValue();
			// 配置文件
			Properties properties = toProperties();
			properties.setProperty(PropertyKeyConst.GROUP_ID, groupId);
			log.info("注册MQ消费者={}", table);

			// 第一个Bean先直接实例化，多余的才通过 Spring 动态注册
			if (primary == null) {
				primary = new ConsumerBean();
				primary.setProperties(properties);
				primary.setSubscriptionTable(table);
				continue;
			}

			final AbstractBeanDefinition definition = BeanDefinitionBuilder.rootBeanDefinition(ConsumerBean.class)
					.addPropertyValue("properties", properties)
					.addPropertyValue("subscriptionTable", table)
					.setInitMethodName("start")
					.setDestroyMethodName("shutdown")
					.getBeanDefinition();
			((BeanDefinitionRegistry) applicationContext).registerBeanDefinition("ConsumerBean#" + groupId, definition);
		}
		return primary;
	}

}
