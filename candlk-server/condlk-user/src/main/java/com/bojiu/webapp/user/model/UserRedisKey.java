package com.bojiu.webapp.user.model;

import com.bojiu.context.model.RedisKey;

public interface UserRedisKey extends RedisKey {

	/** 每个厂商【同步】游戏赔率断点续传的断点值，下一轮查询就从此开始 Hash < (String) BetProvider, GameBetQueryDTO > */
	String BET_SYNC_RELAY = "betSyncRelay";
	/** 从厂商拉取的游戏赔率列表 < (String) BetProvider, List<GameDTO> > */
	String GAME_BETS_PERFIX = "gameBets";

}