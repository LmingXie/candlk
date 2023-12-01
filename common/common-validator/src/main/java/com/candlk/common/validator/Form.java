package com.candlk.common.validator;

import java.io.Serializable;

@CheckForm(label = "表单数据")
public interface Form extends FormHelper, Serializable {

	/** 前置准备 */
	default void preHandle() {
	}

	/** 校验 */
	default void validate() {
	}

	/** 后置处理 */
	default void postHandle() {
	}

}
