package com.candlk.common.validator;

import java.io.Serializable;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.candlk.common.model.ValueEnum;
import com.candlk.common.validator.EnumSet.EnumSetValidator;
import me.codeplayer.util.EnumUtil;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(EnumSetValidator.class)
public @interface EnumSet {

	int FROM_ORDINAL = 0;
	int FROM_NAME = 1;
	int FROM_VALUE_ENUM = 2;

	Class<? extends Enum<?>> value();

	int valueFrom() default -1;

	class EnumSetValidator extends AbstractRuleValidator<EnumSet, Serializable> {

		protected Enum<?>[] values;
		protected boolean isValueEnum;
		protected int valueFrom = -1;

		@Override
		public void init(EnumSet rule) {
			Class<? extends Enum<?>> enumClass = rule.value();
			isValueEnum = ValueEnum.class.isAssignableFrom(enumClass);
			values = enumClass.getEnumConstants();
			valueFrom = rule.valueFrom();
			if (valueFrom < -1 || valueFrom > 2) {
				throw new ValidateException("@EnumSet 注解配置无效，valueFrom 属性值无效！");
			}
			if (valueFrom == FROM_VALUE_ENUM && !isValueEnum) {
				throw new ValidateException("@EnumSet 注解配置无效，指定枚举类必须实现 ValueEnum 接口！");
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		protected String validateInternal(Serializable value, ValidateContext context) {
			if (valueFrom == -1) {
				if (isValueEnum) {
					valueFrom = FROM_VALUE_ENUM;
				} else if (value instanceof Integer) {
					valueFrom = FROM_ORDINAL;
				} else if (value instanceof String) {
					valueFrom = FROM_NAME;
				} else {
					throw new ValidateException("@EnumSet 不支持校验该参数类型！");
				}
			}
			boolean valid = false;
			switch (valueFrom) {
				case 0:
					int val = (int) value;
					valid = val >= 0 && val < values.length - 1;
					break;
				case 1:
					String name = (String) value;
					valid = EnumUtil.of(values[0].getClass(), name) != null;
					break;
				case 2:
					ValueEnum<?, Serializable> valueEnum = (ValueEnum<?, Serializable>) values[0];
					valid = valueEnum.getValueOf(value) != null;
					break;
			}
			return valid ? null : ValidateError.invalid;
		}

	}

}
