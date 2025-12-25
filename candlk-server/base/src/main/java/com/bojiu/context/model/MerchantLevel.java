package com.bojiu.context.model;

import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

import static com.bojiu.context.model.BaseI18nKey.*;

/**
 * 商户VIP等级
 */
@Getter
public enum MerchantLevel implements LabelI18nProxy<MerchantLevel, Integer> {

	/** VIP1 */
	VIP1(1, MERCHANT_VIP_LEVEL_1),
	/** VIP2 */
	VIP2(2, MERCHANT_VIP_LEVEL_2),
	/** VIP3 */
	VIP3(3, MERCHANT_VIP_LEVEL_3),
	/** VIP4 */
	VIP4(4, MERCHANT_VIP_LEVEL_4),
	/** VIP5 */
	VIP5(5, MERCHANT_VIP_LEVEL_5),
	/** VIP2 */
	VIP6(6, MERCHANT_VIP_LEVEL_6),
	/** VIP7 */
	VIP7(7, MERCHANT_VIP_LEVEL_7),
	/** VIP8 */
	VIP8(8, MERCHANT_VIP_LEVEL_8),
	/** VIP9 */
	VIP9(9, MERCHANT_VIP_LEVEL_9);

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