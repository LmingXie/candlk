package com.candlk.webapp;

import java.io.IOException;
import java.util.*;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.indices.GetMappingRequest;
import co.elastic.clients.elasticsearch.indices.PutMappingRequest;
import com.candlk.context.web.Jsons;
import com.candlk.webapp.es.ESEngineClient;
import com.candlk.webapp.user.entity.StopWord;
import com.candlk.webapp.user.entity.TweetWord;
import com.candlk.webapp.user.model.ESIndex;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.NLPTokenizer;
import com.hankcs.hanlp.tokenizer.NotionalTokenizer;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.CollectionUtil;
import me.codeplayer.util.StringUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
public class ESApiTest {

	static ESEngineClient engine;

	@BeforeAll
	public static void init() throws IOException {
		engine = new ESEngineClient(
				"https://127.0.0.1:9200",
				// "f40da2681b97c673f3bf0c65e87d70d75ff166d405f6e36a024a96962df57c4d",
				// "elastic", "aS26ZiHC_U+sCJlDccoW"

				"008d29b8ca1324053f3cc196ae88e9e2dc865463053f0880f9c5cf708588f818",
				"elastic", "YqR6mL+USUyJnPYPKtG="
		);
	}

	@Test
	public void addKeyWords() throws Exception {
		final Date now = new Date();
		Set<String> wordSet = new HashSet<>(Arrays.asList(
		));
		List<String> words = wordSet.stream().toList();
		int size = words.size();
		List<TweetWord> keyWords = new ArrayList<>(size);
		final int offset = 0;
		for (int i = 0; i < size; i++) {
			TweetWord e = new TweetWord(words.get(i), 0, 0, 0L, now);
			e.setId(i + 1L + offset);
			keyWords.add(e);
		}
		engine.bulkAddDoc(ESIndex.KEYWORDS_INDEX, keyWords);
	}

	@Test
	public void addStopWords() throws Exception {
		final Date now = new Date();
		// 示例：批量添加停用词
		List<StopWord> stopWords = Arrays.asList(
				new StopWord("的", now),
				new StopWord("the", now),
				new StopWord("是", now),
				new StopWord("@", now),
				new StopWord("$", now)
		);
		for (int i = 0; i < stopWords.size(); i++) {
			stopWords.get(i).setId(i + 1L);
		}
		engine.bulkAddDoc(ESIndex.STOP_WORDS_INDEX, stopWords);
		// 验证停用词缓存
		System.out.println("停用词缓存: " + engine.stopWordsCache);
	}

	@Test
	public void searchKeywords() throws Exception {
		// 示例：查询关键词（第 1 页，每页 2 条，按 priority 降序）
		List<TweetWord> results = engine.searchKeywords(ESIndex.KEYWORDS_INDEX, TweetWord.class,
				1, 6, TweetWord.UPDATE_TIME, SortOrder.Desc);
		log.info("查询关键词: {}", Jsons.encode(results));
	}

	@Test
	public void delKeywords() throws Exception {
		engine.client.delete(d -> d.index("products").id("bk-1"));
	}

	@Test
	public void hanLpSegmentTest() {
		final String inputStr = "BlockBeats 消息，4 月 30 日，据 Cointelegraph 报道，Ledger 硬件钱包用户收到伪装成官方的诈骗信件，信中以「紧急安全更新」为由，要求用户扫描二维码并提供 24 字私密恢复短语，以窃取钱包控制权。技术评论员 Jacob Canfield 在 X 平台曝光此信件，信件盗用 Ledger 标志、地址和参考编号，威胁不验证将限制钱包访问。\n"
				+ "\n"
				+ "\n"
				+ "\n"
				+ "Ledger 官方回应称此为诈骗，强调不会通过电话、消息或要求提供恢复短语，呼吁用户警惕钓鱼攻击。诈骗或与 2020 年 7 月 Ledger 数据泄露有关，当时超 27 万用户个人信息（包括姓名、电话和地址）被黑客泄露。";
		//标准分词
		// List<Term> termList = StandardTokenizer.segment(inputStr);
		// System.out.println(termList);
		// //标准分词封装
		// System.out.println(HanLP.segment(inputStr));
		// NLP分词 词性标注和命名实体识别
		List<Term> segment = NLPTokenizer.segment(inputStr);
		System.out.println("NLP分词结果：" + StringUtil.join(segment, term -> term.word, " | "));

		System.out.println("实词分词器：" + StringUtil.join(NotionalTokenizer.segment(inputStr), term -> term.word, " | "));

	}

	@Test
	public void hanLpEnSegmentTest() {
		final String inputStr = "\uD83D\uDE80 Excited to power this crucial discussion at #HackSeasonsConference in Dubai! \uD83D\uDD25 \n"
				+ "\n"
				+ "Join @0xPickleCati, @decatanomics, and @MidnightCryptoG as we dive deep into this topic. \n"
				+ "\n"
				+ "Register now:";
		// 标准分词
		// List<Term> termList = StandardTokenizer.segment(inputStr);
		// System.out.println("标准分词结果：" + StringUtil.join(termList, term -> term.word, " | "));
		// //标准分词封装
		// System.out.println("标准分词封装：" + StringUtil.join(HanLP.segment(inputStr), term -> term.word, " | "));
		// // NLP分词 词性标注和命名实体识别
		// List<Term> segment = NLPTokenizer.segment(inputStr);
		// System.out.println("NLP分词结果：" + StringUtil.join(segment, term -> term.word, " | "));

		System.out.println("实词分词器：" + StringUtil.join(NotionalTokenizer.segment(inputStr), term -> term.word, " | "));

	}

	@Test
	public void tokenizerTest() throws IOException {
		final String inputStr = "\uD83C\uDF1E2个聪明钱正在买它！\uD83C\uDF1E\n"
				+ "\uD83C\uDF1E2 smart traders are buying it! \uD83C\uDF1E\n"
				+ "\n"
				+ "\uD83D\uDC8EToken: Longcoin (MC: $4.82K)\n"
				+ "\n"
				+ "Di6SRTDraS7L17UTPHMq8han1n4XBzDTcZXgi8yDpump\n"
				+ "\n"
				+ "查看我主页简介即可加入无延迟群组   \n"
				+ "  View my homepage profile to join the no delay group";
		// 分词
		List<Term> segment = NotionalTokenizer.segment(inputStr);
		Set<String> words = CollectionUtil.toSet(segment, term -> term.word);
		log.info("分词结果：{}", StringUtil.joins(words, " | "));

		if (!words.isEmpty()) {
			SearchResponse<TweetWord> response = engine.client.search(s -> {
				s.index(ESIndex.KEYWORDS_INDEX.value);

				// terms 查询匹配 words 字段
				// s.query(q -> q.terms(t -> t.field(TweetWord.WORDS).terms(tq -> tq.value(
				// 		words.stream().map(FieldValue::of).collect(Collectors.toList())
				// ))));

				// 多字段模糊匹配查询（multi_match or should）
				s.query(q -> q.bool(b -> b.should(
						q1 -> q1.multiMatch(mm -> mm
								.query(inputStr) // 原始文本或提取关键词的字符串
								.fields("words.zh", "words.en", "words.fr", "words.es", "words.ja", "words.ko")
								.type(TextQueryType.BestFields) // 可选：也可用 most_fields 或 cross_fields
								.operator(Operator.Or)
						)
				)));

				// 排序规则：type(desc) > priority(desc) > updateTime(desc)
				s.sort(so -> so.field(f -> f.field(TweetWord.TYPE).order(SortOrder.Asc)));
				s.sort(so -> so.field(f -> f.field(TweetWord.COUNT).order(SortOrder.Desc)));

				// 设置最大返回数量，实际业务中应分页
				s.size(30);
				return s;
			}, TweetWord.class);
			List<TweetWord> tweetWords = ESEngineClient.toT(response);
			// 匹配 ES 中的关键词
			log.info("命中关键词数: {}", tweetWords.size());

			for (TweetWord keyword : tweetWords) {
				log.info("关键词: {}, 类型: {}, 计数: {}, 优先级: {}, 更新时间: {}", keyword.getWords(), keyword.getType(), keyword.getCount(), keyword.getPriority(), keyword.getUpdateTime());
			}

		}
	}

	@Test
	public void testAddField() throws Exception {
		ElasticsearchClient client = engine.client;
		final String indexName = ESIndex.KEYWORDS_ACCURATE_INDEX.name().toLowerCase();
		final String fieldName = "status";
		// Step 1: 获取索引映射关系
		final TypeMapping typeMapping = client.indices()
				.getMapping(GetMappingRequest.of(r -> r.index(indexName)))
				.get(indexName).mappings();
		final Map<String, Property> properties = typeMapping.properties();

		if (!properties.containsKey(fieldName)) {
			// Step 2: 添加字段
			client.indices().putMapping(PutMappingRequest.of(req -> req
					.index(indexName)
					.properties(fieldName, Property.of(p -> p.integer(i -> i))) // TODO 设置为integer类型！
			));

			System.out.println("✅ 添加字段成功：" + fieldName);
		} else {
			System.out.println("ℹ️ 字段已存在：" + fieldName);
		}

		// Step 3: 更新现有文档默认值
		final UpdateByQueryResponse response = client.updateByQuery(UpdateByQueryRequest.of(req -> req
				.index(indexName)
				.script(s -> s
						// 通过脚本设置默认值
						.source(builder -> builder.scriptString("if (ctx._source.status == null) { ctx._source.status = 1; }"))
						.lang("painless")
				)
				.query(q -> q
						.bool(b -> b
								.mustNot(mn -> mn
										.exists(e -> e.field(fieldName))
								)
						)
				)
		));
		System.out.println("🔁 更新文档数据: " + response.updated());
	}

}
