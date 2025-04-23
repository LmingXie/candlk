package com.candlk.webapp.ws;

import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.*;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.candlk.common.security.AES;
import com.candlk.context.web.Jsons;
import com.candlk.webapp.user.entity.AxiomTwitter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AxiomWsListener implements Listener {

	// 使用固定线程池（核心线程数可以按实际 CPU 或事件处理量设定）
	public static final ExecutorService AXIOM_EXECUTOR = Executors.newFixedThreadPool(8);

	public static final byte[] key = "Z>vB5uO^yJ/JQf#|w6p:=Va!fY_8W+IA".getBytes(StandardCharsets.UTF_8);
	public static final AES aes = new AES(key);
	public static final Base64.Decoder decoder = Base64.getDecoder();
	private WebSocket webSocket;

	@Override
	public void onOpen(WebSocket webSocket) {
		this.webSocket = webSocket;
		System.out.println("[WebSocket] Connected.");

		// 订阅：活跃账户列表变更事件
		// webSocket.sendText("{\"action\":\"join\",\"room\":\"twitter_active_list\"}", true);
		// 订阅：帖子变更事件（活跃账户）
		webSocket.sendText("{\"action\":\"join\",\"room\":\"twitter_feed_v2\"}", true);

		Listener.super.onOpen(webSocket);
	}

	List<String> eventTypes = List.of(
			"tweet.update", // 帖子变更事件（活跃账户）
			"following.update", // 账户关注列表变更
			"profile.update", // 用户的资料信息有变更
			"profile.pinned.update" //用户置顶的推文发生了变化
	);

	@Override
	public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
		// 并发处理消息（交给线程池）
		AXIOM_EXECUTOR.submit(() -> {
			// System.out.println("[Message] " + data);

			AxiomTwitter axiomTwitter = Jsons.parseObject(data.toString(), AxiomTwitter.class);
			if (axiomTwitter != null) {
				final String event = axiomTwitter.content.event;
				String[] split = event.split(":");
				byte[] iv = decoder.decode(split[0]);
				try {
					final byte[] decrypt = aes.decrypt(decoder.decode(split[1]), iv);
					JSONObject postInfo = JSON.parseObject(decrypt);
					// System.out.println("解密：" + Jsons.encode(postInfo));
					final String eventType = axiomTwitter.content.eventType;
					if (eventTypes.contains(eventType)) {
						log.info("账户={} 订阅类型={} 事件ID={} 事件内容={}", axiomTwitter.content.handle, axiomTwitter.content.subscriptionType, axiomTwitter.content.eventId, Jsons.encode(postInfo));
					} else {
						log.warn("未知事件类型：账户={} 订阅类型={} 事件ID={} 事件内容={}", axiomTwitter.content.handle, axiomTwitter.content.subscriptionType, axiomTwitter.content.eventId, Jsons.encode(postInfo));
					}
				} catch (GeneralSecurityException e) {
					log.error("解密失败：", e);
				}
			}
			// 可扩展：解析 JSON、分类路由等逻辑
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