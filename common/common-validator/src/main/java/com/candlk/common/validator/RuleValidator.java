package com.candlk.common.validator;

import java.lang.annotation.Annotation;

public interface RuleValidator<A extends Annotation, T> extends Comparable<RuleValidator<?, ?>> {

	/** 出场优先 */
	int ORDER_BEGIN = 0,
	/** 预处理 */
	ORDER_PRE = 20,
	/** 预校验 */
	ORDER_PRE_VALIDATE = 50,
	/** 默认（正常校验） */
	ORDER_DEFAULT = 100,
	/** 最后收尾 */
	ORDER_END = 10000;

	default void init(A rule) {
	}

	Result validate(T val, ValidateContext context);

	default boolean notNull() {
		return true;
	}

	/**
	 * 值越小，越先执行
	 */
	default int getOrder() {
		return ORDER_DEFAULT;
	}

	@Override
	default int compareTo(RuleValidator<?, ?> o) {
		return getOrder() - o.getOrder();
	}

	enum Result {
		YES,
		NO,
		YES_BREAK;
	}

}
