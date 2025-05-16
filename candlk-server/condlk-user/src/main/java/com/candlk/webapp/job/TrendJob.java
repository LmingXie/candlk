package com.candlk.webapp.job;

import java.io.IOException;
import java.util.*;
import javax.annotation.Resource;

import co.elastic.clients.elasticsearch._types.Conflicts;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.DeleteByQueryRequest;
import co.elastic.clients.elasticsearch.core.UpdateByQueryRequest;
import co.elastic.clients.json.JsonData;
import com.candlk.context.web.Jsons;
import com.candlk.webapp.es.ESEngineClient;
import com.candlk.webapp.trend.TrendApi;
import com.candlk.webapp.user.entity.TweetWord;
import com.candlk.webapp.user.model.ESIndex;
import com.candlk.webapp.user.model.TrendProvider;
import com.candlk.webapp.user.service.TweetWordService;
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

	@Scheduled(cron = "${service.cron.TrendJob:0 0/10 * * * ?}")
	public void run() {
		// if (!RedisUtil.getStringRedisTemplate().opsForSet().isMember(RedisKey.SYS_SWITCH, RedisKey.TWEET_TREND_FLAG)) {
		// 	log.info("【爬取趋势热词】开关关闭，跳过执行...");
		// 	return;
		// }
		log.info("开始执行 爬取趋势热词 任务...");

		final Set<String> allWords = new HashSet<>();
		final Date now = new Date();
		for (TrendProvider type : TrendProvider.CACHE) {
			final TrendApi instance = TrendApi.getInstance(type);
			try {
				pullWords(type, instance, now, allWords);
			} catch (Exception e) {
				log.error("【{}】查询趋势热词失败：", type, e);
				try {
					pullWords(type, instance, now, allWords);
				} catch (Exception e2) {
					log.error("【{}】查询趋势热词失败，跳过：", type, e2);
				}
			}
		}
		if (!allWords.isEmpty()) {
			log.info("查询 全部 趋势热词成功，总共录得关键词：{} {}", allWords.size(), Jsons.encode(allWords));
			try {
				tweetWordService.batchDel(allWords);
			} catch (Exception e) {
				log.error("更新热词状态失败：", e);
			}
		}
	}

	private void pullWords(TrendProvider type, TrendApi instance, Date now, Set<String> allWords) throws Exception {
		final Set<String> words = instance.pull();
		if (CollectionUtils.isNotEmpty(words)) {
			log.info("【{}】查询趋势热词成功，总共录得关键词：{}", type, words.size());
			splitImportWords(words, type, now);
			allWords.addAll(words);
		}
	}

	public void splitImportWords(Set<String> batch, TrendProvider type, Date now) {
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
					tweetWord.setStatus(1);
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

	public static void deleteOldHotWords(ESEngineClient esEngineClient, Set<String> hotWords) throws IOException {
		// 构建查询：删除不在 hotWords 中，且 type != TYPE_CUSTOM 的文档
		final Query query = Query.of(q -> q.bool(b -> b
				.mustNot(mn -> mn.bool(inner -> inner
						.should(Arrays.asList(
								TermsQuery.of(t -> t.field("words").terms(v -> v.value(
										hotWords.stream().map(FieldValue::of).toList()
								)))._toQuery(),
								TermQuery.of(t -> t.field("type").value(TweetWord.TYPE_CUSTOM))._toQuery()
						))
						.minimumShouldMatch(String.valueOf(TweetWord.TYPE_CUSTOM)) // 只要有一个 should 成立，就不会被删除（我们取反）
				))
		));

		// 构建删除请求
		final DeleteByQueryRequest request = DeleteByQueryRequest.of(r -> r
				.index(ESIndex.KEYWORDS_ACCURATE_INDEX.value)
				.query(query)
				.conflicts(Conflicts.Proceed)
				.requestsPerSecond(-1f)
				.refresh(true)
		);

		// 执行请求
		var response = esEngineClient.client.deleteByQuery(request);
		if (response.failures() != null && !response.failures().isEmpty()) {
			for (var failure : response.failures()) {
				log.warn("删除失败: " + failure.cause().reason());
			}
		} else {
			log.info("成功删除文档数: " + response.deleted());
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

}
