package com.candlk.webapp.job;

import java.io.IOException;
import java.util.*;
import javax.annotation.Resource;

import co.elastic.clients.elasticsearch._types.Conflicts;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.UpdateByQueryRequest;
import co.elastic.clients.json.JsonData;
import com.candlk.common.redis.RedisUtil;
import com.candlk.context.model.RedisKey;
import com.candlk.context.web.Jsons;
import com.candlk.webapp.es.ESEngineClient;
import com.candlk.webapp.trend.TrendApi;
import com.candlk.webapp.user.entity.TweetWord;
import com.candlk.webapp.user.model.ESIndex;
import com.candlk.webapp.user.model.TrendProvider;
import com.candlk.webapp.user.service.TweetWordService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.CollectionUtil;
import me.codeplayer.util.StringUtil;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
public class TrendJob {

	@Resource
	private TweetWordService tweetWordService;
	@Resource
	ESEngineClient esEngineClient;

	@Scheduled(cron = "${service.cron.TrendJob:0 0/30 * * * ?}")
	public void run() {
		if (!RedisUtil.getStringRedisTemplate().opsForSet().isMember(RedisKey.SYS_SWITCH, RedisKey.TWEET_TREND_FLAG)) {
			log.info("【爬取趋势热词】开关关闭，跳过执行...");
			return;
		}
		log.info("开始执行 爬取趋势热词 任务...");

		final Set<String> allWords = new HashSet<>();
		final Date now = new Date();
		for (TrendProvider type : TrendProvider.CACHE) {
			final TrendApi instance = TrendApi.getInstance(type);
			try {
				final Set<String> words = instance.pull();
				if (CollectionUtils.isNotEmpty(words)) {
					final List<List<String>> batchWords = Lists.partition(new ArrayList<>(words), 2000);
					splitImportWords(batchWords, type, now);
					allWords.addAll(words);
					log.info("【{}】查询趋势热词成功，总共录得关键词：{}", type, words.size());
				}
			} catch (Exception e) {
				log.error("【{}】查询趋势热词失败：{}", type, e);
			}
		}
		if (!allWords.isEmpty()) {
			log.info("查询 全部 趋势热词成功，总共录得关键词：{} {}", allWords.size(), Jsons.encode(allWords));
			try {
				updateHotWordStatus(esEngineClient, allWords, now);
			} catch (Exception e) {
				log.error("更新热词状态失败：", e);
			}
		}
	}

	public static void updateHotWordStatus(ESEngineClient esEngineClient, Set<String> hotWords, Date now) throws IOException {
		// 构建 painless 脚本
		final String scriptSource = """
				if (params.hotWords.contains(ctx._source.words)) {
				    ctx._source.status = 1;
				} else {
				    ctx._source.status = 0;
				}
				ctx._source.updateTime = params.now;
				""";

		// 构建参数
		final Map<String, JsonData> params = new HashMap<>(2, 1F);
		params.put("hotWords", JsonData.of(hotWords)); // List<String> 可被 Elasticsearch painless 脚本识别为 Java List
		params.put("now", JsonData.of(now));

		// 构建查询：排除 自定义 或 手动禁用 的关键词
		final Query query = Query.of(q -> q.bool(b -> b
				.mustNot(mn -> mn.bool(inner -> inner.should(Arrays.asList(
						TermQuery.of(t -> t.field(TweetWord.TYPE).value(TweetWord.TYPE_CUSTOM))._toQuery(),
						TermQuery.of(t -> t.field(TweetWord.STATUS).value(TweetWord.STATUS_DISABLE))._toQuery()
				))))
		));

		// 构建 request
		final UpdateByQueryRequest request = UpdateByQueryRequest.of(r -> r
				.index(ESIndex.KEYWORDS_ACCURATE_INDEX.value)
				.query(query)
				.script(s -> s
						.lang("painless")
						.source(builder -> builder.scriptString(scriptSource))
						.params(params)
				)
				.conflicts(Conflicts.Proceed)
				.requestsPerSecond(-1f) // 取消节流，加速批次处理
				.refresh(true)
		);

		// 执行请求
		var response = esEngineClient.client.updateByQuery(request);
		if (response.failures() != null && !response.failures().isEmpty()) {
			for (var failure : response.failures()) {
				log.warn("更新失败: " + failure.cause().reason());
			}
		} else {
			log.info("成功更新文档数: " + response.updated());
		}
	}

	public void splitImportWords(List<List<String>> batchWords, TrendProvider type, Date now) {
		for (List<String> batch : batchWords) {
			log.info("【{}】开始导入关键词：{} {}", type, batch.size(), Jsons.encode(batch));
			final List<String> oldWords = tweetWordService.findWords(batch);
			if (!oldWords.isEmpty()) {
				batch.removeIf(t -> CollectionUtil.findFirst(oldWords, t::equals) != null);
			}
			if (!batch.isEmpty()) {
				final List<TweetWord> tweetWords = new ArrayList<>(batch.size());
				final int wordType = TweetWord.TYPE_HOT;
				for (String word : batch) {
					if (!StringUtil.isEmpty(word)) {
						final TweetWord tweetWord = new TweetWord();
						final String trim = word.trim();
						tweetWord.setProviderType(type.value);
						tweetWord.setWords(trim);
						tweetWord.setType(wordType);
						tweetWord.setPriority(0);
						tweetWord.initTime(now);
						tweetWords.add(tweetWord);
					}
				}
				try {
					tweetWordService.batchAdd(tweetWords, wordType);
				} catch (Exception e) {
					log.error("导入失败", e);
				}
			}
		}
	}

}
