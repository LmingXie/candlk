package com.candlk.webapp.base.util;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import com.candlk.common.context.I18N;
import com.candlk.common.model.ErrorMessageException;
import com.candlk.common.redis.RedisKeyLifetimeHelper;
import com.candlk.common.redis.RedisUtil;
import com.candlk.context.model.BaseI18nKey;
import com.candlk.context.model.MessagerStatus;
import lombok.Getter;
import me.codeplayer.util.EasyDate;
import me.codeplayer.util.NumberUtil;
import org.springframework.data.redis.core.HashOperations;

/**
 * 频率限制工具类
 */
public class RedisRateLimiter {

	/**
	 * 断言请求频率正常，否则抛出异常
	 *
	 * @param exclusivePeriodInMs 单次请求的独占时间
	 */
	public static void assertFreqValid(String redisKey, long exclusivePeriodInMs) {
		if (!Boolean.TRUE.equals(RedisUtil.getStringRedisTemplate().opsForValue().setIfAbsent(redisKey, "1", exclusivePeriodInMs, TimeUnit.MILLISECONDS))) {
			throw new ErrorMessageException(I18N.msg(BaseI18nKey.REQUEST_TOO_FAST), MessagerStatus.BUSY).report();
		}
	}

	/**
	 * 断言请求频率正常（默认 3s），否则抛出异常
	 */
	public static void assertFreqValid(String redisKey) {
		assertFreqValid(redisKey, 3000);
	}

	/**
	 * 对指定业务操作按天进行限流的限流器
	 */
	@Getter
	public static class DailyRateLimiter {

		final String redisKeyPrefix;
		final RedisKeyLifetimeHelper helper = new RedisKeyLifetimeHelper();

		public DailyRateLimiter(String redisKeyPrefix) {
			this.redisKeyPrefix = redisKeyPrefix;
		}

		/**
		 * 返回当天当前的请求次数
		 *
		 * @param now 当前时间
		 * @param field 限制频率的具体标识（一般是 业务标识前缀 + 用户ID ）
		 * @param queryOrIncr 只 查询 还是先 自增+1
		 */
		public long times(EasyDate now, String field, boolean queryOrIncr) {
			final String redisKey = redisKeyPrefix + now.getDay();
			final HashOperations<String, String, String> redisHash = RedisUtil.opsForHash();
			if (queryOrIncr) {
				final String val = redisHash.get(redisKey, field);
				return NumberUtil.getLong(val, 0);
			}
			final Long times = redisHash.increment(redisKey, field, 1L);
			helper.handleAfterOps(RedisUtil.getStringRedisTemplate(), redisKey, now.getTime(), now);
			return times;
		}

		/**
		 * 限制每日请求次数
		 */
		public <T> T safeExec(String field, int max, @Nullable Predicate<? super T> successTester, Supplier<T> task, @Nullable Supplier<T> fallback) {
			final EasyDate now = new EasyDate();
			final String redisKey = redisKeyPrefix + now.getDay();
			final HashOperations<String, String, String> redisHash = RedisUtil.opsForHash();
			long times = redisHash.increment(redisKey, field, 1L);
			helper.handleAfterOps(RedisUtil.getStringRedisTemplate(), redisKey, now.getTime(), now);
			if (times <= max) {
				boolean rollback = false;
				try {
					final T val = task.get();
					rollback = successTester != null && !successTester.test(val);
					return val;
				} catch (RuntimeException e) {
					rollback = true;
					throw e;
				} finally {
					if (rollback) {
						redisHash.increment(redisKey, field, -1L);
					}
				}
			} else {
				// 这里不能 100% 保证回滚，但是极个别的差错在我们的业务中可以容忍
				redisHash.increment(redisKey, field, -1L);
				if (fallback == null) {
					throw new IllegalStateException("您的请求次数超过限制，请明天再来！");
				}
				return fallback.get();
			}
		}

		/**
		 * 限制每日请求次数
		 */
		public <T> T safeExec(String field, int max, @Nullable Predicate<? super T> successTester, Supplier<T> task) {
			return safeExec(field, max, successTester, task, null);
		}

		/**
		 * 限制请求频率，以及每日请求次数
		 */
		public <T> T fullSafeExec(String field, int max, @Nullable Predicate<? super T> successTester, Supplier<T> task, @Nullable Supplier<T> fallback) {
			assertFreqValid("lock:freq:" + field);
			return safeExec(field, max, successTester, task, fallback);
		}

		/**
		 * 限制请求频率，以及每日请求次数
		 */
		public <T> T fullSafeExec(String field, int max, @Nullable Predicate<? super T> successTester, Supplier<T> task) {
			return fullSafeExec(field, max, successTester, task, null);
		}

	}

}
