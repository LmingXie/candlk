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
				.header("Origin", "https://gmgn.ai")
				.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36")
				.buildAsync(URI.create("wss://ws.gmgn.ai/quotation?device_id=64baecd0-f073-411a-8c86-d71e8523f449&client_id=gmgn_web_20250508-860-943999d&from_app=gmgn&app_ver=20250508-860-943999d&tz_name=Asia%2FShanghai&tz_offset=28800&app_lang=zh-CN&fp_did=718668df0879175894ff6925a2175d69&os=web&uuid=bdf79a4625162232"),
						new X3TweetWsProvider())
				.join();

		// 阻塞主线程
		Thread.currentThread().join();
	}

}
