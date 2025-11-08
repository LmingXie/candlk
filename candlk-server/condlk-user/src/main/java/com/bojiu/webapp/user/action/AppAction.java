package com.bojiu.webapp.user.action;

import com.alibaba.fastjson2.JSONObject;
import com.bojiu.common.model.Messager;
import com.bojiu.context.auth.Permission;
import com.bojiu.context.web.ProxyRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/app")
public class AppAction {

	@GetMapping("/layout")
	@Permission(Permission.NONE)
	public Messager<JSONObject> layout(final ProxyRequest q) {
		return Messager.exposeData(JSONObject.of("layout", "layout"));
	}

}
