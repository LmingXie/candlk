package com.candlk.common.web.mvc;

import javax.servlet.http.*;

import org.springframework.web.servlet.*;

public class NullViewNameTranslator implements RequestToViewNameTranslator {

	@Override
	public String getViewName(HttpServletRequest request) {
		return null;
	}
}
