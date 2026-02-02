package com.bojiu.context.auth;

import java.io.IOException;
import javax.servlet.http.*;

import com.bojiu.common.context.*;
import com.bojiu.common.model.ErrorMessageException;
import com.bojiu.common.model.Messager;
import com.bojiu.common.redis.RedisUtil;
import com.bojiu.common.util.SpringUtil;
import com.bojiu.common.web.Ready;
import com.bojiu.common.web.ServletUtil;
import com.bojiu.common.web.mvc.ActuatorFilter;
import com.bojiu.context.SystemInitializer;
import com.bojiu.context.i18n.UserI18nKey;
import com.bojiu.context.model.*;
import com.bojiu.context.web.ProxyRequest;
import com.bojiu.context.web.RequestContextImpl;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.NumberUtil;
import me.codeplayer.util.X;
import org.apache.commons.lang3.BooleanUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户会话鉴权 及 权限控制 拦截器
 */
@Slf4j
public class PermissionInterceptor implements HandlerInterceptor {

	public static final String _csrfToken = "7ca1703cad3cc0b0a78bb0dab271b342";

	/** 权限定位符数组在 request 中的 KEY 值 */
	public static final String PERMISSION_LOCATOR_KEY = "permissionLocator";
	//
	protected final PermissionPolicy permissionPolicy;
	@Nullable
	protected AutoLoginHandler autoLoginHandler;

	public PermissionInterceptor(PermissionPolicy permissionPolicy, @Nullable AutoLoginHandler autoLoginHandler) {
		this.permissionPolicy = permissionPolicy;
		this.autoLoginHandler = autoLoginHandler;
	}

	public PermissionInterceptor(@Nullable AutoLoginHandler autoLoginHandler) {
		this(new DefaultPermissionPolicy(), autoLoginHandler);
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
		if (handler instanceof HandlerMethod handlerMethod) {
			if (MemberType.fromBackstage() && handlerMethod.getBeanType().getPackageName().startsWith("com.bojiu.webapp.work.")) {
				request.setAttribute(RequestContextImpl.ATTR_FOR_WORK, Boolean.TRUE);
			}
			/* TODO 等前端适配完成并上线后，再启用 CSRF 攻击防御
			if (directDenyAccess(request, response)) {
				return false;
			}
			*/
			if (BooleanUtils.TRUE.equals(request.getAttribute(ActuatorFilter.ATTR_SKIP_CHECK))) {
				return true;
			}
			final Ready ready = handlerMethod.getMethodAnnotation(Ready.class);
			if (ready != null) { // 初始化时区设置
				DefaultPermissionPolicy.setTitle(request, ready.value());
				if (ready.defaultTimezone()) {
					request.setAttribute(RequestContextImpl.ATTR_TIME_ZONE, SystemInitializer.DEFAULT_TIME_ZONE);
				}
			}

			Member member = getSessionMember(request, response);

			// 非正式环境，允许传入 $debug=用户ID 参数进行快速调试
			if (member == null && !Env.inProduction()) {
				Long memberId = NumberUtil.getLong(request.getParameter("$debug"), null);
				if (memberId != null && autoLoginHandler instanceof DefaultAutoLoginHandler impl) {
					member = impl.load(memberId);
					request.getSession().setAttribute(RequestContextImpl.SESSION_USER, member);
				}
			}

			// 只有总台允许传入 merchantId 参数
			// 如果第三方回调等特殊情况需要传入，请在方法上添加 @WithoutMerchant 注解
			/*
			if ((member == null || !member.asAdmin())
					&& StringUtil.notEmpty(request.getParameter("merchantId"))
					&& !handlerMethod.hasMethodAnnotation(WithoutMerchant.class)) {
				throw new ErrorMessageException("Illegal parameter").report();
			}
			*/

			final PermissionLocator locator = permissionPolicy.parse(request, handlerMethod.getBeanType(), handlerMethod.getMethod());
			Long merchantId = null;
			if (locator != null) { // 如果必须登录才能访问
				// 如果用户未登录则检测是否需要登录权限；如果该用户是普通用户，则检测是否需要管理员权限
				// 如果没有登录用户 或者 登录用户不属于当前商户站点，直接跳转登录界面
				boolean deny = member == null
						|| !member.canAccess(merchantId = RequestContextImpl.getMerchantId(request));
				if (deny && merchantId != null && ready != null && !ready.merchantIdRequired()) { // 可查询所有站点时候、接口内部做了范围查询，可放行所属商户
					deny = !member.getMerchantId().equals(merchantId);
				}

				if (deny) {
					final boolean userOrAdmin = !MemberType.fromBackstage();
					String loginURL = Context.nav().getLoginURL(userOrAdmin);
					final boolean acceptJSON = ServletUtil.isAcceptJSON(request);
					// loginURL = encodeLoginURL(loginURL, request, !acceptJSON);
					response(request, response, acceptJSON, loginURL);
					return false;
				}

				// 检查用户状态是否异常
				checkMemberState(member, request);

				boolean allow = checkMemberType(member.type(), locator.memberType) && member.hasPermission(locator.getPermissionCode());
				if (!allow) {
					throw new PermissionException();
				}
				request.setAttribute(PERMISSION_LOCATOR_KEY, locator);

				// 检查后台是否必传 merchantId 参数
				if (MemberType.fromBackstage() && member.intoMerchant() && (ready == null || ready.merchantIdRequired())) {
					merchantId = RequestContextImpl.getSelectedMerchantId(request);
					if (merchantId == null) {
						throw new ErrorMessageException("检测到关键参数缺失，建议您刷新页面或重新登录再试！", false);
					}
				}
			} else if (member != null && !member.canAccess(merchantId = RequestContextImpl.doGetMerchantId(request))) {
				// 即使不需要用户必须登录，但是如果前台用户进行了跨站点访问，则直接将会话置为无效，一般只有测试环境会出现这种情况
				log.warn("检测到跨站点访问：用户={}，商户={}，目标={}", member.getId(), member.getMerchantId(), merchantId);
				if (!member.getMerchantId().equals(merchantId)) {
					request.getSession().removeAttribute(RequestContextImpl.SESSION_USER);
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
		Member member = (Member) session.getAttribute(RequestContext.SESSION_USER);
		if (member == null || autoLoginHandler == null || member.asEmp()) {
			return;
		}
		// 修正 初次请求并发时，多次 Session Id 分发不一致的问题
		final String sessionId = session.getId();
		if (sessionId.equals(member.getSessionId())) {
			return;
		}
		final String requestedSessionId = DefaultAutoLoginHandler.normalizeSessionId(request.getRequestedSessionId());
		if (sessionId.equals(requestedSessionId)) {
			log.warn("用户{} 的会话ID发生变化：{} => {}", member.getId(), member.getSessionId(), sessionId);
			member.setSessionId(sessionId);
			request.getSession().setAttribute(RequestContext.SESSION_USER, member);
			// 异步处理
			SpringUtil.asyncRun(() -> autoLoginHandler.updateSessionId(sessionId, member.getId()), "更新sessionId出错");
		}
	}

	protected void checkMemberState(@Nullable Member member, HttpServletRequest request) throws ErrorMessageException {
		if (member != null) {
			if (!member.valid()) { // 判断用户是否已经被冻结
				throw new ErrorMessageException(I18N.msg(UserI18nKey.USER_AUTO_LOGIN_FROZEN), false);
			}
			if (member.asEmp() && !member.asAdmin()) {
				if (RedisUtil.opsForSet().remove("empForceLogin", member.getId().toString()) > 0) {
					throw new ErrorMessageException(I18N.msg(UserI18nKey.PERMISSION_CHANGE_LOGIN_INVALID), MessagerStatus.LOGIN, false);
				}
				if (Boolean.TRUE.equals(RedisUtil.opsForSet().isMember(RedisKey.FROZEN_MERCHANT_LIST, member.getMerchantId().toString()))) {
					throw new ErrorMessageException(I18N.msg(UserI18nKey.MERCHANT_FROZEN), false);  // 限制后台冻结商户操作
				}
			}
			if (member.asVisitor() && "POST".equals(request.getMethod()) && !request.getRequestURI().startsWith("/emp/")) {
				// 跳过指定的URI
				throw new ErrorMessageException(I18N.msg(UserI18nKey.UNSUPPORTED_OPERATIONS), false);
			}
		}
	}

	protected void response(HttpServletRequest request, HttpServletResponse response, boolean acceptJSON, String loginURL) throws IOException {
		if (acceptJSON) {
			// 如果是 AJAX 提交，响应 JSON
			Messager<Void> m = new Messager<>(MessagerStatus.LOGIN, I18N.msg(UserI18nKey.USER_AUTO_LOGIN_INVALID), loginURL);
			ProxyRequest.writeJSON(response, m, request);
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
		if (PermissionInterceptor.tryDenyCsrf(request) && isExternalAccess(request)) {
			final String json = """
					{"status":"403","msg":"Access denied"}
					""";
			ProxyRequest.writeToResponse(response, json, MediaType.APPLICATION_JSON_VALUE, request);
			return true;
		}
		return false;
	}

	public static boolean isExternalAccess(HttpServletRequest request) {
		return !_csrfToken.equals(request.getParameter("_csrf"));
	}

	@Nullable
	protected Member getSessionMember(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession(false);
		Member member = null;
		if (session != null) {
			member = (Member) session.getAttribute(RequestContext.SESSION_USER);
		}
		if (member == null && autoLoginHandler != null && shouldTryCookieLogin(session)) {
			member = autoLoginHandler.tryAutoLogin(request, response);
		}
		return member;
	}

	protected boolean shouldTryCookieLogin(@Nullable HttpSession session) {
		return session == null || session.isNew() || session.getLastAccessedTime() - session.getCreationTime() < 3000L; // 会话刚刚创建的 3s 内
	}

	/**
	 * 检测 CSRF 攻击
	 *
	 * @return 返回 true 表示检测到 CSRF 攻击
	 */
	public static boolean tryDenyCsrf(HttpServletRequest request) {
		if ("POST".equals(request.getMethod())) {
			String referer = ServletUtil.getReferer(request);
			final int refSize = X.size(referer);
			if (refSize > 0) {
				String host = request.getServerName();
				final int from = referer.indexOf("//", 4) + 2, length = host.length();
				return !referer.regionMatches(from, host, 0, length) || refSize > from + length && referer.charAt(from + length) != '/';
			}
		}
		return false;
	}

	protected boolean checkMemberType(MemberType input, MemberType expect) {
		return expect == null || input == expect || expect.ordinal() <= MemberType.USER.ordinal() || switch (expect) {
			case EMP -> input.ordinal() > expect.ordinal();
			case MERCHANT -> input == MemberType.GROUP || input == MemberType.VISITOR;
			case AGENT, DEALER -> input == MemberType.MERCHANT || input == MemberType.GROUP || input == MemberType.VISITOR;
			default -> false;
		};
	}

}