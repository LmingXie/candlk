package com.bojiu.context.web;

import org.springframework.core.convert.converter.Converter;

/**
 * 在入参时，对 图片进行压缩与CDN处理
 *
 * @see ImageConverter#convert(String)
 */
public class ImageConverter implements Converter<String, String> {

	/** 旧的 AWS CDN地址前缀 */
	public static final String ORIGIN_ENDPOINT = "https://s3.us-east-2.amazonaws.com/fungamecdn.com";
	/** 新的 AWS CDN地址前缀 */
	public static final String CDN = "https://fungamecdn.com";

	@Override
	public String convert(String source) {
		if (source.length() > ORIGIN_ENDPOINT.length()) {
			return source.replace(ORIGIN_ENDPOINT, CDN);
		}
		return source;
	}

}