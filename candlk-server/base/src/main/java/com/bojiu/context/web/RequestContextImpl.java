package com.bojiu.context.web;

import java.nio.charset.Charset;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import javax.servlet.http.*;

import com.bojiu.common.context.*;
import com.bojiu.common.dao.DataSourceSelector;
import com.bojiu.common.model.Bean;
import com.bojiu.common.model.ErrorMessageException;
import com.bojiu.common.web.Client;
import com.bojiu.context.ContextImpl;
import com.bojiu.context.SessionContext;
import com.bojiu.context.auth.DefaultAutoLoginHandler;
import com.bojiu.context.model.*;
import lombok.Setter;
import me.codeplayer.util.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;

public class RequestContextImpl extends RequestContext {

	/** 用于存储 当前会话上下文 的 session key */
	public static final String SESSION_CONTEXT = "sessionContext";
	public static final String ATTR_TIME_ZONE = "$TimeZone";
	public static final String ATTR_MID = "$MID";
	public static final String ATTR_LANG = "$Lang";
	public static final String COUNTRY = "country";

	protected ClientInfo clientInfo;

	protected transient Session session;
	protected boolean flushRequired;

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
	public static SessionRepository<Session> getSessionRepository() {
		SessionRepository<Session> repository = sessionRepository;
		if (repository == null) {
			sessionRepository = repository = Context.getBean(SessionRepository.class);
		}
		return repository;
	}

	public static void removeSession(String sessionId) {
		if (StringUtil.notEmpty(sessionId)) {
			getSessionRepository().deleteById(sessionId);
		}
	}

	public static void flushSession(@NonNull Session session) {
		getSessionRepository().save(session);
	}

	@Override
	public void init(HttpServletRequest request, HttpServletResponse response) {
		super.init(request, response);
		this.clientInfo = null;
		this.session = null;
		this.flushRequired = false;
		ContextImpl.removeCurrentMerchantId();
		ContextImpl.localeThreadLocal.remove();
		DataSourceSelector.reset();
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

	public Long getUserId() {
		return Bean.idOf(sessionUser());
	}

	@NonNull
	public ClientInfo getClientInfo() {
		if (clientInfo == null) {
			clientInfo = ClientInfo.of(super.getAppId());
		}
		return clientInfo;
	}

	@Override
	public String getAppId() {
		return getClientInfo().header();
	}

	@NonNull
	public SessionContext sessionContext() {
		return getSessionContext(getRequest());
	}

	@NonNull
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

	@NonNull
	@Override
	public Client sessionClient() {
		return sessionContext().getClient();
	}

	@NonNull
	public Language getLanguage() {
		HttpServletRequest request = getRequest();
		if (request != null) {
			return doGetLanguage(request);
		}
		if (StringUtil.notEmpty(sessionId)) {
			SessionContext sc = (SessionContext) doGetExternalSessionAttr(SESSION_CONTEXT);
			if (sc != null) {
				return sc.getLanguage();
			}
		}
		return Language.DEFAULT;
	}

	@NonNull
	public static Language doGetLanguage(@Nullable HttpServletRequest request) {
		if (request == null) {
			return Language.DEFAULT;
		}
		Language lang = (Language) request.getAttribute(ATTR_LANG);
		return lang == null ? doGetAndAttrLanguage(request, Language.DEFAULT) : lang;
	}

	protected static Language doGetAndAttrLanguage(@NonNull HttpServletRequest request, Language def) {
		String header = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
		if (StringUtil.notEmpty(header)) {
			Language lang = Language.of(header);
			request.setAttribute(ATTR_LANG, lang);
			return lang;
		}
		return def;
	}

	public static Country doGetCountry(@Nullable HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		String header = request.getHeader(COUNTRY);
		if (StringUtil.notEmpty(header)) {
			return Country.of(header);
		}
		return null;
	}

	public static Long getMerchantId(HttpServletRequest request) {
		Object val = request.getAttribute(ATTR_MID);
		if (val == null) {
			Long mid = doGetMerchantId(request);
			request.setAttribute(ATTR_MID, mid);
			return mid;
		}
		return (Long) val;
	}

	@Setter
	private static Function<HttpServletRequest, Long> merchantIdParser;

	public static Long doGetMerchantIdByEnv(HttpServletRequest request, boolean fromEmpFirst) {
		if (Env.outer()) {
			return merchantIdParser.apply(request);
		}
		if (Env.inTest()) {
			if ("brl.tbgopen.com".equals(request.getServerName())) {
				return 1L; /* 供演示使用的商户站点ID */
			}
		}
		if (fromEmpFirst && MemberType.fromBackstage()) { // 后台直接返回当前员工所属的商户ID
			Member emp = getSessionUser(request);
			if (emp != null) {
				return emp.getMerchantId();
			}
		}
		// 为方便开发调试，非生产环境允许传入 参数或请求头 MID 来自定义商户ID
		String midStr = request.getParameter("MID");
		if (StringUtil.isEmpty(midStr)) {
			midStr = request.getHeader("mid");
		}
		return NumberUtil.getLong(midStr, null);
	}

	public static Long doGetMerchantId(HttpServletRequest request, boolean fromEmpFirst) {
		Long envMerchantId = doGetMerchantIdByEnv(request, fromEmpFirst);
		if (envMerchantId == null && Env.inner()) {
			if ((envMerchantId = getSelectedMerchantId(request)) == null) { // 测试环境使用merchantId
				throw new ErrorMessageException("Invalid Access", Env.inner());
			}
		}
		if (envMerchantId > 0 && MemberType.fromBackstage()) {
			final Member emp = getSessionUser(request);
			if (emp != null) {
				final Long merchantId = getSelectedMerchantId(request);
				if (merchantId != null) {
					if (!emp.canAccess(merchantId)) {
						throw new ErrorMessageException("你没有权限进行此操作!", false);
					}
					return merchantId;
				}
				/*
				if (emp.type() == MemberType.MERCHANT) {
					// 这里必须要兜底，否则可能有提权风险
					return Assert.notNull(CollectionUtil.getAny(emp.merchantIds()));
				}
				*/
			}
		}
		return envMerchantId;
	}

	@Nullable
	public static Long getSelectedMerchantId(HttpServletRequest request) {
		final String name = "merchantId";
		String midStr = request.getParameter(name);
		if (StringUtil.isEmpty(midStr)) {
			midStr = request.getHeader(name);
		}
		return NumberUtil.getLong(midStr, null);
	}

	public static Long doGetMerchantId(HttpServletRequest request) {
		return doGetMerchantId(request, true);
	}

	public Long getMerchantId() {
		HttpServletRequest request = getRequest();
		return request == null ? ContextImpl.currentMerchantId() : getMerchantId(request);
	}

	@NonNull
	public TimeZone getTimeZone() {
		HttpServletRequest request = getRequest();
		Object timeZone = request.getAttribute(ATTR_TIME_ZONE);
		if (timeZone == null) {
			TimeZone tz = ContextImpl.get().fromBackstage(request) ? null : getClientInfo().timeZone();
			// 后台统一使用商户所在时区进行显示
			if (tz == null) {
				tz = ContextImpl.merchantId2TimeZoneMapper.apply(RequestContextImpl.getMerchantId(request));
			}
			request.setAttribute(ATTR_TIME_ZONE, tz);
			return tz;
		}
		return (TimeZone) timeZone;
	}

	@NonNull
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
			return session = getSpringSession(sessionId);
		}
		return session;
	}

	@Nullable
	public static Session getSpringSession(String sessionId) {
		if (StringUtil.notEmpty(sessionId)) {
			return getSessionRepository().findById(DefaultAutoLoginHandler.normalizeSessionId(sessionId));
		}
		return null;
	}

	@Nullable
	public static String decodeSessionId(String encodedSessionId) {
		if (StringUtil.notEmpty(encodedSessionId)) {
			return JavaUtil.newString(Base64.getDecoder().decode(encodedSessionId), Charset.defaultCharset());
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

	/**
	 * 添加一个延迟处理任务，会在 Action方法 执行后并满足 <code> whenFlag </code> 的情况下执行
	 *
	 * @param whenFlag 前置条件 位运算标识：0=任意情况下都执行；1=返回OK时执行；2=返回不OK时执行；4=报错时执行
	 * @see DeferTask#whenAny
	 * @see DeferTask#whenOK
	 * @see DeferTask#whenFail
	 * @see DeferTask#whenError
	 * @see DeferTask#whenFail
	 */
	public void addDeferTask(int whenFlag, Runnable task) {
		DeferTask.addDeferTask(getRequest(), whenFlag, task);
	}

	/**
	 * 注册后置处理任务，只会在响应 OK 时执行
	 */
	public void addDeferTaskAfterOK(Runnable task) {
		addDeferTask(DeferTask.whenOK, task);
	}

}