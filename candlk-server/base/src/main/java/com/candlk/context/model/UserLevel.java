package com.candlk.context.model;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;

import com.candlk.common.model.ValueProxy;
import com.candlk.common.util.Common;
import lombok.Getter;
import me.codeplayer.util.CollectionUtil;
import me.codeplayer.util.StringUtil;

/**
 * 用户VIP等级
 */
@Getter
public enum UserLevel implements LabelI18nProxy<UserLevel, Integer> {
	/** VIP1 */
	VIP0(0, "VIP0"),
	/** VIP1 */
	VIP1(1, "VIP1"),
	/** VIP2 */
	VIP2(2, "VIP2"),
	/** VIP3 */
	VIP3(3, "VIP3"),
	/** VIP4 */
	VIP4(4, "VIP4"),
	/** VIP5 */
	VIP5(5, "VIP5"),
	/** VIP2 */
	VIP6(6, "VIP6"),
	/** VIP7 */
	VIP7(7, "VIP7"),
	/** VIP8 */
	VIP8(8, "VIP8"),
	/** VIP9 */
	VIP9(9, "VIP9"),
	/** VIP10 */
	VIP10(10, "VIP10");

	// 定义私有变量
	public final Integer value;
	final ValueProxy<UserLevel, Integer> proxy;

	// 构造函数，枚举类型只能为私有
	UserLevel(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final UserLevel[] CACHE = values();
	static final List<Integer> levelValues = Common.toList(Arrays.asList(CACHE), UserLevel::getValue);

	public static UserLevel of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, 0);
	}

	/**
	 * 验证用户VIP等级是否存在
	 */
	public static boolean isExist(List<Integer> levels) {
		return CollectionUtil.filter(levels, v -> !levelValues.contains(v)).isEmpty();
	}

	public static String labels(String values) {
		return StringUtil.join(Common.splitAsIntList(values), t -> CACHE[t].getProxy().getLabel(), ",");
	}
}
