package com.candlk.context;

import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.candlk.common.context.Context;
import com.candlk.common.model.AnyRunnable;
import com.candlk.common.util.SpringUtil;
import com.candlk.context.model.Country;
import com.candlk.context.model.MemberType;
import com.candlk.context.web.RequestContextImpl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.ServletContextAware;

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "webapp.context")
public class ContextImpl extends Context implements ServletContextAware {

	/** 商户ID全局上下文 */
	static final ThreadLocal<Long> merchantIdThreadLocal = ThreadLocal.withInitial(() -> {
		RequestContextImpl context = RequestContextImpl.get();
		HttpServletRequest request = context.getRequest();
		if (request != null) {
			return RequestContextImpl.getMerchantId(request);
		}
		return null;
	});

	public ContextImpl() {
		super(true);
	}

	protected boolean fromBackstage;

	@Override
	public boolean matchPhone(String source) {
		return Country.parse(source) != null;
	}

	@Override
	public boolean fromBackstage(@Nullable HttpServletRequest request) {
		return fromBackstage;
	}

	@Override
	public void setServletContext(@Nonnull ServletContext sc) {
		setGlobalServletContext(sc);
	}

	/** 获取当前商户ID */
	public static Long currentMerchantId() {
		return merchantIdThreadLocal.get();
	}

	/** 设置 获取当前商户ID */
	public static void currentMerchantId(Long merchantId) {
		merchantIdThreadLocal.set(merchantId);
	}

	/** 移除  当前线程的 商户ID */
	public static void removeCurrentMerchantId() {
		merchantIdThreadLocal.remove();
	}

	/**
	 * 在 附带商户ID信息 的环境下执行异步任务
	 */
	public static void asyncRunWithMerchant(String errorMsg, AnyRunnable task) {
		asyncRunWithMerchant(task, errorMsg);
	}

	/**
	 * 在 附带商户ID信息 的环境下执行异步任务
	 */
	public static void asyncRunWithMerchant(Supplier<CharSequence> errorMsg, AnyRunnable task) {
		asyncRunWithMerchant(task, errorMsg);
	}

	/**
	 * 在 附带商户ID信息 的环境下执行异步任务
	 */
	static void asyncRunWithMerchant(AnyRunnable task, Object errorMsg) {
		final Long merchantId = currentMerchantId();
		SpringUtil.asyncRun(() -> {
			currentMerchantId(merchantId); // 初始化商户ID
			try {
				task.run();
			} finally {
				removeCurrentMerchantId(); // 清除商户ID
			}
		}, null, SpringUtil.log, errorMsg);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		final MemberType type = MemberType.parse(applicationName());
		MemberType.CURRENT = type;
		fromBackstage = type.asEmp();
	}

}
