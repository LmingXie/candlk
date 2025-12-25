package com.bojiu.context.config;

import java.util.*;
import javax.servlet.http.HttpServletResponse;

import com.bojiu.common.context.*;
import com.bojiu.common.web.CookieUtil;
import com.bojiu.common.web.mvc.*;
import com.bojiu.context.auth.*;
import com.bojiu.context.model.MemberType;
import com.bojiu.context.web.*;
import me.codeplayer.util.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.web.http.CookieSerializer;
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
@EnableSpringHttpSession // 如果需要使用Spring自带的 RedisSessionRepository，则需要更换为 @EnableRedisHttpSession
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
	public FilterRegistrationBean<RequestContextFilter> requestContextFilter(@Value("${webapp.context.appIdSecret:e1eJyulMXqtUJb1w}") String appIdSecret) {
		if (!"false".equals(appIdSecret)) { // "false" 表示不启用
			EnhanceHttpServletRequestWrapper.enableAppIdEncrypt(appIdSecret);
		}
		FilterRegistrationBean<RequestContextFilter> bean = new FilterRegistrationBean<>(new RequestContextFilter());
		bean.setName("requestContextFilter");
		bean.addUrlPatterns("/*");
		bean.setOrder(org.springframework.session.web.http.SessionRepositoryFilter.DEFAULT_ORDER + 100);
		return bean;
	}

	/**
	 * 过滤器：自定义日志级别动态调整过滤器
	 * 【注意】所有 自定义 Filter 的 order 都必须 大于 {@link org.springframework.session.web.http.SessionRepositoryFilter#DEFAULT_ORDER }
	 */
	@Bean
	public FilterRegistrationBean<CustomLogLevelFilter> customLogLevelFilter() {
		FilterRegistrationBean<CustomLogLevelFilter> bean = new FilterRegistrationBean<>(new CustomLogLevelFilter());
		bean.setName("customLogLevelFilter");
		bean.addUrlPatterns("/*");
		bean.setOrder(org.springframework.session.web.http.SessionRepositoryFilter.DEFAULT_ORDER + 102);
		return bean;
	}

	/**
	 * 过滤器：请求上下文
	 * 【注意】所有 自定义 Filter 的 order 都必须 大于 {@link org.springframework.session.web.http.SessionRepositoryFilter#DEFAULT_ORDER }
	 * <p>
	 * https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS#simple_requests
	 */
	@Bean
	public FilterRegistrationBean<CorsFilter> corsFilter() {
		final List<String> any = Collections.singletonList("*");
		final CorsConfiguration cfg = new CorsConfiguration();
		cfg.setAllowedOriginPatterns(any);
		cfg.setAllowedMethods(any);
		cfg.setAllowedHeaders(Arrays.asList(DefaultAutoLoginHandler.authorization, RequestContext.HEADER_APP_ID, "sign", "mid"));
		cfg.setAllowCredentials(Boolean.TRUE);
		cfg.setMaxAge(24 * 3600L /* 默认 1800s */);

		FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(request -> cfg));
		bean.setName("corsFilter");
		bean.addUrlPatterns("/*");
		bean.setOrder(org.springframework.session.web.http.SessionRepositoryFilter.DEFAULT_ORDER + 104);
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
					? "s6-&8*s.Z@1_4A5-"
					: "DC_^11%aTwl)5)4*";
		}
		return new DefaultAutoLoginHandler(tokenSecret);
	}

	/** 拦截器 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		final ApplicationContext context = getApplicationContext();
		registry.addInterceptor(new PermissionInterceptor(autoLoginHandler()));
		// registry.addInterceptor(context.getBean(SiteStatusCheckInterceptor.class));
		if (MemberType.fromBackstage()) {
			registry.addInterceptor(new AsyncExportInterceptor());
			registry.addInterceptor(new MerchantWhitelistCheckInterceptor());
		}
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
		// registry.addConverter(new ImageConverter());
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
	public EnhanceRedisSessionRepository sessionRepository(RedisTemplate<String, Object> redisTemplate) {
		return new EnhanceRedisSessionRepository(redisTemplate);
	}

	@Bean
	public CookieSerializer cookieSerializer(@Value("${spring.application.name}") String appName) {
		final EnhanceCookieSerializer serializer = new EnhanceCookieSerializer();
		serializer.setUseBase64Encoding(false);
		if (useHttps()) {
			serializer.setSameSite("None"); // Google Chrome 要求设置为 None 的必须带有 Secure 属性
			serializer.setUseSecureCookie(true);
		} else {
			serializer.setSameSite(null);
		}

		final MemberType type = MemberType.parse(appName);
		// serializer.setCookieName(""); // 参见 EnhanceCookieSerializer.cookieName( request ) 的动态判断
		if (type == MemberType.ADMIN) {
			serializer.setCookiePath("/api/admin"); // 只有请求后台服务时，才附带 Cookie
		} else { // 如果是用户端，使用 UID，并写在 "/api" 下
			serializer.setCookiePath("/api"); // 避免客户端请求静态资源时，也附带 Cookie，前台有多个服务，所以只能精确到 "/api"
		}

		SessionCookieUtil.cookieRootPath = "/api";
		return serializer;
	}

	public static boolean useHttps() {
		return switch (Env.CURRENT) {
			case LOCAL, DEV -> false;
			default -> true;
		};
	}

	public static void setCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
		final boolean useHttps = useHttps();
		CookieUtil.setCookie(response, name, value, maxAgeSeconds, null, SessionCookieUtil.cookieRootPath, useHttps, useHttps ? "None" : null);
	}

}