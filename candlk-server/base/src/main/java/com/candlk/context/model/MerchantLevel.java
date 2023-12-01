package com.candlk.context.model;

import javax.annotation.Nullable;

import com.candlk.common.model.ValueProxy;
import com.candlk.common.util.Common;
import lombok.Getter;

/**
 * 商户VIP等级
 */
@Getter
public enum MerchantLevel implements LabelI18nProxy<MerchantLevel, Integer> {

	/** VIP1 */
	VIP1(1, "1级"),
	/** VIP2 */
	VIP2(2, "2级"),
	/** VIP3 */
	VIP3(3, "3级"),
	/** VIP4 */
	VIP4(4, "4级"),
	/** VIP5 */
	VIP5(5, "5级"),
	/** VIP2 */
	VIP6(6, "6级"),
	/** VIP7 */
	VIP7(7, "7级"),
	/** VIP8 */
	VIP8(8, "8级"),
	/** VIP9 */
	VIP9(9, "9级");

	// 定义私有变量
	public final Integer value;
	final ValueProxy<MerchantLevel, Integer> proxy;

	// 构造函数，枚举类型只能为私有
	MerchantLevel(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final MerchantLevel[] CACHE = values();

	public static MerchantLevel of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, +1);
	}
}
