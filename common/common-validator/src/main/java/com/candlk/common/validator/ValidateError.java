package com.candlk.common.validator;

import java.text.MessageFormat;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.apache.commons.lang3.ArrayUtils;

public class ValidateError implements Supplier<String> {

	public static final String required = "@validate.required",
			invalid = "@validate.invalid",
			format_invalid = "@validate.format.invalid",
			email_invalid = format_invalid,
			real_name_invalid = "@validate.real_name.invalid",
			range_le_max_invalid = "@validate.range.le.invalid",
			range_lt_max_invalid = "@validate.range.lt.invalid",
			range_ge_min_invalid = "@validate.range.ge.invalid",
			range_gt_min_invalid = "@validate.range.gt.invalid",
			time_min_invalid = "@validate.time.min.invalid",
			time_max_invalid = "@validate.time.max.invalid",
			now = "@now",
			size_string_min_invalid = "@validate.size.string.min.invalid",
			size_string_max_invalid = "@validate.size.string.max.invalid",
			size_items_min_invalid = "@validate.size.items.min.invalid",
			size_items_max_invalid = "@validate.size.items.max.invalid";

	protected String key;
	protected Object[] args;

	static BiFunction<String, Object[], String> errorResolver = MessageFormat::format;

	public ValidateError() {
	}

	public ValidateError(String key, Object... args) {
		this.key = key;
		this.args = args;
	}

	public static void setErrorResolver(BiFunction<String, Object[], String> errorResolver) {
		ValidateError.errorResolver = errorResolver;
	}

	public String get() {
		return resolveError(key, args);
	}

	public static String resolveError(String key, Object... args) {
		if (args != null && args.length > 0) { // 第一个是 label，一般也需要国际化
			args[0] = errorResolver.apply((String) args[0], ArrayUtils.EMPTY_OBJECT_ARRAY);
		}
		return errorResolver.apply(key, args);
	}

	public static String tryResolveKey(String key) {
		if (key != null && key.startsWith("@")) {
			return resolveError(key, ArrayUtils.EMPTY_OBJECT_ARRAY);
		}
		return key;
	}

	public static String resolveRequiredError(String label) {
		return resolveError(required, label);
	}

}
