package com.candlk.context.model;

import java.util.*;
import javax.annotation.Nonnull;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.candlk.common.context.I18N;
import com.candlk.common.model.ValueProxy;
import com.candlk.common.model.ValueProxyImpl;
import lombok.Getter;
import me.codeplayer.util.*;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

@Getter
public enum Language implements ValueProxyImpl<Language, Integer> {
	/** 英语 */
	en(0, "English", "英语", true),
	/** 中文（简体） */
	zh_Hans(1, "中文（简体）", "中文", false),
	/** 越南语 */
	pt(2, "Português", "葡萄牙语", true),
	//
	;
	/** 默认语言 */
	public static Language DEFAULT = en;

	// 定义私有变量
	@EnumValue
	public final Integer value;
	public final String desc;
	/** 开关 */
	public final boolean open;
	public final Locale locale;
	public final String alias;
	final ValueProxy<Language, Integer> proxy;

	Language(Integer value, String label, String desc, boolean open) {
		this.value = value;
		this.desc = desc;
		this.open = open;
		this.alias = name().replace('_', '-');
		this.locale = Locale.forLanguageTag(alias);
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final Language[] CACHE = ArrayUtil.filter(Language.values(), Language::isOpen);

	static final Map<String, Language> mapping = CollectionUtil.toMap(LinkedHashMap::new, Arrays.asList(CACHE), Language::getAlias);

	public static Language of(Integer value) {
		return X.expectNotNull(DEFAULT.getValueOf(value), DEFAULT);
	}

	public static Language nameOf(String value) {
		return EnumUtils.getEnum(Language.class, value, null);
	}

	public String msg(String code) {
		return I18N.msg(code, locale);
	}

	public String msg(String code, Object... args) {
		return I18N.msg(code, locale, args);
	}

	@Nonnull
	public static Language of(String language) {
		// 浏览器标准格式："zh-CN,zh;q=0.9"
		if (StringUtil.notEmpty(language)) {
			switch (language) {
				case "zh-CN", "zh-HK", "zh-TW" -> {
					return zh_Hans;
				}
			}
			String[] locales = StringUtils.split(language, ",;", 2);
			Language lang;
			for (String locale : locales) {
				if (locale.length() > 2) {
					lang = mapping.get(locale);
					if (lang != null) {
						return lang;
					}
					locale = locale.substring(0, 2);
				}
				lang = mapping.get(locale);
				if (lang != null) {
					return lang;
				}
			}
		}
		return en;
	}
}
