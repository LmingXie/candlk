package com.candlk.context.model;

import javax.annotation.Nullable;

import com.candlk.common.model.ValueProxy;
import com.candlk.common.util.Common;
import lombok.Getter;

@Getter
public enum SiteStatus implements LabelI18nProxy<SiteStatus, Integer> {
	LOGOFF(-7, "注销"),
	FROZEN(-6, "冻结"),
	LOGOFF_REVIEW(-5, "注销待集团审"),
	PROHIBIT_GAMES(-4, "禁止游戏"),
	BACKGROUND_LIMITATION(-3, "后台限制"),
	WARN(-2, "预警状态"),
	MAINTAIN(-1, "维护"),
	INIT(0, "建设中"),
	NORMAL(1, "正常");

	// 定义私有变量
	public final Integer value;
	final ValueProxy<SiteStatus, Integer> proxy;

	SiteStatus(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final SiteStatus[] CACHE = values();

	public static SiteStatus of(@Nullable Integer value) {
		return Common.getEnum(CACHE, SiteStatus::getValue, value);
	}

}
