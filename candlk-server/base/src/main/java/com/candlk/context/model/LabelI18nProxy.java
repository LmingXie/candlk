package com.candlk.context.model;

import javax.annotation.Nonnull;

import com.candlk.common.context.I18N;
import com.candlk.common.model.ValueProxyImpl;

public interface LabelI18nProxy<E extends Enum<E>, V> extends ValueProxyImpl<E, V> {

	@Override
	default String getLabel() {
		return I18N.msg(getProxy().getLabel());
	}

	default String getLabel(@Nonnull Language language) {
		return I18N.msg(getProxy().getLabel(), language.locale);
	}

}
