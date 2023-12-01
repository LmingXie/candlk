package com.candlk.common.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lombok.Getter;

@Getter
public enum Status implements ValueProxyImpl<Status, Integer> {
	/** 正面状态。表示"是"、"开放"、"启用"等 */
	YES(1, "是"),
	/** 负面状态。表示"否"、"关闭"、"禁用"等 */
	NO(0, "否");

	public final Integer value;
	public final String label;
	final ValueProxy<Status, Integer> proxy;

	Status(Integer value, String label) {
		this.value = value;
		this.label = label;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public boolean eq(@Nullable Integer val) {
		return value.equals(val);
	}

	public boolean isYes() {
		return this == YES;
	}

	@Nonnull
	public static Status of(boolean yesOrNo) {
		return yesOrNo ? YES : NO;
	}

	@Nonnull
	public static Status of(@Nullable Integer val) {
		return asResult(val) ? YES : NO;
	}

	@Nonnull
	public static Integer valueOf(@Nullable Integer val) {
		return valueOf(asResult(val));
	}

	public static Boolean asBoolean(@Nullable Integer val) {
		return val == null ? null : asResult(val);
	}

	public static boolean asResult(@Nullable Integer val) {
		return YES.value.equals(val);
	}

	@Nonnull
	public static Integer valueOf(boolean yesOrNo) {
		return yesOrNo ? YES.value : NO.value;
	}

	/** 取反 */
	public Status getReverse() {
		return this == YES ? NO : YES;
	}

	/** 返回 "启用" 或 "停用" */
	public String getToggleLabel() {
		return this == Status.YES ? "启用" : "停用";
	}

	/** 返回 "显示" 或 "隐藏" */
	public String getDisplayLabel() {
		return this == Status.YES ? "显示" : "隐藏";
	}

	public static Status[] getValues() {
		return ValueProxy.getCachedArray(Status.class, Status::values);
	}

}
