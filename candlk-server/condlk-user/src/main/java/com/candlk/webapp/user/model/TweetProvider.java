package com.candlk.webapp.user.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.candlk.common.model.ValueProxy;
import com.candlk.common.model.ValueProxyImpl;
import lombok.Getter;
import me.codeplayer.util.ArrayUtil;
import org.apache.commons.lang3.EnumUtils;

/**
 * 第三方推文厂商类型
 */
@Getter
public enum TweetProvider implements ValueProxyImpl<TweetProvider, Integer> {
	/** <a href="https://axiom.trade/trackers">Axiom</a> */
	AXIOM("Axiom"),
	/** <a href="https://www.x3.pro/trending-tweets">X3</a> */
	X3("X3", false),
	;

	@EnumValue
	public final Integer value;
	public final String label;
	public final boolean open;
	final ValueProxy<TweetProvider, Integer> proxy;

	TweetProvider(String label, boolean open) {
		this.value = ordinal();
		this.label = label;
		this.open = open;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	TweetProvider(String label) {
		this(label, true);
	}

	public static final TweetProvider[] CACHE = ArrayUtil.filter(values(), TweetProvider::isOpen);

	public static TweetProvider of(String value) {
		return EnumUtils.getEnum(TweetProvider.class, value);
	}

}
