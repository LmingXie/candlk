package com.bojiu.context.auth;

import java.lang.reflect.Method;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bojiu.common.context.I18N;
import com.bojiu.common.model.Messager;
import com.bojiu.common.web.ServletUtil;
import com.bojiu.context.model.BaseI18nKey;
import com.bojiu.context.model.MessagerStatus;
import com.bojiu.context.web.ProxyRequest;
import com.bojiu.webapp.base.util.Export;
import me.codeplayer.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 异步导出
 *
 * @author LiuRong
 * @since 2024-07-29
 */
public class AsyncExportInterceptor implements HandlerInterceptor {

	static final String EXPORT_CONTEXT_ATTR = "$exportContext";

	public static AsyncExportContext getContext(HttpServletRequest request) {
		return (AsyncExportContext) request.getAttribute(EXPORT_CONTEXT_ATTR);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (handler instanceof HandlerMethod handlerMethod && ExportInterceptor.isExport(request)) {
			Method method = handlerMethod.getMethod();
			final Export export = method.getAnnotation(Export.class);
			if (export != null && export.async()) {
				PermissionLocator locator = (PermissionLocator) request.getAttribute(PermissionInterceptor.PERMISSION_LOCATOR_KEY);
				final AsyncExportContext ctx = new AsyncExportContext();
				ctx.export = export;
				ctx.permissionCode = StringUtils.removeEnd(locator.getPermissionCode(), "-export");
				request.setAttribute(EXPORT_CONTEXT_ATTR, ctx);
				// 获取前端的页面路径，用于查询菜单ID
				String referer = ServletUtil.getReferer(request);
				if (StringUtil.notEmpty(referer)) {
					URL url = new URL(referer);
					String path = url.getPath();
					ctx.frontPath = StringUtil.notEmpty(url.getQuery()) ? path + "?" + url.getQuery() : path;
				}
			}
		}
		return true;
	}

	public static void response(HttpServletResponse response, HttpServletRequest request) {
		Messager<String> msger = Messager.status(MessagerStatus.ASYNC_EXPORT, I18N.msg(BaseI18nKey.EXPORT_SUCCESS));
		ProxyRequest.writeJSON(response, msger.setCode(0), request);
	}

}