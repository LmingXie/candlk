package com.candlk.common.test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import javax.annotation.Nonnull;

import com.candlk.common.redis.FastJson2RedisSerializer;
import com.candlk.common.redis.RedisUtil;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * 【Redis访问测试】基于 原生 Java 运行环境的已集成 Lettuce 依赖的测试基类
 */
@Getter
public class RedisTest {

	public String host = "192.168.0.99";
	public int port = 6379;
	public String username;
	public String password = "sakFsl#89jflka";
	public int db = 0;

	private RedisClient redisClient;
	private RedisTemplate<String, Object> redisTemplate;

	public static RedisClient createClient(String host, int port, String username, String password, int database) {
		final RedisURI.Builder builder = RedisURI.Builder.redis(host, port)
				.withDatabase(database)
				.withTimeout(Duration.of(10, ChronoUnit.SECONDS));
		if (username != null) {
			builder.withAuthentication(username, password);
		} else {
			builder.withPassword(password.toCharArray());
		}
		RedisURI redisUri = builder.build();
		return RedisClient.create(redisUri);
	}

	@Test
	public void test() {
		final RedisTemplate<String, Object> template = getRedisTemplate();
		final RedisScript<Integer> getAndDelete = new DefaultRedisScript<>(
				"local a = redis.call('HGET', KEYS[1], KEYS[2]);" +
				"if (a) then " +
				"redis.call('HDEL', KEYS[1], KEYS[2]);" +
				"end;" +
				"local b = redis.call('GET', KEYS[3]);" +
				"if (b) then " +
				"   redis.call('DEL', KEYS[3]);" +
				"end;" +
				"return a and b and tostring(a + b) or a or b;"
				, Integer.class);

		// 表示另一方回复，获取发送方发送记录，清除 从头开始
		final Integer val = template.execute(getAndDelete, Arrays.asList("a", "b", "c"));
		System.out.println("return " + val);
	}

	@Test
	public void testRedisClient() {
		StatefulRedisConnection<String, String> conn = getRedisClient().connect();
		RedisCommands<String, String> commands = conn.sync();
		String key = "common-test";
		//	commands.set(key, "HelloWorld");
		// System.out.println(commands.get(key));
		System.out.println(commands.del(key));
		conn.close();
	}

	public RedisClient getRedisClient() {
		if (redisClient == null) {
			redisClient = createClient(host, port, username, password, db);
		}
		return redisClient;
	}

	public RedisTemplate<String, Object> getRedisTemplate() {
		if (redisTemplate == null) {
			redisTemplate = createRedisTemplate(host, port, username, password, db);
		}
		return redisTemplate;
	}

	@Nonnull
	public static RedisTemplate<String, Object> createRedisTemplate(String host, int port, String username, String password, int db) {
		RedisStandaloneConfiguration cfg = new RedisStandaloneConfiguration();
		cfg.setHostName(host);
		cfg.setPort(port);
		cfg.setUsername(username);
		cfg.setPassword(password);
		cfg.setDatabase(db);
		LettuceConnectionFactory fac = new LettuceConnectionFactory(cfg);
		fac.afterPropertiesSet();

		// 设置 序列化器
		final RedisSerializer<String> stringRedisSerializer = RedisSerializer.string();
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setStringSerializer(stringRedisSerializer);
		template.setKeySerializer(stringRedisSerializer);
		template.setHashKeySerializer(stringRedisSerializer);
		template.setDefaultSerializer(new FastJson2RedisSerializer());

		template.setConnectionFactory(fac);
		template.afterPropertiesSet();
		return template;
	}

	public static RedissonClient createRedissonClient(String host, int port, String username, String password, int db) {
		Config config = new Config();
		String redisUrl = String.format("redis://%s:%s", host, port);
		config.useSingleServer()
				.setAddress(redisUrl)
				.setUsername(username)
				.setPassword(password)
				.setDatabase(db)
				.setSubscriptionConnectionPoolSize(4)
				.setConnectionMinimumIdleSize(4)
				.setConnectionPoolSize(16);
		final RedissonClient redissonClient = Redisson.create(config);
		RedisUtil.setClient(redissonClient);
		return redissonClient;
	}

	@AfterEach
	public void close() {
		if (redisClient != null) {
			redisClient.shutdown();
		}
	}

}
