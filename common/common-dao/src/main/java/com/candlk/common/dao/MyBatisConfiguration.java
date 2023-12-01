package com.candlk.common.dao;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.zaxxer.hikari.HikariDataSource;
import me.codeplayer.util.X;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ DataSourceProperties.class })
@EnableTransactionManagement(proxyTargetClass = true)
public class MyBatisConfiguration {

	@Bean
	@ConditionalOnExpression("'${spring.shardingsphere.datasource.names:}' == ''")
	@ConditionalOnMissingBean
	@ConfigurationProperties(prefix = "spring.datasource.hikari")
	public HikariDataSource dataSource(DataSourceProperties props) {
		HikariDataSource ds = new HikariDataSource();
		X.use(props.getDriverClassName(), ds::setDriverClassName);
		X.use(props.getUrl(), ds::setJdbcUrl);
		X.use(props.getUsername(), ds::setUsername);
		X.use(props.getPassword(), ds::setPassword);
		return ds;
	}

	@Bean
	@ConditionalOnMissingBean(MybatisPlusInterceptor.class)
	public MybatisPlusInterceptor mybatisPlusInterceptor() {
		MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
		// 分页插件
		interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
		return interceptor;
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
