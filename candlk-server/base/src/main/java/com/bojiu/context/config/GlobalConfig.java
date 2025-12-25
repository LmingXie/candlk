package com.bojiu.context.config;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler;
import com.bojiu.common.context.Context;
import com.bojiu.common.dao.EnhanceMybatisPlusInterceptor;
import com.bojiu.common.dao.FastPaginationInnerInterceptor;
import com.bojiu.common.util.SpringUtil;
import com.bojiu.context.ContextImpl;
import com.bojiu.context.model.*;
import com.bojiu.context.web.RequestContextImpl;
import com.bojiu.webapp.base.entity.Merchant;
import com.bojiu.webapp.base.service.*;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.ArrayUtil;
import me.codeplayer.util.X;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import org.apache.dubbo.config.annotation.DubboService;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @see com.bojiu.common.dao.MyBatisConfiguration
 * @see com.bojiu.common.redis.RedisConfiguration
 */
@Configuration
@MapperScan(Context.BASE_DAO_PACKAGE)
@Slf4j
public class GlobalConfig {

	@Bean(name = SpringUtil.TASK_EXECUTOR_BEAN_NAME)
	@Lazy
	public TaskExecutor defaultTaskExecutor() {
		ThreadPoolTaskExecutor pool = new ThreadPoolTaskExecutor();
		pool.setThreadNamePrefix("Task-Pool-");
		int coreCount = Runtime.getRuntime().availableProcessors();
		int min = Math.max(coreCount, 4);
		pool.setCorePoolSize(min);
		// 需要考虑 任务 的IO类型
		pool.setMaxPoolSize(coreCount * 2 + 4);
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

	@Bean
	public EnhanceMybatisPlusInterceptor mybatisPlusInterceptor() {
		EnhanceMybatisPlusInterceptor interceptor = new EnhanceMybatisPlusInterceptor();
		// 基于表内行级字段的 多租户 插件
		// 使用自定义的多租户插件，以支持 InExpression
		interceptor.addInnerInterceptor(new com.bojiu.context.auth.TenantLineInnerInterceptor(new TenantLineHandler() {
			@Override
			public String getTenantIdColumn() {
				return WithMerchant.MERCHANT_ID;
			}

			@Override
			public Expression getTenantId() {
				if (MemberType.fromBackstage()) {
					final RequestContextImpl req = RequestContextImpl.get();
					final Member emp = req.sessionUser();
					if (emp != null) {
						Long merchantId = req.getMerchantId();
						if (merchantId.equals(emp.getMerchantId())) {
							merchantId = RequestContextImpl.getSelectedMerchantId(req.getRequest());
						}
						if (merchantId != null) { // 如果传入了商户ID
							return emp.type() != MemberType.AGENT ? new LongValue(merchantId) : new AndExpression(new EqualsTo().withRightExpression(new LongValue(merchantId)), new EqualsTo().withRightExpression(new LongValue(emp.getTopUserId())));
						}
						if (!emp.asAdmin()) {
							final Set<Long> merchantIds = emp.merchantIds();
							final InExpression inExp = new InExpression().withRightExpression(
									new ParenthesedExpressionList<>(X.isValid(merchantIds) ? ArrayUtil.toArray(merchantIds, LongValue.class, LongValue::new) : new LongValue[] {}));
							if (emp.type() != MemberType.AGENT) {
								return inExp;
							}
							return new AndExpression(inExp, new EqualsTo().withRightExpression(new LongValue(emp.getTopUserId())));
						}
					}
				}
				return new LongValue(ContextImpl.currentMerchantId());
			}

			@Override
			public boolean ignoreTable(String tableName) {
				Long merchantId = ContextImpl.currentMerchantId();
				if (merchantId == null || Merchant.isPlatform(merchantId)) { // 异步线程 或 当前是平台，默认不自动加商户ID约束
					return true;
				}
				TableInfo tableInfo = TableInfoHelper.getTableInfo(tableName);
				return tableInfo == null || !WithMerchant.class.isAssignableFrom(tableInfo.getEntityType());
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
		interceptor.addInnerInterceptor(new FastPaginationInnerInterceptor(DbType.MYSQL));
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
	 * 分布式定时任务 XXL-JOB 配置
	 */
	@ConditionalOnProperty("xxl-job.admin-addresses")
	@ConfigurationProperties(prefix = "xxl-job")
	@Bean
	public XxlJobSpringExecutor xxlJobExecutor() {
		log.info(">>>>>>>>>>> xxl-job config init.");
		return new XxlJobSpringExecutor();
	}

	/**
	 * 本地缓存跨服务同步刷新支持配置
	 * TODO 需要进行远程同步时，需先进行注册
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnBean(CacheSyncService.class)
	static class RemoteSyncServiceConfig {

		static RemoteSyncService createRemoteSyncServiceIfNecessary(List<CacheSyncService> cacheSyncServiceList) {
			return new RemoteSyncServiceImpl(cacheSyncServiceList);
		}

		@ConditionalOnClass(name = "com.bojiu.webapp.UserApplication")
		@Bean(name = "remoteCacheSyncService")
		// @DubboService(group = RemoteSyncService.USER) // 注入Dubbo远程服务，同时标记为USER分组
		public RemoteSyncService userRemoteCacheSyncService(List<CacheSyncService> cacheSyncServiceList/*全部CacheSyncService实现*/) {
			return createRemoteSyncServiceIfNecessary(cacheSyncServiceList);
		}

		// 	@ConditionalOnClass(name = "com.bojiu.webapp.TradeApplication")
		// 	@Bean(name = "remoteCacheSyncService")
		// 	@DubboService(group = RemoteSyncService.TRADE)
		// 	public RemoteSyncService tradeRemoteSyncService(List<CacheSyncService> cacheSyncServiceList) {
		// 		return createRemoteSyncServiceIfNecessary(cacheSyncServiceList);
		// 	}
		//
		// 	@ConditionalOnClass(name = "com.bojiu.webapp.GameApplication")
		// 	@Bean(name = "remoteCacheSyncService")
		// 	@DubboService(group = RemoteSyncService.GAME)
		// 	public RemoteSyncService gameRemoteSyncService(List<CacheSyncService> cacheSyncServiceList) {
		// 		return createRemoteSyncServiceIfNecessary(cacheSyncServiceList);
		// 	}
		//
		// 	@ConditionalOnClass(name = "com.bojiu.webapp.AdminApplication")
		// 	@Bean(name = "remoteCacheSyncService")
		// 	@DubboService(group = RemoteSyncService.ADMIN)
		// 	public RemoteSyncService adminRemoteSyncService(List<CacheSyncService> cacheSyncServiceList) {
		// 		return createRemoteSyncServiceIfNecessary(cacheSyncServiceList);
		// 	}

	}

}