package com.candlk.context;

import java.util.TimeZone;
import java.util.function.Supplier;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.candlk.common.context.Env;
import com.candlk.common.util.SpringUtil;
import com.candlk.context.web.RequestContextImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.springframework.boot.*;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 应用初始化监听器
 */
@Slf4j
public class SystemInitializer implements SpringApplicationRunListener {

	public static final TimeZone DEFAULT_TIME_ZONE = TimeZone.getTimeZone("GMT+8");

	public SystemInitializer(SpringApplication application, String[] args) {
		// SpringApplicationRunListener 的实现类必须有一个如此参数的构造器
	}

	@Override
	public void starting(ConfigurableBootstrapContext bootstrapContext) {
		beforeStartup();
	}

	/**
	 * 初始化 Env.CURRENT 配置
	 */
	@Override
	public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
		Env.init(environment.getActiveProfiles());
	}

	@Override
	public void contextPrepared(ConfigurableApplicationContext context) {
		SpringUtil.setGlobalApplicationContext(context);
	}

	public static void beforeStartup() {
		TimeZone.setDefault(DEFAULT_TIME_ZONE);
		JSON.configWriterDateFormat("millis"); // 全局设置 fastjson2 统一默认输出 日期 类型为 毫秒级时间戳
		JSON.config(JSONWriter.Feature.WriteEnumUsingOrdinal);
		RequestContextImpl.enable();
	}

	/**
	 * 根据运行环境对Log4j2进行配置更新
	 */
	public static void updateLog4j2Config() {
		// 正式环境，将 日志级别上调至 info，并移除 "Console" Appender
		if (!Boolean.parseBoolean(System.getProperty("debug"))) {
			final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
			final Configuration config = ctx.getConfiguration();
			if (Env.inLocal()) {
				// 本地环境 Dubbo 无法互联，所以直接屏蔽掉循环报错
				final String name = "org.apache.dubbo.remoting.transport.netty4";
				LoggerConfig loggerConfig = new LoggerConfig(name, Level.OFF, false);
				config.addLogger(name, loggerConfig);
			} else {
				for (LoggerConfig cfg : config.getLoggers().values()) {
					if (cfg.getLevel().compareTo(Level.INFO) > 0) {
						cfg.setLevel(Level.INFO);
					}
					cfg.removeAppender("Console");
				}
			}
			//
			ctx.updateLoggers();
		}
	}

	public static <T> T watchError(String errorMsg, Supplier<T> task) {
		try {
			return task.get();
		} catch (RuntimeException e) {
			log.error(errorMsg, e);
			throw e;
		} catch (Throwable e) {
			log.error(errorMsg, e);
			throw new IllegalStateException(e);
		}
	}

}
