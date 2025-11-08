package com.bojiu.webapp.base.util;

import java.util.Locale;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import me.codeplayer.util.FileUtil;

@SuppressWarnings("rawtypes")
public class ExportManager {

	// 暂时不必通过 Spring 容器动态注入
	private static final Map<String, ExportProvider> providerMap = ImmutableMap.of(
			"xlsx", ExcelExportProvider.INSTANCE,
			"xls", ExcelExportProvider.INSTANCE,
			"csv", CsvExportProvider.INSTANCE
	);

	public static ExportProvider getProvider(String fileExt, boolean lowerCaseRequired) {
		if (lowerCaseRequired) {
			fileExt = fileExt.toLowerCase(Locale.ROOT);
		}
		ExportProvider provider = providerMap.get(fileExt);
		if (provider == null) {
			throw new UnsupportedOperationException("Unsupported file extension:" + fileExt);
		}
		return provider;
	}

	public static ExportProvider getProvider(String fileExt) {
		return getProvider(fileExt, true);
	}

	public static ExportProvider getProviderByFileName(String fileName) {
		String ext = FileUtil.getExtension(fileName, true);
		return getProvider(ext);
	}

}