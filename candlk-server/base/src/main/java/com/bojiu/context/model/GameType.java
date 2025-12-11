package com.bojiu.context.model;

import java.util.List;
import javax.annotation.Nullable;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.bojiu.common.model.BizFlag;
import com.bojiu.common.model.ValueProxy;
import com.bojiu.common.util.Common;
import lombok.Getter;

import static com.bojiu.context.i18n.UserModelI18nKey.*;

@Getter
public enum GameType implements LabelI18nProxy<GameType, Integer>, BizFlag {
	/** 棋牌 */
	TABLE(1, GAME_TYPE_TABLE),
	/** 捕鱼 */
	FISH(2, GAME_TYPE_FISH),
	/** 电子 */
	DIGITAL(3, GAME_TYPE_DIGITAL),
	/** 体育 */
	SPORT(4, GAME_TYPE_SPORT),
	/** 视讯 */
	LIVE(5, GAME_TYPE_LIVE),
	/** 彩票 */
	LOTTERY(6, GAME_TYPE_LOTTERY),
	/** 区块链 */
	BLOCKCHAIN(7, GAME_TYPE_BLOCKCHAIN),
	;
	final long bizFlag;
	@EnumValue
	public final Integer value;
	final ValueProxy<GameType, Integer> proxy;

	/** 只有厂商图片的游戏类型（子游戏无图片） */
	public static final List<GameType> onlyVendorImageGameTypes = List.of(SPORT, LIVE);

	GameType(Integer value, String label) {
		this.bizFlag = 1L << ordinal();
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	/** 注意：此处必须是 = value()，否则有些基于下标获取枚举的代码也需要同步变动 */
	public static final GameType[] CACHE = values();
	public static final GameType[] TAG_CACHE = { TABLE, FISH, DIGITAL, LIVE, SPORT, LOTTERY, BLOCKCHAIN };

	public static GameType of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, +1);
	}

	/**
	 * 验证游戏类型是否存在
	 */
	public static boolean containsAll(List<Integer> gameTypes) {
		return DIGITAL.includeAll(gameTypes);
	}

}