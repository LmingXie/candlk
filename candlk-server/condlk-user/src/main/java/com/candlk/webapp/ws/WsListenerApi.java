package com.candlk.webapp.ws;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.candlk.context.ContextImpl;
import com.candlk.webapp.user.model.WsListenerType;
import me.codeplayer.util.LazyCacheLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 第三方WebSocket监听器
 */
public interface WsListenerApi {

	Logger log = LoggerFactory.getLogger(WsListenerApi.class);

	/** 生产厂商 */
	WsListenerType getProvider();

	/** 初始化全部已开启的监听器 */
	private static EnumMap<WsListenerType, WsListenerApi> init() {
		EnumMap<WsListenerType, WsListenerApi> map = ContextImpl.newEnumImplMap(WsListenerType.class, WsListenerApi.class, WsListenerApi::getProvider);
		for (Map.Entry<WsListenerType, WsListenerApi> entry : map.entrySet()) {
			WsListenerType type = entry.getKey();
			if (type.isOpen()) {
				try {
					entry.getValue().connection();
					log.info("监听器【{}】初始化成功。", type);
				} catch (Exception e) {
					log.error("监听器【{}】初始化异常：", type, e);
				}
			}
		}
		return map;
	}

	LazyCacheLoader<EnumMap<WsListenerType, WsListenerApi>> implMapRef = new LazyCacheLoader<>(WsListenerApi::init);

	static WsListenerApi getInstance(WsListenerType wsListenerType) {
		return implMapRef.get().get(wsListenerType);
	}

	// 使用固定线程池（核心线程数可以按实际 CPU 或事件处理量设定）
	ExecutorService WS_EXECUTOR = Executors.newFixedThreadPool(8);

	ByteBuffer PING = ByteBuffer.wrap("ping".getBytes(StandardCharsets.UTF_8));

	/** 调用ping指令 */
	default boolean ping() {
		return false;
	}

	/** 建立连接 */
	void connection();

	// 工具函数：从 Set-Cookie 中提取所有 key=value
	static Map<String, String> parseCookies(List<String> cookies) {
		Map<String, String> tokenMap = new HashMap<>();
		for (String cookie : cookies) {
			final String[] parts = cookie.split(";");
			if (parts.length > 0) {
				final String[] kv = parts[0].split("=", 2); // 只分割前两个，防止 token 中含有 =
				if (kv.length == 2) {
					tokenMap.put(kv[0].trim(), kv[1].trim());
				}
			}
		}
		return tokenMap;
	}

}
