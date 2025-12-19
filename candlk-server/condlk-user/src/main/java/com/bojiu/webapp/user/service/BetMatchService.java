package com.bojiu.webapp.user.service;

import java.util.*;
import java.util.function.Consumer;

import com.bojiu.context.web.Jsons;
import com.bojiu.webapp.user.dto.*;
import com.bojiu.webapp.user.dto.GameDTO.OddsInfo;
import com.bojiu.webapp.user.dto.HedgingDTO.GameRate;
import com.bojiu.webapp.user.dto.HedgingDTO.Odds;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.CollectionUtil;
import me.codeplayer.util.EasyDate;
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
					GameDTO bGame = CollectionUtil.findFirst(bGames, b ->
							aGame.teamHome.contains(b.teamHome) || aGame.teamClient.contains(b.teamHome)
									|| b.teamHome.contains(aGame.teamClient) || b.teamClient.contains(aGame.teamClient)
					);
					if (bGame != null) {
						bGames.remove(bGame);
						gameMapper.put(aGame, bGame);
					} else {
						// 匹配联赛名称（仅一场时则认为是正确的）
						final List<GameDTO> games_ = CollectionUtil.filter(bGames, b -> aGame.league.equals(b.league));
						if (games_.size() == 1) {
							gameMapper.put(aGame, games_.get(0));
							continue;
						} else {
							// 查找别名库
							final GameDTO matchedGame = TeamMatcher.findMatchedGame(aGame, games_);
							if (matchedGame != null) {
								gameMapper.put(aGame, matchedGame);
								continue;
							}
						}
						log.warn("无法匹配赛事：aGame={}\n，bGames={}", Jsons.encodeRaw(aGame), Jsons.encode(bGames));
					}
				} else {
					log.info("无法匹配赛事：aGame={}", Jsons.encode(aGame));
				}
			}
			for (Map.Entry<GameDTO, GameDTO> entry : gameMapper.entrySet()) {
				GameDTO key = entry.getKey();
				GameDTO value = entry.getValue();
				if (!key.teamHome.contains(value.teamHome)
						|| !key.teamClient.contains(value.teamClient)) {
					log.info("名称不匹配的球队：{}\t{},{}\t{},{}\t{},{}", new EasyDate(key.openTime).toDateTimeString(), key.league, value.league, key.teamHome, value.teamHome, key.teamClient, value.teamClient);
				}
			}
			gameMapperCache = gameMapper;
		}
		return gameMapperCache;
	}

	public void match(Map<GameDTO, GameDTO> gameMapper, int parlaysSize/*串子大小*/) {
		if (parlaysSize < 2 || parlaysSize > 3) { // 目前仅支持二串一，三串一，4个以上串子所需算力过大
			throw new IllegalArgumentException("串子大小参数错误：" + parlaysSize);
		}

		GameDTO[] aGames = gameMapper.keySet().toArray(new GameDTO[0]);
		log.info("开始匹配：" + parlaysSize + "串1 串关，共有" + aGames.length + "场比赛");

		// 按开赛时间排序，优化后续的时间间隔过滤逻辑
		Arrays.sort(aGames, Comparator.comparingLong(GameDTO::openTimeMs));

		// 使用回调处理结果，避免 OOM
		int[] count = { 0 };
		final Consumer<HedgingDTO> hedgingBuilder = result -> {
			count[0]++;
			double[] hedgingCoins = result.getHedgingCoins();
			double avgProfit = result.calcAvgProfit(hedgingCoins);
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
	private void backtrack(Map<GameDTO, GameDTO> gameMapper,
	                       GameDTO[] aGames,
	                       int start,
	                       List<Odds> currentPath,
	                       int parlaysSize,
	                       Consumer<HedgingDTO> resultCollector) {

		// 递归终止条件：已达到要求的串子大小
		if (currentPath.size() == parlaysSize) {
			resultCollector.accept(new HedgingDTO(currentPath.toArray(new Odds[0])));
			return;
		}

		for (int i = start; i < aGames.length; i++) {
			GameDTO aGame = aGames[i];

			// --- 约束条件过滤 ---
			if (!currentPath.isEmpty()) {
				// 拿路径中最后一场比赛做时间比对
				// 注意：由于 aGames 已排序，若当前 i 不满足时间，后续 i 可能满足，故用 continue
				if (!isValidTimeGap(currentPath, aGame)) {
					continue;
				}
			}

			List<OddsInfo> aOddsList = aGame.odds;
			for (int oddsIdx = 0, len = aOddsList.size(); oddsIdx < len; oddsIdx++) {
				OddsInfo aOdd = aOddsList.get(oddsIdx);
				if (aOdd == null || !aOdd.type.open) {
					continue;
				}

				// 遍历该盘口下的所有赔率（不再硬编码 0 和 1）
				for (int parlaysIdx = 0; parlaysIdx < aOdd.getRates().length; parlaysIdx++) {
					Double parlaysRate = aOdd.getRates()[parlaysIdx];

					// 赔率范围过滤
					// if (parlaysRate < 0.8 || parlaysRate > 1.2) { TODO 当前计算次数足够，因此不考虑过滤过大过小赔率赛事
					// 	continue;
					// }

					// 获取对冲平台的对应数据
					GameDTO bGame = gameMapper.get(aGame);
					// 逻辑：如果是对冲，通常取对方平台的相反侧索引，这里保留你的原逻辑映射
					int hedgingIdx = (parlaysIdx == 0) ? 1 : 0;

					// 关键点：如果对冲平台找不到该盘口，或者该盘口已关闭，放弃这个组合
					OddsInfo bOdds = bGame.findOdds(aOdd);
					if (bOdds == null || bOdds.getRates() == null || !bOdds.type.open) {
						// log.debug("B平台缺失对应盘口，跳过：{}-{}-{}-{}", aGame.league, aGame.teamHome, aOdd.ratioRate, aOdd.type.getLabel());
						continue;
					}
					Double hedgingRate = bOdds.getRates()[hedgingIdx];

					// --- 选择当前节点 ---
					Odds oddsNode = new Odds(parlaysRate, hedgingRate).initGame(
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

	private boolean isValidTimeGap(List<Odds> path, GameDTO nextGame) {
		final long lastGameTime = path.get(path.size() - 1).getGameOpenTime();
		final long diff = nextGame.openTimeMs() - lastGameTime;
		// 1小时到5天之间
		return diff >= 3600_000L && diff <= 432_000_000L;
	}

}
