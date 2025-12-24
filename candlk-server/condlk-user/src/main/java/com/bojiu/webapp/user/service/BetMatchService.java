package com.bojiu.webapp.user.service;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;

import com.bojiu.context.web.Jsons;
import com.bojiu.context.web.TaskUtils;
import com.bojiu.webapp.user.dto.*;
import com.bojiu.webapp.user.dto.GameDTO.OddsInfo;
import com.bojiu.webapp.user.dto.HedgingDTO.GameRate;
import com.bojiu.webapp.user.dto.HedgingDTO.Odds;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.ArrayUtil;
import me.codeplayer.util.CollectionUtil;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BetMatchService {

	transient Map<GameDTO, GameDTO> gameMapperCache;

	public final Map<GameDTO, GameDTO> getGameMapper(List<GameDTO> gameBets, List<GameDTO> hedgingBets) {
		if (gameMapperCache == null) {
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
							teamHome.contains(b.teamHome) || b.teamHome.contains(teamHome)
									|| teamClient.contains(b.teamHome) || b.teamClient.contains(teamClient)
					);
					if (bGame != null) {
						log.debug("队伍名匹配成功：{}-{}\t{}-{}", teamHome, teamClient, bGame.teamHome, bGame.teamClient);
						gameMapper.put(aGame, bGame);
					} else {
						// 匹配联赛名称（仅一场时则认为是正确的）
						final List<GameDTO> games_ = CollectionUtil.filter(bGames, b -> aGame.league.equals(b.league));
						// 尝试匹配前后 2,3 个字符
						GameDTO bGameDTO = matchPrefixOrSuffix(games_, teamHome), bGameDTO2 = matchPrefixOrSuffix(games_, teamClient);
						if (bGameDTO != null && bGameDTO2 != null) {
							gameMapper.put(aGame, games_.get(0));
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

						log.warn("无法匹配赛事：aGame={}\n，bGames={}", Jsons.encodeRaw(aGame), Jsons.encode(bGames));
					}
				}
			}
			gameMapperCache = gameMapper;
		}
		return gameMapperCache;
	}

	public GameDTO matchPrefixOrSuffix(List<GameDTO> games_, String league) {
		final String[] fix = parseLeaguePerfixAndSuffix(league);
		if (fix != null) {
			List<GameDTO> gameDTOS = CollectionUtil.filter(games_, b ->
					ArrayUtil.matchAny(f -> b.teamHome.contains(f), fix) // 前后2,3 个字符匹配也算命中
							|| ArrayUtil.matchAny(f -> b.teamClient.contains(f), fix)
			);
			return gameDTOS.size() == 1 ? gameDTOS.get(0) : null;
		}
		return null;
	}

	@Nullable
	public String[] parseLeaguePerfixAndSuffix(String league) {
		final int len = league.length();
		if (len <= 2) {
			return len == 2 ? new String[] { league } : null;
		}
		final String[] fix = new String[2];
		if (len > 3) {
			fix[0] = league.substring(0, 3);
			fix[1] = league.substring(len - 3);
		} else {
			fix[0] = league.substring(0, 2);
			fix[1] = league.substring(len - 2);
		}
		return fix;
	}

	static final ThreadPoolExecutor smallTaskThreadPool = TaskUtils.newThreadPool(4, 4
			, 2048, "game-bet-calc-", new ThreadPoolExecutor.AbortPolicy());

	public void match(Map<GameDTO, GameDTO> gameMapper, int parlaysSize/*串子大小*/) {
		if (parlaysSize < 2 || parlaysSize > 3) { // 目前仅支持二串一，三串一，4个以上串子所需算力过大
			throw new IllegalArgumentException("串子大小参数错误：" + parlaysSize);
		}

		final GameDTO[] aGames = gameMapper.keySet().toArray(new GameDTO[0]);
		log.info("开始匹配：" + parlaysSize + "串1 串关，共有" + aGames.length + "场比赛");

		// 按开赛时间排序，优化后续的时间间隔过滤逻辑
		Arrays.sort(aGames, Comparator.comparingLong(GameDTO::openTimeMs));

		// 使用回调处理结果，避免 OOM
		final int[] count = { 0 };
		final Consumer<HedgingDTO> hedgingBuilder = result -> {
			count[0]++;
			final double[] hedgingCoins = result.getHedgingCoins();
			final double avgProfit = result.calcAvgProfit(hedgingCoins);
			if (avgProfit > 0) {
				log.info("估算串关投注方案：{}，平均利润：{}，详细信息：{}", Jsons.encode(hedgingCoins),
						avgProfit, Jsons.encode(result));
			}
		};
		backtrack(gameMapper, aGames, 0, new ArrayList<>(), parlaysSize, hedgingBuilder);
		log.info("匹配完成，共找到{}个结果", count[0]);
	}

	/**
	 * 回溯核心算法
	 *
	 * @param start 当前遍历赛事的起始索引
	 * @param currentPath 已选中的赔率路径
	 */
	private void backtrack(Map<GameDTO, GameDTO> gameMapper, GameDTO[] aGames, int start, List<Odds> currentPath,
	                       int parlaysSize, Consumer<HedgingDTO> resultCollector) {

		// 递归终止条件：已达到要求的串子大小
		if (currentPath.size() == parlaysSize) {
			resultCollector.accept(new HedgingDTO(currentPath.toArray(new Odds[0])));
			return;
		}

		for (int i = start; i < aGames.length; i++) {
			final GameDTO aGame = aGames[i];

			// 时间约束剪枝
			if (!currentPath.isEmpty()) {
				// 拿路径中最后一场比赛做时间比对
				// 注意：由于 aGames 已排序，若当前 i 不满足时间，后续 i 可能满足，故用 continue
				if (isValidTimeGap(currentPath, aGame)) {
					continue;
				}
			}

			final List<OddsInfo> aOddsList = aGame.odds;
			for (int oddsIdx = 0, len = aOddsList.size(); oddsIdx < len; oddsIdx++) {
				final OddsInfo aOdd = aOddsList.get(oddsIdx);
				if (aOdd == null || !aOdd.type.open) {
					continue;
				}

				// 遍历该盘口下的所有赔率
				for (int parlaysIdx = 0; parlaysIdx < aOdd.getRates().length; parlaysIdx++) {
					final Double parlaysRate = aOdd.getRates()[parlaysIdx];

					// 赔率范围过滤
					// if (parlaysRate < 0.8 || parlaysRate > 1.2) { TODO 当前计算次数足够，因此不考虑过滤过大过小赔率赛事
					// 	continue;
					// }

					// 获取对冲平台的对应数据
					final GameDTO bGame = gameMapper.get(aGame);

					// 如果对冲平台找不到该盘口，或者该盘口已关闭，放弃这个组合
					final OddsInfo bOdds = bGame.findOdds(aOdd);
					if (bOdds == null || bOdds.getRates() == null || !bOdds.type.open) {
						// log.debug("B平台缺失对应盘口，跳过：{}-{}-{}-{}", aGame.league, aGame.teamHome, aOdd.ratioRate, aOdd.type.getLabel());
						continue;
					}
					// 逻辑：如果是对冲，通常取对方平台的相反侧索引，这里保留你的原逻辑映射
					final int hedgingIdx = (parlaysIdx == 0) ? 1 : 0;

					final Double hedgingRate = bOdds.getRates()[hedgingIdx];

					// --- 选择当前节点 ---
					final Odds oddsNode = new Odds(parlaysRate, hedgingRate).initGame(
							new GameRate(aGame, oddsIdx, parlaysIdx), new GameRate(bGame, oddsIdx, hedgingIdx));
					// 记录当前比赛的开赛时间，用于下层递归校验
					oddsNode.setGameOpenTime(aGame.openTimeMs());

					currentPath.add(oddsNode);

					// --- 递归下一层：传递 i + 1 确保不选重复比赛 ---
					backtrack(gameMapper, aGames, i + 1, currentPath, parlaysSize, resultCollector);

					// --- 回溯：清理当前节点状态，供循环的下一个分支使用 ---
					currentPath.removeLast();
				}
			}
		}
	}

	static final long limitMin = 1000 * 60 * 60, limitMax = 1000 * 60 * 60 * 24 * 2;

	private boolean isValidTimeGap(List<Odds> path, GameDTO nextGame) {
		final long lastGameTime = path.get(path.size() - 1).getGameOpenTime();
		final long diff = nextGame.openTimeMs() - lastGameTime;
		// 1小时到2天之间
		return diff < limitMin || diff > limitMax;
	}

	public void match2(Map<GameDTO, GameDTO> gameMapper, int parlaysSize) {

		if (parlaysSize < 2 || parlaysSize > 3) {
			throw new IllegalArgumentException("串子大小参数错误：" + parlaysSize);
		}

		final GameDTO[] aGames = gameMapper.keySet().toArray(new GameDTO[0]);

		Arrays.sort(aGames, Comparator.comparingLong(GameDTO::openTimeMs));

		List<Future<LocalTopNArray>> futures = new ArrayList<>();

		for (int i = 0; i < aGames.length; i++) {
			final int start = i;

			futures.add(smallTaskThreadPool.submit(() -> {
				final LocalTopNArray localTop = new LocalTopNArray(1000);
				backtrackParallel(gameMapper, aGames, start,
						new ArrayList<>(4), parlaysSize, localTop);
				return localTop;
			}));
		}

		// ===== 聚合阶段（单线程，量很小）=====
		final int[] totalSize = { 0 };
		final ArrayList<HedgingDTO[]> result = CollectionUtil.toList(futures, f -> {
			try {
				HedgingDTO[] data = f.get().getResult();
				totalSize[0] += data.length;
				return data;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		// 合并到一个大数组
		HedgingDTO[] merged = new HedgingDTO[totalSize[0]];
		int pos = 0;
		for (HedgingDTO[] arr : result) {
			System.arraycopy(arr, 0, merged, pos, arr.length);
			pos += arr.length;
		}
		// 排序（按 avgProfit 升序）
		Arrays.sort(merged, Comparator.comparingDouble(o -> o.avgProfit));

		// 截断保留 TopN
		final int globalTopN = 1000;
		HedgingDTO[] globalTop = merged.length > globalTopN ? Arrays.copyOfRange(merged, merged.length - globalTopN, merged.length) : merged;

		log.info("最终 Top1000 计算完成：{}", Jsons.encode(globalTop));
	}

	private void backtrackParallel(Map<GameDTO, GameDTO> gameMapper, GameDTO[] aGames, int start,
	                               List<Odds> currentPath, int parlaysSize, LocalTopNArray localTop) {

		// 递归终止条件：已达到要求的串子大小
		if (currentPath.size() == parlaysSize) {
			localTop.tryAdd(new HedgingDTO(currentPath.toArray(new Odds[0])));
			return;
		}

		for (int i = start; i < aGames.length; i++) {
			GameDTO aGame = aGames[i];

			// 时间约束剪枝
			if (!currentPath.isEmpty() && isValidTimeGap(currentPath, aGame)) {
				continue;
			}

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

					final Odds oddsNode = new Odds(
							rates[parlaysIdx],
							bOdds.getRates()[hedgingIdx]
					).initGame(
							new GameRate(aGame, oddsIdx, parlaysIdx),
							new GameRate(bGame, oddsIdx, hedgingIdx)
					);
					// 记录当前比赛的开赛时间，用于下层递归校验
					oddsNode.setGameOpenTime(aGame.openTimeMs());
					// 将赔率盘口记录到组合中
					currentPath.add(oddsNode);

					// --- 递归下一层：传递 i + 1 确保不选重复比赛 ---
					backtrackParallel(gameMapper, aGames, i + 1,
							currentPath, parlaysSize, localTop);

					// --- 回溯：清理当前节点状态，供循环的下一个分支使用 ---
					currentPath.removeLast();
				}
			}
		}
	}

	public static final class LocalTopNArray {

		/** TopN排名数组 */
		private final HedgingDTO[] topN;
		/** 超容量缓存区 */
		private final HedgingDTO[] tempBuffer;
		/** 超容量缓存区大小 */
		private static final int TEMP_CAPACITY = 10000;
		/** 容忍的初始最低分 */
		private static final double minScoreLimit = -200;
		/** TopN容量 */
		private final int capacity;
		/** 当前TopN容量 */
		private int size = 0,
		/** 当前临时缓存区容量 */
		tempSize = 0;

		/** 是否已进入超容量阶段 */
		private boolean isSuperSize = false;

		/** 当前 TopN 的最低分（仅在 isSuperSize=true 时有效） */
		private double minScore = minScoreLimit;

		public LocalTopNArray(int capacity) {
			this.capacity = capacity;
			this.topN = new HedgingDTO[capacity];
			this.tempBuffer = new HedgingDTO[TEMP_CAPACITY];
		}

		public void tryAdd(HedgingDTO dto) {
			final double score = dto.calcAvgProfitAndCache(dto.getHedgingCoins());
			if (score < minScoreLimit) {
				return;
			}

			// 未满容量阶段：直接追加，不排序
			if (!isSuperSize) {
				topN[size++] = dto;

				if (size == capacity) {
					// 首次满容量：排序并进入超容量模式
					Arrays.sort(topN, Comparator.comparingDouble(o -> o.avgProfit));
					minScore = topN[0].avgProfit;
					isSuperSize = true;
					log.info("达到额定容量，进入超容量模式：minScore={}", minScore);
				}
				return;
			}

			// 超容量阶段：只缓存可能进入 TopN 的
			if (score <= minScore) {
				return;
			}

			tempBuffer[tempSize++] = dto;

			// tempBuffer 满：合并 + 重新计算 TopN
			if (tempSize == TEMP_CAPACITY) {
				mergeTemp();
			}
		}

		/** 合并 tempBuffer 到 topN，并重算 TopN */
		private void mergeTemp() {
			// 合并到一个新数组
			final HedgingDTO[] merged = new HedgingDTO[capacity + tempSize];
			System.arraycopy(topN, 0, merged, 0, capacity);
			System.arraycopy(tempBuffer, 0, merged, capacity, tempSize);

			// 排序
			Arrays.sort(merged, Comparator.comparingDouble(o -> o.avgProfit));

			// 截取 TopN
			System.arraycopy(merged, merged.length - capacity, topN, 0, capacity);

			// 重置最低分和缓冲区容量
			minScore = topN[0].avgProfit;
			tempSize = 0;
			log.info("合并 tempBuffer 到 topN：minScore={}", minScore);
		}

		/** 取最终 TopN 结果 */
		public HedgingDTO[] getResult() {
			if (!isSuperSize) {
				return Arrays.copyOf(topN, size);
			}

			if (tempSize > 0) {
				mergeTemp();
			}
			return Arrays.copyOf(topN, capacity);
		}

	}

}
