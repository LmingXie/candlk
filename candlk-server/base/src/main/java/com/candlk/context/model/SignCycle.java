package com.candlk.context.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.candlk.common.model.ValueProxy;
import lombok.Getter;

/**
 * 签到周期
 */
@Getter
public enum SignCycle implements LabelI18nProxy<SignCycle, Integer> {
	/** 7天 */
	DAYS_7(7, "7天"),
	/** 15天 */
	DAYS_15(15, "15天"),
	/** 30天 */
	DAYS_30(30, "30天"),
	;
	// 定义私有变量
	@EnumValue
	public final Integer value;
	final ValueProxy<SignCycle, Integer> proxy;

	SignCycle(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final SignCycle[] CACHE = values();

}