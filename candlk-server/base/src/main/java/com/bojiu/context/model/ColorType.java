package com.bojiu.context.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import lombok.Getter;

import static com.bojiu.context.i18n.UserModelI18nKey.COLOR_TYPE_BLUE_PURPLE;

/**
 * 皮肤颜色类型
 */
@Getter
public enum ColorType implements LabelI18nProxy<ColorType, Integer> {

	/** 蓝紫色 */
	BLUE_PURPLE(1, COLOR_TYPE_BLUE_PURPLE),
	;
	@EnumValue
	public final Integer value;
	final ValueProxy<ColorType, Integer> proxy;

	ColorType(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final ColorType[] CACHE = values();

	public static ColorType of(Integer value) {
		return Common.getEnum(CACHE, value, 1);
	}
}