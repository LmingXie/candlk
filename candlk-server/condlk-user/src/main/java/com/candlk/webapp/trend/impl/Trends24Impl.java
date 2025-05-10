package com.candlk.webapp.trend.impl;

import java.util.Set;

import com.candlk.webapp.trend.TrendApi;
import com.candlk.webapp.user.model.TrendProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class Trends24Impl implements TrendApi {

	@Override
	public TrendProvider getProvider() {
		return TrendProvider.TRENDS24;
	}

	@Override
	public Set<String> pull() {
		return parseHtmlKeyword(null, "https://trends24.in/united-states/", "//*[@class='trend-name']//a");
	}

}
