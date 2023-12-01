package com.candlk.webapp.base.form;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson2.JSON;
import com.candlk.common.context.Context;
import com.candlk.common.security.AES;
import com.candlk.context.model.BaseI18nKey;
import com.candlk.context.web.CryptoSuiteContext;
import lombok.*;
import me.codeplayer.util.Assert;
import me.codeplayer.util.StringUtil;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * body属性为 JSON 格式的抽象表单接口标识
 */
@Getter
@Setter
public abstract class JsonBodyForm<E> extends BaseForm<E> {

	protected String body;
	protected E jsonBody;

	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	protected final Type _type;

	public JsonBodyForm() {
		Type superClass = getClass().getGenericSuperclass();
		_type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
	}

	private static CryptoSuiteContext cryptoSuiteContext;

	public final void setBody(String body) {
		if (StringUtil.notEmpty(body)) {
			HttpServletRequest request = loadDecryptRequest();
			if (request != null) { // 需要解密
				CryptoSuiteContext context = cryptoSuiteContext;
				if (context == null) {
					cryptoSuiteContext = context = Context.getBean(CryptoSuiteContext.class);
				}
				final AES aes = context.getAes();
				/*
				#### 请求数据加密、签名
				1. 发送请求数据时，**只需要**对 `body` 参数（该参数数据要么为空，要么一定是JSON格式）的参数值进行加密。
				2.
				*/
				String sign = request.getHeader("Sign");
				Assert.isTrue(sign != null && sign.length() >= 20, BaseI18nKey.SECURITY_SIGN_REQUIRED);
				sign = CryptoSuiteContext.getIvParams(sign);
				final byte[] data = Base64.getDecoder().decode(body);
				final byte[] ivBytes = sign.getBytes(StandardCharsets.UTF_8);
				byte[] bytes;
				try {
					bytes = aes.decrypt(data, ivBytes);
				} catch (GeneralSecurityException e) {
					throw new IllegalArgumentException("Data decode failed!", e);
				}
				body = new String(bytes, StandardCharsets.UTF_8);
			}
			jsonBody = JSON.parseObject(body, _type);
		}
		this.body = body;
	}

	@Nullable
	protected HttpServletRequest loadDecryptRequest() {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		return doLoadDecryptRequest(attributes.getRequest());
	}

	@Nullable
	protected HttpServletRequest doLoadDecryptRequest(HttpServletRequest request) {
		return request;
	}

}
