package com.candlk.context.model;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.candlk.common.model.ValueProxy;
import com.candlk.common.util.Common;
import lombok.Getter;
import me.codeplayer.util.CollectionUtil;
import me.codeplayer.util.StringUtil;

/**
 * 自动层级
 */
@Getter
public enum AutoLayer implements LabelI18nProxy<AutoLayer, Integer> {
	/** v0福利 */
	L1(0, "v0福利"),
	/** v0盈利 */
	L2(1, "v0盈利"),
	/** v1福利 */
	L3(2, "v1福利"),
	/** v1提现 */
	L4(3, "v1提现"),
	/** v1成长 */
	L5(4, "v1成长"),
	/** v1盈利 */
	L6(5, "v1盈利"),
	/** v2福利 */
	L7(6, "v2福利"),
	/** v2提现 */
	L8(7, "v2提现"),
	/** v2成长 */
	L9(8, "v2成长"),
	/** v2盈利 */
	L10(9, "v2盈利"),
	/** v3福利 */
	L11(10, "v3福利"),
	/** v3提现 */
	L12(11, "v3提现"),
	/** v3成长 */
	L13(12, "v3成长"),
	/** v3盈利 */
	L14(13, "v3盈利"),
	/** v4福利 */
	L15(14, "v4福利"),
	/** v4提现 */
	L16(15, "v4提现"),
	/** v4成长 */
	L17(16, "v4成长"),
	/** v4盈利 */
	L18(17, "v4盈利"),
	/** v5福利 */
	L19(18, "v5福利"),
	/** v5提现 */
	L20(19, "v5提现"),
	/** v5成长 */
	L21(20, "v5成长"),
	/** v5盈利 */
	L22(21, "v5盈利"),
	/** v6福利 */
	L23(22, "v6福利"),
	/** v6提现 */
	L24(23, "v6提现"),
	/** v6成长 */
	L25(24, "v6成长"),
	/** v6盈利 */
	L26(25, "v6盈利"),
	/** v7福利 */
	L27(26, "v7福利"),
	/** v7提现 */
	L28(27, "v7提现"),
	/** v7成长 */
	L29(28, "v7成长"),
	/** v7盈利 */
	L30(29, "v7盈利"),
	/** v8福利 */
	L31(30, "v8福利"),
	/** v8提现 */
	L32(31, "v8提现"),
	/** v8成长 */
	L33(32, "v8成长"),
	/** v8盈利 */
	L34(33, "v8盈利"),
	/** v9福利 */
	L35(34, "v9福利"),
	/** v9提现 */
	L36(35, "v9提现"),
	/** v9成长 */
	L37(36, "v9成长"),
	/** v9盈利 */
	L38(37, "v9盈利"),
	/** v10福利 */
	L39(38, "v10福利"),
	/** v10提现 */
	L40(39, "v10提现"),
	/** v10成长 */
	L41(40, "v10成长"),
	/** v10盈利 */
	L42(41, "v10盈利"),
	;
	// 定义私有变量
	@EnumValue
	public final Integer value;
	final ValueProxy<AutoLayer, Integer> proxy;

	AutoLayer(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final AutoLayer[] CACHE = values();

	public static AutoLayer of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, 0);
	}

	static final List<Integer> autoValues = Common.toList(Arrays.asList(CACHE), AutoLayer::getValue);

	/**
	 * 验证层级是否存在
	 */
	public static boolean isExist(List<Integer> fixLayers) {
		return CollectionUtil.filter(fixLayers, v -> !autoValues.contains(v)).isEmpty();
	}

	public static String labels(String values) {
		return StringUtil.join(Common.splitAsIntList(values), t -> CACHE[t].getProxy().getLabel(), ",");
	}
}