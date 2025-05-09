package com.candlk.webapp.user.util;

import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

import com.candlk.context.web.TaskUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConcurrentExecutor {

	private static final ExecutorService executor = TaskUtils.newThreadPool(4,8,2048,"ce-task");

	public static <T> void runConcurrently(List<T> list, Consumer<T> task) throws InterruptedException {
		final List<Future<?>> futures = new CopyOnWriteArrayList<>();

		for (T item : list) {
			futures.add(executor.submit(() -> task.accept(item)));
		}

		// 等待所有任务完成
		for (Future<?> future : futures) {
			try {
				future.get(); // 会抛出异常可选处理
			} catch (ExecutionException e) {
				log.error("任务执行出错", e);
			}
		}
	}

	// 如果你在应用结束后需要关闭线程池
	public static void shutdown() {
		executor.shutdown();
	}

}
