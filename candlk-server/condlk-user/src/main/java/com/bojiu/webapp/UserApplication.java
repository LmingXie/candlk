package com.bojiu.webapp;

import com.bojiu.common.context.Context;
import com.bojiu.common.context.ExtendAnnotationBeanNameGenerator;
import com.bojiu.context.BaseApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @see com.bojiu.context.config.WebMvcConfig
 */
@Slf4j
// @EnableScheduling // TODO 开启定时任务
@SpringBootApplication(scanBasePackages = Context.BASE_PACKAGE, nameGenerator = ExtendAnnotationBeanNameGenerator.class)
public class UserApplication extends BaseApplication {

	public static void main(String[] args) {
		startup(UserApplication.class, args);
	}

}
