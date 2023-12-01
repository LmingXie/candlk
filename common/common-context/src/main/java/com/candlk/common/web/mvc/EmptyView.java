package com.candlk.common.web.mvc;

import java.util.*;

import javax.annotation.*;
import javax.servlet.http.*;

import org.springframework.web.servlet.*;

public class EmptyView implements View {

	public static final View INSTANCE = new EmptyView();
	public static final String VIEW_NAME = "null";

	@Override
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) {
	}

	public static void apply(@Nullable ModelAndView mav) {
		if (mav != null) {
			mav.setViewName(null);
			mav.setView(INSTANCE);
		}
	}

}
