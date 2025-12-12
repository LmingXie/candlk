package com.bojiu.webapp.user.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.model.ValueProxyImpl;
import lombok.Getter;
import me.codeplayer.util.ArrayUtil;
import org.apache.commons.lang3.EnumUtils;

/** 游戏赔率生厂商 */
@Getter
public enum BetProvider implements ValueProxyImpl<BetProvider, Integer> {
	/**
	 * 皇冠
	 * <ul>
	 * <li><a href="https://mos011.com">mos011.com</a>
	 * <li><a href="https://hga038.com">hga038.com</a>
	 * <li><a href="https://m407.mos077.com">m407.mos077.com</a>
	 * <li><a href="https://205.201.0.120">205.201.0.120</a>
	 * <li>C盘口账密：CntBet1203/123456789Cr</a>
	 * <li>D盘口账密：Bet20251202/123456789Cr</a>
	 * </ul>
	 */
	HG("皇冠"),
	/** D1CE/皇冠D盘口 <a href="https://d1ce.com/">D1CE</a>（账密：1611826811@qq.com/123456789Cr） */
	D1CE("D1CE"),
	/** <a href="https://www.g9i3sr.vip:8443/">开云体育</a>（账密：anxiu55m/rewq4321） */
	KAI_YUN("开云体育"),
	;

	@EnumValue
	public final Integer value;
	public final String label;
	public final boolean open;
	final ValueProxy<BetProvider, Integer> proxy;

	BetProvider(String label, boolean open) {
		this.value = ordinal();
		this.label = label;
		this.open = open;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	BetProvider(String label) {
		this(label, true);
	}

	public static final BetProvider[] CACHE = ArrayUtil.filter(values(), BetProvider::isOpen);

	public static BetProvider of(String value) {
		return EnumUtils.getEnum(BetProvider.class, value);
	}

}