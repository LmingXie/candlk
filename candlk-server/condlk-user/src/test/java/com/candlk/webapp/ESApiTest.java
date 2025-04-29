package com.candlk.webapp;

import java.io.IOException;
import java.util.*;

import com.candlk.context.web.Jsons;
import com.candlk.webapp.es.*;
import com.candlk.webapp.user.entity.StopWord;
import com.candlk.webapp.user.entity.TweetWord;
import com.candlk.webapp.user.model.ESIndexType;
import com.cybozu.labs.langdetect.LangDetectException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
public class ESApiTest {

	static ESOptimizedSearchEngine engine;

	@BeforeAll
	public static void init() throws IOException, LangDetectException {
		engine = new ESOptimizedSearchEngine();
	}

	@Test
	public void addKeyWords() throws Exception {
		final Date now = new Date();
		// 示例：批量添加关键词
		List<TweetWord> keyWords = Arrays.asList(
				new TweetWord("Meme", 0, 0, 0L, now),
				new TweetWord("Pump", 0, 0, 0L, now),
				new TweetWord("Pump", 0, 0, 0L, now),
				new TweetWord("Chain", 0, 0, 0L, now),
				new TweetWord("大数据", 0, 0, 0L, now)
		);
		for (int i = 0; i < keyWords.size(); i++) {
			keyWords.get(i).setId(i + 1L);
		}
		engine.bulkAddDoc(ESIndexType.KEYWORDS_INDEX, keyWords);
	}

	@Test
	public void addStopWords() throws Exception {
		final Date now = new Date();
		// 示例：批量添加停用词
		List<StopWord> stopWords = Arrays.asList(
				new StopWord("的", now),
				new StopWord("the", now),
				new StopWord("是", now)
		);
		for (int i = 0; i < stopWords.size(); i++) {
			stopWords.get(i).setId(i + 1L);
		}
		engine.bulkAddDoc(ESIndexType.STOP_WORDS_INDEX, stopWords);
		// 验证停用词缓存
		System.out.println("停用词缓存: " + engine.stopWordsCache);
	}

	@Test
	public void searchKeywords() throws Exception {
		// 示例：查询关键词（第 1 页，每页 2 条，按 priority 降序）
		List<TweetWord> results = engine.searchKeywords(ESIndexType.STOP_WORDS_INDEX, TweetWord.class,
				1, 2, "id", "desc");
		log.info("查询关键词: {}", Jsons.encode(results));
	}

	@Test
	public void delKeywords() throws Exception {
		engine.client.delete(d -> d.index("products").id("bk-1"));
	}
		/*
		添加关键词、停用词索引
		录入关键词（包括热点、二级、普通三类）
		分词器
		批量查询停用词到缓存

		批量更新关键词命中计数，并插入普通词

		查询前TopN关键词
		 */

}
