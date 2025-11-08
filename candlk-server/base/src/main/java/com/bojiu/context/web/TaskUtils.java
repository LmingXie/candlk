package com.bojiu.context.web;

import java.util.concurrent.*;

import com.bojiu.common.redis.RedisUtil;
import com.bojiu.common.util.SpringUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class TaskUtils {

	/** 尽最大努力保存 Redis 断点 */
	public static void savePoint(final String redisHashKey, final String field, final String value, final int maxRetry) {
		int retry = 0;
		do {
			try {
				RedisUtil.opsForHash().put(redisHashKey, field, value);
				if (retry > 0) {
					log.info("保存任务断点重试成功：redisKey={}，field={}，value={}", redisHashKey, field, value);
				}
				break;
			} catch (RuntimeException | Error e) {
				if (retry++ < maxRetry) {
					throw e;
				}
				SpringUtil.logError(log, "保存任务断点时出错：redisKey=" + redisHashKey + "，field=" + field + "，value=" + value + "，正在重试=" + retry, e);
				if (retry >= 2) { // 第2+次重试前先休眠几秒，避免因 Redis 连接闪断导致断点无法保存
					try {
						//noinspection BusyWait
						Thread.sleep(Math.min(retry * 3000L, 10000));
					} catch (InterruptedException ignored) {
					}
				}
			}
		} while (true);
	}

	/** 尽最大努力保存 Redis 断点 */
	public static void savePoint(final String redisHashKey, final String field, final String value) {
		savePoint(redisHashKey, field, value, 3);
	}

	public static ThreadPoolExecutor newThreadPool(int corePoolSize, int maxPoolSize, int keepAliveSeconds, int queueCapacity, final String threadNamePrefix, RejectedExecutionHandler handler) {
		return new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveSeconds, TimeUnit.SECONDS,
				new LinkedBlockingQueue<>(queueCapacity),
				new com.google.common.util.concurrent.ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "%d").build(),
				handler
		);
	}

	public static ThreadPoolExecutor newThreadPool(int corePoolSize, int maxPoolSize, int queueCapacity, final String threadNamePrefix, RejectedExecutionHandler handler) {
		return newThreadPool(corePoolSize, maxPoolSize, 300, queueCapacity, threadNamePrefix, handler);
	}

	public static ThreadPoolExecutor newThreadPool(int corePoolSize, int maxPoolSize, int queueCapacity, final String threadNamePrefix) {
		return newThreadPool(corePoolSize, maxPoolSize, queueCapacity, threadNamePrefix, new ThreadPoolExecutor.CallerRunsPolicy());
	}

}