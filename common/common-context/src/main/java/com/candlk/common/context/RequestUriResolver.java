package com.candlk.common.context;

import java.net.*;

import javax.annotation.*;
import javax.servlet.http.*;

import org.apache.commons.lang3.*;

import com.candlk.common.web.*;
import me.codeplayer.util.*;

public interface RequestUriResolver {

	default boolean isStaticResource(HttpServletRequest request) {
		final String uri = request.getRequestURI();
		final int offset = request.getContextPath().length();
		String[] roots = X.expectNotNull(Context.nav().getStaticResourcePaths(), ArrayUtils.EMPTY_STRING_ARRAY);
		for (String prefix : roots) {
			if (uri.startsWith(prefix, offset)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 检测指定请求是否是后台发起的请求
	 */
	default boolean fromBackstage(@Nullable HttpServletRequest request) {
		if (request == null || Context.startsWithAdminPath(request)) {
			return true;
		}
		if (!Context.nav().isSingleApp()) {
			InternalConfig internal = Context.internal();
			String header = request.getHeader(internal.getAppIdHeaderName());
			return header != null && header.contains(internal.getAdminAppIdKeyword());
		}
		return false;
	}

	default String getClientIP(HttpServletRequest request) {
		return ServletUtil.getClientIP(request);
	}

	default boolean isSafeReferer(HttpServletRequest request, boolean emptyAsSafe) {
		String referer = ServletUtil.getReferer(request);
		return StringUtil.notEmpty(referer) ? isOwnURL(referer) : emptyAsSafe;
	}

	default String getLocalIP() {
		try {
			return ServletUtil.getLocalIP();
		} catch (SocketException e) {
			throw new IllegalStateException(e);
		}
	}

	static String concatURI(String rootURL, String path) {
		if (StringUtil.endsWith(rootURL, '/') && StringUtil.startsWith(path, '/')) {
			//noinspection StringBufferReplaceableByString
			return new StringBuilder(StringUtil.length(rootURL) + StringUtil.length(path) - 1)
					.append(rootURL)
					.append(path, 1, path.length())
					.toString();
		}
		return StringUtil.concat(rootURL, path);
	}

	static URL resolveURL(String uri, boolean errorAsNull) {
		try {
			return new URL(uri);
		} catch (MalformedURLException e) {
			if (errorAsNull) {
				return null;
			}
			throw new IllegalArgumentException("Unable to resolve the uri: [" + uri + "]", e);
		}
	}

	@Nullable
	static String resolveDomain(String uri) {
		return X.map(resolveURL(uri, true), URL::getHost);
	}

	/**
	 * 是否属于站点自己的URL
	 */
	static boolean isOwnURL(final String uri, String... ownDomains) {
		if (ResourceUtil.parseUriLocator(uri) == 1) {
			String host = resolveDomain(uri);
			return inHosts(host, ownDomains);
		}
		return true;
	}

	/**
	 * 指示指定主机名是否包含在指定的主机数组中
	 */
	static boolean inHosts(String host, String[] ownHosts) {
		if (host != null) {
			for (String domain : ownHosts) {
				if (endsWith(host, domain, ".")) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 是否属于站点自己的URL
	 */
	static boolean isOwnURL(final String uri) {
		return true; // TODO 暂时不需要判断
	}

	static boolean endsWith(String str, String suffix, String acceptBeforeBoundaryChars) {
		if (str.endsWith(suffix)) {
			int beforePos = str.length() - suffix.length();
			return beforePos == 0 || acceptBeforeBoundaryChars.indexOf(str.charAt(beforePos - 1)) != -1;
		}
		return false;
	}

}
