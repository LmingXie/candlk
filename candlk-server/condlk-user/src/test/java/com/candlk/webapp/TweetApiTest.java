package com.candlk.webapp;

import java.util.List;
import javax.annotation.Resource;

import com.candlk.common.model.Messager;
import com.candlk.context.web.Jsons;
import com.candlk.webapp.api.TweetApi;
import com.candlk.webapp.api.TweetInfo;
import com.candlk.webapp.user.entity.Tweet;
import com.candlk.webapp.user.service.TweetService;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.StringUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@SpringBootTest(classes = UserApplication.class)
@ExtendWith({ SpringExtension.class, MockitoExtension.class })
public class TweetApiTest {

	@Resource
	TweetService tweetService;

	static TweetApi tweetApi;

	@BeforeAll
	public static void beforeAll() {
		tweetApi = new TweetApi("AAAAAAAAAAAAAAAAAAAAAK450wEAAAAAGq8cOrQ4HTVBBn9Z24umOk8kmik%3DkjB0pGI1V3v3c9WkcQCRVjbfa4DPxJdeTxsF0hWVnIuXrOPVVv",
				"http://127.0.0.1:10809");
	}

	@Test
	public void test() {
		List<Tweet> tweetList = tweetService.lastList(100);
		final String tweetIds = StringUtil.join(tweetList, Tweet::getTweetId, ",");
		log.info("推文ID：{}", tweetIds);
		Messager<List<TweetInfo>> tweets = tweetApi.tweets(tweetIds);
		log.info("推文：{}", Jsons.encode(tweets));
	}

}

