package com.bojiu;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Resource;

import com.bojiu.common.redis.RedisUtil;
import com.bojiu.context.web.Jsons;
import com.bojiu.webapp.UserApplication;
import com.bojiu.webapp.user.bet.BetApi;
import com.bojiu.webapp.user.dto.GameDTO;
import com.bojiu.webapp.user.job.GameBetJob;
import com.bojiu.webapp.user.model.BetProvider;
import lombok.extern.slf4j.Slf4j;
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

	@Test
	public void getGameBetsTest() {
		BetProvider type = BetProvider.KY;
		BetApi api = BetApi.getInstance(type);
		gameBetJob.doQueryAndSyncGameBetsForSingleVendor(api);
	}

	@Test
	public void matchTest() {
		int parlaysSize = 3; // 串关大小（3场比赛为一组）
		BetProvider parlaysProvider = BetProvider.HG; // 组串子的厂家
		BetProvider hedgingProvider = BetProvider.KY; // 组串子的厂家

		// 查询平台上的全部赛事和赔率信息
		List<String> values = RedisUtil.opsForHash().multiGet(GAME_BETS_PERFIX, Arrays.asList(parlaysProvider.name(), hedgingProvider.name()));
		List<GameDTO> gameBets = Jsons.parseArray(values.get(0), GameDTO.class),
				hedgingBets = Jsons.parseArray(values.get(1), GameDTO.class);

		// 从 hedgingBets 过滤能在 hedgingBets 匹配的赛事

		/*
		组串规则：
			1、串子赛事之间的 间隔时间 >= 1小时，且 <= 5 天（保留充足操作时间，同时避免结算周期太长）
			2、舍弃低于0.8，高于1.2 的类型赔率
		 */
	}

}
