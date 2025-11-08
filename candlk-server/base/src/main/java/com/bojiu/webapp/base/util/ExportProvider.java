package com.bojiu.webapp.base.util;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.lang3.tuple.Pair;

public interface ExportProvider<C extends Export.Config, T extends Export.ConfigBuilder<C, T>> {

	ConcurrentMap<Pair<String, Locale>, ExcelExportProvider.ExcelConfig> cache = new ConcurrentHashMap<>(32);

	/**
	 * 返回支持的一到多个文件扩展名，例如：<code>[".xls", ".xlsx"]</code>
	 */
	String[] getFileExts();

	/** 当前导出提供者默认的文件扩展名（包含小数点），形如：".csv" */
	default String getFileExt() {
		return getFileExts()[0];
	}

	/**
	 * 获取缓存配置
	 *
	 * @param request 请求
	 * @param key 缓存key
	 * @param locale 指定国际化地域
	 * @param i18nKeyPrefix 用于国际化配置，资源文件属性前缀
	 * @param propertyPairsString 属性键值对字符串，格式为："field1=label1,field2=label2"
	 * @param disableCache 是否禁用缓存
	 * @param callback 回调处理方法
	 */
	C getCached(HttpServletRequest request, @Nullable String key, @Nullable Locale locale, @Nullable String i18nKeyPrefix,
	            @Nullable String propertyPairsString, boolean disableCache, @Nullable Function<T, T> callback);

	/**
	 * 导出数据到指定响应目标
	 *
	 * @param list 数据集合
	 * @param cfg 配置对象
	 * @param dateFormat 日期格式化对象
	 * @param fileName 文件名，例如 "用户列表.xlsx" 或 "用户列表"（兼容没有后缀的情况）
	 * @param responseStyle 响应方式：0=对外部HTTP请求的响应输出；1=FileOutputStream；2=ByteArrayOutputStream并调用consumer
	 * @param consumer 输入流消费者（可选）
	 */
	void export(List<?> list, final C cfg, @Nullable FastDateFormat dateFormat, @Nullable String fileName,
	            int responseStyle, @Nullable Consumer<InputStream> consumer);

}