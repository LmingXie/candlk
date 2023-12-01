package com.candlk.common.util;

import java.util.Calendar;
import java.util.Date;
import java.util.function.Supplier;

import com.candlk.common.context.*;
import com.candlk.common.context.Matcher.MatchMode;
import com.candlk.common.model.ErrorMessageException;
import me.codeplayer.util.Assert;
import me.codeplayer.util.EasyDate;

public class Validators {

	/**
	 * 如果错误信息不为空，则触发该错误异常
	 */
	public static void triggerError(String error) {
		if (error != null) {
			throw new ErrorMessageException(error);
		}
	}

	/**
	 * 根据校验结果判断是否需要触发异常
	 *
	 * @param result 如果校验结果为 false，则触发异常
	 * @param errorMessage 异常的 message 文本
	 */
	public static boolean triggerError(boolean result, String errorMessage) {
		if (!result) {
			throw new ErrorMessageException(errorMessage);
		}
		return true;
	}

	/**
	 * 根据校验结果判断是否需要触发异常
	 *
	 * @param result 如果校验结果为false，则触发异常
	 * @param errorMessage 异常的message文本
	 */
	public static boolean triggerError(boolean result, Supplier<String> errorMessage) {
		if (!result) {
			throw new ErrorMessageException(errorMessage.get());
		}
		return true;
	}

	static final int[] idCardFactor = { 7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2 };

	/**
	 * 根据公安部身份证校验位算法检测身份证的号码规则是否合法
	 */
	public static boolean matchIdCard(String certNo) {
		int sum = 0;
		try {
			for (int i = 0; i < idCardFactor.length; i++) {
				sum += Character.digit(certNo.charAt(i), 10) * idCardFactor[i];
			}
			return certNo.charAt(17) == "10X98765432".charAt(sum % 11);
		} catch (Exception ignore) {
			return false;
		}
	}

	/**
	 * 校验用户真实姓名是否合规（必须是 2~6位 中文）
	 */
	public static boolean matchRealName(String realName) {
		return Context.get().matchRealName(realName);
	}

	/**
	 * 根据 Unicode 编码完美地判断中文汉字和【标点符号】
	 *
	 * @param ch 需要判断的字符
	 */
	public static boolean isAnyChinese(int ch) {
		return Context.get().isAnyChinese(ch);
	}

	/**
	 * 根据 Unicode 编码判断是否为中文汉字
	 *
	 * @param ch 需要判断的字符
	 */
	public static boolean isChinese(int ch) {
		// https://houbb.github.io/2019/12/25/java-china-punction }
		return Context.get().isChinese(ch);
	}

	/**
	 * 完整的判断中文汉字和符号
	 *
	 * @param strName 需要判断的字符串
	 */
	public static boolean isAllChinese(String strName) {
		return Context.get().matchChinese(strName, MatchMode.ALL);
	}

	/**
	 * 验证用户名是否合法
	 */
	public static boolean matchUsername(String username) {
		return Context.get().matchUsername(username);
	}

	/**
	 * 验证用户名是否合法，如果校验失败则抛出异常
	 *
	 * @param msgWhenError 如果校验失败，需要抛出的异常文本信息
	 */
	public static boolean matchUsername(String username, String msgWhenError) {
		return triggerError(matchUsername(username), msgWhenError);
	}

	/**
	 * 验证昵称是否合法
	 */
	public static boolean matchNickname(String nickname) {
		return Context.get().matchNickname(nickname);
	}

	/**
	 * 验证是否含有emoji表情
	 */
	public static boolean matchEmoji(String slogan) {
		return Context.get().matchEmoji(slogan);
	}

	/**
	 * 验证电话号码是否合法
	 */
	public static boolean matchPhone(String phone) {
		return Context.get().matchPhone(phone);
	}

	/**
	 * 验证电话号码是否合法，如果校验失败则抛出异常
	 *
	 * @param msgWhenError 如果校验失败，需要抛出的异常文本信息
	 */
	public static void matchPhone(String phone, String msgWhenError) {
		triggerError(matchPhone(phone), msgWhenError);
	}

	/**
	 * 验证用户密码是否合法
	 */
	public static boolean matchPassword(String password) {
		return Context.get().matchPassword(password);
	}

	/**
	 * 验证用户密码是否合法，如果校验失败，需要抛出的异常文本信息
	 *
	 * @param msgWhenError 如果校验失败，需要抛出的异常文本信息
	 */
	public static boolean matchPassword(String password, String msgWhenError) {
		return triggerError(matchPassword(password), msgWhenError);
	}

	/**
	 * 验证邮箱是否合法
	 */
	public static boolean matchEmail(String email) {
		return Context.get().matchEmail(email);
	}

	/**
	 * 指示指定的字符串是否符合有效的狭义上的URL格式（绝对路径） 狭义的即为：仅限于http、https
	 */
	public static boolean matchHttpURL(final String url) {
		return Matcher.matchHttpURL(url);
	}

	/**
	 * 指示指定的字符串是否符合有效的URL格式（绝对路径）
	 */
	public static boolean matchURL(final String url) {
		return Matcher.isValidURL(url);
	}

	/**
	 * 检测出生日期是否在指定的年龄范围内
	 *
	 * @param minAge 如果小于0，则不校验
	 * @param maxAge 如果小于0，则不校验
	 */
	public static final boolean matchAge(Date birthday, int minAge, int maxAge, long nowTime) {
		Assert.isTrue(minAge >= 0 || maxAge >= 0);
		final long birthdayTime = birthday.getTime();
		final long ageInMs = nowTime - birthdayTime;
		final int age = (int) (ageInMs / EasyDate.MILLIS_OF_DAY / 365); // 这个 age 只会偏大，不会偏小
		if (minAge >= 0 && age < minAge || maxAge >= 0 && (age - 1) > maxAge) {
			return false;
		}
		final EasyDate d = new EasyDate(nowTime);
		return (minAge < 0 || d.beginOf(Calendar.DAY_OF_MONTH).addYear(-minAge).getTime() >= birthdayTime)
				&& (maxAge < 0 || d.setTime(nowTime).beginOf(Calendar.DAY_OF_MONTH).addYear(-maxAge).getTime() < birthdayTime);
	}

	/**
	 * 检测出生日期是否在指定的年龄范围内
	 *
	 * @param minAge 如果小于0，则不校验
	 * @param maxAge 如果小于0，则不校验
	 */
	public static final boolean matchAge(Date birthday, int minAge, int maxAge) {
		return matchAge(birthday, minAge, maxAge, RequestContext.get().now().getTime());
	}

}
