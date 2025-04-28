package com.candlk.webapp.user.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.candlk.common.model.ValueProxy;
import com.candlk.common.model.ValueProxyImpl;
import lombok.Getter;

/** 推文账号类型 */
@Getter
public enum TweetUserType implements ValueProxyImpl<TweetUserType, Integer> {
	/** 普通账号 */
	ORDINARY("ordinary"),
	/** 特殊关注账号 */
	SPECIAL("special"),
	/** 二级账号 */
	LEVEL2("level2"),
	;

	@EnumValue
	public final Integer value;
	public final String label;
	final ValueProxy<TweetUserType, Integer> proxy;

	TweetUserType(String label) {
		this.value = ordinal();
		this.label = label;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final TweetUserType[] CACHE = values();

	public static TweetUserType of(Integer value) {
		return ORDINARY.getValueOf(value);
	}

}
