package com.candlk.context.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring.http.converter.FastJsonHttpMessageConverter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;

/**
 * 为了方便 iOS 上架过审，所以要对 iOS 端的 JSON 输出数据做统一替换处理，将既定的敏感词属性名 A 自动替换为 对应的 B
 */
public class EnhanceFastJsonHttpMessageConverter extends FastJsonHttpMessageConverter {

	public static FastJsonConfig fastJsonConfig = new FastJsonConfig();

	public EnhanceFastJsonHttpMessageConverter() {
		setDefaultCharset(StandardCharsets.UTF_8);
		setFastJsonConfig(fastJsonConfig);
	}

	static {
		fastJsonConfig.setDateFormat("millis"); // 日期时间类型默认输出时间戳
	}

	@Override
	protected void writeInternal(Object object, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
		if (object instanceof String str) {
			byte[] data = str.getBytes(StandardCharsets.UTF_8);
			writeBytes(outputMessage, data);
			return;
		} else if (object instanceof byte[] data) {
			writeBytes(outputMessage, data);
			return;
		}
		super.writeInternal(object, outputMessage);
	}

	private static void writeBytes(HttpOutputMessage outputMessage, byte[] data) throws IOException {
		HttpHeaders headers = outputMessage.getHeaders(); // 一定要在 write 之前，否则会报错
		outputMessage.getBody().write(data);
		if (headers.getContentLength() < 0 && fastJsonConfig.isWriteContentLength()) {
			headers.setContentLength(data.length);
		}
	}

}
