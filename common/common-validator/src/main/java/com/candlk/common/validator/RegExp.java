package com.candlk.common.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.regex.Pattern;

import com.candlk.common.validator.RegExp.RegExpValidator;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(RegExpValidator.class)
public @interface RegExp {

	String value();

	String replace() default "\0";

	boolean set() default true;

	int flags() default 0;

	class RegExpValidator extends AbstractRuleValidator<RegExp, String> {

		protected RegExp rule;
		protected Pattern pattern;

		@Override
		public void init(RegExp rule) {
			this.rule = rule;
			pattern = Pattern.compile(rule.value(), rule.flags());
		}

		@Override
		protected String validateInternal(String val, ValidateContext context) {
			String replace = rule.replace();
			if ("\0".equals(replace)) {
				return pattern.matcher(val).matches() ? null : ValidateError.format_invalid;
			} else {
				context.setCurrentValue(pattern.matcher(val).replaceAll(replace), rule.set());
				return null;
			}
		}

		@Override
		public int getOrder() {
			return rule.set() ? ORDER_PRE + 10 : ORDER_DEFAULT;
		}

	}

}
