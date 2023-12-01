package com.candlk.context.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.candlk.common.model.ValueProxy;
import lombok.Getter;
import me.codeplayer.util.ArrayUtil;
import org.apache.commons.lang3.EnumUtils;

/**
 * 货币
 */
@Getter
public enum Currency implements LabelI18nProxy<Currency, String> {

	/** 美元（美国） */
	USD("USD", "USD"),
	/** 雷亚尔（巴西） */
	BRL("BRL", "BRL"),
	/** 越南盾（越南） */
	VND("VND(1:1000)", false, "VND", 1000);

	/** 本系统的代码值 */
	@EnumValue
	public final String value;
	/** ISO 国际标准货币代码 */
	public final String code;
	/** 兑换比例。如果为 N，即为 1:N，表示 需要将 用户账户余额 先除以 N 再带入游戏 */
	public final Integer exchangeRatio;
	final ValueProxy<Currency, String> proxy;
	/** 开关 */
	final boolean open;

	Currency(String label, boolean open, String code, Integer exchangeRatio) {
		this.value = name();
		this.proxy = new ValueProxy<>(this, value, label);
		this.code = code;
		this.exchangeRatio = exchangeRatio;
		this.open = open;
	}

	Currency(String label, String code) {
		this(label, true, code, 1);
	}

	public static final Currency[] CACHE = ArrayUtil.filter(Currency.values(), Currency::isOpen);

	public static Currency of(String value) {
		return EnumUtils.getEnum(Currency.class, value);
	}

}
