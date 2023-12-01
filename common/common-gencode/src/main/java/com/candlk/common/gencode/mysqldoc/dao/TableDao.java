package com.candlk.common.gencode.mysqldoc.dao;

import java.util.List;

import com.candlk.common.gencode.mysqldoc.domain.Table;
import com.candlk.common.gencode.mysqldoc.domain.TableField;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 查询表数据
 */
public interface TableDao {

	/**
	 * @return 某个库所有表名和注释 gateway_admin_stable为库名
	 */
	@Select("SELECT table_name AS name, table_comment AS comment FROM information_schema.tables WHERE table_schema = #{dataName} ORDER BY table_name")
	List<Table> getAllTables(@Param("dataName") String dataName);

	@Select("SHOW FULL FIELDS FROM ${tableName}")
	List<TableField> getTable(@Param("tableName") String tableName);

}
