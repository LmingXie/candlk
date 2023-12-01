package com.candlk.common.context;

import java.lang.reflect.*;

import org.apache.commons.lang3.reflect.*;

public interface ReflectHelper {

	default ReflectHelper getReflectHelper() {
		return DefaultReflectHelper.INSTANCE;
	}

	default Class<?> getClass(Object entity) {
		return getReflectHelper().getClass(entity);
	}

	default Object getProperty(Object o, String property) {
		return getReflectHelper().getProperty(o, property);
	}

	class DefaultReflectHelper implements ReflectHelper {

		public static DefaultReflectHelper INSTANCE = new DefaultReflectHelper();

		@Override
		public Class<?> getClass(Object entity) {
			return entity.getClass();
		}

		@Override
		public Object getProperty(Object o, String property) {
			Field field = FieldUtils.getField(o.getClass(), property, true);
			try {
				return field.get(o);
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}

}
