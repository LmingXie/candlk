package com.candlk.common.redis;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONReader;
import com.candlk.common.model.Messager;
import me.codeplayer.util.X;
import org.redisson.api.*;
import org.redisson.client.codec.StringCodec;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.convert.Converters;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * Redis 工具方法类
 */
public abstract class RedisUtil {

	public static <T> Supplier<T> wrapTask(Runnable task) {
		return () -> {
			task.run();
			return null;
		};
	}

	static StringRedisTemplate stringRedisTemplate;
	static HashOperations<String, String, String> redisHash;
	static RedissonClient client;

	public static RedissonClient getClient() {
		return client;
	}

	public static void setClient(RedissonClient client) {
		RedisUtil.client = client;
	}

	public static StringRedisTemplate getStringRedisTemplate() {
		return stringRedisTemplate;
	}

	public static HashOperations<String, String, String> opsForHash() {
		return redisHash;
	}

	public static void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
		RedisUtil.stringRedisTemplate = stringRedisTemplate;
		RedisUtil.redisHash = stringRedisTemplate.opsForHash();
	}

	public static void doInLock(String key, long expireTimeInMs, Runnable task) {
		loadInLock(key, expireTimeInMs, wrapTask(task));
	}

	public static void doInLock(String key, Runnable task) {
		loadInLock(key, 30_000L, wrapTask(task));
	}

	public static <T> T loadInLock(String key, Supplier<T> task) {
		return loadInLock(key, 30_000L, task);
	}

	public static <T> T loadInLock(String key, long expireTimeInMs, Supplier<T> task) {
		final RLock lock = client.getLock(key);
		try {
			lock.lock(expireTimeInMs, TimeUnit.MILLISECONDS);
			return task.get();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 原子性依次执行指定的任务，并屏蔽解锁异常
	 */
	public static <T> T loadInLockAndUnlockSilently(String key, long expireTimeInMs, Supplier<T> task) {
		final RLock lock = client.getLock(key);
		try {
			lock.lock(expireTimeInMs, TimeUnit.MILLISECONDS);
			return task.get();
		} finally {
			try {
				lock.unlock();
			} catch (Exception ignore) {
			}
		}
	}

	/**
	 * 限制连续操作
	 *
	 * @param key 用于标识被限制的连击的 Redis key
	 * @param minIntervalInMs 两次操作的最小时间间隔（毫秒值）
	 * @param task 任务。如果任务报错，则不会视为连击。
	 * @param defaultVal 连续操作被限制时，默认的返回值提供者
	 */
	public static <T> T limitCombo(String key, long minIntervalInMs, Supplier<T> task, @Nullable Supplier<T> defaultVal) {
		boolean locked = getStringRedisTemplate().opsForValue().setIfAbsent(key, "1", minIntervalInMs, TimeUnit.MILLISECONDS);
		if (locked) { // 拿到了锁
			try {
				T val = task.get();
				locked = false; // 成功执行完毕，没有报错，则释放锁
				return val;
			} finally {
				if (!locked) {
					getStringRedisTemplate().delete(key);
				}
			}
		}
		return defaultVal == null ? null : defaultVal.get();
	}

	/**
	 * 限制连续操作
	 *
	 * @param key 用于标识被限制的连击的 Redis key
	 * @param minIntervalInMs 两次操作的最小时间间隔（毫秒值）
	 * @param task 任务。如果任务报错，则不会视为连击。
	 */
	public static <T> T limitCombo(String key, long minIntervalInMs, Supplier<T> task) {
		return limitCombo(key, minIntervalInMs, task, X.castType(defaultValueLoader));
	}

	/**
	 * 3s 内限制连续操作
	 *
	 * @param key 用于标识被限制的连击的 Redis key
	 * @param task 任务。如果任务报错，则不会视为连击。
	 */
	public static <T> T limitCombo(String key, Supplier<T> task) {
		return limitCombo(key, 3000, task, X.castType(defaultValueLoader));
	}

	public static <T> T attemptInLock(String key, long waitTime, long expireTimeInMs, Supplier<T> task, @Nullable Supplier<T> defaultVal) {
		final RLock lock = client.getLock(key);
		boolean locked = false;
		try {
			locked = lock.tryLock(waitTime, expireTimeInMs, TimeUnit.MILLISECONDS);
			if (locked) {
				return task.get();
			}
			return defaultVal == null ? null : defaultVal.get();
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		} finally {
			if (locked) {
				lock.unlock();
			}
		}
	}

	public static <T> T attemptInLock(String key, long expireTimeInMs, Supplier<T> task, @Nullable Supplier<T> defaultVal) {
		return attemptInLock(key, 100, expireTimeInMs, task, defaultVal);
	}

	public static <T> T attemptInLock(String key, long expireTimeInMs, Supplier<T> task) {
		return attemptInLock(key, 100, expireTimeInMs, task, null);
	}

	public static <T> T fastAttemptInLock(String key, Supplier<T> task, @Nullable Supplier<T> defaultVal) {
		return attemptInLock(key, 1, 30_000L, task, defaultVal);
	}

	static Supplier<Messager<?>> defaultValueLoader = () -> Messager.status("busy");

	public static void setDefaultValueLoader(@Nonnull Supplier<Messager<?>> defaultValueLoader) {
		RedisUtil.defaultValueLoader = defaultValueLoader;
	}

	public static <T> Messager<T> fastAttemptInLock(String key, Supplier<Messager<T>> task) {
		return attemptInLock(key, 1, 30_000L, task, X.castType(defaultValueLoader));
	}

	public static <T> T fastAttemptInLock(String key, long expireTimeInMs, Supplier<T> task, @Nullable Supplier<T> defaultVal) {
		return attemptInLock(key, 1, expireTimeInMs, task, defaultVal);
	}

	public static <T> T fastAttemptInLock(String key, long expireTimeInMs, Supplier<T> task) {
		return attemptInLock(key, 1, expireTimeInMs, task, null);
	}

	/**
	 * 拥有指定独占期的快速失败分布式独占锁
	 */
	public static <T> T fastAttemptInExclusivePeriod(String key, long exclusivePeriodInMs, Supplier<T> task, @Nullable Supplier<T> defaultVal) {
		if (client.getBucket(key, StringCodec.INSTANCE).setIfAbsent("1", Duration.of(exclusivePeriodInMs, ChronoUnit.MILLIS))) {
			return task.get();
		}
		return defaultVal == null ? null : defaultVal.get();
	}

	/**
	 * 拥有指定独占期的快速失败分布式独占锁
	 */
	public static <T> T fastAttemptInExclusivePeriod(String key, long exclusivePeriodInMs, Supplier<T> task) {
		return fastAttemptInExclusivePeriod(key, exclusivePeriodInMs, task, null);
	}

	/**
	 * 获取或创建布隆过滤器
	 *
	 * @param filterName 过滤器名称
	 * @param expectedInsertions 预测插入数量 eg: 1000
	 * @param falseProbability 误判率 eg: 0.003
	 */
	public static <T> RBloomFilter<T> getBloomFilter(String filterName, long expectedInsertions, double falseProbability) {
		RBloomFilter<T> filter = client.getBloomFilter(filterName);
		filter.tryInit(expectedInsertions, falseProbability);
		return filter;
	}

	/**
	 * @param dataType 为 null 则表示所有数据类型
	 */
	public static Cursor<String> scan(RedisTemplate<String, ?> redisTemplate, @Nullable DataType dataType, String pattern, int maxCount) {
		final ScanOptions.ScanOptionsBuilder builder = ScanOptions.scanOptions().match(pattern).count(maxCount);
		if (dataType != null) {
			builder.type(dataType);
		}
		ScanOptions scanOptions = builder.build();
		return scan(redisTemplate, scanOptions);
	}

	public static Cursor<String> scan(RedisTemplate<String, ?> redisTemplate, ScanOptions scanOptions) {
		RedisSerializer<String> redisSerializer = X.castType(redisTemplate.getKeySerializer());
		return redisTemplate.executeWithStickyConnection(conn -> new ConvertingCursor<>(conn.scan(scanOptions), redisSerializer::deserialize));
	}

	/**
	 * 检测指定 Redis Key 是否存在过期时间
	 */
	public static boolean hasExpireTime(RedisTemplate<String, ?> redisTemplate, String redisKey) {
		// getExpire() 相当于 TTL 指令：如果指定的 key 不存在，返回 -2；如果指定的 key 不存在有效期（即永久不过期），则返回 -1
		final Long expireSecs = redisTemplate.getExpire(redisKey);
		return expireSecs != null && expireSecs > 0;
	}

	/**
	 * 如果指定的 Redis Key 存在且未设置过期时间，则进行初始化过期时间设置，否则什么都不做
	 */
	public static boolean tryInitExpire(RedisTemplate<String, ?> redisTemplate, String redisKey, final long timeout, final TimeUnit unit) {
		// getExpire() 相当于 TTL 指令：如果指定的 key 不存在，返回 -2；如果指定的 key 不存在有效期（即永久不过期），则返回 -1
		final Long expireSecs = redisTemplate.getExpire(redisKey);
		// 其实这里不可能为 null，只是为了以防 API 变动，导致向后兼容性问题
		if (expireSecs == null || expireSecs == -1L) {
			redisTemplate.expire(redisKey, timeout, unit);
			return true;
		}
		return false;
	}

	/**
	 * 如果指定的 Redis Key 存在且未设置过期时间，则进行初始化过期时间设置，否则什么都不做
	 */
	public static boolean tryInitExpire(RedisTemplate<String, ?> redisTemplate, String redisKey, java.time.Duration timeout) {
		return tryInitExpire(redisTemplate, redisKey, timeout.toMillis(), TimeUnit.MILLISECONDS);
	}

	/**
	 * 指示是否是 Redis 自增溢出异常
	 */
	public static boolean incrementOverflow(Throwable e) {
		if (e instanceof io.lettuce.core.RedisCommandExecutionException) {
			return "ERR increment or decrement would overflow".equals(e.getMessage());
		}
		return e.getCause() != null && incrementOverflow(e.getCause());
	}

	/**
	 * 同时获取多个 key 的数据
	 * 同时避免 IDEA NPE 提示
	 */
	@Nonnull
	public static <T> List<T> multiGet(RedisTemplate<String, T> redisTemplate, Collection<String> keys) {
		//noinspection ConstantConditions
		return redisTemplate.opsForValue().multiGet(keys);
	}

	/**
	 * 同时获取多个 key 的数据
	 * 同时避免 IDEA NPE 提示
	 */
	@Nonnull
	public static <T> List<T> multiGet(RedisTemplate<String, T> redisTemplate, String... keys) {
		return multiGet(redisTemplate, Arrays.asList(keys));
	}

	/**
	 * 基于指定前缀，对 ID对 创建唯一的字符串
	 */
	public static String uniquePairKey(@Nullable String prefix, long fromId, long toId) {
		if (prefix == null) {
			return pair(fromId, toId);
		}
		return fromId < toId ? prefix + fromId + "_" + toId : prefix + toId + "_" + fromId;
	}

	/**
	 * 对 ID对 创建唯一的字符串
	 */
	public static String pair(long fromId, long toId) {
		return fromId < toId ? fromId + "_" + toId : toId + "_" + fromId;
	}

	/**
	 * 当使用 StringRedisTemplate 读取使用 RedisTemplate 保存的字符串时，需要再次反序列化，才能拿到最终的字符串
	 */
	public static String deserializeCompat(String str) {
		if (str != null && str.startsWith("\"")) {
			return (String) JSON.parse(str, JSONReader.Feature.SupportAutoType);
		}
		return str;
	}

	/**
	 * 对 Redis ZSet score 返回值进行精度预处理
	 *
	 * @param scale 指定最多保留的小数位数
	 */
	@Nullable
	public static Double score(@Nullable Double val, int scale) {
		if (val != null) {
			BigDecimal d = new BigDecimal(val);
			if (d.scale() <= scale) {
				return val;
			}
			return d.setScale(scale, RoundingMode.HALF_UP).doubleValue();
		}
		return null;
	}

	/**
	 * 将 Redis ZSet score 返回值预处理为 高精确度 的值
	 *
	 * @param scale 指定最多保留的小数位数
	 */
	@Nullable
	public static BigDecimal scoreDecimal(@Nullable Double val, int scale) {
		if (val != null) {
			BigDecimal d = new BigDecimal(val);
			return d.scale() <= scale ? d : d.setScale(scale, RoundingMode.HALF_UP);
		}
		return null;
	}

	/**
	 * 对 Redis ZSet score 返回值进行精度预处理，如果为 null 则返回 0
	 *
	 * @param scale 指定最多保留的小数位数
	 */
	public static double scoreVal(@Nullable Double val, int scale) {
		return val != null ? score(val, scale) : 0D;
	}

	/**
	 * 取 Redis ZSet 指定成员的 score
	 */
	@Nullable
	public static Double score(String redisKey, String member) {
		return stringRedisTemplate.opsForZSet().score(redisKey, member);
	}

	/**
	 * 取 Redis ZSet 多个成员的 score
	 */
	@Nonnull
	public static List<Double> score(String redisKey, String... members) {
		return stringRedisTemplate.opsForZSet().score(redisKey, (Object[]) members);
	}

	/**
	 * 取 Redis ZSet 指定成员的 score
	 * 如果成员不存在时，默认返回 0
	 */
	public static double score(String redisKey, String member, int scale) {
		return scoreVal(score(redisKey, member), scale);
	}

	/**
	 * 取 Redis ZSet 指定成员的 score
	 * 如果成员不存在时，默认返回 null
	 */
	@Nullable
	public static BigDecimal unboxScore(String redisKey, String member, int scale) {
		Double score = score(redisKey, member);
		if (score == null) {
			return null;
		}
		return BigDecimal.valueOf(score).movePointLeft(scale);
	}

	/**
	 * 在 Redis 事务中执行批处理操作（内部会自动开启、提交事务，抛异常时撤销事务）
	 */
	@Nullable
	public static List<Object> runInTransaction(final Consumer<RedisOperations<String, String>> redisOpsConsumer) {
		return stringRedisTemplate.execute(new SessionCallback<>() {
			@Override
			public <K, V> List<Object> execute(RedisOperations<K, V> operations) throws DataAccessException {
				RedisOperations<String, String> redisOps = X.castType(operations);
				redisOps.multi();
				redisOpsConsumer.accept(redisOps);
				return redisOps.exec();
			}
		});
	}

	/**
	 * 当 Redis key 存在时才设置指定值，并保持过期时间不变
	 */
	public static boolean setIfPresentKeepTtl(RedisTemplate<String, ?> redisTemplate, String redisKey, String value) {
		// SET key value XX KEEPTTL
		//noinspection ConstantConditions
		return redisTemplate.execute(conn -> Converters.stringToBoolean((String) conn.execute("SET", redisKey.getBytes(StandardCharsets.UTF_8), value.getBytes(StandardCharsets.UTF_8), "XX".getBytes(StandardCharsets.UTF_8), "KEEPTTL".getBytes(StandardCharsets.UTF_8))), true);
	}

}
