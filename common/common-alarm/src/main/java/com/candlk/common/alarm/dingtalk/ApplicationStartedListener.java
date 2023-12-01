package com.candlk.common.alarm.dingtalk;

import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.StringUtil;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

/**
 * 系统启动后通知
 */
@Slf4j
@Component
public class ApplicationStartedListener implements ApplicationListener<ApplicationReadyEvent> {

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		final ConfigurableApplicationContext context = event.getApplicationContext();
		final ConfigurableEnvironment env = context.getEnvironment();
		final String msg = "应用启动 ";
		log.info(msg);
		try {
			BugSendService bean = context.getBean(BugSendService.class);
			bean.sendMsg(msg);

			// 运维专属钉钉通知
			final String url = env.getProperty("warn.service.ops");
			if (StringUtil.notEmpty(url)) {
				final BugSendService instance = BugSendFactory.createInstance(url, null, context.getBean(BugWarnExpiredService.class));
				if (instance != null) {
					instance.sendMsg(msg, true);
				}
			}
		} catch (Exception e) {
			log.info("启动失败", e);
		}
	}

}
