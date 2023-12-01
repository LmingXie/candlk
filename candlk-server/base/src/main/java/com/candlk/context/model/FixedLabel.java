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
 * 固定标签
 */
@Getter
public enum FixedLabel implements LabelI18nProxy<FixedLabel, Integer> {
	/** 可疑 */
	DUBIOUS(0, "可疑", "操作行为可疑，需留意的玩家"),
	/** 恶意 */
	MALEVOLENCE(1, "恶意", "明确属于恶意注册、举报投诉、不遵守规则的玩家"),
	/** 套利 */
	INTEREST_ARBITRAGE(2, "套利", "明确属于套利、刷水等行为"),
	/** 诈骗 */
	SWINDLE(3, "诈骗", "存在欺骗、骗单或诈骗"),
	/** 获利异常 */
	PROFIT_ANOMALY(4, "获利异常", "【系统监控】获利监控异常用户"),
	/** 刷子 */
	BOT(5, "刷子", "【系统监控】刷子监控用户"),
	;

	// 定义私有变量
	private final Integer value;
	private final String desc;
	final ValueProxy<FixedLabel, Integer> proxy;

	// 构造函数，枚举类型只能为私有
	FixedLabel(Integer value, String label, String desc) {
		this.value = value;
		this.desc = desc;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final FixedLabel[] CACHE = values();

	public String renderDesc(@Nullable Object val) {
		return desc.replace("{}", val == null ? "0" : val.toString());
	}

	public static FixedLabel of(@Nullable Integer value) {
		return value == null ? null : CACHE[value];
	}

	static final List<Integer> fixValues = Common.toList(Arrays.asList(CACHE), FixedLabel::getValue);

	/**
	 * 验证层级是否存在
	 */
	public static boolean isExist(List<Integer> fixLayers) {
		return CollectionUtil.filter(fixLayers, v -> !fixValues.contains(v)).isEmpty();
	}

	public static String labels(String values) {
		return StringUtil.join(Common.splitAsIntList(values), t -> CACHE[t].getProxy().getLabel(), ",");
	}
}
