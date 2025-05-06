package com.candlk.webapp.user.service;

import java.io.IOException;
import java.util.List;
import javax.annotation.Resource;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.candlk.common.web.Page;
import com.candlk.webapp.base.service.BaseServiceImpl;
import com.candlk.webapp.es.ESEngineClient;
import com.candlk.webapp.user.dao.TweetWordDao;
import com.candlk.webapp.user.entity.TweetWord;
import com.candlk.webapp.user.model.ESIndex;
import me.codeplayer.util.CollectionUtil;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 推特词库 服务实现类
 *
 * @since 2025-04-27
 */
@Service
public class TweetWordService extends BaseServiceImpl<TweetWord, TweetWordDao, Long> {

	@Resource
	ESEngineClient esEngineClient;

	public List<TweetWord> findByWords(List<String> words) {
		return selectList(new QueryWrapper<TweetWord>().in(TweetWord.WORDS, words));
	}

	public Page<TweetWord> search(Page<TweetWord> page, String words, Integer type) throws IOException {

		long pageSize = page.getSize();
		long from = (page.offset() - 1) * pageSize;

		SearchResponse<TweetWord> response = esEngineClient.client.search(s -> {
			s.index((TweetWord.TYPE_STOP == type ? ESIndex.KEYWORDS_ACCURATE_INDEX : ESIndex.STOP_WORDS_INDEX).value)
					.query(q -> q.wildcard(w -> w
							.field("words")
							.value("*" + words + "*")))
					.from((int) from)
					.size((int) pageSize);

			return s;
		}, TweetWord.class);
		List<TweetWord> list = ESEngineClient.toT(response);
		return page.castList(list);
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

}
