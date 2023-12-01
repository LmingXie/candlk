package com.candlk.webapp.base.service;

import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.candlk.common.util.SpringUtil;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.CollectionUtil;
import me.codeplayer.util.X;

@Slf4j
public class RemoteSyncServiceImpl implements RemoteSyncService {

	protected final Map<String, CacheSyncService> cacheSyncServiceMap;

	public RemoteSyncServiceImpl(@Nullable List<CacheSyncService> services) {
		cacheSyncServiceMap = X.isValid(services) ? CollectionUtil.toHashMap(services, CacheSyncService::getCacheId) : null;
	}

	@Override
	public void flushCache(@Nonnull String cacheId, Object... args) {
		// 为了保证代码的严谨性，后台操作时，此处直接报错会好一些
		final CacheSyncService service = cacheSyncServiceMap.get(cacheId);
		if (service != null) {
			log.info("准备刷新缓存：" + cacheId + "……");
			service.flushCache(args);
			if (cacheId.startsWith(PREFIX_TO_PUBLISH_EVENT)) { // 以 @ 开头 的 cacheId 支持发布本地事件
				SpringUtil.publishEvent(new CacheFlushEvent(cacheId, args));
			}
		} else {
			log.info("准备刷新缓存：" + cacheId);
		}

	}

}
