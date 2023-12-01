package com.candlk.common.context;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

public interface ContextConfigurer extends Hook, RequestUriResolver, ExternalConfigProxy {

	default void preHandle() {
	}

	default ApplicationContext getApplicationContext() {
		ServletContext sc = Context.getServletContext();
		return sc == null ? null : (ApplicationContext) sc.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
	}

	default void servletContextInitialized(ServletContext sc) {
		Context.setGlobalServletContext(sc);
	}

	default void requestInitialized(HttpServletRequest request, boolean isFirst) {
		if (isFirst) {
			Context.nav().initWithRequest(request);
		}
	}

	default boolean isSystemSuperAdmin(Number id) {
		return id != null && id.longValue() == Context.internal().getSystemSuperAdminId();
	}

	default HttpServletRequest currentRequest() {
		return RequestContext.get().getRequest();
	}

	default void postHandle() {

	}

}
