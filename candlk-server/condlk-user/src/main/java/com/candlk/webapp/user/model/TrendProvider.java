package com.candlk.webapp.user.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.candlk.common.model.ValueProxy;
import com.candlk.common.model.ValueProxyImpl;
import lombok.Getter;
import me.codeplayer.util.ArrayUtil;
import org.apache.commons.lang3.EnumUtils;

/** 趋势热词生厂商 */
@Getter
public enum TrendProvider implements ValueProxyImpl<TrendProvider, Integer> {
	/** 手动导入 */
	CUSTOM("Custom", false),
	/** <a href="https://trends.google.com/trending?geo=US&hours=4">Google</a> */
	GOOGLE("Google"),
	/** <a href="https://trends24.in/united-states/">trends24</a> */
	TRENDS24("Trends24"),
	/** <a href="https://redditlist.com/nsfw.html">RedditList</a> */
	REDDIT_LIST("RedditList"),
	;

	@EnumValue
	public final Integer value;
	public final String label;
	public final boolean open;
	final ValueProxy<TrendProvider, Integer> proxy;

	TrendProvider(String label, boolean open) {
		this.value = ordinal();
		this.label = label;
		this.open = open;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	TrendProvider(String label) {
		this(label, true);
	}

	public static final TrendProvider[] CACHE = ArrayUtil.filter(values(), TrendProvider::isOpen);

	public static TrendProvider of(String value) {
		return EnumUtils.getEnum(TrendProvider.class, value);
	}

}
