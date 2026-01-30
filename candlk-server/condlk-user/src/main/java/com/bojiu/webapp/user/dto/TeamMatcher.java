package com.bojiu.webapp.user.dto;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.bojiu.common.redis.RedisUtil;
import com.bojiu.webapp.user.bet.BetApi;
import com.bojiu.webapp.user.model.BetProvider;
import com.bojiu.webapp.user.model.UserRedisKey;
import me.codeplayer.util.CollectionUtil;
import me.codeplayer.util.X;
import org.springframework.data.redis.core.HashOperations;

/** 球队匹配器 */
public class TeamMatcher {

	// 定义原始别名常量
	private static final String[] ALIAS_CONFIG = {
			"Qus,El Qusiya",
			"Al Madina Al Monawara SC,El Madina El Monowara",
			"Al-Khaleej,Al Khaleej Saihat",
			"Al-Ettifaq,Al Ettifaq",
			"Al-Nassr,Al Nassr Riyadh",
			"Al-Hilal,Al Hilal Riyadh",
			"Al-Kholood,Al Kholood",
			"Al-Akhdoud,Al Okhdood",
			"Al Khalidiya,Al Khaldiya SC U21",
			"Al-Najma,Al Najma Manama U21",
			"Al Khaldiya SC,Khalidiya,Al Khaldiya",
			"A Ali SC,A Ali Bahrain,A'Ali",
			"Floresta CE,Floresta",
			"Maranguape CE,Maranguape FC",
			"MS Tamya,Tamea",
			"Cascada SC,Cascada",
			"Ternana (W),Ternana",
			"Inter Milan (W),Internazionale",
			"Olympic Club,El Olympi",
			"Al Orobah Sakakah U21,Al Orubah",
			"Al Najma Unaizah U21,Al Najma",
			"Becamex Ho Chi Minh,Binh Duong",
			"Raya Ghazl,Raya SC",
			"El Qanah,El Qanah",
			"Telecom Egypt,WE SC",
			"IMT Novi Beograd,IMT",
			"Radnicki 1923,Radnicki Kragujevac",
			"MP Mikkeli,Mikkelin Palloilijat",
			"HJK Klubi 04,HJK Klubi 04",
	};

	// 预处理后的倒排索引 Map
	private static final Map<String, String> ALIAS_MAP = new HashMap<>();

	static {
		for (String group : ALIAS_CONFIG) {
			final String[] names = group.split(",");
			final String standardName = names[0]; // 以第一个作为标准名
			for (String name : names) {
				ALIAS_MAP.put(name, standardName);
			}
		}
	}

	/** 队名匹配 */
	public static GameDTO findMatchedGame(GameDTO aGame, List<GameDTO> bGames) {
		// 预先计算出 A 平台的主客队标准标识
		final String aHome = ALIAS_MAP.getOrDefault(aGame.teamHome, aGame.teamHome),
				aClient = ALIAS_MAP.getOrDefault(aGame.teamClient, aGame.teamClient);

		for (GameDTO bGame : bGames) {
			final String bHome = ALIAS_MAP.getOrDefault(bGame.teamHome, bGame.teamHome),
					bClient = ALIAS_MAP.getOrDefault(bGame.teamClient, bGame.teamClient);

			// 只需要简单的字符串相等判断
			final boolean isMatch = (aHome.equalsIgnoreCase(bHome) && aClient.equalsIgnoreCase(bClient))
					|| (aHome.equalsIgnoreCase(bClient) && aClient.equalsIgnoreCase(bHome));

			if (isMatch) {
				return bGame;
			}
		}
		return null;
	}

	/** 团队与联赛名的英中文映射（） */
	private static final Map<BetProvider, Map<String, String>> enToZhCache = new ConcurrentHashMap<>(BetProvider.CACHE.length, 1F);

	public static Map<String, String> getEnToZhCacheMap(BetProvider betProvider, Set<GameDTO> gameEnDTOs) {
		return getEnToZhCacheMap(betProvider, gameEnDTOs, false);
	}

	public static Map<String, String> getEnToZhCacheMap(BetProvider betProvider, Set<GameDTO> gameEnDTOs, boolean flush) {
		if (flush) {
			return flushEnToZhCache(betProvider, gameEnDTOs, false);
		}
		return enToZhCache.computeIfAbsent(betProvider, k -> {
			final Map<String, String> redisCache = X.castType(RedisUtil.template().opsForHash().entries(UserRedisKey.TEAM_NAME_EN2ZH_CACHE + betProvider.name()));
			if (redisCache.isEmpty()) {
				return flushEnToZhCache(betProvider, gameEnDTOs, true);
			}
			return redisCache;
		});
	}

	/** 刷新缓存数据 */
	private static Map<String, String> flushEnToZhCache(BetProvider betProvider, Set<GameDTO> gameEnDTOs, boolean isCompute) {
		final Set<GameDTO> gameZhBets = BetApi.getInstance(betProvider).getGameBets(BetApi.LANG_ZH);
		final Map<String, String> cache = isCompute ? new HashMap<>() : enToZhCache.computeIfAbsent(betProvider, k -> new HashMap<>());
		final boolean empty = cache.isEmpty();
		final Map<String, String> newMap = new HashMap<>();
		final HashMap<Long, GameDTO> enGameMap = CollectionUtil.toHashMap(gameEnDTOs, GameDTO::getId);
		for (GameDTO zhDto : gameZhBets) {
			final GameDTO enDto = enGameMap.get(zhDto.getId());
			if (enDto != null) {
				if (empty) {
					newMap.put(enDto.getLeague(), zhDto.getLeague());
					newMap.put(enDto.getTeamHome(), zhDto.getTeamHome());
					newMap.put(enDto.getTeamClient(), zhDto.getTeamClient());
					cache.put(enDto.getLeague(), zhDto.getLeague());
					cache.put(enDto.getTeamHome(), zhDto.getTeamHome());
					cache.put(enDto.getTeamClient(), zhDto.getTeamClient());
				} else {
					if (!cache.containsKey(enDto.getLeague())) {
						newMap.put(enDto.getLeague(), zhDto.getLeague());
						cache.put(enDto.getLeague(), zhDto.getLeague());
					}
					if (!cache.containsKey(enDto.getTeamHome())) {
						newMap.put(enDto.getTeamHome(), zhDto.getTeamHome());
						cache.put(enDto.getTeamHome(), zhDto.getTeamHome());
					}
					if (!cache.containsKey(enDto.getTeamClient())) {
						newMap.put(enDto.getTeamClient(), zhDto.getTeamClient());
						cache.put(enDto.getTeamClient(), zhDto.getTeamClient());
					}
				}
			}
		}
		if (!newMap.isEmpty()) {
			RedisUtil.doInTransaction(redisOps -> {
				final HashOperations<String, Object, Object> opsForHash = redisOps.opsForHash();
				opsForHash.putAll(UserRedisKey.TEAM_NAME_EN2ZH_CACHE + betProvider.name(), newMap);
			});
		}
		return cache;
	}

}