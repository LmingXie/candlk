package com.bojiu.context.web;

import javax.servlet.http.HttpServletRequest;

import com.bojiu.common.context.Env;
import com.bojiu.webapp.base.entity.Merchant;
import com.google.common.net.InternetDomainName;
import me.codeplayer.util.Slice;
import me.codeplayer.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import static com.bojiu.context.ContextImpl.backstageDomain;

public abstract class DomainUtils {

	public static String doGetDomain(final HttpServletRequest request) {
		return request.getServerName();
	}

	public static String removeWww(String domain) {
		return StringUtils.removeStart(domain, "www.");
	}

	public static String removeAgent(String domain) {
		return StringUtils.removeStart(domain, "agent.");
	}

	public static String removeDealer(String domain) {
		return StringUtils.removeStart(domain, "dealer.");
	}

	/**
	 * 获取当前请求的子域名
	 *
	 * @return 例如 "abc.domain.com" => "abc"、"abc.domain.com.cn" => "abc"。如果没有子域名，将返回空字符串 ""。
	 */
	public static String doGetSubDomain(final HttpServletRequest request) {
		final String domain = doGetDomain(request);
		return extractSubDomain(domain);
	}

	/**
	 * 获取当前请求的根域名
	 *
	 * @return 例如 <code> "agent.domain.com" => "domain.com"、"domain.com" => "domain.com" </code>。
	 */
	public static String doGetRootDomain(final HttpServletRequest request) {
		return removeWww(doGetDomain(request));
	}

	/**
	 * 获取当前请求的子域名
	 *
	 * @return 例如 "abc.domain.com" => "abc"、"abc.domain.com.cn" => "abc"。如果没有子域名，将返回空字符串 ""。
	 */
	public static String extractSubDomain(final String domain) {
		final InternetDomainName domainName = InternetDomainName.from(domain);
		// 是否要对主域名进行一致性校验 ？
		return StringUtils.substring(domain, 0, -domainName.topDomainUnderRegistrySuffix().toString().length() - 1);
	}

	private static final long prodMerchantIdOffset = 10000L;
	public static final String uatPrefix = "u-", prodPrefix = "g-";

	/**
	 * 【仅限 UAT、PROD 环境使用 】
	 * 创建完整的 商户后台 域名
	 * 域名规则：
	 * 1. 字母、数字、连字符（-）组成，字母不区分大小写，连字符（-）不得出现在字符串的头部或者尾部。
	 * 2. 单个字符串长度不超过63个字符。
	 * 3. 字符串间以点分割，且总长度（包括末尾的点）不超过254个字符
	 */
	public static String buildBgDomainFor(long merchantId) {
		if (Env.inProduction()) {
			if (merchantId == Merchant.PLATFORM_ID) {
				return "sa." + backstageDomain;
			}
			final long mid = merchantId + prodMerchantIdOffset;
			int prefix = checkDigit(mid);
			return Long.toString(Long.parseLong(prefix + "" + mid), 36) + "." + backstageDomain;
		}
		// UAT
		if (merchantId == Merchant.PLATFORM_ID) {
			return "usa." + backstageDomain;
		}
		return uatPrefix + merchantId + "." + backstageDomain;
	}

	/**
	 * 校验位算法
	 *
	 * @return 1~9 【不能返回 0，否则会出错】
	 */
	static int checkDigit(long val) {
		int h = Long.hashCode(val);
		return Math.abs((h ^ (h >>> 16))) % 9 + 1;
	}

	/**
	 * 从子域名中解析出商户ID
	 */
	public static Long parseSubDomain(final String subDomain) throws DomainException {
		return switch (subDomain) {
			// "sa" 是 总台 在 生产环境 的子域名、"usa"是 总台 在 UAT环境 的子域名
			case "sa", "usa" -> Merchant.PLATFORM_ID;
			default -> doParseSubDomain(subDomain);
		};
	}

	/**
	 * 从子域名中解析出商户ID
	 */
	static Long doParseSubDomain(final String originSubDomain) throws DomainException {
		if (Env.inProduction()) {
			final long val;
			try {
				val = Long.parseLong(originSubDomain, 36);
			} catch (NumberFormatException e) {
				throw new DomainException(originSubDomain, e);
			}
			long mid = val;
			long divisor = 1;
			while (mid >= 10) {
				mid /= 10;
				divisor *= 10;
			}
			mid = val % divisor;
			if (checkDigit(mid) != val / divisor) {
				throw new DomainException(originSubDomain);
			}
			mid = mid - prodMerchantIdOffset;
			if (mid < 0) {
				throw new DomainException(originSubDomain);
			}
			return mid;
		}
		try {
			return StringUtil.removeStart(originSubDomain, uatPrefix, Slice::parseLong);
		} catch (NumberFormatException e) {
			throw new DomainException(originSubDomain, e);
		}
	}

	public static String addProtocol(String domain) {
		return "https://" + domain;
	}

	/**
	 * 获取域名的根域名（私有域名，如 www.baidu.com → baidu.com；a.b.example.co.uk → example.co.uk）
	 *
	 * @param host 任意主机名（支持带/不带子域名、特殊后缀域名）
	 */
	public static String resolveRootDomain(String host, String defaultVal) {
		InternetDomainName domain;
		try {
			domain = InternetDomainName.from(host.trim());
		} catch (IllegalArgumentException e) {
			return defaultVal;
		}
		if (domain.isUnderRegistrySuffix()) { // 必须是 ICANN 注册局的有效域名后缀
			return domain.topDomainUnderRegistrySuffix().toString();
		}
		return defaultVal;
	}

	/**
	 * 获取域名的根域名（私有域名，如 www.baidu.com → baidu.com；a.b.example.co.uk → example.co.uk）
	 *
	 * @param domain 任意域名（支持带/不带子域名、特殊后缀域名）
	 */
	public static String resolveRootDomain(String domain) {
		return resolveRootDomain(domain, domain);
	}

}