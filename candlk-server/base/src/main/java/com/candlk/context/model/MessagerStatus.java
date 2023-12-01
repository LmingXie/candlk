package com.candlk.context.model;

/***
 * Messager status
 */
public interface MessagerStatus {

	/*
	 系统层面
	 */
	/** 实名认证 */
	String NO_AUTH = "no_auth";

	/** 请求次数过快 */
	String BUSY = "busy";
	/** 客户端不受信 */
	String UNTRUSTED = "untrusted";
	/** 冻结 */
	String FROZEN = "frozen";
	/** 存在拉黑 */
	String BLOCKED = "blocked";
	/** 需要登录 */
	String LOGIN = "login";

}
