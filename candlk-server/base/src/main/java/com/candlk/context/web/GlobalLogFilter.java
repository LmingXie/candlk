package com.candlk.context.web;

import java.io.IOException;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson2.JSONObject;
import com.candlk.common.context.RequestContext;
import com.candlk.common.model.Bean;
import com.candlk.common.web.Logs;
import com.candlk.common.web.ServletUtil;
import com.candlk.context.model.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 全局的请求日志过滤器
 */
@Slf4j
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

		try {
			chain.doFilter(req, resp);
		} finally {
			// 请求耗时
			final long endTime = System.currentTimeMillis();

			if (user == null) {
				user = RequestContext.getSessionUser(request);
			}

			// 请求参数
			int paramLength = (request.getContentLength() + 32) / 2;
			boolean notFirst = false;

			final StringBuilder params = new StringBuilder(Math.max(paramLength, 64));
			for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
				String name = entry.getKey();
				if ("password".equals(name)) {
					continue;
				}
				String[] values = entry.getValue();
				if (notFirst) {
					params.append('\n');
				} else {
					notFirst = true;
				}
				params.append(name).append('=');
				if (values.length == 1) {
					params.append(values[0]);
				} else {
					params.append('[');
					for (int i = 0; i < values.length; i++) {
						if (i > 0) {
							params.append(", ");
						}
						params.append(values[i]);
					}
					params.append(']');

				}
			}
			final String paramStr;
			if (params.isEmpty()) {
				final Object body = request.getAttribute(Logs.REQUEST_BODY);
				paramStr = body == null ? "" : (String) body;
			} else {
				paramStr = params.toString();
			}

			// 请求头
			final Enumeration<String> headerNames = request.getHeaderNames();
			final JSONObject headersJson = new JSONObject();
			while (headerNames.hasMoreElements()) {
				String name = headerNames.nextElement();
				headersJson.put(name, request.getHeader(name));
			}

			final Object retVal = request.getAttribute(Logs.RESPONSE);
			final Throwable e = (Throwable) request.getAttribute(Logs.EXCEPTION);

			final long useTimeMs = endTime - startTime;
			Logs.logJSON(Bean.idOf(user), appName, request.getMethod(), uri, headersJson, paramStr, ServletUtil.getClientIP(request), new Date(startTime), useTimeMs, retVal, e);
		}
	}

}
