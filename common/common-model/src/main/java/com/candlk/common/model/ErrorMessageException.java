package com.candlk.common.model;

/**
 * 错误信息异常类，专门用于抛出业务逻辑层面的错误信息内容
 *

 * @date 2014年12月26日
 */
public class ErrorMessageException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	protected Messager<?> msg;
	protected boolean report;

	public ErrorMessageException(String message) {
		super(message);
		msg = new Messager<>(message);
	}

	public ErrorMessageException(String message, Object ext) {
		super(message);
		msg = new Messager<>(message).setExt(ext);
	}

	public ErrorMessageException(String message, String status) {
		super(message);
		msg = new Messager<>(message).setStatus(status);
	}

	public ErrorMessageException(String message, Throwable cause) {
		super(message, cause);
		msg = new Messager<>(message);
	}

	public ErrorMessageException(Messager<?> message) {
		super(message.getMsg());
		msg = message;
	}

	public ErrorMessageException(Messager<?> message, Throwable cause) {
		super(message.getMsg(), cause);
		msg = message;
	}

	public Messager<?> getMessager() {
		return msg;
	}

	public ErrorMessageException report(boolean report) {
		this.report = report;
		return this;
	}

	public ErrorMessageException report() {
		return report(true);
	}

	public boolean isReport() {
		return report;
	}

}
