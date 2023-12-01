package com.candlk.common.context;

import java.lang.Character.UnicodeBlock;
import java.lang.Character.UnicodeScript;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import me.codeplayer.util.*;
import org.apache.commons.lang3.StringUtils;

public interface Matcher {

	default Matcher getMatcher() {
		return DefaultMatcher.INSTANCE;
	}

	default boolean matchUsername(String source) {
		return getMatcher().matchUsername(source);
	}

	default boolean matchPassword(String source) {
		return getMatcher().matchPassword(source);
	}

	default boolean matchRealName(String source) {
		return getMatcher().matchRealName(source);
	}

	default boolean matchNickname(String source) {
		return getMatcher().matchNickname(source);
	}

	default boolean matchPhone(String source) {
		return getMatcher().matchPhone(source);
	}

	default boolean matchEmoji(String source) {
		return getMatcher().matchEmoji(source);
	}

	default boolean matchEmail(String source) {
		return getMatcher().matchEmail(source);
	}

	default boolean matchURL(String source) {
		return getMatcher().matchURL(source);
	}

	default boolean matchChinese(String source, MatchMode mode) {
		return getMatcher().matchChinese(source, mode);
	}

	default boolean isChinese(int ch) {
		return getMatcher().isChinese(ch);
	}

	/**
	 * 根据 Unicode 编码完美地判断中文汉字和【标点符号】
	 *
	 * @param ch 需要判断的字符
	 */
	default boolean isAnyChinese(int ch) {
		return getMatcher().isAnyChinese(ch);
	}

	/**
	 * 指示指定的字符串是否符合有效的URL格式（绝对路径）
	 */
	static boolean isValidURL(final String url) {
		if (StringUtil.isEmpty(url)) {
			return false;
		}
		try {
			new URL(url);
			return true;
		} catch (MalformedURLException ignore) {
			return false;
		}
	}

	/**
	 * 指示指定的字符串是否符合有效的狭义上的URL格式（绝对路径） 狭义的即为：仅限于http、https
	 */
	static boolean matchHttpURL(final String url) {
		return (StringUtils.startsWithIgnoreCase(url, "http:") || StringUtils.startsWithIgnoreCase(url, "https")) && isValidURL(url);
	}

	/**
	 * 检测文件名是否合法
	 */
	static boolean isValidFilename(String filename) {
		if (StringUtils.containsAny(filename, '\\', '/')) {
			return false;
		}
		final char last = filename.charAt(filename.length() - 1);
		return last != '.' && !Character.isWhitespace(last);
	}

	/**
	 * 检测出生日期是否在指定的年龄范围内
	 *
	 * @param minAge 如果小于0，则不校验
	 * @param maxAge 如果小于0，则不校验
	 */
	static boolean matchAge(Date birthday, int minAge, int maxAge, long nowTime) {
		Assert.isTrue(minAge >= 0 || maxAge >= 0);
		final long birthdayTime = birthday.getTime();
		final long ageInMs = nowTime - birthdayTime;
		final int age = (int) (ageInMs / EasyDate.MILLIS_OF_DAY / 365); // 这个 age 只会偏大，不会偏小
		if (minAge >= 0 && age < minAge || maxAge >= 0 && (age - 1) > maxAge) {
			return false;
		}
		final EasyDate d = new EasyDate(nowTime);
		return (minAge < 0 || d.beginOf(Calendar.DATE).addYear(-minAge).getTime() >= birthdayTime)
				&& (maxAge < 0 || d.setTime(nowTime).beginOf(Calendar.DATE).addYear(-maxAge).getTime() < birthdayTime);
	}

	/**
	 * 检测出生日期是否在指定的年龄范围内
	 *
	 * @param minAge 如果小于0，则不校验
	 * @param maxAge 如果小于0，则不校验
	 */
	static boolean matchAge(Date birthday, int minAge, int maxAge) {
		return matchAge(birthday, minAge, maxAge, System.currentTimeMillis());
	}

	enum MatchMode {
		ALL,
		ANY,
		NONE
	}

	class DefaultMatcher implements Matcher {

		public static Matcher INSTANCE = new DefaultMatcher();

		/** 校验邮箱的正则表达式 */
		public Pattern regexEmail = Pattern.compile("^\\w+(?:\\.?[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+$");
		/** 校验用户名的正则表达式：4~16个字符，且必须以字母开头 */
		public Pattern regexUsername = Pattern.compile("^[a-z][a-z0-9_]{3,15}$");

		/** 验证用户名是否合法 */
		@Override
		public boolean matchUsername(String username) {
			return StringUtil.notEmpty(username) && regexUsername.matcher(username).matches();
		}

		@Override
		public boolean matchPassword(String source) {
			final int size = X.size(source);
			return size >= 6 && size <= 20;
		}

		@Override
		public boolean matchPhone(String source) {
			return NumberUtil.isNumber(source, 11) && source.charAt(0) == '1';
		}

		@Override
		public boolean matchEmail(String source) {
			return StringUtil.notEmpty(source) && regexEmail.matcher(source).matches();
		}

		@Override
		public boolean matchURL(String source) {
			return matchHttpURL(source);
		}

		@Override
		public boolean matchNickname(String source) {
			return StringUtil.notEmpty(source);
		}

		@Override
		public boolean matchEmoji(String source) {
			if (StringUtil.notEmpty(source)) {
				char[] chars = source.toCharArray();
				for (char c : chars) {
					if (hasEmoji(c)) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * 判断是否包含Emoji符号
		 */
		public boolean hasEmoji(int codePoint) {
			return (codePoint >= 0x2600 && codePoint <= 0x27BF) // 杂项符号与符号字体
					|| codePoint == 0x303D
					|| codePoint == 0x2049
					|| codePoint == 0x203C
					|| (codePoint >= 0x2000 && codePoint <= 0x200F)//
					|| (codePoint >= 0x2028 && codePoint <= 0x202F)//
					|| codePoint == 0x205F //
					|| (codePoint >= 0x2065 && codePoint <= 0x206F)//
					/* 标点符号占用区域 */
					|| (codePoint >= 0x2100 && codePoint <= 0x214F)// 字母符号
					|| (codePoint >= 0x2300 && codePoint <= 0x23FF)// 各种技术符号
					|| (codePoint >= 0x2B00 && codePoint <= 0x2BFF)// 箭头A
					|| (codePoint >= 0x2900 && codePoint <= 0x297F)// 箭头B
					|| (codePoint >= 0x3200 && codePoint <= 0x32FF)// 中文符号
					|| (codePoint >= 0xD800 && codePoint <= 0xDFFF)// 高低位替代符保留区域
					|| (codePoint >= 0xE000 && codePoint <= 0xF8FF)// 私有保留区域
					|| (codePoint >= 0xFE00 && codePoint <= 0xFE0F)// 变异选择器
					|| codePoint >= 0x10000; // Plane在第二平面以上的，char都不可以存，全部都转
		}

		@Override
		public boolean matchRealName(String source) {
			if (source == null) {
				return false;
			}
			final int length = source.length();
			if (length < 2 || length > 6) {
				return false;
			}
			return matchChinese(source, MatchMode.ALL);
		}

		@Override
		public boolean matchChinese(String source, MatchMode mode) {
			if (source == null) {
				return false;
			}
			final int len = source.length();
			for (int i = 0; i < len; ) {
				int codepoint = source.codePointAt(i);
				if (isChinese(codepoint)) {
					if (mode == MatchMode.ANY) {
						return true;
					}
					if (mode == MatchMode.NONE) {
						return false;
					}
				} else if (mode == MatchMode.ALL) {
					return false;
				}
				i += Character.charCount(codepoint);
			}
			return (len == 0) == (mode == MatchMode.NONE);
		}

		@Override
		public boolean isChinese(int ch) {
			// https://houbb.github.io/2019/12/25/java-china-punction }
			final UnicodeScript ub = UnicodeScript.of(ch);
			return ub == UnicodeScript.HAN;
		}

		/**
		 * 根据 Unicode 编码完美地判断中文汉字和【标点符号】
		 *
		 * @param ch 需要判断的字符
		 */
		@Override
		public boolean isAnyChinese(int ch) {
			final UnicodeBlock ub = UnicodeBlock.of(ch);
			return ub == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
					|| ub == UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
					|| ub == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
					|| ub == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
					|| ub == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C
					|| ub == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D
					|| ub == UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_E
					|| ub == UnicodeBlock.GENERAL_PUNCTUATION // 通用符号：百分号，千分号，单引号，双引号等
					|| ub == UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION // 顿号，句号，书名号，〸，〹，〺 等
					|| ub == UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS // 半角 和 全角符号：大于，小于，等于，括号，感叹号，加，减，冒号，分号等等
					;
		}

	}

}
