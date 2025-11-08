package com.bojiu.context;

import java.util.regex.Pattern;

import com.bojiu.common.context.*;
import com.bojiu.common.model.Messager;
import com.bojiu.common.redis.RedisUtil;
import com.bojiu.common.validator.ValidateError;
import com.bojiu.common.validator.ValidateHelper;
import com.bojiu.common.web.ServletUtil;
import com.bojiu.context.model.BaseI18nKey;
import com.bojiu.context.model.MessagerStatus;
import com.bojiu.context.web.Jsons;
import com.bojiu.context.web.RequestContextImpl;
import me.codeplayer.util.StringUtil;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public abstract class BaseApplication {

	/**
	 * 可配置项
	 *
	 * @see AppRegion#CURRENT 应用所在地域
	 * @see com.bojiu.context.model.Country 国家（地区）/手机号码
	 * @see com.bojiu.context.model.Currency 货币
	 * @see AppRegion#assertPrefixMatch(String, String)  应用名称前缀
	 * @see ContextImpl#frontDomain 前台演示域名
	 * @see ContextImpl#backstageDomain 后台演示域名
	 */
	protected static ConfigurableApplicationContext startup(Class<?> primarySource, String[] args) {
		ServletUtil.setClientIpHeaderCandidates("cf-connecting-ip", "x-forwarded-for", "x-real-ip");
		com.alibaba.fastjson2.JSON.config(com.alibaba.fastjson2.JSONWriter.Feature.BrowserCompatible);
		// 指定 Nacos 的日志配置，否则会输出 "No Root logger was configured, creating default ERROR-level Root logger with Console appender"
		System.setProperty("nacos.logging.config", "classpath:log4j2-spring.xml");
		// 可参见：com.alibaba.nacos.client.logging.NacosLogging、AbstractNacosLogging
		// 全局启用 Log4j2 异步日志，可参见 https://logging.apache.org/log4j/2.x/manual/async.html
		System.setProperty("log4j2.contextSelector", "org.apache.logging.log4j.core.async.BasicAsyncLoggerContextSelector");
		// 当异步队列满时的处理策略，默认"Default"表示阻塞等待，可酌情改为丢弃"Discard"。将会丢弃不高于 log4j2.discardThreshold 配置级别的日志
		System.setProperty("log4j2.asyncQueueFullPolicy", "Discard");
		// 队列满时，丢弃策略可丢弃的最高日志级别，默认 INFO（将丢弃 INFO、DEBUG、TRACE 等级别）
		// System.setProperty("log4j2.discardThreshold", "INFO");
		// 明确禁用 Log4j2 的位置信息，异步日志默认即为 false（同步默认为 true）
		System.setProperty("log4j2.includeLocation", "false");

		System.setProperty("jdk.tls.ephemeralDHKeySize", "1024"); // JDK 17.0.9 调整 TLSv1.2 默认值为 2048
		System.setProperty("dubbo.application.qos-enable", "false"); // 禁止 Dubbo 的 QoS Server 服务
		// 国际化信息 初始化设置
		I18N.setLocaleSupplier(() -> RequestContextImpl.get().getLanguage().getLocale());
		// 自定义 用户名验证 的正则表达式
		Matcher.DefaultMatcher.INSTANCE.regexUsername = Pattern.compile("^(?=.*[a-zA-Z])[a-zA-Z0-9_]{4,16}$", Pattern.CASE_INSENSITIVE);
		ValidateHelper.PHONE_MATCHER = phone -> Context.get().matchPhone(phone);
		ValidateHelper.REAL_NAME_MATCHER = StringUtil::notBlank;
		ServletUtil.JSON_CONVERTER = Jsons::encode;
		/* 校验用户名的正则表达式：4~16个字符,只允许字母、数字、下划线 */
		Matcher.DefaultMatcher.INSTANCE.regexUsername = Pattern.compile("^[a-zA-Z0-9_]{4,16}$", Pattern.CASE_INSENSITIVE);
		// 表单验证 国际化 初始化设置
		ValidateError.setErrorResolver(I18N::msg);
		// Redis 分布式锁 快速返回的默认提示设置
		RedisUtil.setDefaultValueLoader(() -> Messager.status(MessagerStatus.BUSY, I18N.msg(BaseI18nKey.REQUEST_TOO_FAST)));
		// 在 ContextImpl 被 Spring 构造之前，默认是 Context，在这里直接修改掉默认 Hook，否则之前的静态方法调用无法使用 ContextImpl 的重载方法
		Context.setInstance(new ContextImpl());
		return SystemInitializer.watchError("应用启动失败", () ->
				configureInternal(new SpringApplicationBuilder(primarySource))
						.build()
						.run(args)
		);
	}

	protected static SpringApplicationBuilder configureInternal(SpringApplicationBuilder builder) {
		return builder;
	}

}