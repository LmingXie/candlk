package com.candlk.common.web.mvc;

import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson2.JSON;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.servlet.view.AbstractView;

@Getter
@Setter
public class FastJsonView extends AbstractView {

	public static final FastJsonView INSTANCE = new FastJsonView();

	public static final String JSON_KEY = "JSON";

	@Override
	protected void renderMergedOutputModel(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		Object value = model.get(JSON_KEY);

		ServletOutputStream out = response.getOutputStream();
		JSON.writeTo(out, value, "millis", null);

		out.flush();
	}

}
