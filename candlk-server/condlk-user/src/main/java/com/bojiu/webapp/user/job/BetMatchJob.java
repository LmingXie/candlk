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
			Pair.of(D1CE, HG),
			Pair.of(HG, KY),
			Pair.of(HG, D1CE),
			Pair.of(KY, HG),
			Pair.of(D1CE, KY)
	);
	public static final Map<String, String> ALL_PAIR = new TreeMap<>();

	static {
		for (Pair<BetProvider, BetProvider> pair : matchPair) {
			final BetProvider key = pair.getKey(), value = pair.getValue();
			ALL_PAIR.put(key.name() + "-" + value.name(), key.getLabel() + "->" + value.getLabel());
		}
	}

	@Scheduled(cron = "${service.cron.BetMatchJob:0/20 * * * * ?}")
	public void run() {
		long startTime = System.currentTimeMillis();
		final int parlaysSize = 3; // 串关大小（3场比赛为一组）
		final NumberFormat format = NumberFormat.getInstance();
		for (Pair<BetProvider, BetProvider> pair : matchPair) {
			long beginTime = System.currentTimeMillis();
			final String parlaysProvider = pair.getKey().name(), hedgingProvider = pair.getValue().name();
			// 获取A平台到B平台赛事的映射
			final Map<GameDTO, GameDTO> gameMapper = betMatchService.getGameMapper(pair);
			log.info("【{}】->【{}】进行赛事映射完成，总【{}】场。", parlaysProvider, hedgingProvider, gameMapper.size());

			final Pair<HedgingDTO[], Long> topNPair = betMatchService.match(gameMapper, parlaysSize, 1000);
			final HedgingDTO[] topN = topNPair.getKey();
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

			log.info("【{}】->【{}】匹配结束，共【{}】种组合，计算最佳结果：{}，耗时：{} ms", parlaysProvider, hedgingProvider,
					format.format(topNPair.getValue()), topN.length, System.currentTimeMillis() - beginTime);
		}

		log.info("【刷新推荐串子】完成，耗时：{} ms", System.currentTimeMillis() - startTime);
	}

}