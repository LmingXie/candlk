package com.candlk.webapp.base.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 本地定时调度器工具类
 */
@Slf4j
public abstract class LocalScheduler {

	public static volatile ThreadPoolTaskScheduler scheduler;

	public static ThreadPoolTaskScheduler getScheduler() {
		if (scheduler == null) {
			synchronized (LocalScheduler.class) {
				if (scheduler == null) {
					scheduler = new ThreadPoolTaskScheduler();
					scheduler.setPoolSize(Math.max(Runtime.getRuntime().availableProcessors() * 2, 4));
					scheduler.setErrorHandler(e -> log.error("执行定时任务时出错", e));
					scheduler.initialize();
				}
			}
		}
		return scheduler;
	}

}
