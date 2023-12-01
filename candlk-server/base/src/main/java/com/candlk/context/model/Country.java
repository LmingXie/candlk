package com.candlk.context.model;

import java.util.*;
import javax.annotation.Nullable;

import com.candlk.common.context.I18N;
import com.candlk.common.model.ValueProxy;
import lombok.Getter;
import me.codeplayer.util.*;

/**
 * 区域（国家或地区）（取名 Locale 容易与 <code>java.util.Locale</code> 冲突，因此取名 Country  ）
 */
@Getter
public enum Country implements LabelI18nProxy<Country, String> {
	/** 巴西 */
	BR("巴西", "55", 8, 8, "GMT-3:00"),
	//
	;
	/** 该 国家/地区 手机号码前置的区号 */
	final String phoneCode;
	/** 国旗或区旗 */
	final String nationalFlag;
	final ValueProxy<Country, String> proxy;
	/** 手机号码位数（除去 国家&地区 区号 前缀部分） 最小长度 和 最大长度 */
	public final int minLength, maxLength;
	/** 开关 */
	final boolean open;
	final TimeZone timeZone;

	Country(String label, boolean open, String phoneCode, int minLength, int maxLength, String timeZoneId) {
		this.proxy = new ValueProxy<>(this, name(), label);
		this.open = open;
		this.phoneCode = phoneCode;
		this.nationalFlag = name().toLowerCase() + ".png"; // TODO 要填写实际的图片链接（或者让前端直接按照约定自行拼接）
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.timeZone = TimeZone.getTimeZone(timeZoneId);
	}

	Country(String label, String phoneCode, int minLength, int maxLength, String timeZoneId) {
		this(label, true, phoneCode, minLength, maxLength, timeZoneId);
	}

	public static final Country[] CACHE = ArrayUtil.filter(values(), Country::isOpen);

	public static Country of(String value) {
		return BR.getValueOf(value);
	}

	static final Map<String, Country> phoneCodeMap;
	static final Map<String, String> areaPhoneCodeMap;

	static {
		final Country[] array = values();
		phoneCodeMap = new LinkedHashMap<>(array.length, 1F);
		areaPhoneCodeMap = new LinkedHashMap<>(array.length, 1F);
		for (Country c : array) {
			phoneCodeMap.put(c.phoneCode, c);
			areaPhoneCodeMap.put(c.name(), c.phoneCode);
		}
	}

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

	private boolean matchesWithoutCode(String phone) {
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
					return phone;
				}
			} else if (phoneParts.length == 1) {
				if (matchesWithoutCode(phoneParts[0])) {
					return phoneCode + sep + phoneParts[0];
				}
			}
		}
		return null;
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

	@Nullable
	public static Country phoneCodeOf(String code) {
		final Country area = phoneCodeMap.get(code);
		return area != null && area.open ? area : null;
	}

	public static String areaCodeOf(String areaCode) {
		return areaPhoneCodeMap.get(areaCode);
	}

	public EasyDate newEasyDate(long time) {
		return new EasyDate(time).setTimeZone(timeZone);
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
}
