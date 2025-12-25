package com.bojiu.webapp.base.entity;

import java.util.*;

import com.bojiu.common.model.BizFlag;
import com.bojiu.context.model.Currency;
import com.bojiu.webapp.base.dto.MerchantContext;
import lombok.Getter;
import lombok.Setter;
import me.codeplayer.util.CollectionUtil;
import me.codeplayer.util.StringUtil;
import org.jspecify.annotations.Nullable;

/**
 * 商户表
 *
 * @author LeeYd
 * @since 2023-08-31
 */
@Setter
@Getter
public class Merchant extends BizEntity implements BizFlag.WritableBizFlag {

	/** 为 0 的商户ID，则表示平台 */
	public static final Long PLATFORM_ID = 0L;

	public static boolean isPlatform(Long merchantId) {
		return merchantId != null && merchantId == (long) PLATFORM_ID;
	}

	/** 商户数据已被清理（归档+删除） */
	public static final long BIZ_FLAG_DELETED = 1L << 10;

	/**
	 * 站点名称 <br/>
	 * 历史:merchant.name在主站点时存的是商户名，而子站点存的是站点名<br/>
	 * 现统一改为站点名，商户名取 group.name，取商户名使用: {@link  this#getGroupName } <br/>
	 */
	String name;
	/** 站点名称 */
	String siteName;
	/** 货币 */
	Currency currency;
	/** 语言 */
	String languages;
	/** 运营地区编码 */
	String countryCodes;
	/** 商户等级 */
	Integer level;
	/** 游戏厂商数 */
	Integer vendorNum;
	/** 业务标识 */
	long bizFlag;
	/**
	 * 风控状态
	 *
	 * @see com.bojiu.context.model.RiskStatus
	 */
	Integer riskStatus;
	/** PGC抽成模式：1=损益模式；2=打码模式 */
	Integer commissionMode;
	/** 集团ID */
	Long groupId;
	/** 备注 */
	String remark;
	/** 导入指定商户配置ID */
	Long orgId;
	/** 商户配置标识 see {@link SiteConfigBitmask#SITE_BASE_ME} */
	long configFlag;
	/** 关闭时间 */
	Date closeTime;
	/** 品牌ID */
	Long brandId;
	/** ip配置ID */
	Long ipConfigId;

	/**
	 * 获取集团商户名称（注意：{@link #name} 是站点名称 ）
	 */
	transient String groupName;

	public Long getMerchantId() {
		return id;
	}

	public static Map<String, Object> asEmbed(MerchantContext t) {
		return t == null ? null : CollectionUtil.asHashMap(
				"id", t.getId(),
				"name", t.getName()
		);
	}

	public static Map<String, Object> asEmbed(Merchant t) {
		return t == null ? null : CollectionUtil.asHashMap(
				"id", t.getId(),
				"name", t.getName()
		);
	}

	public static Map<String, Object> asEmbed(@Nullable final Map<Long, MerchantContext> contextMap, Long merchantId) {
		if (contextMap == null) {
			return null;
		}
		MerchantContext context = contextMap.get(merchantId);
		return context == null ? Collections.emptyMap() : asEmbed(context);
	}

	public boolean isMerchant() {
		return id.equals(groupId);
	}

	public void addConfigFlag(SiteConfigBitmask configFlag) {
		this.setConfigFlag(this.getConfigFlag() | configFlag.getBizFlag());
	}

	/**
	 * 获取配置标识,
	 *
	 * @param mapSize 便于外面追加，appLayout里的配置标识时而不用再扩容
	 */
	public HashMap<String, Boolean> getConfigFlagMap(Integer mapSize) {
		SiteConfigBitmask[] values = SiteConfigBitmask.values();
		HashMap<String, Boolean> map = mapSize == null ? new HashMap<>(values.length, 1f) : new HashMap<>(mapSize, 1f);
		for (SiteConfigBitmask value : values) {
			map.put(value.getConfigKey(), (this.configFlag & value.getBizFlag()) != 0);
		}
		return map;
	}

	public String getGroupName() {
		if (StringUtil.isEmpty(groupName)) {
			return groupName = MerchantContext.get(this.groupId).getGroupName();
		}
		return groupName;
	}

	public static final String NAME = "name";
	public static final String CURRENCY = "currency";
	public static final String SITE_NAME = "site_name";
	public static final String LANGUAGES = "languages";
	public static final String COUNTRY_CODES = "country_codes";
	public static final String LEVEL = "level";
	public static final String VENDOR_NUM = "vendor_num";
	public static final String BIZ_FLAG = "biz_flag";
	public static final String RISK_STATUS = "risk_status";
	public static final String COMMISSION_MODE = "commission_mode";
	public static final String GROUP_ID = "group_id";
	public static final String REMARK = "remark";
	public static final String ORG_ID = "org_id";
	public static final String CONFIG_FLAG = "config_flag";
	public static final String CLOSE_TIME = "close_time";
	public static final String BRAND_ID = "brand_id";
	public static final String IP_CONFIG_ID = "ip_config_id";

}