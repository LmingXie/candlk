package com.bojiu.context.auth;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bojiu.context.model.Member;

/**
 * 自动登录处理器
 */
public interface AutoLoginHandler {

	String EMP_AUTO_LOGIN_SUPPORT_BEAN_NAME = "empAutoLoginSupport";

	/**
	 * 实现【记住我】的自动登录的准备工作
	 */
	String rememberMe(HttpServletRequest request, HttpServletResponse response, Member member);

	/**
	 * 尝试自动登录。如果无效，则返回 null
	 */
	Member tryAutoLogin(HttpServletRequest request, HttpServletResponse response);

	int updateSessionId(String sessionId, Long userId);

	String flushMemberToken(HttpServletRequest request, HttpServletResponse response, Member member, String clientId, long userExpireTime);

	/**
	 * <code> token = encodeUserId + '@' + member.getPassword() + ':' + expireTimestamp + ':' + key + '#' + clientId </code>
	 */
	class TokenInfo {

		public Long memberId;
		public long expireTime;
		/** <code> [ "encodedUserId", "expireTime", "内token" ] </code> */
		public String[] parts;

		public TokenInfo(Long memberId, long expireTime, String[] parts) {
			this.memberId = memberId;
			this.expireTime = expireTime;
			this.parts = parts;
		}

		public boolean expired() {
			return expireTime < System.currentTimeMillis();
		}

	}

	/**
	 * 自动登录支持
	 */
	interface AutoLoginSupport<E extends Member> {

		String GROUP_USER = "user";
		String userPwdSep = "||";

		@Nullable
		E load(Long id);

		int updateSessionId(String sessionId, Long userId);

		/** 自动登录的回调处理。如果返回 false，则表示禁止自动登录 */
		boolean autoLoginCallback(Member member, AutoLoginForm form);

	}

}