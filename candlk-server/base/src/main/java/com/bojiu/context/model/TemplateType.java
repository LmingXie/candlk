package com.bojiu.context.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import lombok.Getter;
import me.codeplayer.util.ArrayUtil;

import static com.bojiu.context.i18n.UserModelI18nKey.*;

/**
 * 模板类型 1 = 灵动；2 = 经典风格；3 = WG新欧美；4 = 欧美简约风；6=panda
 */
@Getter
public enum TemplateType implements LabelI18nProxy<TemplateType, Integer> {

	/** 灵动 */
	EUROPE(1, TEMPLATE_TYPE_EUROPE, new int[] { 2, 1 }),
	/** 经典风格 */
	CLASSIC(2, TEMPLATE_TYPE_CLASSIC, new int[] { 2, 1 }),
	/** WG欧美风 */
	WG_EUROPE(3, TEMPLATE_TYPE_WG_EUROPE, new int[] { 3, 5 }),
	/** 定制版 */
	CUSTOMIZED(4, TEMPLATE_TYPE_CUSTOMIZED, false),
	/** 欧美简约风 */
	EUROPE_SIMPLIFIED(5, TEMPLATE_TYPE_EUROPE_SIMPLIFIED, false),
	/** PANDA */
	PD(6, TEMPLATE_TYPE_PANDA, new int[] { 2, 1 }),
	;
	@EnumValue
	public final Integer value;
	final ValueProxy<TemplateType, Integer> proxy;
	/** 新巴西 定制版不显示；欧美和亚太环境 欧美简约风不显示，默认都显示 */
	public final boolean isDisplay;
	/**
	 * 系统游戏图片对应下标 0:长方形的下标；1=正方形的下标
	 * 系统游戏图片下标：{@link com.bojiu.webapp.game.entity.Game#images }
	 */
	public final int[] gameImageIndex;

	TemplateType(Integer value, String label) {
		this(value, label, true);
	}

	TemplateType(Integer value, String label, boolean isDisplay) {
		this(value, label, isDisplay, new int[] { 0, 1 });
	}

	TemplateType(Integer value, String label, int[] gameImageIndex) {
		this(value, label, true, gameImageIndex);
	}

	TemplateType(Integer value, String label, boolean isDisplay, int[] gameImageIndex) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
		this.isDisplay = isDisplay;
		this.gameImageIndex = gameImageIndex;
	}

	/** 是否WG模板 */
	public boolean forWG() {
		return this == WG_EUROPE;
	}

	public static final TemplateType[] CACHE = values();
	/** 按地区返回模板类型 */
	public static final TemplateType[] BY_REGION_CACHE = ArrayUtil.filter(values(), TemplateType::isDisplay);

	public static TemplateType of(Integer value) {
		return Common.getEnum(CACHE, value, 1);
	}
}