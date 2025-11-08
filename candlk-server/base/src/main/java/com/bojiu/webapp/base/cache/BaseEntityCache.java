package com.bojiu.webapp.base.cache;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.bojiu.common.dao.MybatisUtil;
import com.bojiu.common.model.Bean;
import com.bojiu.common.redis.RedisUtil;
import com.bojiu.common.util.Common;
import com.bojiu.common.util.SpringUtil;
import com.bojiu.context.model.Member;
import com.bojiu.context.web.*;
import com.bojiu.webapp.base.entity.BaseEntity;
import com.bojiu.webapp.base.service.RemoteBaseService;
import me.codeplayer.util.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.session.SessionRepository;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 实体缓存的基础设施抽象基类
 */
public abstract class BaseEntityCache<S extends BaseEntity, T extends Bean<Long>> implements InitializingBean {

	public static final long BASE_MS = new EasyDate(2024, 1, 16).getTime();

	public final Class<T> type;
	public final String redisKey;

	public BaseEntityCache(Class<T> type, String redisKey) {
		this.type = type;
		this.redisKey = redisKey;
	}

	protected transient HashOperations<String, String, String> redisHash;

	protected abstract RemoteBaseService<S, Long> getRemoteBaseService();

	protected RemoteBaseService<S, Long> initDelegate(Class<S> entityType, RemoteBaseService<S, Long> remoteBaseService) {
		// 只有存在 UserMapper 时，才会有 User 的 TableInfo，因此需要进行检查判断
		if (TableInfoHelper.getTableInfo(entityType) == null) {
			return remoteBaseService;
		}
		return new RemoteBaseService<>() {
			@Override
			public S get(Long id) {
				return MybatisUtil.runWithIgnoreTenant(() -> new QueryChainWrapper<>(entityType).eq(BaseEntity.ID, id).one());
			}

			@Override
			public List<S> findByIds(Collection<Long> ids) {
				return MybatisUtil.runWithIgnoreTenant(() -> new QueryChainWrapper<>(entityType).in(BaseEntity.ID, ids).list());
			}
		};
	}

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
		return Jsons.encodeRaw(beforeSerialize(bean, nowMs));
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

	public void remove(Long id) {
		if (TransactionSynchronizationManager.isActualTransactionActive()) {
			SpringUtil.runAfterCommit(() -> redisHash.delete(redisKey, id.toString()));
		} else {
			redisHash.delete(redisKey, id.toString());
		}
	}

	public void clear() {
		RedisUtil.template().delete(redisKey);
	}

	public void remove(Collection<Long> ids) {
		SpringUtil.runAfterCommit(() -> redisHash.delete(redisKey, (Object[]) Common.toStrArray(ids)));
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
		if (RedisUtil.opsForHash() == null) {
			SpringUtil.getBean(StringRedisTemplate.class);
		}
		redisHash = RedisUtil.opsForHash();
	}

	/**
	 * 更新缓存并同步刷新到 Spring Session
	 */
	public void setAndFlushSession(@Nullable S member) {
		if (member != null) {
			SpringUtil.runAfterCommit(() -> {
				put(of(member));
				String sessionId = ((Member) member).getSessionId();
				if (StringUtil.isEmpty(sessionId)) {
					return;
				}
				RequestContextImpl req = new RequestContextImpl();
				req.setSessionId(sessionId);
				final org.springframework.session.Session session = req.getSpringSession();
				if (session != null) {
					session.setAttribute(RequestContextImpl.SESSION_USER, member);
					req.flushSession(true);
				}
			});
		}
	}

	/**
	 * 清空缓存以及用户会话（以便于让用户重新登录）
	 */
	public void clearCacheAndSession(List<S> members) {
		final int size = X.size(members);
		if (size > 0) {
			SpringUtil.runAfterCommit(() -> {
				if (size == 1) {
					Member m = (Member) members.get(0);
					redisHash.delete(redisKey, m.getId().toString());
					RequestContextImpl.removeSession(m.getSessionId());
					return;
				}
				SessionRepository<?> repo = RequestContextImpl.getSessionRepository();
				final EnhanceRedisSessionRepository customRepository = repo instanceof EnhanceRedisSessionRepository t ? t : null;
				List<Member> list = X.castType(members);
				redisHash.delete(redisKey, (Object[]) Common.toStrArray(members, u -> u.getId().toString()));
				if (customRepository != null) {
					List<String> keys = Common.toList(list, m -> customRepository.getSessionKey(Assert.notNull(m.getSessionId())));
					RedisUtil.template().delete(keys);
				} else {
					for (Member m : list) {
						RequestContextImpl.removeSession(m.getSessionId());
					}
				}
			});
		}
	}

	/**
	 * 清空缓存以及用户会话（以便于让用户重新登录）
	 */
	public void clearCacheAndSession(S... members) {
		clearCacheAndSession(Arrays.asList(members));
	}

}