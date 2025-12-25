package com.bojiu.context.web;

import javax.servlet.ServletContext;
import javax.servlet.SessionCookieConfig;
import javax.servlet.http.*;

import com.bojiu.common.context.Env;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.Nullable;

public abstract class SessionCookieUtil {

	public static final String SESSION_COOKIE_NAME = "ssid";
	public static final String CLIENT_ID_COOKIE_NAME = "cid";

	public static String cookieRootPath = "/";

	/**
	 * 利用 Servlet 3.0 新增的API，动态进行 Cookie 设置
	 */
	public static void configureSessionCookie(ServletContext servletContext) {
		SessionCookieConfig cfg = servletContext.getSessionCookieConfig();
		cfg.setName(SESSION_COOKIE_NAME);
		cfg.setHttpOnly(true);
		if (Env.outer()) {
			cfg.setSecure(true);
		}
	}

	/**
	 * 清除所有的 Cookie（只清除当前URL所在目录下设置的Cookie）
	 */
	public static void clearCookies(HttpServletRequest request, HttpServletResponse response, @Nullable String path, @Nullable String[] cookieWhiteList) {
		final Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			if (cookieWhiteList == null) {
				cookieWhiteList = new String[] { EnhanceCookieSerializer.cookieName(request), CLIENT_ID_COOKIE_NAME, "UID", SESSION_COOKIE_NAME };
			}
			for (Cookie cookie : cookies) {
				if (ArrayUtils.contains(cookieWhiteList, cookie.getName())) {
					continue;
				}
				/*
				根据 RFC 6265 spec 所述:
				Finally, to remove a cookie, the server returns a Set-Cookie header with an expiration date in the past.
				The server will be successful in removing the cookie only if the Path and the Domain attribute in the Set-Cookie header match the values used when the cookie was created.

				Cookie 的 Path 和 Domain 必须和 设置时完全一致才能删除
				*/
				cookie.setValue("");
				cookie.setMaxAge(0);
				if (path != null) {
					cookie.setPath(path);
				}
				response.addCookie(cookie);
			}
		}
	}

	/**
	 * 清除所有的 Cookie（只清除当前URL所在目录下设置的Cookie）
	 */
	public static void clearCookies(HttpServletRequest request, HttpServletResponse response, @Nullable String path) {
		clearCookies(request, response, path, null);
	}

	/**
	 * 清除所有的 Cookie（只清除默认根目录下设置的Cookie）
	 */
	public static void clearCookies(HttpServletRequest request, HttpServletResponse response) {
		clearCookies(request, response, cookieRootPath, null);
	}

}