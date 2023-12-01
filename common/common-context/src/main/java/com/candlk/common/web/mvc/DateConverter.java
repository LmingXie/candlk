package com.candlk.common.web.mvc;

import java.util.Date;

import com.candlk.common.context.Context;
import org.springframework.core.convert.converter.Converter;

public class DateConverter implements Converter<String, Date> {

	public static Date convertToDate(String source, final boolean errorAsNull) {
		return Context.get().smartParseDate(source, errorAsNull);
	}

	@Override
	public Date convert(String source) {
		return convertToDate(source, false);
	}

}
