package com.candlk.context;

import com.candlk.common.context.Context;
import com.candlk.common.context.I18N;
import com.candlk.common.model.Messager;
import com.candlk.common.redis.RedisUtil;
import com.candlk.common.validator.ValidateError;
import com.candlk.common.validator.ValidateHelper;
import com.candlk.context.model.BaseI18nKey;
import com.candlk.context.model.MessagerStatus;
import com.candlk.context.web.RequestContextImpl;
import me.codeplayer.util.StringUtil;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public abstract class BaseApplication {

	protected static ConfigurableApplicationContext startup(Class<?> primarySource, String[] args) {
		System.setProperty("dubbo.application.qos-enable", "false"); // 禁止 Dubbo 的 QoS Server 服务
		System.setProperty("nacos.logging.config", "classpath:log4j2-spring.xml");
		// 国际化信息 初始化设置
		I18N.setLocaleSupplier(() -> RequestContextImpl.get().sessionLanguage().getLocale());
		ValidateHelper.PHONE_MATCHER = phone -> Context.get().matchPhone(phone);
		ValidateHelper.REAL_NAME_MATCHER = StringUtil::notBlank;
		// 表单验证 国际化 初始化设置
		ValidateError.setErrorResolver(I18N::msg);
		// Redis 分布式锁 快速返回的默认提示设置
		RedisUtil.setDefaultValueLoader(() -> Messager.status(MessagerStatus.BUSY, I18N.msg(BaseI18nKey.REQUEST_TOO_FAST)));
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
