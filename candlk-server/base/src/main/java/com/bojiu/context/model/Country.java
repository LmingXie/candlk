package com.bojiu.context.model;

import java.util.*;
import java.util.stream.Collectors;

import com.bojiu.common.context.I18N;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import com.bojiu.context.AppRegion;
import com.bojiu.context.SystemInitializer;
import com.bojiu.context.i18n.UserI18nKey;
import com.bojiu.webapp.base.dto.MerchantContext;
import lombok.Getter;
import me.codeplayer.util.*;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

import static com.bojiu.context.i18n.UserModelI18nKey.*;

/**
 * 区域（国家或地区）（取名 Locale 容易与 <code>java.util.Locale</code> 冲突，因此取名 Country  ）
 */
@Getter
public enum Country implements LabelI18nProxy<Country, String> {
	/** 巴西 */
	BR(COUNTRY_BR, AppRegion.inBr(), "55", 9, 11, "GMT-3:00", Language.pt, AppRegion.inBr(), Currency.BRL),
	/** 马来西亚 */
	@Deprecated
	MY(COUNTRY_MY, false, "60", 9, 10, "GMT+8:00", Language.ms, Currency.MYR),
	/** 菲律宾 */
	PH(COUNTRY_PH, false, "63", 10, 10, "GMT+8:00", Language.ph, Currency.PHP),
	/** 印度尼西亚 */
	ID(COUNTRY_ID, false, "62", 10, 13, "GMT+7:00", Language.id, Currency.IDR),
	/** 印度 */
	IN(COUNTRY_IN, AppRegion.inAsia(), "91", 10, 10, "GMT+5:30", Language.hi, AppRegion.inAsia(), Currency.INR),
	/** 越南 */
	VN(COUNTRY_VN, false, "84", 9, 10, "GMT+7:00", Language.vi, Currency.VND),
	//
	;
	/** 该 国家/地区 手机号码前置的区号 */
	final String phoneCode;
	final ValueProxy<Country, String> proxy;
	/** 手机号码位数（除去 国家&地区 区号 前缀部分） 最小长度 和 最大长度 */
	public final int minLength, maxLength;
	/** 开关 */
	final boolean open;
	/** 该国家默认的主流语言 */
	public final Language language;
	public final TimeZone timeZone;
	/** 总台切换地区 */
	public final boolean isDisplay;
	/** 国家对应的货币 */
	public final Currency currency;

	Country(String label, boolean open, String phoneCode, int minLength, int maxLength, String timeZoneId, Language def, boolean isDisplay, Currency currency) {
		this.proxy = new ValueProxy<>(this, name(), label);
		this.open = open && def.open;
		this.phoneCode = phoneCode;
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.language = def;
		this.timeZone = TimeZone.getTimeZone(timeZoneId);
		this.isDisplay = isDisplay;
		this.currency = currency;
	}

	Country(String label, boolean open, String phoneCode, int minLength, int maxLength, String timeZoneId, Language def, Currency currency) {
		this(label, open, phoneCode, minLength, maxLength, timeZoneId, def, false, currency);
	}

	Country(String label, String phoneCode, int minLength, int maxLength, String timeZoneId, Language def, boolean isDisplay, Currency currency) {
		this(label, true, phoneCode, minLength, maxLength, timeZoneId, def, isDisplay, currency);
	}

	public static final Country[] CACHE = ArrayUtil.filter(values(), Country::isOpen);
	/** 用于总台可以切换的地区 */
	public static final Country[] PLATFORM_SWITCH = ArrayUtil.filter(values(), Country::isDisplay);

	public static Country of(String value) {
		return PRIMARY.getValueOf(value);
	}

	static final Map<String, Country> phoneCodeMap;
	/** 启用时区集合 ["GMT+08:00","GMT+07:00","GMT+03:00","GMT-03:00","GMT-07:00"] */
	public static final List<TimeZone> timeZoneList;
	public static final Map<TimeZone, Set<String>> timeZoneMap;

	static {
		final Country[] array = values();
		phoneCodeMap = new LinkedHashMap<>(array.length, 1F);
		for (Country c : array) {
			phoneCodeMap.put(c.phoneCode, c);
		}
		final ArrayList<TimeZone> timeZones = new ArrayList<>(Common.toSet(Arrays.asList(CACHE), Country::getTimeZone));
		timeZones.sort((a, b) -> Math.toIntExact(b.getRawOffset() - a.getRawOffset()));
		timeZoneList = timeZones;
		timeZoneMap = Arrays.stream(CACHE).collect(Collectors.groupingBy(Country::getTimeZone, Collectors.mapping(Country::getValue, Collectors.toSet())));
	}

	/** 返回业务运营的最小时区 */
	public static TimeZone minTimeZone() {
		final Country[] values = values();
		TimeZone min = null;
		for (Country c : values) {
			if (c.open && (min == null || c.timeZone.getRawOffset() < min.getRawOffset())) {
				min = c.timeZone;
			}
		}
		return min;
	}

	/** 默认国家 */
	public static Country PRIMARY = AppRegion.inBr() ? BR : IN;
	public static final String sep = "-";

	/**
	 * 将 完整手机号码 拆分为 【国家&地区 区号】（例如："55"） 和 【手机号码】"98765432" 两部分
	 *
	 * @param phone 形如 "55-98765432"
	 */
	public static String[] splitParts(String phone) {
		return phone.split(sep);
	}

	/**
	 * 根据手机号码解析所属 国家或地区
	 *
	 * @param phone 手机号，形如 "55-98765432"。
	 * @param assertValid 是否要求手机号码必须有效，否则报错
	 */
	public static Country parse(String phone, boolean assertValid) {
		final Country c = parse(phone);
		if (c == null && assertValid) {
			throw new IllegalArgumentException(I18N.msg(UserI18nKey.PHONE_FORMAT_ERROR));
		}
		return c;
	}

	public boolean matchesWithoutCode(String phone) {
		int length = phone.length();
		return minLength <= length && length <= maxLength && NumberUtil.isNumber(phone);
	}

	/**
	 * 智能匹配该区域的手机号码格式是否正确，支持 有地区编码前缀（"55-12345678"） 和 无前缀（"12345678"） 两种格式
	 */
	public boolean smartMatches(String phone) {
		if (StringUtil.isEmpty(phone)) {
			return false;
		}
		String[] phoneParts = splitParts(phone);
		if (phoneParts.length == 2) {
			return phoneCode.equals(phoneParts[0]) && matchesWithoutCode(phoneParts[1]);
		} else if (phoneParts.length == 1) {
			return matchesWithoutCode(phoneParts[0]);
		}
		return false;
	}

	/**
	 * 智能匹配该区域的手机号码格式是否正确，支持 有地区编码前缀（"55-12345678"） 和 无前缀（"12345678"） 两种格式
	 *
	 * @return 标准化处理后的手机号码格式。如果校验不对，则返回 null
	 */
	@Nullable
	public String normalize(String phone) {
		if (StringUtil.notEmpty(phone)) {
			String[] phoneParts = splitParts(phone);
			if (phoneParts.length == 2) {
				if (phoneCode.equals(phoneParts[0]) && matchesWithoutCode(phoneParts[1])) {
					final String adjust = trimPhoneNumber(phoneParts[1]);
					//noinspection StringEquality
					return adjust == phoneParts[1] ? phone : phoneCode + sep + adjust;
				}
			} else if (phoneParts.length == 1 && matchesWithoutCode(phoneParts[0])) {
				return phoneCode + sep + trimPhoneNumber(phoneParts[0]);
			}
		}
		return null;
	}

	private String trimPhoneNumber(String phone) {
		return this == VN ? StringUtils.removeStart(phone, '0') : phone;
	}

	/**
	 * 根据手机号码解析所属 国家或地区
	 *
	 * @param phone 手机号，形如 "55-98765432"。
	 * @return 手机归属 国家&地区
	 */
	@Nullable
	public static Country parse(String phone) {
		if (StringUtil.isEmpty(phone)) {
			return null;
		}
		String[] phoneParts = splitParts(phone);
		if (phoneParts.length == 2) {
			final Country c = phoneCodeOf(phoneParts[0]);
			return c != null && c.matchesWithoutCode(phoneParts[1]) && NumberUtil.isNumber(phoneParts[1]) ? c : null;
		}
		return null;
	}

	public static String checkPhone(Long merchantId, String phone) {
		Country country = MerchantContext.get(merchantId).getCountry();
		phone = country.normalize(phone); // 判断手机号码 和 商户运营地区 是否兼容，并进行标准化格式处理
		I18N.assertNotNull(phone, UserI18nKey.PHONE_FORMAT_ERROR);
		return phone;
	}

	@Nullable
	public static Country phoneCodeOf(String phoneCode) {
		final Country c = phoneCodeMap.get(phoneCode);
		return c != null && c.open ? c : null;
	}

	public EasyDate newEasyDate(long time) {
		return new EasyDate(time, timeZone);
	}

	/** 返回该国家默认时区的当前时间 */
	public EasyDate now() {
		return newEasyDate(System.currentTimeMillis());
	}

	public EasyDate newEasyDate(Date date) {
		return newEasyDate(date.getTime());
	}

	public static String assembleZone(Country c) {
		return c.getLabel() + "/" + c.getTimeZone().getID();
	}

	/**
	 * 将商户本地时间转换成 GMT+8 时间，传入本地时间 “2024-07-18 00:00:00”，本地时区为 GMT-3，则返回 “2024-07-18 11:00:00”
	 */
	public static Date toGmt8Date(final long localTime, final TimeZone localTimeZone) {
		return new Date(localTime + (SystemInitializer.DEFAULT_TIME_ZONE.getRawOffset() - localTimeZone.getRawOffset()));
	}

	/**
	 * 将 基于系统默认时区的指定时间 转为与 商户当地时区时间 具有相同输出表示 的系统时间对象
	 * <p> 如果系统时区是 GMT+8，传入 <code>"2023-05-02 12:00:00 GMT+8"</code>，商户所在国家时区是 GMT-3，对应的当地时间输出为 <code>"2023-05-02 01:00:00 GMT-3"</code>，
	 * 此时将返回与之具有相同输出的 <code>"2023-05-02 01:00:00 GMT+8"</code> 系统时间对象
	 */
	public static Date toLocalTime(final Date baseTime, final TimeZone timeZone) {
		int diff = timeZone.getRawOffset() - SystemInitializer.DEFAULT_TIME_ZONE.getRawOffset();
		return diff == 0 ? baseTime : new Date(baseTime.getTime() + diff);
	}

	/**
	 * 如果系统时区是 GMT+8，传入 "2023-05-02 12:00:00"（基于 GMT+8），所在国家时区是 GMT-3，则返回 "2023-05-02 01:00:00"（基于 GMT+8）的时间戳
	 */
	public static long toLocalTime(final long baseTime, final TimeZone timeZone) {
		return baseTime - SystemInitializer.DEFAULT_TIME_ZONE.getRawOffset() + timeZone.getRawOffset();
	}

	public Date toLocalTime(final Date baseTime) {
		return toLocalTime(baseTime, timeZone);
	}

	/**
	 * 将本地时间转换成 UTC 时间
	 */
	public Date toUtcTime(final Date baseTime) {
		return new Date(baseTime.getTime() - timeZone.getRawOffset());
	}

	public long toLocalTime(final long baseTime) {
		return toLocalTime(baseTime, timeZone);
	}

	public Date toGmt8Date(final Date localTime) {
		return toGmt8Date(localTime.getTime(), timeZone);
	}

	public String timeZoneId() {
		return StringUtils.removeStart(timeZone.getID(), "GMT");
	}

}