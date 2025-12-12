package com.bojiu.webapp.user.bet;

import java.util.EnumMap;
import java.util.Set;

import com.bojiu.common.model.Messager;
import com.bojiu.common.util.SpringUtil;
import com.bojiu.webapp.user.model.BetProvider;
import me.codeplayer.util.LazyCacheLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 24小时趋势热词爬虫
 */
public interface BetApi {

	/** 正在维护中 */
	String STATUS_MAINTAIN = "game503";
	/** 已被限流的 */
	String STATUS_FREQ_LIMITED = "freqLimited";

	Logger LOGGER = LoggerFactory.getLogger(BetApi.class);

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

	/** 检查当前API服务器状态是否正常 */
	Messager<Void> ping();

}