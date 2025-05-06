package com.candlk.webapp.user.util;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.concurrent.StructuredTaskScope;

public class ConcurrentExecutor {

	static final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

	/**
	 * 并发执行任务，支持虚拟线程，遇异常则中止其他任务。
	 *
	 * @param items 待处理的数据列表
	 * @param maxParallel 最大并发数量（虚拟线程数量）
	 * @param task 任务处理逻辑
	 * @param <T> 数据类型
	 * @throws Exception 子任务异常
	 */
	public static <T> void runConcurrently(List<T> items, int maxParallel, Consumer<T> task) throws Exception {
		try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
			for (T item : items) {
				scope.fork(() -> {
					synchronized (ConcurrentExecutor.class) {
						while (Thread.activeCount() > maxParallel + 2) { // +2为main线程和scope控制线程
							ConcurrentExecutor.class.wait(10); // 控制并发
						}
					}
					task.accept(item);
					return null;
				});
			}

			scope.join(); // 等待全部任务完成或失败
			scope.throwIfFailed(); // 若有异常抛出
		}
	}

}
