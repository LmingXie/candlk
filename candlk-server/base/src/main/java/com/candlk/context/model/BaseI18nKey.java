package com.candlk.context.model;

public interface BaseI18nKey {

	/** 您的系统时间有误，请先校正系统时间！ */
	String SECURITY_CLIENT_TIME_INVALID = "@security.client.time.invalid";
	/** sign不能为空！ */
	String SECURITY_SIGN_REQUIRED = "@security.sign.required";
	/** sign错误！ */
	String SECURITY_SIGN_ERROR = "@security.sign.error";
	/** 非法请求 */
	String ILLEGAL_REQUEST = "@illegal.request";
	/** 非法请求，请刷新重试！ */
	String ILLEGAL_REQUEST_REFRESH_RETRY = "@illegal.request.refresh.retry";
	/** 你没有权限进行此操作! */
	String PERMISSION_DENIED = "@permission.denied";
	/** 你无法进行此操作! */
	String UNSUPPORTED_OPERATIONS = "unsupported.operations";
	/** 网络异常，请稍后重试 */
	String NETWORK_ABORT = "network.abort";
	/** 您的操作过快，请稍后再试！ */
	String REQUEST_TOO_FAST = "request.too.fast";

}
