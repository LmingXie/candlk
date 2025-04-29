package com.candlk.webapp;

import java.io.File;
import java.util.List;
import javax.annotation.Resource;

import com.alibaba.fastjson2.JSONObject;
import com.candlk.common.model.Messager;
import com.candlk.context.web.Jsons;
import com.candlk.webapp.api.*;
import com.candlk.webapp.user.service.TweetService;
import com.candlk.webapp.user.service.TweetUserService;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.FileUtil;
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
	@Resource
	TweetUserService tweetUserService;

	static TweetApi tweetApi;

	@BeforeAll
	public static void beforeAll() {
		tweetApi = new TweetApi("AAAAAAAAAAAAAAAAAAAAAK450wEAAAAAGq8cOrQ4HTVBBn9Z24umOk8kmik%3DkjB0pGI1V3v3c9WkcQCRVjbfa4DPxJdeTxsF0hWVnIuXrOPVVv",
				"http://127.0.0.1:10809");
	}

	@Test
	public void test() {
		List<String> tweetList = tweetService.lastList(100);
		final String tweetIds = StringUtil.joins(tweetList, ",");
		log.info("推文ID：{}", tweetIds);
		Messager<List<TweetInfo>> tweets = tweetApi.tweets(tweetIds);
		log.info("推文：{}", Jsons.encode(tweets));
	}

	@Test
	public void testSync() {
		final String jsonData = FileUtil.readContent(new File("D:\\tweet2.json"));
		JSONObject data = Jsons.parseObject(jsonData);
		List<TweetInfo> tweets = data.getList("data", TweetInfo.class);
		tweetService.sync(tweets);
	}

	@Test
	public void testSyncUserInfo() {
		final String jsonData = FileUtil.readContent(new File("D:\\tweetUser.json"));
		JSONObject data = Jsons.parseObject(jsonData);
		// 同步用户信息数据
		List<TweetUserInfo> tweets = data.getList("data", TweetUserInfo.class);
		tweetUserService.sync(tweets);
	}

}

