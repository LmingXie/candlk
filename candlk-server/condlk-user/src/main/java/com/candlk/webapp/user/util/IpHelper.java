package com.candlk.webapp.user.util;

import java.io.InputStream;
import java.net.InetAddress;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;

import com.candlk.common.util.SpringUtil;
import com.candlk.context.model.Language;
import com.candlk.context.web.RequestContextImpl;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.AsnResponse;
import com.maxmind.geoip2.model.CountryResponse;
import com.maxmind.geoip2.record.Country;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.Assert;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
public abstract class IpHelper {

	/* geoIP解析 */
	static DatabaseReader GEO_IP2_READER;
	static DatabaseReader GEO_IP2_ASN;

	static {
		initGeoIpMap();
	}

	public static long ip2Long(String ipv4) {
		String[] parts = ipv4.split("\\.");
		if (parts.length != 4) {
			return -1;
		}
		long val = 0;
		for (int i = 0; i < parts.length; i++) {
			int part = Integer.parseInt(parts[i]);
			val += (long) (part * Math.pow(256L, 3 - i));
		}
		return val;
	}

	public static boolean fromChina(String ip) {
		return "CN".equals(getCountryCode(ip));
	}

	/**
	 * 是否需要拒绝当前IP访问（目前是登录）
	 */
	public static boolean shouldDenyIp(HttpServletRequest request) {
		final Language language = RequestContextImpl.doGetLanguage(request);
		return language == Language.zh_Hans && fromChina(RequestContextImpl.doGetClientIP(request));
	}

	public static void assertIpValid(HttpServletRequest request, boolean openMode) {
		if (openMode) {
			Assert.notTrue(shouldDenyIp(request), "不支持中国大陆登录");
		}
	}

	public static void initGeoIpMap() {
		try {
			final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			final InputStream ins = classLoader.getResourceAsStream("GeoLite2-Country.mmdb");
			final InputStream asnIns = classLoader.getResourceAsStream("GeoLite2-ASN.mmdb");
			// 读取数据库内容
			GEO_IP2_READER = new DatabaseReader.Builder(ins).build();
			GEO_IP2_ASN = new DatabaseReader.Builder(asnIns).build();
		} catch (Exception e) {
			log.error("读取geo ip2文件时出错", e);
		}
	}

	/**
	 * @return < "US", "United States">
	 */
	public static Pair<String, String> getCountry(String ip) {
		Optional<CountryResponse> resp = doGetCountry(ip);
		if (resp.isPresent()) {
			Country country = resp.get().getCountry();
			return Pair.of(country.getIsoCode(), country.getNames().get("en"));
		}
		return null;
	}

	protected static Optional<CountryResponse> doGetCountry(String ip) {
		Assert.notEmpty(ip);
		DatabaseReader reader = GEO_IP2_READER;
		try {
			// 获取查询结果
			return reader.tryCountry(InetAddress.getByName(ip)); // 要解析的ip地址
		} catch (Exception e) {
			log.error("读取IP数据文件出错", e);
		}
		return Optional.empty();
	}

	/**
	 * @return "US"
	 */
	public static String getCountryCode(String ip) {
		Optional<CountryResponse> resp = doGetCountry(ip);
		//noinspection OptionalIsPresent
		if (resp.isPresent()) {
			return resp.get().getCountry().getIsoCode();
		}
		return null;
	}

	/**
	 * @return 所属运营机构。例如："GOOGLE"
	 */
	public static String getASN(String ip) {
		Assert.notEmpty(ip);
		DatabaseReader reader = GEO_IP2_ASN;
		if (reader == null) {
			SpringUtil.asyncRun(IpHelper::initGeoIpMap, "读取geo ip2文件");
			return null;
		}
		try {
			// 获取查询结果
			Optional<AsnResponse> response = reader.tryAsn(InetAddress.getByName(ip)); // 要解析的ip地址
			if (response.isPresent()) {
				AsnResponse asnResponse = response.get();
				return asnResponse.getAutonomousSystemOrganization();
			}
		} catch (Exception e) {
			log.error("根据IP读取geo ip2文件ASN出错", e);
		}
		return null;
	}

}
