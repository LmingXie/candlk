package com.candlk.context.model;

import java.util.Calendar;
import java.util.Date;
import javax.annotation.Nullable;

import com.candlk.common.model.TimeInterval;
import com.candlk.common.model.ValueProxy;
import com.candlk.common.util.Common;
import lombok.Getter;
import me.codeplayer.util.EasyDate;

@Getter
public enum TimeType implements LabelI18nProxy<TimeType, Integer> {
	/** 今天 */
	TODAY(1, "今天", 0),
	/** 昨天 */
	YESTERDAY(2, "昨天", -1),
	/** 7天 */
	DAYS_7(3, "7天", -7),
	/** 15天 */
	DAYS_15(4, "15天", -15),
	/** 30天 */
	DAYS_30(5, "30天", -30);

	// 定义私有变量
	public final Integer value;
	public final Integer offsetDays;
	final ValueProxy<TimeType, Integer> proxy;

	// 构造函数，枚举类型只能为私有
	TimeType(Integer value, String label, Integer offsetDays) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
		this.offsetDays = offsetDays;
	}

	public static final TimeType[] CACHE = values();

	public static TimeType of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, +1);
	}

	public TimeInterval toInterval(@Nullable EasyDate base) {
		if (base == null) {
			base = new EasyDate();
		}
		Date beginTime = base.addDay(offsetDays).beginOf(Calendar.DATE).toDate();
		Date endTime = base.endOf(Calendar.DATE).toDate();
		return new TimeInterval(beginTime, endTime, -1, -1);
	}

	public TimeInterval toInterval() {
		return toInterval(null);
	}

}
