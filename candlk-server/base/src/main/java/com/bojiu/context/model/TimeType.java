package com.bojiu.context.model;

import java.util.Calendar;
import java.util.Date;

import com.bojiu.common.model.TimeInterval;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import lombok.Getter;
import me.codeplayer.util.EasyDate;
import org.jspecify.annotations.Nullable;

import static com.bojiu.context.i18n.UserModelI18nKey.*;

@Getter
public enum TimeType implements LabelI18nProxy<TimeType, Integer> {
	/** 今天 */
	TODAY(1, TIME_TYPE_TODAY, 0),
	/** 昨天 */
	YESTERDAY(2, TIME_TYPE_YESTERDAY, -1),
	/** 7天 */
	DAYS_7(3, TIME_TYPE_DAYS_7, -7),
	/** 15天 */
	DAYS_15(4, TIME_TYPE_DAYS_15, -15),
	/** 30天 */
	DAYS_30(5, TIME_TYPE_DAYS_30, -30),
	/** 本周 */
	THIS_WEEK(6, TIME_TYPE_THIS_WEEK, -7),
	/** 上周 */
	LAST_WEEK(7, TIME_TYPE_LAST_WEEK, -14),
	/** 本月 */
	THIS_MONTH(8, TIME_TYPE_THIS_MONTH, -30),
	/** 上月 */
	LAST_MONTH(9, TIME_TYPE_LAST_MONTH, -60),
	;

	public final Integer value;
	public final Integer offsetDays;
	final ValueProxy<TimeType, Integer> proxy;

	// 构造函数，枚举类型只能为私有
	TimeType(Integer value, String label, Integer offsetDays) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
		this.offsetDays = offsetDays;
	}

	public static final TimeType[] CACHE = { TODAY, YESTERDAY, DAYS_7, DAYS_15, DAYS_30 };
	public static final TimeType[] WG_CACHE = { TODAY, YESTERDAY, THIS_WEEK, LAST_WEEK, THIS_MONTH, LAST_MONTH };

	public static TimeType of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, +1);
	}

	public static TimeType ofAll(@Nullable Integer value) {
		return Common.getEnum(values(), value, +1);
	}

	public TimeInterval toInterval(@Nullable EasyDate base) {
		if (base == null) {
			base = new EasyDate();
		}
		Date endTime, beginTime;
		switch (this) {
			case YESTERDAY -> {
				endTime = base.addDay(offsetDays).endOf(Calendar.DATE).toDate();
				beginTime = base.beginOf(Calendar.DATE).toDate();
			}
			case TODAY, DAYS_7, DAYS_15, DAYS_30 -> {
				endTime = base.endOf(Calendar.DATE).toDate();
				beginTime = base.addDay(offsetDays).beginOf(Calendar.DATE).toDate();
			}
			case THIS_WEEK -> {
				endTime = base.addDay(7 - base.getWeekDay()).endOf(Calendar.DATE).toDate(); // 周日
				beginTime = base.addDay(1 - base.getWeekDay()).beginOf(Calendar.DATE).toDate();
			}
			case LAST_WEEK -> {
				base.addDay(-base.getWeekDay() - 1).beginOf(Calendar.DATE);
				endTime = base.endOf(Calendar.DAY_OF_WEEK).toDate();
				beginTime = base.addDay(1 - base.getWeekDay()).beginOf(Calendar.DATE).toDate();
			}
			case THIS_MONTH, LAST_MONTH -> {
				if (this == LAST_MONTH) { // 时间设置到上月
					base.beginOf(Calendar.MONTH).addDay(-1);
				}
				endTime = base.endOf(Calendar.MONTH).toDate();
				beginTime = base.beginOf(Calendar.MONTH).toDate();
			}
			default -> throw new UnsupportedOperationException();
		}
		return new TimeInterval(beginTime, endTime, -1, -1);
	}

	public TimeInterval toInterval() {
		return toInterval(null);
	}

}