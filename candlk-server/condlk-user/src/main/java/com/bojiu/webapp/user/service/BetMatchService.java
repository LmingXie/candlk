package com.bojiu.webapp.user.service;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import javax.annotation.Resource;

import com.bojiu.common.context.Env;
import com.bojiu.common.redis.RedisUtil;
import com.bojiu.context.web.Jsons;
import com.bojiu.context.web.TaskUtils;
import com.bojiu.webapp.user.dto.*;
import com.bojiu.webapp.user.dto.GameDTO.OddsInfo;
import com.bojiu.webapp.user.dto.HedgingDTO.Odds;
import com.bojiu.webapp.user.model.BetProvider;
import com.bojiu.webapp.user.vo.HedgingVO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.*;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import static com.bojiu.webapp.base.entity.Merchant.PLATFORM_ID;
import static com.bojiu.webapp.user.model.MetaType.base_rate_config;
import static com.bojiu.webapp.user.model.UserRedisKey.GAME_BETS_PERFIX;
import static com.bojiu.webapp.user.utils.StringSimilarityUtils.similarity;
import static com.bojiu.webapp.user.utils.StringSimilarityUtils.similarityMetaphone;

@Slf4j
@Service
public class BetMatchService {

	@Resource
	MetaService metaService;

	static Cache<BetProvider, List<GameDTO>> cache = Caffeine.newBuilder()
			.initialCapacity(BetProvider.CACHE.length)
			.maximumSize(1024)
			.expireAfterAccess(30, TimeUnit.SECONDS)
			.build();
	static Function<BetProvider, List<GameDTO>> findGameBuilder = k -> Jsons.parseArray(RedisUtil.opsForHash().get(GAME_BETS_PERFIX, k.name()), GameDTO.class);

	public final HedgingVO calcMuti(String value, Date now) {
		final HedgingVO vo = HedgingVO.ofAndInfer(value, now);

		final Odds[] parlays = vo.getParlays();
		final BetProvider currAProvider = parlays[0].aGame.betProvider, currBProvider = parlays[0].bGame.betProvider;
		final BetProvider[] allProvider = BetProvider.CACHE;
		final int len = allProvider.length;
		if (len < 2) {
			return vo;
		}
		final long timeNow = now.getTime();
		// 查找最近的赛事
		Odds next = null;
		int nextIdx = -1;
		for (int i = 0, size = parlays.length; i < size; i++) {
			Odds odds = parlays[i];
			if (odds.gameOpenTime > timeNow) {
				next = odds;
				nextIdx = i;
				break;
			}
		}
		if (next == null) { // 没有找到下一场赛事
			return vo;
		}

		// 查找串关平台赛事数据
		final List<GameDTO> gameBets = cache.get(currAProvider, findGameBuilder);
		final List<HedgingVO> extBOdds = new ArrayList<>(len);
		vo.setNextIdx(nextIdx);
		final int bIdx = next.getBIdx();
		final double bRate = next.bRate;

		// 匹配对冲平台赛事数据
		for (final BetProvider hedgingProvider : allProvider) {
			if (hedgingProvider != currAProvider && hedgingProvider != currBProvider) { // 排除当前串关平台
				final Map<GameDTO, GameDTO> gameMap = getGameMap(gameBets, cache.get(hedgingProvider, findGameBuilder));
				// 查找当前串子是否能匹配当前平台的全部赔率
				final GameDTO bGameDTO = gameMap.get(next.aGame);
				if (bGameDTO != null) {
					final OddsInfo bOdds = bGameDTO.findOdds(next.aOdds); // 查找B平台的赔率信息
					if (bOdds != null) {
						final Double[] rates = bOdds.getRates();
						final Double newRate = bIdx > rates.length - 1 ? null : rates[bIdx];
						if (newRate != null && !newRate.equals(bRate)) { // 与当前对冲平台赔率不一致才计算
							final HedgingVO newVo = Jsons.parseObject(value, HedgingVO.class);
							newVo.parlays[nextIdx].bGame = bGameDTO;
							newVo.parlays[nextIdx].bOdds = bOdds; // 替换B平台的赔率信息
							newVo.parlays[nextIdx].bRate = newRate;
							newVo.bRebate = null; // 清空对冲返水
							newVo.pair[1] = bGameDTO.betProvider;
							newVo.calcHedgingCoinsLock(now); // 重新计算赔率信息
							extBOdds.add(newVo);
						}
					}
				}
			}
		}
		vo.setExtBOdds(extBOdds);
		return vo;
	}

	public final Map<GameDTO, GameDTO> getGameMapper(Pair<BetProvider, BetProvider> pair) {
		return getGameMapper(pair, false);
	}

	/** 获取A平台到B平台赛事的映射 */
	public final Map<GameDTO, GameDTO> getGameMapper(Pair<BetProvider, BetProvider> pair, boolean limieEndTime) {
		// 查询平台上的全部赛事和赔率信息
		final List<String> values = RedisUtil.opsForHash().multiGet(GAME_BETS_PERFIX, Arrays.asList(pair.getKey().name(), pair.getValue().name()));
		final List<GameDTO> gameBets = Jsons.parseArray(values.get(0), GameDTO.class),
				hedgingBets = Jsons.parseArray(values.get(1), GameDTO.class);
		return getGameMap(gameBets, hedgingBets, limieEndTime);
	}

	private @NonNull Map<GameDTO, GameDTO> getGameMap(List<GameDTO> gameBets, List<GameDTO> hedgingBets) {
		return getGameMap(gameBets, hedgingBets, false);
	}

	private @NonNull Map<GameDTO, GameDTO> getGameMap(List<GameDTO> gameBets, List<GameDTO> hedgingBets, boolean limieEndTime) {
		// A平台赛事 与 B平台赛事 的映射
		final Map<GameDTO, GameDTO> gameMapper = new HashMap<>(Math.max(gameBets.size(), hedgingBets.size()), 1F);
		// 根据开赛时间分组（由于时间是更大的尺度，因此先匹配时间，再匹配队伍名称）
		final Map<Date, List<GameDTO>> hedgingMap = CollectionUtil.groupBy(hedgingBets, GameDTO::getOpenTime);
		final boolean isTest = Env.inTest();

		// 只保留最近3天的赛事
		final long maxEndTime = limieEndTime ? new EasyDate().beginOf(Calendar.DATE).addDay(3).getTime() : 0;

		// 从 hedgingBets 过滤能在 hedgingBets 匹配的赛事
		for (GameDTO aGame : gameBets) {
			if (limieEndTime && aGame.openTimeMs() >= maxEndTime) {
				continue;
			}
			final List<GameDTO> bGames = hedgingMap.get(aGame.getOpenTime());
			if (bGames != null) {
				// 匹配队伍名（假设同一时间，同一只队伍不能同时存在两场比赛）
				final String teamHomeLower = aGame.teamHomeLower(), teamClientLower = aGame.teamClientLower(), leagueLower = aGame.leagueLower();
				// if (aGame.teamHome.equals("MVV Maastricht") && aGame.teamClient.equals("Jong Utrecht")) {
				// 	System.out.println("teamHome: " + teamHomeLower + " teamClient: " + teamClientLower);
				// }
				final GameDTO bGame = CollectionUtil.findFirst(bGames, b -> {
					final String bTeamHome = b.teamHomeLower(), bTeamClient = b.teamClientLower(), bLeague = b.leagueLower();
					// 两只队伍名称一致则允许联赛名称不一致
					if ((teamHomeLower.equalsIgnoreCase(bTeamHome) && teamClientLower.equalsIgnoreCase(bTeamClient))) {
						return true;
					}
					final boolean homeContains = teamHomeLower.contains(bTeamHome) || bTeamHome.contains(teamHomeLower),
							clientContains = teamClientLower.contains(bTeamClient) || bTeamClient.contains(teamClientLower);

					//  队伍名称存在包含关系，且联赛名称一致
					if ((homeContains || clientContains) && leagueLower.equals(bLeague)) {
						return true;
					}
					// if ("Dordrecht".equalsIgnoreCase(bTeamHome)) {
					// 	System.out.println(1111);
					// }

					final double teamHomeScore = similarity(teamHomeLower, bTeamHome), teamClientScore = similarity(teamClientLower, bTeamClient),
							leagueScore = similarity(leagueLower, bLeague);
					// 高相似度
					if (teamHomeScore >= 0.8 && teamClientScore >= 0.8 && leagueScore >= 0.7) {
						return true;
					}
					// 队伍名和联赛名 存在 包含关系 或 中等相似度
					if (((homeContains || teamHomeScore >= 0.7) && (clientContains || teamClientScore >= 0.7)
							// 存在单边高相似度
							|| (teamHomeScore >= 0.8 && teamClientScore >= 0.6) || (teamHomeScore >= 0.6 && teamClientScore >= 0.8))
							&& (leagueLower.contains(bLeague) || bLeague.contains(leagueLower) || leagueScore >= 0.6)) {
						return true;
					}
					// 发音相似度算法
					final double teamHomeScoreMetaphone = similarityMetaphone(teamHomeLower, bTeamHome), teamClientScoreMetaphone = similarityMetaphone(teamClientLower, bTeamClient),
							leagueScoreMetaphone = similarityMetaphone(leagueLower, bLeague);

					// 发音相似度比较
					return ((homeContains || teamHomeScoreMetaphone >= 0.6) && (clientContains || teamClientScoreMetaphone >= 0.6)
							// 存在单边高相似度
							|| (teamHomeScoreMetaphone >= 0.65 && teamClientScoreMetaphone >= 0.55) || (teamHomeScoreMetaphone >= 0.55 && teamClientScoreMetaphone >= 0.65))
							&& (leagueLower.contains(bLeague) || bLeague.contains(leagueLower) || leagueScoreMetaphone >= 0.6);
				});
				if (bGame != null) {
					// log.debug("队伍名匹配成功：{}-{}\t{}-{}", teamHome, teamClient, bGame.teamHome, bGame.teamClient);
					gameMapper.put(aGame, bGame);
				} else {
					// 匹配联赛名称（仅一场时则认为是正确的）
					final List<GameDTO> games_ = CollectionUtil.filter(bGames, b -> {
						final String leaguedLower = b.leagueLower();
						return leagueLower.equals(leaguedLower) || similarityMetaphone(leagueLower, leaguedLower) > 0.7 || similarityMetaphone(leagueLower, leaguedLower) > 0.7;
					});
					// 尝试匹配前后 2,3 个字符
					final GameDTO bGameDTO = matchPrefixOrSuffix(games_, teamHomeLower), bGameDTO2 = matchPrefixOrSuffix(games_, teamClientLower);
					if (bGameDTO != null && bGameDTO.equals(bGameDTO2)) {
						gameMapper.put(aGame, bGameDTO);
						// log.debug("前缀匹配成功：{}-{}\t{}-{}\t{}-{}", teamHome, teamClient, bGameDTO.teamHome, bGameDTO.teamClient,
						// 		bGameDTO2.teamHome, bGameDTO2.teamClient);
						continue;
					}

					// 查找别名库
					final GameDTO matchedGame = TeamMatcher.findMatchedGame(aGame, games_);
					if (matchedGame != null) {
						gameMapper.put(aGame, matchedGame);
						// log.debug("查找别名库匹配成功：{}-{}\t{}-{}", teamHome, teamClient, matchedGame.teamHome, matchedGame.teamClient);
						continue;
					}

					if (!isTest && CollectionUtil.findFirst(bGames, b ->
							// 队伍名称存在包含关系
							(teamHomeLower.contains(b.teamHomeLower) || b.teamHomeLower.contains(teamHomeLower)
									|| teamClientLower.contains(b.teamClientLower) || b.teamClientLower.contains(teamClientLower))
					) != null) {
						aGame.setOdds(null);
						for (GameDTO game : bGames) {
							game.setOdds(null);
						}
						log.debug("无法匹配赛事：aGame={}\n{}", Jsons.encodeRaw(aGame), Jsons.encode(bGames));
					}
				}
			}
		}
		return gameMapper;
	}

	public GameDTO matchPrefixOrSuffix(List<GameDTO> games_, String team) {
		final String[] fix = parseLeaguePerfixAndSuffix(team);
		if (fix != null) {
			final boolean is3 = team.length() == 3;
			final List<GameDTO> gameDTOS = CollectionUtil.filter(games_, b -> {
						if (is3) { // 三个字，且首尾相同
							final String word1 = team.substring(0, 1), word2 = team.substring(2, 3);
							if (b.teamHome.startsWith(word1) && b.teamClient.endsWith(word2)
									|| (b.teamClient.startsWith(word1) && b.teamClient.endsWith(word2))) {
								return true;
							}
						}
						// 前后 2,3 个字符匹配
						return ArrayUtil.matchAny(f -> b.teamHome.contains(f), fix)
								&& ArrayUtil.matchAny(f -> b.teamClient.contains(f), fix);
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

	public Pair<HedgingDTO[], Long> match(Map<GameDTO, GameDTO> gameAllMapper, int parlaysSize, int topSize, Pair<BetProvider, BetProvider> pair) {
		// 目前仅支持二串一，三串一，4个以上串子所需算力过大
		if (parlaysSize < 2 || parlaysSize > 3) {
			throw new IllegalArgumentException("串子大小参数错误：" + parlaysSize);
		}

		// 线程级别的TopN注册表（将ThreadLocal暴露出来）
		final ConcurrentMap<Thread, LocalTopNArray> THREAD_TOP_N_REGISTRY = new ConcurrentHashMap<>();

		// 每个线程初始化时创建
		final ThreadLocal<LocalTopNArray> THREAD_LOCAL_TOP = ThreadLocal.withInitial(() -> {
			final LocalTopNArray topN = new LocalTopNArray(topSize);
			THREAD_TOP_N_REGISTRY.put(Thread.currentThread(), topN);
			return topN;
		});

		final List<Future<Boolean>> futures = new ArrayList<>();
		final BaseRateConifg baseRateConifg = metaService.getCachedParsedValue(PLATFORM_ID, base_rate_config, BaseRateConifg.class);
		final Map<Long, Map<GameDTO, GameDTO>> splitGroup = new HashMap<>(4, 1F); // 由于过滤了最近3天的数据，因此不会存在超出的问题
		final EasyDate d = new EasyDate();
		for (Map.Entry<GameDTO, GameDTO> entry : gameAllMapper.entrySet()) {
			final GameDTO gameDTO = entry.getKey();
			final Long openTimeMs = gameDTO.openTimeMs();
			d.setTime(openTimeMs).beginOf(Calendar.DATE).setHour(12);

			// 如果在当天 12 点之前，则归到前一天的 12 点
			if (openTimeMs < d.getTime()) {
				d.addDay(-1);
			}
			final long groupBeginTime = d.getTime();

			splitGroup.computeIfAbsent(groupBeginTime, k -> new HashMap<>())
					.put(gameDTO, entry.getValue());
		}
		for (Map.Entry<Long, Map<GameDTO, GameDTO>> entry : splitGroup.entrySet()) {
			final Map<GameDTO, GameDTO> gameMapper = entry.getValue();
			final GameDTO[] aGames = gameMapper.keySet().toArray(new GameDTO[0]);

			Arrays.sort(aGames, Comparator.comparingLong(GameDTO::openTimeMs));
			log.info("开始并行匹配【{}】：{}串1，共{}场比赛", d.setTime(entry.getKey()), parlaysSize, aGames.length);

			for (int i = 0; i < aGames.length; i++) {
				final int idx = i;
				futures.add(subTaskThreadPool.submit(() -> {
					try {
						// 线程私有 TopN（同线程所有任务共享）
						final LocalTopNArray localTop = THREAD_LOCAL_TOP.get();

						// 第一层逻辑与 match 完全一致
						final GameDTO aGame = aGames[idx];

						calcPathHedgingOdds(gameMapper, aGames, new Odds[parlaysSize], 0, parlaysSize, localTop, aGame, idx, baseRateConifg, pair);
					} catch (Exception e) {
						log.error("计算串子时出错：", e);
					}
					return true;
				}));
			}
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

		// 按 avgProfit 倒序（最高分在最前）
		Arrays.sort(merged, Comparator.comparingDouble((HedgingDTO o) -> o.avgProfit).reversed());

		// 截断保留 TopN
		return Pair.of(merged.length > topSize ? Arrays.copyOfRange(merged, 0, topSize) : merged, counter);
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
	                               Odds[] path, int depth, int parlaysSize, LocalTopNArray localTop, BaseRateConifg baseRateConifg,
	                               Pair<BetProvider, BetProvider> pair) {
		// 递归终止条件：已达到要求的串子大小
		if (depth == parlaysSize) {
			// 由于 currPath 会继续进行回溯，这里进行精准拷贝（长度固定，非常快）
			final Odds[] snapshot = Arrays.copyOf(path, parlaysSize);
			localTop.tryAddAndCounter(new HedgingDTO(pair, snapshot, baseRateConifg));
			return;
		}

		for (int i = start; i < aGames.length; i++) {
			final GameDTO nextGame = aGames[i];
			// 时间约束剪枝
			if (depth > 0 && !isValidTimeGap(path, depth, nextGame)) {
				continue;
			}
			// 计算对冲路径
			calcPathHedgingOdds(gameMapper, aGames, path, depth, parlaysSize, localTop, nextGame, i, baseRateConifg, pair);
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
	private void calcPathHedgingOdds(Map<GameDTO, GameDTO> gameMapper, GameDTO[] aGames, Odds[] path, int depth,
	                                 int parlaysSize, LocalTopNArray localTop, GameDTO aGame, int idx, BaseRateConifg baseRateConifg,
	                                 Pair<BetProvider, BetProvider> pair) {
		final List<OddsInfo> aOddsList = aGame.odds;
		for (final OddsInfo aOdd : aOddsList) {
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

				Double rate = bOdds.getRates()[hedgingIdx]; // B平台可能没有此盘口
				if (rate == null) {
					continue;
				}
				final Odds oddsNode = new Odds(rates[parlaysIdx], rate, parlaysIdx)
						.initGame(aGame, bGame, aOdd, bOdds);
				// 记录当前比赛的开赛时间，用于下层递归校验
				oddsNode.setGameOpenTime(aGame.openTimeMs());

				// 将赔率盘口记录到组合中
				path[depth] = oddsNode;

				// --- 递归下一层：传递 i + 1 确保不选重复比赛 ---
				backtrackParallel(gameMapper, aGames, idx + 1, path, depth + 1, parlaysSize, localTop, baseRateConifg, pair);

				// DFS回溯算法：清理当前节点状态，供循环的下一个分支使用（每层只清理当前层级的节点状态）
				// currentPath.remove(currentPath.size() - 1);
			}
		}
	}

	/** 赛事最小时间间隔 */
	static final long LIMIT_MIN = 1000 * 60 * 60 * 2,
	/** 赛事最大时间间隔 */
	LIMIT_MAX = 1000 * 60 * 60 * 24;

	/** 串子赛事必须在同一天 */
	private boolean isValidTimeGap(Odds[] path, int depth, GameDTO nextGame) {
		final long nextOpenTime = nextGame.openTimeMs();
		final long lastGameTime = path[depth - 1].getGameOpenTime();

		// 与前一场比赛间隔大于2小时，且与第一场比赛间隔不超过1天
		return (nextOpenTime - lastGameTime) >= LIMIT_MIN/* && (nextOpenTime - path[0].getGameOpenTime()) <= LIMIT_MAX*/;
	}

}
