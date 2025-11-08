package com.bojiu.context.web;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.bojiu.common.context.RequestContextFilter;
import com.bojiu.common.model.ErrorMessageException;
import com.bojiu.common.model.Messager;
import com.bojiu.common.security.AES;
import com.bojiu.common.web.CookieUtil;
import com.bojiu.context.model.MessagerStatus;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * 增强的、支持 App-Id 解密的 HttpServletRequestWrapper 实现
 */
@Slf4j
public class EnhanceHttpServletRequestWrapper extends HttpServletRequestWrapper {

	/** 用于获取原始的 App-Id 请求头 */
	public static final String ORIGIN_APP_ID = "APP-ID";
	/** 用于获取解密后的 App-Id 请求头 */
	public static final String APP_ID = "app-id";

	protected String appId;

	static AES aes;
	/** 是否拒绝不可信的请求时间 */
	public static boolean denyUntrustedRequestTime;
	// ** 第三方回调请求路径的后缀 */
	// public static String[] thirdPartyUriSuffixes = { "Callback", "Notify", "Result" };

	/**
	 * Constructs a request object wrapping the given request.
	 *
	 * @param request The request to wrap
	 * @throws IllegalArgumentException if the request is null
	 */
	public EnhanceHttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
	}

	@Override
	public String getHeader(String name) {
		return switch (name) {
			case ORIGIN_APP_ID -> super.getHeader(APP_ID);
			case APP_ID, "App-Id" -> { // 暂时不兼容其他畸形的大小写形式
				if (this.appId == null) {
					this.appId = resolve(super.getHeader(APP_ID), this);
				}
				yield this.appId;
			}
			default -> super.getHeader(name);
		};
	}

	public static String resolve(@Nullable String appIdHeader, @Nullable HttpServletRequest request) {
		if (StringUtil.isEmpty(appIdHeader)) {
			return appIdHeader;
			/*
			if (StringUtils.endsWithAny(super.getRequestURI(), thirdPartyUriSuffixes)) {
				yield header;
			}
			throw new ErrorMessageException(Messager.status(MessagerStatus.UNTRUSTED));
			*/
		}
		// 没有启用，则不解密；没有加密时也不用解密
		// header 可能形如 "xxRNBeFYnpP+ioBKjGjdIVWLKyYuZ/B1yZ18nRSvCO2ZjbP/KIXYpasNA/lYyg5u"，也有 3个 "/"，因此要提高判断精细度
		if (aes == null || appIdHeader.length() < 48 && StringUtils.countMatches(appIdHeader, '/') >= 3) {
			return appIdHeader;
		}
		// "秒级时间戳/密文"
		try {
			final String decoded = aes.decrypt(appIdHeader);
			final String[] parts = decoded.split("/", 2);
			final long timestampInSecs = Long.parseLong(parts[0]);
			String appId = parts[1];
			final long nowInSecs = System.currentTimeMillis() / 1000L;
			final long diff = Math.abs(nowInSecs - timestampInSecs);
			if (diff > 5 * 60) { // 前后差异不能超过 5 分钟
				log.warn("客户端时间异常：AppId={}，cid={}，diff={} s", timestampInSecs + "/" + appId, CookieUtil.getCookieValue(request, SessionCookieUtil.CLIENT_ID_COOKIE_NAME), (nowInSecs - timestampInSecs));
				if (denyUntrustedRequestTime) {
					throw new ErrorMessageException("Please check whether the system time is set correctly!", MessagerStatus.UNTRUSTED, false).report();
				}
			}
			return appId;
		} catch (Exception e) {
			log.error("解密AppId时出错：" + appIdHeader, e);
			throw new ErrorMessageException(Messager.status(MessagerStatus.UNTRUSTED), e).report();
		}
	}

	public static void enableAppIdEncrypt(String secret) {
		aes = new AES(secret);
		RequestContextFilter.setRequestWrapper(EnhanceHttpServletRequestWrapper::new);
	}

}