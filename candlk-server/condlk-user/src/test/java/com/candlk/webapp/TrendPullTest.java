package com.candlk.webapp;

import javax.annotation.Resource;

import com.candlk.context.web.Jsons;
import com.candlk.webapp.job.TrendJob;
import com.candlk.webapp.trend.TrendApi;
import com.candlk.webapp.trend.impl.RedditListImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@SpringBootTest(classes = UserApplication.class)
@ExtendWith({ SpringExtension.class, MockitoExtension.class })
public class TrendPullTest {

	static final String proxyConfig = "http://127.0.0.1:10809";

	public static void main(String[] args) throws Exception {
		// final TrendApi api = new GoogleTrendImpl("http://127.0.0.1:10809");
		// final TrendApi api = new Trends24Impl();
		final TrendApi api = new RedditListImpl();
		System.out.println(" 全部关键词：" + Jsons.encode(api.pull()));
	}

	@Resource
	private TrendJob trendJob;

	@Test
	public void testPull() {
		trendJob.run();
	}

}
