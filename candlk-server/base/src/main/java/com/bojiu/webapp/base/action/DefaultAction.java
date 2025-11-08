package com.bojiu.webapp.base.action;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson2.JSONObject;
import com.bojiu.common.context.Context;
import com.bojiu.common.context.Env;
import com.bojiu.common.model.Messager;
import com.bojiu.context.ContextImpl;
import com.bojiu.context.auth.Permission;
import com.bojiu.context.auth.WithoutMerchant;
import com.bojiu.context.model.MemberType;
import com.bojiu.context.web.Jsons;
import com.bojiu.context.web.ProxyRequest;
import me.codeplayer.util.JavaUtil;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DefaultAction extends BaseAction {

	static byte[] cache;

	@RequestMapping("/")
	@WithoutMerchant
	@Permission(Permission.NONE)
	public void index(HttpServletResponse response) throws IOException {
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		if (cache == null) {
			String appName = Context.applicationName() + "-" + Context.getEnv().getProperty("spring.profiles.active");
			cache = JavaUtil.getUtf8Bytes(Jsons.encodeRaw(Messager.exposeData(appName)));
		}
		response.getOutputStream().write(cache);
	}

	@RequestMapping("/health")
	@WithoutMerchant
	@Permission(Permission.NONE)
	public void health(HttpServletResponse response) throws IOException {
		index(response);
	}

	/**
	 * /api/user/trace 回显客户端与服务端IP
	 * /api/user/trace?headers=1 回显客户端与服务端IP+请求头
	 */
	@RequestMapping("/trace")
	@WithoutMerchant
	@Permission(Permission.NONE)
	public void trace(ProxyRequest q, HttpServletResponse response) {
		final String ip = q.getIP();
		Object nodeIp = ContextImpl.getNodeIP();
		final boolean canExpose = Env.inner() || MemberType.fromBackstage() && q.user() != null;
		JSONObject headers = null;
		HttpServletRequest request = q.getRequest();
		if (canExpose) {
			if (q.getInt("headers", 0) == 1) { // 回显请求头
				headers = new JSONObject();
				final Enumeration<String> names = request.getHeaderNames();
				while (names.hasMoreElements()) {
					String name = names.nextElement();
					headers.put(name, request.getHeader(name));
				}
			}
		} else {
			nodeIp = Objects.hashCode(nodeIp);
		}
		JSONObject object = JSONObject.of("clientIp", ip, "nodeIp", nodeIp, "headers", headers);
		ProxyRequest.writeJSON(response, object, request);
	}

}