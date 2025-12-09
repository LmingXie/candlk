package com.bojiu.webapp.user.bet;

import java.util.*;

import com.bojiu.common.util.SpringUtil;
import com.bojiu.webapp.user.model.BetProvider;
import me.codeplayer.util.LazyCacheLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 24小时趋势热词爬虫
 */
public interface BetApi {

	Logger log = LoggerFactory.getLogger(BetApi.class);

	/** 生产厂商 */
	BetProvider getProvider();

	/** 初始化全部已开启的厂商 */
	private static EnumMap<BetProvider, BetApi> init() {
		return SpringUtil.newEnumImplMap(BetProvider.class, BetApi.class, BetApi::getProvider);
	}

	LazyCacheLoader<EnumMap<BetProvider, BetApi>> implMapRef = new LazyCacheLoader<>(BetApi::init);

	static BetApi getInstance(BetProvider tweetProvider) {
		return implMapRef.get().get(tweetProvider);
	}

	/**
	 * 拉取赔率数据
	 */
	Set<String> pull() throws Exception;

}