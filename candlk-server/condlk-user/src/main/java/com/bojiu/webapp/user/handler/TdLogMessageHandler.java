package com.bojiu.webapp.user.handler;

import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.Client;

/**
 * TDLib 不允许在 Log 回调线程中直接抛异常，否则会导致 native 层死锁/崩溃！！
 */
@Slf4j
public class TdLogMessageHandler implements Client.LogMessageHandler {

	@Override
	public void onLogMessage(int verbosityLevel, String message) {
		if (verbosityLevel == 0) {
			// TDLib 不允许在 Log 回调线程中直接抛异常，否则会导致 native 层死锁/崩溃
			onFatalError(message); // 致命错误
			return;
		}
		log.error("【TDLib】[" + verbosityLevel + "]：" + message);
	}

	private void onFatalError(String errorMessage) {
		if (isDatabaseBrokenError(errorMessage)) {
			log.error("【TDLib】数据库已损坏，请检查数据库文件：" + errorMessage);
		} else if (isDiskFullError(errorMessage)) {
			log.error("【TDLib】磁盘已满，请清理磁盘空间：" + errorMessage);
		} else if (isDiskError(errorMessage)) {
			log.error("【TDLib】磁盘错误：" + errorMessage);
		} else {
			log.error("【TDLib】TDLib fatal error：" + errorMessage);
		}

	}

	private boolean isDatabaseBrokenError(String message) {
		return message.contains("Wrong key or database is corrupted") ||
				message.contains("SQL logic error or missing database") ||
				message.contains("database disk image is malformed") ||
				message.contains("file is encrypted or is not a database") ||
				message.contains("unsupported file format") ||
				message.contains("Database was corrupted and deleted during execution and can't be recreated");
	}

	private boolean isDiskFullError(String message) {
		return message.contains("PosixError : No space left on device") ||
				message.contains("database or disk is full");
	}

	private boolean isDiskError(String message) {
		return message.contains("I/O error") || message.contains("Structure needs cleaning");
	}

}
