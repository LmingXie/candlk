package com.candlk.context.model;

import javax.annotation.Nullable;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.candlk.common.model.ValueProxy;
import com.candlk.common.util.Common;
import lombok.Getter;

/**
 * 活动状态
 */
@Getter
public enum PromotionStatus implements LabelI18nProxy<PromotionStatus, Integer> {
	/** 关闭 */
	CLOSE(0, "关闭"),
	/** 草稿 */
	DRAFT(1, "草稿"),
	/** 待生效 */
	TO_EFFECTIVE(2, "待生效"),
	/** 已生效 */
	EFFECTIVE(3, "已生效"),
	/** 已结束 */
	ENDED(4, "已结束"),
	;
	// 定义私有变量
	@EnumValue
	public final Integer value;
	final ValueProxy<PromotionStatus, Integer> proxy;

	PromotionStatus(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final PromotionStatus[] CACHE = values();

	public static PromotionStatus of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, 0);
	}

}