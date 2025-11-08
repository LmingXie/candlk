package com.bojiu.webapp.base.util;

import java.io.*;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;

import com.bojiu.common.context.RequestContext;
import com.bojiu.common.util.Formats;
import com.bojiu.common.web.ServletUtil;
import com.bojiu.context.web.RequestContextImpl;

public class ExportUtil {

	public static final CsvExportProvider PROVIDER = CsvExportProvider.INSTANCE;

	public static void export(List<?> list, @Nullable String fileName, int responseStyle) {
		CsvExportProvider.export(list, fileName, responseStyle);
	}

	/**
	 * 去掉文件扩展名后缀，例如："hello.png" => "hello"
	 */
	public static String removeExt(String fileName) {
		final int pos = fileName.lastIndexOf('.');
		return pos > 1 ? fileName.substring(0, pos) : fileName;
	}

	/**
	 * @param fileExt 文件扩展名。例如：".csv" 或 ".xls"
	 */
	public static String toDestFileName(String fileName, String fileExt) {
		return removeExt(fileName) + '-' + Formats.formatDate(new Date(), "yyyyMMdd-HHmm") + fileExt;
	}

	/**
	 * @param responseStyle 0=对外部HTTP请求的响应输出；1=FileOutputStream；2=ByteArrayOutputStream并调用consumer
	 * @param fileName 如果指定了文件名，则必须包含后缀
	 * @param fileExt 文件扩展名。例如：".csv" 或 ".xls"
	 */
	public static OutputStream prepareOutputStream(int responseStyle, String fileName, final String fileExt) throws IOException {
		return switch (responseStyle) {
			case 0 -> {
				final RequestContext context = RequestContextImpl.get();
				String destFileName = toDestFileName(fileName, fileExt);
				HttpServletResponse resp = context.getResponse();
				ServletUtil.responseHeaderForDownload(context.getRequest(), resp, destFileName);
				yield resp.getOutputStream();
			}
			case 1 -> new FileOutputStream(fileName);
			default -> new ByteArrayOutputStream();
		};
	}

}