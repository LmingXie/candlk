package com.candlk.common.validator;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

import lombok.Getter;
import lombok.Setter;
import me.codeplayer.util.LazyCacheLoader;
import me.codeplayer.util.StringUtil;

public interface ValidateContext {

	ValidateItem getRoot();

	ValidateItem getCurrent();

	default void setCurrentValue(Object val, boolean setToField) {
		ValidateItem current = getCurrent();
		current.val = val;
		if (setToField) {
			try {
				current.info.target.set(getRoot().val, val);
			} catch (IllegalAccessException e) {
				throw new IllegalStateException("无法保存处理后的参数！", e);
			}
		}
	}

	AnnotatedValidatorEntry getCurrentEntry();

	default String getCurrentLabel() {
		return getCurrent().info.label;
	}

	ValidateItem getItem(String name);

	Map<String, ValidateItem> getItems();

	void addError(Supplier<String> error);

	List<Supplier<String>> getErrors();

	@Getter
	@Setter
	class ValidateItem {

		protected Object originalVal;
		protected Object val;
		protected final ValidateItemInfo info;
		protected Supplier<String> errorMsger;

		public ValidateItem(ValidateItemInfo info, Object val) {
			this.info = info;
			this.originalVal = val;
			this.val = val;
		}

		public ValidateItem(ValidateItemInfo info) {
			this(info, LazyCacheLoader.uninitialized);
		}

		protected Object getFieldValue(Object obj) {
			if (val == LazyCacheLoader.uninitialized) {
				Object v = null;
				if (info.target != null) {
					try {
						v = info.target.get(obj);
					} catch (IllegalAccessException e) {
						throw new IllegalStateException("无法获取属性值：" + info.getName(), e);
					}
				}
				val = originalVal = v;
			}
			return val;
		}

	}

	@Getter
	class ValidateItemInfo {

		protected final Field target;
		protected final Class<?> type;
		protected final String name;
		protected final String label;
		protected final boolean required;
		protected final AnnotatedValidatorEntry[] toValidates;

		public ValidateItemInfo(Field target, Class<?> type, String name, String label, boolean required, AnnotatedValidatorEntry[] toValidates) {
			if (StringUtil.isEmpty(label)) {
				label = target.getName();
			}

			this.target = target;
			this.type = type;
			this.name = name;
			this.label = label;
			this.required = required;
			this.toValidates = toValidates;
		}

		public ValidateItemInfo(Object rootVal, String rootLabel, boolean required) {
			this(null, rootVal.getClass(), null, rootLabel, required, null);
		}

		public ValidateItemInfo(Field field, String label, boolean required, AnnotatedValidatorEntry[] toValidates) {
			field.setAccessible(true);

			if (StringUtil.isEmpty(label)) {
				label = field.getName();
			}

			this.target = field;
			this.name = field.getName();
			this.type = field.getType();
			this.label = label;
			this.required = required;
			this.toValidates = toValidates;
		}

	}

	@Getter
	class AnnotatedValidatorEntry implements Comparable<AnnotatedValidatorEntry> {

		protected final Annotation rule;
		protected final RuleValidator<? extends Annotation, Object> validator;

		public AnnotatedValidatorEntry(Annotation rule, RuleValidator<Annotation, Object> validator) {
			this.rule = rule;
			validator.init(rule);
			this.validator = validator;
		}

		@Override
		public int compareTo(AnnotatedValidatorEntry o) {
			return validator.compareTo(o.validator);
		}

	}

	class DefaultValidateContext implements ValidateContext {

		protected ValidateItem root;
		protected Map<String, ValidateItem> items;
		protected List<Supplier<String>> errors = new LinkedList<>();
		protected ValidateItem current;
		protected AnnotatedValidatorEntry currentEntry;

		public DefaultValidateContext(ValidateItem root, Map<String, ValidateItem> items) {
			this.root = root;
			this.items = items;
		}

		@Override
		public ValidateItem getRoot() {
			return root;
		}

		public void setCurrent(ValidateItem current) {
			this.current = current;
		}

		@Override
		public ValidateItem getCurrent() {
			return current;
		}

		@Override
		public AnnotatedValidatorEntry getCurrentEntry() {
			return currentEntry;
		}

		public void setCurrentEntry(AnnotatedValidatorEntry currentEntry) {
			this.currentEntry = currentEntry;
		}

		@Override
		public ValidateItem getItem(String name) {
			return items.get(name);
		}

		@Override
		public Map<String, ValidateItem> getItems() {
			return items;
		}

		@Override
		public void addError(Supplier<String> error) {
			errors.add(error);
		}

		@Override
		public List<Supplier<String>> getErrors() {
			return errors;
		}

	}

}
