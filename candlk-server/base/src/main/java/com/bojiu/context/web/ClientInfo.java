package com.bojiu.context.web;

import java.io.Serializable;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.bojiu.common.web.Client;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Getter;
import lombok.Setter;
import me.codeplayer.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ClientInfo implements Serializable {

	/**
	 * 应用标识/客户端类型标识/版本号/分发渠道标识/时区标识
	 * <p>
	 * "$appName/$client/$version/$channel/$timezone"
	 * => "appName/iOS/16.7.10/main/+08:00"
	 */
	@Setter
	@Getter
	private String source;
	@Nullable
	private transient String[] parts;

	private transient Client client;
	private transient TimeZone timeZone;

	public ClientInfo() {
	}

	private ClientInfo(String source) {
		this.source = source;
	}

	public String[] parts() {
		if (parts == null) {
			this.parts = StringUtils.split(source, '/');
		}
		return parts;
	}

	String safeGet(int index) {
		String[] parts = parts();
		return parts != null && parts.length > index ? parts[index] : null;
	}

	/**
	 * 获取原始的完整的 App-Id 明文请求头
	 */
	public String header() {
		return source;
	}

	/**
	 * 获取当前请求所属的 应用名称（主要用以区分用户端、代理端、管理后台）
	 */
	public String name() {
		return safeGet(0);
	}

	/**
	 * 获取请求客户端的版本号
	 */
	public String version() {
		return safeGet(2);
	}

	/**
	 * 获取请求客户端所属的应用渠道标识
	 */
	public String channel() {
		return safeGet(3);
	}

	/**
	 * 获取请求客户端所属的时区标识
	 */
	@Nullable
	public String timeZoneID() {
		return safeGet(4);
	}

	/**
	 * 获取请求来源所属的客户端类型（主要用于区分 WAP、PC、Android、iOS、WechatMP 等）
	 */
	public Client client() {
		if (client == null) {
			client = Client.findClient(safeGet(1));
		}
		return client;
	}

	@Nullable
	public TimeZone timeZone() {
		if (timeZone == null) {
			timeZone = parseTimeZone(timeZoneID());
		}
		return timeZone;
	}

	@Nullable
	public static TimeZone parseTimeZone(String timeZoneId) {
		if (StringUtil.notEmpty(timeZoneId) && "+-".indexOf(timeZoneId.charAt(0)) >= 0) {
			return TimeZone.getTimeZone("GMT" + timeZoneId);
		}
		return null;
	}

	static final long idOffset = 10000L;

	/**
	 * 根据 商户ID 和 appId 生成 appKey
	 */
	public static String buildAppKey(long merchantId, long appId) {
		return encode(merchantId) + "$" + encode(merchantId + appId);
	}

	/**
	 * 根据 商户ID 或 appId 生成 appKey 的一部分
	 */
	static String encode(long id) {
		final long mid = id + idOffset;
		int prefix = checkDigit(mid);
		return Long.toString(Long.parseLong(prefix + "" + mid), 36);
	}

	/**
	 * 校验位算法
	 *
	 * @return 1~9 【不能返回 0，否则会出错】
	 */
	public static int checkDigit(long val) {
		int h = Long.hashCode(val);
		return Math.abs((h ^ (h >>> 16))) % 9 + 1;
	}

	/**
	 * 从已编码的字符串中解析出对应的 ID
	 */
	@Nullable
	public static Long decode(final String encoded, int beginIndex, int endIndex, long offset) {
		long mid;
		try {
			final long val = Long.parseUnsignedLong(encoded, beginIndex, endIndex, 36);
			mid = val;
			long divisor = 1;
			while (mid >= 10) {
				mid /= 10;
				divisor *= 10;
			}
			mid = val % divisor;
			if (checkDigit(mid) != val / divisor) {
				return null;
			}
		} catch (NumberFormatException e) {
			return null;
		}
		mid -= offset;
		if (mid < 0) {
			return null;
		}
		return mid;
	}

	/**
	 * 从已编码的字符串中解析出对应的 ID
	 */
	@Nullable
	public static Long decode(final String encoded) {
		return decode(encoded, 0, encoded.length(), idOffset);
	}

	/**
	 * 从子域名中解析出商户ID、分包ID
	 *
	 * @return [ 商户ID, 分包ID ]
	 */
	@Nullable
	public static Long[] parse(final String appKey) {
		final String[] parts = StringUtils.split(appKey, '$');
		if (parts == null || parts.length != 2) {
			return null;
		}
		final Long merchantId = decode(parts[0]);
		if (merchantId != null) {
			final Long appId = decode(parts[1]);
			if (appId != null) {
				return new Long[] { merchantId, appId - merchantId };
			}
		}
		return null;
	}

	static final Cache<String, ClientInfo> cache = Caffeine.newBuilder()
			.initialCapacity(4)
			.maximumSize(1024)
			.expireAfterAccess(10, TimeUnit.MINUTES)
			.build();
	static final Function<String, ClientInfo> loader = ClientInfo::new;

	/*
	public static ClientInfo of(@Nullable String source, @Nullable Long mockAppId) {
		// 如果没有该请求头，或者超过 100 个字符，则直接视为空字符串
		if (source == null || source.length() > 100) {
			source = "";
		}
		if (mockAppId != null && mockAppId > 0 && !Env.inProduction()) {
			ClientInfo info = new ClientInfo(source);
			info.appId = mockAppId;
			return info;
		}
		return cache.get(source, loader);
	}
	*/

	@NonNull
	public static ClientInfo of(@Nullable String source) {
		// 如果没有该请求头，或者超过 100 个字符，则直接视为空字符串
		if (source == null || source.length() > 100) {
			source = "";
		}
		return cache.get(source, loader);
	}

}