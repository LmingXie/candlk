package com.bojiu.context.model;

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
	/** 站点建设中 */
	String INIT = "init";
	/** 存在拉黑 */
	String BLOCKED = "blocked";
	/** 需要登录 */
	String LOGIN = "login";
	/** 站点维护中 */
	String MAINTAIN = "maintain";
	/** 站点关闭 */
	String CLOSED = "closed";
	/** 站点不正常 */
	String ABNORMAL = "abnormal";
	/** 商户白名单 */
	String MERCHANT_WHITELIST = "merchant_whitelist";
	/** 异步导出 */
	String ASYNC_EXPORT = "async_export";
	/** 商户结账 */
	String CLOSE_ACCOUNTS = "close accounts";
	/** 导入异常 */
	String IMPORT_FAIL = "importFail";

}