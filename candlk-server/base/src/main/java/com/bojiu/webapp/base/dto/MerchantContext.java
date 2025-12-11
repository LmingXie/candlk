package com.bojiu.webapp.base.dto;

import java.util.*;
import javax.annotation.Nonnull;

import com.bojiu.common.context.I18N;
import com.bojiu.common.model.ErrorMessageException;
import com.bojiu.common.util.Common;
import com.bojiu.context.i18n.UserI18nKey;
import com.bojiu.context.model.Currency;
import com.bojiu.context.model.*;
import com.bojiu.webapp.base.service.MerchantContextService;
import lombok.Getter;
import lombok.Setter;
import me.codeplayer.util.EasyDate;

/**
 * 商户
 */
@Getter
@Setter
public class MerchantContext {

	/** 商户ID */
	Long id;
	/** 服务器厂家 */
	Integer serverVendor;
	/** 短信供应商 */
	String smsVendor;
	/** 商户等级 */
	Integer level;
	/**
	 * 站点名称 <br/>
	 * 历史:merchant.name在主站点时存的是商户名，而子站点存的是站点名<br/>
	 * 现统一改为站点名，商户名取group.name,取商户名使用: {@link  this#getMerchantName } <br/>
	 */
	String name;
	/** 集团名称 */
	String groupName;
	/** 代理模式：1=打码；2=盈亏 */
	Integer agentMode;
	/** 经销商模式：1=损益金额；2=充值金额；3=首充人数 */
	Integer dealerMode;
	/** 站点状态 */
	SiteStatus status;
	/** 风控状态 */
	RiskStatus riskStatus;
	/** 集团状态(商户状态) */
	SiteStatus groupStatus;
	/** 集团ID */
	Long groupId;
	/** PGC抽成模式：1=损益模式；2=打码模式 */
	Integer commissionMode;
	/** 品牌ID */
	Long brandId;
	/** ip配置ID */
	Long ipConfigId;

	/** 国家 */
	private transient Country country;
	/** 货币 */
	private transient Currency currency;
	/** 语言 */
	private transient Language[] languages;

	public String levelStr() {
		// 这里的目的主要是避免 toString() 会 new 出新的字符串
		return Common.toString(level);
	}

	public void setCountryCodes(String countryCodes) {
		this.country = Country.of(countryCodes); // 目前只有一个国家
	}

	public void setLanguages(String languages) {
		this.languages = Language.parse(languages, null);
	}

	public void setCurrency(String currency) {
		this.currency = Currency.of(currency);
	}

	public boolean isMerchant() {
		return id.equals(groupId);
	}

	public static MerchantContext get(@Nonnull Long merchantId) {
		return MerchantContextService.getCached(merchantId);
	}

	/**
	 * 检查商户状态
	 *
	 * @param forAuth 是否是认证(非注册)
	 */
	public static MerchantContext checkStatus(@Nonnull Long merchantId, boolean forAuth) throws ErrorMessageException {
		MerchantContext context = MerchantContextService.getCached(merchantId);
		if (context == null || context.groupStatus.value.compareTo(SiteStatus.INIT.value) < 0 || context.status.value.compareTo(SiteStatus.INIT.value) < 0) {
			throw new ErrorMessageException(I18N.msg(UserI18nKey.SITE_ABNORMAL), MessagerStatus.ABNORMAL, false);
		}
		// 商户状态为建设中，只能注册最多3个用户
		if (!forAuth && context.groupStatus == SiteStatus.INIT && MerchantContextService.countMerchantUser(context.groupId) >= 3) {
			throw new ErrorMessageException(I18N.msg(UserI18nKey.MERCHANT_INIT_OVER_USER_NUM), MessagerStatus.INIT, false);
		}
		return context;
	}

	/**
	 * 检查商户状态(默认非注册)
	 */
	public static MerchantContext checkStatus(@Nonnull Long merchantId) throws ErrorMessageException {
		return checkStatus(merchantId, true);
	}

	public static Map<Long, MerchantContext> findByIds(Collection<Long> merchantIds) {
		return MerchantContextService.findCached(merchantIds);
	}

	public Country country() {
		return country;
	}

	public TimeZone getTimeZone() {
		return country.timeZone;
	}

	/** 基于商户当地时区初始化指定（毫秒级）时间戳 */
	public EasyDate zonedDateFor(long baseTime) {
		return country.newEasyDate(baseTime);
	}

	/**
	 * 基于商户当地时区初始化指定（毫秒级）时间戳。
	 * <p> 如果指定时间戳是 {@code "2023-05-02 12:00:00 GMT+8"}，商户所在国家时区是 GMT-3，则返回 {@code "2023-05-02 01:00:00 GMT-3" } 时间对象
	 */
	public EasyDate zonedDateFor(Date base) {
		return zonedDateFor(base.getTime());
	}

	/**
	 * 获取基于商户当地时区的当前时间对象。
	 * <p> 如果当前系统时间是 {@code "2023-05-02 12:00:00 GMT+8"}，商户所在国家时区是 GMT-3，则返回 {@code "2023-05-02 01:00:00 GMT-3" } 时间对象
	 */
	public EasyDate zonedDateNow() {
		return zonedDateFor(System.currentTimeMillis());
	}

	/**
	 * 获取基于商户当地时区的当前时间对象。
	 * <p> 如果当前系统时间是 {@code "2023-05-02 12:00:00 GMT+8"}，商户所在国家时区是 GMT-3，则返回与 {@code "2023-05-02 01:00:00 GMT+8" } 对应的 {@code "2023-05-01 14:00:00 GMT-3" } 时间对象
	 */
	public EasyDate zonedLocalDateNow() {
		return zonedDateFor(country.toLocalTime(System.currentTimeMillis()));
	}

	/**
	 * 将 商户当地时间 在【系统时间（GMT+8）下的相同输出表示】转为实际的 GMT+8 时间。
	 * <p> 如果传入的系统时间表示是 {@code "2023-05-02 01:00:00 GMT+8"}，商户所在国家时区是 GMT-3，则表示的实际时间为 {@code "2023-05-02 01:00:00 GMT-3"}，
	 * 此时将返回与之对应的 {@code "2023-05-02 12:00:00 GMT+8" } 系统时间对象
	 */
	public EasyDate countryLocalNow() {
		return new EasyDate(MerchantContext.toLocalTime(id, System.currentTimeMillis()));
	}

	public Date toGmt8Date(final Date localTime) {
		return country.toGmt8Date(localTime);
	}

	/**
	 * @see Country#toLocalTime(Date, TimeZone)
	 */
	public static Date toLocalTime(Long merchantId, Date baseTime) {
		return get(merchantId).country.toLocalTime(baseTime);
	}

	/**
	 * @see Country#toLocalTime(long, TimeZone)
	 */
	public static long toLocalTime(Long merchantId, long baseTime) {
		return get(merchantId).country.toLocalTime(baseTime);
	}

	public String getMerchantName() {
		return this.getGroupName();
	}

}