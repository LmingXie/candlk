package com.bojiu.context.brand;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiPredicate;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.bojiu.common.context.I18N;
import com.bojiu.common.util.*;
import com.bojiu.context.web.Jsons;
import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import me.codeplayer.util.CollectionUtil;
import me.codeplayer.util.StringUtil;

import static com.bojiu.context.brand.FeatureItem.RiskOption;
import static com.bojiu.context.brand.FeatureItem.VipOption;

@Getter
public class FeatureContext {

	final EnumMap<Feature, FeatureConfig> features;

	public String featuresJson;

	public FeatureContext(EnumMap<Feature, FeatureConfig> features) {
		this.features = features;
	}


	public boolean enabled(Feature feature) {
		return features.get(feature) != null;
	}

	/**
	 * 获取 指定功能&特性的 元数据配置，如果未启用该功能，则可能返回 null
	 */
	@Nullable
	public FeatureConfig getConfig(Feature feature) {
		return features.get(feature);
	}

	public static FeatureContext from(String json) {
		final EnumMap<Feature, String> map = Jsons.parseObject(json, new TypeReference<>() {
		});
		EnumMap<Feature, FeatureConfig> features = BeanUtil.replaceValues(map, (k, v) -> new FeatureConfig(v));
		return new FeatureContext(features);
	}

	/**
	 * 获取允许创建的子站点最大数量
	 *
	 * @return null 表示不支持创建子站点，0 表示不限制
	 */
	@Nullable
	public Integer siteNum() {
		FeatureConfig config = getConfig(Feature.Site);
		return config == null ? null : config.toInteger();
	}

	/**
	 * 是否启用【顶部下载条】功能
	 */
	public boolean downloadBar() {
		return enabled(Feature.DownloadBar);
	}

	/**
	 * 是否启用【公积金】功能
	 */
	public boolean deposit() {
		return enabled(Feature.Deposit);
	}

	/**
	 * 是否启用【利息宝】功能
	 */
	public boolean incomeBox() {
		return enabled(Feature.IncomeBox);
	}

	/**
	 * 是否启用【网红博主】功能
	 */
	public boolean blogger() {
		return enabled(Feature.Blogger);
	}

	/**
	 * 是否启用【会员上传头像】功能
	 */
	public boolean userUploadAvatar() {
		return enabled(Feature.UserUploadAvatar);
	}

	/**
	 * 是否启用【分享链接预览】功能
	 */
	public boolean sharePreview() {
		return enabled(Feature.SharePreview);
	}

	/**
	 * 是否启用【主播试玩账号】功能
	 */
	public boolean gameTrialAccount() {
		return enabled(Feature.GameTrialAccount);
	}

	/**
	 * 是否启用【会员提现免首充】功能
	 */
	public boolean cashWithoutRecharge() {
		return enabled(Feature.CashWithoutRecharge);
	}

	/**
	 * 是否启用【更改上级代理】功能
	 */
	public boolean changeParentAgent() {
		return enabled(Feature.ChangeParentAgent);
	}

	/**
	 * 是否启用【用户稽核管理】功能
	 */
	public boolean userAudit() {
		return enabled(Feature.UserAudit);
	}

	/**
	 * 是否启用【刷子监控】功能
	 */
	public boolean botSpy() {
		return getNestedBoolean(Feature.RiskControl, RiskOption.botSpy);
	}

	/**
	 * 是否启用【获利监控】功能
	 */
	public boolean profitSpy() {
		return getNestedBoolean(Feature.RiskControl, RiskOption.profitSpy);
	}

	/**
	 * 是否启用【VIP】功能
	 */
	public boolean vip() {
		return VipOption.loadFeatures(this).contains(VipOption.vip); // 目前是必选项
	}

	/**
	 * 是否启用【VIP 返水/返利】功能
	 */
	public boolean rebate() {
		return VipOption.loadFeatures(this).contains(VipOption.rebate);
	}

	/**
	 * 是否启用【VIP等级管理】功能
	 */
	public boolean vipLevel() {
		return VipOption.loadFeatures(this).contains(VipOption.vipLevel);
	}

	/**
	 * 是否启用【刷子监控】功能
	 */
	private boolean getNestedBoolean(Feature feature, FeatureItem<String> subOption) {
		FeatureConfig config = getConfig(feature);
		if (config != null) {
			Object parsed = config.parsed;
			if (parsed == null) {
				parsed = config.getParsedValue(JSONObject.class, null, null, false);
			}
			if (parsed instanceof EnumMap<?, ?> m) {
				return asBoolean(m.get(subOption));
			} else if (parsed instanceof JSONObject m) {
				return asBoolean(m.get(subOption.getValue()));
			}
		}
		return false;
	}

	static boolean asBoolean(Object val) {
		if (val == null) {
			return false;
		} else if (val instanceof Boolean b) {
			return b;
		}
		if (val instanceof List<?> list) {
			if (list.isEmpty()) {
				return false;
			}
			val = list.get(0);
		}
		if (val instanceof String s) {
			return !s.isEmpty() && !"false".equals(s); // 禁用其实不应该存储，"0" 可能表示选项
		} else if (val instanceof Integer i) {
			return i > 0;
		}
		return false;
	}

	public void assertEnabled(Feature feature) {
		assertEnabled(enabled(feature));
	}

	/**
	 * 获取指定功能的配置，如果未启用该功能，则抛出异常
	 */
	@Nonnull
	public FeatureConfig getRequiredConfig(Feature feature) {
		FeatureConfig config = getConfig(feature);
		assertEnabled(config != null);
		return config;
	}

	public static void assertEnabled(boolean enabled) {
		I18N.assertTrue(enabled, "暂不支持此功能"); // TODO 待国际化
	}

	public static void assertEnabled(@Nullable FeatureConfig config) {
		assertEnabled(config != null);
	}

	@Nonnull
	public static <T, R> ImmutableSet<R> toSet(@Nullable List<T> values, @Nonnull Function<? super T, R> converter) {
		if (values == null || values.isEmpty()) {
			return ImmutableSet.of();
		}
		final ImmutableSet.Builder<R> builder = ImmutableSet.builder();
		for (T value : values) {
			builder.add(converter.apply(value));
		}
		return builder.build();
	}

	@Nonnull
	public <T, R> ImmutableSet<R> toSet(Feature feature, Class<T> valueType, @Nonnull Function<? super T, R> converter) {
		return toSet(getConfig(feature), valueType, converter);
	}

	@Nonnull
	public static <T, R> ImmutableSet<R> toSet(@Nullable FeatureConfig config, Class<T> sourceType, @Nonnull Function<? super T, R> converter) {
		return config == null ? ImmutableSet.of() : toSet(config.getParsedValues(sourceType), converter);
	}

	private transient ConcurrentMap<String, Boolean> featuredMenuCache;

	/**
	 * 判断当前功能配置是否可具有指定的功能菜单权限（多个逗号隔开）
	 */
	public boolean hasMenu(String featureCode) {
		if (StringUtil.isEmpty(featureCode)) {
			return true;
		}
		ConcurrentMap<String, Boolean> cache = featuredMenuCache;
		if (cache == null) {
			featuredMenuCache = cache = new ConcurrentHashMap<>();
		}
		List<String> keys = Common.splitAsStringList(featureCode);
		for (String key : keys) {
			if (!cache.computeIfAbsent(key, this::doHasMenu)) {
				return false;
			}
		}
		return true;
	}

	private boolean doHasMenu(String featureCode) {
		final int pos = featureCode.indexOf('.');
		final String featureName = pos == -1 ? featureCode : featureCode.substring(0, pos);
		Feature feature = Feature.valueOf(featureName);
		FeatureConfig config = features.get(feature);
		if (config == null) {
			return false;
		} else if (pos == -1) {
			return true;
		}
		String subFeature = featureCode.substring(pos + 1);
		final BiPredicate<FeatureConfig, String> custom = feature.customHasMenu;
		if (custom != null) { // 存在自定义的菜单权限判断，就用自定义的
			return custom.test(config, subFeature);
		}
		Object parsed = config.parsed;
		if (parsed == null) {
			parsed = config.getParsedValue(Object.class, null, TransientParsed::new, false);
		}
		if (parsed instanceof TransientParsed t) {
			parsed = t.value;
		}
		if (parsed instanceof EnumMap<?, ?> m) {
			Enum<?> firstKey = m.keySet().iterator().next();
			Enum<?> target = Enum.valueOf(firstKey.getDeclaringClass(), subFeature);
			return m.containsKey(target);
		} else if (parsed instanceof JSONObject m) {
			return m.containsKey(subFeature);
		} else if (parsed instanceof Collection<?> list) { // 有可能类型不匹配
			if (list.contains(subFeature)) {
				return true;
			}
			Object first = CollectionUtil.getAny(list);
			if (first instanceof String s) {
				if (Character.isDigit(s.charAt(0)) == Character.isDigit(subFeature.charAt(0))) {
					return false;
				}
			} else if (first instanceof Enum<?> e) {
				Enum<?> target = Enum.valueOf(e.getDeclaringClass(), subFeature);
				return list.contains(target);
			} else if (first instanceof Integer && Character.isDigit(subFeature.charAt(0))) {
				return list.contains(Integer.valueOf(subFeature));
			}
		}
		throw unsupportedEx(featureCode, config.rawConfig());
	}

	static UnsupportedOperationException unsupportedEx(String featureCode, @Nullable String config) {
		if (config == null) {
			SpringUtil.log.warn("检测到不匹配的菜单Feature：" + featureCode);
		} else {
			SpringUtil.log.warn("检测到不匹配的菜单Feature：" + featureCode + "，配置=" + config);
		}
		throw new UnsupportedOperationException();
	}

	record TransientParsed(Object value) {

	}

	/**
	 * 获取featureContext对象在数据库中保存的数据格式，可以用 from加载成featureContext对象
	 *
	 * @see com.bojiu.context.brand.FeatureContext#from(java.lang.String)
	 */
	public String getFeaturesJson() {
		if (this.featuresJson != null) {
			return this.featuresJson;
		}
		EnumMap<Feature, String> featuresJson = new EnumMap<>(Feature.class);
		for (Feature feature : Feature.values()) {
			FeatureConfig config = features.get(feature);
			if (config != null) {
				featuresJson.put(feature, config.getConfig());
			}
		}
		return this.featuresJson = Jsons.encodeRaw(featuresJson);
	}

	public static Set<Integer> getGameType(FeatureContext context) {
		FeatureConfig config = context.getConfig(Feature.Game);
		LinkedHashMap<String, Map<Long, JSONObject>> currencyVendorMap = config.getParsedValue(new TypeReference<>() {
		}, false);
		Set<Integer> types = new HashSet<>();
		for (Map<Long, JSONObject> m : currencyVendorMap.values()) {
			for (JSONObject dto : m.values()) {
				types.add(dto.getInteger("type"));
			}
		}
		return types;
	}


}