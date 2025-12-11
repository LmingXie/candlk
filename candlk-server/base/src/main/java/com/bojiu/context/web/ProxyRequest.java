package com.bojiu.context.web;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.bojiu.common.context.Context;
import com.bojiu.common.context.RequestContext;
import com.bojiu.common.model.*;
import com.bojiu.common.redis.RedisUtil;
import com.bojiu.common.web.Client;
import com.bojiu.common.web.Page;
import com.bojiu.common.web.mvc.BaseProxyRequest;
import com.bojiu.context.auth.DefaultAutoLoginHandler;
import com.bojiu.context.auth.ExportInterceptor;
import com.bojiu.context.model.Currency;
import com.bojiu.context.model.*;
import com.bojiu.webapp.base.entity.Merchant;
import com.bojiu.webapp.base.util.Export;
import me.codeplayer.util.EasyDate;
import me.codeplayer.util.StringUtil;
import org.apache.commons.lang3.tuple.Pair;

public class ProxyRequest extends BaseProxyRequest {

	public final RequestContextImpl requestContext = RequestContextImpl.get();

	/**
	 * 获取当前session的用户对象<br>
	 * 如果用户未登录，则返回 null
	 */
	public <U extends Member> U getSessionUser() {
		return RequestContext.getSessionUser(request);
	}

	/**
	 * 获取当前session的用户对象<br>
	 * 如果用户未登录，则返回 null
	 */
	public <U extends Member> U user() {
		return RequestContext.getSessionUser(request);
	}

	/**
	 * 获取语言
	 */
	public Language getLanguage() {
		return RequestContextImpl.doGetLanguage(request);
	}

	@Override
	public Client getClient() {
		return requestContext.getClientInfo().client();
	}

	/**
	 * 获取国家（只对总台有效，非总台直接返回null）
	 */
	public Country getCountry() {
		if (!platform()) {
			return null;
		}
		return RequestContextImpl.doGetCountry(request);
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

	@Override
	public TimeInterval parseInterval(@Nullable String beginTime, @Nullable String endTime, boolean localized) {
		TimeZone from, to;
		if (localized) {
			if (MemberType.fromBackstage()) { // 后台不转换时区表示
				from = getTimeZone();
				to = null;
			} else {
				from = requestContext.getClientInfo().timeZone();
				to = getTimeZone();
			}
		} else {
			from = null;
			to = null;
		}
		TimeInterval interval = parseRelativeInterval(beginTime, endTime, from, to);
		if (interval != null) {
			return interval;
		}
		return super.parseInterval(beginTime, endTime, localized);
	}

	@Override
	protected TimeInterval newLocalizedInterval() {
		TimeInterval interval = new TimeInterval();
		interval.setTimeZone(getTimeZone());
		return interval;
	}

	/**
	 * @param required 是否必须传入时间范围参数（ begin 和 end 都不能为 null）
	 * @param max 限制传入最大时间范围的周期数量，如果为 <= 0 表示不限制
	 * @param calendarUnit 限制传入最大时间范围的周期单位
	 * @param defaultAmount 如果没有传入时间范围，则默认构造以 <code>最近 defaultAmount 个 defaultCalendarUnit</code> 的时间范围
	 * <br>（例如 <code>defaultAmount = 3，defaultCalendarUnit=Calendar.MONTH</code>） 表示 最近3个月
	 * <br>（例如 <code>defaultAmount = 1，defaultCalendarUnit=Calendar.DATE</code>） 表示 昨天
	 * @param defaultCalendarUnit 构造默认时间范围时的周期单位
	 */
	@Nonnull
	public TimeInterval getInterval(boolean required, int max, int calendarUnit, int defaultAmount, int defaultCalendarUnit) {
		TimeInterval t = getInterval();
		if (required && (t == null || t.getBegin() == null || t.getEnd() == null)) {
			throw new ErrorMessageException("请选择时间范围", false);
		}
		if (t == null) {
			this.setInterval(t = TimeInterval.ofBefore(defaultAmount, defaultCalendarUnit));
		} else if (max > 0 && !t.isWithin(max, calendarUnit)) {
			String errorMsg;
			if (max == 3 && calendarUnit == Calendar.MONTH) {
				errorMsg = "时间范围不能超过 3 个月";
			} else {
				final String unitLabel = switch (calendarUnit) {
					case Calendar.YEAR -> "年";
					case Calendar.MONTH -> "个月";
					case Calendar.DAY_OF_MONTH -> "天";
					case Calendar.HOUR_OF_DAY -> "小时";
					case Calendar.MINUTE -> "分钟";
					default -> throw new UnsupportedOperationException();
				};
				errorMsg = "时间范围不能超过 " + max + " " + unitLabel;
			}
			throw new ErrorMessageException(errorMsg, false);
		}
		return t;
	}

	@Nonnull
	public TimeInterval getIntervalOrDefault(int max, int calendarField, int defaultAmount, int defaultCalendarField) {
		return getInterval(isExport(), max, calendarField, defaultAmount, defaultCalendarField);
	}

	@Nonnull
	public TimeInterval getIntervalOrDefault(int max, int calendarField) {
		return getIntervalOrDefault(isExport(), max, calendarField);
	}

	@Nonnull
	public TimeInterval getIntervalOrDefault(boolean required, int max, int calendarField) {
		return getInterval(required, max, calendarField, max, calendarField);
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

	/**
	 * 获取请求头 App-Id
	 */
	public String getAppId() {
		return RequestContext.getAppId(request);
	}

	public Long getMerchantId() {
		return RequestContextImpl.getMerchantId(request);
	}

	public TimeZone getTimeZone() {
		return requestContext.getTimeZone();
	}

	/**
	 * 获取请求客户端名称
	 */
	public String getName() {
		return requestContext.getClientInfo().name();
	}

	/**
	 * 获取请求客户端版本号
	 */
	public String getVersion() {
		return requestContext.getClientInfo().version();
	}

	/**
	 * 获取请求渠道号
	 */
	public String getChannel() {
		return requestContext.getClientInfo().channel();
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

	public void setExportData(@Nullable List<?> list) {
		if (list != null) {
			request.setAttribute(Export.attrData, list);
		}
	}

	/** 设置导出所需的 数组集合数据 */
	public void setExportIndexedData(@Nullable List<Object[]> data, String... onlyLabels) {
		setExportAttrs(data, Boolean.TRUE, onlyLabels);
	}

	/** 设置导出所需的 数组集合数据 */
	public void setExportIndexedData(@Nullable List<Object[]> data) {
		setExportAttrs(data, Boolean.TRUE, (String[]) null);
	}

	public void setExportLabels(@Nullable Boolean labelOnly, String... labelOrPairs) {
		if (labelOnly != null) {
			request.setAttribute(Export.attrLabelOnly, labelOnly);
		}
		if (labelOrPairs != null) {
			request.setAttribute(Export.attrValues, labelOrPairs);
		}
	}

	public void setExportAttrs(List<?> data, @Nullable Boolean labelOnly, String... labelOrPairs) {
		setExportData(data);
		setExportLabels(labelOnly, labelOrPairs);
	}

	public void setExportAttrs(List<?> data, String... labelOrPairs) {
		setExportAttrs(data, null, labelOrPairs);
	}

	public void exportExcelAttrs(List<?> list, String i18nPrefix, String... titleOrPairs) {
		if (i18nPrefix != null) {
			request.setAttribute(Export.attrI18nPrefix, i18nPrefix);
		}
		if (titleOrPairs != null) {
			request.setAttribute(Export.attrValues, titleOrPairs);
		}
		if (list != null) {
			request.setAttribute(Export.attrData, list);
		}
	}

	public void exportPairs(String i18nPrefix, String pairs) {
		request.setAttribute(Export.attrI18nPrefix, i18nPrefix);
		exportPairs(pairs);
	}

	@Override
	public void attr(String name, Object o) {
		request.setAttribute(name, o);
	}

	public void exportPairs(String pairs) {
		request.setAttribute(Export.attrValue, pairs);
	}

	public boolean isExport() {
		return ExportInterceptor.isExport(request);
	}

	/**
	 * 请注意该方法可能返回【不可变】的 Page 实例
	 */
	@Override
	@Nonnull
	public <T> Page<T> getPage() {
		return getPage(false);
	}

	/**
	 * 获取当前请求的数据分页参数对象
	 *
	 * @param allowExport 是否允许导出。默认不允许导出，将会限制最大返回 100条 数据；如果为 true，则允许导出，将会返回所有数据
	 * @param allowQueryTotal 是否允许查询总数（ COUNT(*) ）。默认允许查询总数，如果为 false，将不会查询总记录数
	 */
	@Nonnull
	public <T> Page<T> getPage(final boolean allowExport, final boolean allowQueryTotal) {
		if (page == null) {
			page = doInitPage(null);
		}
		if ((allowExport || MemberType.fromBackstage()) && isExport()) {
			page.setSize(Page.SIZE_SKIP_LIMIT);
		}
		if (!allowQueryTotal) {
			page.setSearchCount(false);
		}
		return preHandlePage(page);
	}

	/**
	 * 获取当前请求的数据分页参数对象
	 */
	@Nonnull
	public <T> Page<T> getPageSkipTotal() {
		return getPage(false, false);
	}

	/**
	 * 获取当前请求的数据分页参数对象
	 */
	@Nonnull
	public <T> Page<T> getPage(boolean allowExportAlready) {
		return getPage(allowExportAlready, getClient() == Client.PC || MemberType.fromBackstage());
	}

	/**
	 * 获取当前请求的数据分页参数对象
	 */
	public <T> Page<T> getPageWithTotalWhenPC() {
		return getPage(false, getClient() == Client.PC);
	}

	public void logout() {
		Member member = getSessionUser();
		if (member != null) {
			final Long memberId = member.getId();
			RedisUtil.doInLock(DefaultAutoLoginHandler.concurrentLoginLockKey(memberId), 10_000, () -> {
				final String redisKey = DefaultAutoLoginHandler.concurrentSessionUserKey(memberId);
				// 避免退出登录时，还存在并发请求执行了自动登录，导致又自动登录成功
				RedisUtil.opsForValue().set(redisKey, "", 10, TimeUnit.SECONDS);
				removeSessionUser();
			});
		}
		SessionCookieUtil.clearCookies(request, response);
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
	public void assertCanAccess(Long merchantId) {
		getSessionUser().assertCanAccess(merchantId);
	}

	/**
	 * 检查指定商户ID的用户是否有权访问指定商户归属的对象（平台可以访问商户）
	 */
	public boolean canAccess(Long merchantId) {
		return getSessionUser().canAccess(merchantId);
	}

	/** 当前请求来自平台员工 */
	public boolean platform() {
		return (long) Merchant.PLATFORM_ID == getMerchantId();
	}

	public Long applyMerchantId(Consumer<Long> merchantIdSetter) {
		final Long merchantId = getMerchantId();
		if ((long) Merchant.PLATFORM_ID != merchantId) {
			merchantIdSetter.accept(merchantId);
		}
		return merchantId;
	}

	public Long checkMerchantId(@Nullable Long merchantId) {
		final Long selfMerchantId = getMerchantId();
		return Merchant.isPlatform(selfMerchantId) ? merchantId : selfMerchantId;
	}

	/**
	 * 获取当前会话用户ID
	 */
	public Long userId() {
		return Bean.idOf(user());
	}

	/**
	 * 根据请求头传入的国家返回国家列表和货币
	 */
	public Pair<Country[], Currency> getCountryCurrency() {
		final Country[] countries = getCountries();
		return Pair.of(countries, countries == Country.PLATFORM_SWITCH ? null : countries[0].currency);
	}

	/**
	 * 获取国家列表
	 */
	public Country[] getCountries() {
		final Country country = getCountry();
		return country == null ? Country.PLATFORM_SWITCH : new Country[] { country };
	}

	/**
	 * 根据请求头国家获取货币(只限于总台获取)
	 */
	public Currency getCurrency() {
		final Country country = getCountry();
		return country == null ? null : country.currency;
	}

	@Nullable
	public static int[] parseRelative(@Nullable String source) {
		final int length;
		if (source == null || (length = source.length()) < 3 /* "+1d" */ || length > 6 /* "+9999d" */) {
			return null;
		}
		char sign = source.charAt(0);
		if (sign != '+' && sign != '-') {
			return null;
		}
		final int calendarField;
		switch (source.charAt(length - 1)) {
			case 'd' -> calendarField = Calendar.DATE;
			case 'w' -> calendarField = Calendar.DAY_OF_WEEK;
			case 'm' -> calendarField = Calendar.MONTH;
			case 'y' -> calendarField = Calendar.YEAR;
			default -> {
				return null;
			}
		}
		int val = Integer.parseInt(source, 0, length - 1, 10);
		return new int[] { val, calendarField };
	}

	public static Date parseRelativeTime(@Nullable String source, EasyDate baseTime, boolean beginOrEnd) {
		final int[] meta = parseRelative(source);
		if (meta == null) {
			return null;
		}
		final long current = baseTime.getTime();
		applyWith(baseTime, meta);
		Date date = beginOrEnd ? baseTime.beginOf(meta[1]).toDate() : baseTime.endOf(meta[1]).toDate();
		baseTime.setTime(current); // reset
		return date;
	}

	@Nullable
	public static TimeInterval parseRelativeInterval(@Nullable String beginStr, @Nullable String endStr, @Nullable TimeZone fromTimeZone, @Nullable TimeZone toTimeZone) {
		final int[] beginMeta = parseRelative(beginStr);
		if (beginMeta == null) {
			return null;
		}
		final int[] endMeta = parseRelative(endStr);
		if (endMeta == null) {
			return null;
		}
		final long nowInMs = System.currentTimeMillis();
		final EasyDate d = fromTimeZone == null ? new EasyDate(nowInMs) : new EasyDate(nowInMs, fromTimeZone);
		applyWith(d, beginMeta);
		Date begin = d.beginOf(beginMeta[1]).toDate(), end;
		if (beginMeta[0] == endMeta[0] && beginMeta[1] == endMeta[1]) {
			end = d.endOf(beginMeta[1]).toDate();
		} else {
			d.setTime(nowInMs);
			applyWith(d, endMeta);
			end = d.endOf(endMeta[1]).toDate();
		}
		if (toTimeZone != null && fromTimeZone != null) {
			final int diff = fromTimeZone.getRawOffset() - toTimeZone.getRawOffset();
			if (diff != 0) {
				begin.setTime(begin.getTime() + diff);
				end.setTime(end.getTime() + diff);
				d.setTimeZone(toTimeZone);
			}
		}
		TimeInterval interval = new TimeInterval(begin, end, -1, -1);
		return interval.setEasyDate(d);
	}

	static void applyWith(EasyDate d, final int[] meta) {
		if (meta[0] != 0) {
			if (meta[1] == Calendar.DAY_OF_WEEK) {
				d.addDay(meta[0] * 7);
			} else {
				d.getCalendar().add(meta[1], meta[0]);
			}
		}
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected <T> Page<T> preHandlePage(Page page) {
		if (MemberType.fromBackstage() /* 仅限后台 */
				&& page.getList() == Collections.emptyList() /* 没有设置过 */
				&& page.getTotal() == 0L
				&& page.getSize() >= 0L && page.searchCount() /* 非导出 */
				&& page.getCurrent() != 6 /* 留一个口子，如果是 第6页 时，就不使用缓存 */) {
			final long totalCount = ResponseDataHandler.getCachedTotalCount(request);
			if (totalCount > 0) { // 采用缓存的数据总数
				page.setTotal(totalCount);
				page.setSearchCount(false);
			}
			page.setList(List.of()); // 设置一个空集合，避免重复调用此方法时触发重复判断
		}
		return page;
	}

	/**
	 * 标记当前接口是否允许缓存当前分页数据的 总记录数（ 即 COUNT(*) ）
	 * <p>
	 * 默认情况下，当分页的总记录数超过 10000 时，将会被自动缓存
	 */
	public void setAllowCacheTotal(Boolean allow) {
		ResponseDataHandler.setAllowCacheTotal(request, allow);
	}

}