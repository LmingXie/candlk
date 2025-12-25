package com.bojiu.context.web;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson2.JSONObject;
import com.bojiu.common.context.RequestContext;
import com.bojiu.common.validator.FormValidator;
import com.bojiu.common.web.Logs;
import com.bojiu.common.web.ServletUtil;
import com.bojiu.context.auth.PermissionInterceptor;
import com.bojiu.context.model.Member;
import me.codeplayer.util.JavaUtil;
import me.codeplayer.util.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 全局的请求日志过滤器
 */
@Component
public class GlobalLogFilter implements Filter {

	@Value("${spring.application.name}-${spring.profiles.active}")
	protected String appName;

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		final long startTime = System.currentTimeMillis();
		final HttpServletRequest request = (HttpServletRequest) req;
		final String uri = request.getRequestURI();

		final boolean logout = uri.endsWith("/logout"); // 退出登录时，要提取获取 userId
		Member user = logout ? RequestContext.getSessionUser(request) : null;

		final Logs.LogContext context = Logs.getContext(request);
		context.init(request); // 初始化

		try {
			chain.doFilter(req, resp);
		} finally {
			// 请求耗时
			final long endTime = System.currentTimeMillis();

			if (user == null) {
				user = RequestContext.getSessionUser(request);
			}

			// 请求参数
			String requestBody = context.requestBody;
			if (requestBody != null) {
				// 如果 JSON 请求也附带了 queryString，则将 queryString 也添加到 requestBody 中
				String queryString = request.getQueryString();
				if (StringUtil.notEmpty(queryString)) {
					requestBody = queryString + "\n" + requestBody;
				}
			}
			if (StringUtil.isEmpty(requestBody)) {
				final Map<String, String[]> map = request.getParameterMap();
				if (map.isEmpty()) {
					requestBody = "";
				} else {
					Logs.ParamAppender appender = context.paramAppender;
					if (appender == null) {
						appender = Logs.ParamAppender.DEFAULT;
					}
					boolean notFirst = false;
					final StringBuilder params = context.sb;
					for (Map.Entry<String, String[]> entry : map.entrySet()) {
						String name = entry.getKey();
						if (FormValidator.shouldSkip(name)) {
							continue;
						}
						String[] values = entry.getValue();
						if (notFirst) {
							params.append('\n');
						} else {
							notFirst = true;
						}
						appender.append(name, values, params, request);
					}
					requestBody = params.toString();
					// 如果检测到 非 Latin1 字符，就丢弃该 StringBuilder
					if (JavaUtil.STRING_CODER.applyAsInt(requestBody) != JavaUtil.LATIN1) {
						context.sb = new StringBuilder(Logs.getInitCapacity());
					}
				}
			}

			// 请求头
			final Enumeration<String> headerNames = request.getHeaderNames();
			final JSONObject headersJson = new JSONObject();
			while (headerNames.hasMoreElements()) {
				String name = headerNames.nextElement();
				// sec-ch-ua = "Google Chrome";v="123", "Not:A-Brand";v="8", "Chromium";v="123"
				// sec-ch-ua-mobile = ?1
				// sec-ch-ua-platform = "Android"
				// sec-fetch-dest = empty
				// sec-fetch-mode = cors
				// sec-fetch-site = same-origin
				// 【保留】 x-forwarded-for = 170.79.80.61
				// x-forwarded-host = 771266.com
				// x-forwarded-port = 80
				// x-forwarded-proto = http
				if (name.startsWith("sec-") || name.startsWith("x-forwarded-") && !"x-forwarded-for".equals(name)) {
					continue;
				}
				if (switch (name) {
					case "x-amzn-trace-id" -> true; // AWS 负载均衡器跟踪 ID
					case "cf-ray" -> true; // CF 跟踪ID
					case "cf-visitor" -> true; // 固定值 {"scheme":"https"}
					case "prefer", "dnt", "priority" -> true;
					default -> false;
				}) {
					continue;
				}
				headersJson.put(name, request.getHeader(name));
			}

			if (PermissionInterceptor.tryDenyCsrf(request)) {
				context.addExtItem("csrf"); // 记录疑似 CSRF 的攻击
			}
			Object ext = context.ext;
			if (ext != null) {
				if (ext instanceof String[] array) {
					ext = String.join(",", array);
				} else if (ext instanceof Object[] array) {
					ext = Arrays.toString(array);
				} else {
					ext = ext.toString();
				}
			}
			final long useTimeMs = endTime - startTime;
			Logs.logJSON(user == null ? null : user.getMerchantId(), user == null ? null : user.getId(), appName, request.getMethod(),
					uri, headersJson, requestBody, ServletUtil.getClientIP(request), startTime, useTimeMs, context.response, context.exception, ext);
			context.cleanUp(); // 尽早清理，避免请求线程空闲时持有对象，妨碍垃圾回收
		}
	}

	/**
	 * 基于引用传递的原理修改底层 request.getParameterMap() 指定 key/name 的值
	 * <b>注意</b>：修改后，并不会影响 request.getParameter( name ) 的返回值（仍然返回原始值）
	 *
	 * @return 如果替换成功，则返回 true
	 */
	public static boolean replaceParameterMapValue(HttpServletRequest request, String name, String value) {
		for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
			if (name.equals(entry.getKey())) {
				entry.getValue()[0] = value;
				return true;
			}
		}
		return false;
	}

}