package com.candlk.common.gencode.tool;

import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;

import com.alibaba.fastjson2.JSON;
import com.candlk.common.model.Property;
import com.candlk.common.validator.Check;
import lombok.Getter;
import me.codeplayer.util.CollectionUtil;
import me.codeplayer.util.StringUtil;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.BeanUtils;

public class ApifoxDocsGenerator {

	public static void main(String[] args) {
		exportToDoc(null);
	}

	/**
	 * 示例：
	 * "mandatory": {
	 * "type": "integer",
	 * "title": "是否强制更新",
	 * "description": "0=默认；1=强制"
	 * }
	 */
	public static void exportToDoc(Class<?> clazz) {
		List<DocField> fields = new LinkedList<>();
		classFieldsExportToDoc(fields, clazz, null);
		Map<String, Map<String, Object>> fieldMap = CollectionUtil.toHashMap(fields, DocField::getKey, f -> CollectionUtil.ofHashMap("type", f.getType(), "description", f.getDesc(), "title", f.getTitle()));
		System.out.println(JSON.toJSONString(CollectionUtil.ofHashMap("type", "object", "properties", fieldMap)));
	}

	public static void classFieldsExportToDoc(List<DocField> fields, Class<?> clazz, String prefix) {
		PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(clazz);
		for (PropertyDescriptor pd : pds) {
			if ("class".equals(pd.getName())) {
				continue;
			}
			Method getter = pd.getReadMethod();
			if (getter == null) {
				continue;
			}
			java.lang.reflect.Field field = FieldUtils.getField(clazz, pd.getName(), true);
			if (field != null && Modifier.isTransient(field.getModifiers())) {
				continue;
			}
			Type returnType = getter.getGenericReturnType();
			Class<?> type;
			Class<?> componentType = null;
			if (returnType instanceof ParameterizedType) {
				type = (Class<?>) ((ParameterizedType) returnType).getRawType();
			} else {
				type = (Class<?>) returnType;
			}
			boolean simpleType = BeanUtils.isSimpleValueType(type);
			if (!simpleType) {
				componentType = type.getComponentType();
				if (componentType == null && Collection.class.isAssignableFrom(type)) {
					Type argumentType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
					if (argumentType instanceof Class) {
						componentType = (Class<?>) argumentType;
					}
				}
			}
			DocField f = DocField.of(pd, field, type, componentType);
			if (prefix != null) {
				f.key = prefix.concat(f.key);
			}
			fields.add(f);
			if (componentType != null && !BeanUtils.isSimpleValueType(componentType)) {
				classFieldsExportToDoc(fields, componentType, f.key.concat("."));
			}
		}
	}

	@Getter
	public static class DocField {

		protected String key;
		protected String desc;
		protected String type;
		protected String title;

		public DocField(String key, String desc, String type, String title) {
			this.key = key;
			this.desc = desc;
			this.type = type;
			this.title = title;
		}

		static Map<Class<?>, String> typeMap = CollectionUtil.ofHashMap(
				String.class, "string",
				Integer.class, "integer",
				int.class, "integer",
				Long.class, "integer",
				long.class, "integer",
				BigDecimal.class, "number",
				boolean.class, "boolean",
				Boolean.class, "boolean",
				Date.class, "object"
		);

		public static DocField of(PropertyDescriptor pd, Field field, Class<?> fieldType, Class<?> componentType) {
			String name = pd.getName();
			Property fAnnotation = field == null ? null : field.getAnnotation(Property.class);
			String desc = null;
			String title = null;
			if (fAnnotation != null) {
				desc = fAnnotation.value();
				title = fAnnotation.name();
			} else if (field != null) {
				Check check = field.getAnnotation(Check.class);
				if (check != null) {
					desc = check.value();
				}
			}
			String type;
			if (componentType == null) {
				type = typeMap.get(fieldType);
			} else if (fieldType.isArray() || Collection.class.isAssignableFrom(fieldType)) {
				desc = StringUtil.concat("*", desc);
				type = "array";
			} else {
				desc = StringUtil.concat("*", desc);
				type = "object";
			}
			return new DocField(name, StringUtil.toString(desc), type, title);
		}

	}

}
