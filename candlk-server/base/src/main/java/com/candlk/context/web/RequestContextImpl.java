package com.candlk.context.web;

import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.*;

import com.candlk.common.context.Context;
import com.candlk.common.context.RequestContext;
import com.candlk.common.web.Client;
import com.candlk.context.ContextImpl;
import com.candlk.context.SessionContext;
import com.candlk.context.model.Language;
import com.candlk.context.model.Member;
import me.codeplayer.util.NumberUtil;
import me.codeplayer.util.StringUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;

public class RequestContextImpl extends RequestContext {

	/** 用于存储 当前会话上下文 的 session key */
	public static final String SESSION_CONTEXT = "sessionContext";

	protected Session session;
	protected boolean flushRequired;
	protected Long merchantId;

	static SessionRepository<Session> sessionRepository;

	public static void enable() {
		RequestContext.setRequestContextLoader(RequestContextImpl::new);
	}

	/** 获取当前线程上绑定的请求上下文 */
	public static RequestContextImpl get() {
		return (RequestContextImpl) RequestContext.get();
	}

	/**
	 * 是否是后台请求
	 */
	public boolean fromBackstage() {
		return Context.get().fromBackstage(getRequest());
	}

	public static <U extends Member> void mock(U user, final Date nowDate) {
		System.setProperty("mock", "1");
		RequestContextImpl mock = new RequestContextImpl() {

			@SuppressWarnings("unchecked")
			@Override
			public U sessionUser() {
				return user;
			}

			@Override
			public Date now() {
				return nowDate;
			}
		};
		RequestContextImpl.setRequestContextLoader(() -> mock);
	}

	public static boolean isMock() {
		return "1".equals(System.getProperty("mock"));
	}

	@SuppressWarnings("unchecked")
	protected static SessionRepository<Session> getSessionRepository() {
		SessionRepository<Session> repository = sessionRepository;
		if (repository == null) {
			sessionRepository = repository = Context.getBean(SessionRepository.class);
		}
		return repository;
	}

	public static void removeSession(String sessionId) {
		getSessionRepository().deleteById(sessionId);
	}

	public static void flushSession(@Nonnull Session session) {
		getSessionRepository().save(session);
	}

	@Override
	public void init(HttpServletRequest request, HttpServletResponse response) {
		super.init(request, response);
		this.session = null;
		this.flushRequired = false;
		this.merchantId = null;
		ContextImpl.removeCurrentMerchantId();
	}

	@Override
	public void sessionAttr(String name, Object value) {
		HttpServletRequest request = getRequest();
		if (request == null) { // 非原始 Web 请求
			Session springSession = getSpringSession();
			if (springSession != null) {
				if (value == null) { // 删除
					springSession.removeAttribute(name);
				} else { // 设置
					springSession.setAttribute(name, value);
				}
				flushRequired = true;
			}
		} else {
			if (value == null) {  // 删除
				HttpSession session = request.getSession(false);
				if (session != null) {
					session.removeAttribute(name);
				}
			} else {  // 设置
				request.getSession().setAttribute(name, value);
			}
		}
	}

	@Override
	public void sessionUser(Object user) {
		sessionAttr(SESSION_USER, user);
	}

	@Nonnull
	public SessionContext sessionContext() {
		HttpServletRequest request = getRequest();
		return getSessionContext(request);
	}

	@Nonnull
	public SessionContext getSessionContext(@Nullable HttpServletRequest request) {
		SessionContext sessionContext = (SessionContext) getSessionAttr(request, SESSION_CONTEXT);
		if (sessionContext == null) {
			sessionContext = new SessionContext(doGetClient(request), doGetLanguage(request), doGetClientIP(request));
			sessionAttr(SESSION_CONTEXT, sessionContext);
		}
		if (sessionContext.getFlushListener() == null) {
			sessionContext.setFlushListener(ctx -> sessionAttr(SESSION_CONTEXT, ctx));
		}
		return sessionContext;
	}

	@Override
	public Client sessionClient() {
		return sessionContext().getClient();
	}

	@Nonnull
	public Language sessionLanguage() {
		return sessionContext().getLanguage();
	}

	@Nonnull
	public static Language doGetLanguage(@Nullable HttpServletRequest request) {
		String header = request == null ? null : request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
		return Language.of(header);
	}

	public static Long getMerchantId(HttpServletRequest request) {
		if (Context.get().fromBackstage(request)) { // 后台直接返回当前员工所属的商户ID
			Member emp = getSessionUser(request);
			if (emp != null) {
				return emp.getMerchantId();
			}
		}
		final String midName = "MID";
		String midStr = request.getParameter(midName);
		if (StringUtil.isEmpty(midStr)) {
			midStr = request.getHeader(midName);
		}
		// TODO 按照业务需求，完善获取商户ID的业务逻辑（为方便联调测试，先默认用 1 兜底，后续会根据业务需求调整）
		return NumberUtil.getLong(midStr, 1L);
	}

	public Long getMerchantId() {
		if (merchantId == null) {
			HttpServletRequest request = getRequest();
			merchantId = request == null ? ContextImpl.currentMerchantId() : getMerchantId(request);
		}
		return merchantId;
	}

	@Override
	public String clientIP() {
		return sessionContext().getIp();
	}

	public Session getSpringSession() {
		if (sessionId == null) {
			HttpServletRequest request = getRequest();
			if (request != null) {
				sessionId = request.getSession().getId();
			}
		}
		if (session == null && StringUtil.notEmpty(sessionId)) {
			return session = getSessionRepository().findById(sessionId);
		}
		return session;
	}

	@Nullable
	public static Session getSpringSession(String sessionId) {
		if (StringUtil.notEmpty(sessionId)) {
			return getSessionRepository().findById(sessionId);
		}
		return null;
	}

	@Nullable
	public static String decodeSessionId(String encodedSessionId) {
		if (StringUtil.notEmpty(encodedSessionId)) {
			return new String(Base64.getDecoder().decode(encodedSessionId));
		}
		return null;
	}

	@Nullable
	public static Session getSpringSessionByEncoded(String encodedSessionId) {
		return getSpringSession(decodeSessionId(encodedSessionId));
	}

	@Override
	protected Object doGetExternalSessionAttr(String name) {
		Session session = getSpringSession();
		if (session != null) {
			session.setLastAccessedTime(Instant.now());
			return session.getAttribute(name);
		}
		return null;
	}

	@Override
	public void flushSession(boolean force) {
		if (session != null && (force || flushRequired)) {
			getSessionRepository().save(session);
			flushRequired = false;
		}
	}

}
