package com.bojiu.webapp.user.service;

import java.util.*;
import java.util.concurrent.*;

import com.bojiu.common.redis.RedisUtil;
import com.bojiu.context.web.Jsons;
import com.bojiu.context.web.TaskUtils;
import com.bojiu.webapp.user.dto.*;
import com.bojiu.webapp.user.dto.GameDTO.OddsInfo;
import com.bojiu.webapp.user.dto.HedgingDTO.Odds;
import com.bojiu.webapp.user.model.BetProvider;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.ArrayUtil;
import me.codeplayer.util.CollectionUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import static com.bojiu.webapp.user.model.UserRedisKey.GAME_BETS_PERFIX;

@Slf4j
@Service
public class BetMatchService {

	/** 获取A平台到B平台赛事的映射 */
	public final Map<GameDTO, GameDTO> getGameMapper(Pair<BetProvider, BetProvider> pair) {
		// 查询平台上的全部赛事和赔率信息
		final List<String> values = RedisUtil.opsForHash().multiGet(GAME_BETS_PERFIX, Arrays.asList(pair.getKey().name(), pair.getValue().name()));
		final List<GameDTO> gameBets = Jsons.parseArray(values.get(0), GameDTO.class),
				hedgingBets = Jsons.parseArray(values.get(1), GameDTO.class);

		// A平台赛事 与 B平台赛事 的映射
		final Map<GameDTO, GameDTO> gameMapper = new HashMap<>(Math.max(gameBets.size(), hedgingBets.size()), 1F);
		// 根据开赛时间分组（由于时间是更大的尺度，因此先匹配时间，再匹配队伍名称）
		final Map<Date, List<GameDTO>> hedgingMap = CollectionUtil.groupBy(hedgingBets, GameDTO::getOpenTime);

		// 从 hedgingBets 过滤能在 hedgingBets 匹配的赛事
		for (GameDTO aGame : gameBets) {
			List<GameDTO> bGames = hedgingMap.get(aGame.getOpenTime());
			if (bGames != null) {
				// 匹配队伍名（假设同一时间，同一只队伍不能同时存在两场比赛）
				final String teamHome = aGame.teamHome, teamClient = aGame.teamClient;
				GameDTO bGame = CollectionUtil.findFirst(bGames, b ->
						(teamHome.contains(b.teamHome) || b.teamHome.contains(teamHome)
								|| teamClient.contains(b.teamHome) || b.teamClient.contains(teamClient))
								&& aGame.league.equals(b.league) // 要求联赛名称一致
				);
				if (bGame != null) {
					log.debug("队伍名匹配成功：{}-{}\t{}-{}", teamHome, teamClient, bGame.teamHome, bGame.teamClient);
					gameMapper.put(aGame, bGame);
				} else {
					// if (aGame.teamHome.equals("阿尔菲斯") && aGame.teamClient.equals("阿尔艾利吉达")) {
					// 	System.out.println("teamHome: " + teamHome + " teamClient: " + teamClient);
					// }
					// 匹配联赛名称（仅一场时则认为是正确的）
					final List<GameDTO> games_ = CollectionUtil.filter(bGames, b -> aGame.league.equals(b.league));
					// 尝试匹配前后 2,3 个字符
					final GameDTO bGameDTO = matchPrefixOrSuffix(games_, teamHome), bGameDTO2 = matchPrefixOrSuffix(games_, teamClient);
					if (bGameDTO != null && bGameDTO.equals(bGameDTO2)) {
						gameMapper.put(aGame, bGameDTO);
						log.debug("前缀匹配成功：{}-{}\t{}-{}\t{}-{}", teamHome, teamClient, bGameDTO.teamHome, bGameDTO.teamClient,
								bGameDTO2.teamHome, bGameDTO2.teamClient);
						continue;
					}

					// 查找别名库
					final GameDTO matchedGame = TeamMatcher.findMatchedGame(aGame, games_);
					if (matchedGame != null) {
						gameMapper.put(aGame, matchedGame);
						log.debug("查找别名库匹配成功：{}-{}\t{}-{}", teamHome, teamClient, matchedGame.teamHome, matchedGame.teamClient);
						continue;
					}

					log.warn("无法匹配赛事：aGame={}\n{}", Jsons.encodeRaw(aGame), Jsons.encode(bGames));
				}
			}
		}
		return gameMapper;
	}

	public GameDTO matchPrefixOrSuffix(List<GameDTO> games_, String team) {
		final String[] fix = parseLeaguePerfixAndSuffix(team);
		if (fix != null) {
			boolean is3 = team.length() == 3;
			List<GameDTO> gameDTOS = CollectionUtil.filter(games_, b -> {
						if (is3) { // 三个字，且首尾相同
							final String word1 = team.substring(0, 1), word2 = team.substring(2, 3);
							if (b.teamHome.startsWith(word1) && b.teamClient.endsWith(word2)
									|| (b.teamClient.startsWith(word1) && b.teamClient.endsWith(word2))) {
								return true;
							}
						}
						// 前后 2,3 个字符匹配
						return ArrayUtil.matchAny(f -> b.teamHome.contains(f), fix)
								|| ArrayUtil.matchAny(f -> b.teamClient.contains(f), fix);
					}
			);
			return gameDTOS.size() == 1 ? gameDTOS.get(0) : null;
		}
		return null;
	}

	@Nullable
	public String[] parseLeaguePerfixAndSuffix(String team) {
		final int len = team.length();
		if (len <= 2) {
			return len == 2 ? new String[] { team } : null;
		}
		final String[] fix = new String[2];
		if (len > 3) {
			fix[0] = team.substring(0, 3);
			fix[1] = team.substring(len - 3);
		} else {
			fix[0] = team.substring(0, 2);
			fix[1] = team.substring(len - 2);
		}
		return fix;
	}

	static final ThreadPoolExecutor subTaskThreadPool = TaskUtils.newThreadPool(4, 4
			, 2048, "game-bet-match-", new ThreadPoolExecutor.AbortPolicy());

	public HedgingDTO[] match(Map<GameDTO, GameDTO> gameMapper, int parlaysSize, int topSize) {
		// 目前仅支持二串一，三串一，4个以上串子所需算力过大
		if (parlaysSize < 2 || parlaysSize > 3) {
			throw new IllegalArgumentException("串子大小参数错误：" + parlaysSize);
		}

		final GameDTO[] aGames = gameMapper.keySet().toArray(new GameDTO[0]);

		Arrays.sort(aGames, Comparator.comparingLong(GameDTO::openTimeMs));
		log.info("开始并行匹配（仅第一层异步）：{}串1，共{}场比赛", parlaysSize, aGames.length);

		// 线程级别的TopN注册表（将ThreadLocal暴露出来）
		final ConcurrentMap<Thread, LocalTopNArray> THREAD_TOP_N_REGISTRY = new ConcurrentHashMap<>();

		// 每个线程初始化时创建
		final ThreadLocal<LocalTopNArray> THREAD_LOCAL_TOP = ThreadLocal.withInitial(() -> {
			final LocalTopNArray topN = new LocalTopNArray(topSize);
			THREAD_TOP_N_REGISTRY.put(Thread.currentThread(), topN);
			return topN;
		});

		final List<Future<Boolean>> futures = new ArrayList<>();
		for (int i = 0; i < aGames.length; i++) {
			final int idx = i;
			futures.add(subTaskThreadPool.submit(() -> {
				// 线程私有 TopN（同线程所有任务共享）
				final LocalTopNArray localTop = THREAD_LOCAL_TOP.get();

				// 第一层逻辑与 match 完全一致
				final GameDTO aGame = aGames[idx];

				calcPathHedgingOdds(gameMapper, aGames, new Odds[parlaysSize], 0, parlaysSize, localTop, aGame, idx);
				return true;
			}));
		}

		// 等待全部任务完成
		for (Future<Boolean> f : futures) {
			try {
				f.get();
			} catch (Exception e) {
				log.error("匹配串子路径异常：", e);
			}
		}

		int totalSize = 0;
		long counter = 0;
		// 只统计大小
		for (LocalTopNArray topN : THREAD_TOP_N_REGISTRY.values()) {
			totalSize += topN.getResult().length;
		}

		// 合并到一个大数组
		final HedgingDTO[] merged = new HedgingDTO[totalSize];

		// 获取全部计算结果
		int pos = 0;
		for (LocalTopNArray topN : THREAD_TOP_N_REGISTRY.values()) {
			counter += topN.getCounter();
			HedgingDTO[] arr = topN.getResult();
			if (arr.length > 0) {
				System.arraycopy(arr, 0, merged, pos, arr.length);
				pos += arr.length;
			}
		}

		log.info("共计进行了{}个组合的计算", counter);

		// 按 avgProfit 倒序（最高分在最前）
		Arrays.sort(merged, Comparator.comparingDouble((HedgingDTO o) -> o.avgProfit).reversed());

		// 截断保留 TopN
		return merged.length > topSize ? Arrays.copyOfRange(merged, 0, topSize) : merged;
	}

	/**
	 * 回溯核心算法
	 *
	 * @param gameMapper A、B平台游戏映射
	 * @param aGames A平台 第一层游戏列表
	 * @param start 当前遍历赛事的起始索引
	 * @param path 当前路径
	 * @param depth 当前路径深度
	 * @param parlaysSize 串子大小
	 * @param localTop TopN 缓存
	 */
	private void backtrackParallel(Map<GameDTO, GameDTO> gameMapper, GameDTO[] aGames, int start,
	                               Odds[] path, int depth, int parlaysSize, LocalTopNArray localTop) {
		// 递归终止条件：已达到要求的串子大小
		if (depth == parlaysSize) {
			// 由于 currPath 会继续进行回溯，这里进行精准拷贝（长度固定，非常快）
			final Odds[] snapshot = Arrays.copyOf(path, parlaysSize);
			localTop.tryAddAndCounter(new HedgingDTO(snapshot));
			return;
		}

		for (int i = start; i < aGames.length; i++) {
			final GameDTO aGame = aGames[i];
			// 时间约束剪枝
			if (depth > 0 && isValidTimeGap(path, depth, aGame)) {
				continue;
			}
			// 计算对冲路径
			calcPathHedgingOdds(gameMapper, aGames, path, depth, parlaysSize, localTop, aGame, i);
		}
	}

	/**
	 * 计算路径对冲赔率
	 *
	 * @param gameMapper A、B平台游戏映射
	 * @param aGames A平台 第一层游戏列表
	 * @param path 当前路径
	 * @param depth 当前路径深度
	 * @param parlaysSize 串子大小
	 * @param localTop TopN 缓存
	 * @param aGame 当前层A平台游戏
	 * @param idx 当前层指针
	 */
	private void calcPathHedgingOdds(Map<GameDTO, GameDTO> gameMapper, GameDTO[] aGames, Odds[] path, int depth, int parlaysSize, LocalTopNArray localTop, GameDTO aGame, int idx) {
		final List<OddsInfo> aOddsList = aGame.odds;
		for (int oddsIdx = 0, len = aOddsList.size(); oddsIdx < len; oddsIdx++) {
			final OddsInfo aOdd = aOddsList.get(oddsIdx);
			if (aOdd == null || !aOdd.type.open) { // 跳过未开放的赔率盘口
				continue;
			}

			final Double[] rates = aOdd.getRates();
			// 遍历该盘口下的所有赔率
			for (int parlaysIdx = 0; parlaysIdx < rates.length; parlaysIdx++) {
				final GameDTO bGame = gameMapper.get(aGame); // 查找对冲平台对应游戏
				final OddsInfo bOdds = bGame.findOdds(aOdd); // 查找对应赔率数据
				// 如果对冲平台找不到该盘口，或者该盘口已关闭，放弃这个组合
				if (bOdds == null || bOdds.getRates() == null || !bOdds.type.open) {
					continue;
				}

				// 如果是对冲，通常取对方平台的相反侧索引，这里保留你的原逻辑映射
				final int hedgingIdx = parlaysIdx == 0 ? 1 : 0;

				final Odds oddsNode = new Odds(rates[parlaysIdx], bOdds.getRates()[hedgingIdx])
						.initGame(aGame, bGame, oddsIdx, hedgingIdx, parlaysIdx);
				// 记录当前比赛的开赛时间，用于下层递归校验
				oddsNode.setGameOpenTime(aGame.openTimeMs());

				// 将赔率盘口记录到组合中
				path[depth] = oddsNode;

				// --- 递归下一层：传递 i + 1 确保不选重复比赛 ---
				backtrackParallel(gameMapper, aGames, idx + 1, path, depth + 1, parlaysSize, localTop);

				// DFS回溯算法：清理当前节点状态，供循环的下一个分支使用（每层只清理当前层级的节点状态）
				// currentPath.remove(currentPath.size() - 1);
			}
		}
	}

	/** 赛事最小时间间隔 */
	static final long LIMIT_MIN = 1000 * 60 * 60,
	/** 赛事最大时间间隔 */
	LIMIT_MAX = 1000 * 60 * 60 * 24 * 2;

	/** 两场赛事之间的时间间隔 */
	private boolean isValidTimeGap(Odds[] path, int depth, GameDTO nextGame) {
		final long lastGameTime = path[depth - 1].getGameOpenTime();
		final long diff = nextGame.openTimeMs() - lastGameTime;
		// 1小时到2天之间
		return diff < LIMIT_MIN || diff > LIMIT_MAX;
	}

}
