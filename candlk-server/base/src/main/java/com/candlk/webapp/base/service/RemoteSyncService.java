package com.candlk.webapp.base.service;

import javax.annotation.Nonnull;

import com.github.benmanes.caffeine.cache.Cache;
import me.codeplayer.util.X;
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

	/* ======= user ====== */

	String BannerService = "BannerService";

	/* ======= trade ====== */

	/* ======= game ====== */
	String MerchantGameService = "MerchantGameService";
	String MerchantLevelShareService = "MerchantLevelShareService";

	/* ======= admin ====== */

	/** 可选参数 （ Long(角色ID) ） */
	String RoleMenuService = "RoleMenuService";
	String AdminMenuService = "AdminMenuService";

	/**
	 * 刷新缓存
	 */
	void flushCache(@Nonnull String cacheId, Object... args);

	@SuppressWarnings("unchecked")
	static <T> void clearCacheForMerchant(final Cache<T, ?> cache, Object... args) {
		if (X.isValid(args)) {
			for (Object partKey : args) {
				cache.invalidate((T) partKey);
			}
		} else {
			cache.invalidateAll();
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
