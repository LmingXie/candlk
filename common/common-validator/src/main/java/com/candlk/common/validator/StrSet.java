package com.candlk.common.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Set;

import com.candlk.common.validator.StrSet.StrSetValidator;
import me.codeplayer.util.CollectionUtil;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(StrSetValidator.class)
public @interface StrSet {

	String[] value();

	class StrSetValidator extends AbstractRuleValidator<StrSet, String> {

		protected Set<String> range;

		@Override
		public void init(StrSet rule) {
			String[] values = rule.value();
			range = CollectionUtil.asHashSet(values);
		}

		@Override
		protected String validateInternal(String val, ValidateContext context) {
			return range.contains(val) ? null : ValidateError.invalid;
		}

	}

}
