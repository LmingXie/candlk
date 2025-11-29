package com.bojiu.webapp.user.service;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

import com.bojiu.common.util.SpringUtil;
import com.bojiu.webapp.base.service.RemoteSyncService;
import com.bojiu.webapp.user.entity.Meta;
import com.bojiu.webapp.user.model.MetaType;
import me.codeplayer.util.Assert;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static com.bojiu.webapp.base.service.RemoteSyncService.*;

@Service
public class GlobalCacheSyncService implements InitializingBean {

	static GlobalCacheSyncService instance;

	RemoteSyncServiceProxy all;

	RemoteSyncServiceProxy user, game, trade, admin;

	@PostConstruct
	public void init() {
		// TODO: 2025/11/29  这是单服务本地缓存的临时解决方案
		// 当前服务一定是user，这里先不考虑分布式多服务的情况
		RemoteSyncService user = SpringUtil.getBean("remoteCacheSyncService");
		this.user = new RemoteSyncServiceProxy(user);
	}
	// @DubboReference(group = ALL, cluster = ClusterRules.BROADCAST, merger = "true")
	// public void setAll(RemoteSyncService all) {
	// 	this.all = new RemoteSyncServiceProxy(all);
	// }
	// /**
	//  * 向 USER 分组进行广播，因此所有 {@link CacheSyncService}实例都将会收到通知，并执行调用的方法
	//  * <p>注入：{@link com.bojiu.context.config.GlobalConfig.RemoteSyncServiceConfig#userRemoteCacheSyncService}</p>
	//  */
	// @DubboReference(group = USER, cluster = ClusterRules.BROADCAST)
	// public void setUser(RemoteSyncService user) {
	// 	this.user = new RemoteSyncServiceProxy(user);
	// }
	// @DubboReference(group = GAME, cluster = ClusterRules.BROADCAST)
	// public void setGame(RemoteSyncService game) {
	// 	this.game = new RemoteSyncServiceProxy(game);
	// }
	//
	// @DubboReference(group = TRADE, cluster = ClusterRules.BROADCAST)
	// public void setTrade(RemoteSyncService trade) {
	// 	this.trade = new RemoteSyncServiceProxy(trade);
	// }
	//
	// @DubboReference(group = ADMIN, cluster = ClusterRules.BROADCAST)
	// public void setAdmin(RemoteSyncService admin) {
	// 	this.admin = new RemoteSyncServiceProxy(admin);
	// }
	//
	// public static RemoteSyncServiceProxy all() {
	// 	return instance.all;
	// }

	public static RemoteSyncServiceProxy user() {
		return instance.user;
	}
	//
	// public static RemoteSyncServiceProxy game() {
	// 	return instance.game;
	// }
	//
	// public static RemoteSyncServiceProxy trade() {
	// 	return instance.trade;
	// }
	//
	// public static RemoteSyncServiceProxy admin() {
	// 	return instance.admin;
	// }

	public static void flushAll(boolean afterCommit, @Nonnull String cacheId, Object... args) {
		instance.all.flushCache(afterCommit, cacheId, args);
	}

	public static void flushAll(@Nonnull String cacheId, Object... args) {
		instance.all.flushCache(TransactionSynchronizationManager.isSynchronizationActive(), cacheId, args);
	}

	/**
	 * @param afterCommit 是否在事务提交后才执行
	 * @param merchantId null=所有商户（只适用于type不为 null 时）；单个商户ID（Long）；多个商户ID的 Set< String > 或 Set< Long >
	 */
	public static void flushMetaCache(boolean afterCommit, Object merchantId, @Nonnull MetaType type) {
		String[] services = type.cacheInServices;
		Assert.isTrue(services.length > 0); // 如果没有声明，就不要调用本方法
		for (String service : services) {
			switch (service) {
				case ALL -> instance.all.flushMetaCache(afterCommit, merchantId, type.name());
				case USER -> instance.user.flushMetaCache(afterCommit, merchantId, type.name());
				case TRADE -> instance.trade.flushMetaCache(afterCommit, merchantId, type.name());
				case GAME -> instance.game.flushMetaCache(afterCommit, merchantId, type.name());
				case ADMIN -> instance.admin.flushMetaCache(afterCommit, merchantId, type.name());
			}
		}
	}

	/**
	 * @param merchantId null=所有商户（只适用于type不为 null 时）；单个商户ID（Long）；多个商户ID的 Set< String > 或 Set< Long >
	 */
	public static void flushMetaCache(Object merchantId, @Nonnull MetaType type) {
		flushMetaCache(TransactionSynchronizationManager.isSynchronizationActive(), merchantId, type);
	}

	/**
	 * @param merchantId null=所有商户（只适用于type不为 null 时）；单个商户ID（Long）；多个商户ID的 Set< String > 或 Set< Long >
	 */
	public static void flushMetaCacheAfterCommit(Object merchantId, @Nonnull MetaType type) {
		flushMetaCache(true, merchantId, type);
	}

	public static void refreshCache(Meta meta) {
		flushMetaCache(meta.getMerchantId(), meta.getType());
	}

	public static void refreshCacheAfterCommit(Meta meta) {
		flushMetaCacheAfterCommit(meta.getMerchantId(), meta.getType());
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		instance = this;
	}

	public static class RemoteSyncServiceProxy implements RemoteSyncService {

		final RemoteSyncService delegate;

		private RemoteSyncServiceProxy(RemoteSyncService delegate) {
			this.delegate = delegate;
		}

		@Override
		public void flushCache(@Nonnull String cacheId, Object... args) {
			flushCache(TransactionSynchronizationManager.isSynchronizationActive(), cacheId, args);
		}

		@Override
		public void flushCaches(@Nonnull String[] cacheIds, Object... args) {
			flushCaches(TransactionSynchronizationManager.isSynchronizationActive(), cacheIds, args);
		}

		public void flushCache(boolean afterCommit, @Nonnull String cacheId, Object... args) {
			if (afterCommit) {
				SpringUtil.runAfterCommit(() -> delegate.flushCache(cacheId, args));
				return;
			}
			delegate.flushCache(cacheId, args);
		}

		public void flushCaches(boolean afterCommit, @Nonnull String[] cacheIds, Object... args) {
			if (afterCommit) {
				SpringUtil.runAfterCommit(() -> delegate.flushCaches(cacheIds, args));
				return;
			}
			delegate.flushCaches(cacheIds, args);
		}

		/**
		 * 刷新【元数据】缓存
		 */
		public void flushMetaCache(boolean afterCommit, Object merchantId, String type) {
			flushCache(afterCommit, MetaService, merchantId, type);
		}

		protected static RemoteSyncService wrapFor(RemoteSyncService service) {
			return service instanceof RemoteSyncServiceProxy t ? t : new RemoteSyncServiceProxy(service);
		}

	}

}