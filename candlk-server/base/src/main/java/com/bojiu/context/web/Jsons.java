package com.bojiu.context.web;

import java.util.Collections;
import java.util.List;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.filter.*;
import com.bojiu.common.model.ValueEnum;
import me.codeplayer.util.X;
import org.jspecify.annotations.Nullable;

/**
 * JSON字符串序列化转换工具类
 *
 * @since 2014-10-13
 */
public abstract class Jsons {

	static String dateFormat = "millis";   // 日期时间类型默认输出为 毫秒级时间戳
	public static JSONWriter.Context encodeContext, encodeRawContext, serializeContext;
	static JSONReader.Context decodeContext, deserializeContext;

	static {
		init();
	}

	public static void init() {
		JSON.config(JSONWriter.Feature.BrowserCompatible, JSONWriter.Feature.WriteBigDecimalAsPlain);
		encodeContext = defaultEncodeContext((object, name, value) -> value instanceof ValueEnum<?, ?> t ? t.getValue() : value);
		encodeRawContext = defaultEncodeContext(null);
		encodeRawContext.config(JSONWriter.Feature.BrowserCompatible, false);
		serializeContext = defaultEncodeContext(null, JSONWriter.Feature.WriteClassName);
		decodeContext = defaultDecodeContext();
		deserializeContext = defaultDecodeContext(JSONReader.Feature.SupportAutoType);
	}

	public static void setDateFormat(String dateFormat) {
		Jsons.dateFormat = dateFormat;
		init();
	}

	static JSONWriter.Context defaultEncodeContext(@Nullable ValueFilter valueFilter, JSONWriter.Feature... features) {
		final JSONWriter.Context context = new JSONWriter.Context(features);
		context.setDateFormat(dateFormat);
		if (valueFilter != null) {
			context.setValueFilter(valueFilter);
		}
		return context;
	}

	static JSONReader.Context defaultDecodeContext(JSONReader.Feature... features) {
		return new JSONReader.Context(features);
	}

	/**
	 * 将Java对象编码为JSON字符串。
	 * <p>
	 * 如果对象里存在为null的属性，则不包含在字符串中。
	 * <p>
	 * 【注意】 对于实现了 {@link  ValueEnum } 接口的枚举，将输出其 value 值
	 *
	 * @param obj 指定的任意对象
	 */
	public static String encode(Object obj) {
		return JSON.toJSONString(obj, encodeContext);
	}

	/**
	 * 将Java对象编码为JSON字符串。
	 * <p>
	 * 如果对象里存在为null的属性，则不包含在字符串中。
	 * <p>
	 * 【注意】 所有枚举，将输出其 {@link Enum#ordinal()} 下标值
	 *
	 * @param obj 指定的任意对象
	 */
	public static String encodeRaw(Object obj) {
		return JSON.toJSONString(obj, encodeRawContext);
	}

	/**
	 * 将Java对象编码为JSON字符串。<br/>
	 * 值为null的属性也会保留并输出。
	 *
	 * @param obj 指定的任意对象
	 */
	public static String encodeKeepNull(Object obj) {
		return JSON.toJSONString(obj, dateFormat, JSONWriter.Feature.WriteMapNullValue);
	}

	/**
	 * 将Java对象编码为JSON字符串
	 *
	 * @param obj 指定的任意对象
	 * @param excludeProperties 需要排除的属性数组
	 */
	public static String encodeWithExclude(Object obj, String... excludeProperties) {
		SimplePropertyPreFilter filter = new SimplePropertyPreFilter();
		Collections.addAll(filter.getExcludes(), excludeProperties);
		return JSON.toJSONString(obj, dateFormat, new Filter[] { filter });
	}

	public static String serialize(Object obj, String dateFormat) {
		return JSON.toJSONString(obj, dateFormat, JSONWriter.Feature.WriteClassName);
	}

	/**
	 * 将Java对象编码为JSON字符串
	 *
	 * @param obj 指定的任意对象
	 * @param includeProperties 需要排除的属性数组
	 */
	public static String encodeWithInclude(Object obj, String... includeProperties) {
		return JSON.toJSONString(obj, dateFormat, new Filter[] { new SimplePropertyPreFilter(includeProperties) });
	}

	/**
	 * 将Java对象编码为JSON字符串，并以指定的格式化模式处理日期类型
	 *
	 * @param obj 指定的任意对象
	 * @param dateFormat 指定的格式化字符串，例如{@code "yyyy-MM-dd"}
	 */
	public static String encodeWithDateFormat(Object obj, String dateFormat) {
		return JSON.toJSONString(obj, dateFormat);
	}

	/**
	 * 将JSON字符串转为对应的 JSONObject 或 JSONArray 对象
	 *
	 * @param text 指定的JSON字符串
	 */
	public static Object parse(String text) {
		return JSON.parse(text, decodeContext);
	}

	/**
	 * 将JSON字符串转为指定类型的Java对象
	 *
	 * @param text 指定的JSON字符串
	 * @param clazz 指定的类型
	 */
	public static <T> T parseObject(String text, Class<T> clazz) {
		return JSON.parseObject(text, clazz, decodeContext);
	}

	/**
	 * 将JSON字符串转为指定类型的Java对象
	 *
	 * @param text 指定的JSON字符串
	 * @param typeReference 指定的类型引用
	 */
	public static <T> T parseObject(String text, TypeReference<T> typeReference, JSONReader.Feature... features) {
		return JSON.parseObject(text, typeReference, features);
	}

	/**
	 * 将JSON字符串转为JSONObject形式的对象(类似于增强型的 LinkedHashMap)
	 *
	 * @param text 指定的JSON字符串
	 */
	public static JSONObject parseObject(String text) {
		return JSON.parseObject(text, decodeContext);
	}

	/**
	 * 将JSON字符串转为JSONObject形式的对象(类似于增强型的 LinkedHashMap)
	 *
	 * @param text 指定的JSON字符串
	 */
	public static JSONObject parseObjectOrdered(String text) {
		return JSON.parseObject(text, JSONObject.class, JSONReader.Feature.FieldBased);
	}

	/**
	 * 将JSON字符串转为JSONArray形式的对象(类似于增强型的ArrayList)
	 *
	 * @param text 指定的JSON字符串
	 */
	public static JSONArray parseArray(String text) {
		return JSON.parseArray(text);
	}

	/**
	 * 将JSON字符串转为List形式的指定类型的对象集合
	 *
	 * @param text 指定的JSON字符串
	 * @param clazz 指定的类型
	 */
	public static <T> List<T> parseArray(String text, Class<T> clazz) {
		return JSON.parseArray(text, clazz);
	}

	/**
	 * 将指定的Java对象序列化为JSON字符串
	 *
	 * @param obj 指定的对象
	 * @since 0.1
	 */
	public static String serialize(Object obj) {
		return JSON.toJSONString(obj, serializeContext);
	}

	/**
	 * 将指定的JSON字符串反序列化为指定的Java对象
	 *
	 * @param text 指定的JSON字符串
	 * @since 0.1
	 */
	public static <T> T deserialize(String text) {
		return X.castType(JSON.parse(text, deserializeContext));
	}

}