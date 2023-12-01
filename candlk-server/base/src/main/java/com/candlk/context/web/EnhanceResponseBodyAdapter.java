package com.candlk.context.web;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.candlk.common.model.Messager;
import com.candlk.common.security.AES;
import com.candlk.common.web.Page;
import com.candlk.context.auth.ExportInterceptor;
import com.candlk.webapp.base.util.Export;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.*;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 对部分接口的响应数据进行加密和签名
 * 同时支持对 @Export 导出注解进行响应处理
 *
 * @see ExportInterceptor
 */
@Getter
@Setter
@RestControllerAdvice
@Slf4j
public class EnhanceResponseBodyAdapter implements ResponseBodyAdvice<Object> {

	CryptoSuiteContext context;

	public EnhanceResponseBodyAdapter(CryptoSuiteContext context) {
		this.context = context;
	}

	/**
	 * 该方法用于判断当前请求的返回值，是否要执行 {@link #beforeBodyWrite}
	 *
	 * @param methodParameter handler方法的参数对象
	 * @param converterType 将会使用到的 HTTP 消息转换器类类型
	 */
	@Override
	public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> converterType) {
		return context.isEnabled();
	}

	/**
	 * 在 HTTP 消息转换器转换之前执行
	 *
	 * @param body 服务端的响应数据
	 * @param methodParameter handler方法的参数对象
	 * @param mediaType 响应的ContentType
	 * @param converterType 将会使用到的Http消息转换器类类型
	 * @param serverHttpRequest serverHttpRequest
	 * @param serverHttpResponse serverHttpResponse
	 * @return 返回 一个自定义的 HttpInputMessage，可以为 null，表示没有任何响应
	 */
	@Override
	@Nullable
	public Object beforeBodyWrite(@Nullable Object body, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> converterType, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
		HttpServletRequest request = ((ServletServerHttpRequest) serverHttpRequest).getServletRequest();
		if (request.getRequestURI().startsWith("/actuator/")) { // 监控服务，无需响应加密
			return body;
		}
		// 处理导出
		if (ExportInterceptor.isExport(request)) {
			final Export export = methodParameter.getMethodAnnotation(Export.class);
			if (export != null) {
				Object listToExport = request.getAttribute(ExportInterceptor.EXPORT_EXCEL_LIST);
				if (listToExport == null) {
					if (body instanceof Messager<?> msger) {
						if (msger.data() instanceof Page<?> page) {
							listToExport = page.getList();
						} else if (msger.data() instanceof List<?> list) {
							listToExport = list;
						}
					} else if (body instanceof Page<?> page) {
						listToExport = page.getList();
					} else if (body instanceof List) {
						listToExport = body;
					}
				}
				//noinspection unchecked
				final List<Object> toExport = listToExport == null ? Collections.emptyList() : (List<Object>) listToExport;
				ExportInterceptor.handleExport(request, export, toExport, null);
				return null;
			}
		}
		if (body != null) {
			final AES aes = context.getAes();
			boolean shouldEncrypt = aes != null && "1".equals(request.getParameter("encrypt")); // TODO 需要确认要加密的具体接口列表
			if (shouldEncrypt) {
				String jsonStr = Jsons.encode(body);
				HttpServletResponse response = ((ServletServerHttpResponse) serverHttpResponse).getServletResponse();
				final String sign = RandomStringUtils.randomAlphanumeric(24);
				final byte[] ivBytes = CryptoSuiteContext.getIvParams(sign).getBytes(StandardCharsets.UTF_8);
				try {
					body = Base64.encodeBase64(aes.encrypt(jsonStr.getBytes(StandardCharsets.UTF_8), ivBytes));
				} catch (GeneralSecurityException e) {
					log.error("加密响应数据时出错：", e);
					return jsonStr; // 如果报错，直接降级为明文响应
				}
				response.setHeader("Access-Control-Expose-Headers", "Sign");
				response.setHeader("Sign", sign);
			}
		}
		return body;
	}

}
