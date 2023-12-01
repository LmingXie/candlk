package com.candlk.common.context;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import lombok.Getter;
import lombok.Setter;
import me.codeplayer.util.X;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

/**
 * 公共的常量类，用于存储程序中需要使用的常量
 *
 * @since 2014年12月25日
 */
@Getter
@Setter
public class Context extends AppHook implements ContextConfigurer, InitializingBean {

	public static final String COOKIE_CLIENT_ID = "cid";
	public static final String BASE_PACKAGE = "com.candlk";
	public static final String BASE_DAO_PACKAGE = BASE_PACKAGE + ".**.dao";
	public static final Logger LOGGER = LoggerFactory.getLogger(Context.class);
	//
	protected Navigator nav = new Navigator();
	protected InternalConfig internal = new InternalConfig();
	protected final ConcurrentMap<String, Object> external = new ConcurrentHashMap<>(64);

	public Context(boolean asGlobal) {
		if (LOGGER.isTraceEnabled()) {
			// 添加堆栈信息，便于跟踪调用的方法
			LOGGER.trace(getClass().getName() + " 初始化…………以下是相关堆栈信息：", new Throwable());
		} else {
			LOGGER.info("{} 初始化……", getClass().getName());
		}
		preHandle();
		if (asGlobal) {
			instance = this;
		}
	}

	public Context() {
		this(false);
	}

	//
	/** 全局 ServletContext Hook */
	@Getter
	protected static ServletContext servletContext;

	public static void setGlobalServletContext(@Nonnull ServletContext sc) {
		servletContext = sc;
	}

	public static Context getInstance() {
		return (Context) AppHook.getInstance(Context.class);
	}

	@Override
	public void servletContextInitialized(ServletContext sc) {
		ContextConfigurer.super.servletContextInitialized(sc);
		getNav().initWithServletContext(sc);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Context> T get() {
		return (T) AppHook.getInstance(Context.class);
	}

	public static Navigator nav() {
		return get().getNav();
	}

	public static InternalConfig internal() {
		return get().getInternal();
	}

	public static ConcurrentMap<String, Object> external() {
		return get().getExternal();
	}

	public static <T> T getBean(Class<T> requiredType) throws BeansException {
		return get().getApplicationContext().getBean(requiredType);
	}

	public static <T> T getBean(Class<T> requiredType, @Nullable Supplier<T> defaultBeanLoader) {
		ApplicationContext context = get().getApplicationContext();
		String[] names = context.getBeanNamesForType(requiredType);
		if (X.isValid(names)) {
			return context.getBean(requiredType);
		}
		return defaultBeanLoader == null ? null : defaultBeanLoader.get();
	}

	public static Environment getEnv() {
		return get().getApplicationContext().getEnvironment();
	}

	/** 获取 Spring Boot 应用的应用名称 */
	public static String applicationName() {
		return getEnv().getProperty("spring.application.name");
	}

	public static boolean startsWithRelativePath(HttpServletRequest request, String path) {
		return request.getRequestURI().startsWith(path, request.getContextPath().length());
	}

	/**
	 * 指示当前请求的目标路径是否是后台路径
	 */
	public static boolean startsWithAdminPath(HttpServletRequest request) {
		return startsWithRelativePath(request, nav().getAdminPath());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		X.use(getServletContext(), this::servletContextInitialized);
		getInternal().init();
		postHandle();
	}

}
