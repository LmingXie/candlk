package com.candlk.common.web.mvc;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.servlet.http.*;

import com.alibaba.fastjson2.JSON;
import com.candlk.common.context.Context;
import com.candlk.common.context.Env;
import com.candlk.common.model.Messager;
import com.candlk.common.model.TimeInterval;
import com.candlk.common.web.*;
import lombok.Getter;
import me.codeplayer.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

public class BaseProxyRequest {

	/** 表示 尚未初始化设置的 TimeInterval 对象 */
	protected static final TimeInterval UNINITIALIZED_TIME_INTERVAL = new TimeInterval(null, null, -1, -1);

	//
	@Getter
	protected HttpServletRequest request;
	@Getter
	protected HttpServletResponse response;

	protected ModelMap model;
	private TimeInterval interval = UNINITIALIZED_TIME_INTERVAL;
	protected Page<?> page;

	protected transient Client client;
	private static Supplier<TimeInterval> timeIntervalSupplier = TimeInterval::new;

	public BaseProxyRequest init(HttpServletRequest request, HttpServletResponse response, ModelMap model) {
		this.request = request;
		this.response = response;
		this.model = model;
		this.page = initPage(null);
		return this;
	}

	/**
	 * @param prefix 默认为 <code>"interval."</code>
	 * @param localized 是否基于用户上下文进行时区本地化
	 */
	public TimeInterval initInterval(@Nullable String prefix, boolean localized, final boolean putInModel) {
		final String beginStr, endStr;
		if (prefix == null) {
			beginStr = "interval.begin";
			endStr = "interval.end";
		} else if (prefix.isEmpty()) {
			beginStr = "begin";
			endStr = "end";
		} else if (prefix.charAt(prefix.length() - 1) == '.') {
			beginStr = prefix.concat("begin");
			endStr = prefix.concat("end");
		} else {
			beginStr = prefix.concat(".begin");
			endStr = prefix.concat(".end");
		}
		Date begin = DateConverter.convertToDate(request.getParameter(beginStr), true), end = DateConverter.convertToDate(request.getParameter(endStr), true);
		if (begin != null || end != null) {
			final TimeInterval interval = localized ? timeIntervalSupplier.get() : new TimeInterval();
			interval.setBegin(begin);
			interval.setEnd(end);
			if (putInModel) {
				putInModel(prefix, interval);
			}
			return interval;
		}
		return null;
	}

	/**
	 * @param prefix 默认为 <code>"interval."</code>
	 */
	public TimeInterval initInterval(@Nullable String prefix, final boolean putInModel) {
		return initInterval(prefix, true, putInModel);
	}

	public static void setTimeIntervalSupplier(Supplier<TimeInterval> supplier) {
		timeIntervalSupplier = Assert.notNull(supplier);
	}

	/**
	 * @param prefix 默认为 <code>"interval."</code>
	 */
	public void putInModel(@Nullable String prefix, TimeInterval interval) {
		if (prefix == null) {
			model.put("interval", interval);
		} else if (prefix.isEmpty()) {
			model.put("begin", interval.getBegin());
			model.put("end", interval.getEnd());
		} else {
			int lastIndex = prefix.length() - 1;
			if (prefix.charAt(lastIndex) == '.') {
				prefix = prefix.substring(0, lastIndex);
			}
			model.put(prefix, interval);
		}
	}

	/**
	 * @param prefix 默认为 <code>"page."</code>
	 */
	public <T> Page<T> initPage(@Nullable String prefix) {
		String pageStr = prefix == null ? "_page" : prefix.concat("_page");
		String sizeStr = prefix == null ? "_size" : prefix.concat("_size");
		int current = getInt(pageStr, 1), size = getInt(sizeStr, 10);
		/* TODO 定义 default
		if (current == 1 && size == 10) {

		}
		*/
		return new Page<>(current, size);
	}

	public TimeInterval getInterval() {
		return getInterval(null);
	}

	/**
	 * @param localized 是否基于用户上下文进行时区本地化
	 */
	public TimeInterval getInterval(boolean localized) {
		if (interval == UNINITIALIZED_TIME_INTERVAL) {
			this.interval = initInterval(null, localized, false);
		}
		return interval;
	}

	/**
	 * 将本地时区的时间表示转为指定时区的相同时间表示
	 * 例如：将 "2023-01-01 00:00 GMT+2" 转为 "GMT+8" 时区的 "2023-01-01 00:00 GMT+8" 表示
	 *
	 * @param toTimeZone 指定的目标时区
	 */
	public TimeInterval getInterval(@Nullable TimeZone toTimeZone) {
		if (interval == UNINITIALIZED_TIME_INTERVAL) {
			TimeInterval ti = initInterval(null, true, false);
			if (ti != null && toTimeZone != null && ti.getTimeZone() != toTimeZone) {
				Date begin = ti.getBegin(), end = ti.getEnd();
				if (begin != null || end != null) {
					EasyDate d = ti.getEasyDate(begin == null ? end : begin);
					final TimeZone from = d.getTimeZone();
					if (from != toTimeZone) {
						int diff = from.getRawOffset() - toTimeZone.getRawOffset();
						if (begin != null) {
							begin.setTime(begin.getTime() + diff);
						}
						if (end != null) {
							end.setTime(end.getTime() + diff);
						}
						ti.setTimeZone(toTimeZone);
						d.setTimeZone(toTimeZone);
					}
				}
			}
			this.interval = ti;
		}
		return interval;
	}

	public void setInterval(TimeInterval interval) {
		this.interval = interval;
	}

	/**
	 * 请注意该方法可能返回【不可变】的 Page 实例
	 */
	public <T> Page<T> getPage() {
		if (page == null) {
			page = initPage(null);
		}
		/*
		if (page == Page.getDefault()) {
			return null;
		}
		*/
		return X.castType(page);
	}

	public <T> Page<T> asPage(List<T> all) {
		if (all == null) {
			all = Collections.emptyList();
		}
		final int total = all.size();
		final Page<T> old = getPage();
		final long[] adjusts = Page.adjustWithOffsetAndLimit(old == null ? 1L : old.getCurrent(), old == null ? 10 : old.getSize(), 10);
		int offset = NumberUtil.getInt(adjusts[1]), size = NumberUtil.getInt(adjusts[2]);
		return new Page<T>(adjusts[0], size, total).setList(offset >= total ? null : all.subList(offset, Math.min(total, offset + size)));
	}

	public void setPage(Page<?> page) {
		this.page = page;
		model.put(Context.internal().getPageAttr(), page);
	}

	/**
	 * 设置页面的标题
	 *
	 * @param title 标题
	 */
	public void setTitle(String title) {
		model.put(Context.internal().getTitleAttr(), title);
	}

	/**
	 * 获取指定参数名称 int 形式参数值
	 *
	 * @param name 参数名称
	 * @throws NumberFormatException 如果参数为空或无效
	 */
	public int getInt(final String name) throws NumberFormatException {
		return Math.toIntExact(getLongInternal(name, 0L, true));
	}

	/**
	 * 获取指定参数名称int形式参数值
	 *
	 * @param name 参数名称
	 * @param defaultValue 如果参数值无效，则返回该值
	 */
	public int getInt(final String name, final int defaultValue) {
		return Math.toIntExact(getLongInternal(name, defaultValue, false));
	}

	/**
	 * 获取指定参数名称 integer 形式参数值
	 *
	 * @param name 参数名称
	 * @param defaultValue 如果参数值无效，则返回该值
	 */
	public Integer getInteger(String name, @Nullable Integer defaultValue) {
		return getNumber(name, Integer::parseInt, defaultValue);
	}

	/**
	 * 获取指定参数名称 long 形式参数值
	 *
	 * @param name 参数名称
	 * @throws NumberFormatException 如果参数为空或无效
	 */
	public long getLong(final String name) throws NumberFormatException {
		return getLongInternal(name, 0L, true);
	}

	/**
	 * 获取指定参数名称 long 形式参数值
	 *
	 * @param name 参数名称
	 * @param defaultValue 如果参数值无效，则返回该值
	 */
	public long getLong(final String name, final long defaultValue) {
		return getLongInternal(name, defaultValue, false);
	}

	/**
	 * 获取指定参数名称 Long 形式参数值
	 *
	 * @param name 参数名称
	 * @param defaultValue 如果参数值无效，则返回该值
	 */
	public Long getLongObj(String name, @Nullable Long defaultValue) {
		return getNumber(name, Long::parseLong, defaultValue);
	}

	/**
	 * 获取指定参数名称 Number 形式的参数值
	 *
	 * @param name 参数名称
	 * @param defaultValue 如果参数值无效，则返回该值
	 */
	protected long getLongInternal(String name, long defaultValue, boolean errorIfInvalid) {
		final String val = request.getParameter(name);
		if (StringUtils.isEmpty(val)) {
			if (errorIfInvalid) {
				throw new NumberFormatException("For input string: \"" + val + "\"");
			}
			return defaultValue;
		}
		if (errorIfInvalid) {
			return Long.parseLong(val);
		}
		try {
			return Long.parseLong(val);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * 获取指定参数名称 Number 形式的参数值
	 *
	 * @param name 参数名称
	 * @param defaultValue 如果参数值无效，则返回该值
	 */
	protected <N extends Number> N getNumber(String name, java.util.function.Function<String, N> numberParser, N defaultValue) {
		final String val = request.getParameter(name);
		if (StringUtils.isEmpty(val)) {
			return defaultValue;
		}
		try {
			return numberParser.apply(val);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * 快速进行文件下载
	 *
	 * @param inputStream 指定的用于下载的文件输入流
	 * @param downloadFileName 指定响应到客户浏览器的下载文件名称
	 * @return result name
	 */
	public String _download(InputStream inputStream, String downloadFileName) {
		request.setAttribute("__is", inputStream);
		request.setAttribute("__file", new String(downloadFileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
		return "global_download";
	}

	/**
	 * 快速进行文件下载
	 *
	 * @param file 指定的用于下载的文件
	 * @param downloadFileName 指定响应到客户浏览器的下载文件名称
	 * @return result name
	 * @throws IllegalArgumentException see {@link FileNotFoundException}
	 */
	public String _download(File file, String downloadFileName) throws IllegalArgumentException {
		try {
			return _download(new FileInputStream(file), downloadFileName);
		} catch (FileNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * 快速进行文件下载
	 *
	 * @param filepath 文件所在路径
	 * @param downloadFileName 指定响应到客户浏览器的下载文件名称
	 * @return result name
	 * @throws IllegalArgumentException see {@link FileNotFoundException}
	 */
	public String _download(String filepath, String downloadFileName) throws IllegalArgumentException {
		return _download(new File(filepath), downloadFileName);
	}

	/**
	 * 临时重定向到指定的URL
	 *
	 * @param url 指定的URL
	 */
	public String redirect(String url) {
		if (isAcceptJSON()) {
			return writeJSON(new Messager<Void>("redirect", null, url));
		}
		return UrlBasedViewResolver.REDIRECT_URL_PREFIX.concat(url);
	}

	/**
	 * 将指定文本内容（以 UTF-8 字符编码）写入响应流中
	 *
	 * @param text 需要写入的文本内容
	 */
	public String writeText(String text) {
		return writeToResponse(text, MediaType.TEXT_HTML_VALUE + ";charset=UTF-8");
	}

	/**
	 * 将指定文本内容写入响应流中
	 *
	 * @param text 需要写入的文本内容
	 * @param contentType 指定的内容类型
	 */
	public static String writeToResponse(final HttpServletResponse response, String text, String contentType) {
		response.setContentType(contentType);
		try {
			response.getWriter().write(text);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return EmptyView.VIEW_NAME;
	}

	/**
	 * 将指定文本内容写入响应流中
	 *
	 * @param text 需要写入的文本内容
	 * @param contentType 指定的内容类型
	 */
	public String writeToResponse(String text, String contentType) {
		request.setAttribute(Logs.RESPONSE, text);
		return writeToResponse(response, text, contentType);
	}

	/**
	 * 将指定文本内容写入响应流中，并设置字符集编码为UTF-8
	 *
	 * @param text 需要写入的文本内容
	 */
	public String writeToResponse(String text) {
		return writeToResponse(text, null);
	}

	public Client getClient() {
		if (client == null) {
			client = Client.getClient(request);
		}
		return client;
	}

	/**
	 * 是否是后台请求
	 */
	public boolean fromBackstage() {
		return Context.get().fromBackstage(request);
	}

	/**
	 * 判断当前请求是否为POST请求
	 */
	public boolean isPOST() {
		return "POST".equals(request.getMethod());
	}

	/**
	 * 判断当前请求是否为 GET 请求
	 */
	public boolean isGET() {
		return "GET".equals(request.getMethod());
	}

	/**
	 * 判断当前请求是否为 【只读】 请求
	 */
	public boolean isReadOnly() {
		String method = request.getMethod();
		return switch (method) {
			case "GET", "OPTIONS", "HEAD", "TRACE" -> true;
			default -> false;
		};
	}

	/**
	 * 返回 BigDecimal 形式的参数值
	 *
	 * @param clip 如果小于等于0，则不做任何处理；如果=1，则对数值进行保留两位的四舍五入处理；如果 =2，则在1的基础上校验其值不得小于0
	 */
	public BigDecimal getBigDecimal(String name, int clip) {
		BigDecimal val = new BigDecimal(request.getParameter(name));
		if (clip > 0) {
			val = scale(val, Context.internal().getScale());
			if (clip > 1 && val.compareTo(BigDecimal.ZERO) < 0) {
				throw new IllegalArgumentException();
			}
		}
		return val;
	}

	public static BigDecimal scale(BigDecimal val) {
		return scale(val, Context.internal().getScale());
	}

	public static BigDecimal scale(BigDecimal val, final int maxScale) {
		if (val.scale() > maxScale) {
			val = val.setScale(maxScale, Context.internal().getRoundingMode());
		}
		return val;
	}

	/**
	 * 返回 BigDecimal 形式的参数值
	 *
	 * @param defaultValue 参数值不合法时，返回的默认值（可以为null）
	 * @param clip clip 如果小于等于0，则不做任何处理；如果=1，则对数值进行保留两位的四舍五入处理；如果 =2，则在1的基础上校验其值不得小于0
	 */
	public BigDecimal getBigDecimal(String name, BigDecimal defaultValue, int clip) {
		BigDecimal val = NumberUtil.getBigDecimal(request.getParameter(name), defaultValue);
		if (clip > 0 && !val.equals(defaultValue)) {
			val = scale(val, Context.internal().getScale());
			if (clip > 1 && val.compareTo(BigDecimal.ZERO) < 0) {
				throw new IllegalArgumentException();
			}
		}
		return val;
	}

	/**
	 * 清空当前session存储，并使其作废
	 */
	public void clearSession() {
		request.getSession().invalidate();
	}

	/**
	 * 获取请求用户的真实IP地址<br>
	 * 该方法会将第一次获取到的用户IP地址缓存到session中，之后直接从session中获取
	 */
	public String getIP() {
		return Context.get().getClientIP(request);
	}

	/**
	 * 把object对象转换为JSON格式后response出去
	 */
	@SuppressWarnings("deprecation")
	public static String writeJSON(HttpServletResponse response, Object object) {
		return writeToResponse(response, object == null ? "{}" : JSON.toJSONString(object), MediaType.APPLICATION_JSON_UTF8_VALUE);
	}

	/**
	 * 把object对象转换为JSON格式后response出去
	 */
	public String writeJSON(Object object) {
		return writeJSON(response, object);
	}

	/**
	 * 把对象转换为JSON格式后 response 出去
	 */
	public static String writeJSON(HttpServletResponse response, boolean forApp, Object obj) {
		if (forApp && !Env.inProduction() && obj instanceof Messager<?> msger && StringUtil.notEmpty(Context.nav().getRoot())) {
			// 针对非正式环境，避免向APP端输出的 url 属性中带有 root 路径前缀
			msger.setUrl(StringUtils.removeStart(msger.getUrl(), Context.nav().getRoot()));
		}
		return writeJSON(response, obj);
	}

	/**
	 * 把 对象 转换为JSON文本后 response 出去
	 */
	public String writeJSON(boolean forApp, Object obj) {
		return writeJSON(response, forApp, obj);
	}

	/**
	 * 响应404
	 */
	public String responseNotFound() {
		try {
			response.sendError(404, request.getRequestURI());
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return null;
	}

	public void attr(String name, Object o) {
		model.put(name, o);
	}

	public void attrInternal(String name, Object o) {
		request.setAttribute(name, o);
	}

	public Object attrInternal(String name) {
		return request.getAttribute(name);
	}

	public <T> T sessionAttr(String name) {
		HttpSession session = request.getSession(false);
		if (session == null) {
			return null;
		}
		return X.castType(session.getAttribute(name));
	}

	public void sessionAttr(String name, Object o) {
		request.getSession().setAttribute(name, o);
	}

	public void removeSessionAttr(String name) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.removeAttribute(name);
		}
	}

	public String getParameter(String name) {
		return request.getParameter(name);
	}

	public String[] getParameterValues(String name) {
		return request.getParameterValues(name);
	}

	public String getRequestURI() {
		return request.getRequestURI();
	}

	public String getQueryString() {
		return request.getQueryString();
	}

	public String getMappingPath() {
		return (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
	}

	public boolean isAcceptJSON() {
		return ServletUtil.isAcceptJSON(request);
	}

}
