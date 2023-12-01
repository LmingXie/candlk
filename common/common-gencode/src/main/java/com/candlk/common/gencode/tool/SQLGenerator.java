package com.candlk.common.gencode.tool;

import java.sql.Connection;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;

import com.candlk.common.gencode.meta.DbSchema;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import lombok.SneakyThrows;
import me.codeplayer.util.Assert;
import me.codeplayer.util.Words;
import me.codeplayer.util.X;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import static com.candlk.common.gencode.meta.DbSchema.ColumnInfo;
import static com.candlk.common.gencode.meta.DbSchema.from;

/**
 * SQL 代码生成器
 */
public class SQLGenerator {

	public static String saveOrUpdateByDuplicate(DataSource ds, String tableName, String... columnsWhenUpdate) {
		List<Field> fields = tableFields(ds, tableName);
		String sql = "INSERT INTO " + tableName + " (" + fields.stream().map(f -> f.name).collect(Collectors.joining(", ")) + ")" +
				" VALUES ( " + fields.stream().map(f -> f.variable).collect(Collectors.joining(", ")) + " )" +
				" ON DUPLICATE KEY UPDATE " + getLimitFields(fields, trySplitColumn(columnsWhenUpdate)).stream().map(f -> f.name + " = " + f.variable).collect(Collectors.joining(", "));
		System.out.println(sql);
		return sql;
	}

	public static void printColumnExpression(DataSource ds, String tableName, Class<?> entityClass, String... limitedColumns) {
		List<Field> fields = tableFields(ds, tableName);

		limitedColumns = trySplitColumn(limitedColumns);

		List<Field> limitFields = getLimitFields(fields, limitedColumns);

		fillTypes(entityClass, limitFields);

		String sql = fields.stream().map(f -> f.name).collect(Collectors.joining(", "));
		System.out.println("\n\n========= SELECT * 列 ========");
		System.out.println("SELECT " + sql + " FROM " + tableName);

		sql = limitFields.stream().map(f -> f.name.equals(f.property) ? f.name : f.name + " AS " + f.property).collect(Collectors.joining(", "));
		System.out.println("\n========= SELECT AS 列 ========");
		System.out.println("SELECT " + sql + " FROM " + tableName);

		sql = limitFields.stream().map(f -> f.name + " = " + f.variable).collect(Collectors.joining(", "));
		System.out.println("\n========= UPDATE SET 列 ========");
		System.out.println("UPDATE " + tableName + " SET " + sql);

		sql = " WHERE " + limitFields.stream().map(f -> f.name + " = " + f.variable).collect(Collectors.joining(" AND "));
		System.out.println("\n========= WHERE 列 ========");
		System.out.println(sql);

		sql = "DELETE FROM " + tableName + sql;
		System.out.println("\n========= DELETE 列 ========");
		System.out.println(sql);

		System.out.println("\n========= 方法签名 ========");
		// @Param("userId") Long userId, @Param("expireTime") String expireTime
		System.out.println(limitFields.stream().map(f -> "@Param(\"" + f.property + "\") " + f.type + " " + f.property).collect(Collectors.joining(", ")));
	}

	private static void fillTypes(Class<?> entityClass, List<Field> limitFields) {
		for (Field field : limitFields) {
			try {
				field.type = FieldUtils.getField(entityClass, field.property, true).getType().getSimpleName();
			} catch (RuntimeException e) {
				throw new IllegalStateException("无法获取属性：" + entityClass + "." + field.property, e);
			}
		}
	}

	public static void printIf(Class<?> entityClass, String... columns) {
		columns = trySplitColumn(columns);
		Map<String, Operator> ops = getOperators();
		final Operator eq = ops.get("=");

		Field[] fields = new Field[columns.length];
		for (int i = 0; i < columns.length; i++) {
			String col = columns[i];
			String name = col;
			Operator operator = eq;
			for (Map.Entry<String, Operator> entry : ops.entrySet()) {
				String op = entry.getKey();
				name = StringUtils.removeEnd(col, op);
				//noinspection StringEquality
				if (name != col) {
					operator = entry.getValue();
					break;
				}
			}
			Field f = new Field(name);
			f.operator = operator;
			fields[i] = f;
		}

		fillTypes(entityClass, Arrays.asList(fields));

		System.out.println("\n========= SET 子句  ========");
		String sql = Arrays.stream(fields).map(f -> f.operator.converter.apply(f)).collect(Collectors.joining(", "));
		System.out.println(sql);

		System.out.println("\n========= SET IF 标签  ========");
		sql = Arrays.stream(fields).map(f -> f.ifTag(true)).filter(Objects::nonNull).collect(Collectors.joining("", "<set>", "\n</set>"));
		System.out.println(sql);

		System.out.println("\n========= WHERE IF 标签  ========");
		sql = Arrays.stream(fields).map(f -> f.ifTag(false)).filter(Objects::nonNull).collect(Collectors.joining("", "<where>", "\n</where>"));
		System.out.println(sql);
	}

	public static Map<String, Operator> getOperators() {
		Map<String, Operator> map = new HashMap<>();
		map.put("++", new Operator(e -> e.name + " = " + e.name + " + " + e.variable, true));
		map.put("--", new Operator(e -> e.name + " = " + e.name + " - " + e.variable, true));
		final String[] ops = { "=", ">", ">=", "<", "<=", "!=", "<>" };
		for (String op : ops) {
			map.put(op, new Operator(e -> e.name + " " + op + " " + e.variable));
		}
		return map;
	}

	@Nonnull
	public static String[] trySplitColumn(String[] limitedColumns) {
		return Arrays.stream(limitedColumns).flatMap(s -> Arrays.stream(StringUtils.split(s, ", "))).map(s -> Words.from(s).to(Words.SNAKE_CASE)).toArray(String[]::new);
	}

	public static List<Field> getLimitFields(List<Field> fields, String[] limitedColumns) {
		if (X.isValid(limitedColumns)) {
			List<Field> sublist = fields.stream().filter(f -> ArrayUtils.contains(limitedColumns, f.name)).collect(Collectors.toList());
			Assert.isTrue(limitedColumns.length == sublist.size(), "存在无效的列名");
			return sublist;
		}
		return fields;

	}

	@SneakyThrows
	public static List<Field> tableFields(DataSource ds, String tableName) {
		Connection conn = ds.getConnection();
		DbSchema db = from("dt_intl", conn, tableName);
		ArrayList<Field> fields = new ArrayList<>();
		for (ColumnInfo col : db.tables[0].columns) {
			Field f = new Field(col.name);
			fields.add(f);
		}
		return fields;
	}

	public static class Operator {

		public final Function<Field, String> converter;
		public final boolean setOnly;

		public Operator(Function<Field, String> converter, boolean setOnly) {
			this.converter = converter;
			this.setOnly = setOnly;
		}

		public Operator(Function<Field, String> converter) {
			this(converter, false);
		}

	}

	public static class Field {

		public String name;
		public String property;
		public String type;
		public String variable;
		public Operator operator;

		public Field(String name) {
			this.name = name;
			this.property = NamingStrategy.underlineToCamel(name);
			this.variable = "#{" + property + "}";
		}

		@Nullable
		public String ifTag(boolean setOrWhere) {
			if (!setOrWhere && operator.setOnly) {
				return null;
			}
			StringBuilder sb = new StringBuilder();
			if ("String".equals(type)) {
				sb.append("\n\t<if test=\"not empty ").append(property).append("\">");
			} else {
				sb.append("\n\t<if test=\"").append(property).append(" != null\">");
			}
			sb.append("\n\t\t").append(operator.converter.apply(this)).append(setOrWhere ? "," : " AND ");
			sb.append("\n\t</if>");
			return sb.toString();
		}

	}

}
