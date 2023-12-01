package com.candlk.common.gencode.meta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import me.codeplayer.util.ArrayUtil;
import me.codeplayer.util.StringUtil;
import me.codeplayer.util.X;
import org.apache.commons.lang3.StringUtils;

public class DbSchema {

	public String name;
	public TableInfo[] tables;

	public DbSchema(String name, TableInfo[] tables) {
		this.name = name;
		this.tables = tables;
	}

	public static DbSchema from(@Nullable String dbName, Connection conn, String... tableNames) throws SQLException {
		DatabaseMetaData metaData = conn.getMetaData();
		List<TableInfo> list = new ArrayList<>();
		if (StringUtil.isEmpty(dbName)) {
			dbName = conn.getCatalog();
		}
		if (!X.isValid(tableNames)) {
			ResultSet tables = metaData.getTables(dbName, null, "%", null);
			while (tables.next()) {
				list.add(new TableInfo(dbName, tables, metaData));
			}
		} else {
			for (String tableName : tableNames) {
				ResultSet tables = metaData.getTables(dbName, null, tableName, null);
				while (tables.next()) {
					list.add(new TableInfo(dbName, tables, metaData));
				}
			}
		}
		TableInfo[] tables = ArrayUtil.toArray(list, TableInfo.class);
		return new DbSchema(dbName, tables);
	}

	public static DbSchema from(String dbName, Connection conn) throws SQLException {
		return from(dbName, conn, (String[]) null);
	}

	public static DbSchema from(Connection conn, String... tableNames) throws SQLException {
		return from(null, conn, tableNames);
	}

	public static DbSchema from(Connection conn) throws SQLException {
		return from(null, conn);
	}

	public static class TableInfo {

		/** 表名 */
		public String name;
		/** 数据列的信息数组 */
		public ColumnInfo[] columns;
		public String remark;

		public TableInfo(String name, ColumnInfo[] columns, String remark) {
			this.name = name;
			this.columns = columns;
			this.remark = remark;
		}

		public TableInfo(String dbName, ResultSet tableRs, DatabaseMetaData metaData) throws SQLException {
			this.name = tableRs.getString("TABLE_NAME");
			this.remark = tableRs.getString("REMARKS");
			ResultSet columnRsCursor = metaData.getColumns(dbName, null, name, null);
			List<ColumnInfo> list = new ArrayList<>();
			while (columnRsCursor.next()) {
				list.add(new ColumnInfo(columnRsCursor));
			}
			this.columns = ArrayUtil.toArray(list, ColumnInfo.class);
		}

	}

	public static class ColumnInfo {

		/** 字段名称 */
		public String name;
		/** 字段类型 {@link Types } */
		public int type;
		/** 字段类型 {@link Types } */
		public String typeName;
		/** 长度（包括小数点后的位数） */
		public int length;
		/** 小数点后的有效位数，如果没有，则为 -1 */
		public int scale;
		/** 是否可为 null */
		public boolean nullable;
		/** 是否为 unsigned */
		public boolean unsigned;
		/** 是否为 自增长 */
		public boolean isAutoIncrement;
		/** 默认值 */
		public Object def;
		/** 备注 */
		public String remark;
		public String property;

		public ColumnInfo(String name, int type, String typeName, int length, int scale, boolean nullable, boolean unsigned,
		                  boolean isAutoIncrement, Object def, String remark) {
			this.name = name;
			this.type = type;
			this.typeName = typeName;
			this.length = length;
			this.scale = scale;
			this.nullable = nullable;
			this.unsigned = unsigned;
			this.isAutoIncrement = isAutoIncrement;
			this.def = def;
			this.remark = remark;
		}

		public ColumnInfo(final ResultSet columnRs) throws SQLException {
			// @see DatabaseMetaData.getColumns
			this.name = columnRs.getString("COLUMN_NAME");
			this.type = columnRs.getInt("DATA_TYPE");
			this.typeName = columnRs.getString("TYPE_NAME");
			this.length = columnRs.getInt("COLUMN_SIZE");
			Integer scale = columnRs.getObject("DECIMAL_DIGITS", Integer.class);
			this.scale = scale == null ? -1 : scale;
			this.nullable = columnRs.getInt("NULLABLE") != 0;
			this.unsigned = StringUtils.contains(typeName, "UNSIGNED");
			this.isAutoIncrement = "YES".equals(columnRs.getString("IS_AUTOINCREMENT"));
			this.def = columnRs.getString("COLUMN_DEF");
			this.remark = columnRs.getString("REMARKS");
		}

	}

}
