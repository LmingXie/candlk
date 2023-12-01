package com.candlk.common.validator;

import java.lang.annotation.*;
import javax.validation.ConstraintDeclarationException;

import com.candlk.common.validator.WordCase.WordCaseValidator;
import me.codeplayer.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(WordCaseValidator.class)
public @interface WordCase {

	/** 小写 */
	String LOWER = "lower",
	/** 大写 */
	UPPER = "upper",
	/** 首字母大写 */
	UCFIRST = "ucfirst",
	/** 首字母小写 */
	LCFIRST = "lcfirst";

	String value();

	boolean set() default true;

	class WordCaseValidator extends AbstractRuleValidator<WordCase, String> {

		protected WordCase rule;

		@Override
		public void init(WordCase rule) {
			this.rule = rule;
		}

		@Override
		protected String validateInternal(String val, ValidateContext context) {
			if (rule.set()) {
				switch (rule.value()) {
					case LOWER:
						return val.toLowerCase();
					case UPPER:
						return val.toUpperCase();
					case UCFIRST:
						return StringUtil.capitalize(val);
					case LCFIRST:
						return StringUtil.decapitalize(val);
				}
				throw new ConstraintDeclarationException("不支持的格式转换操作：" + rule.value());
			}
			// TODO 未使用到，暂不国际化
			switch (rule.value()) {
				case LOWER:
					return StringUtils.isAllLowerCase(val) ? null : "{0}必须全部小写！";
				case UPPER:
					return StringUtils.isAllUpperCase(val) ? null : "{0}必须全部大写！";
				case UCFIRST:
					return Character.isUpperCase(val.charAt(0)) ? null : "{0}首字母必须大写！";
				case LCFIRST:
					return Character.isLowerCase(val.charAt(0)) ? null : "{0}首字母必须小写！";
			}
			throw new ConstraintDeclarationException("无法识别指定格式：" + rule.value());
		}

		@Override
		public int getOrder() {
			return rule.set() ? ORDER_PRE : ORDER_DEFAULT;
		}

	}

}
