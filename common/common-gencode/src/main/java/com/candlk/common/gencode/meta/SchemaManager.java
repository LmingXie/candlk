package com.candlk.common.gencode.meta;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.*;

import com.candlk.common.gencode.meta.AppSchema.EntityInfo;
import com.candlk.common.gencode.meta.AppSchema.FieldInfo;
import com.candlk.common.gencode.meta.DbSchema.ColumnInfo;
import com.candlk.common.gencode.meta.DbSchema.TableInfo;
import com.candlk.common.model.Bean;
import me.codeplayer.util.StringUtil;
import me.codeplayer.util.Words;
import me.codeplayer.util.X;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class SchemaManager {

	/** 当遇到未知的列类型时，是否继续生成 */
	public boolean continueWhenUnknownColumnType;

	public String tablePrefix;
	public String joinIdSuffix;

	public String basePackage;
	public Class<?> baseClass;
	public Class<?>[] interfaces;
	public Set<String> parentClassFields;

	public SchemaManager(String tablePrefix, String joinIdSuffix, String basePackage, Class<?> baseClass, Class<?>[] interfaces, String... parentClassFields) {
		this.tablePrefix = StringUtil.toString(tablePrefix);
		this.joinIdSuffix = StringUtil.toString(joinIdSuffix);
		this.basePackage = basePackage;
		this.baseClass = baseClass;
		this.interfaces = interfaces;
		this.parentClassFields = new HashSet<>(Arrays.asList(parentClassFields));

		init();
	}

	public SchemaManager() {
		this("dt_", "_id",
				Bean.class.getPackage().getName(),
				null, null, "id");
	}

	protected final Map<Integer, Class<?>> column2FieldTypeMapping = new HashMap<>();

	protected void init() {
		final Map<Integer, Class<?>> map = column2FieldTypeMapping;
		if (map.isEmpty()) {
			mapTypes(map, Integer.class, Types.INTEGER, Types.SMALLINT, Types.TINYINT);
			mapTypes(map, Long.class, Types.BIGINT);
			mapTypes(map, boolean.class, Types.BOOLEAN, Types.BIT);
			mapTypes(map, BigDecimal.class, Types.FLOAT, Types.DOUBLE, Types.DECIMAL, Types.NUMERIC);
			mapTypes(map, Date.class, Types.DATE, Types.TIME, Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE, Types.TIME_WITH_TIMEZONE);
			mapTypes(map, String.class, Types.CHAR, Types.VARCHAR, Types.NCHAR, Types.NVARCHAR, Types.LONGVARCHAR, Types.LONGNVARCHAR, Types.CLOB);
		}
	}

	protected static void mapTypes(Map<Integer, Class<?>> map, Class<?> clazz, int... types) {
		for (int type : types) {
			map.put(type, clazz);
		}
	}

	public AppSchema db2App(DbSchema db) {
		return new AppSchema().init(Arrays.stream(db.tables).map(this::toEntity).toArray(EntityInfo[]::new));
	}

	public void printToCode(DbSchema db) {
		AppSchema app = db2App(db);
		printCode(app);
	}

	public EntityInfo toEntity(TableInfo tab) {
		return new EntityInfo().init(
				table2EntityName(tab),
				Arrays.stream(tab.columns).map(this::column2Field).toArray(FieldInfo[]::new),
				tab.remark
		);
	}

	public String table2EntityName(TableInfo tab) {
		String tableName = StringUtils.removeStart(tab.name, tablePrefix);
		return Words.from(tableName).to(Words.PASCAL_CASE);
	}

	public FieldInfo column2Field(final ColumnInfo col) {
		String name = col.name;
		final boolean assoc = name.endsWith(joinIdSuffix);
		Class<?> type;
		String typeName = null;
		if (assoc) {
			name = name.substring(0, name.length() - joinIdSuffix.length());
			type = null;
		} else {
			type = column2FieldTypeMapping.get(col.type);
			if (type == null) {
				if (continueWhenUnknownColumnType) {
					typeName = "<" + col.typeName + ">";
				} else {
					System.err.println("无法识别的数据类型：" + col.type);
					System.err.println(ToStringBuilder.reflectionToString(col, ToStringStyle.MULTI_LINE_STYLE));
					System.exit(-1);
					return null;
				}
			}
		}
		Words words = Words.from(name);
		name = words.to(Words.CAMEL_CASE);
		if (assoc) {
			typeName = words.to(Words.PASCAL_CASE);
		} else if (typeName == null) {
			typeName = type.getSimpleName();
		}
		AssocType assocType = assoc ? AssocType.MANY_TO_ONE : null;
		return new FieldInfo().init(name, type, typeName, assocType, col.remark);
	}

	public void printCode(AppSchema schema) {
		for (EntityInfo entity : schema.entities) {
			System.out.println(toCode(entity));
		}
	}

	public String toCode(EntityInfo bean) {
		final StringBuilder sb = new StringBuilder(bean.fields.length * 24 + 64);
		// package com.xxx.entity;
		sb.append("package ").append(basePackage).append(";\n\n");
		sb.append("@Entity\n");
		// public class ClassName extends ParentClass implements A, B, C...
		sb.append("public class ").append(bean.name);
		if (baseClass != null) {
			sb.append(" extends ").append(baseClass.getSimpleName());
		}
		if (X.isValid(interfaces)) {
			sb.append(" implements");
			boolean notFirst = false;
			for (Class<?> clazz : interfaces) {
				if (notFirst) {
					sb.append(',');
				} else {
					notFirst = true;
				}
				sb.append(" ").append(clazz.getSimpleName());
			}
		}
		// {
		sb.append(" {\n\n");
		// protect TypeName fields;
		for (FieldInfo field : bean.fields) {
			if (parentClassFields.contains(field.name)) {
				continue;
			}
			fieldAppendToCode(sb, field);
		}
		// }
		sb.append("}\n\n");
		return sb.toString();
	}

	public void fieldAppendToCode(StringBuilder sb, FieldInfo f) {
		if (StringUtil.notEmpty(f.remark)) {
			sb.append("\t/** ").append(f.remark).append(" */\n");
		}
		if (f.assocType != null) {
			switch (f.assocType) {
				case MANY_TO_ONE:
					sb.append("\t@ManyToOne(fetch = FetchType.LAZY)\n");
					sb.append("\t@JoinColumn\n");
					break;
				case ONE_TO_ONE:
				case ONE_TO_MANY:
				case MANY_TO_MANY:
					// TODO 完善其他的关联关系
					break;
			}
		}
		sb.append("\tprotected ").append(f.typeName).append(" ").append(f.name).append(";\n");
	}

	/**
	 * @param type {@link Types}
	 */
	public boolean isBoolean(final int type) {
		return column2FieldTypeMapping.get(type) == boolean.class;
	}

	/**
	 * @param type {@link Types}
	 */
	public boolean isInt(final int type) {
		return column2FieldTypeMapping.get(type) == Integer.class;
	}

	/**
	 * @param type {@link Types}
	 */
	public boolean isLong(final int type) {
		return column2FieldTypeMapping.get(type) == Long.class;
	}

	/**
	 * 是否为浮点数（不一定是浮点类型）
	 *
	 * @param type {@link Types}
	 */
	public boolean isDouble(final int type) {
		return column2FieldTypeMapping.get(type) == BigDecimal.class;
	}

	/**
	 * @param type {@link Types}
	 */
	public boolean isDate(final int type) {
		return column2FieldTypeMapping.get(type) == Date.class;
	}

	/**
	 * @param type {@link Types}
	 */
	public boolean isString(final int type) {
		return column2FieldTypeMapping.get(type) == String.class;
	}

	/**
	 * @param type {@link Types}
	 */
	public boolean isBinary(final int type) {
		switch (type) {
			case Types.BINARY:
			case Types.VARBINARY:
			case Types.LONGVARBINARY:
			case Types.BLOB:
				return true;
			default:
				return false;
		}
	}

	public static enum AssocType {
		ONE_TO_ONE,
		ONE_TO_MANY,
		MANY_TO_ONE,
		MANY_TO_MANY,
	}

}
