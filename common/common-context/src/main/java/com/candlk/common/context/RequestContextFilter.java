package com.candlk.common.context;

import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RequestContextFilter implements Filter {

	private static Function<HttpServletRequest, HttpServletRequest> requestWrapper = Function.identity();
	private static Function<HttpServletResponse, HttpServletResponse> responseWrapper = Function.identity();

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = requestWrapper.apply((HttpServletRequest) request);
		HttpServletResponse resp = responseWrapper.apply((HttpServletResponse) response);
		RequestContext.reuseContext(req, resp, true);
		chain.doFilter(req, resp);
		if (resp.getStatus() != 200) {
			log.info("异常请求：状态={}，路径={}", resp.getStatus(), req.getRequestURI());
		}
	}

	public static void setRequestWrapper(Function<HttpServletRequest, HttpServletRequest> requestWrapper) {
		RequestContextFilter.requestWrapper = Objects.requireNonNull(requestWrapper);
	}

	public static void setResponseWrapper(Function<HttpServletResponse, HttpServletResponse> responseWrapper) {
		RequestContextFilter.responseWrapper = Objects.requireNonNull(responseWrapper);
	}

}
