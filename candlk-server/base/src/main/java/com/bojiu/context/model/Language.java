package com.bojiu.context.model;

import java.util.*;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.context.I18N;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.model.ValueProxyImpl;
import com.bojiu.common.util.Common;
import com.bojiu.context.AppRegion;
import lombok.Getter;
import me.codeplayer.util.*;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import static com.bojiu.context.model.BaseI18nKey.*;

@Getter
public enum Language implements ValueProxyImpl<Language, Integer> {
	/** 英语 */
	en(0, "English", LANGUAGE_EN, true),
	/** 中文（简体） */
	zh(1, "简体中文", LANGUAGE_ZH, true),
	/** 葡萄牙语 */
	pt(2, "Português", LANGUAGE_MS, AppRegion.inBr()),
	/** 马来西亚语 */
	@Deprecated
	ms(3, "Melayu", LANGUAGE_PT, false),
	/** 印尼语 */
	@Deprecated
	id(4, "Bahasa Indonesia", LANGUAGE_ID, false),
	/** 菲律宾语 */
	@Deprecated
	ph(5, "Pilipino", LANGUAGE_PH, false),
	/** 印地语 */
	hi(6, "हिन्दी", LANGUAGE_HI, AppRegion.inAsia()),
	/** 越南语 */
	@Deprecated
	vi(7, "Tiếng Việt", LANGUAGE_VI, false),
	//
	;
	/** 默认语言 */
	public static Language DEFAULT = en;

	@EnumValue
	public final Integer value;
	private final String desc;
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

	public static final Language[] CACHE;

	static final Map<String, Language> mapping;

	static {
		CACHE = ArrayUtil.filter(Language.values(), Language::isOpen);
		List<Language> list = CollectionUtil.asArrayList(CACHE);
		list.add(Language.vi);
		mapping = CollectionUtil.toMap(LinkedHashMap::new, list, Language::getAlias);
	}

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

	private static Language map(String alias) {
		return switch (alias) {
			case "zh-CN", "zh-HK", "zh-TW", "zh" -> Language.zh;
			default -> mapping.get(alias);
		};
	}

	public String getDesc() {
		return I18N.msg(desc);
	}

	public static <V> V getValueOrDefault(final Map<Language, V> languageMap, Language language) {
		if (languageMap == null) {
			return null;
		}
		V v = languageMap.get(language);
		if (v == null && language != Language.DEFAULT) {
			v = languageMap.get(Language.DEFAULT);
		}
		return v;
	}

	public static <V> V getValueOrDefault(@Nullable Map<String, V> languageMap, String language) {
		if (languageMap == null) {
			return null;
		}
		V v = languageMap.get(language);
		if (v == null && !Language.DEFAULT.name().equals(language)) {
			v = languageMap.get(Language.DEFAULT.name());
		}
		return v;
	}

	@NonNull
	public static Language of(String language) {
		return of(language, DEFAULT);
	}

	public static Language of(String language, Language defaultLang) {
		final int size = X.size(language);
		if (size > 0) {
			if (size <= 4) { // 4个字符以下的语言标识，不需要拆分处理，因为 "zh,en"、"zh-CN"、"zh_CN" 或 "zh;q=1" 至少都要包含 5 个字符
				Language lang = map(language);
				if (lang != null) {
					return lang;
				}
			} else { // 浏览器标准格式："zh-CN,zh;q=0.9,en;q=0.8"
				String[] locales = size == 5 && switch (language.charAt(2)) {
					case '-', '_' -> true;
					default -> false;
				} ? new String[] { language } : StringUtils.split(language, ",;", 2);
				for (String locale : locales) {
					Language lang = map(locale);
					if (lang != null) {
						return lang;
					}
					if (locale.length() > 2) {
						lang = map(locale.substring(0, 2));
						if (lang != null) {
							return lang;
						}
					}
				}
			}
		}
		return defaultLang;
	}

	public static List<Language> resolve(String languages, @Nullable Language primary) {
		final List<Language> langs = StringUtil.splitIntAsList(languages, Common.CHAR_SEP, Language::of);
		if (primary != null) {
			int index = langs.indexOf(primary);
			if (index < 0) {
				throw new IllegalArgumentException("该商户所在国家必须包含语言：" + primary.getLabel());
			}
			if (index > 0) {
				langs.set(index, langs.get(0));
				langs.set(0, primary);
			}
		}
		return langs;
	}

	public static Language[] parse(String languages, @Nullable Language primary) {
		return resolve(languages, primary).toArray(new Language[0]);
	}

}