package com.bojiu.webapp.user.model;

import java.util.*;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.model.ValueProxyImpl;
import com.bojiu.webapp.user.dto.KySiteConfig;
import lombok.Getter;
import me.codeplayer.util.CollectionUtil;
import org.apache.commons.lang3.EnumUtils;

import static com.bojiu.webapp.user.dto.KySiteConfig.ofT0;

/** 周期类型 */
@Getter
public enum KySiteType implements ValueProxyImpl<KySiteType, Integer> {
	/**
	 * <a href="https://www.pd0gwd.vip:9192/register78975?i_code=76458213">开云【4002】</a><br/>
	 * <a href="https://www.4d5wf3.vip:8003/register47503?i_code=20478471">九游【10001】</a><br/>
	 * <a href="https://www.milan195.com:8000/register73668?i_code=135308676">米兰【9001】</a><br/>
	 * <a href="https://www.za384d.vip:8003/register58986?i_code=2799261">乐鱼【2001】</a><br/>
	 * <a href="https://www.2uk1vq.vip:9193/register64016?i_code=2068790">华体会【3001】</a><br/>
	 * <a href="https://www.wmckvd.vip:9960/register22926?i_code=4423696">爱游戏【1001】</a><br/>
	 */
	T0("一类"),
	/**
	 * <a href="https://www.n5uwrn.vip:9968/register?agent_code=55076830">星空</a><br/>
	 * <a href="https://www.qv3km3.vip:9168/register?agent_code=6522078">OD</a><br/>
	 */
	T1("二类"),
	/**
	 * <a href="https://www.mk2029.com:6003/CHS/register51673?i_code=18321889">MK</a>
	 */
	T2("三类"),
	;

	public static final List<KySiteConfig> SITE_CONFIG = new ArrayList<>() {{
		add(ofT0("开云", "https://www.pd0gwd.vip:9192", "king0312", "123456789Cr", "4002"));
		add(ofT0("九游", "https://www.4d5wf3.vip:8003", "king0312", "123456789Cr", "10001"));
		add(ofT0("米兰", "https://www.milan195.com:8000", "king0312", "123456789Cr", "9001"));
		add(ofT0("乐鱼", "https://www.za384d.vip:8003", "king0312", "123456789Cr", "2001"));
		add(ofT0("华体会", "https://www.2uk1vq.vip:9193", "king0312", "123456789Cr", "3001"));
		add(ofT0("爱游戏", "https://www.wmckvd.vip:9960", "king0312", "123456789Cr", "1001"));

		add(KySiteConfig.of("星空", "https://www.n5uwrn.vip:9968", "king0312", "123456789Cr", T1));
		add(KySiteConfig.of("OD", "https://www.qv3km3.vip:9168", "king0312", "123456789Cr", T1));

		add(KySiteConfig.of("MK", "https://www.mk2029.com:6003", "king0312", "123456789Cr", T2));
	}};

	/** 站点类型-站点配置 */
	public static final Map<KySiteType, List<KySiteConfig>> TYPE_SITE_CONFIG = CollectionUtil.groupBy(SITE_CONFIG, KySiteConfig::getType);
	/** 站点名称-站点配置 */
	public static final Map<String, KySiteConfig> NAME_SITE_CONFIG = CollectionUtil.toHashMap(SITE_CONFIG, KySiteConfig::getName);

	@EnumValue
	public final Integer value;
	public final String label;
	final ValueProxy<KySiteType, Integer> proxy;

	KySiteType(String label) {
		this.value = ordinal();
		this.label = label;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final KySiteType[] CACHE = values();

	public static KySiteType of(String value) {
		return EnumUtils.getEnum(KySiteType.class, value);
	}

}