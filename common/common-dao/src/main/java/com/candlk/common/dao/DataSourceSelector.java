package com.candlk.common.dao;

import java.lang.reflect.Method;
import java.util.Map;
import javax.annotation.Nullable;
import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.X;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

@Slf4j
public class DataSourceSelector extends AbstractRoutingDataSource {

	/** 是否已启用多数据源 */
	private static boolean enabled;

	public DataSourceSelector() {
		enabled = true;
	}

	/**
	 * 如果希望所有数据源在启动配置时就加载好，这里通过设置数据源Key值来切换数据，定制这个方法
	 */
	@Override
	protected DataSourceType determineCurrentLookupKey() {
		DataSourceType type = Holder.context.get();
		if (type == null) {
			Holder.context.set(type = DataSourceType.DEFAULT);
		}
		log.debug("数据源选择={}", type);
		return type;
	}

	/**
	 * 设置数据源
	 *
	 * @see #setTargetDataSources(Map)
	 */
	public void setDataSourceMap(Map<DataSourceType, DataSource> dataSourceMap) {
		super.setTargetDataSources(X.castType(dataSourceMap));
	}

	/**
	 * 切换数据源
	 */
	public static DataSourceType select(DataSourceType type) {
		final DataSourceType old = Holder.context.get();
		// 如果当前线程之前已经执行过 写 操作事务，则后续操作只能沿用该数据源
		if (old != DataSourceType.WRITE && old != type) {
			Holder.context.set(type);
			return type;
		}
		return old;
	}

	/**
	 * 获取数据源
	 */
	@Nullable
	public static DataSourceType getCurrent() {
		return Holder.context.get();
	}

	/**
	 * 重置数据源
	 */
	public static void reset() {
		if (enabled) { // 如果未开启，避免初始化 ThreadLocal
			Holder.context.remove();
		}
	}

	/**
	 * 重置数据源【慎重使用】
	 */
	public static void resetAs(DataSourceType type) {
		if (enabled) {  // 如果未开启，避免初始化 ThreadLocal
			Holder.context.set(type);
		}
	}

	public static void debug(DataSourceType type, Method method) {
		if (log.isDebugEnabled()) {
			log.debug("数据源选择判断：{}.{}={}", method.getDeclaringClass().getSimpleName(), method.getName(), type == null ? DataSourceType.DEFAULT : type);
		}
	}

	static class Holder {

		/**
		 * 将 WRITE 数据源的 key 作为默认数据源的 key
		 */
		static final ThreadLocal<DataSourceType> context = new ThreadLocal<>();

	}

}
