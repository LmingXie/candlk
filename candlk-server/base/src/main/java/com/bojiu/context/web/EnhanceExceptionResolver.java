package com.bojiu.context.web;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;

import com.bojiu.common.context.*;
import com.bojiu.common.model.ErrorMessageException;
import com.bojiu.common.model.Messager;
import com.bojiu.common.util.*;
import com.bojiu.common.warn.MsgWarnChannel;
import com.bojiu.common.web.Logs;
import com.bojiu.common.web.ServletUtil;
import com.bojiu.common.web.mvc.FastJsonView;
import com.bojiu.context.model.BaseI18nKey;
import com.bojiu.context.model.Member;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerMethodExceptionResolver;

/**
 * 功能增强的异常拦截器，用于处理所有的程序异常
 */
@Slf4j
public class EnhanceExceptionResolver extends AbstractHandlerMethodExceptionResolver {

	public static final String errorMessageAttr = "errorMessage";
	// static final AtomicLong globalErrorCounter = new AtomicLong();

	static final Cache<ExceptionIdentity, ErrorCounter> errorCounterCache = Caffeine.newBuilder()
			.initialCapacity(16)
			.maximumSize(2048)
			.expireAfterAccess(3, TimeUnit.MINUTES)
			.build();

	@Override
	public int getOrder() {
		return -1;
	}

	@Setter
	@Nonnull
	protected ExceptionMessagerResolver messagerResolver = new DefaultExceptionCauseResolver();
	/** 当 {@link Messager#getExt()} 中返回的字符串具有该前缀时，则表示视图名称，否则仅用于备注信息 */
	@Getter
	@Setter
	@Nonnull
	protected String viewNamePrefix = "@";

	@Nullable
	static MsgWarnChannel msgWarnChannel;

	@Autowired(required = false)
	public void setMsgWarnChannel(MsgWarnChannel service) {
		msgWarnChannel = service;
	}

	@Override
	protected boolean shouldApplyTo(HttpServletRequest request, Object handler) {
		return true;
	}

	@Override
	protected ModelAndView doResolveHandlerMethodException(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, Exception ex) {
		request.setAttribute(Logs.EXCEPTION, ex);
		if (ex instanceof BindException be) {
			return handleValidateError(request, response, be);
		}
		if (ex instanceof org.springframework.web.servlet.NoHandlerFoundException) {
			return handleNotFound(request, response, ex);
		}
		if (ex instanceof org.springframework.web.HttpRequestMethodNotSupportedException || ex instanceof HttpMediaTypeException) {
			return handleMethodNotSupported(request, response, ex);
		}
		if (ex instanceof ValidationException && ex.getCause() instanceof Exception c) {
			ex = c;
		}
		return handleDefault(request, response, ex);
	}

	protected ModelAndView handleValidateError(HttpServletRequest request, HttpServletResponse response, BindException e) {
		ObjectError globalError = e.getBindingResult().getGlobalError();
		String error;
		if (globalError == null) {
			error = I18N.msg(BaseI18nKey.PARAMETER_ERROR);
			logError(request, e);
		} else {
			error = globalError.getDefaultMessage();
		}
		Messager<Void> msger = Messager.status(Messager.INPUT_ERROR, error);
		return response(request, response, HttpServletResponse.SC_BAD_REQUEST, msger);
	}

	protected ModelAndView handleMethodNotSupported(HttpServletRequest request, HttpServletResponse response, Exception e) {
		Messager<Void> msger = Messager.status("405", "Invalid request mode!");
		return response(request, response, HttpServletResponse.SC_METHOD_NOT_ALLOWED, msger);
	}

	protected ModelAndView handleNotFound(HttpServletRequest request, HttpServletResponse response, Exception e) {
		Messager<Void> msger = Messager.status("404", I18N.msg(BaseI18nKey.PAGE_NOT_FOUND));
		return response(request, response, HttpServletResponse.SC_NOT_FOUND, msger);
	}

	protected ModelAndView handleDefault(HttpServletRequest request, HttpServletResponse response, Exception e) {
		logError(request, e);
		if (e instanceof DomainException) { // 如果是域名映射处理异常，则直接报404
			return response(request, response, HttpServletResponse.SC_NOT_FOUND, Messager.status("D404"));
		}
		Messager<?> msger = messagerResolver.resolve(request, response, e);
		return response(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msger);
	}

	public static void logError(HttpServletRequest request, Throwable e) {
		final Member member = RequestContext.getSessionUser(request);
		final String identity = member == null ? "0" : member.getMerchantId() + "-" + member.getId();
		String queryString = request.getQueryString();
		final boolean empty = StringUtil.isEmpty(queryString);
		// final String errorId = StringUtil.zeroFill(globalErrorCounter.addAndGet(1), 6);
		// request.setAttribute("errorId", errorId);
		String msg = /* errorId + ":" + */ "系统出现业务逻辑异常[" + identity + "]：" + request.getMethod()
				+ " \"" + request.getRequestURL() + (empty ? "" : "?") + (empty ? "" : queryString)
				+ "\" [" + ServletUtil.getReferer(request) + ']';
		final MsgWarnChannel service = msgWarnChannel;
		if (service != null && shouldReport(request, e)) {
			String uri = request.getRequestURI();
			SpringUtil.asyncRun(() -> service.sendBugMsg(uri, e), "钉钉告警时出错");
		}
		log.error(msg, e);
	}

	/**
	 * 是否应该上报异常告警：当异常为 ErrorMessageException 时，如果 report = false，则不上报
	 */
	static boolean shouldReport(HttpServletRequest request, Throwable e) {
		if (e instanceof ErrorMessageException eme) {
			return eme.isReport();
		}
		if (e.getCause() instanceof ErrorMessageException eme) {
			return eme.isReport();
		}
		if (e instanceof AssertException) {
			return false;
		}
		if (!Env.inProduction()) { // 非生产环境主要用于联调测试，有报错应该告警
			return true;
		}
		final ExceptionIdentity identity = getExceptionIdentity(e, request.getRequestURI());
		return errorCounterCache.get(identity, ErrorCounter.FACTORY).shouldReport();
	}

	@Nonnull
	static ExceptionIdentity getExceptionIdentity(Throwable e, String requestURI) {
		final StackTraceElement[] traceElements = e.getStackTrace();
		StackTraceElement ste = null;
		for (StackTraceElement traceElement : traceElements) {
			final String fileName = traceElement.getFileName();
			if ("Assert.java".equals(fileName) || "I18N.java".equals(fileName) || !traceElement.getClassName().startsWith("com.bojiu.")) {
				continue;
			}
			ste = traceElement;
			break;
		}
		return ste == null
				? new ExceptionIdentity(requestURI, "", 0)
				: new ExceptionIdentity(requestURI, ste.getFileName(), ste.getLineNumber());
	}

	record ExceptionIdentity(String uri, String fileName, int lineNumber) {

	}

	static class ErrorCounter {

		static final Function<ExceptionIdentity, ErrorCounter> FACTORY = k -> new ErrorCounter();

		private final AtomicInteger counter = new AtomicInteger();
		private volatile long nextWarnTime;

		boolean shouldReport() {
			final int cnt = counter.incrementAndGet();
			if (cnt % 10 == 0) { // 短时间内报错满10次就告警一次
				final long nowInMs = System.currentTimeMillis();
				if (nextWarnTime == 0 || nowInMs > nextWarnTime) {
					synchronized (this) {
						if (nextWarnTime == 0 || nowInMs > nextWarnTime) {
							// 每分钟最多只告警一次
							nextWarnTime = System.currentTimeMillis() + EasyDate.MILLIS_OF_MINUTE;
							counter.addAndGet(-cnt);
							return true;
						}
					}
				}
			}
			return false;
		}

	}

	/** 此处必须返回 NOT NULL，否则会被忽略 */
	@Nonnull
	protected ModelAndView response(HttpServletRequest request, HttpServletResponse response, int statusCode, @Nonnull Messager<?> msger) {
		if (statusCode > 0 && RequestContext.getAppId(request) != null) {
			response.setStatus(statusCode);
		}
		if (StringUtil.startsWith(msger.getMsg(), '@')) {
			msger.setMsg(RequestContextImpl.doGetLanguage(request).msg(msger.getMsg()));
		}
		request.setAttribute(Logs.RESPONSE, msger);
		if (shouldOutputJSON(request)) { // 如果是AJAX请求，将错误信息以JSON格式响应回去
			return new ModelAndView(FastJsonView.INSTANCE, FastJsonView.JSON_KEY, msger);
		}
		Object extra = msger.getExt();
		ModelAndView view = null;
		String remark = null;
		if (extra != null) {
			if (extra instanceof ModelAndView mav) {
				view = mav;
			} else if (extra instanceof CharSequence) {
				remark = extra.toString();
				final String prefix = getViewNamePrefix();
				if (remark.startsWith(prefix)) {
					view = new ModelAndView(remark.substring(prefix.length()));
					msger.setExt(remark = null);
				}
			}
		}
		if (StringUtil.isEmpty(remark)) {
			remark = I18N.msg(BaseI18nKey.SYSTEM_DEFAULT_FAULT_TIP);
		}
		request.setAttribute("messager", msger);
		request.setAttribute(errorMessageAttr, Context.get().escapeHtml(msger.getMsg()));
		request.setAttribute("remark", remark);
		if (view == null) {
			view = new ModelAndView();
		}
		if (view.getViewName() == null && view.getView() == null) {
			String viewName = "/common/error.html";
			view.setViewName(viewName);
		}
		return view;
	}

	protected boolean shouldOutputJSON(HttpServletRequest request) {
		return true;
		// return ServletUtil.isAcceptJSON(request);
	}

	public interface ExceptionMessagerResolver {

		@Nonnull
		Messager<?> resolve(HttpServletRequest request, HttpServletResponse response, Exception e);

	}

	static EnhanceExceptionResolver instance;

	public static EnhanceExceptionResolver getInstance() {
		EnhanceExceptionResolver handler = instance;
		if (handler == null) {
			instance = handler = Context.getBean(EnhanceExceptionResolver.class, EnhanceExceptionResolver::new);
		}
		return handler;
	}

	private static void prepareCommonAttr(HttpServletRequest request, boolean fromBackstage) {
		request.setAttribute("fromBackstage", fromBackstage);
		request.setAttribute("homepageURL", Context.nav().getSiteURL(!fromBackstage));
	}

	public static class DefaultExceptionCauseResolver implements ExceptionMessagerResolver {

		@Nonnull
		@Override
		public Messager<?> resolve(HttpServletRequest request, HttpServletResponse response, Exception e) {
			if (e instanceof ValidationException && e.getCause() instanceof Exception c) {
				e = c;
			}
			final Messager<?> msger;
			if (e instanceof ErrorMessageException em) {
				msger = em.getMessager();
			} else {
				msger = new Messager<>();
				String errorMsg = tryHideDetail(e);
				if (errorMsg == null) {
					errorMsg = tryAsIllegal(e);
				}
				if (StringUtil.isEmpty(errorMsg)) {
					errorMsg = e.getLocalizedMessage();
					if (StringUtil.isEmpty(errorMsg) || errorMsg.contains("xception") /* 模糊匹配 "Exception" 或 "exception" */) {
						errorMsg = I18N.msg(BaseI18nKey.SYSTEM_ERROR);
					}
				}
				msger.setMsg(errorMsg).setERROR();
			}
			return msger;
		}

		/** 这些异常需要统一对外提示"网络异常" */
		protected Class<?>[] networkExceptionTypes = toClasses(
				IllegalMonitorStateException.class, // Redis 分布式锁 unlock 异常
				"org.apache.dubbo.rpc.RpcException",
				org.redisson.client.RedisException.class,
				IOException.class);

		/** 这些异常需要对外隐藏错误细节 */
		protected Class<?>[] hideExceptionTypes = toClasses(
				ClassCastException.class,
				ArrayIndexOutOfBoundsException.class,
				"com.baomidou.mybatisplus.core.exceptions.MybatisPlusException",
				"org.springframework.dao.DataAccessException",
				"org.springframework.transaction.TransactionException",
				"org.springframework.beans.TypeMismatchException",
				"org.springframework.web.bind.ServletRequestBindingException"
		);

		protected static Class<?>[] toClasses(Object... classNames) {
			return Stream.of(classNames).map(o -> o.getClass() == Class.class ? (Class<?>) o : ClassUtil.tryLoadClass((String) o))
					.filter(Common.nonNull())
					.toArray(Class[]::new);
		}

		protected boolean contains(Exception e, Class<?>[] groupTypes) {
			final Throwable cause = e.getCause();
			for (Class<?> type : groupTypes) {
				if (type.isInstance(e) || type.isInstance(cause)) {
					return true;
				}
			}
			return false;
		}

		/** IO异常、数据库异常 或 JSP异常，应该隐藏异常源信息 */
		protected String tryHideDetail(Exception e) {
			if (contains(e, networkExceptionTypes) || isDubboRemoteException(e)) {
				return I18N.msg(BaseI18nKey.NETWORK_ABORT);
			} else if (e instanceof java.lang.NumberFormatException
					|| e instanceof org.springframework.jdbc.UncategorizedSQLException
					|| e instanceof org.springframework.beans.InvalidPropertyException
					|| e instanceof org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
					|| e instanceof org.springframework.web.multipart.MultipartException
			) {
				return I18N.msg(BaseI18nKey.PARAMETER_ERROR);
			}
			String error = dbDataTruncationError(e);
			if (error != null) {
				return error;
			}
			return contains(e, hideExceptionTypes) ? I18N.msg(BaseI18nKey.SYSTEM_BUSY) : null;
		}

		@Nullable
		static String dbDataTruncationError(Exception e) {
			// Cause: com.mysql.cj.jdbc.exceptions.MysqlDataTruncation: Data truncation: Out of range value for column 'move_times' at row 1
			// Cause: com.mysql.cj.jdbc.exceptions.MysqlDataTruncation: Data truncation: Data too long for column 'texts' at row 1
			if (e instanceof org.springframework.dao.DataIntegrityViolationException) {
				Throwable cause = e.getCause();
				String msg;
				if (cause != null && StringUtils.startsWith(msg = cause.getMessage(), "Data truncation")) {
					if (StringUtils.indexOf(msg, "Data too long", 15) > 0) { // "Data too long"
						return I18N.msg(BaseI18nKey.INPUT_TOO_LONG);
					} else if (StringUtils.indexOf(msg, "Out of range", 15) > 0) { // "Out of range"
						return I18N.msg(BaseI18nKey.INPUT_OUT_OF_RANGE);
					} else { // "Incorrect string value"（一般是超出编码范围）
						// "Incorrect date value" 一般遇不到，因为会提前转为 Java中的 Date 对象，再保存到数据库
						return I18N.msg(BaseI18nKey.INPUT_UNSUPPORTED);
					}
				}
			}
			return null;
		}

		protected boolean isDubboRemoteException(Exception e) {
			return e.getClass() == RuntimeException.class &&
					// ErrorMessageException 可能不输出堆栈，但也需要抑制对外输出类型信息
					(StringUtils.contains(e.getMessage(), "at org.apache.dubbo.rpc.") || StringUtils.startsWith(e.getMessage(), ErrorMessageException.class.getName()));
		}

		/** 无定制信息的异常，应该隐藏异常源信息 */
		protected String tryAsIllegal(Exception e) {
			final String msg = e.getMessage();
			if (StringUtil.notEmpty(msg)) {
				// JDK 17 改进了 NPE 提示的细节定位能力，因此需要对外屏蔽细节
				// java.lang.NullPointerException: Cannot read field "username" because "user" is null
				// java.lang.NullPointerException: Cannot invoke "...Class.getUsername()" because "user" is null
				if (e instanceof NullPointerException && StringUtils.endsWith(msg, "is null")) {
					return BaseI18nKey.ILLEGAL_REQUEST;
				}
			} else if (e instanceof NullPointerException
					|| e instanceof IllegalArgumentException
					|| e instanceof AssertException
					|| e instanceof IllegalStateException) {
				return BaseI18nKey.ILLEGAL_REQUEST;
			}
			return null;
		}

	}

}