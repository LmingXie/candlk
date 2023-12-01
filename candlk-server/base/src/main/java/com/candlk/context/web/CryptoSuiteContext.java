package com.candlk.context.web;

import javax.annotation.Nullable;

import com.candlk.common.security.AES;
import lombok.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = CryptoSuiteContext.PREFIX)
public class CryptoSuiteContext implements InitializingBean {

	static final String PREFIX = "webapp.context.encrypt";

	boolean enabled = true;
	String aesKey;

	@Setter(AccessLevel.NONE)
	@Nullable
	transient AES aes;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (isEnabled()) {
			this.aes = new AES(aesKey);
		}
	}

	public static String getIvParams(String sign) {
		return sign.substring(4, 20);
	}

}
