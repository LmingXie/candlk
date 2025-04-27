package com.candlk.webapp.user.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.candlk.common.model.ValueProxy;
import com.candlk.common.model.ValueProxyImpl;
import lombok.Getter;
import me.codeplayer.util.ArrayUtil;

/** 推文类型 */
@Getter
public enum TweetType implements ValueProxyImpl<TweetType, Integer> {
	/** 发帖 */
	TWEET("tweet"),
	/** 回复 */
	REPLY("reply"),
	/** 引用 */
	QUOTE("quote"),
	/** 转发 */
	RETWEET("retweet", false),
	;

	@EnumValue
	public final Integer value;
	public final String label;
	public final boolean open;
	final ValueProxy<TweetType, Integer> proxy;

	TweetType(String label, boolean open) {
		this.value = ordinal();
		this.label = label;
		this.open = open;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	TweetType(String label) {
		this(label, true);
	}

	public static final TweetType[] CACHE = ArrayUtil.filter(values(), TweetType::isOpen);

	public static TweetType of(Integer value) {
		return TWEET.getValueOf(value);
	}

	public static TweetType of(String value) {
		for (TweetType tweetType : CACHE) {
			if (tweetType.label.equalsIgnoreCase(value)) {
				return tweetType;
			}
		}
		return null;
	}

}
