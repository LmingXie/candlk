package com.bojiu.context.model;

import java.util.Calendar;
import java.util.Date;
import javax.annotation.Nullable;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.context.Env;
import com.bojiu.common.model.TimeInterval;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import com.bojiu.context.i18n.AdminI18nKey;
import lombok.Getter;
import me.codeplayer.util.EasyDate;

/**
 * 循环方式
 */
@Getter
public enum CycleMode implements LabelI18nProxy<CycleMode, Integer> {
	/** 单次活动 */
	SINGLE(1, AdminI18nKey.CYCLE_MODE_SINGLE, -1),
	/** 每日循环 */
	DAILY_CYCLE(2, AdminI18nKey.CYCLE_MODE_DAILY_CYCLE, 1),
	/** 每周循环 */
	WEEKLY_CYCLE(3, AdminI18nKey.CYCLE_MODE_WEEKLY_CYCLE, 7),
	/** 每月循环 */
	MONTHLY_CYCLE(4, AdminI18nKey.CYCLE_MODE_MONTHLY_CYCLE, 31),
	/** 每季度循环 */
	QUARTERLY_CYCLE(5, AdminI18nKey.CYCLE_MODE_QUARTERLY_CYCLE, 92),
	/** 每半年循环 */
	SEMIYEARLY_CYCLE(6, AdminI18nKey.CYCLE_MODE_SEMIYEARLY_CYCLE, 183),
	/** 每小时循环 */
	HOUR_CYCLE(7, AdminI18nKey.CYCLE_MODE_HOUR_CYCLE, 1),
	/** 每年循环 */
	YEARLY_CYCLE(8, AdminI18nKey.CYCLE_MODE_YEARLY_CYCLE, 365),
	;

	@EnumValue
	public final Integer value;
	final ValueProxy<CycleMode, Integer> proxy;
	/** 对应循环模式的生命周期天数：-1=无限制，随结束时间而结束；> 0 对应的【最大】有效期天数 */
	public final int days;

	CycleMode(Integer value, String label, int days) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
		this.days = days;
	}

	public static final CycleMode[] CACHE = values();
	/** 活动循环方式 */
	public static final CycleMode[] PROMOTION_CYCLE_CACHE = { SINGLE, DAILY_CYCLE, WEEKLY_CYCLE };
	/** 公积金循环方式 */
	public static final CycleMode[] DEPOSIT_POOL_CYCLE_CACHE = { SINGLE, MONTHLY_CYCLE, QUARTERLY_CYCLE, !Env.outer() ? HOUR_CYCLE : SEMIYEARLY_CYCLE, YEARLY_CYCLE };
	/** 任务循环方式 */
	public static final CycleMode[] CACHE_TASK = { DAILY_CYCLE, WEEKLY_CYCLE };
	/** 日/周/月循环方式 */
	public static final CycleMode[] CACHE_DWM = { DAILY_CYCLE, WEEKLY_CYCLE, MONTHLY_CYCLE };
	/** 季度值 */
	public static final Integer[] QUARTERLY_VALUES = { 3, 6, 9, 12 };
	/** 消息发送循环方式 */
	public static final CycleMode[] MSG_CYCLE_CACHE = { SINGLE, DAILY_CYCLE, WEEKLY_CYCLE, MONTHLY_CYCLE };

	public static CycleMode of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, 1);
	}

	public static CycleMode ofTask(@Nullable Integer value) {
		return Common.getEnum(CACHE_TASK, value);
	}

	/**
	 * 获取时间范围
	 */
	public TimeInterval getTimeInterval(final EasyDate zonedDate, Date beginTime, Date endTime) {
		return switch (this) {
			case SINGLE -> {
				long oldTime = zonedDate.getTime();
				TimeInterval interval = new TimeInterval(
						zonedDate.setDate(beginTime).beginOf(Calendar.DATE).toDate(),
						zonedDate.setDate(endTime).endOf(Calendar.DATE).toDate(),
						-1, -1);
				zonedDate.setTime(oldTime);
				yield interval;
			}
			case WEEKLY_CYCLE -> TimeInterval.ofFast(zonedDate, Calendar.DAY_OF_WEEK);
			case MONTHLY_CYCLE -> TimeInterval.ofFast(zonedDate, Calendar.MONTH);
			case DAILY_CYCLE -> TimeInterval.ofFast(zonedDate, Calendar.DATE);
			case HOUR_CYCLE -> TimeInterval.ofFast(zonedDate, Calendar.HOUR_OF_DAY); // 每小时循环
			case QUARTERLY_CYCLE -> extractMonthRange(zonedDate, 3);
			case SEMIYEARLY_CYCLE -> extractMonthRange(zonedDate, 6);
			case YEARLY_CYCLE -> extractMonthRange(zonedDate, 12);
		};
	}

	static TimeInterval extractMonthRange(final EasyDate zonedNow, final int unitMonths) {
		final long oldTime = zonedNow.getTime();
		final int month = zonedNow.getMonth() /* 当前月份 */, diff = (month - 1) % unitMonths;
		final int from = month - diff, to = from + unitMonths - 1;
		Date begin = zonedNow.setMonth(from).beginOf(Calendar.MONTH).toDate();
		Date end = zonedNow.setMonth(to).endOf(Calendar.MONTH).toDate();
		zonedNow.setTime(oldTime);
		return new TimeInterval(begin, end, -1, 1);
	}

	/**
	 * 根据循环模式获取 Calendar模式(目前支持小时,日,周,月,年)
	 */
	public static Integer castCalendar(CycleMode cycleMode) {
		return switch (cycleMode) {
			case DAILY_CYCLE:
				yield Calendar.DATE;
			case WEEKLY_CYCLE:
				yield Calendar.DAY_OF_WEEK;
			case MONTHLY_CYCLE:
				yield Calendar.MONTH;
			case HOUR_CYCLE:
				yield Calendar.HOUR_OF_DAY; // 每小时循环
			case YEARLY_CYCLE:
				yield Calendar.YEAR;
			default:
				yield null;
		};
	}

}