package com.bojiu.webapp.user.model;

import com.bojiu.context.model.RedisKey;

public interface UserRedisKey extends RedisKey {

	/** 每个厂商【同步】游戏赔率断点续传的断点值，下一轮查询就从此开始 Hash < (String) BetProvider, GameBetQueryDTO > */
	String BET_SYNC_RELAY = "betSyncRelay";
	/** 从厂商拉取的游戏赔率列表 < (String) BetProvider, List<GameDTO> > */
	String GAME_BETS_PERFIX = "gameBets";
	/** 推荐匹配列表 ZSet < $info, $score > */
	String BET_MATCH_DATA_KEY = "betMatchData:";
	/** 对冲方案自增ID（String） */
	String HEDGING_ID_INCR_KEY = "hedgingIncr";
	/** 存档的对冲方案 ZSet < $info, $id > */
	String HEDGING_LIST_KEY = "hedgingList";
	/**存档对冲方案的范围*/
	Double DEFAULT_MIN_SCORE = -Double.MAX_VALUE,
			DEFAULT_MAX_SCORE = Double.MAX_VALUE;

	/** 英中文映射数据 */
	String TEAM_NAME_EN2ZH_CACHE = "teamEn2Zh:";
}