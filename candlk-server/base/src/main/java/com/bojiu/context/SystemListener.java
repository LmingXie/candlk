package com.bojiu.context;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;

import com.bojiu.common.context.Context;
import com.bojiu.common.context.RequestContext;
import com.bojiu.context.web.SessionCookieUtil;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.stereotype.Component;

@Component
public class SystemListener implements ServletRequestListener, ServletContextInitializer {

	/** 是否已经初始化过第一个请求 */
	protected static final AtomicBoolean noRequestInitialized = new AtomicBoolean(true);

	/**
	 * 在系统启动时，初始化
	 */
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException {
		startup(servletContext, this);
	}

	public static void startup(ServletContext servletContext, SystemListener listener) {
		SystemInitializer.updateLog4j2Config();
		if (Context.getServletContext() == null) {
			Context.setGlobalServletContext(servletContext);
			Context.get().servletContextInitialized(servletContext);
		}
		startupWebComponents(servletContext, listener);
	}

	public static void startupWebComponents(ServletContext servletContext, @Nullable SystemListener listener) {
		System.out.println("【应用正在启动】：rootURI=[" + servletContext.getContextPath() + "] => rootPath=[" + servletContext.getRealPath("/") + "]");
		SessionCookieUtil.configureSessionCookie(servletContext);
		if (listener != null) {
			servletContext.addListener(listener);
		}
	}

	/**
	 * 为了避免项目部署以后必须要点击一次首页的问题。这里把部分常量的初始化放在 ServletRequest 监听器中
	 */
	@Override
	public void requestInitialized(ServletRequestEvent event) {
		boolean isFirst = noRequestInitialized.get();
		if (isFirst) {
			isFirst = noRequestInitialized.compareAndSet(true, false);
		}
		Context.get().requestInitialized((HttpServletRequest) event.getServletRequest(), isFirst);
	}

	@Override
	public void requestDestroyed(ServletRequestEvent event) {
		RequestContext.destroy(); // 去除对 request / response 的引用依赖，避免 memory leak
	}

}
