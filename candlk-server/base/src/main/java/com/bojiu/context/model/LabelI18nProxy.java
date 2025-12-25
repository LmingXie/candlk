package com.bojiu.context.model;

import java.util.Locale;

import com.bojiu.common.context.I18N;
import com.bojiu.common.model.ValueProxyImpl;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public interface LabelI18nProxy<E extends Enum<E>, V extends java.io.Serializable> extends ValueProxyImpl<E, V> {

	@Override
	default String getLabel() {
		return I18N.msg(getProxy().getLabel());
	}

	default String getLabel(@NonNull Language language) {
		return I18N.msg(getProxy().getLabel(), language.locale);
	}

	default String getLabel(@NonNull Locale locale) {
		return I18N.msg(getProxy().getLabel(), locale);
	}

	static String label(@Nullable LabelI18nProxy<?, ?> t, @NonNull Language language) {
		return t == null ? null : t.getLabel(language);
	}

}