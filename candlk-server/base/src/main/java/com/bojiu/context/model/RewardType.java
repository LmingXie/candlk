package com.bojiu.context.model;

import javax.annotation.Nullable;

import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import lombok.Getter;

import static com.bojiu.context.model.BaseI18nKey.*;

@Getter
public enum RewardType implements LabelI18nProxy<RewardType, Integer> {

	/** 次数 */
	COUNT(1, REWARD_TYPE_COUNT),
	/** 金币 */
	GOLD(2, REWARD_TYPE_GOLD),
	/** 积分 */
	INTEGRAL(3, REWARD_TYPE_INTEGRAL),
	/** 额度（例如：每日提款限额） */
	LIMIT(4, REWARD_TYPE_LIMIT),
	;

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
