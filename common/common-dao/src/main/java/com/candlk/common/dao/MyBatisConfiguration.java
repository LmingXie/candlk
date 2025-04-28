package com.candlk.common.dao;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.Properties;
import javax.sql.DataSource;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableTransactionManagement(proxyTargetClass = true)
public class MyBatisConfiguration {

	@Bean
	@ConditionalOnMissingBean(MybatisPlusInterceptor.class)
	public MybatisPlusInterceptor mybatisPlusInterceptor() {
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
		// 分页插件
		interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
		return interceptor;
	}

	/** 【默认】单一数据源 */
	@Bean
	@ConditionalOnExpression("'${spring.datasource.names:}' == ''")
	@ConditionalOnMissingBean
	@ConfigurationProperties(prefix = "spring.datasource")
	public SpringHikariDataSource dataSource() {
		log.info("当前实例正在使用【默认】单一数据源……");
		return createDataSource();
	}

	/** 多数据源 */
	@Getter
	@Setter
	@Configuration
	@ConditionalOnProperty(name = "spring.datasource.names")
	@ConfigurationProperties("spring.datasource")
	public static class MultiDataSourceConfig {

		SpringHikariDataSource master;
		SpringHikariDataSource slave;

		@Bean
		@Primary // 自动装配时当出现多个Bean候选者时，被注解为 @Primary 的 Bean 将作为首选者，否则将抛出异常
		public DataSource dataSource() {
			final DataSourceSelector selector = new DataSourceSelector();
			EnumMap<DataSourceType, DataSource> dataSourceMap = new EnumMap<>(DataSourceType.class);
			dataSourceMap.put(DataSourceType.WRITE, master);
			// 尽量继承 master 的配置，并可以被 slave 重写
			// 线程池大小相关参数不能继承
			dataSourceMap.put(DataSourceType.READ, slave.init(master));
			// 将 READ 数据源作为默认指定的数据源
			selector.setDefaultTargetDataSource(dataSourceMap.get(DataSourceType.DEFAULT));
			// 将 READ 和 WRITE 数据源作为指定的数据源
			selector.setDataSourceMap(dataSourceMap);
			log.info("当前实例初始化多数据源成功……");
			return selector;
		}

		@Bean
		public PlatformTransactionManager transactionManager() {
			// 配置事务管理, 使用事务时在方法头部添加 @Transactional 注解即可
			return new DataSourceTransactionManager(dataSource());
		}

		@Slf4j
		@Aspect
		@Order(-1)  // 该切面应当先于 @Transactional 执行
		@Component
		@ConditionalOnProperty(name = "spring.datasource.names")
		public static class DynamicDataSourceAspect {

			/**
			 * 定义拦截规则：拦截 com.candlk..service 包下面的所有类中。
			 */
			@Pointcut("@annotation(DataSource) || @annotation(org.springframework.transaction.annotation.Transactional) || execution(public * com.candlk..*Service*.*(..))")
			public void pointcut() {
			}

			/**
			 * 切换数据源
			 */
			@Before("pointcut()")
			public void switchDataSource(JoinPoint point) {
				final MethodSignature sign = (MethodSignature) point.getSignature();
				final Method method = sign.getMethod();
				com.candlk.common.dao.DataSource ds = method.getAnnotation(com.candlk.common.dao.DataSource.class);
				DataSourceType current;
				if (ds != null) {
					DataSourceSelector.resetAs(current = ds.value());
				} else {
					current = DataSourceSelector.getCurrent();
					if (current != DataSourceType.WRITE) {
						if (TransactionSynchronizationManager.isActualTransactionActive() || txRequired(method)) {
							DataSourceSelector.resetAs(current = DataSourceType.WRITE);
						}
					}
				}

				DataSourceSelector.debug(current, method);
			}

			static boolean txRequired(final Method method) {
				Transactional trans = method.getAnnotation(Transactional.class);
				if (trans == null) {
					trans = AnnotationUtils.getAnnotation(method.getDeclaringClass(), Transactional.class);
				}
				return trans != null && switch (trans.propagation()) {
					case REQUIRED, REQUIRES_NEW, MANDATORY, NESTED -> true;
					default -> false;
				};
			}

		}

	}

	static SpringHikariDataSource createDataSource() {
		return new SpringHikariDataSource();
	}

	public static class SpringHikariDataSource extends HikariDataSource {

		public SpringHikariDataSource() {
		}

		public SpringHikariDataSource init(HikariDataSource base) {
			this.setDriverClassName(this.getDriverClassName() == null ? base.getDriverClassName() : this.getDriverClassName());
			this.setJdbcUrl(this.getJdbcUrl() == null ? base.getJdbcUrl() : this.getJdbcUrl());
			this.setUsername(this.getUsername() == null ? base.getUsername() : this.getUsername());
			this.setPassword(this.getPassword() == null ? base.getPassword() : this.getPassword());
			this.setAutoCommit(base.isAutoCommit());
			this.setConnectionTimeout(base.getConnectionTimeout());
			this.setConnectionTestQuery(base.getConnectionTestQuery());
			this.setIdleTimeout(base.getIdleTimeout());
			this.setMaxLifetime(base.getMaxLifetime());
			this.setKeepaliveTime(base.getKeepaliveTime());
			this.setDataSourceProperties(new Properties(base.getDataSourceProperties()));
			return this;
		}

		public void setUrl(String url) {
			super.setJdbcUrl(url);
		}

	}

	/*
	@Aspect
	@Configuration
	@EnableTransactionManagement(order = 0, proxyTargetClass = true)
	public static class TxAdviceConfig {

		static final String AOP_POINTCUT_EXPRESSION = "execution(public * @org.springframework.stereotype.Service com.candlk..*.*(..))";

		@Bean
		public DataSourceTransactionManager txManager(DataSource dataSource) {
			return new DataSourceTransactionManager(dataSource);
		}

		@Bean
		@Order(2)
		public TransactionInterceptor txAdvice(TransactionManager txManager) {
			// 只读事务，不做更新操作
			RuleBasedTransactionAttribute readOnlyTx = new RuleBasedTransactionAttribute();
			readOnlyTx.setReadOnly(true);
			readOnlyTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
			// 当前存在事务就使用当前事务，当前不存在事务就创建一个新的事务
			RuleBasedTransactionAttribute requiredTx = new RuleBasedTransactionAttribute();
			requiredTx.setRollbackRules(Collections.singletonList(new RollbackRuleAttribute(Exception.class)));
			requiredTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
			// requiredTx.setTimeout(5);

			Map<String, TransactionAttribute> txMap = new HashMap<>();
			txMap.put("get*", readOnlyTx);
			txMap.put("find*", readOnlyTx);
			txMap.put("*", requiredTx);
			NameMatchTransactionAttributeSource source = new NameMatchTransactionAttributeSource();
			source.setNameMap(txMap);

			return new TransactionInterceptor(txManager, source);
		}

		@Bean
		@ConfigurationProperties(prefix = "spring.tx")
		public DefaultPointcutAdvisor txAdviceAdvisor(TransactionInterceptor txAdvice) {
			AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
			pointcut.setExpression(AOP_POINTCUT_EXPRESSION);
			return new DefaultPointcutAdvisor(pointcut, txAdvice);
		}

	}
	*/

}
