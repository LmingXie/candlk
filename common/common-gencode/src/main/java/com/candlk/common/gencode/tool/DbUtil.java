package com.candlk.common.gencode.tool;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.sql.DataSource;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

public abstract class DbUtil {

	public static DataSourceProperties cfg = MvcTemplateGenerator.GeneratorConfig.getDefaultDataSourceProperties();

	public static DataSource getDataSource() throws SQLException {
		MysqlDataSource ds = new MysqlDataSource();
		ds.setUrl(cfg.getUrl());
		ds.setUser(cfg.getUsername());
		ds.setPassword(cfg.getPassword());
		ds.setCharacterEncoding("UTF-8");
		ds.setServerTimezone("GMT+8");
		ds.setUseSSL(false);
		/*
		ds.setAllowPublicKeyRetrieval(true);
		ds.setUseServerPrepStmts(true);
		ds.setCachePrepStmts(true);
		ds.setUseLocalSessionState(true);
		ds.setUseUnbufferedInput(false);
//		ds.setMaintainTimeStats(false);
		ds.setElideSetAutoCommits(true);
		ds.setCacheServerConfiguration(true);
//		ds.setEnableQueryTimeouts(false); // 高性能负载环境下可以考虑为false
		ds.setConnectionAttributes("none"); // since 5.1.25(MySQL 5.6+) 可提高数据库连接创建/初始化速度
		ds.setPrepStmtCacheSize(250);
		ds.setPrepStmtCacheSqlLimit(2048);
		*/
		return ds;
	}

	public static void doInJDBC(Work<Connection> work) throws Exception {
		Connection conn = DbUtil.getConnection(cfg);
		try {
			conn.setAutoCommit(false);
			work.execute(conn);
			conn.commit();
		} catch (Exception e) {
			conn.rollback();
			throw new IllegalStateException(e);
		} finally {
			conn.close();
		}
	}

	/**
	 * 测试数据库链接是否正常，并打印数据库的版本信息
	 */
	public static void testDBConnection(DataSourceProperties cfg) throws ClassNotFoundException, SQLException {
		Connection conn = getConnection(cfg);
		DatabaseMetaData metaData = conn.getMetaData();
		System.out.println("连接的数据库为：" + metaData.getDatabaseProductName() + "-" + metaData.getDatabaseProductVersion());
	}

	public static Connection getConnection(DataSourceProperties cfg) throws SQLException, ClassNotFoundException {
		Class.forName(cfg.getDriverClassName());
		return DriverManager.getConnection(cfg.getUrl(), cfg.getUsername(), cfg.getPassword());
	}

	@FunctionalInterface
	public static interface Work<E> {

		void execute(E e) throws Exception;

	}

}
