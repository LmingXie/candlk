package com.candlk.common.gencode.meta;

import java.util.Arrays;
import javax.annotation.Nullable;

import com.candlk.common.gencode.meta.DbSchema.ColumnInfo;
import me.codeplayer.util.X;

import static com.candlk.common.gencode.meta.SchemaManager.AssocType;

public class AppSchema {

	//
	public EntityInfo[] entities;

	public AppSchema init(EntityInfo[] entities) {
		this.entities = entities;
		return this;
	}

	public static class EntityInfo {

		public String name;
		public String remark;
		public FieldInfo[] fields;
		public ColumnInfo[] columns;

		public EntityInfo init(String name, FieldInfo[] fields, String remark) {
			this.name = name;
			this.fields = fields;
			this.remark = remark;
			return this;
		}

		public FieldInfo[] distinctFields(SchemaManager cfg) {
			if (X.isValid(cfg.parentClassFields)) {
				return Arrays.stream(fields).filter(f -> !cfg.parentClassFields.contains(f.name)).toArray(FieldInfo[]::new);
			}
			return fields;
		}

	}

	public static class FieldInfo {

		public String name;
		public Class<?> type;
		public String typeName;
		public String remark;
		public AssocType assocType;

		public FieldInfo init(String name, Class<?> type, String typeName, @Nullable AssocType assocType, @Nullable String remark) {
			this.name = name;
			this.type = type;
			this.typeName = typeName;
			this.assocType = assocType;
			this.remark = remark;
			return this;
		}

		public FieldInfo init(String name, String typeName, @Nullable AssocType assocType, @Nullable String remark) {
			return init(name, null, typeName, assocType, remark);
		}

	}

}
