package com.candlk.context.auth;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.annotation.*;
import javax.servlet.http.*;

import com.candlk.common.context.Context;
import com.candlk.common.redis.RedisUtil;
import com.candlk.common.security.AES;
import com.candlk.common.web.*;
import com.candlk.context.ContextImpl;
import com.candlk.context.model.Member;
import com.candlk.context.web.ProxyRequest;
import com.candlk.context.web.RequestContextImpl;
import com.candlk.webapp.base.entity.BaseEntity;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

@Slf4j
@SuppressWarnings("rawtypes")
public class DefaultAutoLoginHandler implements AutoLoginHandler, InitializingBean {

	/** 用户记住我 Cookie key */
	public static String rememberMeKey = "rm_ads";
	/** 用户 Token 请求头 */
	public static final String authorization = "authorization";
	/** 用户 sessionId 分隔符 */
	public static final String sessionIdSep = ".";
	/** Token 有效期 */
	public static long expireOffsetMs = 15 * EasyDate.MILLIS_OF_DAY;
	/** Token 即将过期前3天内会自动续期 */
	public static long renewTokenCountDownMs = 3 * EasyDate.MILLIS_OF_DAY;

	static Duration TIMEOUT = Duration.of(10, ChronoUnit.SECONDS);

	static final ThreadLocal<Long> tokenExpireTime = new ThreadLocal<>();

	public static final String CONCURRENT_SESSION_USER_PREFIX = "concurrentSessionUser:";
	public static final String CONCURRENT_LOGIN_LOCK_PREFIX = "concurrentLoginLock:";

	final String key;
	final AES aes;

	@Resource
	StringRedisTemplate stringRedisTemplate;
	@Resource
	RedisSerializer<Object> springSessionDefaultRedisSerializer;

	/** 实际使用的自动登录支持实现，优先使用本地调用，本地没有时才使用 Dubbo 远程调用 */
	transient AutoLoginSupport autoLoginSupport;
	transient AutoLoginSupport remoteAutoLoginSupport;

	public DefaultAutoLoginHandler(String key) {
		this.key = key;
		aes = new AES(key);
	}

	@Qualifier("userAutoLoginSupportImpl")
	@Autowired(required = false)
	public void setUserAutoLoginSupport(AutoLoginSupport autoLoginSupport) {
		this.autoLoginSupport = autoLoginSupport;
	}

	@Qualifier("empAutoLoginSupportImpl")
	@Autowired(required = false)
	public void setEmpAutoLoginSupport(AutoLoginSupport autoLoginSupport) {
		this.autoLoginSupport = autoLoginSupport;
	}

	/**
	 * 实现【记住我】的自动登录的准备工作
	 */
	@Override
	public String rememberMe(HttpServletRequest request, HttpServletResponse response, Member member) {
		String clientId = CookieUtil.getCookieValue(request, Context.COOKIE_CLIENT_ID);
		if (StringUtil.isEmpty(clientId)) {
			clientId = UUID.randomUUID().toString();
			CookieUtil.setCookie(response, Context.COOKIE_CLIENT_ID, clientId, (int) TimeUnit.DAYS.toSeconds(365), null, true);
		}
		return flushMemberToken(request, response, member, clientId);
	}

	@Nonnull
	String buildToken(String encodeUserId, Member member, long expireTimestamp, String clientId) {
		return Encrypter.md5(encodeUserId + '@' + member.getPassword() + ':' + expireTimestamp + ':' + key + '#' + clientId);
	}

	/**
	 * 尝试自动登录。如果无效，则返回 null
	 */
	@Override
	public Member tryAutoLogin(HttpServletRequest request, HttpServletResponse response) {
		final Cookie cookie = CookieUtil.getCookie(request, rememberMeKey);
		final boolean hasCookie = cookie != null;
		String tokenVal = hasCookie ? ServletUtil.decodeURL(cookie.getValue()) : request.getHeader(authorization);
		if (StringUtil.notEmpty(tokenVal) && tokenVal.length() <= 256) {
			if (cookie == null) { // 如果 Cookie 为空
				StringBuilder sb = Logs.logRequest(request, null, ProxyRequest.getAppId(request) + "无法获取Cookie：", null, null, "Cookie");
				log.error(sb.toString());
			}
			final RequestContextImpl req = RequestContextImpl.get();
			int pos = tokenVal.indexOf(sessionIdSep);
			final String tokenId = pos == -1 ? Encrypter.md5(tokenVal) : tokenVal.substring(0, pos);
			final String finalTokenVal = pos == -1 ? tokenVal : tokenVal.substring(pos + 1);
			Member member = RedisUtil.loadInLockAndUnlockSilently(CONCURRENT_LOGIN_LOCK_PREFIX + tokenId, 10_000L, () -> {
				final String redisKey = CONCURRENT_SESSION_USER_PREFIX + tokenId;
				String memberJson = stringRedisTemplate.opsForValue().get(redisKey);
				Member m = null;
				if (memberJson != null) {
					try {
						m = (Member) springSessionDefaultRedisSerializer.deserialize(memberJson.getBytes(StandardCharsets.UTF_8));
					} catch (Exception e) {
						log.error("解析自动登录用户JSON数据时出错：" + ServletUtil.getRequestURI(request) + " => " + memberJson, e);
					}
				}
				if (m == null) {
					Boolean userOrEmp = null;
					final boolean fromBackstage = Context.get().fromBackstage(request);
					if (fromBackstage) {
						// 后台代理商使用的是前台用户进行登录
						userOrEmp = StringUtils.contains(request.getHeader(Context.internal().getAppIdHeaderName()), "_agent/PC/");
					}
					m = parseToken(userOrEmp, finalTokenVal, hasCookie ? TokenSource.Cookie : TokenSource.Header, CookieUtil.getCookieValue(request, Context.COOKIE_CLIENT_ID));
					if (m != null) {
						//noinspection ConstantConditions
						String jsonStr = new String(springSessionDefaultRedisSerializer.serialize(m), StandardCharsets.UTF_8);
						stringRedisTemplate.opsForValue().set(redisKey, jsonStr, TIMEOUT);
						autoLoginSupport.autoLoginCallback(m, req.getSessionContext(request).getClient(), fromBackstage);
						tryRenewToken(request, response, m);
					}
				}
				return m;
			});

			if (member != null && req.sessionUser() == null) {
				req.sessionUser(member);
			}
			return member;
		}
		if (cookie != null) {
			CookieUtil.removeCookie(response, cookie); // remove if invalid
		}
		return null;
	}

	@Override
	public Member parseToken(@Nullable Boolean userOrEmp, String tokenVal, TokenSource tokenSource, @Nullable String clientId) {
		String text = decrypt(tokenVal);
		final String[] parts = StringUtils.split(text, ':');
		if (parts != null && (parts.length == 3 || parts.length == 4)) {
			String userID = parts[0];
			Long userId;
			try {
				userId = BaseEntity.alias2Id(userID);
			} catch (Exception e) {
				log.error("解析自动登录的 userId 时出错：" + userID, e);
				userId = null;
			}

			final long expireTime = NumberUtil.getLong(parts[1], -1);
			if (tokenSource == TokenSource.WebSocket || expireTime >= System.currentTimeMillis()) {
				if (userId != null) {
					Member user = userOrEmp != null ? autoLoginSupport.load(userOrEmp, userId) : autoLoginSupport.load(userId);
					if (user != null && user.isValid()) {
						boolean match = true;
						if (StringUtil.notEmpty(clientId)) {
							String token = parts[2];
							String expectToken = buildToken(userID, user, expireTime, clientId);
							match = expectToken.equals(token);
						}
						if (match) {
							tokenExpireTime.set(expireTime);
							return user;
						}
					}
				}
			} else {
				final HttpServletRequest request = RequestContextImpl.get().getRequest();
				log.warn("用户{} 的Token已过期：{} => {}", userId, tokenVal, request == null ? "" : request.getRequestURI());
			}
		}
		return null;
	}

	@Override
	public String parseSessionId(String urlToken) {
		String text = decrypt(urlToken);
		final String[] parts = StringUtils.split(text, ':');
		return parts != null && parts.length >= 4 ? parts[3] : null;
	}

	@Override
	public int updateSessionId(String sessionId, Long userId) {
		return autoLoginSupport.updateSessionId(sessionId, userId);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		rememberMeKey = ContextImpl.get().fromBackstage(null) ? "MID_rm" : "UID_rm";
		if (autoLoginSupport == null) {
			log.warn("本地没有 AutoLoginSupport 实现，使用 Dubbo引用 替代：{}", autoLoginSupport = remoteAutoLoginSupport);
		} else {
			log.info("初始化 AutoLoginSupport 本地实现：{}", autoLoginSupport);
		}
		remoteAutoLoginSupport = null; // 清除引用
	}

	@Nullable
	public String tryRenewToken(HttpServletRequest request, HttpServletResponse response, Member member) {
		final Long expireTimeInMs = tokenExpireTime.get();
		if (expireTimeInMs != null && (expireTimeInMs - System.currentTimeMillis()) < renewTokenCountDownMs) {
			try {
				String clientId = CookieUtil.getCookieValue(request, Context.COOKIE_CLIENT_ID);
				return flushMemberToken(request, response, member, clientId);
			} catch (Exception e) {
				log.error("自动登录Token续期失败：" + member.getId(), e);
			}
		}
		return null;
	}

	protected String flushMemberToken(HttpServletRequest request, HttpServletResponse response, Member member, String clientId) {
		final long expireTime = System.currentTimeMillis() + expireOffsetMs;
		final String userID = BaseEntity.id2Alias(member.getId());
		String token = buildToken(userID, member, expireTime, clientId);
		String cookieValueSource = userID + ':' + expireTime + ':' + token;
		String cookieValue = request.getSession().getId() + sessionIdSep + encrypt(cookieValueSource);
		CookieUtil.setCookie(response, rememberMeKey, cookieValue, (int) (expireOffsetMs / 1000), null, true);
		return cookieValue;
	}

	String encrypt(String source) {
		final byte[] data = source.getBytes(StandardCharsets.UTF_8);
		final byte[] encoded;
		try {
			encoded = aes.encrypt(data);
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException(e);
		}
		return Base64.encodeBase64URLSafeString(encoded);
	}

	@Nullable
	String decrypt(String source) {
		final byte[] decoded;
		try {
			byte[] data = java.util.Base64.getUrlDecoder().decode(source);
			decoded = aes.decrypt(data);
		} catch (Throwable e) {
			log.error("解析自动登录 Token 时出错：" + source, e);
			return null;
		}
		return new String(decoded, StandardCharsets.UTF_8);
	}

}
