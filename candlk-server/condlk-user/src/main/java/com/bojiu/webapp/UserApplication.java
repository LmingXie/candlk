package com.bojiu.webapp;

import com.bojiu.common.context.Context;
import com.bojiu.common.context.ExtendAnnotationBeanNameGenerator;
import com.bojiu.context.BaseApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @see com.bojiu.context.config.WebMvcConfig
 */
@Slf4j
@SpringBootApplication(scanBasePackages = Context.BASE_PACKAGE, nameGenerator = ExtendAnnotationBeanNameGenerator.class)
public class UserApplication extends BaseApplication {

	public static void main(String[] args) {
		startup(UserApplication.class, args);
	}

}
