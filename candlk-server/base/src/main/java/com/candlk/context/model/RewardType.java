package com.candlk.context.model;

import javax.annotation.Nullable;

import com.candlk.common.model.ValueProxy;
import com.candlk.common.util.Common;
import lombok.Getter;

@Getter
public enum RewardType implements LabelI18nProxy<RewardType, Integer> {

	/** 次数 */
	COUNT(1, "次数"),
	/** 金币 */
	GOLD(2, "金币"),
	/** 积分 */
	INTEGRAL(3, "积分"),
	/** 额度（例如：每日提款限额） */
	LIMIT(4, "额度"),
	;

	// 定义私有变量
	public final Integer value;
	final ValueProxy<RewardType, Integer> proxy;

	// 构造函数，枚举类型只能为私有
	RewardType(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final RewardType[] CACHE = values();

	public static RewardType of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, +1);
	}
}
