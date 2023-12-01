package com.candlk.common.context;

import me.codeplayer.util.StringUtil;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

public interface SecurityHelper {

	default SecurityHelper getSecurityHelper() {
		return DefaultSecurityHelper.INSTANCE;
	}

	default String escapeHtml(String text) {
		return getSecurityHelper().escapeHtml(text);
	}

	default String escapeJs(String text) {
		return getSecurityHelper().escapeJs(text);
	}

	default boolean containsScript(String text) {
		return getSecurityHelper().containsScript(text);
	}

	default String defendXSS(final String text) {
		return getSecurityHelper().defendXSS(text);
	}

	class DefaultSecurityHelper implements SecurityHelper {

		public static DefaultSecurityHelper INSTANCE = new DefaultSecurityHelper();

		@Override
		public String escapeHtml(String text) {
			if (StringUtil.isEmpty(text)) {
				return text;
			}
			return StringEscapeUtils.escapeHtml4(text); // Encode.forHtml(text);
		}

		@Override
		public String escapeJs(String text) {
			if (StringUtil.isEmpty(text)) {
				return text;
			}
			return StringEscapeUtils.escapeEcmaScript(text); // Encode.forJavaScript(text);
		}

		@Override
		public boolean containsScript(String text) {
			return StringUtils.containsIgnoreCase(text, "script");
		}

		/**
		 * 检测指定的字符串中是否存在可能导致XSS攻击的敏感字符序列，如果发现则报错，否则直接返回原始字符串
		 */
		public String defendXSS(final String source) {
			if (StringUtil.notEmpty(source)) {
				boolean deny = StringUtils.containsAny(source, '<', '>', '\'', '"')
						|| source.contains("&#");
				if (deny) {
					throw new IllegalArgumentException("Invalid character detected. Please input again！");
				}
			}
			return source;
		}

	}

}
