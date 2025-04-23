package com.candlk.webapp.ws;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.concurrent.CompletionStage;

import com.candlk.webapp.user.model.WsListenerType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class X3WsListener implements Listener, WsListenerApi {

	public WebSocket webSocket;

	@Override
	public WsListenerType getProvider() {
		return WsListenerType.X3;
	}

	@Override
	public void connection() {
		// 构建 HttpClient 并设置线程池
		HttpClient client = HttpClient.newBuilder().executor(WS_EXECUTOR).build();

		// 建立连接
		X3WsListener listener = new X3WsListener();
		this.webSocket = client.newWebSocketBuilder()
				.header("Origin", "https://www.x3.pro")
				.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36")
				.buildAsync(URI.create("wss://www.x3.pro/api/ws?Authorization=006a90e6-52f0-4497-af6c-e9db41ae3641"), listener)
				.join();
	}

	@Override
	public boolean ping() {
		this.webSocket.sendPing(PING);
		return true;
	}

	@Override
	public void onOpen(WebSocket webSocket) {
		System.out.println("[WebSocket] Connected.");

		// 订阅：包含CA的所有帖子
		webSocket.sendText("{\"action\":1,\"topic\":\"CA_ALL\"}", true);
		// 订阅：实时帖子
		webSocket.sendText("{\"action\":1,\"topic\":\"REALTIME\"}", true);
		// 订阅：实时帖子
		webSocket.sendText("{\"action\":1,\"topic\":\"REALTIME\"}", true);

		// 定时任务间隔1分钟发送一次ping心跳

		Listener.super.onOpen(webSocket);
	}

	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
		// 并发处理消息（交给线程池）
		WS_EXECUTOR.submit(() -> {
			System.out.println("[Message] " + data);

		});
		return Listener.super.onText(webSocket, data, last);
	}

	@Override
	public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
		System.out.printf("[WebSocket] Closed. Code: %d, Reason: %s%n", statusCode, reason);
		return Listener.super.onClose(webSocket, statusCode, reason);
	}

	@Override
	public void onError(WebSocket webSocket, Throwable error) {
		System.err.println("[WebSocket] Error: " + error.getMessage());
		Listener.super.onError(webSocket, error);
	}

}