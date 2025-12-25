package com.bojiu.webapp.base.service;

import java.util.Arrays;
import java.util.List;

import com.github.benmanes.caffeine.cache.Cache;
import me.codeplayer.util.*;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEvent;

/**
 * 远程同步服务（一般用于缓存同步）
 */
public interface RemoteSyncService {

	/** cacheId 的值如果有此前缀，就会自动发布本地 Spring 事件 {@link CacheFlushEvent } */
	String PREFIX_TO_PUBLISH_EVENT = "@";

	String ALL = "*";
	String USER = "user";
	String TRADE = "trade";
	String GAME = "game";
	String ADMIN = "admin";

	/* ======= 全局 ====== */

	/** 可选参数 ( Long(商户ID), String( MetaType.name() ) ) */
	String MetaService = "MetaService";
	String MerchantContextService = "MerchantContextService";
	String PromotionService = "PromotionService";
	String RebateConfigService = "RebateConfigService";
	String SiteStatusChecker = "SiteStatusChecker";
	String UserTaskConfigService = "UserTaskConfigService";
	String MerchantLayerConfigService = "MerchantLayerConfigService";
	String MerchantGroupService = "MerchantGroupService";
	String FeedbackConfigService = "FeedbackConfigService";
	/** 用于域名缓存刷新 */
	String ContextImpl = "ContextImpl";
	String TicketGrantService = "TicketGrantService";
	/* ======= user ====== */

	String BannerService = "BannerService";
	String OauthUserService = "OauthUserService";
	String RegionLimitService = "RegionLimitService";
	String UserPromotionService = "UserPromotionService";
	String MarketingDomainService = "MarketingDomainService";
	String AgentModelService = "AgentModelService";
	String PromotionGroupTaskMember = "PromotionGroupTaskMember";
	String UserFaqService = "UserFaqService";
	String UserVendorService = "UserVendorService";
	String FindUsConfigService = "FindUsConfigService";
	String MediaManagementService = "MediaManagementService";
	String QuickShareConfigService = "QuickShareConfigService";
	String UserModuleManagementService = "UserModuleManagementService";
	String RegisterPopService = "RegisterPopService";
	String AppStoreConfigService = "AppStoreConfigService";
	String AppDownloadService = "AppDownloadService";
	String BrandJackpotService = "BrandJackpotService";
	String UserMerchantVendorService = "UserMerchantVendorService";
	String SiteContentService = "SiteContentService";
	String UserAvatarService = "UserAvatarService";

	/* ======= trade ====== */
	String SnsItemService = "SnsItemService";

	/* ======= game ====== */
	String MerchantGameService = "MerchantGameService";
	String PromotionAuditService = "PromotionAuditService";
	String UserCtrlRuleService = "UserCtrlRuleService";
	String UserGameCoinService = "UserGameCoinService";
	String VendorService = "VendorService";
	String MerchantVendorService = "MerchantVendorService";
	/* ======= admin ====== */

	/** 可选参数 （ Long(角色ID) ） */
	String RoleMenuService = "RoleMenuService";
	String AdminMenuService = "AdminMenuService";
	String MerchantShareConfigService = "MerchantShareConfigService";
	String MerchantWhitelistCheckFilter = "MerchantWhitelistCheckFilter";
	String EmpService = "EmpService";

	/** 游戏 gid 映射 */
	String GameService = "GameService";
	String AgentUserMapAdminService = "AgentUserMapAdminService";
	String MsgMarqueeService = "MsgMarqueeService";
	String BrandService = "BrandService";
	String PromotionTag = "PromotionTag";

	String AdminModuleManagementService = "AdminModuleManagementService";
	String AdminModuleContentService = "AdminModuleContentService";

	/**
	 * 刷新缓存
	 */
	void flushCache(@NonNull String cacheId, Object... args);

	/**
	 * 刷新同参数的多个缓存
	 */
	void flushCaches(@NonNull String[] cacheIds, Object... args);

	/**
	 * 刷新【元数据】缓存
	 */
	default void flushMetaCache(Object merchantId, String type) {
		flushCache(MetaService, merchantId, type);
	}

	/**
	 * 清除以 商户ID 作为 Key 的缓存工具方法
	 *
	 * @param args 不传表示全部清空；传入1个，只清空指定；传入数组，则清空指定多个
	 */
	@SuppressWarnings("unchecked")
	static <T> void clearCacheByKey(final Cache<T, ?> cache, Object... args) {
		switch (X.size(args)) {
			case 0 -> cache.invalidateAll();
			case 1 -> cache.invalidate((T) args[0]);
			default -> {
				List<T> keys = X.castType(Arrays.asList(args));
				cache.invalidateAll(keys);
			}
		}
	}

	static void clearCacheByIds(final Cache<Long, ?> cache, Object... ids) {
		switch (X.size(ids)) {
			case 0 -> cache.invalidateAll();
			case 1 -> cache.invalidate(NumberUtil.getLong(ids[0]));
			default -> {
				List<Long> keys = ArrayUtil.toList(NumberUtil::getLong, ids);
				cache.invalidateAll(keys);
			}
		}
	}

	class CacheFlushEvent extends ApplicationEvent {

		public Object[] args;

		public CacheFlushEvent(String cacheId, Object... args) {
			super(cacheId);
			this.args = args;
		}

	}

}