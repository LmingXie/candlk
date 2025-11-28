package com.bojiu.webapp.user.handler;

/** 用于处理添加到内部TDLib日志的消息的接口。 */
public interface LogMessageHandler {

	/**
	 * 注意：TDLib 此方法不可直接抛异常，否则会导致 native 层死锁/崩溃！！
	 *
	 * @param verbosityLevel 添加消息的日志冗长级别，从-1到1024。
	 * 如果为0，则TDLib将在回调返回时立即崩溃。
	 * 任何TDLib方法都不能从回调中调用。
	 * @param message 添加到内部TDLib日志中的消息。
	 */
	void onLogMessage(int verbosityLevel, String message);

}