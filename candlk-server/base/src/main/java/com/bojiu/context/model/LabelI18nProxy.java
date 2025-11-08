package com.bojiu.context.model;

import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.bojiu.common.context.I18N;
import com.bojiu.common.model.ValueProxyImpl;

public interface LabelI18nProxy<E extends Enum<E>, V extends java.io.Serializable> extends ValueProxyImpl<E, V> {

	@Override
	default String getLabel() {
		return I18N.msg(getProxy().getLabel());
	}

	default String getLabel(@Nonnull Language language) {
		return I18N.msg(getProxy().getLabel(), language.locale);
	}

	default String getLabel(@Nonnull Locale locale) {
		return I18N.msg(getProxy().getLabel(), locale);
	}

	static String label(@Nullable LabelI18nProxy<?, ?> t, @Nonnull Language language) {
		return t == null ? null : t.getLabel(language);
	}

}