package com.candlk.webapp.job;

import java.util.*;
import javax.annotation.Resource;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.candlk.common.model.Messager;
import com.candlk.context.web.Jsons;
import com.candlk.webapp.api.DeepSeekApi;
import com.candlk.webapp.api.DeepSeekChat;
import com.candlk.webapp.es.ESEngineClient;
import com.candlk.webapp.user.entity.TokenEvent;
import com.candlk.webapp.user.entity.Tweet;
import com.candlk.webapp.user.model.TweetUserType;
import com.candlk.webapp.user.service.TokenEventService;
import com.candlk.webapp.user.service.TweetService;
import com.candlk.webapp.user.util.ConcurrentExecutor;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.NotionalTokenizer;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.StringUtil;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
// @Configuration
public class GenTokenJob {

	@Resource
	TweetService tweetService;
	@Resource
	TokenEventService tokenEventService;
	@Resource
	ESEngineClient esEngineClient;

	static final DeepSeekApi deepSeekApi = DeepSeekApi.getInstance();

	/**
	 * 根据评分排名，生成Token（生产：1 m/次；本地：1 m/次）
	 */
	@Scheduled(cron = "${service.cron.GenTokenJob:0 0/1 * * * ?}")
	public void run() throws Exception {
		log.info("开始生成Token数据信息...");

		// 根据评分排名，生成Token TODO 调整limit
		List<Tweet> tweets = tweetService.lastGenToken(1);
		if (!tweets.isEmpty()) {
			List<UpdateWrapper<Tweet>> updateWrappers = new ArrayList<>(tweets.size());
			for (Tweet tweet : tweets) {
				updateWrappers.add(new UpdateWrapper<Tweet>()
						.set(Tweet.STATUS, Tweet.NEW_TOKEN)
						.eq(Tweet.ID, tweet.getId()));
			}
			// 更新推文状态
			tweetService.updateBatchByWrappers(updateWrappers);
			ConcurrentExecutor.runConcurrently(tweets, 10, tweet -> {
				try {
					final String[] pair = aiGenToken(tweet.getText());

					// 通过DeepSeek 生成代币名称和符号
					TokenEvent token = new TokenEvent()
							.setTweetId(tweet.getId())
							.setCoin(pair[0])
							.setSymbol(pair[1])
							.setStatus(TokenEvent.CREATE)
							// 热门推文 由定时任务检查是否可以成功 浏览量猛增 推文
							.setType(TweetUserType.SPECIAL == tweet.getUserType()
									? TokenEvent.TYPE_SPECIAL : TokenEvent.TYPE_HOT);
					tokenEventService.save(token);
				} catch (Exception e) {
					log.error("生成代币名称和代币符号失败：", e);
				}
			});
		}
		log.info("结束生成Token数据任务。");
	}

	public String[] aiGenToken(String text) {
		String coin = "", symbol = "";

		try {
			List<Term> segment = NotionalTokenizer.segment(text);

			Set<String> words = new HashSet<>(segment.size());
			for (Term term : segment) {
				// 字符必须大于1 && 不包含在内部停用词中
				if (term.word.length() < 2 || esEngineClient.stopWordsCache.contains(term.word)) {
					continue;
				}
				words.add(term.word);
			}
			Messager<DeepSeekChat> chat = deepSeekApi.chat("根据推文生成代币名称和代币符号，仅输出json格式的{\"name\":\"\",\"symbol\":\"\"}。" + StringUtil.joins(words, " "));
			if (chat.isOK()) {
				DeepSeekChat data = chat.data();
				if (CollectionUtils.isNotEmpty(data.choices)) {
					final String content = data.choices.getFirst().message.content;
					final String fixedText = content.replaceAll("```json", "").replaceAll("```", "");
					if (JSON.isValid(fixedText)) {
						JSONObject tokenInfo = Jsons.parseObject(fixedText);
						coin = tokenInfo.getString("name");
						symbol = tokenInfo.getString("symbol");
					}
				}
			}
		} catch (Exception e) {
			log.error("【DeepSeek】生成代币名称和代币符号失败：", e);
		}
		return new String[] { coin, symbol };
	}

}
