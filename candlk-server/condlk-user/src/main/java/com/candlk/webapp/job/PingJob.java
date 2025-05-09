package com.candlk.webapp.job;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.candlk.webapp.user.model.TweetProvider;
import com.candlk.webapp.ws.TweetDeduplicate;
import com.candlk.webapp.ws.TweetWsApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
public class PingJob {

	@PostConstruct
	public void init() {
		for (TweetProvider type : TweetProvider.CACHE) {
			TweetWsApi.getInstance(type);
		}
	}

	@Scheduled(cron = "${service.cron.PingJob:0/30 * * * * ?}")
	public void run() {
		log.info("开始执行心跳任务...");
		EnumMap<TweetProvider, TweetWsApi> map = TweetWsApi.implMapRef.get();
		for (Map.Entry<TweetProvider, TweetWsApi> entry : map.entrySet()) {
			if (entry.getKey().isOpen()) {
				boolean ping = entry.getValue().ping();
				if (!ping) {
					log.warn("监听器【{}】心跳检测失败！", entry.getKey());
				}
			}
		}

		TweetDeduplicate.clear();
		// log.info("结束执行心跳任务。");
	}

}
