package com.candlk.common.web;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;

import com.candlk.common.context.Context;
import me.codeplayer.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import static com.candlk.common.web.Client.*;

public interface RequestClientResolver {

	@Nonnull
	default RequestClient resolveClient(final HttpServletRequest request) {
		// App-Id："cp_front/WechatMP/1.1.0/main"
		String AppId = request.getHeader(Context.internal().getAppIdHeaderName());
		if (StringUtil.notEmpty(AppId)) {
			String clientAlias = StringUtils.substringBetween(AppId, "/", "/");
			return findClient(clientAlias);
		} else {
			Client client = findClient(request.getParameter("xClient"));
			if (client == UNKNOWN) {
				String userAgent = request.getHeader("User-Agent");
				if (fromWechat(request, userAgent)) {
					return WECHAT;
				}
				// SLB 的测试请求可能不带请求头，所以要允许 UA 为空的情况
				if (StringUtils.contains(userAgent, "Mobile")) {
					return WAP;
				}
				return Client.PC;
			}
			return client;
		}
	}

	@Nonnull
	static Client findClientByAppId(String appId) {
		if (StringUtil.notEmpty(appId)) {
			String clientAlias = StringUtils.substringBetween(appId, "/", "/");
			return findClient(clientAlias);
		}
		return UNKNOWN;
	}

	default boolean fromWechat(HttpServletRequest request, String userAgent) {
		return StringUtil.notEmpty(userAgent) && userAgent.contains("MicroMessenger");
	}

	default boolean isWAP(RequestClient t) {
		Client c = t.mapInternal();
		return c == WAP || c == WECHAT;
	}

	/** 指示是否是APP端（Android、iOS） */
	default boolean isAPP(RequestClient t) {
		Client c = t.mapInternal();
		return c == APP_IOS || c == APP_ANDROID;
	}

	/** 指示客户端是否处于浏览器网页环境中 */
	default boolean inBrowser(RequestClient t) {
		Client c = t.mapInternal();
		return c == PC || c == WAP || c == WECHAT;
	}

	/** 指示是否是移动端（WAP（含微信）、Android、iOS） */
	default boolean isMobile(RequestClient t) {
		Client c = t.mapInternal();
		return c == WECHAT_MP || c == WAP || c == WECHAT || isAPP(t);
	}

	class DefaultRequestClientResolver implements RequestClientResolver {

	}

}
