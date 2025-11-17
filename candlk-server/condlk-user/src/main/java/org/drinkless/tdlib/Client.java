//
// Copyright Aliaksei Levin (levlam@telegram.org), Arseny Smirnov (arseny30@gmail.com) 2014-2025
//
// Distributed under the Boost Software License, Version 1.0. (See accompanying
// file LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)
//
package org.drinkless.tdlib;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Getter;

/** 与TDLib交互的主类。 */
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

			System.out.println("[TDLib] DLL 加载成功: " + tdDir.getAbsolutePath());
		} catch (Throwable e) {
			System.err.println("[TDLib] 加载失败: " + e.getMessage());
			e.printStackTrace();
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
			String path = Client.class
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
	 * 调用ResultHandler时抛出的异常处理程序的接口。
	 * 默认情况下，所有这些异常都会被忽略。
	 * 所有从ExceptionHandler抛出的异常都会被忽略。
	 */
	public interface ExceptionHandler {

		/**
		 * 在调用ResultHandler时抛出异常时调用的回调。
		 *
		 * @param e 由ResultHandler抛出的异常。
		 */
		void onException(Throwable e);

	}

	/** 用于处理添加到内部TDLib日志的消息的接口。 */
	public interface LogMessageHandler {

		/**
		 * 对添加到内部TDLib日志的消息调用回调。
		 *
		 * @param verbosityLevel 添加消息的日志冗长级别，从-1到1024。
		 * 如果为0，则TDLib将在回调返回时立即崩溃。
		 * 任何TDLib方法都不能从回调中调用。
		 * @param message 添加到内部TDLib日志中的消息。
		 */
		void onLogMessage(int verbosityLevel, String message);

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
	 * @param exceptionHandler Exception handler with onException method which will be called on
	 * exception thrown from resultHandler. If it is null, then
	 * defaultExceptionHandler will be called.
	 */
	public void send(TdApi.Function query, ResultHandler resultHandler, ExceptionHandler exceptionHandler) {
		long queryId = currentQueryId.incrementAndGet();
		if (resultHandler != null) {
			handlers.put(queryId, new Handler(resultHandler, exceptionHandler));
		}
		nativeClientSend(nativeClientId, queryId, query);
	}

	public <T extends TdApi.Object> T sendSync(TdApi.Function query, long timeoutMillis) {
		return sendSync(query, timeoutMillis, TimeUnit.MILLISECONDS);
	}

	public <T extends TdApi.Object> T sendSync(TdApi.Function query, long timeout, TimeUnit unit) {
		CompletableFuture<TdApi.Object> future = new CompletableFuture<>();

		this.send(query, future::complete, future::completeExceptionally);

		try {
			@SuppressWarnings("unchecked")
			T result = (T) future.get(timeout, unit);
			return result;
		} catch (TimeoutException e) {
			throw new RuntimeException("TDLib request timeout", e);
		} catch (Exception e) {
			throw new RuntimeException("TDLib request failed", e);
		}
	}

	/**
	 * 使用空的ExceptionHandler向TDLib发送请求。
	 *
	 * @param query 对象，表示对TDLib的查询。
	 * @param resultHandler 带有onResult方法的结果处理程序，该方法将在查询结果或TdApi时调用。错误作为参数。如果它为空，则将调用defaultExceptionHandler。
	 */
	public void send(TdApi.Function query, ResultHandler resultHandler) {
		send(query, resultHandler, null);
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
	public static Client create(ResultHandler updateHandler, ExceptionHandler updateExceptionHandler, ExceptionHandler defaultExceptionHandler) {
		Client client = new Client(updateHandler, updateExceptionHandler, defaultExceptionHandler);
		synchronized (responseReceiver) {
			if (!responseReceiver.isRun) {
				responseReceiver.isRun = true;

				Thread receiverThread = new Thread(responseReceiver, "TDLib thread");
				receiverThread.setDaemon(true);
				receiverThread.start();
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

		public boolean isRun = false;

		/** 最大事件ID TODO 根据账号数量进行调整 */
		private static final int MAX_EVENTS = 1000;
		private final int[] clientIds = new int[MAX_EVENTS];
		private final long[] eventIds = new long[MAX_EVENTS];
		private final TdApi.Object[] events = new TdApi.Object[MAX_EVENTS];

		@Override
		public void run() {
			while (true) {
				int resultN = nativeClientReceive(clientIds, eventIds, events, 100000.0 /*seconds*/);
				for (int i = 0; i < resultN; i++) {
					processResult(clientIds[i], eventIds[i], events[i]);
					events[i] = null;
				}
			}
		}

		private void processResult(int clientId, long id, TdApi.Object object) {
			boolean isClosed = id == 0 && object instanceof TdApi.UpdateAuthorizationState updateAuth
					&& updateAuth.authorizationState instanceof TdApi.AuthorizationStateClosed;

			Handler handler = id == 0 ? updateHandlers.get(clientId) : handlers.remove(id);
			if (handler != null) {
				try {
					handler.resultHandler.onResult(object);
				} catch (Throwable cause) {
					ExceptionHandler exceptionHandler = handler.exceptionHandler;
					if (exceptionHandler == null) {
						exceptionHandler = defaultExceptionHandlers.get(clientId);
					}
					if (exceptionHandler != null) {
						try {
							exceptionHandler.onException(cause);
						} catch (Throwable ignored) {
						}
					}
				}
			}

			if (isClosed) {
				updateHandlers.remove(clientId);           // 不会有更多的更新
				defaultExceptionHandlers.remove(clientId); // 忽略其他异常
			}
		}

	}

	@Getter
	private final int nativeClientId;

	private static final ConcurrentHashMap<Integer, ExceptionHandler> defaultExceptionHandlers = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Integer, Handler> updateHandlers = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Long, Handler> handlers = new ConcurrentHashMap<>();
	private static final AtomicLong currentQueryId = new AtomicLong();

	private static final ResponseReceiver responseReceiver = new ResponseReceiver();

	private record Handler(ResultHandler resultHandler, ExceptionHandler exceptionHandler) {

	}

	private Client(ResultHandler updateHandler, ExceptionHandler updateExceptionHandler, ExceptionHandler defaultExceptionHandler) {
		nativeClientId = createNativeClient();
		if (updateHandler != null) {
			updateHandlers.put(nativeClientId, new Handler(updateHandler, updateExceptionHandler));
		}
		if (defaultExceptionHandler != null) {
			defaultExceptionHandlers.put(nativeClientId, defaultExceptionHandler);
		}
		send(new TdApi.GetOption("version"), null, null);
	}

	private static native int createNativeClient();

	private static native void nativeClientSend(int nativeClientId, long eventId, TdApi.Function function);

	private static native int nativeClientReceive(int[] clientIds, long[] eventIds, TdApi.Object[] events, double timeout);

	private static native TdApi.Object nativeClientExecute(TdApi.Function function);

	private static native void nativeClientSetLogMessageHandler(int maxVerbosityLevel, LogMessageHandler logMessageHandler);

}
