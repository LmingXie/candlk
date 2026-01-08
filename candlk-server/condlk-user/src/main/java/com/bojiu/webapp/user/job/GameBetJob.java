package com.bojiu.webapp.user.job;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

import com.bojiu.common.redis.RedisUtil;
import com.bojiu.common.util.SpringUtil;
import com.bojiu.context.web.Jsons;
import com.bojiu.context.web.TaskUtils;
import com.bojiu.webapp.user.bet.BetApi;
import com.bojiu.webapp.user.dto.*;
import com.bojiu.webapp.user.dto.GameDTO.OddsInfo;
import com.bojiu.webapp.user.model.BetProvider;
import com.bojiu.webapp.user.vo.HedgingVO;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;

import static com.bojiu.webapp.user.model.UserRedisKey.*;

@Slf4j
@Configuration
public class GameBetJob {

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
			if (!entry.getKey().open) {
				latch.countDown();
				continue;
			}
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
		if (begin.lastTime == null || begin.lastTime.getTime() + 1000 * 30 < System.currentTimeMillis()) {
			// RedisUtil.fastAttemptInLock((BET_SYNC_RELAY + "_" + providerName), 1000 * 60 * 3L, () -> {
				final long beginTime = System.currentTimeMillis();
				final Set<GameDTO> gameBets = gameApi.getGameBets();
				if (gameBets != null && !gameBets.isEmpty()) {
					begin.lastTime = new Date();
					final List<Object> objects = RedisUtil.execInTransaction(redisOps -> {
						final HashOperations<String, Object, Object> opsForHash = redisOps.opsForHash();
						final ZSetOperations<String, String> opsForZSet = redisOps.opsForZSet();
						opsForHash.put(BET_SYNC_RELAY, providerName, Jsons.encode(begin));
						opsForHash.put(GAME_BETS_PERFIX, providerName, Jsons.encode(gameBets));
						// 查询正在进行的串子
						opsForZSet.reverseRangeByScore(HEDGING_LIST_KEY, DEFAULT_MIN_SCORE, DEFAULT_MAX_SCORE, 0, 1000);
					});
					final Set<String> hedgingList = X.castType(objects.get(2));
					if (!hedgingList.isEmpty()) {
						try {
							final Map<Long, ScoreResult> scoreResult = gameApi.getScoreResult();
							this.flushHedgingBet(provider, gameBets, hedgingList, scoreResult);
						} catch (Throwable e) {
							log.error("厂商【{}】刷新正在进行中的串子赔率异常", providerName, e);
						}
					}
					log.info("厂商【{}】同步游戏赔率成功，数量={}，耗时：{} ms", providerName, gameBets.size(), begin.lastTime.getTime() - beginTime);
				}
				// return true;
			// });
		}
	}

	/** 刷新正在进行中的串子赔率/结算赛果 */
	public void flushHedgingBet(BetProvider provider, Set<GameDTO> gameBets, Set<String> hedgingList, Map<Long, ScoreResult> scoreResult) {
		Date now = new Date();
		final Map<Long, String> updates = new HashMap<>(hedgingList.size(), 1F);
		// 查询全部正在进行的串子
		for (String json : hedgingList) {
			final HedgingVO vo = HedgingVO.ofAndFlush(json);
			for (HedgingDTO.Odds parlay : vo.parlays) {
				final GameDTO bGame = parlay.bGame;
				final boolean isA = parlay.aGame.betProvider == provider, isB = bGame.betProvider == provider;
				if (isB || isA) {
					final ScoreResult result = scoreResult.get((isA ? parlay.aGame : bGame).getId()); // 查找赛果
					if (result != null) {
						vo.update = parlay.settle(result, isA); // 结算赛果
					}
				}
				if (!isB/*厂家不匹配*/ || X.isValid(parlay.lock)/*被锁定*/ || now.after(bGame.openTime) /*已开赛*/) {
					continue;
				}
				// 根据id匹配并更新bGame中的赔率（aGame为串子赔率，创建后将不再更新）
				GameDTO dto = CollectionUtil.findFirst(gameBets, game -> game.getId() != null && game.getId().equals(bGame.getId()));
				if (dto != null) {
					final OddsInfo oldOdds = parlay.bOdds;
					OddsInfo newOdds = dto.findOdds(oldOdds); // 查找新的赔率信息
					if (newOdds == null) {
						log.warn("未找到对应新的赔率信息：赛事ID={}，赔率类型={}，详细信息：{}", vo.getId(), Jsons.encode(oldOdds), Jsons.encode(dto));
						continue;
					}
					final String ratioRate = newOdds.ratioRate;
					// 验证赔率盘口是否一致
					if (oldOdds.type != newOdds.type || !oldOdds.ratioRate.equals(ratioRate)) {
						final OddsInfo first = CollectionUtil.findFirst(dto.odds, o -> o.type == oldOdds.type && o.ratioRate.equals(ratioRate));
						if (first == null) {
							log.warn("盘口类型不匹配：赛事ID={}，赔率类型={}，详细信息：{}", vo.getId(), oldOdds.type, Jsons.encode(bGame));
							continue;
						} else {
							newOdds = first;
							log.warn("盘口位置发生变化：赛事ID={}，盘口类型={}，详细信息：{}", vo.getId(), oldOdds.type, Jsons.encode(bGame));
						}
					}
					// 更新赔率
					parlay.setNewBRateOdds(newOdds);
					vo.update = true;
				}
			}
			// 针对开赛时间晚于当前时间的赛事，重新计算投注金额（已结束的赛事投注金额确定，只能手动修改）
			vo.calcHedgingCoinsLock(now);
			if (vo.update != null && vo.update) {
				updates.put(vo.getId(), Jsons.encode(vo));
			}
		}

		if (!updates.isEmpty()) {
			RedisUtil.doInTransaction(redisOps -> {
				final ZSetOperations<String, String> opsForZSet = redisOps.opsForZSet();
				for (Map.Entry<Long, String> entry : updates.entrySet()) {
					final Long id = entry.getKey();
					opsForZSet.removeRangeByScore(HEDGING_LIST_KEY, id, id); // 更新数据
					opsForZSet.incrementScore(HEDGING_LIST_KEY, entry.getValue(), id); // 更新数据
				}
			});
		}
	}

}