package com.bojiu.webapp.user.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.annotation.PostConstruct;

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

	public String basePath = "D:\\mnt\\tg",
			imgPath = basePath + "\\img",
			videoPath = basePath + "\\video",
			avatarPath = basePath + "\\avatar",
			filePath = basePath + "\\file";

	@PostConstruct
	public void init() {
		// 确保目录存在
		try {
			Files.createDirectories(Paths.get(basePath));
			Files.createDirectories(Paths.get(imgPath));
			Files.createDirectories(Paths.get(videoPath));
			Files.createDirectories(Paths.get(avatarPath));
			Files.createDirectories(Paths.get(filePath));
		} catch (IOException ignore) {
		}
	}

}