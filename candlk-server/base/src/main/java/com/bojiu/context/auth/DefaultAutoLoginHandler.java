package com.bojiu.context.auth;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.servlet.http.*;

import com.bojiu.common.dao.DataSourceSelector;
import com.bojiu.common.redis.RedisUtil;
import com.bojiu.common.security.AES;
import com.bojiu.common.security.AesGcm;
import com.bojiu.common.util.Common;
import com.bojiu.common.web.CookieUtil;
import com.bojiu.common.web.ServletUtil;
import com.bojiu.context.config.WebMvcConfig;
import com.bojiu.context.model.Member;
import com.bojiu.context.model.MemberType;
import com.bojiu.context.web.*;
import com.bojiu.webapp.base.entity.BaseEntity;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.*;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;

@Slf4j
@SuppressWarnings("rawtypes")
public class DefaultAutoLoginHandler implements AutoLoginHandler, InitializingBean {

	/** 用户 Token 请求头 */
	public static final String authorization = "authorization";
	/** 用户 sessionId 分隔符 */
	public static final String sessionIdSep = ".";
	/** Token 有效期 */
	public static long expireOffsetMs = 15 * EasyDate.MILLIS_OF_DAY;
	/** Token 即将过期前3天内会自动续期 */
	public static final long renewTokenCountDownMs = 3 * EasyDate.MILLIS_OF_DAY;

	static Duration TIMEOUT = Duration.of(10, ChronoUnit.SECONDS);

	public static final String CONCURRENT_SESSION_USER_PREFIX = "concurrentSessionUser:";
	public static final String CONCURRENT_LOGIN_LOCK_PREFIX = "concurrentLoginLock:";

	/** < 自动登录Token， 对应的成员对象 > */
	static final Cache<String, Member> memberTokenCache = Caffeine.newBuilder()
			.initialCapacity(16)
			.maximumSize(10240)
			.expireAfterAccess(6, TimeUnit.SECONDS)
			.build();

	final String key;
	private AES aes;
	private AesGcm aesGcm;

	@Resource
	RedisSerializer<Object> springSessionDefaultRedisSerializer;

	/** 实际使用的自动登录支持实现，优先使用本地调用，本地没有时才使用 Dubbo 远程调用 */
	transient AutoLoginSupport autoLoginSupport;
	transient AutoLoginSupport remoteAutoLoginSupport;

	public DefaultAutoLoginHandler(String key) {
		this.key = key;
		if (MemberType.worker()) {
			aesGcm = new AesGcm(key);
		} else {
			aes = new AES(key);
		}
	}

	@Qualifier("userAutoLoginSupportImpl")
	@Autowired(required = false)
	public void setUserAutoLoginSupport(AutoLoginSupport autoLoginSupport) {
		this.autoLoginSupport = autoLoginSupport;
	}

	@Qualifier(AutoLoginSupport.LOCAL_BEAN_NAME)
	@Autowired(required = false)
	public void setLocalAutoLoginSupport(AutoLoginSupport autoLoginSupport) {
		this.autoLoginSupport = autoLoginSupport;
	}

	// @DubboReference(group = AutoLoginSupport.GROUP_USER)
	// public void setRemoteUserAutoLoginSupport(AutoLoginSupport remoteAutoLoginSupport) {
	// 	if (!Context.get().fromBackstage(null)) {
	// 		this.remoteAutoLoginSupport = remoteAutoLoginSupport;
	// 	}
	// }

	public static String normalizeSessionId(String sessionId) {
		return StringUtils.removeEnd(sessionId, sessionIdSep);
	}

	public static String memberTypeSep() {
		return switch (MemberType.CURRENT) {
			case USER -> "u";
			case WORKER -> "w";
			default -> "m";
		};
	}

	public static String concurrentSessionUserKey(Long memberId) {
		return CONCURRENT_SESSION_USER_PREFIX + memberTypeSep() + "-" + memberId;
	}

	public static String concurrentLoginLockKey(Long memberId) {
		return CONCURRENT_LOGIN_LOCK_PREFIX + memberTypeSep() + "-" + memberId;
	}

	/**
	 * 实现【记住我】的自动登录的准备工作
	 */
	@Override
	public String rememberMe(HttpServletRequest request, HttpServletResponse response, Member member) {
		String clientId = getClientId(request);
		if (StringUtil.isEmpty(clientId)) {
			clientId = NanoIdUtils.randomNanoId(16);
			WebMvcConfig.setCookie(response, SessionCookieUtil.CLIENT_ID_COOKIE_NAME, clientId, 3 * 365 * 24 * 3600 /* 3年 */);
		}
		return flushMemberToken(request, response, member, clientId);
	}

	@Nullable
	static String getClientId(HttpServletRequest request) {
		return CookieUtil.getCookieValue(request, SessionCookieUtil.CLIENT_ID_COOKIE_NAME);
	}

	@NonNull
	public static String buildToken(String encodeUserId, String password, long expireTimestamp, String key, String clientId) {
		return Encrypter.md5(encodeUserId + '@' + StringUtil.toString(password) + ':' + expireTimestamp + ':' + key + '#' + clientId);
	}

	@NonNull
	String buildToken(String encodeUserId, Member member, long expireTimestamp, String clientId) {
		return buildToken(encodeUserId, member.getPassword(), expireTimestamp, key, clientId);
	}

	/**
	 * 尝试自动登录。如果无效，则返回 null
	 */
	@Override
	public Member tryAutoLogin(HttpServletRequest request, HttpServletResponse response) {
		final Cookie cookie = CookieUtil.getCookie(request, rememberMeKey(request));
		final boolean hasCookie = cookie != null;
		final String originToken = hasCookie ? ServletUtil.decodeURL(cookie.getValue()) : request.getHeader(authorization);
		final int size = X.size(originToken);
		Member member = null;
		if (size >= 48 && size <= 256) {
			member = memberTokenCache.get(originToken, token -> {
				final TokenInfo info = parseToken(token);
				if (info == null) {
					return null;
				}
				final Long memberId = info.memberId;
				if (info.expired()) {
					log.warn("【自动登录】用户{} 的Token已过期：{} => {}", memberId, token, request.getRequestURI());
					return null;
				}
				final String clientId = getClientId(request);
				// 没有 clientId 则不允许自动登录
				return StringUtil.isEmpty(clientId) ? null
						: RedisUtil.loadInLock(concurrentLoginLockKey(memberId), 10_000L, () -> {
					final String redisKey = concurrentSessionUserKey(memberId);
					String memberJson = RedisUtil.opsForValue().get(redisKey);
					Member m = null;
					byte[] jsonBytes = null;
					if (memberJson != null) {
						if (memberJson.isEmpty()) { // 如果为空字符串，则表示用户退出登录时清空了该值，并发请求不能再自动登录
							return null;
						}
						try {
							byte[] bytes = JavaUtil.getUtf8Bytes(memberJson);
							m = (Member) springSessionDefaultRedisSerializer.deserialize(bytes);
							jsonBytes = bytes;
						} catch (Exception e) {
							log.error("【自动登录】解析用户JSON数据时出错：" + ServletUtil.getRequestURI(request) + " => " + memberJson, e);
						}
					}
					if (m != null || (m = parseMember(info, clientId)) != null) {
						if (!autoLoginSupport.autoLoginCallback(m, AutoLoginForm.create(request, clientId))) {
							return null; // 禁止自动登录
						}
						final byte[] value = jsonBytes != null ? jsonBytes : springSessionDefaultRedisSerializer.serialize(m);
						RedisUtil.template().execute((RedisCallback<Object>) conn
								-> conn.set(JavaUtil.getUtf8Bytes(redisKey), value, Expiration.from(TIMEOUT), RedisStringCommands.SetOption.UPSERT));
						// 如果走了自动登录，需要将数据源再切换回 默认读库
						DataSourceSelector.reset();
						// 配置自动退出登录
						if (info.expireTime - System.currentTimeMillis() < renewTokenCountDownMs) {
							renewToken(request, response, m, info, clientId);
						}
					}
					return m;
				});
			});
		}

		if (member == null) {
			SessionCookieUtil.clearCookies(request, response);  // remove if invalid
		} else {
			RequestContextImpl.setSessionUser(request, member);
		}
		return member;
	}

	@Nullable
	public TokenInfo parseToken(@NonNull String originToken, @Nullable String[] sessionIdRef) {
		final int pos = originToken.indexOf(sessionIdSep, 18); // sessionId 不低于18位，减少遍历范围
		final String token = pos == -1 ? originToken : originToken.substring(pos + 1);

		String text = decrypt(token);
		final String[] parts = StringUtils.split(text, ':');
		if (parts != null && (parts.length == 3 || parts.length == 4)) {
			String userID = parts[0];
			Long userId;
			try {
				userId = BaseEntity.alias2Id(userID);
			} catch (Exception e) {
				log.error("【自动登录】解析 userId 时出错：" + userID + " => " + originToken, e);
				return null;
			}

			if (sessionIdRef != null) {
				sessionIdRef[0] = pos == -1 ? null : originToken.substring(0, pos);
			}

			final long expireTime = NumberUtil.getLong(parts[1], -1);
			return new TokenInfo(userId, expireTime, parts);
		}
		return null;
	}

	@Nullable
	public TokenInfo parseToken(@NonNull String originToken) {
		return parseToken(originToken, null);
	}

	public Long parseUserId(String token) {
		TokenInfo tokenInfo = parseToken(token);
		return tokenInfo == null ? null : tokenInfo.memberId;
	}

	protected Member load(Long memberId) {
		return autoLoginSupport.load(memberId);
	}

	public Member parseMember(TokenInfo info, @Nullable String clientId) {
		final Member user = autoLoginSupport.load(info.memberId);
		MemberType type;
		if (user != null && user.valid() && (type = user.type()).family() == MemberType.CURRENT.family() /* 前后端必须一致 */) {
			// User 的 password 实际存在另外一个关联表中，User 中的同名字段是 transient 的，无法跨服务传输，因此附加到 sessionId 中进行传输
			// 获取到后，需要自行拆分还原处理
			if (type == MemberType.USER) {
				final String[] parts = StringUtils.splitByWholeSeparatorPreserveAllTokens(user.getSessionId(), AutoLoginSupport.userPwdSep);
				if (parts != null) {
					user.setSessionId(parts[0]);
					if (parts.length == 2) {
						user.setPassword(parts[1]);
					}
				} else {
					log.warn("【自动登录】用户 sessionId 缺失：用户ID={}", info.memberId);
				}
			}
			boolean match = true;
			if (StringUtil.notEmpty(clientId)) {
				String expectToken = buildToken(info.parts[0], user, info.expireTime, clientId);
				String token = info.parts[2];
				match = expectToken.equals(token);
			}
			if (match) {
				return user;
			}
			log.warn("【自动登录】验证不通过：用户ID={}", info.memberId);
		}
		return null;
	}

	@Override
	public int updateSessionId(String sessionId, Long userId) {
		return autoLoginSupport.updateSessionId(sessionId, userId);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (autoLoginSupport == null) {
			log.warn("【自动登录】本地没有 AutoLoginSupport 实现，使用 Dubbo引用 替代：{}", autoLoginSupport = remoteAutoLoginSupport);
		} else {
			log.info("【自动登录】初始化 AutoLoginSupport 本地实现：{}", autoLoginSupport);
		}
		remoteAutoLoginSupport = null; // 清除引用
	}

	void renewToken(HttpServletRequest request, HttpServletResponse response, Member member, TokenInfo info, String clientId) {
		try {
			flushMemberToken(request, response, member, clientId);
		} catch (Exception e) {
			log.error("【自动登录】Token续期失败：" + member.getId(), e);
		}
	}

	protected String flushMemberToken(HttpServletRequest request, HttpServletResponse response, Member member, String clientId) {
		return flushMemberToken(request, response, member, clientId, expireOffsetMs);
	}

	@Override
	public String flushMemberToken(HttpServletRequest request, HttpServletResponse response, Member member, String clientId, long userExpireTime) {
		final long expireTime = System.currentTimeMillis() + userExpireTime;
		final String userID = BaseEntity.id2Alias(member.getId());
		String token = buildToken(userID, member, expireTime, clientId);
		String cookieValueSource = userID + ':' + expireTime + ':' + token;
		String cookieValue = request.getSession().getId() + sessionIdSep + encrypt(cookieValueSource);
		WebMvcConfig.setCookie(response, rememberMeKey(request), cookieValue, (int) (userExpireTime / 1000));
		return cookieValue;
	}

	/** 用户记住我 Cookie key */
	public static String rememberMeKey(MemberType memberType) {
		return switch (memberType) {
			case ADMIN -> "PID_rm";
			case MERCHANT -> "MID_rm";
			case WORKER -> "WID_rm";
			case AGENT -> "AID_rm";
			default -> "UID_rm";
		};
	}

	/** 用户记住我 Cookie key */
	public static String rememberMeKey(HttpServletRequest request) {
		return rememberMeKey(MemberType.parseFrom(request));
	}

	String encrypt(String source) {
		final byte[] data = JavaUtil.getUtf8Bytes(source);
		final byte[] encoded;
		try {
			if (MemberType.worker()) {
				encoded = aesGcm.encryptRaw(data, null);
			} else {
				encoded = aes.encrypt(data);
			}
		} catch (GeneralSecurityException e) {
			throw new IllegalArgumentException(e);
		}
		return Common.base64ToString(encoded, true);
	}

	@Nullable
	String decrypt(String source) {
		final byte[] decoded;
		try {
			byte[] data = java.util.Base64.getUrlDecoder().decode(source);
			if (MemberType.worker()) {
				decoded = aesGcm.decryptRaw(data, null);
			} else {
				decoded = aes.decrypt(data);
			}
		} catch (Throwable e) {
			log.error("【自动登录】解析 Token 时出错：" + source, e);
			return null;
		}
		return JavaUtil.newString(decoded, StandardCharsets.UTF_8);
	}

}