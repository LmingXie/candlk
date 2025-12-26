package com.bojiu.webapp.user.job;

import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import com.bojiu.common.redis.RedisUtil;
import com.bojiu.common.util.SpringUtil;
import com.bojiu.context.web.Jsons;
import com.bojiu.context.web.TaskUtils;
import com.bojiu.webapp.user.bet.BetApi;
import com.bojiu.webapp.user.dto.*;
import com.bojiu.webapp.user.model.BetProvider;
import com.bojiu.webapp.user.service.BetMatchService;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.NumberUtil;
import me.codeplayer.util.StringUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.scheduling.annotation.Scheduled;

import static com.bojiu.webapp.user.model.BetProvider.*;
import static com.bojiu.webapp.user.model.UserRedisKey.*;

/** 刷新推荐串子组合 */
@Slf4j
@Configuration
public class BetMatchJob {

	@Resource
	BetMatchService betMatchService;

	/** 需要匹配的平台对 */
	static final List<Pair<BetProvider, BetProvider>> matchPair = List.of(
			Pair.of(HG, KY),
			Pair.of(HG, D1CE),
			Pair.of(D1CE, KY)
	);

	@Scheduled(cron = "${service.cron.BetMatchJob:0 0/3 * * * ?}")
	public void run() {
		long startTime = System.currentTimeMillis();
		final int parlaysSize = 3; // 串关大小（3场比赛为一组）
		final NumberFormat format = NumberFormat.getInstance();
		for (Pair<BetProvider, BetProvider> pair : matchPair) {
			long beginTime = System.currentTimeMillis();
			final String parlaysProvider = pair.getKey().name(), hedgingProvider = pair.getValue().name();
			// 获取A平台到B平台赛事的映射
			final Map<GameDTO, GameDTO> gameMapper = betMatchService.getGameMapper(pair);
			log.info("【{}】->【{}】进行赛事映射完成，总：{}", parlaysProvider, hedgingProvider, gameMapper.size());

			final Pair<HedgingDTO[], Long> topNPair = betMatchService.match(gameMapper, parlaysSize, 1000);
			// 缓存匹配结果
			final HedgingDTO[] topN = topNPair.getKey();
			RedisUtil.opsForHash().put(BET_MATCH_DATA_KEY, parlaysProvider + "-" + hedgingProvider, Jsons.encode(topN));

			log.info("【{}】->【{}】匹配结束，共【{}】种组合，计算最佳结果：{}，耗时：{} ms", parlaysProvider, hedgingProvider,
					format.format(topNPair.getValue()), topN.length, System.currentTimeMillis() - beginTime);
		}

		log.info("【刷新推荐串子】完成，耗时：{} ms", System.currentTimeMillis() - startTime);
	}

}