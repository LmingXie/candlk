package com.bojiu.context.web;

/**
 * 商户域名映射异常
 */
public class DomainException extends RuntimeException {

	public DomainException(String message) {
		super(message, null, false, false);
	}

	public DomainException(String message, Throwable cause) {
		super(message, cause);
	}

}
