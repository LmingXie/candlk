package com.bojiu;

import java.util.*;
import javax.annotation.Resource;

import com.bojiu.common.redis.RedisUtil;
import com.bojiu.context.web.Jsons;
import com.bojiu.webapp.UserApplication;
import com.bojiu.webapp.user.bet.BetApi;
import com.bojiu.webapp.user.dto.GameDTO;
import com.bojiu.webapp.user.job.GameBetJob;
import com.bojiu.webapp.user.model.BetProvider;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.CollectionUtil;
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

		// 以下将串关平台称为“A”，对冲平台称为“B”

		// 查询平台上的全部赛事和赔率信息
		List<String> values = RedisUtil.opsForHash().multiGet(GAME_BETS_PERFIX, Arrays.asList(parlaysProvider.name(), hedgingProvider.name()));
		List<GameDTO> gameBets = Jsons.parseArray(values.get(0), GameDTO.class),
				hedgingBets = Jsons.parseArray(values.get(1), GameDTO.class);

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
					List<GameDTO> games_ = CollectionUtil.filter(bGames, b -> aGame.league.equals(b.league));
					if (games_.size() == 1) {
						gameMapper.put(aGame, games_.get(0));
						continue;
					}
					// 查找别名库
					log.warn("无法匹配赛事：aGame={}，bGames={}", Jsons.encodeRaw(aGame), Jsons.encode(bGames));
				}
			}/* else {
				log.info("无法匹配赛事：aGame={}", Jsons.encode(aGame));
			}*/
		}
		/*
		组串规则：
			1、串子赛事之间的 间隔时间 >= 1小时，且 <= 5 天（保留充足操作时间，同时避免结算周期太长）
			2、舍弃低于0.8，高于1.2 的类型赔率
		 */
	}

}
