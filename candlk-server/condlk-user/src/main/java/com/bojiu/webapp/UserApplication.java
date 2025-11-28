package com.bojiu.webapp;

import java.io.IOError;
import java.io.IOException;

import com.bojiu.common.context.Context;
import com.bojiu.common.context.ExtendAnnotationBeanNameGenerator;
import com.bojiu.context.BaseApplication;
import com.bojiu.webapp.user.handler.TdlibLogMessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @see com.bojiu.context.config.WebMvcConfig
 */
@Slf4j
@EnableScheduling
@SpringBootApplication(scanBasePackages = Context.BASE_PACKAGE, nameGenerator = ExtendAnnotationBeanNameGenerator.class)
public class UserApplication extends BaseApplication {

	public static void main(String[] args) {
		Client.setLogMessageHandler(0, new TdlibLogMessageHandler());

		// 禁用TDLib日志，并将致命错误和普通日志消息重定向到一个文件
		try {
			Client.execute(new TdApi.SetLogVerbosityLevel(3));  // 0=none, 1=error, 2=warn, 3=info, 4+=debug
			Client.execute(new TdApi.SetLogStream(new TdApi.LogStreamFile("tdlib.log", 1 << 27, false)));
		} catch (Client.ExecutionException error) {
			throw new IOError(new IOException("Write access to the current directory is required"));
		}
		startup(UserApplication.class, args);
	}

}
