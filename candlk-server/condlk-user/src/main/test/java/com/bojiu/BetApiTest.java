package com.bojiu;

import java.util.Map;
import javax.annotation.Resource;

import com.bojiu.context.web.Jsons;
import com.bojiu.webapp.UserApplication;
import com.bojiu.webapp.user.bet.BetApi;
import com.bojiu.webapp.user.bet.impl.PsBetImpl;
import com.bojiu.webapp.user.dto.*;
import com.bojiu.webapp.user.job.BetMatchJob;
import com.bojiu.webapp.user.job.GameBetJob;
import com.bojiu.webapp.user.model.BetProvider;
import com.bojiu.webapp.user.service.BetMatchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@SpringBootTest(classes = UserApplication.class)
@ExtendWith({ SpringExtension.class, MockitoExtension.class })
public class BetApiTest {

	@Resource
	GameBetJob gameBetJob;
	@Resource
	BetMatchService betMatchService;
	@Resource
	BetMatchJob betMatchJob;

	@Test
	public void getBetApi() {
		BetProvider type = BetProvider.PS;
		PsBetImpl api = (PsBetImpl) BetApi.getInstance(type);
		for (int i = 0; i < 10; i++) {
			long systemTime = System.currentTimeMillis();
			api.getGameBets(BetApi.LANG_EN);
			log.info("【{}】查询耗时：{}ms", i + 1, System.currentTimeMillis() - systemTime);
			try {
				Thread.sleep(3000L);
			} catch (InterruptedException ignore) {
			}
		}
	}

	@Test
	public void getGameBetsTest() {
		BetApi api = BetApi.getInstance(BetProvider.PS);
		gameBetJob.doQueryAndSyncGameBetsForSingleVendor(api);
	}

	@Test
	public void getGameScoreResultTest() {
		BetProvider type = BetProvider.HG;
		BetApi api = BetApi.getInstance(type);
		Map<Long, ScoreResult> scoreResult = api.getScoreResult();
		log.info("赛果数据：{}", Jsons.encode(scoreResult));
	}

	@Test
	public void matchTest() {
		int parlaysSize = 3; // 串关大小（3场比赛为一组）
		BetProvider parlaysProvider = BetProvider.PS; // 组串子的厂家
		BetProvider hedgingProvider = BetProvider.KY; // 组串子的厂家

		// 以下将串关平台称为“A”，对冲平台称为“B”
		long startTime = System.currentTimeMillis();

		// 获取A平台到B平台赛事的映射
		Map<GameDTO, GameDTO> gameMapper = betMatchService.getGameMapper(Pair.of(parlaysProvider, hedgingProvider));
		log.info("查询赛事映射完成，耗时：{}ms", System.currentTimeMillis() - startTime);

		startTime = System.currentTimeMillis();
		Pair<HedgingDTO[], Long> pair = betMatchService.match(gameMapper, parlaysSize, 1000);
		final HedgingDTO[] globalTop = pair.getKey();

		for (HedgingDTO hedgingDTO : globalTop) {
			double avgProfit = hedgingDTO.avgProfit;
			if (avgProfit > 50) {
				log.info("估算串关投注方案：{}，平均利润：{}，详细信息：{}", Jsons.encode(hedgingDTO.getHedgingCoins()),
						avgProfit, Jsons.encode(hedgingDTO));
			}
		}
		log.info("计算【{}】种组合耗时：{}ms", pair.getValue(), System.currentTimeMillis() - startTime);
	}

	@Test
	public void matchJobTest() {
		betMatchJob.run();
	}

}
