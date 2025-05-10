package com.candlk.webapp.trend.impl;

import java.util.*;

import com.candlk.webapp.trend.TrendApi;
import com.candlk.webapp.user.model.TrendProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GDTTrendImpl implements TrendApi {

	@Override
	public TrendProvider getProvider() {
		return TrendProvider.GDT;
	}

	static final String baseUrl = "https://getdaytrends.com";
	static final List<String> countryUrls = Arrays.asList(
			baseUrl + "/united-states/",
			baseUrl + "/singapore/",
			baseUrl + "/argentina/",
			baseUrl + "/brazil/",
			baseUrl + "/canada/",
			baseUrl + "/malaysia/"
	);

	@Override
	public Set<String> pull() {
		final Set<String> keywords = new HashSet<>();

		for (String url : countryUrls) {
			parseHtmlKeyword(keywords, url, "//*[@class='inset']//a");
		}

		return keywords;
	}

	@Override
	public String parseHtmlWord(String word) {
		return TrendApi.formatWord(word).replaceFirst("#", "");
	}

}
