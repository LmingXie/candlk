package com.candlk.common.web.mvc;

import java.io.IOException;
import java.net.InetAddress;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.StringUtil;

@Slf4j
public class ActuatorFilter implements Filter {

	public static final String URL_PATTERN = "/actuator/*";

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		// 由于 dev 环境无法获取外网IP，因此通过 "X-Forwarded-For" 请求头 进行兼容性判断
		String ip = request.getHeader("X-Forwarded-For");
		boolean fromLAN = StringUtil.isEmpty(ip);
		if (!fromLAN) {
			ip = request.getRemoteAddr();
			InetAddress ipAddr = null;
			try {
				ipAddr = InetAddress.getByName(ip);
			} catch (Exception e) {
				log.error("解析IP地址时出错：", e);
			}
			fromLAN = ipAddr != null && (ipAddr.isSiteLocalAddress() || ipAddr.isLoopbackAddress());
		}
		if (fromLAN) {
			filterChain.doFilter(servletRequest, servletResponse);
		}
	}

}
