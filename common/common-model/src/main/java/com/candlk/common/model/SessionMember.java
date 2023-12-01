package com.candlk.common.model;

/**
 * 会话成员
 */
public interface SessionMember<K extends Number> extends Bean<K> {

	boolean isAdmin();

	String getPassword();

}
