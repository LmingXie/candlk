package com.bojiu.webapp.base.util;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.bojiu.common.redis.RedisUtil;
import com.bojiu.context.model.RedisKey;
import me.codeplayer.util.*;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.redis.core.HashOperations;

import static com.bojiu.context.model.RedisKey.USER_LEVEL;

public class UserLevelUtil {

	/** 人工调整用户等级=等级 * 100 */
	public static final Integer LEVEL_ADJUST_RADIX = 100;
	/** 等级0对应的映射值99 */
	public static final int ZERO_LEVEL_MAPPING = 99;

	/**
	 * 获取具体用户等级
	 *
	 * @return < 人工控制等级, 真实升级等级 >
	 */
	public static Pair<Integer, Integer> getSpeUserLevel(Long userId) {
		return resolveSpeLevel(RedisUtil.opsForHash().get(RedisKey.USER_LEVEL, userId.toString()));
	}

	public static Pair<Integer, Integer> resolveSpeLevel(Object levelStr) {
		Integer level = NumberUtil.getInteger(levelStr, 0);
		if (level < LEVEL_ADJUST_RADIX) {
			return Pair.of(null, level % LEVEL_ADJUST_RADIX);
		}
		final Integer adjustLevel = level / LEVEL_ADJUST_RADIX;
		return Pair.of(Cmp.eq(adjustLevel, ZERO_LEVEL_MAPPING) ? 0 : adjustLevel, level % LEVEL_ADJUST_RADIX);
	}

	/**
	 * 优先获取人工修改后的用户等级，没有人工修改则取系统自动升级的等级
	 */
	public static Integer getUserLevel(Long userId) {
		return resolveLevel(RedisUtil.opsForHash().get(RedisKey.USER_LEVEL, userId.toString()));
	}

	protected static Integer resolveLevel(String levelStr) {
		if (StringUtil.isEmpty(levelStr)) {
			return 0;
		}
		int level = NumberUtil.getInt(levelStr);
		final Integer adjustLevel = level / LEVEL_ADJUST_RADIX;
		return levelStr.length() > 2 ? Cmp.eq(adjustLevel, ZERO_LEVEL_MAPPING) ? 0 : adjustLevel : level;
	}

	/**
	 * 获取真实升级的用户等级
	 */
	public static Integer getAutoUserLevel(Long userId) {
		return getSpeUserLevel(userId).getValue();
	}

	/**
	 * 批量获取用户等级，优先获取人工干预的，取不到再获取系统自动升级的
	 */
	public static List<Integer> findUserLevel(Collection<String> userIds) {
		final List<String> levels = RedisUtil.opsForHash().multiGet(RedisKey.USER_LEVEL, userIds);
		final List<Integer> ls = new ArrayList<>();
		for (String level : levels) {
			ls.add(resolveLevel(level));
		}
		return ls;
	}

	public static Integer findUserLevel(String userId) {
		String level = RedisUtil.opsForHash().get(USER_LEVEL, userId);
		return resolveLevel(level);
	}

	/**
	 * 批量获取用户等级，优先获取人工干预的，取不到再获取系统自动升级的
	 */
	public static Map<Long, Integer> findUserLevelToMap(Collection<String> userIds) {
		if (!X.isValid(userIds)) {
			return Collections.emptyMap();
		}
		final List<String> levels = RedisUtil.opsForHash().multiGet(RedisKey.USER_LEVEL, userIds);
		final Map<Long, Integer> map = new HashMap<>(userIds.size(), 1L);
		int i = 0;
		for (String userId : userIds) {
			String level = levels.get(i++);
			map.put(Long.parseLong(userId), resolveLevel(level));
		}
		return map;
	}

	/**
	 * 更新redis中用户等级
	 */
	public static void updateUserLevelCache(String userId, String newLevel, String statNewLevel, String oldLevel, String userLevelMonthKey, String userLevelWeekKey, String userLevelDailyKey, String vipNumKey) {
		RedisUtil.doInTransaction(redisOps -> {
			HashOperations<String, String, String> redisHash = redisOps.opsForHash();
			redisHash.put(USER_LEVEL, userId, newLevel);

			redisHash.putIfAbsent(userLevelMonthKey, userId, oldLevel);
			redisOps.expire(userLevelMonthKey, 62, TimeUnit.DAYS);

			redisHash.putIfAbsent(userLevelWeekKey, userId, oldLevel);
			redisOps.expire(userLevelWeekKey, 15, TimeUnit.DAYS);

			redisHash.putIfAbsent(userLevelDailyKey, userId, oldLevel);
			redisOps.expire(userLevelDailyKey, 3, TimeUnit.DAYS);

			redisHash.increment(vipNumKey, oldLevel, -1);
			redisHash.increment(vipNumKey, statNewLevel, 1);
		});
	}

}