package com.candlk.context.auth;

import java.io.IOException;
import javax.annotation.Nullable;
import javax.servlet.http.*;

import com.candlk.common.context.*;
import com.candlk.common.model.ErrorMessageException;
import com.candlk.common.model.Messager;
import com.candlk.common.util.SegmentLock;
import com.candlk.common.util.SpringUtil;
import com.candlk.common.web.ServletUtil;
import com.candlk.context.model.*;
import com.candlk.context.web.ProxyRequest;
import com.candlk.context.web.RequestContextImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class UserSessionInterceptor implements HandlerInterceptor {

	@Nullable
	protected AutoLoginHandler autoLoginHandler;

	public UserSessionInterceptor(@Nullable AutoLoginHandler autoLoginHandler) {
		this.autoLoginHandler = autoLoginHandler;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		if (handler instanceof HandlerMethod handlerMethod) {
			if (directDenyAccess(request, response)) {
				return false;
			}
			final Member sessionUser = getLoginUser(request, response);
			Permission p = PermissionPolicy.findPermission(handlerMethod.getMethod(), handlerMethod.getBeanType());
			// 方法上有权限注解
			if (p != null) {
				checkUserState(sessionUser);
				// 如果用户未登录则检测是否需要登录权限；如果该用户是普通用户，则检测是否需要管理员权限
				String redirectLoginURL = getRedirectLoginURL(p, sessionUser, request);
				if (redirectLoginURL != null) {
					final boolean acceptJSON = ServletUtil.isAcceptJSON(request);
					String loginURL = encodeLoginURL(redirectLoginURL, request, !acceptJSON);
					response(request, response, acceptJSON, loginURL);
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		final HttpSession session = request.getSession(false);
		if (session == null) {
			return;
		}
		Member sessionUser = (Member) session.getAttribute(RequestContext.SESSION_USER);
		// 修正 初次请求并发时，多次 Session Id 分发不一致的问题
		if (sessionUser != null && !sessionUser.asEmp() && autoLoginHandler != null) {
			final String sessionId = session.getId();
			if (!sessionId.equals(sessionUser.getSessionId()) && sessionId.equals(request.getRequestedSessionId())) {
				log.warn("用户{} 的会话ID发生变化：{} => {}", sessionUser.getId(), sessionUser.getSessionId(), sessionId);
				sessionUser.setSessionId(sessionId);
				request.getSession().setAttribute(RequestContext.SESSION_USER, sessionUser);
				// 异步处理
				SpringUtil.asyncRun(() -> autoLoginHandler.updateSessionId(sessionId, sessionUser.getId()), "更新sessionId出错");
			}
		}
	}

	protected void checkUserState(@Nullable Member sessionUser) throws ErrorMessageException {
		// 判断用户是否已经被冻结
		if (sessionUser != null) {
			if (!sessionUser.isValid()) {
				throw new ErrorMessageException(I18N.msg(UserI18nKey.INVALID_USER));
			}
		}
	}

	protected void response(HttpServletRequest request, HttpServletResponse response, boolean acceptJSON, String loginURL) throws IOException {
		if (acceptJSON) {
			// 如果是 AJAX 提交，响应 JSON
			Messager<Void> m = new Messager<>(MessagerStatus.LOGIN, null, loginURL);
			ProxyRequest.writeJSON(response, m);
		} else {
			// 直接重定向
			response.setStatus(302);
			response.sendRedirect(loginURL);
		}
	}

	/**
	 * @param sendRedirect 响应重定向
	 */
	protected String encodeLoginURL(String redirectLoginURL, HttpServletRequest request, boolean sendRedirect) {
		return redirectLoginURL;
		/*
		String encodedSource = "true";
		// 直接重定向请求时，部分场景需要预先附带好来源信息
		if (sendRedirect && ("GET".equals(request.getMethod()) || !Context.get().isSafeReferer(request, false))) {
			// Referer 不安全或为空时，通过JS跳转后，可能无法跳转回来
			String queryString = request.getQueryString();
			String from = StringUtil.concat(request.getRequestURI(), StringUtil.notEmpty(queryString) ? "?" : null, queryString);
			encodedSource = ServletUtil.encodeURL(from);
		}
		return StringUtil.concats(redirectLoginURL, "?", sourceParamName, "=", encodedSource);
		*/

	}

	/** 是否直接拒绝访问 */
	protected boolean directDenyAccess(HttpServletRequest request, HttpServletResponse response) throws IOException {
		/*
		String token = AdminTokenUtil.getValidToken(request, response);
		if (token == null) {
			response.sendError(404, request.getRequestURI());
			return true;
		}
		*/
		return false;
	}

	protected String getRedirectLoginURL(Permission p, @Nullable Member sessionUser, HttpServletRequest request) {
		// 如果没有登录用户 或者 登录用户不属于当前商户站点，直接跳转登录界面
		boolean deny = sessionUser == null || !sessionUser.getMerchantId().equals(RequestContextImpl.getMerchantId(request));
		if (deny) {
			final boolean userOrAdmin = Permission.USER.equals(p.value());
			return Context.nav().getLoginURL(userOrAdmin);
		}
		return null;
	}

	@Nullable
	protected Member getLoginUser(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession();
		Member user = null;
		if (session != null) {
			user = (Member) session.getAttribute(RequestContext.SESSION_USER);
		}
		if (user == null && autoLoginHandler != null && shouldTryCookieLogin(session)) {
			// 防止并发登录请求
			synchronized (SegmentLock.get("login", ServletUtil.getClientIP(request))) {
				user = RequestContext.getSessionUser(request);
				if (user == null) {
					user = autoLoginHandler.tryAutoLogin(request, response);
				}
			}
		}
		return user;
	}

	protected boolean shouldTryCookieLogin(@Nullable HttpSession session) {
		return session == null || session.isNew()
		       || session.getLastAccessedTime() - session.getCreationTime() < 3000L; // 会话刚刚创建的 3s 内
	}

}
