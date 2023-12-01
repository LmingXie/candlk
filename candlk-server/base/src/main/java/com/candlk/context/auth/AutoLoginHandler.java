package com.candlk.context.auth;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.candlk.common.web.Client;
import com.candlk.context.model.Member;

/**
 * 自动登录处理器
 */
public interface AutoLoginHandler {

	/**
	 * 实现【记住我】的自动登录的准备工作
	 */
	String rememberMe(HttpServletRequest request, HttpServletResponse response, Member member);

	/**
	 * 尝试自动登录。如果无效，则返回 null
	 */
	Member tryAutoLogin(HttpServletRequest request, HttpServletResponse response);

	default Member parseToken(String tokenVal, TokenSource tokenSource, @Nullable String clientId) {
		return parseToken(null, tokenVal, tokenSource, clientId);
	}

	Member parseToken(@Nullable Boolean userOrEmp, String tokenVal, TokenSource tokenSource, @Nullable String clientId);

	String parseSessionId(String token);

	int updateSessionId(String sessionId, Long userId);

	/**
	 * 自动登录支持
	 */
	interface AutoLoginSupport<E extends Member> {

		String GROUP_USER = "user", GROUP_EMP = "emp";

		@Nullable
		E load(Long id);

		@Nullable
		default Member load(boolean userOrEmp, Long id) {
			return load(id);
		}

		default int updateSessionId(String sessionId, Long userId) {
			return 0;
		}

		/** 自动登录的回调处理。如果返回 false，则表示禁止自动登录 */
		boolean autoLoginCallback(Member member, Client client, boolean fromBackstage);

	}

	enum TokenSource {
		Cookie,
		Header,
		WebSocket
	}

}
