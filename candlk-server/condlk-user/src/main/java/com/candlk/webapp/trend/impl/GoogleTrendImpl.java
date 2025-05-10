package com.candlk.webapp.trend.impl;

import java.io.IOException;
import java.net.http.*;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

import com.alibaba.fastjson2.JSONArray;
import com.candlk.common.util.BaseHttpUtil;
import com.candlk.context.web.Jsons;
import com.candlk.webapp.trend.TrendApi;
import com.candlk.webapp.user.model.TrendProvider;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.NotionalTokenizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GoogleTrendImpl implements TrendApi {

	@Override
	public TrendProvider getProvider() {
		return TrendProvider.GOOGLE;
	}

	final HttpClient proxyHttpClient;

	public GoogleTrendImpl(@Value("${service.proxy-conf}") String proxyConfig) {
		proxyHttpClient = BaseHttpUtil.getProxyOrDefaultClient(proxyConfig);
	}

	@Override
	public Set<String> pull() throws IOException {
		final HttpRequest.Builder builder = BaseHttpUtil.requestBuilder(HttpMethod.POST, "https://trends.google.com/_/TrendsUi/data/batchexecute?rpcids=i0OFE&source-path=%2Ftrending&f.sid=-3103657266364708976&hl=zh-CN&_reqid=247094&rt=c",
				"f.req=[[[\"i0OFE\",\"[null,null,\\\"US\\\",0,\\\"zh-CN\\\",4,1]\",null,\"generic\"]]]&at=ALBOKy2Kv88pZ92nR0ve_c0HhWEn:" + System.currentTimeMillis() + "&", false);

		builder.timeout(Duration.of(15, ChronoUnit.SECONDS))
				.setHeader("content-type", "application/x-www-form-urlencoded;charset=UTF-8")
				.setHeader("origin", "https://trends.google.com")
				.setHeader("referer", "https://trends.google.com/");
		HttpResponse<String> response = BaseHttpUtil.doSend(proxyHttpClient, builder.build(), BaseHttpUtil.responseBodyHandler);
		final String responseBody = response.body();
		final String[] split = responseBody.split("\\n");

		final JSONArray jsonArray = Jsons.parseArray(split[3]).getJSONArray(0).getJSONArray(2).getJSONArray(1);
		final int size = jsonArray.size();
		final Set<String> googleTrendingKeywords = new HashSet<>(size, 1F);
		for (int i = 0; i < size; i++) {
			final String word = jsonArray.getJSONArray(i).getString(0);
			final List<Term> segment = NotionalTokenizer.segment(word);
			if (!segment.isEmpty()) {
				googleTrendingKeywords.add(TrendApi.formatWord(word));
			}
		}
		log.info("【{}】查询趋势热词成功，总共录得关键词：{} 全部关键词：{}", getProvider(), googleTrendingKeywords.size(), size);
		return googleTrendingKeywords;
	}

}
