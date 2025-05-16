package com.candlk.webapp.user.service;

import java.io.IOException;
import java.util.*;
import javax.annotation.Resource;

import co.elastic.clients.elasticsearch._types.ScriptSortType;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.candlk.common.dao.SmartQueryWrapper;
import com.candlk.common.util.BeanUtil;
import com.candlk.common.util.Common;
import com.candlk.common.web.Page;
import com.candlk.webapp.base.service.BaseServiceImpl;
import com.candlk.webapp.es.ESEngineClient;
import com.candlk.webapp.job.TrendJob;
import com.candlk.webapp.user.dao.TweetWordDao;
import com.candlk.webapp.user.entity.*;
import com.candlk.webapp.user.form.TweetWordQuery;
import com.candlk.webapp.user.model.ESIndex;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.CollectionUtil;
import me.codeplayer.util.X;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 推特词库 服务实现类
 *
 * @since 2025-04-27
 */
@Slf4j
@Service
public class TweetWordService extends BaseServiceImpl<TweetWord, TweetWordDao, Long> {

	@Resource
	ESEngineClient esEngineClient;

	public Page<TweetWord> findPage(Page<?> page, TweetWordQuery query) {
		return baseDao.findPage(page, new SmartQueryWrapper<TweetWord>()
				.like(TweetWord.WORDS, query.words)
				.eq(TweetWord.TYPE, query.type)
				.orderByDesc(TweetUser.ID)
		);
	}

	public List<TweetWord> findByWords(List<String> words) {
		return selectList(new QueryWrapper<TweetWord>().in(TweetWord.WORDS, words));
	}

	public Page<TweetWord> search(Page<TweetWord> page, TweetWordQuery query) throws IOException {
		final long pageSize = page.getSize(), from = page.offset();
		final SearchResponse<TweetWord> response = esEngineClient.client.search(s -> {
			boolean notStop = query.type == null || TweetWord.TYPE_STOP != query.type;
			s.index((notStop ? ESIndex.KEYWORDS_ACCURATE_INDEX : ESIndex.STOP_WORDS_INDEX).value)
					.from((int) from)
					.size((int) pageSize);

			s.query(q -> q.bool(b -> {
				// 模糊匹配（忽略大小写）
				if (X.isValid(query.words)) {
					b.must(m -> m.wildcard(w -> w
							.field(TweetWord.WORDS)
							.value("*" + query.words.toLowerCase() + "*")
					));
				}
				// 类型条件
				if (query.type != null && TweetWord.TYPE_STOP != query.type) {
					b.must(m -> m.term(t -> t
							.field(TweetWord.TYPE)
							.value(query.type)
					));
				}
				return b;
			}));

			// 排序
			if (notStop) {
				s.sort(so -> so.script(script -> script
						.type(ScriptSortType.Number)
						.script(sc -> sc
								.lang("painless")
								.source(builder -> builder.scriptString("if (doc['status'].value == 1) return 0; else if (doc['status'].value == 2) return 1; else return 2;"))
						)
				));

				s.sort(so -> so.field(f -> f.field(TweetWord.COUNT).order(SortOrder.Desc)));
			}

			return s;
		}, TweetWord.class);
		final List<TweetWord> list = ESEngineClient.toT(response);

		// 设置总记录数（分页必需）
		final long total = response.hits().total() != null ? response.hits().total().value() : 0;
		page.setTotal(total);
		return page.castList(list);
	}

	public List<TweetWord> rank(Integer type) throws IOException {
		SearchResponse<TweetWord> response = esEngineClient.client.search(s -> {
			s.index((TweetWord.TYPE_STOP == type ? ESIndex.KEYWORDS_ACCURATE_INDEX : ESIndex.STOP_WORDS_INDEX).value)
					.query(q -> q
							.term(t -> t
									.field(TweetWord.TYPE)
									.value(type)
							)
					)
					.sort(so -> so.field(f -> f.field(TweetWord.COUNT).order(SortOrder.Desc)))
					.from(0)
					.size(100);

			return s;
		}, TweetWord.class);
		return ESEngineClient.toT(response);
	}

	public List<String> findWords(Collection<String> words) {
		return baseDao.findWords(new QueryWrapper<TweetWord>().in(TweetWord.WORDS, words));
	}

	@Transactional
	public void batchAdd(List<TweetWord> tweetWords, Integer type) throws Exception {
		try {
			super.saveBatch(tweetWords);
		} catch (DuplicateKeyException e) {
			log.error("存在重复的关键词：", e);
			throw new IllegalArgumentException("存在重复的关键词！");
		}
		if (type.equals(TweetWord.TYPE_STOP)) {
			esEngineClient.bulkAddDoc(ESIndex.STOP_WORDS_INDEX, BeanUtil.convertAndCopy(tweetWords, StopWord::new));
		} else {
			esEngineClient.bulkAddDoc(ESIndex.KEYWORDS_ACCURATE_INDEX, tweetWords);
		}
	}

	@Transactional
	public void batchDel(Set<String> excludeWords) throws Exception {
		delete(new UpdateWrapper<TweetWord>().notIn(TweetWord.WORDS, excludeWords));
		TrendJob.deleteOldHotWords(esEngineClient, excludeWords);
	}

	@Transactional
	public void del(List<TweetWord> words) throws Exception {
		Map<Boolean, List<TweetWord>> group = Common.groupBy(words, t -> TweetWord.TYPE_STOP == t.getType());
		List<TweetWord> stopWords = group.get(true);
		List<TweetWord> nonStopWords = group.get(false);

		super.deleteByIds(CollectionUtil.toList(words, TweetWord::getId));

		if (CollectionUtils.isNotEmpty(stopWords)) {
			esEngineClient.stopWordsCache.removeAll(CollectionUtil.toSet(stopWords, TweetWord::getWords));

			esEngineClient.batchDelByIds(ESIndex.STOP_WORDS_INDEX, CollectionUtil.toList(stopWords, word -> word.getId().toString()));
		}
		if (CollectionUtils.isNotEmpty(nonStopWords)) {
			esEngineClient.batchDelByIds(ESIndex.KEYWORDS_ACCURATE_INDEX, CollectionUtil.toList(nonStopWords, word -> word.getId().toString()));
		}
	}

	@Transactional
	public void edit(List<TweetWord> word, String field, Integer newValue, Date now) throws Exception {
		super.update(new UpdateWrapper<TweetWord>()
				.set(field, newValue)
				.set(TweetWord.UPDATE_TIME, now)
				.in(TweetWord.ID, CollectionUtil.toList(word, TweetWord::getId))
		);

		// 同步更新ES
		final Long updated = esEngineClient.updateFieldByIds(ESIndex.KEYWORDS_ACCURATE_INDEX, word, field, newValue, now);
		log.info("更新结果： field:{} type={} words={} updated={}", field, newValue, word.size(), updated);
	}

}
