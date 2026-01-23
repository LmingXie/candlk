package com.bojiu.webapp.user.job;

import java.text.NumberFormat;
import java.util.*;
import javax.annotation.Resource;

import com.bojiu.common.redis.RedisUtil;
import com.bojiu.webapp.user.dto.GameDTO;
import com.bojiu.webapp.user.dto.HedgingDTO;
import com.bojiu.webapp.user.model.BetProvider;
import com.bojiu.webapp.user.service.BetMatchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;

import static com.bojiu.webapp.user.model.BetProvider.*;
import static com.bojiu.webapp.user.model.UserRedisKey.BET_MATCH_DATA_KEY;

/** 刷新推荐串子组合 */
@Slf4j
@Configuration
public class BetMatchJob {

	@Resource
	BetMatchService betMatchService;

	/** 需要匹配的平台对 */
	static final List<Pair<BetProvider, BetProvider>> matchPair = List.of(
			Pair.of(D1CE, KY),
			Pair.of(D1CE, HG),
			Pair.of(D1CE, PS),

			Pair.of(KY, HG),
			Pair.of(KY, PS)
	);
	static final List<Pair<BetProvider, BetProvider>> matchPair2 = List.of(
			Pair.of(KY, HG),
			Pair.of(KY, PS)
	);
	public static final Map<String, String> ALL_PAIR = new TreeMap<>(), ALL_PAIR2 = new TreeMap<>();

	static {
		for (Pair<BetProvider, BetProvider> pair : matchPair) {
			final BetProvider key = pair.getKey(), value = pair.getValue();
			final String k = key.name() + "-" + value.name(), v = key.getLabel() + "->" + value.getLabel();
			ALL_PAIR.put(k, v);
			if (matchPair2.contains(pair)) {
				ALL_PAIR2.put(k, v);
			}
		}
	}

	@Scheduled(cron = "${service.cron.BetMatchJob:0/10 * * * * ?}")
	public void run() {
		RedisUtil.fastAttemptInLock("bet-match-job", 5 * 1000 * 60, () -> {
			long startTime = System.currentTimeMillis();
			final int parlaysSize = 3; // 串关大小（3场比赛为一组）
			final NumberFormat format = NumberFormat.getInstance();
			for (Pair<BetProvider, BetProvider> pair : matchPair) {
				long beginTime = System.currentTimeMillis();
				final String parlaysProvider = pair.getKey().name(), hedgingProvider = pair.getValue().name();
				// 获取A平台到B平台赛事的映射
				final Map<GameDTO, GameDTO> gameMapper = betMatchService.getGameMapper(pair, true);
				log.info("【{}】->【{}】进行赛事映射完成，总【{}】场。", parlaysProvider, hedgingProvider, gameMapper.size());

				final Pair<HedgingDTO[], Long> topNPair = betMatchService.match(gameMapper, parlaysSize, 1000);
				final HedgingDTO[] topN = topNPair.getKey();
				if (topN.length > 0) {
					for (HedgingDTO dto : topN) {
						dto.toJson();
					}
					// 缓存匹配结果
					RedisUtil.doInTransaction(redisOps -> {
						final ZSetOperations<String, String> opsForZSet = redisOps.opsForZSet();
						final String key = BET_MATCH_DATA_KEY + parlaysProvider + "-" + hedgingProvider;
						redisOps.delete(key); // 删除历史数据
						for (HedgingDTO dto : topN) {
							opsForZSet.add(key, dto.json, dto.avgProfit);
						}
					});
				}

				log.info("【{}】->【{}】匹配结束，共【{}】种组合，计算最佳结果：{}，耗时：{} s", parlaysProvider, hedgingProvider,
						format.format(topNPair.getValue()), topN.length, (System.currentTimeMillis() - beginTime) / 1000D);
			}

			log.info("【刷新推荐串子】完成，耗时：{} s", (System.currentTimeMillis() - startTime) / 1000D);
			return true;
		});
	}

}