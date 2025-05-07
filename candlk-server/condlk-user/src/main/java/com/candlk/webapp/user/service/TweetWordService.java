package com.candlk.webapp.user.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;

import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.candlk.common.dao.SmartQueryWrapper;
import com.candlk.common.web.Page;
import com.candlk.webapp.base.service.BaseServiceImpl;
import com.candlk.webapp.es.ESEngineClient;
import com.candlk.webapp.user.dao.TweetWordDao;
import com.candlk.webapp.user.entity.TweetUser;
import com.candlk.webapp.user.entity.TweetWord;
import com.candlk.webapp.user.form.TweetWordQuery;
import com.candlk.webapp.user.model.ESIndex;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.*;
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
		long pageSize = page.getSize();
		long from = page.offset();

		SearchResponse<TweetWord> response = esEngineClient.client.search(s -> {
			boolean notStop = query.type == null || TweetWord.TYPE_STOP != query.type;
			s.index((notStop ? ESIndex.KEYWORDS_ACCURATE_INDEX : ESIndex.STOP_WORDS_INDEX).value)
					.from((int) from)
					.size((int) pageSize);

			if (X.isValid(query.words)) {
				s.query(q -> q.wildcard(w -> w
						.field(TweetWord.WORDS)
						.value("*" + query.words + "*")));
			}

			if (query.type != null && TweetWord.TYPE_STOP != query.type) {
				s.query(q -> q
						.term(t -> t
								.field(TweetWord.TYPE)
								.value(query.type)
						)
				);
			}
			if (notStop) {
				s.sort(so -> so
						.field(f -> f
								.field(TweetWord.COUNT)
								.order(SortOrder.Desc)
						)
				);
			}

			return s;
		}, TweetWord.class);
		List<TweetWord> list = ESEngineClient.toT(response);

		// 设置总记录数（分页必需）
		long total = response.hits().total() != null ? response.hits().total().value() : 0;
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
					.sort(so -> so
							.field(f -> f
									.field(TweetWord.COUNT)
									.order(SortOrder.Desc)
							)
					)
					.from(0)
					.size(100);

			return s;
		}, TweetWord.class);
		return ESEngineClient.toT(response);
	}

	@Transactional
	public void batchAdd(List<TweetWord> tweetWords, Integer type) throws Exception {
		try {
			super.saveBatch(tweetWords);
		} catch (DuplicateKeyException e) {
			throw new IllegalArgumentException("存在重复的关键词！");
		}
		if (type.equals(TweetWord.TYPE_STOP)) {
			esEngineClient.bulkAddDoc(ESIndex.STOP_WORDS_INDEX, tweetWords);
		} else {
			esEngineClient.bulkAddDoc(ESIndex.KEYWORDS_ACCURATE_INDEX, tweetWords);
		}
	}

	@Transactional
	public void del(List<Long> ids, Integer type) throws Exception {
		final boolean isStop = type.equals(TweetWord.TYPE_STOP);
		if (isStop) {
			List<TweetWord> byIds = findByIds(ids);
			if (byIds.isEmpty()) {
				return;
			}
			esEngineClient.stopWordsCache.removeAll(CollectionUtil.toSet(byIds, TweetWord::getWords));
		}

		super.deleteByIds(ids);

		esEngineClient.batchDelByIds(isStop ? ESIndex.STOP_WORDS_INDEX : ESIndex.KEYWORDS_ACCURATE_INDEX,
				CollectionUtil.toList(ids, Object::toString));

	}

	@Transactional
	public void edit(TweetWord word, Integer type) throws Exception {
		super.update(new UpdateWrapper<TweetWord>().set(TweetWord.TYPE, type).eq(TweetWord.ID, word.getId()));

		// 同步更新ES
		final String wordId = word.getId().toString();
		final UpdateRequest<Object, Object> updateRequest = UpdateRequest.of(b -> b
				.index(ESIndex.KEYWORDS_ACCURATE_INDEX.value)
				.id(wordId)
				.doc(Map.of(TweetUser.TYPE, type))  // 更新字段
		);

		final UpdateResponse<Object> response = esEngineClient.client.update(updateRequest, Object.class);

		if (response.result() != Result.Updated && response.result() != Result.NoOp) {
			log.warn("更新type失败: userId={}, result={}", wordId, response.result());
		} else {
			log.info("成功更新 userId={} 的 type 为 {}", wordId, type);
		}
	}

}
