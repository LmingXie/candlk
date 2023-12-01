package com.candlk.context.web;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import com.candlk.common.context.Context;
import com.candlk.common.context.RequestContext;
import com.candlk.common.model.TimeInterval;
import com.candlk.common.web.Page;
import com.candlk.common.web.mvc.BaseProxyRequest;
import com.candlk.context.SystemInitializer;
import com.candlk.context.auth.ExportInterceptor;
import com.candlk.context.config.GlobalConfig;
import com.candlk.context.model.*;
import me.codeplayer.util.StringUtil;
import me.codeplayer.util.X;
import org.apache.commons.lang3.StringUtils;

public class ProxyRequest extends BaseProxyRequest {

	/**
	 * 获取当前session的用户对象<br>
	 * 如果用户未登录，则返回 null
	 */
	public <U extends Member> U getSessionUser() {
		return RequestContext.getSessionUser(request);
	}

	/**
	 * 获取语言
	 */
	public Language sessionLanguage() {
		return RequestContextImpl.get().sessionLanguage();
	}

	/**
	 * 获取请求头数据
	 */
	public String getHeader(String head) {
		return request.getHeader(head);
	}

	/**
	 * 获取当前操作时间
	 */
	public Date now() {
		return RequestContextImpl.get().now();
	}

	/**
	 * 设置当前session的用户对象
	 */
	public void setSessionUser(Member user) {
		RequestContext.setSessionUser(request, user);
	}

	/**
	 * 清空当前session中存储的用户对象
	 */
	public void removeSessionUser() {
		removeSessionAttr(RequestContext.SESSION_USER);
	}

	/**
	 * 返回来源地址<br>
	 * 如果从登录页跳转的链接，或者新开窗口导致没有获取到来源地址，则返回null
	 */
	public String getSessionReferer() {
		Object referer = sessionAttr("referer");
		String ref = referer == null ? null : referer.toString();
		if (referer != null && !ref.contains("login")) {
			return ref;
		}
		return null; // 如果从登录页或者新开一个页面进入操作页，进行操作后，默认跳转至首页。
	}

	public static String getAppId(HttpServletRequest request) {
		return request.getHeader(Context.internal().getAppIdHeaderName());
	}

	public String getAppId() {
		return getAppId(request);
	}

	public Long getMerchantId() {
		return RequestContextImpl.getMerchantId(request);
	}

	/**
	 * 获取请求客户端名称
	 */
	public String getName() {
		return getName(getAppId());
	}

	public static String getName(@Nullable String appId) {
		if (StringUtil.notEmpty(appId)) {
			int endPos = appId.indexOf('/');
			if (endPos != -1) {
				return appId.substring(0, endPos);
			}
		}
		return "";
	}

	/**
	 * 获取请求客户端版本号
	 */
	public String getVersion() {
		return getVersion(getAppId());
	}

	/**
	 * 获取请求客户端版本号
	 */
	public static String getVersion(@Nullable String appId) {
		if (StringUtil.notEmpty(appId)) {
			int endPos = appId.lastIndexOf('/');
			if (endPos != -1) {
				int beginPos = appId.lastIndexOf('/', endPos - 1);
				if (beginPos != -1) {
					return appId.substring(beginPos + 1, endPos);
				}
			}
		}
		return "";
	}

	/**
	 * 获取请求渠道号
	 */
	public String getChannel() {
		return getChannel(getAppId());
	}

	public static String getChannel(@Nullable String appId) {
		if (StringUtil.notEmpty(appId)) {
			return StringUtils.substringAfterLast(appId, "/");
		}
		return "";
	}

	public static String getClient(@Nullable String appId) {
		if (StringUtil.notEmpty(appId)) {
			return StringUtils.substringBetween(appId, "/", "/");
		}
		return "";
	}

	/**
	 * 清除 session 中的邀请码
	 */
	public final void clearInviteCode(String inviteCode) {
		if (StringUtil.notEmpty(inviteCode)) {
			removeSessionAttr(Context.internal().getInviteCodeSessionAttr());
		}
	}

	public boolean isFlush() {
		return getInt("flush", 0) == 1;
	}

	public void exportExcelAttrs(String[] title, List<?> list, boolean excelTitleAsPairs) {
		if (title != null) {
			request.setAttribute(ExportInterceptor.EXPORT_EXCEL_TITLE, title);
		}
		if (list != null) {
			request.setAttribute(ExportInterceptor.EXPORT_EXCEL_LIST, list);
		}
		if (excelTitleAsPairs) {
			request.setAttribute(ExportInterceptor.EXCEL_TITLE_AS_PAIRS, true);
		}
	}

	public boolean isExport() {
		return ExportInterceptor.isExport(request);
	}

	protected static Boolean admin;

	/**
	 * 请注意该方法可能返回【不可变】的 Page 实例
	 */
	@Override
	public <T> Page<T> getPage() {
		return getPage(false);
	}

	/**
	 * 请注意该方法可能返回【不可变】的 Page 实例
	 */
	public <T> Page<T> getPage(boolean allowExportAlready) {
		if (page == null) {
			page = initPage(null);
		}
		if (admin == null) {
			admin = StringUtils.containsIgnoreCase(Context.applicationName(), "admin");
		}
		if ((allowExportAlready || admin) && isExport()) {
			page.setSize(Page.SIZE_SKIP_LIMIT);
		}
		return X.castType(page);
	}

	public void exportExcelAttrs(String[] title, List<?> list) {
		exportExcelAttrs(title, list, false);
	}

	/**
	 * 检查商户ID是否与当前实体一致（否则报错）
	 */
	public Long assertSame(Long merchantId) {
		WithMerchant.assertSame(getMerchantId(), merchantId);
		return merchantId;
	}

	/**
	 * 检查指定商户ID的用户是否有权访问指定商户归属的对象（平台可以访问商户），否则报错
	 */
	public Long assertCanAccess(Long merchantId) {
		WithMerchant.assertCanAccess(getMerchantId(), merchantId);
		return merchantId;
	}

	/** 当前请求来自平台员工 */
	public boolean platform() {
		return GlobalConfig.PLATFORM_MERCHANT_ID == (long) getMerchantId();
	}

	public Long applyMerchantId(Consumer<Long> merchantIdSetter) {
		final Long merchantId = getMerchantId();
		if (GlobalConfig.PLATFORM_MERCHANT_ID != (long) merchantId) {
			merchantIdSetter.accept(merchantId);
		}
		return merchantId;
	}

	/**
	 * 将本地时区的时间表示转为 GMT+8（系统默认时区） 的相同时间表示
	 * 例如：将 "2023-01-01 00:00 GMT+2" 转为 "GMT+8" 时区的 "2023-01-01 00:00 GMT+8" 表示
	 */
	public TimeInterval getIntervalToGMT8() {
		return getInterval(SystemInitializer.DEFAULT_TIME_ZONE);
	}

}
