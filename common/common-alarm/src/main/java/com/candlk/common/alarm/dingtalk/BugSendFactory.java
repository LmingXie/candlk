package com.candlk.common.alarm.dingtalk;

import java.util.List;
import javax.annotation.Nullable;

import me.codeplayer.util.StringUtil;

/**
 * 工厂类
 */
public abstract class BugSendFactory {

	/**
	 * 阿里钉钉告警 简易工厂类
	 *
	 * @param urls 如 <code>http://aaa.com#123456,http://aaa.com</code>, 逗号分隔，#号后面是私钥，没有可以不填
	 */
	@Nullable
	public static BugSendService createInstance(String urls, @Nullable String packageName, BugWarnExpiredService bugWarnExpiredService) {
		final List<AlarmEndpoint> endpoints = AlarmEndpoint.parse(urls);
		if (endpoints == null) {
			return null;
		}
		packageName = StringUtil.trim(packageName);
		if (packageName.isEmpty()) {
			packageName = "com.candlk";
		}
		return new BugSendServiceImpl(endpoints, packageName, bugWarnExpiredService);
	}

}
