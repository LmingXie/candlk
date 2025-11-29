//
// Copyright Aliaksei Levin (levlam@telegram.org), Arseny Smirnov (arseny30@gmail.com) 2014-2025
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
package org.drinkless.tdlib;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import com.bojiu.common.util.SpringUtil;
import com.bojiu.context.web.TaskUtils;
import com.bojiu.webapp.user.handler.DefaultUpdateHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/** 与TDLib交互的主类。 */
@Slf4j
public final class Client {

	static {
		try {
			// 1️⃣ 获取当前运行目录（无论是IDE运行还是jar运行都能正确识别）
			String baseDir = getAppRootDir();

			// 2️⃣ 构造 td 库目录路径
			Path tdlibPath = Paths.get(baseDir, "lib", "td");
			File tdDir = tdlibPath.toFile();

			if (!tdDir.exists() || !tdDir.isDirectory()) {
				throw new IllegalStateException("TDLib 目录不存在: " + tdDir.getAbsolutePath());
			}

			// 3️⃣ 按顺序加载依赖库（顺序非常重要）
			System.load(new File(tdDir, "libcrypto-3-x64.dll").getAbsolutePath());
			System.load(new File(tdDir, "libssl-3-x64.dll").getAbsolutePath());
			System.load(new File(tdDir, "zlib1.dll").getAbsolutePath());
			System.load(new File(tdDir, "tdjni.dll").getAbsolutePath());

			log.info("[TDLib] DLL 加载成功: {}", tdDir.getAbsolutePath());
		} catch (Throwable e) {
			log.error("[TDLib] 加载失败: ", e);
			System.exit(1);
		}
	}

	/**
	 * 获取项目运行时根目录路径：
	 * - IDE 中运行时返回项目根目录
	 * - jar 运行时返回 jar 所在目录
	 */
	public static String getAppRootDir() {
		try {
			// 获取当前 jar 或 class 文件的绝对路径
			final String path = Client.class
					.getProtectionDomain()
					.getCodeSource()
					.getLocation()
					.toURI()
					.getPath();

			File file = new File(path);
			File dir = file.isFile() ? file.getParentFile() : file.getParentFile().getParentFile(); // 若为 jar，则取上级目录
			return dir.getAbsolutePath();
		} catch (Exception e) {
			// 兜底方案：返回当前工作目录
			return System.getProperty("user.dir");
		}
	}

	/**
	 * 用于处理对TDLib的查询结果和从TDLib传入的更新的处理程序的接口。
	 */
	public interface ResultHandler {

		/**
		 * 对TDLib的查询结果或从TDLib传入的更新调用回调。
		 *
		 * @param object 查询或更新TdApi类型的结果。更新新事件。
		 */
		void onResult(TdApi.Object object);

	}

	/**
	 * 执行 {@link #execute(TdApi.Function)} 时发生TDLib错误时抛出的异常类
	 */
	public static class ExecutionException extends Exception {

		/**
		 * 执行其中一个同步函数时发生原始TDLib错误。
		 */
		public final TdApi.Error error;

		/**
		 * @param error TDLib 执行时发生错误 {@link #execute(TdApi.Function)}.
		 */
		ExecutionException(TdApi.Error error) {
			super(error.code + ": " + error.message);
			this.error = error;
		}

	}

	/**
	 * Sends a request to the TDLib.
	 *
	 * @param query Object representing a query to the TDLib.
	 * @param resultHandler Result handler with onResult method which will be called with result
	 * of the query or with TdApi.Error as parameter. If it is null, nothing
	 * will be called.
	 */
	public void send(TdApi.Function query, ResultHandler resultHandler) {
		long queryId = currentQueryId.incrementAndGet();
		if (resultHandler != null) {
			handlers.put(queryId, resultHandler);
		}
		nativeClientSend(nativeClientId, queryId, query);
	}

	public <T extends TdApi.Object> T sendSync(TdApi.Function query, long timeoutMillis) {
		return sendSync(query, timeoutMillis, TimeUnit.MILLISECONDS);
	}

	@SuppressWarnings("unchecked")
	public <T extends TdApi.Object> T sendSync(TdApi.Function query, long timeout, TimeUnit unit) {
		CompletableFuture<TdApi.Object> future = new CompletableFuture<>();

		this.send(query, future::complete);

		try {
			return (T) future.get(timeout, unit);
		} catch (TimeoutException e) {
			throw new RuntimeException("TDLib request timeout", e);
		} catch (Exception e) {
			throw new RuntimeException("TDLib request failed", e);
		}
	}

	/** 同步执行TDLib请求。只有少数相应标记的请求可以同步执行。 */
	@SuppressWarnings("unchecked")
	public static <T extends TdApi.Object> T execute(TdApi.Function<T> query) throws ExecutionException {
		TdApi.Object object = nativeClientExecute(query);
		if (object instanceof TdApi.Error) {
			throw new ExecutionException((TdApi.Error) object);
		}
		return (T) object;
	}

	/** 创建一个新的客户端 */
	public static Client create(ResultHandler updateHandler) {
		final Client client;
		if (updateHandler instanceof DefaultUpdateHandler handler) {
			client = new Client(handler);
			handler.setClient(client); // 传递给内部使用
		} else {
			client = new Client(updateHandler);
		}
		synchronized (responseReceiver) {
			if (!responseReceiver.isRun) {
				responseReceiver.isRun = true;

				Thread receiverThread = new Thread(responseReceiver, "TDClient thread");
				receiverThread.setDaemon(true);
				receiverThread.start();
				log.info("[TDLib] 启动成功: {}", receiverThread.getName());
			}
		}
		return client;
	}

	/**
	 * 设置 TDLib 内部的日志处理器
	 * 任何 TDLib 方法都不能从回调中调用。
	 *
	 * @param maxVerbosityLevel 日志级别（ 0=none, 1=error, 2=warn, 3=info, 4+=debug）
	 * @param logMessageHandler 日志处理器（用于在Java端接收TDLib内部的日志）
	 */
	public static void setLogMessageHandler(int maxVerbosityLevel, LogMessageHandler logMessageHandler) {
		nativeClientSetLogMessageHandler(maxVerbosityLevel, logMessageHandler);
	}

	private static class ResponseReceiver implements Runnable {

		private boolean isRun = false;
		/** 最大事件ID */
		private static final int MAX_EVENTS = 1000;
		private final int[] clientIds = new int[MAX_EVENTS];
		/**
		 * 请求ID，通过 {@link Client#nativeClientSend}发送请求时传递，用来匹配返回结果对应的处理器。
		 */
		private final long[] eventIds = new long[MAX_EVENTS];
		private final TdApi.Object[] events = new TdApi.Object[MAX_EVENTS];
		/** 阻塞超时时间（单位：秒） */
		private static final double TIMEOUT = 10 * 60.0;

		/**
		 * <p>1、Pull 模式，每次最多100条，当没有消息时 nativeClientReceive 会阻塞，线程进入休眠。
		 * <p>2、如果超时，会直接返回 0，表示“没有事件”。
		 * <p>3、nativeClientReceive 接收 clientIds, eventIds, events 的引用，在 TDLib 中会直接修改指针位置的数据，不会有额外的传输成本。
		 */
		@Override
		public void run() {
			while (isRun) {
				int resultN = nativeClientReceive(clientIds, eventIds, events, TIMEOUT);
				if (resultN != 0) {
					for (int i = 0; i < resultN; i++) {
						processResult(clientIds[i], eventIds[i], events[i]);
						events[i] = null;
					}
				}
			}
		}

		/** 线程数量不宜过多，避免 内存占用过大 以及 增加 资源IO 争用 */
		static final ThreadPoolExecutor tdTaskThreadPool = TaskUtils.newThreadPool(8, 20, 10240, "td-task-");

		private void processResult(int clientId, long eventId, TdApi.Object obj) {
			boolean isClosed = eventId == 0 && obj instanceof TdApi.UpdateAuthorizationState updateAuth
					&& updateAuth.authorizationState instanceof TdApi.AuthorizationStateClosed;

			ResultHandler handler = eventId == 0/*TDLib主动消息，而非Client请求消息*/ ? defaultUpdateHandlers.get(clientId) : handlers.remove(eventId);
			if (handler != null) {
				tdTaskThreadPool.execute(() -> {
					try {
						handler.onResult(obj);
					} catch (Throwable e) {
						// 关键业务，上报异常信息
						SpringUtil.logError(log, "处理结果异常：clientId=" + clientId + "，eventId=" + eventId, e);
					}
				});
			}

			if (isClosed) {
				defaultUpdateHandlers.remove(clientId); // 不会有更多的更新
			}
		}

	}

	@Getter
	private final int nativeClientId;

	/** 默认更新处理器（处理TDLib接收到的来自User、Chat、Group、Supergroup等的更新信息） */
	private static final ConcurrentHashMap<Integer, ResultHandler> defaultUpdateHandlers = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Long, ResultHandler> handlers = new ConcurrentHashMap<>();
	private static final AtomicLong currentQueryId = new AtomicLong();

	private static final ResponseReceiver responseReceiver = new ResponseReceiver();

	private Client(ResultHandler defaultUpdateHandler) {
		nativeClientId = createNativeClient();
		if (defaultUpdateHandler != null) {
			defaultUpdateHandlers.put(nativeClientId, defaultUpdateHandler);
		}
		send(new TdApi.GetOption("version"), null);
	}

	private static native int createNativeClient();

	private static native void nativeClientSend(int nativeClientId, long eventId, TdApi.Function function);

	private static native int nativeClientReceive(int[] clientIds, long[] eventIds, TdApi.Object[] events, double timeout);

	private static native TdApi.Object nativeClientExecute(TdApi.Function function);

	private static native void nativeClientSetLogMessageHandler(int maxVerbosityLevel, LogMessageHandler logMessageHandler);

	/**
	 * 用于处理添加到内部TDLib日志的消息的接口。
	 * <h5>TDLib要求LogMessageHandler必须在Client中，否则将无法解析！</h5>
	 */
	public interface LogMessageHandler {

		/**
		 * TDLib日志回调
		 * <h5>注意：TDLib 此方法不可直接抛异常，否则会导致 native 层死锁/崩溃！！<h5/>
		 *
		 * @param verbosityLevel 添加消息的日志冗长级别，从-1到1024。
		 * 如果为0，则TDLib将在回调返回时立即崩溃。
		 * 任何TDLib方法都不能从回调中调用。
		 * @param message 添加到内部TDLib日志中的消息。
		 */
		void onLogMessage(int verbosityLevel, String message);

	}

}
