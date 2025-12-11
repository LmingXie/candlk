package com.bojiu.context;

import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import com.bojiu.common.context.Context;
import com.bojiu.common.context.Env;
import com.bojiu.common.model.AnyRunnable;
import com.bojiu.common.redis.RedisUtil;
import com.bojiu.common.util.Formats;
import com.bojiu.common.util.SpringUtil;
import com.bojiu.context.model.*;
import com.bojiu.context.web.*;
import com.bojiu.webapp.base.service.CacheSyncService;
import com.bojiu.webapp.base.service.RemoteSyncService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.codeplayer.util.NumberUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.context.ServletContextAware;

@Configuration(proxyBeanMethods = false)
@ConfigurationProperties(prefix = "webapp.context")
public class ContextImpl extends Context implements ServletContextAware, CacheSyncService {

	/** 默认数值的存储精度，指示小数部分的位数 */
	public static final int SCALE = 2;

	/** 测试环境 前台默认域名 */
	public static final String testDomain = AppRegion.inAsia() ? "hnb2test.com" : "hnb1test.com";
	/** 前台默认域名 */
	public static final String frontDomain = AppRegion.inAsia() ? "hnb2user.com" : "hnb1user.com";
	/** 后台默认域名 */
	public static final String backstageDomain = AppRegion.inAsia() ? "hnb2manage.com" : "xeamanage.com";
	/** 【旧的】前台默认域名 */
	public static final String oldFrontDomain = AppRegion.inAsia() ? "hcg2mem.com" : "hnb1user.com";
	/** 【旧的】后台默认域名 */
	public static final String oldBackstageDomain = AppRegion.inAsia() ? "hcg2manage.com" : "hnb1manage.com";

	/** 本服务节点IP */
	private static String nodeIP;

	/** 根据商户ID 获取对应的本地时区 */
	public static Function<Long, TimeZone> merchantId2TimeZoneMapper;

	/** 商户ID全局上下文 */
	static final ThreadLocal<Long> merchantIdThreadLocal = ThreadLocal.withInitial(() -> {
		RequestContextImpl context = RequestContextImpl.get();
		HttpServletRequest request = context.getRequest();
		if (request != null) {
			return RequestContextImpl.getMerchantId(request);
		}
		return null;
	});

	public static final ThreadLocal<Locale> localeThreadLocal = ThreadLocal.withInitial(() -> {
		RequestContextImpl req = RequestContextImpl.get();
		return req.getRequest() == null ? null : req.getLanguage().getLocale();
	});

	// <域名, 商户ID>
	static final Cache<String, Long> domainCache = Caffeine.newBuilder()
			.initialCapacity(4)
			.maximumSize(2048)
			.expireAfterAccess(10, TimeUnit.MINUTES)
			.build();

	public ContextImpl() {
		super(true);
	}

	/** 本服务节点（内网）IP */
	public static String getNodeIP() {
		if (nodeIP == null) {
			// TODO: 2025/11/8 暂时不支持多服务
			nodeIP = /*get().getApplicationContext().getBean(NacosDiscoveryProperties.class).getIp();*/ "127.0.0.1";
		}
		return nodeIP;
	}

	protected static boolean fromBackstage;

	public static void initWithEnv(Environment env) {
		final String appName = env.getProperty("spring.application.name");
		final String namespace = env.getProperty("spring.cloud.nacos.discovery.namespace");
		AppRegion.CURRENT.assertPrefixMatch(appName, namespace);
		final MemberType type = MemberType.parse(appName);
		MemberType.CURRENT = type;
		final boolean backstage = type.asEmp();
		fromBackstage = backstage;

		if (Env.inner()) { // 只有线上环境才需要通过域名解析
			return;
		}

		if (backstage) { // 后台
			// 【平台后台】UAT 环境 => "usa.opkmanage.com"； 生产环境 =>"sa.opkmanage.com"
			// 【商户后台】UAT 环境 => "u-$mid.opkmanage.com"； 生产环境 =>"$$mid.opkmanage.com"
			// 【代理后台】UAT 环境 => "agent-u-$mid.opkmanage.com"； 生产环境 =>"agent-$$mid.opkmanage.com"
			// 【代理后台】UAT 环境 => "dealer-u-$mid.opkmanage.com"； 生产环境 =>"dealer-$$mid.opkmanage.com"
			// $$mid = 编码函数($mid)
			final boolean inUat = Env.inUat();
			RequestContextImpl.setMerchantIdParser(request -> {
				String domain = DomainUtils.doGetDomain(request);
				final boolean bg = isDefaultBackstageDomain(domain);
				if (bg || inUat && isDefaultFrontDomain(domain)) { // 如果是默认的域名，则按子域名解析区分
					if (bg) {
						domain = StringUtils.removeStart(domain, "agent-");
						domain = StringUtils.removeStart(domain, "dealer-");
					}
					final String subDomain = DomainUtils.extractSubDomain(domain);
					return DomainUtils.parseSubDomain(subDomain);
				} else { // 如果是自定义的域名，则按映射配置区分
					domain = DomainUtils.removeAgent(domain);
					domain = DomainUtils.removeDealer(domain);
					final Function<String, Long> merchantDomainLoader = s -> NumberUtil.getLong(RedisUtil.opsForHash().get(RedisKey.MERCHANT_BG_DOMAINS, s), null);
					return loadMerchantIdByDomain(request, domainCache, DomainUtils.removeWww(domain), merchantDomainLoader);
				}
			});
		} else { // 前台
			if (Env.inProduction()) {  // 生产环境 => "opkuser.com"、"g-$mid.opkuser.com"（内部测试使用）、"a.com"、"b.com" 每个主域名映射一个商户ID
				final Function<String, Long> merchantDomainLoader = s -> NumberUtil.getLong(RedisUtil.opsForHash().get(RedisKey.MERCHANT_DOMAINS, s), null);
				RequestContextImpl.setMerchantIdParser(request -> {
					final String domain = DomainUtils.doGetRootDomain(request); // 已处理子域名前缀
					Long mid = parseInternalFront(domain);
					if (mid != null) {
						return mid;
					}
					// 传入该参数，将更新域名缓存
					return loadMerchantIdByDomain(request, domainCache, domain, merchantDomainLoader);
				});
			} else { // UAT 环境 => "u-$mid.opkuser.com"
				RequestContextImpl.setMerchantIdParser(request -> {
					final String domain = DomainUtils.doGetDomain(request);
					final String subDomain = DomainUtils.extractSubDomain(domain);
					String mid = StringUtils.removeStart(subDomain, DomainUtils.uatPrefix);
					try {
						return Long.valueOf(mid);
					} catch (NumberFormatException e) {
						throw new DomainException(domain, e);
					}
				});
			}
		}
	}

	public static boolean isDefaultFrontDomain(String domain) {
		return domain.endsWith("." + frontDomain) || domain.endsWith("." + oldFrontDomain);
	}

	public static boolean isDefaultBackstageDomain(String domain) {
		return domain.endsWith("." + backstageDomain) || domain.endsWith("." + oldBackstageDomain);
	}

	/** 解析 g-*.opkuser.com */
	@Nullable
	public static Long parseInternalFront(String domain) {
		if (domain.startsWith("g-") && isDefaultFrontDomain(domain)) {
			final String subDomain = DomainUtils.extractSubDomain(domain);
			String mid = StringUtils.removeStart(subDomain, "g-");
			try {
				return Long.valueOf(mid);
			} catch (NumberFormatException e) {
				throw new DomainException(domain, e);
			}
		}
		return null;
	}

	private static Long loadMerchantIdByDomain(HttpServletRequest request, Cache<String, Long> cache, String domain, Function<String, Long> merchantDomainLoader) {
		// 传入该参数，将更新域名缓存
		if ("flushDomain".equals(request.getParameter("cmd"))) {
			cache.invalidate(domain);
		}
		return cache.get(domain, merchantDomainLoader);
	}

	@Override
	public boolean matchPhone(String source) {
		return Country.parse(source) != null;
	}

	@Override
	public boolean fromBackstage(@Nullable HttpServletRequest request) {
		return fromBackstage;
	}

	@Override
	public FastDateFormat getDateFormat(String pattern) {
		final RequestContextImpl req = RequestContextImpl.get();
		if (req.getRequest() == null) {
			return Formats.getDateFormat(pattern);
		}
		return Formats.getDateFormat(pattern, req.getTimeZone());
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
	 * 在 附带商户ID信息 的环境下【执行】异步任务
	 */
	static void asyncRunWithMerchant(AnyRunnable task, Object errorMsg) {
		final Long merchantId = currentMerchantId();
		SpringUtil.asyncRun(() -> runWithMerchant(task, merchantId), null, SpringUtil.log, errorMsg);
	}

	/**
	 * 在 附带商户ID信息 的环境下【同步】异步任务
	 */
	public static void runWithMerchant(AnyRunnable task, final Long withMerchantId) throws Exception {
		currentMerchantId(withMerchantId); // 初始化商户ID
		try {
			task.run();
		} finally {
			removeCurrentMerchantId(); // 清除商户ID
		}
	}

	@Override
	public String getCacheId() {
		return RemoteSyncService.ContextImpl;
	}

	@Override
	public void flushCache(Object... args) {
		domainCache.invalidate((String) args[0]);
	}

}