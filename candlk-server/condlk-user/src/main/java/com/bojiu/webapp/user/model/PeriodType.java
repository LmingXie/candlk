package com.bojiu.webapp.user.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.model.ValueProxyImpl;
import lombok.Getter;
import org.apache.commons.lang3.EnumUtils;

/** 周期类型 */
@Getter
public enum PeriodType implements ValueProxyImpl<PeriodType, Integer> {
	/** 全场 */
	FULL("全场"),
	/** 上半场 */
	HALF("上半场"),
	;

	@EnumValue
	public final Integer value;
	public final String label;
	final ValueProxy<PeriodType, Integer> proxy;

	PeriodType(String label, boolean open) {
		this.value = ordinal();
		this.label = label;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	PeriodType(String label) {
		this(label, true);
	}

	public static final PeriodType[] CACHE = values();

	public static PeriodType of(String value) {
		return EnumUtils.getEnum(PeriodType.class, value);
	}

}