package com.candlk.context.web;

import javax.servlet.ServletContext;
import javax.servlet.SessionCookieConfig;
import javax.servlet.http.*;

import com.candlk.common.context.Env;
import org.apache.commons.lang3.ArrayUtils;

public abstract class SessionCookieUtil {

	public static final String SESSION_COOKIE_NAME = "ssid";
	public static final String CLIENT_ID_COOKIE_NAME = "cid";

	/**
	 * 利用 Servlet 3.0 新增的API，动态进行 Cookie 设置
	 */
	public static void configureSessionCookie(ServletContext servletContext) {
		SessionCookieConfig cfg = servletContext.getSessionCookieConfig();
		cfg.setName(SESSION_COOKIE_NAME);
		cfg.setHttpOnly(true);
		if (Env.inProduction()) {
			cfg.setSecure(true);
		}
	}

	static final String[] cookieWhiteList = { SESSION_COOKIE_NAME, CLIENT_ID_COOKIE_NAME };

	/**
	 * 清除所有的 Cookie（只清除当前URL所在目录下设置的Cookie）
	 */
	public static void clearCookies(HttpServletRequest request, HttpServletResponse response, String path) {
		final Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (ArrayUtils.contains(cookieWhiteList, cookie.getName())) {
					continue;
				}
				cookie.setMaxAge(0);
				if (path != null) {
					cookie.setPath(path);
				}
				response.addCookie(cookie);
			}
		}
	}

	/**
	 * 彻底清除所有的 Cookie （清除所有的URL） ( {@code path = "/" })
	 */
	public static void clearCookies(HttpServletRequest request, HttpServletResponse response) {
		clearCookies(request, response, "/");
	}

}
