package com.bojiu.webapp.base.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson2.support.csv.CSVWriter;
import com.bojiu.common.context.I18N;
import com.bojiu.common.util.PropertyBean;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.StringUtil;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;

/**
 * Csv导出
 */
@Slf4j
public class CsvExportProvider implements ExportProvider<Export.Config, CsvExportProvider.CsvConfigBuilder> {

	static final CsvExportProvider INSTANCE = new CsvExportProvider();

	@Override
	public String[] getFileExts() {
		return new String[] { getFileExt() };
	}

	@Override
	public String getFileExt() {
		return ".csv";
	}

	public Export.Config getCached(HttpServletRequest request, @Nullable String key, @Nullable Locale locale, @Nullable String i18nPrefix,
	                               @Nullable String propertyPairsString, boolean disableCache, @Nullable Function<CsvConfigBuilder, CsvConfigBuilder> callback) {
		// 不缓存 CsvConfig，每次都去builder
		if (disableCache) {
			return builderCsvConfig(locale, i18nPrefix, propertyPairsString, callback);
		}

		if (StringUtil.isEmpty(key)) {
			key = request.getRequestURI();
		}
		Export.Config value = cache.get(Pair.of(key, locale));
		if (value == null) {
			value = builderCsvConfig(locale, i18nPrefix, propertyPairsString, callback);
			cache.put(Pair.of(key, locale), value);
		}
		return value;
	}

	/**
	 * @param list 表格内容
	 * @param cfg CSV配置
	 * @param responseStyle 0=对外部HTTP请求的响应输出；1=FileOutputStream；2=ByteArrayOutputStream并调用consumer
	 */
	public void export(List<?> list, final Export.Config cfg, @Nullable FastDateFormat dateFormat, @Nullable String fileName, int responseStyle, @Nullable Consumer<InputStream> consumer) {
		if (dateFormat == null) {
			dateFormat = cfg.dateFormat;
		}
		try (final OutputStream os = prepareOutputStream(responseStyle, fileName, ".csv", utf8BOM());
		     OutputStreamWriter out = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
			out.write("\uFEFF");
			// 使用 FileOutputStream 有巴西语言的内容时会乱码
			final CSVWriter writer = CSVWriter.of(out);
			writer.writeLine((Object[]) cfg.headers);
			final int columnSize = cfg.headers.length;
			int count = 0;
			for (Object rowData : list) { // 行
				if (cfg.indexes != null) {
					final Object[] row = (Object[]) rowData;
					writer.writeLine(row);
				} else if (rowData instanceof List<?> rowAsList) {
					writer.writeLine(rowAsList);
				} else {
					final PropertyBean bean = PropertyBean.getInstance(rowData.getClass());
					for (int j = 0; j < columnSize; j++) {// 列
						if (j != 0) {
							writer.writeComma();
						}
						Object value = bean.getProperty(rowData, cfg.fields[j]);
						if (value instanceof Date dateVal) {
							value = dateFormat.format(dateVal);
						} else if (value instanceof String str && !str.isEmpty() && (str.startsWith("0") || str.contains(","))) {
							// 如果是0开头的字符串，需要转义处理，否则前导0会丢失
							value = "=\"" + str + "\"";
						}
						writer.writeValue(value);
					}
					writer.writeLine();
				}
				if (++count >= 100) { // fastjson2 目前写入恰好 65535 个字节时会报错，因此需要手动定期 flush 一下
					writer.flush();
					count = 0;
				}
			}
			writer.close(); // 没有这一行，数据将为空
			if (consumer != null) {
				try (InputStream is = new ByteArrayInputStream(((ByteArrayOutputStream) os).toByteArray())) {
					consumer.accept(is);
				} catch (Throwable e) {
					log.error("处理CSV输出流时出现异常", e);
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public static CsvConfigBuilder builder() {
		return new CsvConfigBuilder();
	}

	static final ConcurrentMap<Pair<String, Locale>, Export.Config> cache = new ConcurrentHashMap<>(32);

	private static Export.Config builderCsvConfig(Locale locale, String i18nKeyPrefix, String propertyPairsString, Function<CsvConfigBuilder, CsvConfigBuilder> callback) {
		CsvConfigBuilder builder = new CsvConfigBuilder();
		if (propertyPairsString != null) {
			builder.resolvePairString(locale, i18nKeyPrefix, propertyPairsString);
		}
		if (callback != null) {
			builder = callback.apply(builder);
		}
		return builder.build();
	}

	public static class CsvConfigBuilder extends Export.ConfigBuilder<Export.Config, CsvConfigBuilder> {

		private CsvConfigBuilder() {
			super(new Export.Config());
		}

	}

	/**
	 * @param responseStyle 0=对外部HTTP请求的响应输出；1=FileOutputStream；2=ByteArrayOutputStream并调用consumer
	 * @param fileName 如果指定了文件名，则必须包含后缀
	 * @param fileExt 文件扩展名。例如：".csv" 或 ".xls"
	 * @param withBOM 写入 BOM 文件头
	 */
	public static OutputStream prepareOutputStream(int responseStyle, String fileName, final String fileExt, byte[] withBOM) throws IOException {
		OutputStream os = ExportUtil.prepareOutputStream(responseStyle, fileName, fileExt);
		os.write(withBOM);
		return os;
	}

	/**
	 * UTF-8 BOM 文件头字节数组
	 */
	static byte[] utf8BOM() {
		return new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
	}

	public static void export(List<?> list, @Nullable String fileName, int responseStyle) {
		try (final OutputStream os = prepareOutputStream(responseStyle, I18N.msg(Export.FILENAME_I18N_PREFIX + fileName), ".csv", utf8BOM());
		     OutputStreamWriter out = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
			final CSVWriter writer = CSVWriter.of(out);
			for (Object rowData : list) { // 行
				final Object[] row = (Object[]) rowData;
				writer.writeLine(row);
			}
			writer.close(); // 没有这一行，数据将为空
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}