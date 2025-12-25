package com.bojiu.webapp.base.util;

import java.lang.annotation.*;
import java.util.Locale;

import com.bojiu.common.context.I18N;
import com.bojiu.common.util.Common;
import com.bojiu.common.util.Formats;
import me.codeplayer.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.jspecify.annotations.Nullable;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Export {

	/**
	 * 用于动态设置 {@link #value()} 属性 的 attributeName，
	 * 与 {@link #attrValues} 属性互斥，二选一
	 */
	String attrValue = "Export.value";

	/**
	 * 用于动态设置 {@link #values()} 属性 的 attributeName
	 * 与 {@link #attrValue} 属性互斥，二选一
	 * <p>
	 * 当 values 有值时，如果 titleInValues = true，则 values 中必为 { "property1","title1", "property2","title2" } 格式
	 * 如果 titleInValues = true，则 values 为 <code> { "title1","title2" } </code> 格式
	 */
	String attrValues = "Export.values";

	/** 用于动态设置 需要导出的数据集合 的 attributeName */
	String attrData = "Export.data";

	/** 用于动态设置 {@link #labelOnly()} 的 attributeName */
	String attrLabelOnly = "Export.labelOnly";

	/** 用于动态设置 {@link #i18nPrefix()} 的 attributeName */
	String attrI18nPrefix = "Export.i18nPrefix";

	/** 用于动态设置 {@link #filename()} 的 attributeName */
	String attrFilename = "Export.filename";

	/** 文件名称 {@link #filename()} 在国际化配置文件中约定的前缀 */
	String FILENAME_I18N_PREFIX = "filename.";

	/**
	 * 指示不缓存
	 *
	 * @see #key()
	 */
	String NO_CACHE = "*";

	/**
	 * 指定导出每行数据所需的 键值对表达式，例如：
	 * <p> <code>"id=用户ID;username=用户名；status_=用户状态；empName()=操作人"</code>
	 * <p> 如果需要<b>国际化</b>，则仅需指定属性名即可，例如："id,username,status_,empName()"</code>，label 会自动通过拼接 {@link #i18nPrefix()} 去获取
	 * <p> 也可通过 <code> request.setAttribute(Export.attrValue, "id=ID;name=名称") </code> 进行动态设置
	 *
	 * <p> 【注意】如果需要<b>国际化</b>，请预先在国际化配置文件定义好 <code>$i18nPrefix+propertyName</code>，例如 <code>export.label.username=用户名</code>
	 *
	 * @see #attrValue
	 * @see #attrI18nPrefix
	 */
	String value() default "";

	/**
	 * 标题数组
	 * <p> 例如：<code> { "label1","label2" } </code>【需配合 {@link #labelOnly()} = true 使用，此时数据内的元素为 数组 或 List 集合 】
	 * <p> 或 键值对 数组 例如：<code> { "property1","label1", "property2","label2" } </code>
	 * <p> 也可通过 <code> request.setAttribute(Export.attrValues, new String[]{}) </code> 进行动态设置
	 */
	String[] values() default {};

	/**
	 * {@link #values()} 是否仅为字段标题数组，而不是 "propertyName", "label" 成对出现的数组
	 * <p> 一般只有 每一行数据 是 数组 或 List 时，才会用到（因为 属性名 就是 数组下标，无需额外指定）
	 */
	boolean labelOnly() default false;

	/**
	 * 日期格式化表达式
	 */
	String dateFormat() default Formats.formatDate_yMd_Hms;

	/**
	 * 缓存配置 的 key，如果为 "*"( 即 {@link #NO_CACHE} ) 则不缓存配置
	 *
	 * @see #NO_CACHE
	 */
	String key() default "";

	/**
	 * 响应输出的文件名（前缀），例如：<code>"用户列表.xlsx" 或 "用户列表"</code>（兼容没有后缀的情况）
	 * <p> 如果不设置时，将自动从注解 {@link com.bojiu.common.web.Ready#value()} 中获取
	 * <p> 你也可以通过 {@link com.bojiu.context.web.ProxyRequest#setTitle(String)} 进行动态设置
	 * <p> 如果需要 <b>国际化</b>，请预先在国际化配置文件定义好 <code>filename.$filename</code>，例如 <code>export.filename.user_list=用户列表</code>
	 *
	 * @see #FILENAME_I18N_PREFIX
	 */
	String filename() default "";

	/**
	 * 对字段名称进行国际化资源配置的前缀，一般以 "." 结尾
	 * <p>
	 * 如果为 "!"，则表示禁用国际化
	 */
	String i18nPrefix() default "";

	/**
	 * 是否异步导出
	 */
	boolean async() default false;

	class Config {

		/**
		 * 默认日期格式化器("yyyy-MM-dd")
		 */
		public static FastDateFormat DEFAULT_DATE_FORMAT = Formats.getDateFormat("yyyy-MM-dd HH:mm:ss");

		/** 标题行，例如：<code> ["订单号", "用户名", "操作人" ] </code> */
		protected String[] headers;
		/** 每行的属性名，例如：<code> ["orderNo", "user.username", "empName()"]</code> */
		protected String[] fields;
		protected FastDateFormat dateFormat = DEFAULT_DATE_FORMAT;
		/** 如果数据对象是数组，则该int数组不为null，且是与 <code>fields</code> 等价的整数数组 */
		protected int[] indexes;

		public void validate() {
			Assert.isTrue(headers != null && headers.length > 0, "invalid CsvConfig.columnNames");
			final int columnSize = headers.length; // 列数
			Assert.isTrue(fields != null && fields.length == columnSize, "invalid CsvConfig.columnExprs");
			Assert.isTrue(dateFormat != null, "invalid CsvConfig.dateFormat");
			if (indexes == null && NumberUtil.isNumber(fields[0])) { // 如果 属性表达式 为数字，则表示对象为数组
				indexes = new int[columnSize];
				for (int i = 0; i < columnSize; i++) {
					indexes[i] = NumberUtil.getInt(fields[i]);
				}
			}
		}

	}

	class ConfigBuilder<T extends Config, C extends ConfigBuilder<T, C>> {

		protected final T cfg;
		@SuppressWarnings("unchecked")
		protected final C typedThis = (C) this;

		protected ConfigBuilder(T cfg) {
			this.cfg = cfg;
		}

		public C resolvePairString(@Nullable Locale locale, @Nullable String i18nKeyPrefix, String propertyPairsString) {
			final String[] pairs = StringUtils.split(propertyPairsString, ",;=");
			return resolvePairs(false, locale, i18nKeyPrefix, pairs);
		}

		/**
		 * @param locale 不传入 locale 时，则不进行国际化
		 */
		public C resolvePairs(final boolean labelOnly, @Nullable Locale locale, @Nullable String i18nKeyPrefix, String... propertyPairs) {
			final boolean i18n = locale != null, single = i18n || labelOnly;
			Assert.isTrue(propertyPairs.length > 0 && (i18n || labelOnly || (propertyPairs.length & 1) == 0), "invalid length of propertyPairs");
			final int factor = single ? 1 : 2;
			final int columnSize = propertyPairs.length / factor;
			final String[] labels = new String[columnSize], fields = new String[columnSize];
			final int[] indexes = labelOnly ? new int[columnSize] : null;
			for (int i = 0, j = 0; j < columnSize; j++) {
				if (labelOnly) {
					indexes[j] = j;
				}
				final String property = propertyPairs[i++];
				fields[j] = labelOnly ? Common.toString(j) : property;
				String label = single ? property : propertyPairs[i++];
				if (i18n) {
					String code = StringUtil.concat(i18nKeyPrefix, labelOnly ? label : StringUtils.removeEnd(label, "()"));
					label = I18N.msg(code, locale);
				}
				labels[j] = label;
			}
			cfg.headers = labels;
			cfg.fields = fields;
			if (labelOnly) {
				cfg.indexes = indexes;
			}
			return typedThis;
		}

		public C dateFormat(String pattern) {
			return dateFormat(Formats.getDateFormat(pattern));
		}

		public C dateFormat(FastDateFormat dateFormat) {
			cfg.dateFormat = dateFormat;
			return typedThis;
		}

		public T build() {
			cfg.validate();
			return cfg;
		}

	}

}