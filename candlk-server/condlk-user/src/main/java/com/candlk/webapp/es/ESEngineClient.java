package com.candlk.webapp.es;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLContext;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.transport.TransportUtils;
import com.candlk.context.web.Jsons;
import com.candlk.webapp.base.entity.BaseEntity;
import com.candlk.webapp.user.entity.StopWord;
import com.candlk.webapp.user.model.ESIndex;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ESEngineClient {

	private static final String serverUrl = "https://localhost:9200";
	private static final String fingerprint = "008d29b8ca1324053f3cc196ae88e9e2dc865463053f0880f9c5cf708588f818";
	private static final String pwd = "YqR6mL+USUyJnPYPKtG=";

	public final ElasticsearchClient client;
	/** 停用词本地缓存 */
	public final Set<String> stopWordsCache;

	public ESEngineClient() throws IOException {
		SSLContext sslContext = TransportUtils.sslContextFromCaFingerprint(fingerprint);
		client = ElasticsearchClient.of(b -> b
				.host(serverUrl)
				.usernameAndPassword("elastic", pwd)
				.sslContext(sslContext)
		);
		stopWordsCache = Collections.newSetFromMap(new ConcurrentHashMap<>());
		loadStopWordsToCache(); // 初始化时加载停用词
	}

	/** 初始化时从 stopwords_index 加载所有停用词到缓存 */
	private void loadStopWordsToCache() throws IOException {
		SearchResponse<StopWord> response = client.search(s -> s
						.index(ESIndex.STOP_WORDS_INDEX.value)
						.query(q -> q.matchAll(m -> m))
						.size(10000), // 设置足够大的 size，覆盖大部分停用词
				StopWord.class);

		List<Hit<StopWord>> hits = response.hits().hits();
		for (Hit<StopWord> hit : hits) {
			StopWord source = hit.source();
			if (source != null) {
				stopWordsCache.add(source.words);
			}
		}
		log.info("成功加载停用词：{} {}", stopWordsCache.size(), Jsons.encode(stopWordsCache));
	}

	/**
	 * 批量添加文档数据
	 * <p><a href="https://www.elastic.co/docs/reference/elasticsearch/clients/java/usage/indexing-bulk#_indexing_application_objects">
	 * 关于Bulk 批量操作API
	 * </a></p>
	 */
	public <T extends BaseEntity> void bulkAddDoc(ESIndex type, List<T> keyWords) throws Exception {
		if (keyWords == null || keyWords.isEmpty()) {
			return;
		}

		List<BulkOperation> operations = new ArrayList<>();
		for (T keyword : keyWords) {
			operations.add(BulkOperation.of(b -> b
					.index(op -> op.index(type.value)
							.id(keyword.getId().toString()) // 自定义ID
							.document(keyword))));
			if (type == ESIndex.STOP_WORDS_INDEX) {
				// 更新缓存
				stopWordsCache.add(((StopWord) keyword).getWords());
			}
		}

		// 批量添加
		BulkRequest bulkRequest = BulkRequest.of(b -> b.operations(operations));
		BulkResponse bulkResponse = client.bulk(bulkRequest);

		if (bulkResponse.errors()) {
			bulkResponse.items().forEach(item -> {
				if (item.error() != null) {
					log.warn("批量添加关键词失败: " + item.error().reason());
				}
			});
		} else {
			log.info("成功批量添加 " + keyWords.size() + " 个关键词");
		}
	}

	/**
	 * 查询关键词，支持分页
	 * <p><a href="https://www.elastic.co/docs/reference/elasticsearch/clients/java/getting-started#_searching_documents">
	 * 关于文档的增删改查
	 * </a></p>
	 *
	 * @param page 从 1 开始的页码
	 * @param pageSize 每页大小
	 * @param sortField 排序字段（例如 "priority", "count"），可为 null
	 * @param sortOrder 排序顺序（"asc" 或 "desc"），可为 null
	 * @return 关键词列表
	 */
	public <T extends BaseEntity> List<T> searchKeywords(ESIndex type, Class<T> clazz, int page, int pageSize, String sortField, SortOrder sortOrder) throws Exception {
		if (page < 1 || pageSize < 1) {
			throw new IllegalArgumentException("页码和页面大小必须大于 0");
		}

		int from = (page - 1) * pageSize;

		SearchResponse<T> response = client.search(s -> {
			s.index(type.value)
					.query(q -> q.matchAll(m -> m))
					.from(from)
					.size(pageSize);

			if (sortField != null && !sortField.isEmpty()) {
				s.sort(so -> so.field(f -> f.field(lineToHump(sortField)).order(sortOrder)));
			}
			return s;
		}, clazz);
		return toT(response);
	}

	public static <T extends BaseEntity> @NotNull List<T> toT(SearchResponse<T> response) {
		List<Hit<T>> hits = response.hits().hits();
		List<T> results = new ArrayList<>(hits.size());
		for (Hit<T> hit : hits) {
			T source = hit.source();
			if (source != null) {
				results.add(source);
			}
		}
		return results;
	}

	public static Date parseDate(String time) {
		return Date.from(Instant.parse(time));
	}

	private static Pattern linePattern = Pattern.compile("_(\\w)");

	public static String lineToHump(String str) {
		str = str.toLowerCase();
		Matcher matcher = linePattern.matcher(str);
		StringBuilder sb = new StringBuilder();
		while (matcher.find()) {
			matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

}
