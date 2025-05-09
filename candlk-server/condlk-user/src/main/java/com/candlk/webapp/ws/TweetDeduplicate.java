package com.candlk.webapp.ws;

import java.util.Map;
import java.util.concurrent.*;

import lombok.extern.slf4j.Slf4j;

/** 推文去重器 */
@Slf4j
public class TweetDeduplicate {

	// 保存 tweetId 及其插入时间
	private static final ConcurrentHashMap<String, Long> seenTweetMap = new ConcurrentHashMap<>(500, 1F);

	// 去重记录保存的时间（毫秒），默认 30 s
	private static final long EXPIRATION_MILLIS = 30 * 1000;

	// 定时清理任务调度器
	private static final ScheduledExecutorService cleanerScheduler = Executors.newSingleThreadScheduledExecutor();

	// 启动定时清理任务
	public static void clear() {
		final long now = System.currentTimeMillis();
		for (Map.Entry<String, Long> entry : seenTweetMap.entrySet()) {
			if (now - entry.getValue() > EXPIRATION_MILLIS) {
				seenTweetMap.remove(entry.getKey());
			}
		}
		log.info("定时清理推文ID，当前剩余：{} 条缓存ID。", seenTweetMap.size());
	}

	/**
	 * 判断 tweetId 是否已存在，如果不存在则记录并返回 true
	 * 否则返回 false，表示重复
	 */
	public static boolean shouldInsert(String tweetId, long now) {
		return seenTweetMap.putIfAbsent(tweetId, now) == null;
	}

	/**
	 * 可选：手动移除 tweetId
	 */
	public static void remove(String tweetId) {
		seenTweetMap.remove(tweetId);
	}

	/**
	 * 可选：获取当前缓存大小
	 */
	public static int size() {
		return seenTweetMap.size();
	}

	/**
	 * 可选：关闭定时清理任务（应用关闭时调用）
	 */
	public static void shutdown() {
		cleanerScheduler.shutdown();
	}

}
