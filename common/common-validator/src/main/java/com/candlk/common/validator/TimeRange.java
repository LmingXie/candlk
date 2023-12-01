package com.candlk.common.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Date;
import java.util.function.Supplier;

import com.candlk.common.validator.TimeRange.TimeRangeValidator;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Rule(TimeRangeValidator.class)
public @interface TimeRange {

	String min() default "";

	String minTitle() default "";

	String max() default "";

	String maxTitle() default "";

	String NOW = "now";

	class TimeRangeValidator extends AbstractRuleValidator<TimeRange, Date> {

		protected TimeRange rule;
		protected Date min, max;

		@Override
		public void init(TimeRange rule) {
			this.rule = rule;
			min = parse(rule.min());
			max = parse(rule.max());
		}

		protected Date parse(String dateStr) {
			if (dateStr.isEmpty() || NOW.equals(dateStr)) {
				return null;
			}
			return ValidateHelper.DATE_PARSER.apply(dateStr);
		}

		@Override
		protected String validateInternal(Date val, ValidateContext context) {
			return null;
		}

		@Override
		protected Supplier<String> validateInternal(Date val, String label, ValidateContext context) {
			long time = val.getTime();
			long now = System.currentTimeMillis();
			Date min = getMutableDate(this.min, now, rule.min()),
					max = getMutableDate(this.max, now, rule.max());

			// {0} must be not less than {1}
			if (min != null && time < min.getTime()) {
				// return () -> label + "不能小于" + semanticDate(rule.min(), rule.minTitle());
				return new ValidateError(ValidateError.time_min_invalid, label, semanticDate(rule.min(), rule.minTitle()));
			}
			if (max != null && time > max.getTime()) {
				// return () -> label + "不能大于" + semanticDate(rule.max(), rule.maxTitle());
				return new ValidateError(ValidateError.time_max_invalid, label, semanticDate(rule.max(), rule.maxTitle()));
			}
			return null;
		}

		private String semanticDate(String dateStr, String title) {
			if (!title.isEmpty()) {
				return ValidateError.tryResolveKey(title);
			}
			return NOW.equals(dateStr) ? ValidateError.tryResolveKey(ValidateError.now) : ValidateError.tryResolveKey(dateStr);
		}

		private Date getMutableDate(Date date, long now, String dateStr) {
			if (date == null && NOW.equals(dateStr)) {
				date = new Date(now);
			}
			return date;
		}

	}

}
