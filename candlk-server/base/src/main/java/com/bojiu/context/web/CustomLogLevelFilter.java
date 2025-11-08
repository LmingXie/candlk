package com.bojiu.context.web;

import java.io.IOException;
import java.security.GeneralSecurityException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import com.bojiu.common.context.Env;
import com.bojiu.common.security.AES;
import com.bojiu.common.util.Common;
import com.bojiu.common.util.SpringUtil;
import me.codeplayer.util.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;

/**
 * 自定义调整本次请求的 日志级别 的过滤器
 */
public class CustomLogLevelFilter implements Filter {

	/** 需要全局触发 debug 日志的相关信息：[ pathPrefix, logLevel ] */
	@Nullable
	private static DebugConfig globalConfig;

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		final String debugToken = req.getParameter("_debug");
		final String logLevel;
		if (debugToken != null) {
			logLevel = Env.inner() && "1".equals(debugToken) ? "debug" : safeParseLogLevel(debugToken, req.getRequestURI());
		} else {
			final DebugConfig config = globalConfig;
			logLevel = config == null ? null : getGlobalLogLevel(config, req);
		}
		if (logLevel == null) {
			chain.doFilter(request, response);
		} else {
			ThreadContext.put("logLevel", logLevel);
			try {
				chain.doFilter(request, response);
			} catch (RuntimeException | Error e) {
				ThreadContext.clearMap();
				throw e;
			}
		}
	}

	private static String getGlobalLogLevel(DebugConfig config, HttpServletRequest req) {
		if (config.expireTimeInMs > System.currentTimeMillis()) {
			if (req.getRequestURI().startsWith(config.pathPrefix)) {
				return config.logLevel;
			}
		} else {
			globalConfig = null;
		}
		return null;
	}

	@Nullable
	public static String safeParseLogLevel(@Nonnull String debug, @Nonnull String requestURI) {
		try {
			return decrypt(getAes(), debug, requestURI);
		} catch (Throwable ignored) {
			return null;
		}
	}

	@Nonnull
	private static AES getAes() {
		AES aes = EnhanceHttpServletRequestWrapper.aes;
		if (aes == null) {
			String secretKey = SpringUtil.getApplicationContext().getEnvironment().getProperty("webapp.context.appIdSecret");
			EnhanceHttpServletRequestWrapper.aes = aes = new AES(secretKey);
		}
		return aes;
	}

	@Nullable
	private static String doParseLogLevel(String debug, String requestURI) {
		return decrypt(getAes(), debug, requestURI);
	}

	public static String encrypt(AES aes, String pathPrefix, String logLevel, long expireTimeInMs) {
		// "*" 开头表示全局 debug 模式
		Assert.isTrue(pathPrefix.startsWith("/") || pathPrefix.startsWith("*/"));
		// "日志级别^过期时间（秒级时间戳）^路径前缀"
		String source = logLevel + "^" + Long.toString(expireTimeInMs / 1000L, 36) + "^" + pathPrefix;
		byte[] bytes = JavaUtil.getUtf8Bytes(source);
		byte[] result;
		try {
			result = aes.encrypt(bytes);// 加密
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException("对数据进行AES加密时出错：", e);
		}
		return Common.base64ToString(result, true);
	}

	public static String encrypt(AES aes, String pathPrefix, Level logLevel, long expireTimeInMs) {
		return encrypt(aes, pathPrefix, logLevel.name().toLowerCase(), expireTimeInMs);
	}

	public static String encrypt(String secretKey, String pathPrefix, Level logLevel, long expireTimeInMs) {
		return encrypt(new AES(secretKey), pathPrefix, logLevel, expireTimeInMs);
	}

	@Nullable
	public static String decrypt(AES aes, String encoded, String requestURI) {
		if (encoded.length() <= 20) { // 20个以下字符，不是有效的密文，直接返回 null
			return null;
		}
		// "日志级别^过期时间（秒级时间戳）^路径前缀"
		String source = aes.decrypt(encoded, true);
		final int pos1 = source.indexOf('^', 3);
		int pos2 = source.indexOf('^', pos1 + 6); // 时间戳 至少6位字符
		long expireTimeInMs = Long.parseUnsignedLong(source, pos1 + 1, pos2, 36) * 1000L;
		if (expireTimeInMs < System.currentTimeMillis()) {
			return null;
		}
		final boolean global = source.startsWith("*", ++pos2);
		if (global) {
			pos2++;
		}
		if (!requestURI.regionMatches(0, source, pos2, source.length() - pos2)) {
			return null;
		}
		String logLevel = source.substring(0, pos1);
		if (global) {
			final String pathPrefix = source.substring(pos2);
			SpringUtil.log.info("检测到全局 debug 模式设置：pathPrefix={}，level={}，expireTime={}", pathPrefix, logLevel, new EasyDate(expireTimeInMs).toDateTimeString());
			// 考虑到性能，目前只支持设置一个全局 debug 模式设置
			globalConfig = new DebugConfig(pathPrefix, logLevel, expireTimeInMs);
		}
		return logLevel;
	}

	record DebugConfig(String pathPrefix, String logLevel, long expireTimeInMs) {

	}

}