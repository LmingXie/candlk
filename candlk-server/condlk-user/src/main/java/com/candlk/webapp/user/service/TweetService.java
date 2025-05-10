package com.candlk.webapp.user.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Resource;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.candlk.common.dao.SmartQueryWrapper;
import com.candlk.common.model.Status;
import com.candlk.common.model.TimeInterval;
import com.candlk.common.web.Page;
import com.candlk.context.web.Jsons;
import com.candlk.webapp.api.TweetInfo;
import com.candlk.webapp.base.service.BaseServiceImpl;
import com.candlk.webapp.es.ESEngineClient;
import com.candlk.webapp.user.dao.TweetDao;
import com.candlk.webapp.user.entity.*;
import com.candlk.webapp.user.form.TweetQuery;
import com.candlk.webapp.user.model.*;
import com.candlk.webapp.user.vo.TweetVO;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.NotionalTokenizer;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 推文信息表 服务实现类
 *
 * @since 2025-04-27
 */
@Slf4j
@Service
public class TweetService extends BaseServiceImpl<Tweet, TweetDao, Long> {

	@Resource
	TweetUserService tweetUserService;
	@Resource
	ESEngineClient esEngineClient;

	public Page<TweetVO> findPage(Page<?> page, TweetQuery query, TimeInterval interval) {
		final String prefix = "t.";
		var wrapper = new SmartQueryWrapper<Tweet>()
				.eq(prefix + TokenEvent.TYPE, query.type)
				.eq(prefix + TokenEvent.STATUS, query.status)
				.eq("tw." + Tweet.USERNAME, query.username)
				.between(prefix + Tweet.ADD_TIME, interval);
		/*
		全部类型推文/特殊关注推文：按照发布时间倒序
		热门评分推文：按照评分倒序
		浏览猛增推文：按照浏览量倒序
		 */
		if (query.type == null || query.type == 0) {
			wrapper.orderByDesc("tw." + Tweet.ADD_TIME);
		} else if (query.type == 1) {
			wrapper.orderByDesc("( tw.score + tu.score )");
		} else {
			wrapper.orderByDesc("tw.impression");
		}
		return baseDao.findPage(page, wrapper);
	}

	public Page<TweetVO> findPageTrackers(Page<?> page, TweetQuery query, TimeInterval interval) {
		final String prefix = "t.";
		var wrapper = new SmartQueryWrapper<Tweet>()
				.eq(prefix + Tweet.USERNAME, query.username)
				.gt(prefix + Tweet.ID, query.minId)
				.between(prefix + Tweet.ADD_TIME, interval)
				.orderByDesc("t.id");
		if (query.status != null) {
			if (query.status == TokenEvent.CREATE) {
				wrapper.isNull("te." + TokenEvent.TYPE);
			} else {
				wrapper.isNotNull("te." + TokenEvent.TYPE);
			}
		}
		return baseDao.findPageTrackers(page, wrapper);
	}

	@Transactional
	public void saveTweet(Tweet tweetInfo, TweetProvider provider, String tweetId, TweetUser tweetUser) {
		try {
			tweetInfo.setStatus(Tweet.QUALITY_NOT_PASS)
					.setScore(calcScore(tweetInfo)); // 计算推文评分
			// 特殊账号无需命中关键词 -> 验证是否为特殊账号
			if (tweetUserService.updateStat(tweetUser)/*更新成功说明用户存在*/ && Tweet.INIT != tweetInfo.getStatus()/*评分未达标*/) {
				final TweetUser user = tweetUserService.findByUsername(tweetInfo.getUsername());
				if (user != null && user.getType() == TweetUserType.SPECIAL) {
					tweetInfo.setStatus(Tweet.INIT);
				}
			}
			// 添加推文
			super.save(tweetInfo);

		} catch (DuplicateKeyException e) { // 违反唯一约束
			log.warn("【{}】推文已存在：{}", provider, tweetId);
		} catch (Exception e) {
			log.error("【{}】保存推文失败：{}", provider, tweetId, e);
		}
	}

	private static final List<String> blockKeywords = Arrays.asList("聪明钱正在买它", "CA: ");

	// Ethereum 地址：0x 开头 + 40个 hex 字符
	private static final Pattern ETH_ADDRESS_PATTERN = Pattern.compile("0x[a-fA-F0-9]{40}");

	// Solana 地址：Base58，32~44个字符
	private static final Pattern SOL_ADDRESS_PATTERN = Pattern.compile("\\b[1-9A-HJ-NP-Za-km-z]{32,44}\\b");

	public static boolean containsEthAddress(String text) {
		return ETH_ADDRESS_PATTERN.matcher(text).find();
	}

	public static boolean containsSolAddress(String text) {
		return SOL_ADDRESS_PATTERN.matcher(text).find();
	}

	public static boolean preCheck(String input) {
		// 屏蔽 包含地址的推文
		if (containsEthAddress(input) || containsSolAddress(input)) {
			return false;
		}

		// 屏蔽包含敏感字词的推文
		for (String keyword : blockKeywords) {
			if (input.contains(keyword)) {
				return false;
			}
		}
		return true;
	}

	public BigDecimal calcScore(Tweet tweetInfo) {
		final String text = tweetInfo.getText();
		if (!preCheck(text)) {
			return BigDecimal.ZERO;
		}

		// TODO 优化：识别语言再分词（日语、韩语准确度不高）
		// 推文分词：采用【实词分词器】将自动过滤 符号、表情、语气助词 等特殊字符
		final List<Term> segment = NotionalTokenizer.segment(text);
		final int size = segment.size();
		if (size < 2) {
			return BigDecimal.ZERO;
		}
		final Set<String> words = new HashSet<>(size);
		for (Term term : segment) {
			// 字符必须大于1 && 不包含在内部停用词中
			if (term.word.length() < 2 || esEngineClient.stopWordsCache.contains(term.word)) {
				continue;
			}
			words.add(term.word);
		}
		if (words.size() < 2) {
			return BigDecimal.ZERO;
		}

		// 纯文本：0.5分
		BigDecimal score = new BigDecimal("0.5");
		try {
			// 命中关键词 -> 1分
			List<TweetWord> tweetWords = matchKeywords(ESIndex.KEYWORDS_ACCURATE_INDEX, words);
			if (!tweetWords.isEmpty()) {
				tweetInfo.setStatus(Tweet.INIT);
				score = score.add(BigDecimal.ONE);
				tweetInfo.setWords(Jsons.encode(CollectionUtil.toList(tweetWords, TweetWord::getWords)));
			}
		} catch (IOException e) {
			log.error("【{}】查询关键词失败：{}", tweetInfo.getTweetId(), e);
		}

		// 媒体推文 -> 纯文本：0.5分；附带图片：1分；附带视频/GIF：1.5分；文本+视频 2分
		if (StringUtils.isNotEmpty(tweetInfo.getImages())) {
			score = score.add(BigDecimal.ONE);
		}
		if (StringUtils.isNotEmpty(tweetInfo.getVideos())) {
			score = score.add(new BigDecimal("1.5"));
		}
		return score;
	}

	public List<TweetWord> matchKeywords(ESIndex index, Set<String> words) throws IOException {
		final List<TweetWord> result = esMatchKeywords(esEngineClient.client, index, words);
		// 命中后更新计数 count += 1
		if (!result.isEmpty()) {
			BulkRequest.Builder br = new BulkRequest.Builder();
			for (TweetWord hit : result) {
				br.operations(op -> op.update(ub -> ub
						.index(index.value)
						.id(hit.getId().toString()) // 确保 TweetWord 实体类中 getId() 方法能获取文档 _id
						.action(a -> a.script(s -> s
								.lang("painless")
								.source(builder -> builder.scriptString("ctx._source.count += 1")))
						)
				));
			}
			esEngineClient.client.bulk(br.build()); // 执行批量更新
		}
		return result;
	}

	@NotNull
	public static List<TweetWord> esMatchKeywords(ElasticsearchClient client, ESIndex index, Set<String> words) throws IOException {
		final SearchResponse<TweetWord> response = client.search(s -> {
			s.index(index.value);
			s.query(q -> q.bool(b -> {
				// 模糊查询索引：根据ES分词后的字段模糊匹配
				if (index == ESIndex.KEYWORDS_INDEX) {
					b.should(q1 -> q1.multiMatch(mm -> mm
							.query(StringUtil.joins(words, " "))
							.fields("words.zh", "words.en", "words.fr", "words.es", "words.ja", "words.ko")
							.type(TextQueryType.BestFields)
							.operator(Operator.Or)
					));
				} else {
					// 精确查询索引：根据 ES 原始字段精确匹配
					q.terms(t -> t.field(TweetWord.WORDS)
							.terms(tq -> tq.value(CollectionUtil.toList(words, FieldValue::of)))
					);
				}

				// 将主查询和 status == 1 组合为 must 子句
				return b.must(q1 -> q1.term(t -> t.field(TweetWord.STATUS).value(Status.YES.value)));
			}));

			// 排序规则：type(desc) > priority(desc) > updateTime(desc)
			s.sort(so -> so.field(f -> f.field(TweetWord.TYPE).order(SortOrder.Asc)));
			s.sort(so -> so.field(f -> f.field(TweetWord.COUNT).order(SortOrder.Desc)));

			// 设置最大返回数量，实际业务中应分页
			s.size(50);
			return s;
		}, TweetWord.class);

		return ESEngineClient.toT(response);
	}

	private transient TimeInterval localTimeInterval;

	@Nonnull
	public TimeInterval lastInterval() {
		if (localTimeInterval == null) {
			// 只查询 3小时 内的推文数据
			final EasyDate d = new EasyDate();
			final Date end = d.endOf(Calendar.DATE).toDate();
			final Date start = d.addHour(-3).beginOf(Calendar.DATE).toDate();
			localTimeInterval = new TimeInterval(start, end, -1, -1);
		}
		return localTimeInterval;
	}

	public List<Tweet> surgeToken(Integer limit, Date prevTime) {
		EasyDate d = new EasyDate();
		// 查询 3 分钟前的推文【太短没有意义】
		Date end = prevTime == null ? d.addMinute(-3).toDate() : prevTime;
		Date start = d.addDay(-3).beginOf(Calendar.DATE).toDate();
		TimeInterval timeInterval = new TimeInterval(start, end, -1, -1);

		return baseDao.lastGenToken(new SmartQueryWrapper<>()
				.eq("t.status", Tweet.NEW_TOKEN)
				.between("t.update_time", timeInterval) // idx_addTime_status 索引
				.eq("te.type", TokenEvent.TYPE_HOT)
				.eq("te.status", TokenEvent.CREATE) // 状态必须是 未创建
				.orderByDesc("t.update_time")
				.last("LIMIT " + limit)
		);
	}

	public List<Tweet> lastGenToken(Integer limit) {
		return baseDao.lastGenToken(new SmartQueryWrapper<>()
				.eq("t.status", Tweet.SYNC)
				.between("t.add_time", lastInterval()) // idx_addTime_status 索引
				.isNull("te.tweet_id")
				// .orderByDesc("(t.score + tu.score)")
				.orderByDesc("t.add_time")
				.last("LIMIT " + limit)
		);
	}

	public List<Tweet> lastList(Integer limit) {
		return baseDao.lastList(new SmartQueryWrapper<>()
				.eq(Tweet.STATUS, Tweet.INIT)
				.between(Tweet.ADD_TIME, lastInterval()) // idx_addTime_status 索引
				.orderByDesc(Tweet.SCORE)
				.last("LIMIT " + limit)
		);
	}

	@Transactional
	public void syncStatus(List<Tweet> tweets) {
		if (!tweets.isEmpty()) {
			super.update(new UpdateWrapper<Tweet>()
					.set(Tweet.STATUS, Tweet.SYNC)
					.eq(Tweet.STATUS, Tweet.INIT)
					.in(Tweet.ID, CollectionUtil.toList(tweets, Tweet::getId)));
		}
	}

	@Transactional
	public void sync(List<TweetInfo> tweets, boolean asSurge) {
		if (!tweets.isEmpty()) {
			final int size = tweets.size();
			List<UpdateWrapper<Tweet>> wrappers = new ArrayList<>(size);
			for (TweetInfo tweet : tweets) {
				UpdateWrapper<Tweet> wrapper = new UpdateWrapper<Tweet>()
						.set(Tweet.ORG_MSG, Jsons.encode(tweet))
						.eq(Tweet.TWEET_ID, tweet.id);
				if (!asSurge) {
					wrapper.set(Tweet.STATUS, Tweet.SYNC)
							.eq(Tweet.STATUS, Tweet.INIT);
				}
				final String text = tweet.getText();
				if (StringUtils.isNotEmpty(text)) {
					wrapper.set(Tweet.TEXT, text);
				} else {
					log.warn("【{}】推文无内容：{}", tweet.id, Jsons.encode(tweet));
				}
				if (tweet.publicMetrics != null) {
					wrapper.set(X.isValid(tweet.publicMetrics.retweetCount), Tweet.RETWEET, tweet.publicMetrics.retweetCount)
							.set(X.isValid(tweet.publicMetrics.replyCount), Tweet.REPLY, tweet.publicMetrics.replyCount)
							.set(X.isValid(tweet.publicMetrics.likeCount), Tweet.LIKES, tweet.publicMetrics.likeCount)
							.set(X.isValid(tweet.publicMetrics.quoteCount), Tweet.QUOTE, tweet.publicMetrics.quoteCount)
							.set(X.isValid(tweet.publicMetrics.bookmarkCount), Tweet.BOOKMARK, tweet.publicMetrics.bookmarkCount)
							.set(X.isValid(tweet.publicMetrics.impressionCount), Tweet.IMPRESSION, tweet.publicMetrics.impressionCount)
							.set(Tweet.UPDATE_TIME, new Date());
				} else {
					log.warn("【{}】推文无统计数据：{}", tweet.id, Jsons.encode(tweet));
				}
				wrappers.add(wrapper);
			}
			super.updateBatchByWrappers(wrappers);
		}
	}

}
