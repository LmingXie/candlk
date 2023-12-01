package com.candlk.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.*;
import java.util.*;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.candlk.common.context.Matcher;
import me.codeplayer.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

public class Formats {

/*
	【必读】：约定的方法后缀表示对应的特定含义，约定如下：

	以"_"结尾的，为当前业务属性最常用的时间格式

	以"_*H"结尾的，表示包含HTML代码

	"_"之后包含"C"结尾的，表示包含字符（中文字符）。

	"formatDate_"       -> 当前业务属性最常用的日期格式
	"formatDate_D"      -> "yyyy-MM-dd"
	"formatDate_DT"     -> "yyyy-MM-dd HH:mm:ss"
	"formatDate_L"      -> "yyyy-MM-dd HH:mm:ss.SSS"
	"formatDate_O"      -> "刚刚"、"几分钟前"

	"format{Number}_I"  -> "#" 整数
	"format{Number}_D"  -> "#.00" 两位小数
	"formatNumber_M"	-> "#,###.00"
	 */
	// Time
	/** (D)ate */
	public static final String formatDate_D = "yyyy-MM-dd";
	/** (T)ime */
	public static final String formatDate_T = "HH:mm:ss";
	/** (D)ate(M)inute */
	public static final String formatDate_DM = "yyyy-MM-dd HH:mm";
	/** (D)ate(T)ime */
	public static final String formatDate_DT = "yyyy-MM-dd HH:mm:ss";
	/** (L)ong */
	public static final String formatDate_L = "yyyy-MM-dd HH:mm:ss.SSS";

	public static final String formatDate_YMD = "yyyyMMdd";

	/* Date Format start */
	public static final TimeZone DEF_TIME_ZONE = TimeZone.getDefault();

	/**
	 * 获取指定模式的日期格式化工具
	 */
	public static FastDateFormat getDateFormat(String pattern) {
		return FastDateFormat.getInstance(pattern, DEF_TIME_ZONE, Locale.getDefault());
	}

	/**
	 * 格式化指定的日期，并转换为"yyyy-MM-dd HH:mm:ss"格式的字符串
	 */
	public static String formatDatetime(@Nullable Date date) {
		return formatDate(date, formatDate_DT);
	}

	/**
	 * 格式化指定的日期，并转换为"yyyy-MM-dd HH:mm"格式的字符串
	 */
	public static String formatDefDate(@Nullable Date date) {
		return formatDate(date, formatDate_DM);
	}

	/**
	 * 格式化指定的日期，并转换为"yyyy-MM-dd"格式的字符串
	 */
	public static String formatDate_D(@Nullable Date date) {
		return formatDate(date, formatDate_D);
	}

	/**
	 * 格式化指定的日期，并转换为"yyyy-MM-dd"格式的字符串
	 *
	 * @param defaultValue 如果 <code>date</code> 为null，则返回该值
	 */
	public static String formatDate_D(@Nullable Date date, String defaultValue) {
		return date == null ? defaultValue : formatDate_D(date);
	}

	/**
	 * 格式化指定的日期，并转换为"yyyy-MM-dd HH:mm:ss"格式的字符串
	 */
	public static String formatDate_DT(@Nullable Date date) {
		return formatDate(date, "yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 格式化指定的日期，并转换为"HH:mm"格式的字符串
	 */
	public static String formatDate_HM(Date date) {
		return formatDate(date, "HH:mm");
	}

	/**
	 * 将指定时间与当前时间比较，返回人性化的时间差描述
	 */
	public static String formatDate_O(Date base) {
		if (base == null) {
			return null;
		}
		long diff = System.currentTimeMillis() - base.getTime();
		final boolean negative = diff < 0;
		if (negative) {
			diff = -diff;
		}
		if (diff < 1000 * 60) { // 1分钟之内(1000 * 60 * 1)
			return "刚刚";
		}
		final char offset = negative ? '后' : '前';
		if (diff < (1000 * 60 * 60)) { // 1小时之内
			return (diff / (1000 * 60)) + "分" + offset;
		} else if (diff < (1000L * 60 * 60 * 24)) { // 1天之内
			return (diff / (1000 * 60 * 60)) + "小时" + offset;
		} else if (diff < (1000L * 60 * 60 * 24 * 30)) { // 1个月内
			return (diff / (1000L * 60 * 60 * 24)) + "天" + offset;
		} else if (diff < (1000L * 60 * 60 * 24 * 365)) { // 1年内
			return (diff / (1000L * 60 * 60 * 24 * 30)) + "个月" + offset;
		} else {
			return (diff / (1000L * 60 * 60 * 24 * 365)) + "年" + offset;
		}
	}

	/**
	 * 仅保留整数部分<p>
	 * <code>number -> "123"</code>
	 */
	public static String formatNumber_I(Number value) {
		return value == null ? "" : Long.toString(value.longValue());
	}

	/**
	 * 仅保留两位小数<p>
	 * <code>number -> "123.00"</code>
	 */
	public static String formatNumber_D(Number value) {
		return formatNumber(value, "0.00");
	}

	/**
	 * 仅保留两位小数 + 每三位数字（千分位）用 "," 隔开<p>
	 * <code>number -> "1,234,567.00"</code>
	 */
	public static String formatNumber_M(Number value) {
		return formatNumber(value, "#,##0.00");
	}

	/**
	 * 保留整数部分
	 * <code> number -> 1,231,512 </code>
	 */
	public static String formatNumber_INT(Number value) {
		return formatNumber(value, "#,###");
	}

	/**
	 * 格式化金额数字为"###,###.00"格式，并附带单位等HTML代码
	 */
	public static String formatSignedMoney(Number money, char signCh, boolean withHtml) {
		String className, sign;
		switch (signCh) {
			case '+':
				className = "sign-plus";
				sign = "+";
				break;
			case '-':
				className = "sign-minus";
				sign = "";
				break;
			default:
				className = "sign-none";
				sign = " ";
				break;
		}
		if (withHtml) {
			return "<span class=\"" + className + "\">" + sign + formatNumber_M(money) + "</span>";
		} else {
			return sign + formatNumber_M(money);
		}
	}

	/**
	 * 根据金额大小，显示对应单位对应格式的金额字符串输出<p>
	 * {@code val -> [ "123.12", "万" ] }
	 *
	 * @param val 指定的金额数字
	 * @param scale 指定的精确度(精确到的小数位数)
	 * @param roundingMode 舍入模式
	 * @param formatStyle <ul>
	 * <li>0(固定)：固定精度(小数位数)</li>
	 * <li>1(半固定)：小数部分为0时则无小数，否则按照固定精度显示</li>
	 * <li>2(半浮动)：小数部分为0时则无小数，否则按浮动精度显示(如果精度大于指定精度，则以指定的精度显示；否则以实际精度显示)</li>
	 * <li>3(半浮动)：小数部分为0时则无小数，否则以 浮动精度且不丢失精度的形式 显示(数值 + 合理单位])</li>
	 * </ul>
	 * @return <code>[ "数值", "数值单位"（""、"万" 或 "亿" ） ]</code>
	 * @since 1.0
	 */
	public static String[] splitNumberUnit(final Number val, final int scale, RoundingMode roundingMode, final int formatStyle) {
		if (formatStyle < 0 || formatStyle > 3) {
			throw new ArithmeticException("Unexpected formatStyle: " + formatStyle);
		}
		if (scale < 0 || scale > 19) {
			throw new ArithmeticException("Unexpected scale: " + scale);
		}
		BigDecimal d;
		if (val instanceof BigDecimal) {
			d = (BigDecimal) val;
		} else if (val instanceof Integer || val instanceof Long) {
			d = BigDecimal.valueOf(val.longValue());
		} else {
			d = new BigDecimal(val.toString());
		}
		String unit;
		int pointToLeft = 0;
		if (d.compareTo(Arith.MYRIAD) < 0) { // 1万以下，显示单位为"元"
			unit = "";
		} else if (d.compareTo(Arith.HANDRED_MILLION) < 0) { // 1亿以下，显示单位为"万元"
			if (formatStyle != 3 || d.remainder(Arith.HUNDRED).compareTo(BigDecimal.ZERO) == 0) {
				pointToLeft = 4;
				unit = "万";
			} else {
				unit = "";
			}
		} else {
			pointToLeft = 8;
			unit = "亿";
		}
		BigDecimal bridge = pointToLeft == 0 ? d : d.movePointLeft(pointToLeft); // 得到指定单位的数值
		String str = null;
		switch (formatStyle) {
			case 0:
				if (bridge.scale() == scale) {
					str = bridge.toPlainString();
				}
				break;
			case 1:
				if (bridge.scale() == 0) {
					str = bridge.toPlainString();
				}
				break;
			case 2:
				if (bridge.scale() <= scale) {
					str = bridge.stripTrailingZeros().toPlainString();
				}
				break;
			case 3:
				str = bridge.stripTrailingZeros().toPlainString();
				break;
		}
		if (str == null) {
			BigDecimal result = bridge.setScale(scale, roundingMode); // 最后所需的结果数值
			str = result.toPlainString(); // 最终显示的结果字符串
			if ((formatStyle == 1 || formatStyle == 2) && bridge.compareTo(result) == 0) { // 如果实际精度没有改变，并且需要输出非固定格式
				int length = str.length();
				if (length > 2) { // "x.x"至少是3位以上字符才需要进行格式化处理
					// 小数点的索引，后缀0的个数，循环索引
					int point = -1, zeroLength = 0, i = length;
					boolean lastIsZero = true; // 上一个数字是否为'0'
					while (i-- > 1) { // 第一位无需循环处理
						char ch = str.charAt(i);
						if (ch == '.') {
							point = i;
							break;
						}
						if (lastIsZero) {
							if (ch == '0') { // 如果为'0'，则增加后缀0的个数
								zeroLength++;
							} else {
								lastIsZero = false;
							}
						}
					}
					if (point != -1) { // 如果有小数点，则需要特殊处理
						if (length == point + 1 + zeroLength) { // 如果小数点后全是0
							str = str.substring(0, point);
						} else if (zeroLength > 0 && formatStyle == 2) { // 如果小数点后存在后缀0，并且采用半浮动的格式输出
							str = str.substring(0, length - zeroLength);
						}
					}
				}
			}
		}
		return new String[] { str, unit };
	}

	/**
	 * 根据金额大小，显示对应单位对应格式的金额字符串输出【舍入模式：四舍五入】<p>
	 * {@code val -> [ "123.12", "万" ] }
	 *
	 * @param val 指定的金额数字
	 * @param scale 指定的精确度(精确到的小数位数)
	 * @param formatStyle <ul>
	 * <li>0(固定)：固定精度(小数位数)</li>
	 * <li>1(半固定)：小数部分为0时则无小数，否则按照固定精度显示</li>
	 * <li>2(半浮动)：小数部分为0时则无小数，否则按浮动精度显示(如果精度大于指定精度，则以指定的精度显示；否则以实际精度显示)</li>
	 * <li>3(半浮动)：小数部分为0时则无小数，否则以 浮动精度且不丢失精度的形式 显示(数值 + 合理单位])</li>
	 * </ul>
	 * @return <code>[ "数值", "数值单位"（""、"万" 或 "亿" ） ]</code>
	 * @since 1.0
	 */
	public static String[] splitNumberUnit(final Number val, final int scale, final int formatStyle) {
		return splitNumberUnit(val, scale, RoundingMode.HALF_UP, formatStyle);
	}

	/**
	 * 根据金额大小，显示对应单位对应格式的金额字符串输出<p>
	 * <code> value + "&lt;em>%D&lt;/em>%U元" -> "&lt;em><123.02&lt;/em>万元"（例如） </code>
	 *
	 * @param template 渲染模板，可使用占位符 <code>"#0"</code> 和 <code>"#1"</code> 分别表示 数值 和 单位部分。
	 * @see #splitNumberUnit(Number, int, int)
	 */
	public static String formatNumberWithTemplate(Number value, int scale, int formatStyle, @Nullable String template) {
		final String[] blocks = splitNumberUnit(value, scale, formatStyle);
		if (template == null) {
			return blocks[0].concat(blocks[1]);
		}
		return renderTemplate(template, blocks);
	}

	/**
	 * 渲染模板
	 *
	 * @param template 渲染模板，可使用占位符 <code>"#0"</code> 和 <code>"#1"</code> 分别表示 数值 和 单位部分。
	 * @param digitAndUnitPair 只会替换前面两个元素的占位符
	 */
	public static String renderTemplate(@Nonnull String template, String... digitAndUnitPair) {
		return StringUtils.replaceEach(template, new String[] { "#0", "#1" }, digitAndUnitPair);
	}

	/**
	 * 根据金额大小，显示对应单位对应格式的金额字符串输出<p>
	 * <code> money -> "&lt;em><123.02&lt;/em>万" </code>
	 *
	 * @see #splitNumberUnit(Number, int, int)
	 */
	public static String formatMoneyWithUnit(Number money, int scale, int formatStyle) {
		return formatNumberWithTemplate(money, scale, formatStyle, "<em>#0</em>#1");
	}

	/**
	 * 语义化数字，使数字以常人能够理解的简短形式进行展示（例如："2000"、"5万"、"2.35万"、"2.561亿"）
	 */
	public static String semanticNumber(@Nullable Number value) {
		if (value == null) {
			return "";
		}
		BigDecimal d;
		if (value instanceof BigDecimal) {
			d = ((BigDecimal) value);
		} else {
			double val = value.doubleValue();
			if (val < 10000) {
				long longVal = value.longValue();
				if (val == longVal) {
					return Long.toString(longVal);
				}
			}
			d = new BigDecimal(value.toString());
		}
		final String unit;
		final int pointToLeft;
		if (d.compareTo(Arith.MYRIAD) < 0) { // 1万以下 或 （>= 10000 且 精度小于十位）数时，显示单位为"元"
			pointToLeft = 0;
			unit = "";
		} else if (d.compareTo(Arith.HANDRED_MILLION) < 0) { // 1亿以下，显示单位为"万元"
			if (d.remainder(Arith.HUNDRED).compareTo(BigDecimal.ZERO) == 0) {
				pointToLeft = 4;
				unit = "万";
			} else {
				pointToLeft = 0;
				unit = "";
			}
		} else { // 大金额显示为"亿元"
			pointToLeft = 8;
			unit = "亿";
		}
		BigDecimal bridge = pointToLeft == 0 ? d : d.movePointLeft(pointToLeft); // 得到指定单位的数值
		String valStr;
		if (bridge.scale() == 0) {
			valStr = bridge.toPlainString();
		} else {
			valStr = bridge.stripTrailingZeros().toPlainString();
		}
		return valStr.concat(unit);
	}

	/**
	 * 语义化数字，使数字以国际上能够理解的简短形式进行展示（例如："200"、"1.1k"、"2.3m"、"1.2b"）
	 */
	public static String internationalNumber(@Nullable Number value) {
		if (value == null) {
			return "";
		}
		final int unitFactor = 1000;
		final long val = value.longValue();
		if (val < unitFactor) {
			return Long.toString(val);
		}
		final String units = "kmb";
		final int len = units.length();
		double base = val;
		int i = 0;
		while (i < len && (base = base / unitFactor) >= unitFactor) {
			i++;
		}
		return new BigDecimal(base).setScale(1, RoundingMode.FLOOR).toPlainString() + units.charAt(Math.min(i, len - 1));
	}

	/**
	 * 根据指定的格式化器，将指定的Date值格式化为对应的文本字符串
	 *
	 * @param pattern 表示格式化器的字符串
	 * @param value 需要格式化的值
	 */
	public static String formatDate(Date value, String pattern) {
		if (value == null) {
			return null;
		}
		switch (pattern) {
			case "yyyy-MM-dd": // "yyyy-MM-dd"
				return EasyDate.toString(value);
			case "yyyy-MM-dd HH:mm:ss": // "yyyy-MM-dd HH:mm:ss"
				return EasyDate.toDateTimeString(value);
			default:
				return getDateFormat(pattern).format(value);
		}
	}

	/**
	 * 根据指定的格式化器，将指定的Number值格式化为对应的文本字符串
	 *
	 * @param pattern 表示格式化器的字符串
	 * @param value 需要格式化的值
	 */
	public static String formatNumber(Number value, String pattern) {
		if (value == null) {
			return null;
		}
		Format format = (Format) ThreadLocalUtil.get(pattern);
		if (format == null) {
			ThreadLocalUtil.put(pattern, format = new DecimalFormat(pattern));
		}
		return format.format(value);
	}

	/**
	 * 匿名化手机号码
	 */
	public static String anonymousPhone(final String phone) {
		return StringUtil.isEmpty(phone) ? "" : StringUtil.replaceChars(phone, '*', 6, 10);
	}

	/**
	 * 匿名化用户名
	 */
	public static String anonymousUsername(final String username) {
		if (username.charAt(0) == '@') {
			return StringUtil.replaceSubstring(username, "****", 4, -4);
		}
		return StringUtil.replaceSubstring(username, "**", 2, -1);
	}

	/**
	 * 匿名化真实姓名
	 */
	public static String anonymousRealName(final String realName) {
		if (StringUtil.notEmpty(realName)) {
			return StringUtil.replaceChars(realName, '*', 0, -1);
		}
		return realName;
	}

	/**
	 * 匿名化邮件地址
	 */
	public static String anonymousEmail(final String email) {
		if (StringUtil.notEmpty(email)) {
			return StringUtil.replaceSubstring(email, "***", 3, email.lastIndexOf('@'));
		}
		return email;
	}

	/**
	 * 匿名化证件号码
	 */
	public static String anonymousCertNo(final String certNo) {
		if (StringUtil.isEmpty(certNo)) {
			return certNo;
		}
		return StringUtil.replaceSubstring(certNo, "***", 3, -3);
	}

	/**
	 * 匿名化银行卡号
	 */
	public static String anonymousBankAccount(final String bankAccount) {
		if (StringUtil.isEmpty(bankAccount)) {
			return bankAccount;
		}
		return StringUtil.replaceSubstring(bankAccount, "******", 4, -4);
	}

	/**
	 * 根据出生日前获取年龄
	 */
	public static int getAge(Date birthday) {
		return (int) new EasyDate().calcDifference(birthday, Calendar.YEAR, RoundingMode.FLOOR);
	}

	/**
	 * 根据证件信息初始化该用户的性别、出生日期
	 */
	public static void initWithCertNo(String idCardNo, @Nullable Consumer<Integer> genderSetter, @Nullable Consumer<Date> birthdaySetter) {
		Assert.isTrue(idCardNo.length() == 18, "身份证号码必须为18位！");
		if (genderSetter != null) {
			genderSetter.accept("13579".indexOf(idCardNo.charAt(16)) != -1 ? 1 : 0);// 奇数表示男性，偶数表示女性；
		}
		if (birthdaySetter != null) {
			String birth = idCardNo.substring(6, 14);
			FastDateFormat fastDateFormat = getDateFormat("yyyyMMdd");
			try {
				birthdaySetter.accept(fastDateFormat.parse(birth));
			} catch (Exception e) {
				throw new IllegalArgumentException("解析出生日期时出错", e);
			}
		}
	}

	/**
	 * 从身份证号码中解析出对应的出生日期
	 */
	public static Date parseBirthdayByIdCardNo(String idCardNo, int checkMinAge) {
		Assert.isTrue(idCardNo.length() == 18, "身份证号码必须为18位！");
		String birth = idCardNo.substring(6, 14);
		FastDateFormat fastDateFormat = getDateFormat("yyyyMMdd");
		Date birthday;
		try {
			birthday = fastDateFormat.parse(birth);
		} catch (ParseException e) {
			throw new IllegalArgumentException("请输入有效的身份证号码！", e);
		}
		if (checkMinAge >= 0 && !Matcher.matchAge(birthday, checkMinAge, -1)) {
			throw new IllegalArgumentException("用户必须年满 " + checkMinAge + " 周岁！");
		}
		return birthday;
	}

	public static int getYyyyMM(EasyDate d) {
		return d.getYear() * 100 + d.getMonth();
	}

	public static int getYyyyMMdd(EasyDate d) {
		return d.getYear() * 10000 + d.getMonth() * 100 + d.getDay();
	}

}
