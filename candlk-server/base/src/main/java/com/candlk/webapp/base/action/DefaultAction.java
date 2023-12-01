package com.candlk.webapp.base.action;

import javax.servlet.http.HttpServletResponse;

import com.candlk.common.context.Context;
import com.candlk.common.model.Messager;
import com.candlk.context.web.Jsons;
import com.candlk.context.web.ProxyRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DefaultAction extends BaseAction {

	static String[] cache;

	static String[] cache() {
		if (cache == null) {
			cache = new String[] {
					Jsons.encode(Messager.OK(Context.applicationName() + "-" + Context.getEnv().getProperty("spring.profiles.active"))),
					Jsons.encode(new Messager<String>().setOK().setCode(200))
			};
		}
		return cache;
	}

	@GetMapping("/")
	public void index(HttpServletResponse response) {
		ProxyRequest.writeToResponse(response, cache()[0], MediaType.APPLICATION_JSON_VALUE);
	}

	@RequestMapping("/health")
	public void health(HttpServletResponse response) {
		ProxyRequest.writeToResponse(response, cache()[1], MediaType.APPLICATION_JSON_VALUE);
	}

}
