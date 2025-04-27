package com.candlk.webapp.ws;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.candlk.webapp.user.model.TweetProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class X3TweetWsProvider implements Listener, TweetWsApi {

	public WebSocket webSocket;

	@Override
	public TweetProvider getProvider() {
		return TweetProvider.X3;
	}

	@Override
	public void connection() {
		// 构建 HttpClient 并设置线程池
		HttpClient client = HttpClient.newBuilder().executor(WS_EXECUTOR).build();

		// 建立连接
		X3TweetWsProvider listener = new X3TweetWsProvider();
		this.webSocket = client.newWebSocketBuilder()
				.header("Origin", "https://www.x3.pro")
				.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36")
				.buildAsync(URI.create("wss://www.x3.pro/api/ws?Authorization=" + UUID.randomUUID()), listener)
				.join();
	}

	@Override
	public boolean ping() {
		webSocket.sendText("ping", true);
		return true;
	}

	@Override
	public void onOpen(WebSocket webSocket) {
		System.out.println("[WebSocket] Connected.");

		// 订阅：包含CA的所有帖子
		// webSocket.sendText("{\"action\":1,\"topic\":\"CA_ALL\"}", true);
		// 订阅：焦点帖子
		// webSocket.sendText("{\"action\":1,\"topic\":\"FOCUS\"}", true);
		// 订阅：账号监听帖子
		// webSocket.sendText("{\"action\":1,\"topic\":\"MONITOR\"}", true);
		// 订阅：实时帖子
		webSocket.sendText("{\"action\":1,\"topic\":\"REALTIME\"}", true);
		// 订阅：账号剃刀帖子
		// webSocket.sendText("{\"action\":1,\"topic\":\"SCRAPER_NOTICE\"}", true);

		// 定时任务间隔1分钟发送一次ping心跳

		Listener.super.onOpen(webSocket);
	}

	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
		// 并发处理消息（交给线程池）
		WS_EXECUTOR.submit(() -> {
			System.out.println("[Message] " + data);
			JSONObject postInfo = JSON.parseObject(data.toString());
			if ("1".equals(postInfo.getString("msgType"))) {
				JSONObject postData = postInfo.getJSONObject("data");
				log.info("帖子消息：{}", postData.toJSONString());
			}

		});
		return Listener.super.onText(webSocket, data, last);
	}

	@Override
	public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
		reConnection(webSocket, statusCode, reason);
		return Listener.super.onClose(webSocket, statusCode, reason);
	}

	@Override
	public void onError(WebSocket webSocket, Throwable error) {
		System.err.println("[WebSocket] Error: " + error.getMessage());
		Listener.super.onError(webSocket, error);
	}

}