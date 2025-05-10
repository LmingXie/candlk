package com.candlk.webapp.trend.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
public class Trends24Impl implements TrendApi {

	@Override
	public TrendProvider getProvider() {
		return TrendProvider.TRENDS24;
	}

	@Override
	public Set<String> pull() throws IOException {
		final Connection connect = Jsoup.connect("https://trends24.in/united-states/");
		final Document document = connect.get();
		final Elements aTags = document.selectXpath("//*[@class='trend-name']//a");
		if (!aTags.isEmpty()) {
			final int size = aTags.size();
			final Set<String> keywords = new HashSet<>(size, 1F);
			for (Element a : aTags) {
				final String word = TrendApi.formatWord(a.text()).replaceFirst("#", "").replaceFirst("\\$", "");
				keywords.add(word);
			}
			return keywords;
		}
		return null;
	}

}
