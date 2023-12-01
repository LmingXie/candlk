package com.candlk.common.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Redis 配置
 */
@Configuration
public class RedisConfiguration {

	/**
	 * {@link org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration#setDefaultRedisSerializer}
	 */
	@Bean
	@ConditionalOnMissingBean(name = "springSessionDefaultRedisSerializer") // 支持覆盖替换
	public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
		return new FastJson2RedisSerializer();
	}

	/**
	 * @see org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration#redisTemplate(RedisConnectionFactory)
	 */
	@Bean
	public RedisTemplate<String, ?> redisTemplate(RedisConnectionFactory connectionFactory, RedisSerializer<Object> springSessionDefaultRedisSerializer) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		// 设置 序列化器
		final RedisSerializer<String> stringRedisSerializer = RedisSerializer.string();
		template.setStringSerializer(stringRedisSerializer);
		template.setKeySerializer(stringRedisSerializer);
		template.setHashKeySerializer(stringRedisSerializer);

		template.setDefaultSerializer(springSessionDefaultRedisSerializer);
		return template;
	}

	/**
	 * @see org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration#stringRedisTemplate(RedisConnectionFactory)
	 */
	@Bean
	public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		StringRedisTemplate template = new StringRedisTemplate();
		template.setConnectionFactory(redisConnectionFactory);
		RedisUtil.setStringRedisTemplate(template);
		return template;
	}

	@Bean
	public RedissonClient redissonClient(org.springframework.boot.autoconfigure.data.redis.RedisProperties redisProperties) {
		Config config = new Config();
		String redisUrl = String.format("redis://%s:%s", redisProperties.getHost(), redisProperties.getPort());
		config.useSingleServer()
				.setAddress(redisUrl)
				.setPassword(redisProperties.getPassword()) // 默认=null
				.setDatabase(Math.min(redisProperties.getDatabase() + 8, 15)) // 默认=0
				.setConnectionMinimumIdleSize(4) // 默认=24
				.setConnectionPoolSize(16); // 默认=64
		final RedissonClient redissonClient = Redisson.create(config);
		RedisUtil.setClient(redissonClient);
		return redissonClient;
	}

}
