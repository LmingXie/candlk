package com.candlk.common.model;

import java.util.*;

import lombok.Getter;
import me.codeplayer.util.EasyDate;

/**
 * 时间区间类，用于存储表示指定时间范围的开始时间 {@code begin} 和结束时间 {@code end} 临界点
 *
 * @date 2015年4月19日
 * @since 1.0
 */
public class TimeInterval implements Interval<Date> {

	protected Date begin, end;

	/** 开始时间 默认不修正 */
	@Getter
	protected int beginCalendarField = -1;
	/** 结束时间 默认修正到当天的结束时间 */
	@Getter
	protected int endCalendarField = Calendar.DATE /* 与 Calendar.DAY_OF_MONTH 的值相同 */;

	@Getter
	protected TimeZone timeZone;
	protected transient EasyDate easyDate;

	public TimeInterval() {
	}

	public TimeInterval(Date begin, Date end, int beginField, int endField) {
		this.begin = begin;
		this.end = end;
		this.beginCalendarField = beginField;
		this.endCalendarField = endField;
	}

	public TimeInterval(TimeInterval interval, int beginField, int endField) {
		if (interval != null) {
			this.begin = interval.getBegin();
			this.end = interval.getEnd();
		}
		this.beginCalendarField = beginField;
		this.endCalendarField = endField;
	}

	public TimeInterval(Date begin, Date end) {
		this.begin = begin;
		this.end = end;
	}

	@Override
	public boolean endClosed() {
		return true;
	}

	/**
	 * 请参考 {@link #of(Date, int) }
	 */
	public TimeInterval(Date date, int calendarField) {
		this.end = this.begin = date;
		beginCalendarField = endCalendarField = calendarField;
	}

	@Override
	public Date getBegin() {
		return begin;
	}

	@Override
	public Date getFinalBegin() {
		if (begin != null && beginCalendarField > 0) {
			final EasyDate d = getEasyDate(begin);
			if (endCalendarField == Calendar.DAY_OF_WEEK) {
				final int weekDay = d.getWeekDay();
				if (weekDay != 1) {
					d.addDay(1 - weekDay); // 周一
				}
				return d.beginOf(Calendar.DATE).toDate();
			}
			return d.beginOf(beginCalendarField).toDate();
		}
		return begin;
	}

	public void setBegin(Date begin) {
		this.begin = begin;
	}

	@Override
	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public void setBeginCalendarField(int beginCalendarField) {
		this.beginCalendarField = beginCalendarField;
	}

	public void setEndCalendarField(int endCalendarField) {
		this.endCalendarField = endCalendarField;
	}

	@Override
	public Date getFinalEnd() {
		if (end != null && endCalendarField > 0) {
			final EasyDate d = getEasyDate(end);
			if (endCalendarField == Calendar.DAY_OF_WEEK) {
				final int weekDay = d.getWeekDay();
				if (weekDay != 7) {
					d.addDay(7 - weekDay); // 周日
				}
				return d.endOf(Calendar.DATE).toDate();
			}
			return d.endOf(endCalendarField).toDate();
		}
		return end;
	}

	public EasyDate getEasyDate(Date date) {
		EasyDate base = easyDate;
		if (base == null) {
			easyDate = base = new EasyDate(date);
			if (timeZone != null && base.getTimeZone() != timeZone) {
				base.setTimeZone(timeZone);
			}
		} else {
			base.setDate(date);
		}
		return base;
	}

	public TimeInterval setEasyDate(EasyDate easyDate) {
		this.easyDate = easyDate;
		return this;
	}

	public TimeInterval setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
		return this;
	}

	/**
	 * 指示当前对象是否设置了属性值
	 *
	 * @return 只要 {@code begin } 和 {@code end } 中（至少）有一个不为 null，则返回 true
	 */
	public boolean hasValue() {
		return begin != null || end != null;
	}

	/**
	 * 根据指定时间设置时间区间的开始时间点和结束时间点，它们分别是指定时间在该时间计量单位区间内的最小值和最大值
	 *
	 * @param both 指定时间
	 * @param bothCalendarField 支持的时间字段有{@link Calendar#YEAR}、{@link Calendar#MONTH}、 {@link Calendar#DATE}、 {@link Calendar#HOUR_OF_DAY}、 {@link Calendar#MINUTE}、{@link Calendar#SECOND}
	 * @since 1.0
	 */
	public static TimeInterval of(Date both, int bothCalendarField) {
		return new TimeInterval(both, both, bothCalendarField, bothCalendarField);
	}

	/**
	 * 处理TimeInterval对象，并按照指定的时间字段设置区间边界
	 *
	 * @param interval TimeInterval对象
	 * @param beginCalendarField 开始时间的基准时间字段，如果不大于0，则表示忽略设置
	 * @param endCalendarField 结束时间的基准时间字段，如果不大于0，则表示忽略设置
	 * @since 1.0
	 */
	public static TimeInterval init(TimeInterval interval, int beginCalendarField, int endCalendarField) {
		if (interval != null) {
			interval.beginCalendarField = beginCalendarField;
			interval.endCalendarField = endCalendarField;
		}
		return interval;
	}

	/**
	 * 构造一个 {@code [ begin, begin.atEndOf(endCalendarField) ]} 的时间区间对象，区间上限为 {@code begin} 在当前指定的时间计量单位（年、月、日、时、分、秒）的结束时间点
	 *
	 * @param begin 指定的开始时间
	 * @param endCalendarField 支持的时间字段有{@link Calendar#YEAR}、{@link Calendar#MONTH}、 {@link Calendar#DATE}、 {@link Calendar#HOUR_OF_DAY}、 {@link Calendar#MINUTE}、{@link Calendar#SECOND}
	 * @since 1.0
	 */
	public static TimeInterval ofToEnd(Date begin, int endCalendarField) {
		return new TimeInterval(begin, begin, -1, endCalendarField);
	}

	/**
	 * 构造一个 {@code [ end.atEndOf(beginCalendarField), end ]} 的时间区间对象，区间下限为 {@code end} 在当前指定的时间计量单位（年、月、日、时、分、秒）的开始时间点
	 *
	 * @param end 指定的结束时间
	 * @param beginCalendarField 支持的时间字段有{@link Calendar#YEAR}、{@link Calendar#MONTH}、 {@link Calendar#DATE}、 {@link Calendar#HOUR_OF_DAY}、 {@link Calendar#MINUTE}、{@link Calendar#SECOND}
	 * @since 1.0
	 */
	public static TimeInterval ofFromBegin(Date end, int beginCalendarField) {
		return new TimeInterval(end, end, beginCalendarField, -1);
	}

	/**
	 * 根据指定时间设置时间区间的开始时间点和结束时间点，它们分别是指定时间在该时间计量单位区间内的最小值和最大值
	 *
	 * @param base 指定时间。内部【不会】修改该引用的时间值
	 * @param bothCalendarField 支持的时间字段有{@link Calendar#YEAR}、{@link Calendar#MONTH}、 {@link Calendar#DAY_OF_WEEK}
	 * {@link Calendar#DATE}、{@link Calendar#HOUR_OF_DAY}、 {@link Calendar#MINUTE}、{@link Calendar#SECOND}
	 * @since 1.0
	 */
	public static TimeInterval ofFast(final EasyDate base, int bothCalendarField) {
		if (bothCalendarField < 0) {
			return of(base.toDate(), -1);
		}
		final long originTime = base.getTime();
		final Date begin, end;
		if (bothCalendarField == Calendar.DAY_OF_WEEK) { // 一周
			begin = base.addDay(1 - base.getWeekDay()).beginOf(Calendar.DATE).toDate();
			end = base.addDay(6).endOf(Calendar.DATE).toDate();
		} else {
			begin = base.beginOf(bothCalendarField).toDate();
			end = base.endOf(bothCalendarField).toDate();
		}
		base.setTime(originTime);
		return new TimeInterval(begin, end, -1, -1);
	}

}
