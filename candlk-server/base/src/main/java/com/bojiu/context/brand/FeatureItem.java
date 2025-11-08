package com.bojiu.context.brand;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.alibaba.fastjson2.JSONObject;
import com.bojiu.common.util.Common;
import com.bojiu.context.model.LoginWayConfig;
import com.bojiu.context.web.EnhanceEnumConverterFactory;
import lombok.Getter;
import me.codeplayer.util.Assert;
import me.codeplayer.util.CollectionUtil;
import org.springframework.core.convert.converter.Converter;

public interface FeatureItem<V extends Serializable> {

	V getValue();

	default V getItemValue() {
		return getValue();
	}

	String getLabel();

	default String getItemLabel() {
		return getLabel();
	}

	/** {@link #children()}  返回不为 null 时，才会用到该值 */
	default int childrenNum(boolean minOrMax) {
		return 0;
	}

	@Nullable
	default <E extends Serializable> FeatureItem<E>[] children() {
		return null;
	}

	/**
	 * @return 1=必选；0=可选；-1=禁用该选项
	 */
	default int required() {
		return 0;
	}

	default <E extends Serializable> void postHandle(JSONObject node, @Nullable Object selectedChildren, FeatureContext context) {
		final FeatureItem<E>[] children = children();
		if (children == null) {
			// 如果存在，实际上就只能为 ["1"]，不能存在多个子项，此处采用宽松型兼容
			Assert.isTrue(!(selectedChildren instanceof Collection<?> c) || c.size() == 1);
			return;
		}
		Collection<?> selected = selectedChildren == null ? Collections.emptyList() : (Collection<?>) selectedChildren;
		Object firstSelected = CollectionUtil.getAny(selected);
		Function<Object, Object> converter = firstSelected == null ? Function.identity() : converter(children[0].getItemValue(), firstSelected);
		List<JSONObject> items = new ArrayList<>(children.length);
		for (FeatureItem<E> child : children) {
			Serializable value = child.getItemValue();
			boolean match = selected.contains(converter.apply(value));
			JSONObject item = FeatureConfigHandler.toItem(value, child.getLabel(), FeatureConfigHandler.toSelected(match, child.required()));
			items.add(item);
		}
		int min = childrenNum(true);
		if (min > 0) {
			node.put("min", min);
		}
		int max = childrenNum(false);
		if (min > 0) {
			node.put("max", max);
		}
		node.put("items", items);
	}

	private static Function<Object, Object> converter(@Nonnull Object from, @Nonnull Object to) {
		if (to.getClass() == from.getClass()) {
			return Function.identity();
		}
		if (to instanceof String) {
			return Object::toString;
		} else if (to instanceof Integer) {
			return v -> Integer.valueOf(v.toString());
		} else if (to instanceof Enum<?> t) {
			final Converter<String, ?> converter = EnhanceEnumConverterFactory.getCachedConverter(t.getClass());
			return v -> converter.convert(v.toString());
		}
		throw new ClassCastException("Cannot convert " + from.getClass() + " to " + to.getClass());
	}

	@Getter
	enum VipOption implements FeatureItem<Integer> {
		/** VIP 返利（返水） */
		vip("VIP"),
		/** VIP 返利（返水） */
		rebate("VIP 返利"),
		/** VIP 等级管理 */
		vipLevel("VIP等级调整"),
		;

		final Integer value;
		final String label;

		VipOption(String label) {
			this.value = ordinal();
			this.label = label;
		}

		@Override
		public int required() {
			return this == vip ? 1 : 0;
		}

		public static final VipOption[] CACHE = values();

		public static VipOption of(Integer value) {
			return Common.getEnum(CACHE, value, 0);
		}

		public static Set<VipOption> loadFeatures(FeatureContext context) {
			return context.toSet(Feature.Vip, Integer.class, VipOption::of);
		}

	}

	@Getter
	enum AuthOption implements FeatureItem<String> {
		/** 弹窗方式 */
		popUp("弹窗方式"),
		/** 注册方式 */
		registerWay("注册方式"),
		/** 登录方式 */
		loginWay("登录方式"),
		/** 三方快捷登录 */
		thirdLogin("三方快捷登录"),
		/** 行为验证 */
		captcha("行为验证"),
		;

		final String label;

		AuthOption(String label) {
			this.label = label;
		}

		@Override
		public String getValue() {
			return name();
		}

		public static final AuthOption[] CACHE = values();

		@Override
		public int childrenNum(boolean minOrMax) {
			if (!minOrMax) {
				return FeatureItem.super.childrenNum(false);
			}
			return switch (this) {
				case popUp, registerWay, loginWay -> 1;
				default -> 0;
			};
		}

		@SuppressWarnings("unchecked")
		@Nullable
		@Override
		public FeatureItem<Integer>[] children() {
			return switch (this) {
				case popUp -> PopUpOption.values();
				case registerWay, loginWay -> LoginWayConfig.CACHE;
				default -> null;
			};
		}

		/** 弹窗方式：0=弹窗；1=半屏；2=全屏 */
		@Getter
		public enum PopUpOption implements FeatureItem<Integer> {
			/** 弹窗方式 */
			popUp("弹窗方式"),
			/** 半屏 */
			halfScreen("半屏"),
			/** 全屏 */
			fullScreen("全屏"),
			;

			final Integer value;
			final String label;

			PopUpOption(String label) {
				this.value = ordinal();
				this.label = label;
			}

			public static final PopUpOption[] CACHE = values();

			public static PopUpOption of(Integer value) {
				return Common.getEnum(CACHE, value, 0);
			}
		}

	}

	@Getter
	enum RiskOption implements FeatureItem<String> {
		/** 会员调控 */
		rtpControl("会员调控"),
		/** 刷子监控 */
		botSpy("刷子监控"),
		/** 获利监控 */
		profitSpy("获利监控"),
		;

		final String label;

		RiskOption(String label) {
			this.label = label;
		}

		@Override
		public String getValue() {
			return name();
		}

		public static final RiskOption[] CACHE = values();

	}

}