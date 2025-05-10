package com.candlk.webapp.job;

import java.util.*;
import javax.annotation.Resource;

import com.candlk.common.redis.RedisUtil;
import com.candlk.context.model.RedisKey;
import com.candlk.context.web.Jsons;
import com.candlk.webapp.trend.TrendApi;
import com.candlk.webapp.user.entity.TweetWord;
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

	@Scheduled(cron = "${service.cron.TrendJob:0 0/30 * * * ?}")
	public void run() {
		if (!RedisUtil.getStringRedisTemplate().opsForSet().isMember(RedisKey.SYS_SWITCH, RedisKey.TWEET_TREND_FLAG)) {
			log.info("【爬取趋势热词】开关关闭，跳过执行...");
			return;
		}
		log.info("开始执行 爬取趋势热词 任务...");

		for (TrendProvider type : TrendProvider.CACHE) {
			final TrendApi instance = TrendApi.getInstance(type);
			try {
				final Set<String> words = instance.pull();
				if (CollectionUtils.isNotEmpty(words)) {
					final List<List<String>> batchWords = Lists.partition(new ArrayList<>(words), 2000);
					splitImportWords(batchWords, type);
					log.info("【{}】查询趋势热词成功，总共录得关键词：{}", type, words.size());
				}
			} catch (Exception e) {
				log.error("【{}】查询趋势热词失败：{}", type, e);
			}
		}

	}

	public void splitImportWords(List<List<String>> batchWords, TrendProvider type) {
		for (List<String> batch : batchWords) {
			log.info("【{}】开始导入关键词：{} {}", type, batch.size(), Jsons.encode(batch));
			final Set<String> oldWords = tweetWordService.findWords(batch);
			if (!oldWords.isEmpty()) {
				batch.removeIf(t -> CollectionUtil.findFirst(oldWords, t::equals) != null);
			}
			if (!batch.isEmpty()) {
				final Date now = new Date();
				final List<TweetWord> tweetWords = new ArrayList<>(batch.size());
				final int typeSecond = TweetWord.TYPE_SECOND;
				for (String word : batch) {
					if (!StringUtil.isEmpty(word)) {
						final TweetWord tweetWord = new TweetWord();
						final String trim = word.trim();
						tweetWord.setProviderType(type.value);
						tweetWord.setWords(trim);
						tweetWord.setType(typeSecond);
						tweetWord.setPriority(0);
						tweetWord.initTime(now);
						tweetWords.add(tweetWord);
					}
				}
				try {
					tweetWordService.batchAdd(tweetWords, typeSecond);
				} catch (Exception e) {
					log.error("导入失败", e);
				}
			}
		}

	}

}
