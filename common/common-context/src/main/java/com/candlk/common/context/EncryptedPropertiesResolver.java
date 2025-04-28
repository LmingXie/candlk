package com.candlk.common.context;

import java.io.IOException;
import java.util.*;
import javax.annotation.Nullable;

import me.codeplayer.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

public class EncryptedPropertiesResolver {

	static final Logger LOGGER = LoggerFactory.getLogger(EncryptedPropertiesResolver.class);
	static final String DEFAULT_SALT = "DjS$5M&F2vIiJ**g%IC#m0B6.8zMS^T|zx*1vlCNX-l|IW/NT";

	protected String encryptDelimiterStart = "$(";
	protected String encryptDelimiterEnd = ")";
	protected final DES des;

	static EncryptedPropertiesResolver instance;

	public EncryptedPropertiesResolver(String key, String salt) {
		this.des = buildDES(key, salt);
	}

	protected boolean isEncrypted(String val) {
		return StringUtil.notEmpty(val) && val.startsWith(encryptDelimiterStart) && val.endsWith(encryptDelimiterEnd);
	}

	public void setEncryptDelimiterStart(String encryptDelimiterStart) {
		if (StringUtil.notBlank(encryptDelimiterStart)) {
			this.encryptDelimiterStart = encryptDelimiterStart.trim();
		}
	}

	public void setEncryptDelimiterEnd(String encryptDelimiterEnd) {
		if (StringUtil.notBlank(encryptDelimiterEnd)) {
			this.encryptDelimiterEnd = encryptDelimiterEnd.trim();
		}
	}

	public String decryptProperty(String propertyValue) {
		if (isEncrypted(propertyValue)) {
			String encrypted = propertyValue.substring(encryptDelimiterStart.length(), propertyValue.length() - encryptDelimiterEnd.length()).trim();
			String plain = des.decode(encrypted);
			if (!Env.inProduction()) {
				LOGGER.trace("「{}」被解密为「{}」", propertyValue, plain);
			}
			return plain;
		}
		return propertyValue;
	}

	public void convertProperties(Properties props) {
		Enumeration<?> propertyNames = props.propertyNames();
		while (propertyNames.hasMoreElements()) {
			String propertyName = (String) propertyNames.nextElement();
			String propertyValue = props.getProperty(propertyName);
			String convertedValue = decryptProperty(propertyValue);
			//noinspection StringEquality
			if (propertyValue != convertedValue) {
				props.setProperty(propertyName, convertedValue);
			}
		}
	}

	public String encrypt(String source) {
		return des.encode(source);
	}

	public static String encrypt(String key, @Nullable String salt, String source) {
		DES des = buildDES(key, salt);
		return des.encode(source);
	}

	public String decrypt(String ciphertext) {
		return des.decode(ciphertext);
	}

	public static String decrypt(String key, @Nullable String salt, String source) {
		DES des = buildDES(key, salt);
		return des.decode(source);
	}

	public static DES buildDES(String key, @Nullable String salt) {
		salt = X.expectNotNull(salt, DEFAULT_SALT);
		return new DES(generateKey(key, salt));
	}

	public static String generateKey(String key, String salt) {
		String val = StringUtil.concat(key, salt);
		if ("null".equals(val)) {
			return "Undefined!";
		}
		return val;
	}

	public static EncryptedPropertiesResolver getInstance(String key, @Nullable String salt) {
		EncryptedPropertiesResolver resolver = instance;
		if (resolver == null) {
			instance = resolver = new EncryptedPropertiesResolver(key, salt);
		}
		return resolver;
	}

	@SuppressWarnings("rawtypes")
	public static EncryptedPropertiesResolver getInstance(Map props) {
		return getInstance((String) props.get("webapp.key"), (String) props.get("webapp.salt"));
	}

	public static Properties resolveProperties(InputStreamSource resource) {
		Properties props;
		try {
			props = resource instanceof EncodedResource
					? PropertiesLoaderUtils.loadProperties((EncodedResource) resource)
					: PropertiesLoaderUtils.loadProperties((Resource) resource);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to load configuration from：" + resource, e);
		}
		getInstance(props).convertProperties(props);
		return props;
	}

}
