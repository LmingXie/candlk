package com.candlk.context.auth;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.candlk.common.context.RequestContext;
import com.candlk.context.model.MemberType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class PermissionInterceptor implements HandlerInterceptor {

	/** 权限定位符数组在request中的KEY值 */
	public static final String PERMISSION_LOCATOR_KEY = "permissionLocator";
	//
	protected PermissionPolicy permissionPolicy = new DefaultPermissionPolicy();

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (handler instanceof HandlerMethod method) {
			/* 先关闭同源安全检测
			if (!Env.inLocal() && tryDenyCsrf(request, response)) {
				return false;
			}
			*/
			final PermissionLocator locator = permissionPolicy.parsePermission(request, method.getBeanType(), method.getMethod());
			if (locator != null) {
				final MemberRole sessionUser = RequestContext.getSessionUser(request);
				boolean allow = sessionUser != null
						&& checkMemberType(sessionUser.type(), locator.memberType)
						&& sessionUser.hasPermission(locator.getPermissionCode());
				if (!allow) {
					throw new PermissionException();
				}
				request.setAttribute(PERMISSION_LOCATOR_KEY, locator);
			}
		}
		return true;
	}

	protected boolean checkMemberType(MemberType input, @Nullable MemberType expect) {
		return expect == null || input == expect
				|| expect.ordinal() <= MemberType.USER.ordinal()
				|| (expect == MemberType.EMP && input.ordinal() > expect.ordinal());
	}

	public void setPermissionPolicy(PermissionPolicy permissionPolicy) {
		this.permissionPolicy = permissionPolicy;
	}

}
