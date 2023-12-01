package com.candlk.common.context;

import java.util.*;

import org.springframework.context.support.*;

/**
 * Spring Properties 配置文件 属性解密类
 *

 * @date 2018年11月11日
 */
public class EncryptedPropertyPlaceholderConfigurer extends PropertySourcesPlaceholderConfigurer {

	@Override
	protected void convertProperties(Properties props) {
		EncryptedPropertiesResolver.getInstance(props).convertProperties(props);
	}

}
