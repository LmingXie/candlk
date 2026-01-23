package com.bojiu.webapp.user.entity;

import java.util.Date;

import com.bojiu.common.model.State;
import com.bojiu.common.model.Status;
import com.bojiu.common.util.BeanUtil;
import com.bojiu.context.model.SerializedConfig;
import com.bojiu.webapp.base.entity.Merchant;
import com.bojiu.webapp.base.entity.TimeBasedEntity;
import com.bojiu.webapp.user.model.MetaType;
import lombok.*;
import me.codeplayer.util.*;
import org.jspecify.annotations.Nullable;

/**
 * 商户站点元数据配置表
 *
 * @author LeeYd
 * @since 2023-09-07
 */
@Setter
@Getter
public class Meta extends TimeBasedEntity implements SerializedConfig {

	/** 商户ID */
	Long merchantId;
	/** 配置类型：1=开关 */
	MetaType type;
	/** 配置键 */
	String name;
	/** 配置值 */
	String value;
	/** 显示名称 */
	String label;
	/** 扩展值 */
	String ext;
	/** 状态：1=有效；0=无效 */
	Integer status;

	/** 已解析到的配置对象 */
	@Nullable
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	public transient volatile Object parsedValue;

	public Meta init(Long merchantId, MetaType type, String name, String value, String label, String ext, Integer status, Date now) {
		this.merchantId = merchantId;
		this.type = type;
		this.name = name;
		this.setValue(value);
		this.label = label;
		this.ext = StringUtil.toString(ext);
		this.status = status;
		this.initTime(now);
		return this;
	}

	public Meta init(Meta input, Date now) {
		this.merchantId = X.expectNotNull(merchantId, input.merchantId);
		this.type = X.expectNotNull(type, input.type);
		this.name = X.expectNotNull(name, input.name);
		this.value = X.expectNotNull(input.value, value);
		this.label = X.expectNotNull(label, input.label);
		this.ext = X.expectNotNull(input.ext, ext, "");
		this.status = X.expectNotNull(input.status, status, Status.YES.value);
		this.initTime(now);
		return this;
	}

	public Meta init(Long merchantId, MetaType type, String name, String value, String label, Date now) {
		return this.init(merchantId, type, name, value, label, "", Status.YES.value, now);
	}

	public Meta init(Long merchantId, MetaType type, String name, String value, Date now) {
		return this.init(merchantId, type, name, value, label, now);
	}

	public Meta init(Long merchantId, MetaType type, String value, Date now) {
		return this.init(merchantId, type, type.name(), value, type.getLabel(), now);
	}

	public void setValue(String value) {
		this.value = value;
		this.parsedValue = null;
	}

	@Override
	public String rawConfig() {
		return value;
	}

	@Override
	public Object parsedConfig() {
		return parsedValue;
	}

	@Override
	public void initParsedConfig(Object parsedConfig) {
		parsedValue = parsedConfig;
	}

	@Override
	public void clearIfFront(boolean canClear) {
		// if (canClear && !ContextImpl.get().fromBackstage(null)) { // TODO 暂时不清空缓存
		// 	value = null;
		// 	addTime = null;
		// 	updateTime = null;
		// 	type = null;
		// 	name = null;
		// 	label = null;
		// 	ext = null;
		// }
	}

	/**
	 * 判断 指定商户的成员是否可看见该元数据
	 */
	public boolean canSeeFor(Long merchantId) {
		final State state = type.getState();
		return switch (state) {
			case PRIVATE -> false;
			case INTERNAL -> Merchant.isPlatform(merchantId);
			case PROTECTED -> this.merchantId.equals(merchantId); // TODO 商户的也有可能会给平台看
			default -> true; // public 对所有商户都可以访问
		};
	}

	public static String serializeValue(Object value) {
		return SerializedConfig.serializeValue(value);
	}

	public static <T> Meta of(Long merchantId, MetaType type, T value, @Nullable Meta old) {
		return of(merchantId, type, type.name(), value, old, type.getLabel());
	}

	public static <T> Meta of(Long merchantId, MetaType type, String name, T value, @Nullable Meta old) {
		return of(merchantId, type, name, value, old, type.getLabel());
	}

	/**
	 * @deprecated 请使用 {@link #of(Long, MetaType, String, Object, Meta)} 替代
	 */
	@Deprecated
	public static <T> Meta of(Long merchantId, MetaType metaType, T value, @Nullable Meta old, String name) {
		return of(merchantId, metaType, value, old, name, metaType.getLabel());
	}

	/**
	 * @deprecated 请使用 {@link #of(Long, MetaType, String, Object, Meta, String)} 替代
	 */
	@Deprecated
	public static <T> Meta of(Long merchantId, MetaType metaType, T value, @Nullable Meta old, String name, String label) {
		return of(merchantId, metaType, name, value, old, label);
	}

	/**
	 * @deprecated 请使用 {@link #of(Long, MetaType, Object, Meta)} 替代
	 */
	public static <T> Meta of(Long merchantId, MetaType metaType, T value, @Nullable Meta old, boolean encodeRaw) {
		return of(merchantId, metaType, metaType.name(), value, old, metaType.getLabel());
	}

	public static <T> Meta of(Long merchantId, MetaType metaType, String name, T value, @Nullable Meta old, String label) {
		Meta meta = new Meta();
		if (old != null) {
			BeanUtil.copy(old, meta);
			meta.setValue(serializeValue(value));
			return meta;
		}
		meta.setMerchantId(merchantId);
		meta.setType(metaType);
		meta.setName(name);
		meta.setLabel(label);
		meta.setValue(serializeValue(value));
		return meta;
	}

	/**
	 * 转换VIP配置
	 */
	public Integer nameAsInt() {
		return NumberUtil.getInteger(name, null);
	}

	public static final String MERCHANT_ID = "merchant_id";
	public static final String TYPE = "type";
	public static final String NAME = "name";
	public static final String VALUE = "value";
	public static final String LABEL = "label";
	public static final String EXT = "ext";
	public static final String STATUS = "status";

}