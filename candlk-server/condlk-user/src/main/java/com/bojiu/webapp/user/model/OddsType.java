package com.bojiu.webapp.user.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.model.ValueProxyImpl;
import lombok.Getter;
import org.apache.commons.lang3.EnumUtils;

/** 盘口类型/赔率类型 */
@Getter
public enum OddsType implements ValueProxyImpl<OddsType, Integer> {
	/** 全场让球盘（Runline） */
	R("全场让球", PeriodType.FULL),
	/** 全场大小盘（Over/Under） */
	OU("全场大小", PeriodType.FULL),
	/** 全场独赢盘/胜负平（Moneyline） */
	M("全场独赢", PeriodType.FULL),
	/** 上半场让球盘（Runline） */
	HR("上半场让球", PeriodType.HALF),
	/** 上半场大小盘（Over/Under） */
	HOU("上半场大小", PeriodType.HALF),
	/** 上半场独赢盘/胜负平（Moneyline） */
	HM("上半场独赢", PeriodType.HALF),
	/** 双方球队进球（Both Teams To Score） */
	TS("双方球队进球", PeriodType.FULL),
	/** 单双盘（预测比赛最终总进球数是单数 (Odd) 还是双数 (Even)） */
	EO("单双盘", PeriodType.FULL),
	;

	@EnumValue
	public final Integer value;
	public final String label;
	public final boolean open;
	public final PeriodType type;
	final ValueProxy<OddsType, Integer> proxy;

	OddsType(String label, PeriodType type) {
		this(label, true, type);
	}

	OddsType(String label, boolean open, PeriodType type) {
		this.value = ordinal();
		this.label = label;
		this.proxy = new ValueProxy<>(this, value, label);
		this.open = open;
		this.type = type;
	}

	public static final OddsType[] CACHE = values();

	public static OddsType of(String value) {
		return EnumUtils.getEnum(OddsType.class, value);
	}

}