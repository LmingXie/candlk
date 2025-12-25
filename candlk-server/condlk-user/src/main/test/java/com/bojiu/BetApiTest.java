package com.bojiu;

import java.util.*;
import javax.annotation.Resource;

import com.bojiu.common.redis.RedisUtil;
import com.bojiu.context.web.Jsons;
import com.bojiu.webapp.UserApplication;
import com.bojiu.webapp.user.bet.BetApi;
import com.bojiu.webapp.user.dto.GameDTO;
import com.bojiu.webapp.user.dto.HedgingDTO;
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

import static com.bojiu.webapp.user.model.UserRedisKey.GAME_BETS_PERFIX;

@Slf4j
@SpringBootTest(classes = UserApplication.class)
@ExtendWith({ SpringExtension.class, MockitoExtension.class })
public class BetApiTest {

	@Resource
	GameBetJob gameBetJob;
	@Resource
	BetMatchService betMatchService;

	@Test
	public void getGameBetsTest() {
		BetProvider type = BetProvider.HG;
		BetApi api = BetApi.getInstance(type);
		gameBetJob.doQueryAndSyncGameBetsForSingleVendor(api);
	}

	@Test
	public void matchTest() throws Exception {
		int parlaysSize = 3; // 串关大小（3场比赛为一组）
		BetProvider parlaysProvider = BetProvider.HG; // 组串子的厂家
		BetProvider hedgingProvider = BetProvider.KY; // 组串子的厂家

		// 以下将串关平台称为“A”，对冲平台称为“B”
		long startTime = System.currentTimeMillis();

		// 查询平台上的全部赛事和赔率信息
		List<String> values = RedisUtil.opsForHash().multiGet(GAME_BETS_PERFIX, Arrays.asList(parlaysProvider.name(), hedgingProvider.name()));
		List<GameDTO> gameBets = Jsons.parseArray(values.get(0), GameDTO.class),
				hedgingBets = Jsons.parseArray(values.get(1), GameDTO.class);

		// 获取A平台到B平台赛事的映射
		Map<GameDTO, GameDTO> gameMapper = betMatchService.getGameMapper(Pair.of(parlaysProvider, hedgingProvider));
		log.info("查询赛事映射完成，耗时：{}ms", System.currentTimeMillis() - startTime);

		startTime = System.currentTimeMillis();
		final HedgingDTO[] globalTop = betMatchService.match(gameMapper, parlaysSize, 1000);

		for (HedgingDTO hedgingDTO : globalTop) {
			double avgProfit = hedgingDTO.avgProfit;
			if (avgProfit > 0) {
				log.info("估算串关投注方案：{}，平均利润：{}，详细信息：{}", Jsons.encode(hedgingDTO.getHedgingCoins()),
						avgProfit, Jsons.encode(hedgingDTO));
			}
		}
		log.info("方案1：计算组合耗时：{}ms", System.currentTimeMillis() - startTime);

	}

}
