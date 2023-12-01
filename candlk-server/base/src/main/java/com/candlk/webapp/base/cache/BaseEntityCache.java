package com.candlk.webapp.base.cache;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.*;

import com.candlk.common.model.Bean;
import com.candlk.context.model.Member;
import com.candlk.context.web.Jsons;
import com.candlk.context.web.RequestContextImpl;
import com.candlk.webapp.base.entity.BaseEntity;
import com.candlk.webapp.base.service.RemoteBaseService;
import me.codeplayer.util.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 实体缓存的基础设施抽象基类
 */
public abstract class BaseEntityCache<S extends BaseEntity, T extends Bean<Long>> implements InitializingBean {

	public static final long BASE_MS = new EasyDate(2023, 10, 1).getTime();

	public final Class<T> type;
	public final String redisKey;

	public BaseEntityCache(Class<T> type, String redisKey) {
		this.type = type;
		this.redisKey = redisKey;
	}

	@Resource
	protected StringRedisTemplate stringRedisTemplate;
	transient HashOperations<String, String, String> redisHash;

	protected abstract RemoteBaseService<S, Long> getRemoteBaseService();

	public T get(Long id) {
		return get(id, true);
	}

	public T get(Long id, final boolean useCache) {
		final String hashKey = id.toString();
		final long now = System.currentTimeMillis();
		T bean = useCache ? deserialize(redisHash.get(redisKey, hashKey), now) : null;
		if (bean == null) {
			final S entity = getRemoteBaseService().get(id);
			if (entity != null) {
				bean = of(entity);
				redisHash.put(redisKey, hashKey, serialize(bean, now));
			}
		}
		return bean;
	}

	protected abstract T of(S entity);

	protected String serialize(T bean, long nowMs) {
		return Jsons.encode(beforeSerialize(bean, nowMs));
	}

	protected T beforeSerialize(@Nullable T bean, long nowMs) {
		return bean;
	}

	@Nullable
	public T deserialize(@Nullable String jsonStr, long nowMs) {
		final T bean = Jsons.parseObject(jsonStr, type);
		return afterDeserialize(bean, nowMs);
	}

	protected T afterDeserialize(@Nullable T bean, long nowMs) {
		return bean;
	}

	/**
	 * 判定指定ID是否存在（这个用户已经注销也可能返回 true）
	 */
	public boolean contains(Long id) {
		final String hashKey = id.toString();
		boolean exists = redisHash.hasKey(redisKey, hashKey);
		if (!exists) {
			final S entity = getRemoteBaseService().get(id);
			if (entity != null) {
				T bean = of(entity);
				final long now = System.currentTimeMillis();
				redisHash.put(redisKey, hashKey, serialize(bean, now));
				return true;
			}
		}
		return exists;
	}

	public void put(T bean) {
		redisHash.put(redisKey, cacheKey(bean).toString(), serialize(bean, System.currentTimeMillis()));
	}

	@SuppressWarnings("unchecked")
	<E> void putAll(Collection<E> beans, @Nullable Function<E, T> converter) {
		if (X.isValid(beans)) {
			final long nowMs = System.currentTimeMillis();
			Map<String, String> map = new HashMap<>(beans.size(), 1F);
			for (E bean : beans) {
				final T t = converter == null ? (T) bean : of((S) bean);
				map.put(cacheKey(t).toString(), serialize(t, nowMs));
			}
			redisHash.putAll(redisKey, map);
		}
	}

	public void putAll(Collection<T> beans) {
		putAll(beans, null);
	}

	public static long timeToOffsetBaseSecs(long timeInMs) {
		return (timeInMs - BASE_MS) / 1000L;
	}

	public static long baseOffsetSecsToTimeMs(long offsetSecs) {
		return offsetSecs * 1000L + BASE_MS;
	}

	public void set(@Nullable S bean) {
		if (bean != null) {
			put(of(bean));
		}
	}

	public void setAll(@Nullable Collection<S> beans) {
		putAll(beans, this::of);
	}

	static final Function<Long, String> idToString = Object::toString;

	@Nonnull
	public Map<Long, T> find(Collection<Long> ids) {
		if (!X.isValid(ids)) {
			return Collections.emptyMap();
		}
		final HashMap<Long, String> map = CollectionUtil.newHashMap(ids.size());
		for (Long id : ids) {
			map.computeIfAbsent(id, idToString);
		}
		return doFind(map);
	}

	@Nonnull
	public Map<Long, T> find(Long... ids) {
		return find(Arrays.asList(ids));
	}

	@Nonnull
	public Map<Long, T> doFind(HashMap<Long, String> idPairMap) {
		final List<String> list = redisHash.multiGet(redisKey, idPairMap.values());
		final int size = list.size();
		final long now = System.currentTimeMillis();
		HashMap<Long, Object> map = X.castType(idPairMap);
		int missingCount = 0;
		for (int i = 0; i < size; i++) {
			final String jsonStr = list.get(i);
			T bean = deserialize(jsonStr, now);
			if (bean != null) {
				map.put(cacheKey(bean), bean);
			} else {
				missingCount++;
			}
		}

		if (missingCount > 0) { // 存在缓存未命中的ID
			List<Long> missingIds = new ArrayList<>(missingCount);
			for (Map.Entry<Long, Object> e : map.entrySet()) {
				if (e.getValue() instanceof String) {
					missingIds.add(e.getKey());
				}
			}
			final List<S> entities = getRemoteBaseService().findByIds(missingIds);

			final int supplied = entities.size();
			if (supplied > 0) {
				Map<String, String> missingMap = new HashMap<>(supplied, 1F);
				for (S entity : entities) {
					final T bean = of(entity);
					final Long cachedId = cacheKey(bean);
					missingMap.put(idPairMap.get(cachedId), serialize(bean, now));
					map.put(cachedId, bean);
				}
				redisHash.putAll(redisKey, missingMap);
			}

			if (missingCount > supplied) {
				map.entrySet().removeIf(e -> e.getValue() instanceof String);
			}
		}
		return X.castType(map);
	}

	public Long cacheKey(T bean) {
		return bean.getId();
	}

	@Nonnull
	public <E> Map<Long, T> find(@Nullable Collection<E> c, Function<? super E, Long> idGetter) {
		return find(c, idGetter, null);
	}

	@Nonnull
	public <E> Map<Long, T> find(@Nullable Collection<E> c, Function<? super E, Long> idGetter, @Nullable Function<? super E, Long> nextIdGetter) {
		if (!X.isValid(c)) {
			return Collections.emptyMap();
		}
		boolean hasNextGetter = nextIdGetter != null;
		final HashMap<Long, String> map = CollectionUtil.newHashMap(c.size() * (hasNextGetter ? 2 : 1));
		final Consumer<Long> putIfAbsent = i -> map.computeIfAbsent(i, idToString);
		for (E e : c) {
			X.use(idGetter.apply(e), putIfAbsent);
			if (hasNextGetter) {
				X.use(nextIdGetter.apply(e), putIfAbsent);
			}
		}
		return doFind(map);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		redisHash = stringRedisTemplate.opsForHash();
	}

	/**
	 * 更新缓存并同步刷新到 Spring Session
	 */
	public void setAndFlushSession(@Nullable S member) {
		if (member != null) {
			put(of(member));
			RequestContextImpl req = new RequestContextImpl();
			req.setSessionId(((Member) member).getSessionId());
			final org.springframework.session.Session session = req.getSpringSession();
			if (session != null) {
				session.setAttribute(RequestContextImpl.SESSION_USER, member);
				req.flushSession(true);
			}
		}
	}

}
