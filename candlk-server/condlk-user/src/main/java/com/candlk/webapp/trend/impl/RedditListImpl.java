package com.candlk.webapp.trend.impl;

import java.util.HashSet;
import java.util.Set;

import com.candlk.common.util.BaseHttpUtil;
import com.candlk.webapp.trend.TrendApi;
import com.candlk.webapp.user.model.TrendProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RedditListImpl implements TrendApi {

	@Override
	public TrendProvider getProvider() {
		return TrendProvider.REDDIT_LIST;
	}

	@Override
	public Set<String> pull() throws Exception {
		// 设置忽略证书验证
		BaseHttpUtil.trustAllHttpsCertificates();

		final Set<String> keywords = new HashSet<>();
		for (int i = 1; i <= 5; i++) {
			parseHtmlKeyword(keywords, "https://redditlist.com/" + (i < 2 ? "index" : "all" + i) + ".html", "//*[@class='listing-item']//a");
		}
		for (int i = 1; i <= 5; i++) {
			parseHtmlKeyword(keywords, "https://redditlist.com/nsfw" + (i < 2 ? "" : i) + ".html", "//*[@class='listing-item']//a");
		}
		for (int i = 1; i <= 5; i++) {
			parseHtmlKeyword(keywords, "https://redditlist.com/sfw" + (i < 2 ? "" : i) + ".html", "//*[@class='listing-item']//a");
		}
		return keywords;
	}

}
