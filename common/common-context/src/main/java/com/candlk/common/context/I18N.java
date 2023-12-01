package com.candlk.common.context;

import java.util.Locale;
import java.util.function.Supplier;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.Nullable;

public abstract class I18N {

	static MessageSource ms;

	static Supplier<Locale> localeSupplier = LocaleContextHolder::getLocale;

	public static Locale get() {
		return localeSupplier.get();
	}

	public static MessageSource ms() {
		if (ms == null) {
			ms = Context.getBean(MessageSource.class);
		}
		return ms;
	}

	public static void setLocaleSupplier(Supplier<Locale> localeSupplier) {
		I18N.localeSupplier = localeSupplier;
	}

	public static String msg(String code, @Nullable Object[] args, @Nullable String defaultMessage) {
		return ms().getMessage(code, args, defaultMessage, localeSupplier.get());
	}

	public static String msg(String code, Object... args) {
		return ms().getMessage(code, args, localeSupplier.get());
	}

	public static String msg(String code, Locale locale) {
		return ms().getMessage(code, null, locale);
	}

	public static String msg(String code, Locale locale, Object... args) {
		return ms().getMessage(code, args, locale);
	}

	public static String msg(String code) {
		return msg(code, (Object[]) null);
	}

	public static void assertTrue(boolean result) {
		if (!result) {
			throw new IllegalArgumentException();
		}
	}

	public static void assertTrue(boolean result, String msgCode) {
		if (!result) {
			throw new IllegalArgumentException(msg(msgCode));
		}
	}

	public static void assertNotNull(Object obj, String msgCode) {
		if (obj == null) {
			throw new IllegalArgumentException(msg(msgCode));
		}
	}

	public static void assertNotNull(Object obj) {
		if (obj == null) {
			throw new IllegalArgumentException();
		}
	}

	public static void assertFalse(boolean result, String msgCode) {
		if (result) {
			throw new IllegalArgumentException(msg(msgCode));
		}
	}

}
