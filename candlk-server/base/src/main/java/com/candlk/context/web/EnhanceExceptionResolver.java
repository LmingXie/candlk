package com.candlk.context.web;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ValidationException;

import com.candlk.common.alarm.dingtalk.BugSendService;
import com.candlk.common.context.*;
import com.candlk.common.model.*;
import com.candlk.common.util.ClassUtil;
import com.candlk.common.util.SpringUtil;
import com.candlk.common.web.Logs;
import com.candlk.common.web.ServletUtil;
import com.candlk.common.web.mvc.FastJsonView;
import com.candlk.context.model.BaseI18nKey;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.StringUtil;
import me.codeplayer.util.X;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerMethodExceptionResolver;

/**
 * 功能增强的异常拦截器，用于处理所有的程序异常
 */
@Slf4j
public class EnhanceExceptionResolver extends AbstractHandlerMethodExceptionResolver {

	public static final String errorMessageAttr = "errorMessage";
	static final AtomicLong errorCounter = new AtomicLong();

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
	static BugSendService bugSendService;

	@Autowired(required = false)
	public void setBugSendService(BugSendService service) {
		bugSendService = service;
	}

	@Override
	protected boolean shouldApplyTo(HttpServletRequest request, Object handler) {
		return true;
	}

	@Override
	protected ModelAndView doResolveHandlerMethodException(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, Exception ex) {
		request.setAttribute(Logs.EXCEPTION, ex);
		if (ex instanceof BindException) {
			return handleValidateError(request, response, (BindException) ex);
		}
		if (ex instanceof org.springframework.web.servlet.NoHandlerFoundException) {
			return handleNotFound(request, response, ex);
		}
		if (ex instanceof HttpRequestMethodNotSupportedException || ex instanceof HttpMediaTypeException) {
			return handleMethodNotSupported(request, response, ex);
		}
		if (ex instanceof ValidationException && ex.getCause() instanceof Exception) {
			ex = (Exception) ex.getCause();
		}
		return handleDefault(request, response, ex);
	}

	protected ModelAndView handleValidateError(HttpServletRequest request, HttpServletResponse response, BindException e) {
		ObjectError globalError = e.getBindingResult().getGlobalError();
		String error;
		if (globalError == null) {
			error = "Parameter error, please confirm!";
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
		Messager<Void> msger = Messager.status("404", "Page Not Found!");
		return response(request, response, HttpServletResponse.SC_NOT_FOUND, msger);
	}

	protected ModelAndView handleDefault(HttpServletRequest request, HttpServletResponse response, Exception e) {
		logError(request, e);
		Messager<?> msger = messagerResolver.resolve(request, response, e);
		return response(request, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msger);
	}

	public static void logError(HttpServletRequest request, Throwable e) {
		final Bean<Number> sessionUser = RequestContext.getSessionUser(request);
		final Long userId = Bean.toLongId(sessionUser);
		String queryString = request.getQueryString();
		final boolean empty = StringUtil.isEmpty(queryString);
		final String errorId = StringUtil.zeroFill(Long.toString(errorCounter.addAndGet(1)), 6);
		request.setAttribute("errorId", errorId);
		String msg = errorId + ":系统出现业务逻辑异常[" + userId + "]：" + request.getMethod()
		             + " \"" + request.getRequestURL() + (empty ? "" : "?") + (empty ? "" : queryString)
		             + "\" [" + ServletUtil.getReferer(request) + ']';
		final BugSendService service = bugSendService;
		if (service != null && shouldReport(e)) {
			String uri = request.getRequestURI();
			SpringUtil.asyncRun(() -> service.sendBugMsg(uri, e), "钉钉告警时出错");
		}
		log.error(msg, e);
	}

	/**
	 * 是否应该上报异常告警：当异常为 ErrorMessageException 时，如果 report = false，则不上报
	 */
	static boolean shouldReport(Throwable e) {
		if (e instanceof ErrorMessageException) {
			return ((ErrorMessageException) e).isReport();
		}
		e = e.getCause();
		return !(e instanceof ErrorMessageException) || ((ErrorMessageException) e).isReport();
	}

	/** 此处必须返回 NOT NULL，否则会被忽略 */
	@Nonnull
	protected ModelAndView response(HttpServletRequest request, HttpServletResponse response, int statusCode, @Nonnull Messager<?> msger) {
		if (statusCode > 0 && request.getHeader(Context.internal().getAppIdHeaderName()) != null) {
			response.setStatus(statusCode);
		}
		if (StringUtil.startsWith(msger.getMsg(), '@')) {
			msger.setMsg(I18N.msg(msger.getMsg()));
		}
		request.setAttribute(Logs.RESPONSE, msger);
		if (shouldOutputJSON(request)) { // 如果是AJAX请求，将错误信息以JSON格式响应回去
			return new ModelAndView(FastJsonView.INSTANCE, FastJsonView.JSON_KEY, msger);
		}
		request.setAttribute("messager", msger);
		Object extra = msger.getExt();
		ModelAndView view = null;
		String remark = null;
		if (extra != null) {
			if (extra instanceof ModelAndView) {
				view = (ModelAndView) extra;
			} else if (extra instanceof CharSequence) {
				remark = (String) extra;
				final String prefix = getViewNamePrefix();
				if (remark.startsWith(prefix)) {
					view = new ModelAndView(remark.substring(prefix.length()));
					msger.setExt(remark = null);
				}
			}
		}
		if (StringUtil.isEmpty(remark)) {
			remark = "The system has recorded the fault. Perform the operation as prompted.";
		}
		request.setAttribute(errorMessageAttr, Context.get().escapeHtml(msger.getMsg()));
		request.setAttribute("remark", remark);
		if (view == null) {
			view = new ModelAndView();
		}
		if (view.getViewName() == null && view.getView() == null) {
			final boolean fromBackstage = Context.startsWithAdminPath(request);
			prepareCommonAttr(request, fromBackstage);
			String viewName = "/common/error.peb";
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
			if (e instanceof ValidationException && e.getCause() instanceof Exception) {
				e = (Exception) e.getCause();
			}
			final Messager<?> msger;
			if (e instanceof ErrorMessageException) {
				msger = ((ErrorMessageException) e).getMessager();
			} else {
				msger = new Messager<>();
				String errorMsg = tryHideDetail(e);
				if (errorMsg == null) {
					errorMsg = tryAsIllegal(e);
				}
				if (StringUtil.isEmpty(errorMsg)) {
					errorMsg = e.getLocalizedMessage();
				}
				msger.setMsg(X.expectNotEmpty(errorMsg, "System error, please try again later!")).setERROR();
			}
			return msger;
		}

		/** 这些异常需要统一对外提示"网络异常" */
		protected Class<?>[] networkExceptionTypes = toClasses(
				IllegalMonitorStateException.class, // Redis 分布式锁 unlock 异常
				"org.apache.dubbo.rpc.RpcException",
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
					.filter(Objects::nonNull)
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
				return "Network error, please try again later!";
			}
			return contains(e, hideExceptionTypes) ? BaseI18nKey.ILLEGAL_REQUEST : null;
		}

		protected boolean isDubboRemoteException(Exception e) {
			return e.getClass() == RuntimeException.class && StringUtils.contains(e.getMessage(), "at org.apache.dubbo.rpc.");
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
			           || e instanceof IllegalStateException) {
				return BaseI18nKey.ILLEGAL_REQUEST;
			}
			return null;
		}

	}

}
