package com.candlk.webapp.base.util;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.candlk.common.context.Context;
import com.candlk.common.context.RequestContext;
import com.candlk.common.util.Formats;
import com.candlk.common.util.PropertyBean;
import com.candlk.common.web.ServletUtil;
import com.candlk.context.web.RequestContextImpl;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;

/**
 * Excel导出
 */
@Slf4j
public final class ExcelUtil {

	/**
	 * @param list 表格内容
	 * @param cfg Excel配置
	 * @param response 是否作为对外部HTTP请求的响应输出
	 */
	public static void export(List<?> list, final ExcelConfig cfg, @Nullable String fileName, boolean response) {
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
		final int columnSize = cfg.columnNames.length;
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
				final boolean isArray = cfg.columnExprIndexes != null;
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
					Object value = isArray ? array == null ? null : array[cfg.columnExprIndexes[j]]
							: bean.getProperty(o, cfg.columnExprs[j]);
					String valueStr;
					if (value instanceof Number) { // 如果表格值是数字，则向右对齐
						if (isFirst) {
							styles[j].setAlignment(HorizontalAlignment.RIGHT);
						}
						valueStr = value.toString();
					} else if (value instanceof Date) {
						valueStr = cfg.dateFormat.format((Date) value);
					} else {
						valueStr = StringUtil.toString(value);
					}
					cfg.columnWidths[j] = calcColumnWidthFor(valueStr, cfg.maxColumnWidth);
					cell.setCellValue(valueStr);
					cell.setCellStyle(styles[j]);
				}
				isFirst = false;
			}
			if (response) {
				final RequestContext req = RequestContextImpl.get();
				final HttpServletRequest request = req.getRequest();
				if (StringUtil.isEmpty(fileName)) {
					fileName = request.getAttribute(Context.internal().getTitleAttr()) + ".xls";
				}
				final HttpServletResponse resp = req.getResponse();
				String destFileName = fileName;
				int dotPos = destFileName.lastIndexOf('.');
				if (dotPos != -1) {
					destFileName = destFileName.substring(0, dotPos);
				}
				destFileName = destFileName + '-' + Formats.formatDate(new Date(), "yyyyMMdd-HH-mm") + (dotPos > 0 ? fileName.substring(dotPos) : "");
				ServletUtil.responseHeaderForDownload(request, resp, destFileName);
				os = resp.getOutputStream();
			} else {
				os = new FileOutputStream(fileName);
			}
			wb.write(os);
		} catch (Exception e) {
			throw new IllegalStateException("导出文件异常", e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					log.error("关闭Excel输出流时出现异常", e);
				}
			}
			try {
				wb.close();
			} catch (IOException e) {
				log.error("关闭workBook出现异常", e);
			}
		}
	}

	protected static void drawHeader(ExcelConfig cfg, Workbook workbook, Sheet sheet, Row row0, HSSFCellStyle style) {
		row0.setHeightInPoints(cfg.rowHeight);
		for (int i = 0; i < cfg.columnNames.length; i++) {
			Cell cell = row0.createCell(i);
			cell.setCellValue(cfg.columnNames[i]);
			cell.setCellStyle(style);
			sheet.setColumnWidth(i, cfg.columnWidths[i]);
		}
	}

	protected static int calcColumnWidthFor(String content, int maxColumnWidth) {
		int w = Math.min(content.length(), 1) * 768;
		return Math.min(w, maxColumnWidth);
	}

	public static class ExcelConfig {

		/**
		 * 默认行高
		 *
		 * @see Row#setHeightInPoints(float)
		 */
		public static int DEFAULT_ROW_HEIGHT = 20;
		/**
		 * 默认日期格式化器("yyyy-MM-dd")
		 */
		public static FastDateFormat DEFAULT_DATE_FORMAT = Formats.getDateFormat("yyyy-MM-dd");

		/**
		 * 默认的最大列宽
		 *
		 * @see Sheet#setColumnWidth(int, int)
		 */
		public static int DEFAULT_MAX_COLUMN_WIDTH = 256 * 20;
		// fields
		String[] columnNames;
		String[] columnExprs;
		int[] columnWidths;
		int maxColumnWidth = DEFAULT_MAX_COLUMN_WIDTH;
		FastDateFormat dateFormat = DEFAULT_DATE_FORMAT;
		/** 如果数据对象是数组，则该int数组不为null，且是与 <code>columnExprs</code> 等价的整数数组 */
		int[] columnExprIndexes;
		/**
		 * 行高
		 *
		 * @see Row#setHeightInPoints(float)
		 */
		public int rowHeight = DEFAULT_ROW_HEIGHT;

		public ExcelConfig() {
		}

		public void setZone(String zone) {
			dateFormat = FastDateFormat.getInstance("yyyy-MM-dd hh:mm:ss", TimeZone.getTimeZone("GMT" + zone));
		}

		public static ExcelConfigBuilder builder() {
			return new ExcelConfigBuilder();
		}

		static final ConcurrentMap<String, ExcelConfig> cache = new ConcurrentHashMap<>(32);

		public static ExcelConfig getCached(HttpServletRequest request, @Nullable String key,
		                                    @Nullable String propertyPairsString, boolean disableCache, @Nullable Function<ExcelConfigBuilder, ExcelConfigBuilder> callback) {
			// 不缓存 ExcelConfig，每次都去builder
			if (disableCache) {
				return builderExcelConfig(propertyPairsString, callback);
			}

			if (StringUtil.isEmpty(key)) {
				key = request.getRequestURI();
			}
			ExcelConfig value = cache.get(key);
			if (value == null) {
				value = builderExcelConfig(propertyPairsString, callback);
				cache.put(key, value);
			}
			return value;
		}

	}

	private static ExcelConfig builderExcelConfig(String propertyPairsString, Function<ExcelConfigBuilder, ExcelConfigBuilder> callback) {
		ExcelConfigBuilder builder = new ExcelConfigBuilder();
		if (propertyPairsString != null) {
			builder.propertyPairsString(propertyPairsString);
		}
		if (callback != null) {
			builder = callback.apply(builder);
		}
		return builder.build();
	}

	public static class ExcelConfigBuilder {

		final ExcelConfig cfg = new ExcelConfig();

		private ExcelConfigBuilder() {
		}

		public ExcelConfigBuilder propertyPairsString(String propertyPairsString) {
			final String[] pairs = StringUtils.split(propertyPairsString, "=;");
			return propertyPairs(false, pairs);
		}

		public ExcelConfigBuilder propertyPairs(final boolean asTitleArrayOnly, String... propertyPairs) {
			Assert.isTrue(propertyPairs.length > 0 && (asTitleArrayOnly || (propertyPairs.length & 1) == 0), "invalid length of propertyPairs");
			final int columnSize = asTitleArrayOnly ? propertyPairs.length : propertyPairs.length >> 1;
			final String[] names = new String[columnSize], exprs = new String[columnSize];
			int[] exprIndexes = asTitleArrayOnly ? new int[columnSize] : null;
			for (int i = 0, j = 0; i < propertyPairs.length; j++) {
				if (asTitleArrayOnly) {
					exprIndexes[j] = j;
				}
				exprs[j] = asTitleArrayOnly ? Integer.toString(j) : propertyPairs[i++];
				names[j] = propertyPairs[i++];
			}
			cfg.columnNames = names;
			cfg.columnExprs = exprs;
			if (asTitleArrayOnly) {
				cfg.columnExprIndexes = exprIndexes;
			}
			return this;
		}

		public ExcelConfigBuilder columnWiths(int... widths) {
			cfg.columnWidths = widths;
			return this;
		}

		public ExcelConfigBuilder maxColumnWidth(int maxColumnWidth) {
			cfg.maxColumnWidth = maxColumnWidth;
			return this;
		}

		public static int[] columnWithsByNames(int maxWidth, String... names) {
			final int[] columnWidths = new int[names.length];
			for (int i = 0; i < names.length; i++) {
				columnWidths[i] = calcColumnWidthFor(names[i], maxWidth);// 设置宽度为合适的值
				columnWidths[i] = columnWidths[i] > maxWidth ? maxWidth : columnWidths[i] + 256;
			}
			return columnWidths;
		}

		public ExcelConfigBuilder dateFormat(String pattern) {
			cfg.dateFormat = Formats.getDateFormat(pattern);
			return this;
		}

		public ExcelConfigBuilder rowHeight(int height) {
			cfg.rowHeight = height;
			return this;
		}

		public ExcelConfig build() {
			Assert.isTrue(cfg.columnNames != null && cfg.columnNames.length > 0, "invalid ExcelConfig.columnNames");
			final int columnSize = cfg.columnNames.length; // 列数
			Assert.isTrue(cfg.columnExprs != null && cfg.columnExprs.length == columnSize, "invalid ExcelConfig.columnExprs");
			Assert.isTrue(cfg.maxColumnWidth >= 256, "invalid ExcelConfig.maxColumnWidth(expected >= 256)");
			if (cfg.columnWidths == null) {
				cfg.columnWidths = columnWithsByNames(cfg.maxColumnWidth, cfg.columnNames);
			}
			Assert.isTrue(cfg.columnWidths.length == columnSize, "invalid ExcelConfig.columnWidths");
			Assert.isTrue(cfg.dateFormat != null, "invalid ExcelConfig.dateFormat");
			Assert.isTrue(cfg.rowHeight >= 10, "invalid ExcelConfig.rowHeight(expected >= 10)");
			if (cfg.columnExprIndexes == null && NumberUtil.isNumber(cfg.columnExprs[0])) { // 如果 属性表达式 为数字，则表示对象为数组
				cfg.columnExprIndexes = new int[columnSize];
				for (int i = 0; i < columnSize; i++) {
					cfg.columnExprIndexes[i] = NumberUtil.getInt(cfg.columnExprs[i]);
				}
			}
			return cfg;
		}

	}

}
