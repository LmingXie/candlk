package com.bojiu.webapp.user.bet;

import java.net.URI;
import java.net.http.WebSocket;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletionStage;

import com.bojiu.webapp.user.dto.BetApiConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class WsBaseBetApiImpl extends LoginBaseBetApiImpl implements WebSocket.Listener {

	protected WebSocket ws;

	protected abstract String getWsUrl();

	public WebSocket getWsConnection() {
		if (ws == null) {
			final String wsUrl = getWsUrl();
			// 建立连接
			final BetApiConfig config = this.getConfig();
			final WebSocket.Builder builder = currentClient().newWebSocketBuilder()
					.connectTimeout(Duration.of(15, ChronoUnit.SECONDS));
			LOGGER.info("【{}】开始建立 WebSocket 连接：{}", getProvider(), wsUrl);
			this.ws = builder
					.header("Origin", config.endPoint)
					.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36")
					.buildAsync(URI.create(wsUrl), this)
					.join();
		}
		return ws;
	}

	/** 心跳消息 */
	protected abstract String getPingMsg();

	@Override
	public void onOpen(WebSocket webSocket) {
		WebSocket.Listener.super.onOpen(webSocket);
		LOGGER.info("【{}】建立 WebSocket 连接成功！", getProvider());
	}

	@Override
	public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
		this.ws = null;
		return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
	}

	@Override
	public void onError(WebSocket webSocket, Throwable error) {
		LOGGER.error("【{}】WS连接异常：{}", getProvider(), error);
		WebSocket.Listener.super.onError(webSocket, error);
	}

}
