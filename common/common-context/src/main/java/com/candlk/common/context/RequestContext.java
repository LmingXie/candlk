package com.candlk.common.context;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.*;

import com.candlk.common.web.*;
import lombok.Getter;
import lombok.Setter;
import me.codeplayer.util.StringUtil;
import org.springframework.web.context.request.*;

/**
 * 请求上下文
 *
 * @since 1.0
 */
public class RequestContext {

	protected static Callable<RequestContext> requestContextLoader;
	protected static final ThreadLocal<RequestContext> requestContextHolder = new ThreadLocal<>() {

		protected RequestContext initialValue() {
			RequestContext ctx = create();
			set(ctx);
			return ctx;
		}
	};
	//
	/** session attribute name for current client ip */
	static String CLIENT_IP = "clientIP";
	/** session attribute name for current session user */
	public static String SESSION_USER = "sessionUser";
	/** session attribute name for current language */
	static String LANGUAGE = "language";
	//
	/** 只有 request == null 时，才会设置该值 */
	@Getter
	@Setter
	protected transient String sessionId;
	@Nullable
	private HttpServletRequest request;
	@Nullable
	private HttpServletResponse response;
	protected transient Date now;

	/**
	 * 设置请求上下文的默认初始化加载器
	 */
	public static void setRequestContextLoader(final Callable<RequestContext> loader) {
		requestContextLoader = loader;
	}

	/**
	 * 将指定的请求上下文对象绑定到当前线程上
	 */
	public static void setContext(final RequestContext requestContext) {
		requestContextHolder.set(requestContext);
	}

	/**
	 * 将指定的请求上下文对象绑定到当前线程上，如果当前线程已存在请求上下文对象，则重置其 {@code request } 和 {@code response }，以便于重用该对象
	 *
	 * @param createIfAbsent 当无法从 ThreadLocal 中获取到当前线程对应的值时，是否允许自动创建 RequestContext。 <br>
	 * 如果为true，且存在RequestContextLoader，则优先使用该Loader创建 RequestContext，否则 <code> new RequestContext()</code>
	 */
	public static void reuseContext(final HttpServletRequest request, final HttpServletResponse response, final boolean createIfAbsent) {
		RequestContext ctx = requestContextHolder.get();
		if (ctx == null && createIfAbsent) {
			ctx = create();
			requestContextHolder.set(ctx);
		}
		if (ctx != null) {
			ctx.init(request, response);
		}
	}

	public static RequestContext create() {
		final Callable<RequestContext> loader = requestContextLoader;
		if (loader != null) {
			try {
				return loader.call();
			} catch (Exception e) {
				// ignore
			}
		}
		return new RequestContext();
	}

	/**
	 * 解除当前线程绑定的 RequestContext 资源
	 */
	public static void destroy() {
		requestContextHolder.remove();
	}

	/**
	 * 获取当前线程上绑定的请求上下文
	 */
	public static RequestContext get() {
		return requestContextHolder.get();
	}

	public void register() {
		setContext(this);
	}

	/**
	 * 初始化设置指定的 request 和 response，并将 <code>now</code> 重置为 null
	 */
	public void init(final HttpServletRequest request, final HttpServletResponse response) {
		this.request = request;
		this.response = response;
		this.sessionId = null;
		this.now = null;
	}

	/**
	 * 在非请求环境下，清理之前的遗留数据
	 */
	public void tryClean() {
		ServletRequestAttributes attributes = getRequestAttributes();
		if (attributes == null) { // 不是同一个线程，才清理
			init(null, null);
		}
	}

	/**
	 * 获取指定请求参数名称的参数值
	 */
	public String getParameter(final String name) {
		HttpServletRequest request = getRequest();
		return request == null ? null : request.getParameter(name);
	}

	/**
	 * 获取指定请求参数名称的参数值数组
	 */
	public String[] getParameterValues(final String name) {
		HttpServletRequest request = getRequest();
		return request == null ? null : request.getParameterValues(name);
	}

	/**
	 * 获取当前请求的所有参数K-V集合
	 */
	public Map<String, String[]> getParameterMap() {
		HttpServletRequest request = getRequest();
		return request == null ? null : request.getParameterMap();
	}

	/**
	 * 获取指定名称的Request Attribute
	 */
	@SuppressWarnings("unchecked")
	public <T> T attr(final String name) {
		HttpServletRequest request = getRequest();
		return request == null ? null : (T) request.getAttribute(name);
	}

	/**
	 * 设置或移除指定名称的 Request Attribute
	 *
	 * @param value 如果 value 为 null，则表示移除
	 */
	public void attr(final String name, final Object value) {
		HttpServletRequest request = getRequest();
		if (value == null) {
			request.removeAttribute(name);
		} else {
			request.setAttribute(name, value);
		}
	}

	/**
	 * 获取指定名称的Session Attribute
	 */
	@SuppressWarnings("unchecked")
	public <T> T sessionAttr(final String name) {
		return (T) getSessionAttr(name);
	}

	/**
	 * 设置或移除指定名称的Session Attribute
	 *
	 * @param value 如果value为null，则表示移除
	 */
	public void sessionAttr(final String name, final Object value) {
		HttpServletRequest request = getRequest();
		if (value == null) {
			HttpSession session = request.getSession(false);
			if (session != null) {
				session.removeAttribute(name);
			}
		} else {
			request.getSession().setAttribute(name, value);
		}
	}

	/**
	 * 获取指定名称的ServletContext Attribute
	 */
	@SuppressWarnings("unchecked")
	public <T> T appAttr(final String name) {
		HttpServletRequest request = getRequest();
		return (T) request.getServletContext().getAttribute(name);
	}

	/**
	 * 设置或移除指定名称的ServletContext Attribute
	 *
	 * @param value 如果value为null，则表示移除
	 */
	public void appAttr(final String name, final Object value) {
		HttpServletRequest request = getRequest();
		if (value == null) {
			request.getServletContext().removeAttribute(name);
		} else {
			request.getServletContext().setAttribute(name, value);
		}
	}

	/**
	 * 获取指定名称的请求头
	 */
	public String header(final String name) {
		HttpServletRequest request = getRequest();
		return request == null ? null : request.getHeader(name);
	}

	/**
	 * 设置或移除指定名称的请求头
	 *
	 * @param value 如果value为null，则表示移除
	 */
	public void header(final String name, final String value) {
		HttpServletResponse response = getResponse();
		response.setHeader(name, value);
	}

	/**
	 * 获取请求头名称集合
	 */
	@Nullable
	public Enumeration<String> headers() {
		HttpServletRequest request = getRequest();
		return request == null ? null : request.getHeaderNames();
	}

	/**
	 * 获取指定名称的日期类型的请求头
	 */
	public long dateHeader(final String name) {
		HttpServletRequest request = getRequest();
		return request.getDateHeader(name);
	}

	/**
	 * 获取当前请求的 Method
	 */
	public String getMethod() {
		HttpServletRequest request = getRequest();
		return request == null ? null : request.getMethod();
	}

	/**
	 * 设置或移除指定名称的日期类型的请求头
	 */
	public void dateHeader(final String name, final long value) {
		HttpServletResponse response = getResponse();
		response.setDateHeader(name, value);
	}

	/**
	 * 获取指定名称的整数类型的请求头
	 */
	public int intHeader(final String name) {
		HttpServletRequest request = getRequest();
		return request.getIntHeader(name);
	}

	/**
	 * 设置或移除指定名称的整数类型的请求头
	 */
	public void intHeader(final String name, final int value) {
		getResponse().setIntHeader(name, value);
	}

	private ServletRequestAttributes getRequestAttributes() {
		RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
		if (attributes == null) {
			sessionId = "";
			return null;
		}
		return (ServletRequestAttributes) attributes;
	}

	/**
	 * 获取对应的Request对象
	 */
	public HttpServletRequest getRequest() {
		if (request == null && sessionId == null) {
			ServletRequestAttributes attributes = getRequestAttributes();
			if (attributes != null) {
				request = attributes.getRequest();
			}
		}
		return request;
	}

	/**
	 * 获取对应的Response对象
	 */
	public HttpServletResponse getResponse() {
		if (response == null && sessionId == null) {
			ServletRequestAttributes attributes = getRequestAttributes();
			if (attributes != null) {
				response = attributes.getResponse();
			}
		}
		return response;
	}

	/**
	 * 获取当前时间
	 */
	public Date now() {
		if (now == null || getRequest() == null) {
			now = new Date();
		}
		return now;
	}

	/**
	 * 获取当前请求客户端IP地址
	 */
	@Nonnull
	public String clientIP() {
		return getRequiredSessionAttr(CLIENT_IP, RequestContext::doGetClientIP);
	}

	/**
	 * 获取当前请求会话的用户
	 */
	@SuppressWarnings("unchecked")
	public <E> E sessionUser() {
		return (E) getSessionAttr(SESSION_USER);
	}

	@Nonnull
	protected <T> T getRequiredSessionAttr(String name, Function<HttpServletRequest, ? extends T> defaultLoader) {
		HttpServletRequest request = getRequest();
		Object val = getSessionAttr(request, name);
		if (val == null) {
			val = defaultLoader.apply(request);
			sessionAttr(name, val);
		}
		return (T) val;
	}

	/**
	 * 获取当前请求会话的客户端类型
	 */
	@Nonnull
	public Client sessionClient() {
		return getRequiredSessionAttr(Context.internal().getUserClientSessionAttr(), RequestContext::doGetClient);
	}

	/**
	 * 设置/替换当前session的会话用户
	 */
	public void sessionUser(final Object user) {
		HttpServletRequest request = getRequest();
		if (request != null) {
			request.getSession().setAttribute(SESSION_USER, user);
		}
	}

	/**
	 * 获取指定name的Cookie
	 */
	public Cookie getCookie(String name) {
		HttpServletRequest request = getRequest();
		if (request == null) {
			return null;
		}
		return CookieUtil.getCookie(request, name);
	}

	/**
	 * 获取指定name的Cookie Value
	 */
	public String getCookieValue(String name) {
		HttpServletRequest request = getRequest();
		if (request == null) {
			return null;
		}
		return CookieUtil.getCookieValue(request, name);
	}

	/**
	 * 获取所有的Cookie
	 */
	public Cookie[] getCookies() {
		HttpServletRequest request = getRequest();
		if (request == null) {
			return null;
		}
		return request.getCookies();
	}

	/**
	 * 添加Cookie
	 */
	public boolean addCookie(Cookie cookie) {
		HttpServletResponse response = getResponse();
		if (response != null) {
			response.addCookie(cookie);
			return true;
		}
		return false;
	}

	/**
	 * 添加Cookie
	 *
	 * @param expireInSecs 默认=浏览器默认值
	 * @param domain 默认=当前域名
	 * @param path 默认=当前URL所在目录
	 */
	public boolean addCookie(String name, @Nullable String value, @Nullable Integer expireInSecs, @Nullable String domain, @Nullable String path, @Nullable Boolean httpOnly) {
		HttpServletResponse response = getResponse();
		if (response != null) {
			CookieUtil.setCookie(response, name, value, expireInSecs, domain, path, null, httpOnly);
			return true;
		}
		return false;
	}

	/**
	 * 移除指定 name 的 Cookie
	 */
	public boolean removeCookie(String name) {
		HttpServletResponse response = getResponse();
		if (response != null) {
			CookieUtil.removeCookie(response, name, null);
			return true;
		}
		return false;
	}

	/**
	 * 获取指定请求客户端IP地址
	 * 【注意】：不要使用这个方法，请使用子类的静态方法
	 */
	@Nonnull
	public static String doGetClientIP(@Nullable HttpServletRequest request) {
		if (request == null) {
			return "127.0.0.1";
		}
		return ServletUtil.getClientIP(request);
	}

	/**
	 * 获取指定请求客户端IP地址
	 * 【注意】：不要使用这个方法，请使用子类的静态方法
	 */
	@Nonnull
	public static Client doGetClient(@Nullable HttpServletRequest request) {
		if (request != null) {
			return (Client) Client.getResolver().resolveClient(request);
		}
		return Client.UNKNOWN;
	}

	/**
	 * 获取当前用户的登录用户
	 */
	@SuppressWarnings("unchecked")
	public static <E> E getSessionUser(final HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		final HttpSession session = request.getSession(false);
		return session == null ? null : (E) session.getAttribute(SESSION_USER);
	}

	/**
	 * 设置当前用户的登录用户
	 */
	public static void setSessionUser(final HttpServletRequest request, final Object user) {
		request.getSession().setAttribute(SESSION_USER, user);
	}

	public Object getSessionAttr(@Nullable HttpServletRequest request, String name) {
		if (request != null) {
			HttpSession session = request.getSession(false);
			return session == null ? null : session.getAttribute(name);
		}
		if (StringUtil.notEmpty(sessionId)) {
			return doGetExternalSessionAttr(name);
		}
		return null;
	}

	public Object getSessionAttr(String name) {
		return getSessionAttr(getRequest(), name);
	}

	@Nullable
	protected Object doGetExternalSessionAttr(String name) {
		// in RequestContextImpl
		return null;
	}

	public void flushSession(boolean force) {
		// in RequestContextImpl
	}

}
