package com.candlk.webapp;

import java.net.URI;
import java.net.http.HttpClient;

import com.candlk.webapp.ws.X3TweetWsProvider;
import lombok.extern.slf4j.Slf4j;

import static com.candlk.webapp.ws.TweetWsApi.WS_EXECUTOR;

@Slf4j
public class X3TwitterWsClient {

	public static void main(String[] args) throws InterruptedException {

		runListener();
	}

	public static void runListener() throws InterruptedException {
		// 构建 HttpClient 并设置线程池
		HttpClient client = HttpClient.newBuilder().executor(WS_EXECUTOR).build();

		// 建立连接
		client.newWebSocketBuilder()
				.header("Origin", "https://www.x3.pro")
				.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36")
				.buildAsync(URI.create("wss://www.x3.pro/api/ws?Authorization=006a90e6-52f0-4497-af6c-e9db41ae3641"), new X3TweetWsProvider())
				.join();

		// 阻塞主线程
		Thread.currentThread().join();
	}

}
