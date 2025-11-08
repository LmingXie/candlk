package com.bojiu.context.web;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import com.alibaba.fastjson2.*;
import com.alibaba.fastjson2.support.config.FastJsonConfig;
import com.alibaba.fastjson2.support.spring.http.converter.FastJsonHttpMessageConverter;
import com.bojiu.common.web.Logs;
import me.codeplayer.util.JavaUtil;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * 为了方便 iOS 上架过审，所以要对 iOS 端的 JSON 输出数据做统一替换处理，将既定的敏感词属性名 A 自动替换为 对应的 B
 */
public class EnhanceFastJsonHttpMessageConverter extends FastJsonHttpMessageConverter {

	public EnhanceFastJsonHttpMessageConverter() {
		setDefaultCharset(StandardCharsets.UTF_8);
	}

	static void logIfNeeded(String attrName, Object val) {
		RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
		if (attributes != null) {
			attributes.setAttribute(attrName, val, RequestAttributes.SCOPE_REQUEST);
		}
	}

	static void logRequestIfNeeded(Object val) {
		if (val instanceof byte[] bytes) {
			try {
				val = new String(bytes, StandardCharsets.UTF_8);
			} catch (Throwable e) {
				val = "Failed to obtain request body: " + e;
			}
		}
		logIfNeeded(Logs.REQUEST_BODY, val);
	}

	static void logResponseIfNeeded(Object val) {
		logIfNeeded(Logs.RESPONSE, val);
	}

	@Override
	public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		return readType(getType(type, contextClass), inputMessage);
	}

	@Override
	protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
		return readType(getType(clazz, null), inputMessage);
	}

	protected Object readType(Type type, HttpInputMessage inputMessage) throws IOException {
		final long contentLength = inputMessage.getHeaders().getContentLength(); // -1 表示未知
		final byte[] body = Logs.readAll(inputMessage.getBody(), contentLength);
		logRequestIfNeeded(body);
		final FastJsonConfig config = getFastJsonConfig();
		try {
			return JSON.parseObject(body, type, config.getDateFormat(), config.getReaderFilters(), config.getReaderFeatures());
		} catch (JSONException ex) {
			throw new HttpMessageNotReadableException("JSON parse error: " + ex.getMessage(), ex, inputMessage);
		}
	}

	@Override
	protected void writeInternal(Object object, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
		HttpHeaders headers = outputMessage.getHeaders(); // 一定要在 write 之前，否则会报错
		final int contentLength;

		if (object instanceof String str) {
			logResponseIfNeeded(str);
			byte[] data = JavaUtil.getUtf8Bytes(str);
			contentLength = data.length;
			outputMessage.getBody().write(data);
		} else if (object instanceof byte[] data) {
			contentLength = data.length;
			outputMessage.getBody().write(data);
		} else {
			try {
				if (object instanceof JSONPObject) {
					headers.setContentType(APPLICATION_JAVASCRIPT);
				} else {
					headers.setContentType(MediaType.APPLICATION_JSON);
				}
				final String json = Jsons.encode(object);
				logResponseIfNeeded(json);
				final byte[] bytes = JavaUtil.getUtf8Bytes(json);
				outputMessage.getBody().write(bytes);
				contentLength = bytes.length;
			} catch (JSONException ex) {
				throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage(), ex);
			}
		}

		if (headers.getContentLength() < 0) {
			headers.setContentLength(contentLength);
		}
	}

}