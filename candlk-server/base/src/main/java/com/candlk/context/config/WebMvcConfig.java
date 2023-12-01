package com.candlk.context.config;

import java.util.*;
import javax.servlet.http.HttpServletRequest;

import com.candlk.common.context.*;
import com.candlk.common.web.mvc.*;
import com.candlk.context.auth.*;
import com.candlk.context.web.*;
import me.codeplayer.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.*;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.RequestToViewNameTranslator;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.function.support.RouterFunctionMapping;
import org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping;
import org.springframework.web.servlet.resource.ResourceUrlProvider;

@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 600) // 启用基于 Servlet 的 Spring Session
@EnableConfigurationProperties(CryptoSuiteContext.class)
public class WebMvcConfig extends DelegatingWebMvcConfiguration {

	@Override
	public BeanNameUrlHandlerMapping beanNameHandlerMapping(FormattingConversionService conversionService, ResourceUrlProvider resourceUrlProvider) {
		return null;
	}

	@Override
	public RouterFunctionMapping routerFunctionMapping(FormattingConversionService conversionService, ResourceUrlProvider resourceUrlProvider) {
		return null;
	}

	/**
	 * 请求路径匹配逻辑定制
	 */
	@Override
	protected void configurePathMatch(PathMatchConfigurer configurer) {
		super.configurePathMatch(configurer);
		// "/user/" 和 "/user" 【不】视为相同的路径
		configurer.setUseTrailingSlashMatch(false);
	}

	/**
	 * 过滤器：请求上下文
	 * 【注意】所有 自定义 Filter 的 order 都必须 大于 {@link org.springframework.session.web.http.SessionRepositoryFilter#DEFAULT_ORDER }
	 */
	@Bean
	public FilterRegistrationBean<RequestContextFilter> requestContextFilter(@Value("${webapp.context.appIdSecret:BjCo1_345F78GOl0}") String appIdSecret) {
		EnhanceHttpServletRequestWrapper.enableAppIdEncrypt(appIdSecret);
		FilterRegistrationBean<RequestContextFilter> bean = new FilterRegistrationBean<>(new RequestContextFilter());
		bean.setName("requestContextFilter");
		bean.addUrlPatterns("/*");
		bean.setOrder(org.springframework.session.web.http.SessionRepositoryFilter.DEFAULT_ORDER + 100);
		return bean;
	}

	/**
	 * 过滤器：请求上下文
	 * 【注意】所有 自定义 Filter 的 order 都必须 大于 {@link org.springframework.session.web.http.SessionRepositoryFilter#DEFAULT_ORDER }
	 */
	@Bean
	public FilterRegistrationBean<CorsFilter> corsFilter() {
		final List<String> any = Collections.singletonList("*");
		final CorsConfiguration cfg = new CorsConfiguration();
		cfg.setAllowedOriginPatterns(any);
		cfg.setAllowedMethods(any);
		cfg.setAllowedHeaders(Arrays.asList("Authorization", Context.internal().getAppIdHeaderName(), "Sign", "Accept-Language", "MID"));
		cfg.setAllowCredentials(Boolean.TRUE);
		cfg.setMaxAge(24 * 3600L /* 默认 1800s */);

		FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(request -> cfg));
		bean.setName("corsFilter");
		bean.addUrlPatterns("/*");
		bean.setOrder(org.springframework.session.web.http.SessionRepositoryFilter.DEFAULT_ORDER + 101);
		return bean;
	}

	/**
	 * 过滤器：健康检查
	 * <p>
	 * 【注意】所有 自定义 Filter 的 order 都必须 大于 {@link org.springframework.session.web.http.SessionRepositoryFilter#DEFAULT_ORDER }
	 */
	@Bean
	public FilterRegistrationBean<ActuatorFilter> actuatorFilter() {
		FilterRegistrationBean<ActuatorFilter> bean = new FilterRegistrationBean<>(new ActuatorFilter());
		bean.setName("actuatorFilter");
		bean.addUrlPatterns(ActuatorFilter.URL_PATTERN);
		bean.setOrder(0);
		return bean;
	}

	@Bean
	public DefaultAutoLoginHandler autoLoginHandler() {
		// 密钥长度只能是 128（16个字节）、192（24个字节） 或 256位（32个字节）
		// 由于还存在 password 验签机制，因此密钥即使泄露也影响不大
		String tokenSecret = getApplicationContext().getEnvironment().getProperty("webapp.context.tokenSecret");
		if (StringUtil.isEmpty(tokenSecret)) {
			tokenSecret = Env.inProduction()
					? "Bj@1-2_6*&Z45+s&"
					: "DC1aT1%wl)5_^)4*";
		}
		return new DefaultAutoLoginHandler(tokenSecret);
	}

	/** 拦截器 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new UserSessionInterceptor(autoLoginHandler()));
		registry.addInterceptor(new PermissionInterceptor());
		// ExportInterceptor 已经合并到 EnhanceResponseBodyAdapter#beforeBodyWrite
		// registry.addInterceptor(new ExportInterceptor());
	}

	/**
	 * 参数转换器
	 */
	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverterFactory(new EnhanceEnumConverterFactory());
		registry.addConverter(new DateConverter());
		registry.addConverter(new ImageConverter());
	}

	/**
	 * 使用 fastjson 替换 默认的 jackson
	 */
	@Override
	protected void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		EnhanceFastJsonHttpMessageConverter converter = new EnhanceFastJsonHttpMessageConverter();
		converter.setSupportedMediaTypes(Arrays.asList(
				MediaType.APPLICATION_JSON
				, MediaType.TEXT_HTML
		));
		converters.add(converter);
		converters.add(new StringHttpMessageConverter()); // 要兼容支持 prometheus 监控
	}

	/**
	 * 处理器方法的自定义参数解析器
	 */
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new CustomHandlerMethodArgumentResolver());
	}

	/**
	 * 屏蔽默认的 DefaultRequestToViewNameTranslator。不然 返回 void 类型的 请求方法将会根据请求路径进行视图渲染
	 */
	@Override
	@Bean
	public RequestToViewNameTranslator viewNameTranslator() {
		return new NullViewNameTranslator();
	}

	/** 异常处理 */
	@Bean
	public EnhanceExceptionResolver exceptionResolver() {
		return new EnhanceExceptionResolver();
	}

	/** 获取当前环境的子域名前缀。例如：""（生产环境）、"dev."（开发环境）、"test."（测试环境） */
	public static String envSubdomainPrefix() {
		return envSubdomainPrefix(Env.CURRENT);
	}

	/** 获取指定环境的子域名前缀。例如：""（生产环境）、"dev."（开发环境）、"test."（测试环境） */
	public static String envSubdomainPrefix(Env env) {
		return env == Env.PROD ? "" : env.value + ".";
	}

	@Bean
	public HttpSessionIdResolver httpSessionIdResolver() {
		return new HeaderHttpSessionIdResolver("X-Auth-Token") {
			@Override
			public List<String> resolveSessionIds(HttpServletRequest request) {
				List<String> sessionIds = super.resolveSessionIds(request);
				if (sessionIds.isEmpty()) {
					String auth = request.getHeader(DefaultAutoLoginHandler.authorization);
					if (StringUtil.notEmpty(auth)) {
						String sessionId = StringUtils.substringBefore(auth, DefaultAutoLoginHandler.sessionIdSep);
						if (StringUtil.notEmpty(sessionId)) {
							return Collections.singletonList(sessionId);
						}
					}
				}
				return sessionIds;
			}
		};
	}

	@Bean
	public CookieSerializer cookieSerializer(@Value("spring.application.name") String appName) {
		DefaultCookieSerializer serializer = new DefaultCookieSerializer();
		if (Env.inLocal() || Env.inDev()) {
			serializer.setSameSite(null);
		} else {
			serializer.setSameSite("None");// Google Chrome 要求设置为 None 的必须带有 Secure 属性
			serializer.setUseSecureCookie(true);
		}
		String cookieName;
		if (StringUtils.contains(appName, Permission.ADMIN)) { // 平台
			cookieName = "MID";
		} else {
			cookieName = "UID";
		}
		serializer.setCookieName(cookieName);
		serializer.setCookiePath("/");
		return serializer;
	}

}
