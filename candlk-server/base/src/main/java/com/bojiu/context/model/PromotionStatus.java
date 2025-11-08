package com.bojiu.context.model;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import lombok.Getter;

/**
 * 活动状态
 */
@Getter
public enum PromotionStatus implements LabelI18nProxy<PromotionStatus, Integer> {
	/** 关闭 */
	CLOSE(0, BaseI18nKey.PROMOTION_STATUS_CLOSE),
	/** 草稿 */
	DRAFT(1, BaseI18nKey.PROMOTION_STATUS_DRAFT),
	/** 待生效 */
	TO_EFFECTIVE(2, BaseI18nKey.PROMOTION_STATUS_TO_EFFECTIVE),
	/** 已生效 */
	EFFECTIVE(3, BaseI18nKey.PROMOTION_STATUS_EFFECTIVE),
	/** 已结束 */
	ENDED(4, BaseI18nKey.PROMOTION_STATUS_ENDED),
	;

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

	public static final List<Integer> validStatus = Arrays.asList(TO_EFFECTIVE.value, EFFECTIVE.value);
	public static final List<Integer> canEditStatus = Arrays.asList(DRAFT.value, TO_EFFECTIVE.value, EFFECTIVE.value);
}
