package com.candlk.webapp;

import com.candlk.common.context.Context;
import com.candlk.common.context.ExtendAnnotationBeanNameGenerator;
import com.candlk.context.BaseApplication;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.CollectionUtil;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @see com.candlk.context.config.WebMvcConfig
 */
@Slf4j
@SpringBootApplication(scanBasePackages = Context.BASE_PACKAGE, nameGenerator = ExtendAnnotationBeanNameGenerator.class)
public class UserApplication extends BaseApplication {

	public static void main(String[] args) {
		startup(UserApplication.class, args);
	}

}
