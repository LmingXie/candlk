package com.candlk.context.config;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.candlk.common.alarm.dingtalk.CacheBugWarnExpiredService;
import com.candlk.common.context.Context;
import com.candlk.common.util.SpringUtil;
import com.candlk.context.ContextImpl;
import com.candlk.context.model.WithMerchant;
import com.candlk.webapp.base.service.*;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.schema.Column;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.session.data.redis.config.ConfigureRedisAction;

/**
 * @see com.candlk.common.dao.MyBatisConfiguration
 * @see com.candlk.common.redis.RedisConfiguration
 */
@Configuration
@MapperScan(Context.BASE_DAO_PACKAGE)
@Slf4j
public class GlobalConfig {

	/** 为 0 的商户ID，则表示平台 */
	public static final Long PLATFORM_MERCHANT_ID = 0L;

	@ConditionalOnProperty(name = "warn.service.impl", havingValue = "caffeine")
	@Bean
	public CacheBugWarnExpiredService bugWarnExpiredService() {
		final CaffeineCacheManager cacheManager = new CaffeineCacheManager();
		final Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
		                                                  .initialCapacity(32)
		                                                  .maximumSize(512)
		                                                  .expireAfterWrite(60, TimeUnit.SECONDS);
		cacheManager.setCaffeine(caffeine);
		return new CacheBugWarnExpiredService(cacheManager);
	}

	@Bean(name = SpringUtil.TASK_EXECUTOR_BEAN_NAME)
	@Lazy
	public TaskExecutor defaultTaskExecutor() {
		ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
		pool.setThreadNamePrefix("Task-Pool-");
		int coreCount = Runtime.getRuntime().availableProcessors();
		int min = Math.max(coreCount >> 1, 1);
		pool.setCorePoolSize(min);
		// 需要考虑 任务 的IO类型
		pool.setMaxPoolSize(coreCount * 2 + 2);
		pool.setQueueCapacity(1024);
		pool.setKeepAliveSeconds(300);
		pool.setWaitForTasksToCompleteOnShutdown(true);
		pool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		return pool;
	}

	/**
	 * 国际化配置
	 */
	@Bean
	public ResourceBundleMessageSource messageSource() {
		final ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
		ms.setBasenames("base", "messages");
		ms.setUseCodeAsDefaultMessage(true);
		ms.setFallbackToSystemLocale(false);
		ms.setDefaultEncoding("UTF-8");
		return ms;
	}

	/**
	 * 避免 阿里云 Redis 7.0 缺失 config get 命令导致启动出错
	 *
	 * @see org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration.EnableRedisKeyspaceNotificationsInitializer#afterPropertiesSet()
	 */
	@Bean
	public ConfigureRedisAction configureRedisAction() {
		return ConfigureRedisAction.NO_OP;
	}

	@Bean
	public MybatisPlusInterceptor mybatisPlusInterceptor() {
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
		// 基于表内行级字段的 多租户 插件
		interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler() {
			@Override
			public String getTenantIdColumn() {
				return WithMerchant.MERCHANT_ID;
			}

			@Override
			public Expression getTenantId() {
				return new LongValue(ContextImpl.currentMerchantId());
			}

			@Override
			public boolean ignoreTable(String tableName) {
				Long merchantId = ContextImpl.currentMerchantId();
				if (merchantId == null || PLATFORM_MERCHANT_ID.equals(merchantId)) { // 异步线程 或 当前是平台，默认不自动加商户ID约束
					return true;
				}
				TableInfo tableInfo = TableInfoHelper.getTableInfo(tableName);
				return !WithMerchant.class.isAssignableFrom(tableInfo.getEntityType());
			}

			@Override
			public boolean ignoreInsert(List<Column> columns, String tenantIdColumn) {
				for (Column col : columns) {
					if (tenantIdColumn.equals(col.getColumnName())) {
						return true;
					}
				}
				return false;
			}
		}));
		// 分页插件
		interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
		return interceptor;
	}

	// 静态注入缓存处理类
	/* 开启后，需要引入更多的依赖，且需要添加更多的 add-opens
	static {
		// 默认支持序列化 FstSerialCaffeineJsqlParseCache、JdkSerialCaffeineJsqlParseCache
		JsqlParserGlobal.setJsqlParseCache(new FstSerialCaffeineJsqlParseCache(cache -> cache.maximumSize(1024).expireAfterAccess(1, TimeUnit.MINUTES)));
	}
	*/


	/**
	 * 本地缓存跨服务同步刷新支持配置
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnBean(CacheSyncService.class)
	static class RemoteSyncServiceConfig {

		static RemoteSyncService createRemoteSyncServiceIfNecessary(List<CacheSyncService> cacheSyncServiceList) {
			return new RemoteSyncServiceImpl(cacheSyncServiceList);
		}

	}

}
