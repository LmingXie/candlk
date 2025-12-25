package com.bojiu.webapp.base.util;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;

import com.bojiu.common.util.PropertyBean;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.Assert;
import me.codeplayer.util.StringUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.jspecify.annotations.Nullable;

/**
 * Excel导出
 */
@Slf4j
public class ExcelExportProvider implements ExportProvider<ExcelExportProvider.ExcelConfig, ExcelExportProvider.ExcelConfigBuilder> {

	static final ExcelExportProvider INSTANCE = new ExcelExportProvider();

	@Override
	public String[] getFileExts() {
		return new String[] { ".xlsx", ".xls" };
	}

	@Override
	public String getFileExt() {
		return ".xlsx";
	}

	public ExcelConfig getCached(HttpServletRequest request, @Nullable String key, @Nullable Locale locale, @Nullable String i18nPrefix,
	                             @Nullable String propertyPairsString, boolean disableCache, @Nullable Function<ExcelConfigBuilder, ExcelConfigBuilder> callback) {
		// 不缓存 ExcelConfig，每次都去builder
		if (disableCache) {
			return builderExcelConfig(locale, i18nPrefix, propertyPairsString, callback);
		}

		if (StringUtil.isEmpty(key)) {
			key = request.getRequestURI();
		}
		ExcelConfig value = cache.get(Pair.of(key, locale));
		if (value == null) {
			value = builderExcelConfig(locale, i18nPrefix, propertyPairsString, callback);
			cache.put(Pair.of(key, locale), value);
		}
		return value;
	}

	/**
	 * @param list 表格内容
	 * @param cfg Excel配置
	 * @param responseStyle 0=对外部HTTP请求的响应输出；1=FileOutputStream；2=ByteArrayOutputStream并调用consumer
	 */
	public void export(List<?> list, final ExcelConfig cfg, @Nullable FastDateFormat dateFormat, @Nullable String fileName, int responseStyle, @Nullable Consumer<InputStream> consumer) {
		if (dateFormat == null) {
			dateFormat = cfg.dateFormat;
		}
		final Workbook wb = new HSSFWorkbook();
		OutputStream os = null;
		final String sheetName = "sheet";
		Sheet sheet = wb.createSheet(sheetName);
		sheet.setColumnWidth(1, calcColumnWidthFor(sheetName, cfg.maxColumnWidth));
		// 冻结首行
		// sheet.createFreezePane(columnSize, 1);
		// 创建表头
		Row row0 = sheet.createRow(0);
		HSSFCellStyle style = (HSSFCellStyle) wb.createCellStyle(); // 单元格样式：水平居中 & 垂直居中
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		final int columnSize = cfg.headers.length;
		drawHeader(cfg, wb, sheet, row0, style);
		boolean isFirst = true;
		HSSFCellStyle[] styles = new HSSFCellStyle[columnSize];
		final int size = list.size();
		final int maxRows = 60000;
		try {
			for (int i = 0, rowIndex = 0; i < size; i++) { // 行
				if (++rowIndex > maxRows) { // 如果记录数超过6W，则另起一个sheet
					rowIndex = 1;
					int x = i / maxRows;
					sheet = wb.createSheet("sheet" + x);
					row0 = sheet.createRow(0);
					drawHeader(cfg, wb, sheet, row0, style);
				}
				Object o = list.get(i);
				final boolean isArray = cfg.indexes != null;
				final Object[] array = isArray ? (Object[]) o : null;
				final PropertyBean bean = isArray ? null : PropertyBean.getInstance(o.getClass());
				Row row = sheet.createRow(rowIndex);
				row.setHeightInPoints(cfg.rowHeight);
				for (int j = 0; j < columnSize; j++) {// 列
					if (isFirst) {
						styles[j] = (HSSFCellStyle) wb.createCellStyle();
						styles[j].setAlignment(HorizontalAlignment.CENTER);
						styles[j].setVerticalAlignment(VerticalAlignment.CENTER);
					}
					Cell cell = row.createCell(j);
					Object value = isArray ? array == null ? null : array[cfg.indexes[j]]
							: bean.getProperty(o, cfg.fields[j]);
					String valueStr;
					if (value instanceof Number) { // 如果表格值是数字，则向右对齐
						if (isFirst) {
							styles[j].setAlignment(HorizontalAlignment.RIGHT);
						}
						valueStr = value.toString();
					} else if (value instanceof Date) {
						valueStr = dateFormat.format((Date) value);
					} else {
						valueStr = StringUtil.toString(value);
					}
					cfg.columnWidths[j] = calcColumnWidthFor(valueStr, cfg.maxColumnWidth);
					cell.setCellValue(valueStr);
					cell.setCellStyle(styles[j]);
				}
				isFirst = false;
			}
			os = ExportUtil.prepareOutputStream(responseStyle, fileName, ".xls");
			wb.write(os);
			if (responseStyle == 2 && consumer != null) {
				try (InputStream is = new ByteArrayInputStream(((ByteArrayOutputStream) os).toByteArray())) {
					consumer.accept(is);
				} catch (Exception e) {
					log.error("处理Excel输出流时出现异常", e);
				}
			}
		} catch (Exception e) {
			throw new IllegalStateException("导出文件异常", e);
		} finally {
			IOUtils.closeQuietly(os);
			IOUtils.closeQuietly(wb);
		}
	}

	protected static void drawHeader(ExcelConfig cfg, Workbook workbook, Sheet sheet, Row row0, HSSFCellStyle style) {
		row0.setHeightInPoints(cfg.rowHeight);
		for (int i = 0; i < cfg.headers.length; i++) {
			Cell cell = row0.createCell(i);
			cell.setCellValue(cfg.headers[i]);
			cell.setCellStyle(style);
			sheet.setColumnWidth(i, cfg.columnWidths[i]);
		}
	}

	public static int calcColumnWidthFor(String content, int maxColumnWidth) {
		int w = Math.min(StringUtil.length(content), 1) * 768;
		return Math.min(w, maxColumnWidth);
	}

	static final ConcurrentMap<Pair<String, Locale>, ExcelConfig> cache = new ConcurrentHashMap<>(32);

	private static ExcelConfig builderExcelConfig(Locale locale, String prefix, String propertyPairsString, Function<ExcelConfigBuilder, ExcelConfigBuilder> callback) {
		ExcelConfigBuilder builder = new ExcelConfigBuilder();
		if (propertyPairsString != null) {
			builder.resolvePairString(locale, prefix, propertyPairsString);
		}
		if (callback != null) {
			builder = callback.apply(builder);
		}
		return builder.build();
	}

	public static class ExcelConfig extends Export.Config {

		/**
		 * 默认行高
		 *
		 * @see Row#setHeightInPoints(float)
		 */
		public static int DEFAULT_ROW_HEIGHT = 20;

		/**
		 * 默认的最大列宽
		 *
		 * @see Sheet#setColumnWidth(int, int)
		 */
		public static int DEFAULT_MAX_COLUMN_WIDTH = 256 * 20;

		protected int[] columnWidths;
		protected int maxColumnWidth = DEFAULT_MAX_COLUMN_WIDTH;
		/**
		 * 行高
		 *
		 * @see Row#setHeightInPoints(float)
		 */
		public int rowHeight = DEFAULT_ROW_HEIGHT;

		public static ExcelConfigBuilder builder() {
			return new ExcelConfigBuilder();
		}

		@Override
		public void validate() {
			super.validate();
			Assert.isTrue(maxColumnWidth >= 256, "invalid ExcelConfig.maxColumnWidth(expected >= 256)");
			if (columnWidths == null) {
				columnWidths = columnWithsByNames(maxColumnWidth, headers);
			}
			final int columnSize = headers.length; // 列数
			Assert.isTrue(columnWidths.length == columnSize, "invalid ExcelConfig.columnWidths");
			Assert.isTrue(rowHeight >= 10, "invalid ExcelConfig.rowHeight(expected >= 10)");
		}

		static int[] columnWithsByNames(int maxWidth, String... headers) {
			final int[] columnWidths = new int[headers.length];
			for (int i = 0; i < headers.length; i++) {
				columnWidths[i] = calcColumnWidthFor(headers[i], maxWidth);// 设置宽度为合适的值
				columnWidths[i] = columnWidths[i] > maxWidth ? maxWidth : columnWidths[i] + 256;
			}
			return columnWidths;
		}

	}

	public static class ExcelConfigBuilder extends Export.ConfigBuilder<ExcelConfig, ExcelConfigBuilder> {

		protected ExcelConfigBuilder(ExcelConfig cfg) {
			super(cfg);
		}

		private ExcelConfigBuilder() {
			super(new ExcelConfig());
		}

		public ExcelConfigBuilder columnWiths(int... widths) {
			cfg.columnWidths = widths;
			return this;
		}

		public ExcelConfigBuilder maxColumnWidth(int maxColumnWidth) {
			cfg.maxColumnWidth = maxColumnWidth;
			return this;
		}

		public ExcelConfigBuilder rowHeight(int height) {
			cfg.rowHeight = height;
			return this;
		}

	}

}