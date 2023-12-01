package com.candlk.common.security;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import javax.servlet.http.HttpServletRequest;

import com.candlk.common.context.Context;
import me.codeplayer.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * 安全处理工具类，用于进行安全拦截或过滤，以便于提高系统安全性
 *
 * @date 2015年3月2日
 */
public abstract class SecurityUtil {

	/** 判断指定的文本内容是否包含脚本代码 */
	public static boolean containsScript(String content) {
		return StringUtils.containsIgnoreCase(content, "script");
	}

	/**
	 * 转义文本内容中的所有HTML代码
	 */
	public static boolean inviteCodeSafeCheck(final String inviteCode) {
		if (inviteCode == null) {
			return true;
		}
		final int len = inviteCode.length();
		for (int i = 0; i < len; i++) {
			final char ch = inviteCode.charAt(i);
			if (!(ch >= '0' && ch <= '9') && !(ch >= 'a' && ch <= 'z') && !(ch >= 'A' && ch <= 'Z')) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 检测指定的字符串中是否存在可能导致XSS攻击的敏感字符序列，如果发现则报错，否则直接返回原始字符串
	 */
	public static String defendXSS(final String source) {
		if (StringUtil.notEmpty(source)) {
			boolean deny = false;
			final char[] chars = { '<', '>', '\'', '"' };
			for (char c : chars) {
				if (source.indexOf(c) != -1) {
					deny = true;
					break;
				}
			}
			if (!deny) {
				deny = source.contains("&#");
			}
			if (deny) {
				throw new IllegalArgumentException("Invalid character detected，Please input again！");
			}
		}
		return source;
	}

	/**
	 * 对传入的 src 参数进行预处理，防止跳转到外部链接
	 *
	 * @return 如果是站点内部链接，则返回 {@code src} ，否则 {@code null}
	 */
	public static String preprocessInternalRedirect(HttpServletRequest request, String src) {
		if (StringUtil.notEmpty(src)) {
			if (src.startsWith("/") || src.startsWith(Context.nav().getSiteURL())) {
				return src;
			}
			URL url;
			try {
				url = new URL(src);
			} catch (MalformedURLException e) {
				return null;
			}
			if (request.getScheme().equals(url.getProtocol())
					&& request.getServerName().equals(url.getHost())) {
				int port = url.getPort();
				if (port == -1 ? url.getDefaultPort() == request.getServerPort() : port == request.getServerPort()) {
					return src;
				}
			}
		}
		return null;
	}

	/** 解码 Base64 编码文本 */
	public static byte[] decodeBase64(String encoded) {
		return Base64.getDecoder().decode(encoded);
	}

	/** 解码 Base64 编码文本 */
	public static String decodeBase64String(String encoded) {
		return new String(Base64.getDecoder().decode(encoded));
	}

}
