package com.bojiu.webapp.user.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 数据配置
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "webapp")
public class UserConfig {

	/** Telegram ApiId */
	public Integer apiId = 94575;
	/** Telegram ApiHash */
	public String apiHash = "a3406de8d171bb422bb6ddf3bbd800e2";

}