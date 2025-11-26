package com.bojiu.webapp.user.service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.bojiu.common.model.Status;
import com.bojiu.webapp.base.service.*;
import com.bojiu.webapp.user.dao.MetaDao;
import com.bojiu.webapp.user.entity.Meta;
import com.bojiu.webapp.user.model.MetaType;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.NumberUtil;
import me.codeplayer.util.X;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.bojiu.webapp.user.entity.Meta.*;

/**
 * 商户站点元数据配置表 服务实现类
 *
 * @author LeeYd
 * @since 2023-09-07
 */
@Slf4j
@Service
public class MetaService extends BaseServiceImpl<Meta, MetaDao, Long> implements CacheSyncService, InitializingBean {

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

	@Nonnull
	public Map<String, Meta> findCached(@Nonnull Long merchantId, @Nonnull MetaType type, boolean flush) {
		type.checkServiceRanges(); // 检查调用缓存的服务是否超出声明的服务范围
		EnumMap<MetaType, Map<String, Meta>> keyValueMap = cache.get(merchantId, merchantMapBuilder);
		Map<String, Meta> map = keyValueMap.get(type);
		if (map == null || flush) {
			keyValueMap.put(type, map = findBy(merchantId, type));
		}
		return map;
	}

	@Nonnull
	public ImmutableMap<String, Meta> findBy(@Nonnull Long merchantId, @Nonnull MetaType type) {
		final List<Meta> metas = find(merchantId, type);
		ImmutableMap.Builder<String, Meta> builder = ImmutableMap.builder();
		for (Meta meta : metas) {
			builder.put(meta.getName(), meta);
		}
		return builder.build();
	}

	@Nonnull
	public Map<String, Meta> findCached(@Nonnull Long merchantId, @Nonnull MetaType type) {
		return findCached(merchantId, type, false);
	}

	@Nonnull
	public Map<Long, Meta> multiGetCached(@Nonnull MetaType type, Collection<Long> merchantIds) {
		type.checkServiceRanges(); // 检查调用缓存的服务是否超出声明的服务范围
		final Map<Long, Meta> map = new HashMap<>(merchantIds.size(), 1F);
		final List<Long> missingIds = new ArrayList<>();
		for (Long merchantId : merchantIds) {
			EnumMap<MetaType, Map<String, Meta>> m = cache.getIfPresent(merchantId);
			Map<String, Meta> typedMap = m == null ? null : m.get(type);
			if (typedMap == null) {
				missingIds.add(merchantId);
			} else {
				map.put(merchantId, typedMap.get(type.name()));
			}
		}
		if (!missingIds.isEmpty()) {
			final List<Meta> metas = find(type.name(), type, missingIds);
			for (Meta meta : metas) {
				Long merchantId = meta.getMerchantId();
				map.put(merchantId, meta);
				EnumMap<MetaType, Map<String, Meta>> enumMap = cache.get(merchantId, merchantMapBuilder);
				Map<String, Meta> old = enumMap.put(type, ImmutableMap.of(type.name(), meta));
				if (old != null) {
					enumMap.remove(type);
				}
			}
		}
		return map;
	}

	public List<Meta> find(Long merchantId, MetaType type, @Nullable String name) {
		return find(merchantId, type, name, null);
	}

	public List<Meta> find(Long merchantId, MetaType type, @Nullable String name, @Nullable String ext) {
		var wrapper = smartEq(TYPE, type, NAME, name, STATUS, Status.YES.value)
				.eq(MERCHANT_ID, merchantId)
				.eq(ext != null, EXT, ext)
				.orderByAsc(ID);
		return selectList(wrapper);
	}

	public List<Meta> find(Long merchantId, @Nullable String name, MetaType... types) {
		var wrapper = smartEq(NAME, name, STATUS, Status.YES.value)
				.eq(true, MERCHANT_ID, merchantId)
				.ins(TYPE, types)
				.orderByAsc(ID);
		return selectList(wrapper);
	}

	public List<Meta> find(@Nullable String name, MetaType type, Collection<Long> merchantIds) {
		return this.findAll(name, type, merchantIds, Status.YES.value);
	}

	public List<Meta> findAll(@Nullable String name, MetaType type, Collection<Long> merchantIds, @Nullable Integer status) {
		var wrapper = smartEq(TYPE, type, NAME, name, STATUS, status)
				.in(MERCHANT_ID, merchantIds)
				.orderByAsc(ID);
		return selectList(wrapper);
	}

	public List<Meta> find(@Nullable String name, MetaType type, String merchantIds) {
		var wrapper = smartEq(TYPE, type, NAME, name, STATUS, Status.YES.value)
				.inSql(MERCHANT_ID, merchantIds)
				.orderByAsc(ID);
		return selectList(wrapper);
	}

	public List<Meta> find(Long merchantId, MetaType type) {
		return find(merchantId, type, null);
	}

	public List<Meta> find(Long merchantId, MetaType... type) {
		return find(merchantId, null, type);
	}

	public Meta getCached(@Nonnull Long merchantId, @Nonnull MetaType type, @Nonnull String name, boolean flush) {
		Map<String, Meta> cached = findCached(merchantId, type, flush);
		return cached.get(name);
	}

	public Meta getCached(@Nonnull Long merchantId, @Nonnull MetaType type, @Nonnull String name) {
		return getCached(merchantId, type, name, false);
	}

	public Meta getCached(@Nonnull Long merchantId, @Nonnull MetaType type) {
		return getCached(merchantId, type, type.name(), false);
	}

	public <T> T getCachedParsedValue(@Nonnull Long merchantId, @Nonnull MetaType type, Class<T> clazz) {
		return getCachedParsedValue(merchantId, type, type.name(), clazz);
	}

	public <T> T getCachedParsedValue(@Nonnull Long merchantId, @Nonnull MetaType type, String name, Class<T> clazz) {
		Meta meta = getCached(merchantId, type, name, false);
		return meta != null ? meta.getParsedValue(clazz) : null;
	}

	public Meta getBy(@Nonnull Long merchantId, @Nonnull MetaType type, @Nonnull String name) {
		List<Meta> list = find(merchantId, type, name);
		return list.isEmpty() ? null : list.get(0);
	}

	@Transactional
	public void saveOrUpdate(Meta meta) {
		super.saveOrUpdate(meta, meta.hasValidId());
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
	public Meta getCached(@Nonnull MetaType type, @Nonnull String name, @Nonnull Long... merchantIds) {
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