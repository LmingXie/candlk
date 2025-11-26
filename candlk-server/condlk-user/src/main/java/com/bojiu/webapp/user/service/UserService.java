package com.bojiu.webapp.user.service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.bojiu.common.model.Status;
import com.bojiu.webapp.base.service.*;
import com.bojiu.webapp.user.dao.UserDao;
import com.bojiu.webapp.user.entity.User;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.codeplayer.util.ArrayUtil;
import me.codeplayer.util.CollectionUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

/** 账号表 服务实现类 */
@Service
public class UserService extends BaseServiceImpl<User, UserDao, Long> implements CacheSyncService {

	/** 缓存：$userId -> $user */
	static final Cache<Long, User> cache = Caffeine.newBuilder()
			.initialCapacity(100)
			.maximumSize(1024)
			.expireAfterWrite(1, TimeUnit.DAYS)
			.build();

	public Collection<User> getCacheByIds(Long... ids) {
		final Function<Long, Long> converter = c -> c;
		Map<Long, @NonNull User> all = cache.getAll(ArrayUtil.toList(converter, ids),
				ids_ -> CollectionUtil.toHashMap(findByIds(ids), User::getUserId));
		return all.values();
	}

	public User getCache(Long userId) {
		return cache.get(userId, this::get);
	}

	public Collection<User> getCacheAll() {
		return cache.asMap().values();
	}

	public List<User> findAllNormal() {
		List<User> users = selectList(smartEq(User.STATUS, Status.YES.value));
		if (!users.isEmpty()) {
			for (User user : users) {
				cache.put(user.getUserId(), user);
			}
		}
		return users;
	}

	@Override
	public String getCacheId() {
		return RemoteSyncService.UserService;
	}

	@Override
	public void flushCache(Object... args) {
		Long userId = (Long) ArrayUtils.get(args, 0);
		if (userId == null) {
			cache.invalidateAll();
		} else {
			cache.invalidate(userId);
		}
	}

}
