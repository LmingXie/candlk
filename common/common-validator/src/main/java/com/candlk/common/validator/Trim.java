package com.candlk.common.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.candlk.common.validator.Trim.TrimValidator;
import me.codeplayer.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(TrimValidator.class)
public @interface Trim {

	boolean all() default false;

	boolean set() default true;

	class TrimValidator extends AbstractRuleValidator<Trim, String> {

		protected Trim rule;

		@Override
		public void init(Trim rule) {
			this.rule = rule;
		}

		@Override
		protected String validateInternal(String val, ValidateContext context) {
			val = rule.all() ? StringUtils.deleteWhitespace(val) : StringUtil.trim(val);
			context.setCurrentValue(val, rule.set());
			return null;
		}

		@Override
		public int getOrder() {
			return ORDER_PRE - 10;
		}
	}

}
