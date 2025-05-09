package com.candlk.webapp;

import com.candlk.webapp.ws.GmgnTweetWsProvider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GmgnTwitterWsClient {

	public static void main(String[] args) throws InterruptedException {

		runListener();
	}

	public static void runListener() throws InterruptedException {
		GmgnTweetWsProvider provider = new GmgnTweetWsProvider("http://127.0.0.1:10809");
		provider.connection();
		// 阻塞主线程
		Thread.currentThread().join();
	}

}
