package com.candlk.common.test;

import javax.annotation.*;
import javax.sql.*;

import org.springframework.test.annotation.*;
import org.springframework.transaction.annotation.*;

/**
 * 【数据访问测试】基于 Spring ( Boot ) 运行环境的已集成 数据库访问 依赖的测试基类
 */
@Transactional
@Commit
public abstract class DbTest extends SpringTest {

	@Resource
	protected DataSource dataSource;

}
