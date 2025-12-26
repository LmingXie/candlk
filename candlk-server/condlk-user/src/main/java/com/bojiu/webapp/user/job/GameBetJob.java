package com.bojiu.webapp.user.job;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.PostConstruct;

import com.bojiu.common.redis.RedisUtil;
import com.bojiu.common.util.SpringUtil;
import com.bojiu.context.web.Jsons;
import com.bojiu.context.web.TaskUtils;
import com.bojiu.webapp.user.bet.BetApi;
import com.bojiu.webapp.user.dto.GameBetQueryDTO;
import com.bojiu.webapp.user.dto.GameDTO;
import com.bojiu.webapp.user.model.BetProvider;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.StringUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.scheduling.annotation.Scheduled;

import static com.bojiu.webapp.user.model.UserRedisKey.BET_SYNC_RELAY;
import static com.bojiu.webapp.user.model.UserRedisKey.GAME_BETS_PERFIX;

@Slf4j
@Configuration
public class GameBetJob {

	@PostConstruct
	public void init() {
		for (BetProvider type : BetProvider.CACHE) {
			BetApi.getInstance(type);
		}
	}

	// 线程数量不宜过多，避免 内存占用过大 以及 增加 资源IO 争用
	static final ThreadPoolExecutor smallTaskThreadPool = TaskUtils.newThreadPool(4, 4
			// 这里的队列容量（ 128 ） 一定不能小于 BetProvider.CACHE.length
			, 128, "game-bet-sync-", new ThreadPoolExecutor.AbortPolicy());

	@Scheduled(cron = "${service.cron.GameBetJob:0/30 * * * * ?}")
	public void run() throws InterruptedException {
		final EnumMap<BetProvider, BetApi> enumMap = BetApi.implMapRef.get();
		final int size = enumMap.size();
		final long startTime = System.currentTimeMillis();
		final CountDownLatch latch = new CountDownLatch(size);
		for (Map.Entry<BetProvider, BetApi> entry : enumMap.entrySet()) {
			final BetApi betApi = entry.getValue();
			smallTaskThreadPool.execute(() -> {
				try {
					doQueryAndSyncGameBetsForSingleVendor(betApi);
				} catch (Throwable e) {
					// 关键业务，上报异常信息
					SpringUtil.logError(log, "同步游戏赔率时出错：厂商=" + betApi.getProvider(), e);
				} finally {
					latch.countDown();
				}
			});
		}
		latch.await(); // 被阻塞，等待唤醒
		log.info("【游戏赔率】同步完成，耗时：{} ms", System.currentTimeMillis() - startTime);
	}

	public void doQueryAndSyncGameBetsForSingleVendor(BetApi gameApi) {
		final BetProvider provider = gameApi.getProvider();
		final String providerName = provider.name();

		final String nextJson = RedisUtil.opsForHash().get(BET_SYNC_RELAY, providerName);
		final GameBetQueryDTO begin = StringUtil.isEmpty(nextJson) ? new GameBetQueryDTO() : Jsons.parseObject(nextJson, GameBetQueryDTO.class);
		if (begin.lastTime == null || begin.lastTime.getTime() + 1000 * 60 < System.currentTimeMillis()) {
			RedisUtil.fastAttemptInLock((BET_SYNC_RELAY + "_" + providerName), 1000 * 60 * 5L, () -> {
				long beginTime = System.currentTimeMillis();
				Set<GameDTO> gameBets = gameApi.getGameBets();
				if (!gameBets.isEmpty()) {
					begin.lastTime = new Date();
					RedisUtil.doInTransaction(redisOps -> {
						HashOperations<String, Object, Object> opsForHash = redisOps.opsForHash();
						opsForHash.put(BET_SYNC_RELAY, providerName, Jsons.encode(begin));
						opsForHash.put(GAME_BETS_PERFIX, providerName, Jsons.encode(gameBets));
					});
					log.info("厂商【{}】同步游戏赔率成功，数量={}，耗时：{} ms", providerName, gameBets.size(), begin.lastTime.getTime() - beginTime);
				}
				return true;
			});
		}

	}

}