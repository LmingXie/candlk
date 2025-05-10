package com.candlk.webapp.trend;

import java.util.EnumMap;
import java.util.Set;

import com.candlk.context.ContextImpl;
import com.candlk.webapp.user.model.TrendProvider;
import me.codeplayer.util.LazyCacheLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 24小时趋势热词爬虫
 */
public interface TrendApi {

	Logger log = LoggerFactory.getLogger(TrendApi.class);

	/** 生产厂商 */
	TrendProvider getProvider();

	/** 初始化全部已开启的厂商 */
	private static EnumMap<TrendProvider, TrendApi> init() {
		return ContextImpl.newEnumImplMap(TrendProvider.class, TrendApi.class, TrendApi::getProvider);
	}

	LazyCacheLoader<EnumMap<TrendProvider, TrendApi>> implMapRef = new LazyCacheLoader<>(TrendApi::init);

	static TrendApi getInstance(TrendProvider tweetProvider) {
		return implMapRef.get().get(tweetProvider);
	}

	/**
	 * 拉取最新趋势热词
	 */
	Set<String> pull() throws Exception;

	static String formatWord(String word) {
		return word.trim().toLowerCase();
	}

}
