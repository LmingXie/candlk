package com.candlk.webapp.user.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.candlk.common.model.ValueProxy;
import com.candlk.common.model.ValueProxyImpl;
import lombok.Getter;
import me.codeplayer.util.ArrayUtil;
import org.apache.commons.lang3.EnumUtils;

/** 推文类型 */
@Getter
public enum TweetType implements ValueProxyImpl<TweetType, String> {
	/** 发帖 */
	TWEET("tweet"),
	/** 回复 */
	REPLY("reply"),
	/** 引用 */
	QUOTE("quote"),
	/** 转发 */
	RETWEET("retweet"),
	;

	@EnumValue
	public final String value;
	public final String label;
	public final boolean open;
	final ValueProxy<TweetType, String> proxy;

	TweetType(String label, boolean open) {
		this.value = name();
		this.label = label;
		this.open = open;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	TweetType(String label) {
		this(label, true);
	}

	public static final TweetType[] CACHE = ArrayUtil.filter(values(), TweetType::isOpen);

	public static TweetType of(String value) {
		return EnumUtils.getEnum(TweetType.class, value);
	}

}
