package com.candlk.common.alarm.dingtalk;

import javax.annotation.Nullable;

import com.candlk.common.model.Messager;

/**
 * bug 告警服务
 *
 */
public interface BugSendService {

	/**
	 * 程序异常发送 bug 信息【短时间内自动去除重复异常】
	 */
	Messager<String> sendBugMsg(@Nullable String uri, Throwable e, boolean atAll);

	/**
	 * 程序异常发送 bug 信息【短时间内自动去除重复异常】
	 * 默认不会 @ 所有人
	 */
	default Messager<String> sendBugMsg(@Nullable String uri, Throwable e) {
		return sendBugMsg(uri, e, false);
	}

	/**
	 * 程序异常发送 bug 信息【短时间内自动去除重复异常】
	 * 默认不会 @ 所有人
	 */
	default Messager<String> sendBugMsg(Exception e) {
		return sendBugMsg(null, e);
	}

	/**
	 * 业务异常发送 bug 信息【短时间内自动去除重复异常】
	 */
	Messager<String> sendBugMsg(String bugMsg, boolean atAll);

	/**
	 * 业务异常发送 bug 信息【短时间内自动去除重复异常】
	 * 默认会 @ 所有人
	 */
	default Messager<String> sendBugMsg(String bugMsg) {
		return sendBugMsg(bugMsg, true);
	}

	/**
	 * 发送信息（没有阻塞条件）
	 */
	Messager<String> sendMsg(String msg, boolean atAll);

	/**
	 * 发送信息（没有阻塞条件）
	 * 默认会 @ 所有人
	 */
	default Messager<String> sendMsg(String msg) {
		return sendMsg(msg, true);
	}

	/**
	 * 发送 Markdown 信息（没有阻塞条件）
	 */
	Messager<String> sendMarkdownMsg(String title, String markdown, boolean atAll);

	/**
	 * 发送 Markdown 信息（没有阻塞条件）
	 * 默认会 @ 所有人
	 */
	default Messager<String> sendMarkdownMsg(String title, String markdown) {
		return sendMarkdownMsg(title, markdown, true);
	}

}
