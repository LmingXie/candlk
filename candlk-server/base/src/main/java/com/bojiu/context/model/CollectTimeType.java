package com.bojiu.context.model;

import java.util.Arrays;
import java.util.List;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import com.bojiu.context.i18n.AdminI18nKey;
import lombok.Getter;
import me.codeplayer.util.CollectionUtil;
import org.jspecify.annotations.Nullable;

/**
 * 领取时间类型：0=实时领取；1=次日领取；2=每日；3=下周；4=每周；5=下月；6=每月；
 */
@Getter
public enum CollectTimeType implements LabelI18nProxy<CollectTimeType, Integer> {
	/** 实时 */
	REAL_TIME(0, BaseI18nKey.COLLECT_TIME_TYPE_REAL_TIME),
	/** 次日 */
	NEXT_DAY(1, AdminI18nKey.COLLECT_TIME_TYPE_NEXT_DAY, 1),
	/** 每日 */
	EVERYDAY(2, AdminI18nKey.COLLECT_TIME_TYPE_EVERYDAY, 2),
	/** 下周 */
	NEXT_WEEK(3, AdminI18nKey.COLLECT_TIME_TYPE_NEXT_WEEK, 1),
	/** 每周 */
	WEEKLY(4, AdminI18nKey.COLLECT_TIME_TYPE_WEEKLY, 2),
	/** 下月 */
	NEXT_MONTH(5, AdminI18nKey.COLLECT_TIME_TYPE_NEXT_MONTH, 1),
	/** 每月 */
	MONTHLY(6, AdminI18nKey.COLLECT_TIME_TYPE_MONTHLY, 2),
	;

	@EnumValue
	public final Integer value;
	final ValueProxy<CollectTimeType, Integer> proxy;
	/** 时间段分组 0:只配开始时间；1：配了一个时间段 */
	public final int periodGroup;

	CollectTimeType(Integer value, String label) {
		this(value, label, 0);
	}

	CollectTimeType(Integer value, String label, Integer periodGroup) {
		this.periodGroup = periodGroup;
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final CollectTimeType[] CACHE = values();

	public static CollectTimeType of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, 0);
	}

	/** 只配置了开始时间的集合 */
	public static final List<CollectTimeType> SINGLE_TIME_PERIODS = CollectionUtil.filter(Arrays.asList(CACHE), t -> t.periodGroup == 1);
	/** 配置了时间段的集合 */
	public static final List<CollectTimeType> TIME_PERIODS = CollectionUtil.filter(Arrays.asList(CACHE), t -> t.periodGroup == 2);
}