package com.bojiu.context.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.model.ValueProxyImpl;
import com.google.common.base.CaseFormat;
import lombok.Getter;
import org.apache.commons.lang3.EnumUtils;

/**
 * 侧边栏功能类型
 */
@Getter
public enum FeatureType implements ValueProxyImpl<FeatureType, String> {

	AGENT("代理"),
	DEPOSIT_POOL("公积金"),
	INCOME_BOX("利息宝"),
	VIP("VIP"),
	RECHARGE_MAX_RATE("最大充值最大优惠"),
	TASK("任务"),
	DOWNLOAD("APP下载"),
	CS("客服"),
	FAQ("常见问题"),
	LANGUAGES("语言"),
	ABOUT_US("关于我们"),
	FIND_US("找到我们"),
	OFFICIAL_MEDIA("官网媒体"),
	FEEDBACK("有奖反馈"),
	REBATE("返水"),
	;

	@EnumValue
	public final String value;
	public final String label;
	private transient String camelCaseName;
	final ValueProxy<FeatureType, String> proxy;

	public static final FeatureType[] CACHE = values();

	FeatureType(String label) {
		this.value = name();
		this.label = label;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static FeatureType of(String value) {
		return EnumUtils.getEnum(FeatureType.class, value);
	}

	public String getCamelCaseName() {
		if (camelCaseName == null) {
			camelCaseName = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name());
		}
		return camelCaseName;
	}

}