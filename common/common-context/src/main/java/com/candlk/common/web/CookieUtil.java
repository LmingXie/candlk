package com.candlk.common.web;

import javax.annotation.Nullable;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Cookie 操作工具类
 *

 * @date 2016年7月6日
 * @since 1.0
 */
public abstract class CookieUtil {

	/**
	 * 获取当前请求中指定名称的cookie
	 */
	@Nullable
	public static Cookie getCookie(HttpServletRequest request, String name) {
		final Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(name)) {
					return cookie;
				}
			}
		}
		return null;
	}

	/**
	 * 获取当前请求中指定名称的cookie
	 */
	@Nullable
	public static String getCookieValue(HttpServletRequest request, String name, @Nullable String encoding) {
		Cookie cookie = getCookie(request, name);
		if (cookie != null) {
			String value = cookie.getValue();
			if (encoding != null) {
				value = ServletUtil.decodeURL(value, encoding);
			}
			return value;
		}
		return null;
	}

	/**
	 * 获取当前请求中指定名称的 Cookie
	 */
	@Nullable
	public static String getCookieValue(HttpServletRequest request, String name) {
		return getCookieValue(request, name, "UTF-8");
	}

	/**
	 * 为当前请求的响应设置 Cookie
	 *
	 * @param expireInSecs 单位：秒。默认=浏览器默认值
	 * @param domain 默认=当前域名
	 * @param path 默认=当前URL所在目录
	 * @param encoding 默认=UTF-8
	 */
	public static Cookie setCookie(HttpServletResponse response, String name, @Nullable String value, @Nullable Integer expireInSecs,
	                               @Nullable String domain, @Nullable String path, @Nullable String encoding, @Nullable Boolean httpOnly) {
		if (value == null) {
			value = "";
		} else if (value.length() > 0) {
			value = ServletUtil.encodeURL(value, encoding == null ? "UTF-8" : encoding);
		}
		Cookie cookie = new Cookie(name, value);
		if (domain != null) {
			cookie.setDomain(domain);
		}
		if (path != null) {
			cookie.setPath(path);
		}
		if (expireInSecs != null) {
			cookie.setMaxAge(expireInSecs);
		}
		if (httpOnly != null) {
			cookie.setHttpOnly(httpOnly);
		}
		response.addCookie(cookie);
		return cookie;
	}

	/**
	 * 为当前请求的响应设置 Cookie ( {@code path="/"} )。
	 *
	 * @param expireInSecs 默认=浏览器默认值
	 * @param domain 默认=当前域名
	 */
	public static Cookie setCookie(HttpServletResponse response, String name, @Nullable String value, @Nullable Integer expireInSecs,
	                               @Nullable String domain, @Nullable Boolean httpOnly) {
		return setCookie(response, name, value, expireInSecs, domain, "/", null, httpOnly);
	}

	/**
	 * 移除指定的 Cookie
	 */
	public static void removeCookie(HttpServletResponse response, @Nullable Cookie cookie) {
		if (cookie != null) {
			cookie.setMaxAge(0);
			response.addCookie(cookie);
		}
	}

	/**
	 * 移除指定的 Cookie
	 *
	 * @param domain 默认=当前域名
	 * @param path 默认=当前URL所在目录
	 */
	public static void removeCookie(HttpServletResponse response, String name, @Nullable String domain, @Nullable String path) {
		setCookie(response, name, "", 0, domain, path, null, null);
	}

	/**
	 * 移除指定的 Cookie
	 *
	 * @param domain 默认=当前域名
	 */
	public static void removeCookie(HttpServletResponse response, String name, @Nullable String domain) {
		setCookie(response, name, "", 0, domain, "/", null, null);
	}

	/**
	 * 清除所有的 Cookie（只清除当前URL所在目录下设置的Cookie）
	 */
	public static void clearCookies(HttpServletRequest request, HttpServletResponse response, String path) {
		final Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
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
