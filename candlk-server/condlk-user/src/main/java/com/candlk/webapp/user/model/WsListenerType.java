package com.candlk.webapp.user.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.candlk.common.model.ValueProxy;
import com.candlk.common.model.ValueProxyImpl;
import lombok.Getter;
import me.codeplayer.util.ArrayUtil;
import org.apache.commons.lang3.EnumUtils;

/**
 * 第三方WebSocket监听器类型
 */
@Getter
public enum WsListenerType implements ValueProxyImpl<WsListenerType, String> {
	/** <a href="https://axiom.trade/trackers">Axiom</a> */
	AXIOM("Axiom", false),
	/** <a href="https://www.x3.pro/trending-tweets">X3</a> */
	X3("X3"),
	;

	@EnumValue
	public final String value;
	public final String label;
	public final boolean open;
	final ValueProxy<WsListenerType, String> proxy;

	WsListenerType(String label, boolean open) {
		this.value = name();
		this.label = label;
		this.open = open;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	WsListenerType(String label) {
		this(label, true);
	}

	public static final WsListenerType[] CACHE = ArrayUtil.filter(values(), WsListenerType::isOpen);

	public static WsListenerType of(String value) {
		return EnumUtils.getEnum(WsListenerType.class, value);
	}

}
