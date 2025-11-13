package com.bojiu.context.auth;

import java.lang.reflect.Method;
import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bojiu.common.context.Context;
import com.bojiu.common.context.I18N;
import com.bojiu.common.model.Bean;
import com.bojiu.common.util.Formats;
import com.bojiu.common.util.SpringUtil;
import com.bojiu.common.web.Logs;
import com.bojiu.common.web.Page;
import com.bojiu.common.web.mvc.EmptyView;
import com.bojiu.context.ContextImpl;
import com.bojiu.context.model.RiskStatus;
import com.bojiu.context.web.RequestContextImpl;
import com.bojiu.webapp.base.util.*;
import me.codeplayer.util.StringUtil;
import me.codeplayer.util.X;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@SuppressWarnings("unchecked")
public class ExportInterceptor implements HandlerInterceptor {

	public static final String EXPORT_PARAM = "export";

	public static boolean isExport(HttpServletRequest request) {
		return StringUtils.isNotBlank(request.getParameter(EXPORT_PARAM))
				&& RiskStatus.BACKGROUND_LIMIT.assertUnlimited(request);
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) {
		if (isExport(request) && handler instanceof HandlerMethod hm) {
			response.setCharacterEncoding("UTF-8");
			handleExport(request, hm.getMethod(), null, modelAndView);
		}
	}

	public static boolean handleExport(HttpServletRequest request, Method method, @Nullable List<Object> listToExport, @Nullable ModelAndView modelAndView) {
		final Export export = method.getAnnotation(Export.class);
		if (export == null) {
			return false;
		}
		handleExport(request, export, listToExport, modelAndView);
		return true;
	}

	static boolean allowCustom = true, i18n = true, timeZone = true;

	@SuppressWarnings("rawtypes")
	public static void handleExport(HttpServletRequest request, Export export, @Nullable List<Object> listToExport, @Nullable ModelAndView modelAndView) {
		if (listToExport == null) {
			listToExport = tryLoadData(request, modelAndView);
		}
		// 导出数据一般较多，不需要记录导出的数据明细，只记录一下导出的数据量 {"exportCount":12345}
		final int size = X.size(listToExport);
		if (size >= 10_0000) { // 导出数据量超过 10W 时，记录一下日志，避免后面可能因为 OOM，导致请求日志中看不到 exportCount
			SpringUtil.log.warn("【数据导出】本次导出的数据量={}，用户ID={}，请求路径={}", size, Bean.idOf(RequestContextImpl.getSessionUser(request)), request.getRequestURL());
		}
		request.setAttribute(Logs.RESPONSE, "{\"exportCount\":" + size + "}");
		// 获取表格标题，优先从请求中获取，如果没有则从注解中获取
		// final String fileExt = "csv"; // 如果是从文件名称中获取，一定要先转为小写
		final ExportProvider provider = ExportUtil.PROVIDER; // ExportManager.getProvider(fileExt);

		final Export.Config cfg = loadExportConfig(request, export, allowCustom, provider);

		FastDateFormat dateFormat = timeZone
				? Formats.getDateFormat(export.dateFormat(), RequestContextImpl.get().getTimeZone())
				: Formats.getDateFormat(export.dateFormat());

		String fileName = getFinalFileName(request, export.filename(), i18n ? RequestContextImpl.doGetLanguage(request).locale : null);

		// 导出时设置时间格式和时区
		provider.export(listToExport, cfg, dateFormat, fileName, 0, null);
		if (modelAndView != null) {
			modelAndView.setViewName(null);
			modelAndView.setView(EmptyView.INSTANCE);
		}
	}

	public static String getFinalFileName(@Nullable HttpServletRequest request, @Nullable String exportFileName, @Nullable Locale locale) {
		if (request == null) {
			request = RequestContextImpl.get().getRequest();
		}
		String fileName = null;
		if (request != null) {
			fileName = (String) request.getAttribute(Export.attrFilename);
		}
		if (StringUtil.isEmpty(fileName) && StringUtil.notEmpty(exportFileName)) {
			fileName = exportFileName;
		}
		if (StringUtil.notEmpty(fileName)) {
			if (i18n && locale != null) {
				return I18N.msg(Export.FILENAME_I18N_PREFIX + fileName, locale);
			}
		} else if (request != null) {
			fileName = (String) request.getAttribute(ContextImpl.internal().getTitleAttr());
		}
		return fileName;
	}

	@Nonnull
	public static Export.Config loadExportConfig(HttpServletRequest request, Export export, boolean allowCustom, ExportProvider provider) {
		String value = "";
		// 先从 request 上下文中获取动态设置，获取不到时，再从注解中获取静态设置
		String[] values = (String[]) request.getAttribute(Export.attrValues);
		if (!X.isValid(values)) {
			value = (String) request.getAttribute(Export.attrValue);

			if (StringUtil.isEmpty(value)) {
				value = export.value();

				if (StringUtil.isEmpty(value)) {
					values = export.values();
				}
			}
		}
		// 目前只有 value() 字符串才支持客户端动态扩展导出字段
		final String pairsStr = allowCustom ? getFinalPairs(request, value) : value;
		final boolean disableCache = Export.NO_CACHE.equals(export.key()) || X.size(pairsStr) != value.length();
		final String i18nKeyPrefix = request.getAttribute(Export.attrI18nPrefix) instanceof String str ? str : export.i18nPrefix();
		// 获取请求语言
		final Locale locale = i18n ? RequestContextImpl.doGetLanguage(request).locale : null;

		final Export.Config cfg;
		if (StringUtil.notEmpty(pairsStr)) {
			cfg = provider.getCached(request, export.key(), locale, i18nKeyPrefix, pairsStr, disableCache, null);
		} else {
			if (X.size(values) == 0) { // 没有 value，就一定要有 values
				throw new IllegalArgumentException("invalid @Export config!");
			}
			final String[] finalValues = values;
			cfg = provider.getCached(request, export.key(), locale, null, null, disableCache,
					(builder) -> ((Export.ConfigBuilder) builder).resolvePairs(getLabelOnly(request, export), locale, i18nKeyPrefix, finalValues));
		}
		return cfg;
	}

	@Nonnull
	public static Export.Config loadExportConfig(HttpServletRequest request, Export export, boolean allowCustom) {
		return loadExportConfig(request, export, allowCustom, ExportUtil.PROVIDER);
	}

	static boolean getLabelOnly(HttpServletRequest request, Export export) {
		Object labelOnly = request.getAttribute(Export.attrLabelOnly);
		return labelOnly != null ? (Boolean) labelOnly : export.labelOnly();
	}

	@Nonnull
	static List<Object> tryLoadData(HttpServletRequest request, @Nullable ModelAndView modelAndView) {
		List<Object> listToExport = (List<Object>) request.getAttribute(Export.attrData);
		if (listToExport == null && modelAndView != null) {
			Page<Object> page = (Page<Object>) modelAndView.getModelMap().get(Context.internal().getPageAttr());
			listToExport = page.getList();
		}
		if (listToExport == null) {
			listToExport = Collections.emptyList();
		}
		return listToExport;
	}

	/**
	 * 获取自定义导出的完整 title pairs
	 */
	public static String getFinalPairs(HttpServletRequest request, String defaultPairs) {
		// 和前端约定以逗号分隔，2个属性名的顺序和长度是相对应的关系
		String exportField = request.getParameter("exportField");
		if (StringUtil.notEmpty(exportField)) {
			String exportFieldName = request.getParameter("exportFieldName");
			String[] fields = exportField.split(",");
			String[] names = exportFieldName.split(",");
			final int size = fields.length, nameSize = names.length;
			final StringBuilder sb = new StringBuilder(defaultPairs.length() + fields.length * 16).append(defaultPairs);
			for (int i = 0; i < size; i++) {
				if (nameSize > i) {
					sb.append(';').append(fields[i]).append('=').append(names[i]);
				}
			}
			return sb.toString();
		}
		return defaultPairs;
	}

}