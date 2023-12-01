package com.candlk.context.auth;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.candlk.common.context.Context;
import com.candlk.common.web.Logs;
import com.candlk.common.web.Page;
import com.candlk.common.web.mvc.EmptyView;
import com.candlk.webapp.base.util.ExcelUtil;
import com.candlk.webapp.base.util.ExcelUtil.ExcelConfig;
import com.candlk.webapp.base.util.Export;
import me.codeplayer.util.StringUtil;
import me.codeplayer.util.X;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class ExportInterceptor implements HandlerInterceptor {

	public static final String EXPORT_EXCEL_TITLE = "exportExcelTitle";
	public static final String EXPORT_EXCEL_LIST = "exportExcelList";
	public static final String EXPORT_PARAM = "export";
	/**
	 * 该属性指示设置的 EXPORT_EXCEL_TITLE 数组是否是键值对形式
	 */
	public static final String EXCEL_TITLE_AS_PAIRS = "excelTitleAsPairs";

	public static boolean isExport(HttpServletRequest request) {
		final String export = request.getParameter(EXPORT_PARAM);
		return !StringUtils.isEmpty(export);
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) {
		if (handler instanceof HandlerMethod && isExport(request)) {
			request.setAttribute(Logs.RESPONSE, null); // 导出数据一般较多，因此无论方法实际返回什么，都不需要记录导出的数据明细
			Method method = ((HandlerMethod) handler).getMethod();
			handleExport(request, method, null, modelAndView);
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

	public static void handleExport(HttpServletRequest request, Export export, @Nullable List<Object> listToExport, @Nullable ModelAndView modelAndView) {
		if (listToExport == null) {
			listToExport = X.castType(request.getAttribute(EXPORT_EXCEL_LIST));
			if (listToExport == null && modelAndView != null) {
				Page<Object> page = X.castType(modelAndView.getModelMap().get(Context.internal().getPageAttr()));
				listToExport = page.getList();
			}
			if (listToExport == null) {
				listToExport = Collections.emptyList();
			}
		}

		final boolean customExt = Export.CUSTOM_EXT.equals(export.key()); // 客户端自定义扩展字段
		final String pairsStr = getFinalPairs(request, export.value());

		final boolean inMethod = Export.IN_METHOD.equals(pairsStr);
		final boolean disableCache = customExt || Export.NO_CACHE.equals(export.key());

		ExcelConfig cfg;
		if (!inMethod && !pairsStr.isEmpty()) {
			cfg = ExcelConfig.getCached(request, export.key(), pairsStr, disableCache, null);
		} else {
			cfg = ExcelConfig.getCached(request, export.key(), null, disableCache, (builder) -> {
				final String[] title = inMethod ? (String[]) request.getAttribute(EXPORT_EXCEL_TITLE) : export.values();
				if (title == null || title.length == 0) {
					throw new IllegalArgumentException("invalid @Export config!");
				}
				Object excelTitleAsPairs = request.getAttribute(EXCEL_TITLE_AS_PAIRS);
				return builder.propertyPairs(!Boolean.TRUE.equals(excelTitleAsPairs), title);
			});
		}
		final String zone = request.getParameter(EXPORT_PARAM);
		// 导出时设置时间格式和时区
		cfg.setZone(zone.startsWith("-") ? zone : ("+" + zone));
		ExcelUtil.export(listToExport, cfg, export.filename(), true);
		if (modelAndView != null) {
			modelAndView.setViewName(null);
			modelAndView.setView(EmptyView.INSTANCE);
		}
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
