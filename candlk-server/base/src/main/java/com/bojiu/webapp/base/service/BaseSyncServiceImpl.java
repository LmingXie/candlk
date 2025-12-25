package com.bojiu.webapp.base.service;

import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.CollectionUtil;
import me.codeplayer.util.X;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class BaseSyncServiceImpl {

	protected Map<String, CacheSyncService> cacheSyncServiceMap;

	@Autowired(required = false)
	public void setCacheSyncServiceList(@Nullable List<CacheSyncService> cacheSyncServiceList) {
		cacheSyncServiceMap = X.isValid(cacheSyncServiceList) ? CollectionUtil.toHashMap(cacheSyncServiceList, CacheSyncService::getCacheId) : null;
	}

	public void flushCache(@NonNull String cacheId) {
		log.info("准备刷新缓存：" + cacheId);
		// 为了保证代码的严谨性，后台操作时，此处直接报错会好一些
		final CacheSyncService service = cacheSyncServiceMap.get(cacheId);
		service.flushCache();
	}

}