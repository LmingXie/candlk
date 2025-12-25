package com.bojiu.context.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.model.ValueProxyImpl;
import com.bojiu.context.AppRegion;
import lombok.Getter;
import me.codeplayer.util.ArrayUtil;
import org.apache.commons.lang3.EnumUtils;
import org.jspecify.annotations.Nullable;

/**
 * 货币
 */
@Getter
public enum Currency implements ValueProxyImpl<Currency, String> {

	/** 美元（美国） */
	USD("USD", false, "USD", "$"),
	/** 雷亚尔（巴西） */
	BRL("BRL", AppRegion.inBr(), "BRL", "R$"),
	/** 比索（菲律宾） */
	PHP("PHP", false, "PHP", 1000, "₱"),
	/** 印尼盾（印度尼西亚） */
	IDR("IDR", false, "IDR", 1000, "Rp"),
	/** 卢比（印度） */
	INR("INR", AppRegion.inAsia(), "INR", "₹"),
	/** 林吉特（马来西亚） */
	MYR("MYR", false, "MYR", "$"),
	/** 越南盾（越南） */
	VND("VND", false, "VND", "₫"),
	//
	;

	/** 本系统的代码值 */
	@EnumValue
	public final String value;
	/** ISO 国际标准货币代码 */
	public final String code;
	/** 货币符号 */
	public final String symbol;
	/** 兑换比例。如果为 N，即为 1:N，表示 需要将 用户账户余额 先乘以 N 再带入游戏 */
	public final Integer exchangeRatio;
	final ValueProxy<Currency, String> proxy;
	/** 开关 */
	final boolean open;

	Currency(String label, boolean open, String code, Integer exchangeRatio, String symbol) {
		this.value = name();
		this.proxy = new ValueProxy<>(this, value, label);
		this.code = code;
		this.symbol = symbol;
		this.exchangeRatio = exchangeRatio;
		this.open = open;
	}

	Currency(String label, String code, String symbol) {
		this(label, true, code, symbol);
	}

	Currency(String label, boolean open, String code, String symbol) {
		this(label, open, code, 1, symbol);
	}

	public static final Currency[] CACHE = ArrayUtil.filter(Currency.values(), Currency::isOpen);

	public static final Currency PRIMARY = AppRegion.inBr() ? BRL : INR;
	public static final Currency[] FULL = { AppRegion.inBr() ? BRL : INR }; // 此处不能随意添加元素
	public static final Currency[] PHP_IDR = { PHP, IDR }; // 此处不能随意添加元素
	public static final Currency[] IDR_INR = { IDR, INR }; // 此处不能随意添加元素
	public static final Currency[] PHP_INR = { PHP, INR }; // 此处不能随意添加元素
	public static final Currency[] PHP_IDR_INR = { PHP, IDR, INR }; // 此处不能随意添加元素
	public static final Currency[] PHP_IDR_INR_VND = { PHP, IDR, INR, VND }; // 此处不能随意添加元素

	public static Currency of(String value) {
		return EnumUtils.getEnum(Currency.class, value);
	}

	public boolean needExchange() {
		return exchangeRatio > 1;
	}

	/** 根据兑换比例输出原生金额：1:1000比例，50 -> 50000 */

	public BigDecimal exchangeOut(@Nullable BigDecimal val) {
		return val == null ? BigDecimal.ZERO : (needExchange() ? val.multiply(BigDecimal.valueOf(exchangeRatio)) : val);
	}

	/**
	 * 根据兑换比例输出原生金额：1:1000比例，50 -> 50000
	 *
	 * @param val 金额
	 * @param scale 金额小数位
	 */
	public BigDecimal exchangeOut(@Nullable Long val, final int scale) {
		return val == null ? BigDecimal.ZERO : exchangeOut(BigDecimal.valueOf(val).movePointLeft(scale));
	}

	/** 根据兑换比例输出原生金额：1:1000比例，50 -> 50000 */
	public long exchangeOut(Long val) {
		return val == null ? 0L : needExchange() ? val * exchangeRatio : val;
	}

	/** 原生金额根据兑换比例转换：1:1000比例，50000 -> 50 */
	public BigDecimal exchangeIn(BigDecimal val) {
		return val == null ? BigDecimal.ZERO : (needExchange() ? val.divide(BigDecimal.valueOf(exchangeRatio), 2, RoundingMode.DOWN) : val);
	}

	/** 原生金额根据兑换比例转换：1:1000比例，50000 -> 50 */
	public long exchangeIn(Long val) {
		return val == null ? 0L : needExchange() ? val / exchangeRatio : val;
	}

}