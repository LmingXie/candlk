package com.candlk.common.web;

import javax.annotation.*;
import javax.servlet.http.*;

import me.codeplayer.util.*;

public interface RequestClient {

	@Nonnull
	String getValue();

	String getLabel();

	@Nonnull
	Client mapInternal();

	default boolean isWAP() {
		return Client.getResolver().isWAP(this);
	}

	/** 指示是否是APP端（Android、iOS） */
	default boolean isAPP() {
		return Client.getResolver().isAPP(this);
	}

	/** 指示客户端是否处于浏览器网页环境中 */
	default boolean inBrowser() {
		return Client.getResolver().inBrowser(this);
	}

	/** 指示是否是移动端（WAP（含微信）、Android、iOS） */
	default boolean isMobile() {
		return Client.getResolver().isMobile(this);
	}

	/**
	 * 指示当前请求是否来自微信内置浏览器
	 */
	static boolean fromWechat(final HttpServletRequest request) {
		return fromWechat(request.getHeader("User-Agent"));
	}

	/**
	 * 指示当前用户代理是否来自微信内置浏览器
	 */
	static boolean fromWechat(final String userAgent) {
		return StringUtil.notEmpty(userAgent) && userAgent.contains("MicroMessenger");
	}

}
