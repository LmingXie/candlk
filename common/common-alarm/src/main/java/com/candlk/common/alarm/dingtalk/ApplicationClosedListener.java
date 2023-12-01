package com.candlk.common.alarm.dingtalk;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ApplicationClosedListener implements ApplicationListener<ContextClosedEvent> {

	@Override
	public void onApplicationEvent(ContextClosedEvent event) {
		log.info("应用关闭 ");
		try {
			BugSendService bean = event.getApplicationContext().getBean(BugSendService.class);
			bean.sendMsg("应用关闭 ", true);
		} catch (Exception e) {
			log.info("【告警通知】配置有误", e);
		}
	}

}
