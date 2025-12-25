package com.bojiu.context.web;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

import lombok.Getter;
import me.codeplayer.util.X;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.session.*;
import org.springframework.util.Assert;

/**
 * 支持自定义 sessionId 的增强版 RedisSessionRepository
 *
 * @see org.springframework.session.data.redis.RedisSessionRepository
 */
public class EnhanceRedisSessionRepository implements SessionRepository<EnhanceRedisSessionRepository.RedisSession> {

	/**
	 * The default namespace for each key and channel in Redis used by Spring Session.
	 */
	public static final String DEFAULT_KEY_NAMESPACE = "ssid";

	@Getter
	private final RedisOperations<String, Object> sessionRedisOperations;

	private Duration defaultMaxInactiveInterval = Duration.ofSeconds(600);

	private String keyNamespace = DEFAULT_KEY_NAMESPACE + ":";

	private FlushMode flushMode = FlushMode.ON_SAVE;

	private SaveMode saveMode = SaveMode.ON_SET_ATTRIBUTE;

	/**
	 * Create a new {@link org.springframework.session.data.redis.RedisSessionRepository} instance.
	 *
	 * @param sessionRedisOperations the {@link RedisOperations} to use for managing
	 * sessions
	 */
	public EnhanceRedisSessionRepository(RedisOperations<String, Object> sessionRedisOperations) {
		Assert.notNull(sessionRedisOperations, "sessionRedisOperations mut not be null");
		this.sessionRedisOperations = sessionRedisOperations;
	}

	/**
	 * Set the default maxInactiveInterval.
	 *
	 * @param defaultMaxInactiveInterval the default maxInactiveInterval
	 */
	public void setDefaultMaxInactiveInterval(Duration defaultMaxInactiveInterval) {
		Assert.notNull(defaultMaxInactiveInterval, "defaultMaxInactiveInterval must not be null");
		this.defaultMaxInactiveInterval = defaultMaxInactiveInterval;
	}

	/**
	 * Set the key namespace.
	 *
	 * @param keyNamespace the key namespace
	 * @deprecated since 2.4.0 in favor of {@link #setRedisKeyNamespace(String)}
	 */
	@Deprecated
	public void setKeyNamespace(String keyNamespace) {
		Assert.hasText(keyNamespace, "keyNamespace must not be empty");
		this.keyNamespace = keyNamespace;
	}

	/**
	 * Set the Redis key namespace.
	 *
	 * @param namespace the Redis key namespace
	 */
	public void setRedisKeyNamespace(String namespace) {
		Assert.hasText(namespace, "namespace must not be empty");
		this.keyNamespace = namespace.trim() + ":";
	}

	/**
	 * Set the flush mode.
	 *
	 * @param flushMode the flush mode
	 */
	public void setFlushMode(FlushMode flushMode) {
		Assert.notNull(flushMode, "flushMode must not be null");
		this.flushMode = flushMode;
	}

	/**
	 * Set the save mode.
	 *
	 * @param saveMode the save mode
	 */
	public void setSaveMode(SaveMode saveMode) {
		Assert.notNull(saveMode, "saveMode must not be null");
		this.saveMode = saveMode;
	}

	@Override
	public EnhanceRedisSessionRepository.RedisSession createSession() {
		return createSession(NanoIdUtils.randomNanoId() /* UUID.randomUUID().toString() */);
	}

	protected EnhanceRedisSessionRepository.RedisSession createSession(String sessionId) {
		MapSession cached = new MapSession(sessionId);
		cached.setMaxInactiveInterval(this.defaultMaxInactiveInterval);
		EnhanceRedisSessionRepository.RedisSession session = new EnhanceRedisSessionRepository.RedisSession(cached, true);
		session.flushIfRequired();
		return session;
	}

	@Override
	public void save(EnhanceRedisSessionRepository.RedisSession session) {
		/* 先屏蔽校验
		if (!session.isNew) {
			String key = getSessionKey(session.hasChangedSessionId() ? session.originalSessionId : session.getId());
			Boolean sessionExists = this.sessionRedisOperations.hasKey(key);
			if (sessionExists == null || !sessionExists) {
				throw new IllegalStateException("Session was invalidated");
			}
		}
		*/
		session.save();
	}

	@Override
	public EnhanceRedisSessionRepository.RedisSession findById(String sessionId) {
		String key = getSessionKey(sessionId);
		Map<String, Object> entries = this.sessionRedisOperations.<String, Object>opsForHash().entries(key);
		if (entries.isEmpty()) {
			return createSession(sessionId); // 直接基于传入的 sessionId 构造一个新的 session
			// return null;
		}
		MapSession session = new RedisSessionMapper(sessionId).apply(entries);
		if (session == null || session.isExpired()) {
			deleteById(sessionId);
			return null;
		}
		return new EnhanceRedisSessionRepository.RedisSession(session, false);
	}

	@Override
	public void deleteById(String sessionId) {
		String key = getSessionKey(sessionId);
		this.sessionRedisOperations.delete(key);
	}

	public String getSessionKey(String sessionId) {
		return this.keyNamespace + "sessions:" + sessionId;
	}

	private static String getAttributeKey(String attributeName) {
		return RedisSessionMapper.ATTRIBUTE_PREFIX + attributeName;
	}

	/**
	 * A {@link Function} that converts a {@link Map} representing Redis hash to a
	 * {@link MapSession}.
	 *
	 * @author Vedran Pavic
	 * @since 2.2.0
	 */
	static final class RedisSessionMapper implements Function<Map<String, Object>, MapSession> {

		/**
		 * The key in the hash representing {@link Session#getCreationTime()}.
		 */
		static final String CREATION_TIME_KEY = "creationTime";

		/**
		 * The key in the hash representing {@link Session#getLastAccessedTime()}.
		 */
		static final String LAST_ACCESSED_TIME_KEY = "lastAccessedTime";

		/**
		 * The key in the hash representing {@link Session#getMaxInactiveInterval()}.
		 */
		static final String MAX_INACTIVE_INTERVAL_KEY = "maxInactiveInterval";

		/**
		 * The prefix of the key in the hash used for session attributes. For example, if the
		 * session contained an attribute named {@code attributeName}, then there would be an
		 * entry in the hash named {@code sessionAttr:attributeName} that mapped to its value.
		 */
		static final String ATTRIBUTE_PREFIX = "sessionAttr:";

		private final String sessionId;

		RedisSessionMapper(String sessionId) {
			Assert.hasText(sessionId, "sessionId must not be empty");
			this.sessionId = sessionId;
		}

		@Override
		@Nullable
		public MapSession apply(Map<String, Object> map) {
			Assert.notEmpty(map, "map must not be empty");
			MapSession session = new MapSession(this.sessionId);
			Long creationTime = (Long) map.get(CREATION_TIME_KEY);
			if (creationTime == null) {
				// 当 session 被删除后，还有并发的 delta 更新，会导致 creationTime 为空报错，这里直接返回 null（视为 无效）
				return null;
				// handleMissingKey(CREATION_TIME_KEY);
			}
			session.setCreationTime(Instant.ofEpochMilli(creationTime));
			Long lastAccessedTime = (Long) map.get(LAST_ACCESSED_TIME_KEY);
			if (lastAccessedTime == null) {
				handleMissingKey(LAST_ACCESSED_TIME_KEY);
			}
			session.setLastAccessedTime(Instant.ofEpochMilli(lastAccessedTime));
			Integer maxInactiveInterval = (Integer) map.get(MAX_INACTIVE_INTERVAL_KEY);
			if (maxInactiveInterval == null) {
				handleMissingKey(MAX_INACTIVE_INTERVAL_KEY);
			}
			session.setMaxInactiveInterval(Duration.ofSeconds(maxInactiveInterval));
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				String name = entry.getKey();
				if (name.startsWith(ATTRIBUTE_PREFIX)) {
					session.setAttribute(name.substring(ATTRIBUTE_PREFIX.length()), entry.getValue());
				}
			}
			return session;
		}

		private static void handleMissingKey(String key) {
			throw new IllegalStateException(key + " key must not be null");
		}

	}

	/**
	 * An internal {@link Session} implementation used by this {@link SessionRepository}.
	 */
	public final class RedisSession implements Session {

		private final MapSession cached;

		private final Map<String, Object> delta = new HashMap<>();

		private boolean isNew;

		private String originalSessionId;

		RedisSession(MapSession cached, boolean isNew) {
			this.cached = cached;
			this.isNew = isNew;
			this.originalSessionId = cached.getId();
			if (this.isNew) {
				this.delta.put(RedisSessionMapper.CREATION_TIME_KEY, cached.getCreationTime().toEpochMilli());
				this.delta.put(RedisSessionMapper.MAX_INACTIVE_INTERVAL_KEY,
						(int) cached.getMaxInactiveInterval().getSeconds());
				this.delta.put(RedisSessionMapper.LAST_ACCESSED_TIME_KEY, cached.getLastAccessedTime().toEpochMilli());
			}
			if (this.isNew || (saveMode == SaveMode.ALWAYS)) {
				for (String attributeName : getAttributeNames()) {
					this.delta.put(getAttributeKey(attributeName), cached.getAttribute(attributeName));
				}
			}
		}

		@Override
		public String getId() {
			return this.cached.getId();
		}

		@Override
		public String changeSessionId() {
			return this.cached.changeSessionId();
		}

		@Override
		public <T> T getAttribute(String attributeName) {
			T attributeValue = this.cached.getAttribute(attributeName);
			if (attributeValue != null && saveMode.equals(SaveMode.ON_GET_ATTRIBUTE)) {
				this.delta.put(getAttributeKey(attributeName), attributeValue);
			}
			return attributeValue;
		}

		@Override
		public Set<String> getAttributeNames() {
			return this.cached.getAttributeNames();
		}

		@Override
		public void setAttribute(String attributeName, Object attributeValue) {
			this.cached.setAttribute(attributeName, attributeValue);
			this.delta.put(getAttributeKey(attributeName), attributeValue);
			flushIfRequired();
		}

		@Override
		public void removeAttribute(String attributeName) {
			setAttribute(attributeName, null);
		}

		@Override
		public Instant getCreationTime() {
			return this.cached.getCreationTime();
		}

		@Override
		public void setLastAccessedTime(Instant lastAccessedTime) {
			this.cached.setLastAccessedTime(lastAccessedTime);
			this.delta.put(RedisSessionMapper.LAST_ACCESSED_TIME_KEY, getLastAccessedTime().toEpochMilli());
			flushIfRequired();
		}

		@Override
		public Instant getLastAccessedTime() {
			return this.cached.getLastAccessedTime();
		}

		@Override
		public void setMaxInactiveInterval(Duration interval) {
			this.cached.setMaxInactiveInterval(interval);
			this.delta.put(RedisSessionMapper.MAX_INACTIVE_INTERVAL_KEY, (int) getMaxInactiveInterval().getSeconds());
			flushIfRequired();
		}

		@Override
		public Duration getMaxInactiveInterval() {
			return this.cached.getMaxInactiveInterval();
		}

		@Override
		public boolean isExpired() {
			return this.cached.isExpired();
		}

		private void flushIfRequired() {
			if (flushMode == FlushMode.IMMEDIATE) {
				save();
			}
		}

		private boolean hasChangedSessionId() {
			return !getId().equals(this.originalSessionId);
		}

		private void save() {
			saveChangeSessionId();
			saveDelta();
			if (this.isNew) {
				this.isNew = false;
			}
		}

		private void saveChangeSessionId() {
			if (hasChangedSessionId()) {
				if (!this.isNew) {
					String originalSessionIdKey = getSessionKey(this.originalSessionId);
					String sessionIdKey = getSessionKey(getId());
					sessionRedisOperations.rename(originalSessionIdKey, sessionIdKey);
				}
				this.originalSessionId = getId();
			}
		}

		private void saveDelta() {
			if (this.delta.isEmpty()) {
				return;
			}
			String key = getSessionKey(getId());
			sessionRedisOperations.executePipelined(new SessionCallback<>() {
				public <K, V> Object execute(@NonNull RedisOperations<K, V> operations) throws DataAccessException {
					RedisOperations<String, Object> redisOps = X.castType(operations);
					redisOps.opsForHash().putAll(key, delta);
					redisOps.expire(key, getMaxInactiveInterval());
					return null;
				}
			});
			this.delta.clear();
		}

	}

}