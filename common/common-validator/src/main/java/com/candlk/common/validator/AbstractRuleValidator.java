package com.candlk.common.validator;

import java.lang.annotation.Annotation;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public abstract class AbstractRuleValidator<A extends Annotation, T> implements RuleValidator<A, T> {

	@Nullable
	protected abstract String validateInternal(T val, ValidateContext context);

	@Nullable
	protected Supplier<String> validateInternal(T val, String label, ValidateContext context) {
		String error = validateInternal(val, context);
		if (error != null) {
			return makeError(error, val, label, context);
		}
		return null;
	}

	@Override
	public final com.candlk.common.validator.RuleValidator.Result validate(T val, ValidateContext context) {
		Supplier<String> error = validateInternal(val, context.getCurrentLabel(), context);
		if (error == null) {
			return Result.YES;
		}
		context.addError(error);
		return Result.NO;
	}

	protected Supplier<String> makeError(String errorTemplate, T val, String label, ValidateContext context) {
		return new ValidateError(errorTemplate, label);
	}

}
