package com.candlk.context.model;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.candlk.common.model.ValueProxy;
import com.candlk.common.util.Common;
import lombok.Getter;
import me.codeplayer.util.CollectionUtil;

@Getter
public enum GameType implements LabelI18nProxy<GameType, Integer> {
	/** 棋牌 */
	CHESS(1, "棋牌"),
	/** 捕鱼 */
	FISH(2, "捕鱼"),
	/** 电子 */
	DIGITAL(3, "电子"),
	/** 电竞 */
	E_SPORTS(4, "电竞"),
	/** 体育 */
	SPORT(5, "体育"),
	/** 视讯 */
	VIDEO(6, "视讯"),
	;

	// 定义私有变量
	@EnumValue
	public final Integer value;
	final ValueProxy<GameType, Integer> proxy;

	GameType(Integer value, String label) {
		this.value = value;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static final GameType[] CACHE = values();

	public static GameType of(@Nullable Integer value) {
		return Common.getEnum(CACHE, value, +1);
	}

	public static int minTypeValue() {
		return CACHE[0].value;
	}

	public static int maxTypeValue() {
		return CACHE[CACHE.length - 1].value;
	}

	public static Integer getValue(String label) {
		return switch (label) {
			case "热门" -> 0;
			case "最近游戏" -> 7;
			case "个人收藏" -> 8;
			default -> CollectionUtil.findFirst(Arrays.asList(values()), t -> t.getProxy().label.equals(label)).getValue();
		};
	}

	static final List<Integer> types = Common.toList(Arrays.asList(CACHE), GameType::getValue);

	/**
	 * 验证游戏类型是否存在
	 */
	public static boolean isExist(List<Integer> gameTypes) {
		return CollectionUtil.filter(gameTypes, v -> !types.contains(v)).isEmpty();
	}
}