package com.bojiu.webapp.base.service;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bojiu.context.ContextImpl;
import com.bojiu.context.SystemInitializer;
import com.bojiu.context.model.RiskStatus;
import com.bojiu.context.model.SiteStatus;
import com.bojiu.webapp.base.dao.MerchantContextDao;
import com.bojiu.webapp.base.dto.MerchantContext;
import com.bojiu.webapp.base.entity.Merchant;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.codeplayer.util.CollectionUtil;
import me.codeplayer.util.EasyDate;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商户 服务实现类
 *
 * @author wensl
 * @since 2023-09-07
 */
@Service
public class MerchantContextService extends BaseServiceImpl<Merchant, MerchantContextDao, Long> implements InitializingBean, CacheSyncService {

	static final Cache<Long, MerchantContext> cache = Caffeine.newBuilder()
			.initialCapacity(4)
			.maximumSize(1024)
			.expireAfterAccess(1, TimeUnit.HOURS)
			.build();

	private static MerchantContextService instance;

	public static MerchantContext getCached(@NonNull Long merchantId, boolean flush) {
		if (flush) {
			MerchantContext context = instance.getContext(merchantId);
			cache.put(merchantId, context);
			return context;
		}
		return cache.get(merchantId, k -> instance.getContext(k));
	}

	public static MerchantContext getCached(@NonNull Long merchantId) {
		return getCached(merchantId, false);
	}

	public static Map<Long, MerchantContext> findCached(Collection<Long> merchantIds) {
		return cache.getAll(merchantIds, ids -> {
			var wrapper = new QueryWrapper<Merchant>()
					.in("m." + Merchant.ID, ids);
			List<MerchantContext> contexts = instance.baseDao.findContexts(wrapper);
			return CollectionUtil.toHashMap(contexts, MerchantContext::getId);
		});
	}

	/** 预加载全部【有效的】商户上下文 */
	public static void preloadAllCache() {
		final String prefix = "m.";
		// 只查询站点状态为【正常+初始化中】 或者 最后更新时间 在1天内的商户数据
		final List<MerchantContext> contexts = instance.baseDao.findContexts(new QueryWrapper<>()
				.ge(prefix + Merchant.STATUS, SiteStatus.INIT.value)
				.or()
				.ge(prefix + Merchant.UPDATE_TIME, new Date(System.currentTimeMillis() - EasyDate.MILLIS_OF_DAY)));
		HashMap<Long, MerchantContext> merchantMap = CollectionUtil.toHashMap(contexts, MerchantContext::getId);
		cache.putAll(merchantMap);
	}

	protected MerchantContext getContext(Long merchantId) {
		return baseDao.getContext(merchantId);
	}

	@Transactional
	public int updateRiskStatus(RiskStatus riskStatus, Long id) {
		return baseDao.updateRiskStatus(riskStatus.value, id);
	}

	@Override
	public String getCacheId() {
		return RemoteSyncService.MerchantContextService;
	}

	@Override
	public void flushCache(Object... args) {
		RemoteSyncService.clearCacheByKey(cache, args);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		ContextImpl.merchantId2TimeZoneMapper = merchantId -> {
			if (Merchant.isPlatform(merchantId)) {
				return SystemInitializer.DEFAULT_TIME_ZONE;
			}
			MerchantContext context = getCached(merchantId);
			return context == null ? SystemInitializer.DEFAULT_TIME_ZONE : context.country().getTimeZone();
		};
		instance = this;
	}

	/**
	 * 统计商户下的用户数量(建设中商户低频触发该方法)
	 */
	public static Long countMerchantUser(Long merchantId) {
		return instance.baseDao.countMerchantUser(merchantId);
	}

}