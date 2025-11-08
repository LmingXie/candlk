package com.bojiu.context.brand;

import com.bojiu.context.model.SerializedConfig;
import lombok.*;

/** 功能 & 特性 元信息配置 */
@NoArgsConstructor
public class FeatureConfig implements SerializedConfig {

	/** 序列化后的配置字符串，大多是是一个 JSON 字符串，也有可能是一个简单字符串（例如："12"、"true"） */
	@Getter
	@Setter
	String config;
	/** 反序列化后的 Java 对象缓存 */
	public volatile Object parsed;

	/** 为元数据准备的专用缓存对象 */
	public Object cacheForOptions;

	public FeatureConfig(String config) {
		this.config = config;
	}

	@Override
	public String rawConfig() {
		return config;
	}

	@Override
	public Object parsedConfig() {
		return parsed;
	}

	@Override
	public void initParsedConfig(Object parsedConfig) {
		this.parsed = parsedConfig;
	}

}