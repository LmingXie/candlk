package com.candlk.common.web;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import com.candlk.common.context.Context;
import com.candlk.common.model.ValueProxy;
import com.candlk.common.model.ValueProxyImpl;
import lombok.Getter;
import me.codeplayer.util.StringUtil;

/**
 * 客户端类型
 *

 * @date 2015年7月28日
 * @since 1.0
 */
@Getter
public enum Client implements RequestClient, ValueProxyImpl<Client, String> {
	/** PC端 */
	PC("PC", "PC端"),
	/** WAP端 */
	WAP("WAP", "WAP端"),
	/** Android端 */
	APP_ANDROID("Android", "Android"),
	/** iOS端 */
	APP_IOS("iOS", "iOS"),
	/** 系统自动操作 */
	SYSTEM("System", "系统操作"),
	/** 第三方：微信 */
	WECHAT("Wechat", "微信"),
	/** 第三方：微信小程序 */
	WECHAT_MP("WechatMP", "微信小程序"),
	/** 其他未知的客户端 */
	UNKNOWN("?", "未知");

	public final String value;
	public final String label;
	public final ValueProxy<Client, String> proxy;

	static RequestClientResolver resolver;

	Client(String value, String label) {
		this.value = value;
		this.label = label;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	public static Client of(Integer value) {
		Client client = null;
		if (value != null) {
			Client[] values = getValues();
			if (value >= 0 && value < values.length) {
				client = values[value];
			}
		}
		return client;
	}

	public static RequestClientResolver getResolver() {
		RequestClientResolver res = resolver;
		if (res == null) {
			resolver = res = Context.getBean(RequestClientResolver.class, RequestClientResolver.DefaultRequestClientResolver::new);
		}
		return res;
	}

	@Nonnull
	public static Client findClient(String val) {
		if (StringUtil.notEmpty(val)) {
			for (Client client : getValues()) {
				if (val.equals(client.value)) {
					return client;
				}
			}
		}
		return UNKNOWN;
	}

	/**
	 * 根据当前请求，解析并设置来源客户端
	 */
	static RequestClient resolveAndSetClient(HttpServletRequest request, @Nullable String userClientSessionAttr) {
		RequestClient client = getResolver().resolveClient(request);
		return setClientInternal(request, userClientSessionAttr, client);
	}

	/**
	 * 根据当前请求，解析并设置来源客户端
	 */
	@SuppressWarnings("unchecked")
	public static <T extends RequestClient> T resolveAndSetClient(HttpServletRequest request) {
		return (T) resolveAndSetClient(request, Context.internal().getUserClientSessionAttr());
	}

	/**
	 * 根据当前请求，解析并设置来源客户端
	 */
	public static RequestClient setClient(HttpServletRequest request, RequestClient client) {
		return setClientInternal(request, Context.internal().getUserClientSessionAttr(), client);
	}

	/**
	 * 根据当前请求，解析并设置来源客户端
	 */
	static RequestClient setClientInternal(HttpServletRequest request, @Nullable String userClientSessionAttr, RequestClient client) {
		if (StringUtil.notEmpty(userClientSessionAttr)) {
			request.getSession().setAttribute(userClientSessionAttr, client);
		}
		return client;
	}

	/**
	 * 根据当前请求，获取来源客户端
	 */
	@Nonnull
	public static RequestClient getRequestClient(HttpServletRequest request) {
		String userClientSessionAttr = Context.internal().getUserClientSessionAttr();
		RequestClient client = StringUtil.isEmpty(userClientSessionAttr) ? null : (RequestClient) request.getSession().getAttribute(userClientSessionAttr);
		if (client == null) {
			client = resolveAndSetClient(request, userClientSessionAttr);
		}
		return client;
	}

	/**
	 * 根据当前请求，获取来源客户端
	 * 【注意】：不要使用该方法
	 */
	@Nonnull
	@Deprecated
	public static Client getClient(HttpServletRequest request) {
		return getRequestClient(request).mapInternal();
	}

	public static Client[] getValues() {
		return ValueProxy.getCachedAll(Client.class);
	}

	/**
	 * 根据请求来源客户端的数值，获取对应的客户端枚举实例
	 */
	public static Client of(String value) {
		return PC.getValueOf(value);
	}

	@Nonnull
	@Override
	public Client mapInternal() {
		return this;
	}

}
