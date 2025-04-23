package com.candlk.webapp.job;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.candlk.webapp.user.model.WsListenerType;
import com.candlk.webapp.ws.WsListenerApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
public class PingJob {

	@PostConstruct
	public void init() {
		for (WsListenerType type : WsListenerType.CACHE) {
			WsListenerApi.getInstance(type);
		}
	}

	@Scheduled(cron = "${service.cron.PingJob:0 0/1 * * * ?}")
	public void run() throws Exception {
		log.info("开始执行心跳任务...");
		EnumMap<WsListenerType, WsListenerApi> map = WsListenerApi.implMapRef.get();
		for (Map.Entry<WsListenerType, WsListenerApi> entry : map.entrySet()) {
			if (entry.getKey().isOpen()) {
				boolean ping = entry.getValue().ping();
				if (!ping) {
					log.warn("监听器【{}】心跳检测失败！", entry.getKey());
				}
			}
		}
		// log.info("结束执行心跳任务。");
	}

}
