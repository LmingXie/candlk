package com.candlk.webapp.trend.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.candlk.common.util.BaseHttpUtil;
import com.candlk.webapp.trend.TrendApi;
import com.candlk.webapp.user.model.TrendProvider;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
			try {
				final Connection connect = Jsoup.connect("https://redditlist.com/" + (i < 2 ? "index" : "all" + i) + ".html")
						.timeout(10000); // 可设置超时
				parseWords(keywords, i, connect);
			} catch (Exception e) {
				log.error("【{}】查询趋势热词失败：{}", getProvider(), e);
			}
		}
		for (int i = 1; i <= 5; i++) {
			try {
				final Connection connect = Jsoup.connect("https://redditlist.com/nsfw" + (i < 2 ? "" : i) + ".html")
						.timeout(10000); // 可设置超时
				parseWords(keywords, i, connect);
			} catch (Exception e) {
				log.error("【{}】查询趋势热词失败：{}", getProvider(), e);
			}
		}
		for (int i = 1; i <= 5; i++) {
			try {
				final Connection connect = Jsoup.connect("https://redditlist.com/sfw" + (i < 2 ? "" : i) + ".html")
						.timeout(10000); // 可设置超时
				parseWords(keywords, i, connect);
			} catch (Exception e) {
				log.error("【{}】查询趋势热词失败：{}", getProvider(), e);
			}
		}
		return keywords;
	}

	private void parseWords(Set<String> keywords, int i, Connection connect) throws IOException {
		final Document document = connect.get();
		Elements aTags = document.selectXpath("//*[@class='listing-item']//a");
		if (!aTags.isEmpty()) {
			for (Element a : aTags) {
				final String word = TrendApi.formatWord(a.text());
				keywords.add(word);
			}
		}
		log.info("【{}】查询 全部 趋势热词第【{}】页 ", getProvider(), i);
	}

}
