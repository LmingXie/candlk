package com.bojiu.context.model;

import java.util.Arrays;
import java.util.Comparator;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import lombok.Getter;
import me.codeplayer.util.ArrayUtil;

import static com.bojiu.context.i18n.UserModelI18nKey.*;

/**
 * 模板类型 1 = 灵动；2 = 经典风格；3 = WG新欧美；4 = 欧美简约风；6=panda；7=综合版1
 * 厂商图获取实现：see {@link com.bojiu.webapp.game.entity.MerchantVendor#querySloganImage }
 * 厂商热门图获取实现：see {@link com.bojiu.webapp.game.entity.MerchantVendor#queryHotImage }
 * 游戏图获取实现：see {@link com.bojiu.webapp.game.entity.MerchantGame#queryImage }
 */
@SuppressWarnings("JavadocReference")
@Getter
public enum TemplateType implements LabelI18nProxy<TemplateType, Integer> {

	/** 灵动 */
	EUROPE(1, TEMPLATE_TYPE_EUROPE, false, new int[] { 2, 1 }, 1),
	/** 经典风格 */
	CLASSIC(2, TEMPLATE_TYPE_CLASSIC, false, new int[] { 2, 1 }, 2),
	/** WG欧美风(综合版2) */
	WG_EUROPE(3, TEMPLATE_TYPE_WG_EUROPE, new int[] { 3, 5 }, 8),
	/** 定制版 */
	CUSTOMIZED(4, TEMPLATE_TYPE_CUSTOMIZED, false, 4),
	/** 欧美简约风 */
	EUROPE_SIMPLIFIED(5, TEMPLATE_TYPE_EUROPE_SIMPLIFIED, false, 5),
	/** PANDA */
	PD(6, TEMPLATE_TYPE_PANDA, false, new int[] { 2, 1 }, 6),
	/** 综合版1 */
	COMPREHENSIVE_VERSION_1(7, COMPREHENSIVE_VERSION_ONE, new int[] { 3, 5 }, new int[] { 1, 1 }, new int[] { 0, 1 }, 7),
	/** 综合版3 */
	COMPREHENSIVE_VERSION_3(8, COMPREHENSIVE_VERSION_THIRD, new int[] { 3, 5 }, new int[] { 0, 1 }, new int[] { 0, 1 }, 8),
	/** 定制版1 */
	CUSTOMIZED_VERSION_1(9, TEMPLATE_TYPE_CUSTOMIZED_ONE, new int[] { 3, 5 }, new int[] { 1, 1 }, new int[] { 0, 1 }, 9),
	/** 综合版4 */
	COMPREHENSIVE_VERSION_4(10, COMPREHENSIVE_VERSION_FOUR, new int[] { 3, 5 }, new int[] { 1, 1 }, new int[] { 0, 1 }, 10),
	/** 综合版5 */
	COMPREHENSIVE_VERSION_5(11, COMPREHENSIVE_VERSION_FIVE, new int[] { 3, 5 }, new int[] { 0, 1 }, new int[] { 0, 1 }, 11),
	;

	/** ※是否WG模板【增加WG皮肤时需要增加】 */
	public boolean forWG() {
		return ArrayUtil.ins(this, WG_EUROPE, COMPREHENSIVE_VERSION_1, COMPREHENSIVE_VERSION_3, CUSTOMIZED_VERSION_1, COMPREHENSIVE_VERSION_4, COMPREHENSIVE_VERSION_5);
	}

	@EnumValue
	public final Integer value;
	final ValueProxy<TemplateType, Integer> proxy;
	/** 新巴西 定制版不显示；欧美和亚太环境 欧美简约风不显示，默认都显示 */
	public final boolean isDisplay;
	/**
	 * <pre>
	 * 定义游戏图片的长方形和正方形图片的取值下标<br/>
	 *      如配置为: { 3, 5 } 则表示 长方形去下标为3的图片，正方形取下标为5的图片<br/>
	 * 游戏图标下标有：<br/>
	 *      0=长方形(原图)；1=正方形(原图)；2=长方形(初版带遮罩)；3=长方形(欧美)；4=长方形(有文字)；5=正方形(亚太)<br/>
	 * 系统游戏图片下标：see {@link com.bojiu.webapp.game.entity.Game#images }
	 * </pre>
	 */
	public final int[] gameImageIndex;
	/**
	 * <pre>
	 * 配置厂商图正方形和长方对应图片的下标,如果某个皮肤固定取某一个，都定义一致就行<br/>
	 *      如配置为: { 0, 1 } 则表示 长方形去下标为0的图片，正方形取下标为1的图片<br/>
	 * 厂商图标下标有：<br/>
	 *      0=欧美；1=亚太<br/>
	 * 厂商图片下标：see {@link com.bojiu.webapp.game.entity.Vendor#sloganImages }
	 * </pre>
	 */
	public final int[] vendorImageIndex;
	/**
	 * <pre>
	 * 配置厂商热门图正方形和长方对应图片的下标<br/>
	 *      如配置为: { 0, 1 } 则表示 长方形去下标为0的图片，正方形取下标为1的图片<br/>
	 * 厂商热门图标下标有：<br/>
	 *      0=欧美热门；1=亚太热门<br/>
	 * 厂商热门图片下标：see {@link com.bojiu.webapp.game.entity.Vendor#hotImages }
	 * </pre>
	 */
	public final int[] vendorHotImageIndex;
	/**
	 * 排序
	 */
	public final int sort;

	TemplateType(Integer value, String label) {
		this(value, label, true, 0);
	}

	TemplateType(Integer value, String label, boolean isDisplay, int sort) {
		this(value, label, isDisplay, new int[] { 0, 1 }, new int[] { 0, 1 }, new int[] { 0, 1 }, sort);
	}

	TemplateType(Integer value, String label, int[] gameImageIndex, int sort) {
		this(value, label, true, gameImageIndex, sort);
	}

	TemplateType(Integer value, String label, boolean isDisplay, int[] gameImageIndex, int sort) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
		this.isDisplay = isDisplay;
		this.gameImageIndex = gameImageIndex;
		this.vendorImageIndex = new int[] { 0, 1 };
		this.vendorHotImageIndex = new int[] { 0, 1 };
		this.sort = sort;
	}

	TemplateType(Integer value, String label, int[] gameImageIndex, int[] vendorImageIndex, int[] vendorHotImageIndex, int sort) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
		this.isDisplay = true;
		this.gameImageIndex = gameImageIndex;
		this.vendorImageIndex = vendorImageIndex;
		this.vendorHotImageIndex = vendorHotImageIndex;
		this.sort = sort;
	}

	TemplateType(Integer value, String label, boolean isDisplay, int[] gameImageIndex, int[] vendorImageIndex, int[] vendorHotImageIndex, int sort) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
		this.isDisplay = isDisplay;
		this.gameImageIndex = gameImageIndex;
		this.vendorImageIndex = vendorImageIndex;
		this.vendorHotImageIndex = vendorHotImageIndex;
		this.sort = sort;
	}

	public static final TemplateType[] CACHE = values();
	/** 按地区返回模板类型 */
	public static final TemplateType[] BY_REGION_CACHE = ArrayUtil.filter(values(), TemplateType::isDisplay);

	static {
		Arrays.sort(BY_REGION_CACHE, Comparator.comparingInt(o -> o.sort));
	}

	public static TemplateType of(Integer value) {
		return Common.getEnum(CACHE, value, 1);
	}
}