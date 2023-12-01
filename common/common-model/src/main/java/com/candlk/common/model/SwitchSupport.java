package com.candlk.common.model;

import java.util.Calendar;
import java.util.Date;

import me.codeplayer.util.EasyDate;

public interface SwitchSupport extends StateBean {

	/** 活动开始时间 */
	Date getBeginTime();

	/** 活动结束时间 */
	Date getEndTime();

	default boolean isActive(long baseTime) {
		return isEnabled() && between(baseTime, getBeginTime(), getEndTime());
	}

	static boolean between(final long baseTime, Date beginTime, Date endTime) {
		return beginTime != null && beginTime.getTime() <= baseTime && (endTime == null || endTime.getTime() >= baseTime);
	}

	static boolean between(final Date baseTime, Date beginTime, Date endTime) {
		return between(baseTime.getTime(), beginTime, endTime);
	}

	static int statusValue(final long baseTime, long beginTime, Date endTime) {
		return statusValue(true, baseTime, beginTime, endTime);
	}

	static int statusValue(boolean isEnabled, final long baseTime, long beginTime, Date endTime) {
		if (!isEnabled || endTime != null && endTime.getTime() < baseTime) {
			return -1; // 已结束
		} else if (beginTime > baseTime) {
			return 0; // 即将开始
		} else {
			return 1; // 进行中
		}
	}

	@SuppressWarnings("deprecation")
	static int calcMinutesOfThatDay(Date date) {
		return date.getHours() * 60 + date.getMinutes();
	}

	static Date endTimeHandle(Date endTime, boolean inOrOut) {
		if (endTime == null) {
			return null;
		}
		return new EasyDate(endTime).addDay(inOrOut ? 1 : -1).beginOf(Calendar.DAY_OF_MONTH).toDate();
	}

	static int calcRemainDays(final long beginTime, final long endTime) {
		// 需要加 1 ms，因为 开始时间 ~ 结束时间 一般为"2021-01-01 00:00:00.000" ~ "2021-01-02 23:59:59.999" 形式
		final long remainInMs = endTime - beginTime + 1;
		if (remainInMs < EasyDate.MILLIS_OF_DAY) {
			return 0;
		}
		return (int) (remainInMs / EasyDate.MILLIS_OF_DAY);
	}

	/**
	 * 计算时间范围是否冲突
	 *
	 * @param targetBeginTime 目标开始时间
	 * @param targetEndTime 目标结束时间段
	 * @param beginTime 指定开始时间段
	 * @param endTime 指定结束时间段
	 */
	static boolean existsConflict(final Date targetBeginTime, final Date targetEndTime, final long beginTime, final long endTime) {
		return !((targetEndTime != null && targetEndTime.getTime() <= beginTime) || (targetBeginTime != null && targetBeginTime.getTime() >= endTime));
	}

}
