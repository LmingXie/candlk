package com.bojiu.webapp.user.service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.bojiu.common.redis.RedisUtil;
import com.bojiu.context.model.RedisKey;
import com.bojiu.webapp.base.service.CacheSyncService;
import com.bojiu.webapp.base.service.RemoteSyncService;
import com.bojiu.webapp.user.entity.Meta;
import com.bojiu.webapp.user.model.MetaType;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.NumberUtil;
import me.codeplayer.util.X;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.stereotype.Service;

/**
 * 商户站点元数据配置表 服务实现类
 *
 * @author LeeYd
 * @since 2023-09-07
 */
@Slf4j
@Service
public class MetaService /*extends BaseServiceImpl<Meta, MetaDao, Long>*/ implements CacheSyncService, InitializingBean {

	static MetaService instance;

	public static MetaService get() {
		return instance;
	}

	/**
	 * < 商户ID, < 类型, < name, value > > >
	 * <p>
	 * 出于 跨服务通用的设计 考虑，采用根据 商户ID + 类型 的【局部】懒加载方式来 初始化数据
	 */
	static final Cache<Long, EnumMap<MetaType, Map<String, Meta>>> cache = Caffeine.newBuilder()
			.initialCapacity(4)
			.maximumSize(1024)
			.expireAfterAccess(1, TimeUnit.HOURS)
			.build();

	static final Function<Long, EnumMap<MetaType, Map<String, Meta>>> merchantMapBuilder = k -> new EnumMap<>(MetaType.class);

	@NonNull
	public Map<String, Meta> findCached(@NonNull Long merchantId, @NonNull MetaType type, boolean flush) {
		type.checkServiceRanges(); // 检查调用缓存的服务是否超出声明的服务范围
		EnumMap<MetaType, Map<String, Meta>> keyValueMap = cache.get(merchantId, merchantMapBuilder);
		Map<String, Meta> map = keyValueMap.get(type);
		if (map == null || flush) {
			keyValueMap.put(type, map = findBy(merchantId, type));
		}
		return map;
	}

	@NonNull
	public ImmutableMap<String, Meta> findBy(@NonNull Long merchantId, @NonNull MetaType type) {
		final List<Meta> metas = find(merchantId, type);
		ImmutableMap.Builder<String, Meta> builder = ImmutableMap.builder();
		for (Meta meta : metas) {
			builder.put(meta.getName(), meta);
		}
		return builder.build();
	}

	@NonNull
	public Map<String, Meta> findCached(@NonNull Long merchantId, @NonNull MetaType type) {
		return findCached(merchantId, type, false);
	}

	public List<Meta> find(Long merchantId, MetaType type, @Nullable String name) {
		final HashOperations<String, String, String> opsForHash = RedisUtil.opsForHash();
		if (name != null) {
			final String value = opsForHash.get(RedisKey.META_PREFIX + merchantId + ":" + type.value, name);
			final Meta meta;
			if (value == null) {
				meta = MetaService.getDefaultMeta(merchantId, type, name);
			} else {
				meta = new Meta();
				meta.setMerchantId(merchantId);
				meta.setType(type);
				meta.setName(name);
				meta.setValue(value);
				meta.setLabel(type.getLabel());
			}
			return value != null ? List.of(meta) : Collections.emptyList();
		} else {
			final Map<String, String> map = opsForHash.entries(RedisKey.META_PREFIX + merchantId + ":" + type.value);
			if (map.isEmpty()) {
				return Collections.emptyList();
			}
			final List<Meta> metas = new ArrayList<>(map.size());
			for (Map.Entry<String, String> entry : map.entrySet()) {
				final Meta meta = new Meta();
				meta.setMerchantId(merchantId);
				meta.setType(type);
				meta.setName(entry.getKey());
				meta.setValue(entry.getValue());
				meta.setLabel(type.getLabel());
				metas.add(meta);
			}
			return metas;
		}
	}

	public static Meta getDefaultMeta(Long merchantId, MetaType type, String name) {
		final Meta meta = new Meta();
		meta.setMerchantId(merchantId);
		meta.setType(type);
		meta.setName(name);
		meta.setValue(switch (type) {
			case base_rate_config -> "{\"aPrincipal\":1000,\"aRebate\":0.02,\"aRechargeRate\":0,\"bRebate\":0.025}";
			default -> throw new RuntimeException("MetaType not found");
		});
		meta.setLabel(type.getLabel());
		return meta;
	}

	public List<Meta> find(Long merchantId, MetaType type) {
		return find(merchantId, type, null);
	}

	public Meta getCached(@NonNull Long merchantId, @NonNull MetaType type, @NonNull String name, boolean flush) {
		Map<String, Meta> cached = findCached(merchantId, type, flush);
		return cached.get(name);
	}

	public Meta getCached(@NonNull Long merchantId, @NonNull MetaType type, @NonNull String name) {
		return getCached(merchantId, type, name, false);
	}

	public Meta getCached(@NonNull Long merchantId, @NonNull MetaType type) {
		return getCached(merchantId, type, type.name(), false);
	}

	public <T> T getCachedParsedValue(@NonNull Long merchantId, @NonNull MetaType type, Class<T> clazz) {
		return getCachedParsedValue(merchantId, type, type.name(), clazz);
	}

	public <T> T getCachedParsedValue(@NonNull Long merchantId, @NonNull MetaType type, String name, Class<T> clazz) {
		Meta meta = getCached(merchantId, type, name, false);
		return meta != null ? meta.getParsedValue(clazz) : null;
	}

	public Meta getBy(@NonNull Long merchantId, @NonNull MetaType type, @NonNull String name) {
		List<Meta> list = find(merchantId, type, name);
		return list.isEmpty() ? null : list.get(0);
	}

	@Override
	public String getCacheId() {
		return RemoteSyncService.MetaService;
	}

	@Override
	public void flushCache(Object... args) {
		final int size = X.size(args);
		switch (size) {
			case 0 -> cache.invalidateAll(); // 如果没有传入任何参数，直接清空所有
			case 1 -> {  // 如果只传入了 merchantId，则清空该商户所有缓存数据
				final Long merchantId = (Long) args[0];
				cache.invalidate(merchantId);
			}
			case 2 -> { // 如果传入了2个参数，参数0 可以为 null（表示全部）、单个商户ID、多个商户ID的 Set；参数1 为对应的 metaType
				final MetaType metaType = args[1] instanceof MetaType type ? type : MetaType.of((String) args[1]);
				if (args[0] == null) {
					for (Long merchantId : cache.asMap().keySet()) {
						clearCacheItem(merchantId, metaType);
					}
				} else if (args[0] instanceof Collection<?> merchantIds) {
					for (Object merchantId : merchantIds) {
						clearCacheItem(NumberUtil.getLong(merchantId, null), metaType);
					}
				} else {
					clearCacheItem((Long) args[0], metaType);
				}
			}
			default -> throw new IllegalArgumentException();
		}
	}

	private static void clearCacheItem(Long merchantId, MetaType metaType) {
		EnumMap<MetaType, Map<String, Meta>> map = cache.getIfPresent(merchantId);
		if (map != null) {
			map.remove(metaType);
		}
	}

	/**
	 * 返回第一个有值的对象
	 */
	public Meta getCached(@NonNull MetaType type, @NonNull String name, @NonNull Long... merchantIds) {
		Meta meta = null;
		for (Long merchantId : merchantIds) {
			meta = getCached(merchantId, type, name, false);
			if (meta != null) {
				break;
			}
		}
		return meta;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		instance = this;
	}

}