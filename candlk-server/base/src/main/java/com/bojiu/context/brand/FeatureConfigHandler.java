package com.bojiu.context.brand;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import com.alibaba.fastjson2.*;
import com.bojiu.common.model.ValueEnum;
import com.bojiu.common.util.Common;
import com.bojiu.context.model.Option;
import com.bojiu.context.model.SerializedConfig;
import com.bojiu.context.web.Jsons;
import com.google.common.collect.ImmutableSet;
import me.codeplayer.util.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * 每个功能特性配置数据 FeatureConfig 对应的数据描述元信息
 */
public interface FeatureConfigHandler<R> {

	/**
	 * 处理对应功能特性的输入参数，并返回直接存储到数据库的序列化字符串。如果返回 null，表示禁用此功能特性，无需存取
	 *
	 * @param input 一般是一个 JSONObject 或 JSONArray
	 * @param superContext 限制当前可选择的功能选项的全集，目前用不到该参数，为 null 表示无需限制
	 */
	@Nullable
	String handleInput(Feature feature, @NonNull Object input, FeatureContext context, @Nullable FeatureContext superContext);

	/**
	 * 处理从数据库读取的配置数据，返回用于输出配置页面所需的展示数据
	 *
	 * @param superContext 限制当前可选择的功能选项的全集，目前用不到该参数，为 null 表示无需限制
	 */
	Object handleOutput(Feature feature, @Nullable FeatureConfig config, @Nullable FeatureContext context, @Nullable FeatureContext superContext);

	/**
	 * 处理从数据库读取的配置数据，返回解析后的用于对应功能业务代码所需的配置对象
	 *
	 * @param superContext 限制当前可选择的功能选项的全集，目前用不到该参数，为 null 表示无需限制
	 */
	R getParsed(Feature feature, FeatureConfig config, FeatureContext context, @Nullable FeatureContext superContext);

	@Nullable
	static Integer toSelected(boolean enabled, boolean required) {
		if (required) {
			return NumberUtils.INTEGER_TWO;
		}
		if (enabled) {
			return NumberUtils.INTEGER_ONE;
		}
		return null;
	}

	@Nullable
	static Integer toSelected(boolean enabled, int required) {
		return toSelected(enabled, required > 0);
	}

	static JSONObject toCheckboxGroup(Feature feature, @Nullable List<JSONObject> items) {
		JSONObject map = JSONObject.of(
				"label", feature.label,
				"min", feature.min,
				"max", feature.max
		);
		if (items != null) {
			map.put("items", items);
		}
		return map;
	}

	static JSONObject toItem(String value, String label, Integer selected) {
		return JSONObject.of(
				"value", value,
				"label", label,
				"selected", selected
		);
	}

	static JSONObject toItem(Integer value, String label, Integer selected) {
		return toItem(Common.toString(value), label, selected);
	}

	static JSONObject toItem(Object value, String label, Integer selected) {
		return toItem(value.toString(), label, selected);
	}

	/**
	 * <p>输入：<code> ["1"] </code>
	 * <p>存储：<code> true </code>
	 * <p>输出：<code> { "items": [ "value":"1", "label":"$label" ] } </code>
	 */
	static FeatureConfigHandler<Boolean> forBoolean() {
		return BooleanHandler.INSTANCE;
	}

	/**
	 * <p>输入：<code> ["12"] </code>
	 * <p>存储：<code> 12 </code>
	 * <p>输出：<code> ["12"] 或 [] （未启用时） </code>
	 */
	static FeatureConfigHandler<Integer> forInteger() {
		return IntegerHandler.INSTANCE;
	}

	/**
	 * <p>输入：<code> < 前端自定义 > </code>
	 * <p>存储：<code> < 前端自定义 > </code>
	 * <p>输出：<code> < 前端自定义 > </code>
	 */
	static FeatureConfigHandler<Object> forFront() {
		return NoopHandler.INSTANCE;
	}

	static <E> LinkedHashMap<String, E> toItemMap(E[] allItems, Function<? super E, ? extends Serializable> valueGetter) {
		final LinkedHashMap<String, E> itemMap = new LinkedHashMap<>(allItems.length, 1F);
		for (E item : allItems) {
			itemMap.put(Option.asString(valueGetter.apply(item)), item);
		}
		return itemMap;
	}

	static <T> FeatureConfigHandler<Set<String>> forMultiValues(LinkedHashMap<String, T> itemMap, ConfigItemDescriptor<T> descriptor) {
		return new MultiValuesHandler<>(itemMap, descriptor);
	}

	static <E extends Enum<E> & ValueEnum<E, ? extends Serializable>> FeatureConfigHandler<Set<String>> forMultiValues(ConfigItemDescriptor<E> descriptor, E... allItems) {
		return new MultiValuesHandler<>(toItemMap(allItems, ValueEnum::getValue), descriptor);
	}

	static <E extends Enum<E> & ValueEnum<E, ? extends Serializable>> FeatureConfigHandler<Set<String>> forMultiValues(E... allItems) {
		return forMultiValues(ConfigItemDescriptor.forEnum(), allItems);
	}

	static <E extends FeatureItem> FeatureConfigHandler<Set<String>> forMultiValues(E... allItems) {
		return new MultiValuesHandler<>(toItemMap(allItems, FeatureItem::getItemValue), ConfigItemDescriptor.forConfigItem());
	}

	static <E, C> FeatureConfigHandler<Map<String, C>> forNestedMultiValues(LinkedHashMap<String, E> itemMap, ConfigItemDescriptor<E> descriptor) {
		return new NestedMultiValuesHandler<>(itemMap, descriptor);
	}

	static <E extends Enum<E> & ValueEnum<E, ? extends Serializable>, C> FeatureConfigHandler<Map<String, C>> forNestedMultiValues(ConfigItemDescriptor<E> descriptor, E... allItems) {
		return new NestedMultiValuesHandler<>(toItemMap(allItems, ValueEnum::getValue), descriptor);
	}

	static <E extends Enum<E> & ValueEnum<E, ? extends Serializable>, C> FeatureConfigHandler<Map<String, C>> forNestedMultiValues(E... allItems) {
		return forNestedMultiValues(ConfigItemDescriptor.forEnum(), allItems);
	}

	static <E extends FeatureItem, C> FeatureConfigHandler<Map<String, C>> forNestedMultiValues(E... allItems) {
		return forNestedMultiValues(ConfigItemDescriptor.forConfigItem(), allItems);
	}

	static <E extends FeatureItem, C> FeatureConfigHandler<Map<String, C>> forNestedMultiValues(ConfigItemDescriptor<E> descriptor, E... allItems) {
		return new NestedMultiValuesHandler<>(toItemMap(allItems, FeatureItem::getItemValue), descriptor);
	}

	/**
	 * 【单个开关型】配置处理器，只保存开关配置
	 * 数据库中保存为："true"
	 * 选中值输出为："1"，即 <pre><code>
	 *  { "items": [ "value":"1", "label":"$label", "selected": 1 ] }
	 * </code></pre>
	 */
	class BooleanHandler implements FeatureConfigHandler<Boolean> {

		public static final BooleanHandler INSTANCE = new BooleanHandler();

		@Nullable
		@Override
		public String handleInput(Feature feature, @NonNull Object input, FeatureContext context, @Nullable FeatureContext superContext) {
			String value;
			if (input instanceof JSONArray selected) {  // [ "1" ]
				Assert.isTrue(selected.size() == 1);
				value = selected.getString(0);
			} else if (input instanceof String || input instanceof Boolean || input instanceof Integer) {
				value = input.toString();
			} else {
				throw new IllegalArgumentException();
			}
			Assert.isTrue(SerializedConfig.parseBoolean(value));
			return Boolean.TRUE.toString();
		}

		@Override
		public JSONObject handleOutput(Feature feature, @Nullable FeatureConfig config, FeatureContext context, @Nullable FeatureContext superContext) {
			Integer selected = toSelected(config != null && config.toBoolean(), feature.min == 1);
			JSONObject item = toItem("1", feature.label, selected);
			return toCheckboxGroup(feature, Collections.singletonList(item));
		}

		@Override
		public Boolean getParsed(Feature feature, FeatureConfig config, FeatureContext context, @Nullable FeatureContext superContext) {
			return config.toBoolean();
		}

	}

	/**
	 * 【单个开关型】配置处理器，只保存开关配置
	 * 数据库中保存为："N"
	 * 选中值输出为："N"，即 <pre><code>
	 *  "property":"N"
	 * </code></pre>
	 */
	class IntegerHandler implements FeatureConfigHandler<Integer> {

		public static final IntegerHandler INSTANCE = new IntegerHandler();

		@Nullable
		@Override
		public String handleInput(Feature feature, @NonNull Object input, FeatureContext context, @Nullable FeatureContext superContext) {
			int value;
			if (input instanceof JSONArray selected) {  // [ "1" ]
				Assert.isTrue(selected.size() == 1);
				value = selected.getInteger(0);
			} else if (input instanceof Integer selected) {
				value = selected;
			} else if (input instanceof String selected) {
				value = Integer.parseInt(selected);
			} else {
				throw new IllegalArgumentException();
			}
			Assert.isTrue(value >= 0);
			return Common.toString(value);
		}

		@Override
		public List<Integer> handleOutput(Feature feature, @Nullable FeatureConfig config, FeatureContext context, @Nullable FeatureContext superContext) {
			return config == null ? Collections.emptyList() : Collections.singletonList(config.toInteger());
			/*
			JSONObject group = toCheckboxGroup(feature, null);
			if (config != null) {
				group.put("value", config.toInteger());
			}
			return group;
			*/
		}

		@Override
		public Integer getParsed(Feature feature, FeatureConfig config, FeatureContext context, @Nullable FeatureContext superContext) {
			return config.toInteger();
		}

	}

	/**
	 * 【委托给前端/什么也不做】配置处理器
	 */
	class NoopHandler implements FeatureConfigHandler<Object> {

		public static final NoopHandler INSTANCE = new NoopHandler();

		@Nullable
		@Override
		public String handleInput(Feature feature, @NonNull Object input, FeatureContext context, @Nullable FeatureContext superContext) {
			Assert.isTrue(feature.delegateToFront()); // 皮肤 等 已经和前端约定好，由前端自行处理，后端直接保存或输出
			return input.toString();
		}

		@Override
		public Object handleOutput(Feature feature, @Nullable FeatureConfig config, FeatureContext context, @Nullable FeatureContext superContext) {
			return config == null ? "" : getParsed(feature, config, context, superContext);
		}

		@Override
		public Object getParsed(Feature feature, FeatureConfig config, FeatureContext context, @Nullable FeatureContext superContext) {
			String str = config.rawConfig();
			if (str.startsWith("{") || str.startsWith("[")) {
				return JSON.parse(str);
			}
			return str;
		}

	}

	interface ConfigItemDescriptor<E> {

		Function<? super E, String> labelGetter();

		@Nullable
		default ToIntFunction<? super E> requiredMatcher() {
			return null;
		}

		default void postHandle(JSONObject item, E current, @Nullable Object selectedChildren, FeatureContext context) {
		}

		default void postHandle(JSONObject item, E current, FeatureContext context) {
			postHandle(item, current, null, context);
		}

		static <E extends Enum<E> & ValueEnum<E, ? extends Serializable>> ConfigItemDescriptor<E> forEnum() {
			return () -> ValueEnum::getLabel;
		}

		ConfigItemDescriptor<FeatureItem> CONFIG_ITEM_DESCRIPTOR = new ConfigItemDescriptor<>() {
			@Override
			public Function<? super FeatureItem, String> labelGetter() {
				return FeatureItem::getItemLabel;
			}

			@Nullable
			@Override
			public ToIntFunction<? super FeatureItem> requiredMatcher() {
				return FeatureItem::required;
			}

			@Override
			public void postHandle(JSONObject item, FeatureItem current, @Nullable Object selectedChildren, FeatureContext context) {
				current.postHandle(item, selectedChildren, context);
			}
		};

		static <E extends FeatureItem> ConfigItemDescriptor<E> forConfigItem() {
			return X.castType(CONFIG_ITEM_DESCRIPTOR);
		}

	}

	/**
	 * 【多个开关型】配置处理器，只保存开关配置
	 * 数据库中保存为：["1", "2, "3"]
	 * 选中值输出为："$value"，即 <pre><code>
	 * { "items": [ "value":"$value", "label":"$label", "selected": 1 ] ... }
	 * </code></pre>
	 */
	class MultiValuesHandler<E> implements FeatureConfigHandler<Set<String>> {

		/** < value, object > */
		final LinkedHashMap<String, E> allItemMap;
		final ConfigItemDescriptor<E> descriptor;

		public MultiValuesHandler(LinkedHashMap<String, E> itemMap, ConfigItemDescriptor<E> descriptor) {
			this.allItemMap = itemMap;
			this.descriptor = descriptor;
		}

		@Nullable
		@Override
		public String handleInput(Feature feature, @NonNull Object input, FeatureContext context, @Nullable FeatureContext superContext) {
			JSONArray selected = (JSONArray) input; // [ "1", "2", "3" ]
			final int size = selected.size();
			final TreeSet<Object> valueSet = new TreeSet<>();
			int asInt = 0;
			for (int i = 0; i < size; i++) {
				String val = selected.getString(i);
				Assert.isTrue(allItemMap.containsKey(val));
				if (asInt == 0) {
					asInt = NumberUtil.isNumber(val) ? 1 : -1;
				}
				// 存储时，如果是数字，可以优先存为整数，避免存储时双引号的额外转义处理导致浪费空间
				Object value = asInt > 0 ? tryParseInt(val) : val;
				Assert.isTrue(valueSet.add(value));
			}
			return Jsons.encodeRaw(valueSet);
		}

		static Object tryParseInt(String source) {
			try {
				return Integer.valueOf(source);
			} catch (NumberFormatException ignored) {
				return source;
			}
		}

		@Override
		public JSONObject handleOutput(Feature feature, @Nullable FeatureConfig config, FeatureContext context, @Nullable FeatureContext superContext) {
			final Set<String> selected = config == null ? Collections.emptySet() : getParsed(feature, config, context, superContext);
			final List<JSONObject> items = new ArrayList<>(allItemMap.size());
			final ToIntFunction<? super E> matcher = descriptor.requiredMatcher();
			for (Map.Entry<String, E> entry : allItemMap.entrySet()) {
				final int required = matcher == null ? 0 : matcher.applyAsInt(entry.getValue());
				if (required < 0) {
					continue;
				}
				String value = entry.getKey();
				final E t = entry.getValue();
				JSONObject item = toItem(value, descriptor.labelGetter().apply(t), toSelected(selected.contains(value), required));
				descriptor.postHandle(item, t, context);
				items.add(item);
			}
			return toCheckboxGroup(feature, items);
		}

		@Override
		public Set<String> getParsed(Feature feature, FeatureConfig config, FeatureContext context, @Nullable FeatureContext superContext) {
			return config.getParsedValues(String.class, ImmutableSet.class, ImmutableSet::copyOf, true);
		}

	}

	/**
	 * 【多个嵌套配置型】配置处理器，每个开关还附带多个子选项
	 * 数据库中保存为：{ “styleA”:[ "skin1", "skin2" ], “styleB”:[ "skin5", "skin6" ] }
	 * 选中值输出形如 <pre><code>
	 * { "items": [ {
	 *      "value":"styleA",
	 *      "label":"$label",
	 *      "items":[
	 *          { "value": "1", "label": "skin1", "selected": 1 },
	 *          { "value": "2", "label": "skin2" }
	 *      ]
	 *  } // ...
	 * ] }
	 * </code></pre>
	 */
	class NestedMultiValuesHandler<E, C> implements FeatureConfigHandler<Map<String, C>> {

		/** < value, object > */
		final LinkedHashMap<String, E> allItemMap;
		final ConfigItemDescriptor<E> descriptor;

		public NestedMultiValuesHandler(LinkedHashMap<String, E> allItemMap, ConfigItemDescriptor<E> descriptor) {
			this.allItemMap = allItemMap;
			this.descriptor = descriptor;
		}

		@Nullable
		@Override
		public String handleInput(Feature feature, @NonNull Object input, FeatureContext context, @Nullable FeatureContext superContext) {
			/*
			{
		        "styleA": [
		            "skinA",
		            "skinB"
		        ],
		        "styleB": [
		            "skinC",
		            "skinD"
		        ]
		    }
		    或者
		    {
		        "BRL":[
		            {"vendor":"PG", "type": 1, "rate": 2.36},
		            {"vendor":"PG", "type": 2, "rate": 2.35},
		        ],
		        "PHP":[
		            {"vendor":"PG", "type": 1, "rate": 5},
		            {"vendor":"PG", "type": 2, "rate": 6}
		        ]
		    }
			*/
			final JSONObject config = (JSONObject) input;
			final JSONObject target = new JSONObject(config.size(), 1F);
			for (Map.Entry<String, Object> entry : config.entrySet()) {
				Object val = entry.getValue();
				if (val instanceof JSONArray t && !t.isEmpty() && NumberUtil.isNumeric(t.getString(0))) {
					try {
						val = t.toList(Integer.class);
					} catch (Exception ignored) {
					}
				}
				target.put(entry.getKey(), val);
			}
			// TODO 后续再加上校验
			return target.toJSONString();
		}

		@Override
		public Object handleOutput(Feature feature, @Nullable FeatureConfig config, FeatureContext context, @Nullable FeatureContext superContext) {
			/*
			"currency": {
		        "label": "货币",
		        "min": 1,
		        "max": 0,
		        "items": [
		            {
		                "value": "BRL",
		                "label": "巴西雷亚尔",
		                "selected": 1,
		                "items":[
		                    "value": "PG",
		                    "label": "PG",
		                    "selected": 1,
		                    "items":[
		                        "value": "1",
		                        "label": "电子",
		                        "selected": 1,
		                        "vendorRate":"2.36"
		                        "rate":"2.36"
		                    ]
		                ]
		            }
		        ]
		    }
			*/
			final List<JSONObject> items = new ArrayList<>(allItemMap.size());
			final Map<String, C> selected = config == null ? Collections.emptyMap() : getParsed(feature, config, context, superContext);
			final ToIntFunction<? super E> matcher = descriptor.requiredMatcher();
			for (Map.Entry<String, E> entry : allItemMap.entrySet()) {
				String value = entry.getKey();
				final E t = entry.getValue();
				final int required = matcher == null ? 0 : matcher.applyAsInt(t);
				if (required < 0) {
					continue;
				}
				Object selectedChildren = selected.get(value);
				JSONObject item = toItem(value, descriptor.labelGetter().apply(t), toSelected(selectedChildren != null, required));
				descriptor.postHandle(item, t, selectedChildren, context);
				items.add(item);
			}
			return toCheckboxGroup(feature, items);
		}

		@Override
		public Map<String, C> getParsed(Feature feature, FeatureConfig config, FeatureContext context, @Nullable FeatureContext superContext) {
			return config.getParsedValue(new TypeReference<>() {
			}, true);
		}

	}

}