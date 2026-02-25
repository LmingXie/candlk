package com.bojiu.webapp.user.dto;

import java.util.List;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.*;
import org.drinkless.tdlib.TdApi;

@Setter
@Getter
public class JsonInfo {

	@JSONField(name = "sdk")
	public String sdk;
	@JSONField(name = "sex")
	public Object sex;
	@JSONField(name = "ipv6")
	public Boolean ipv6;
	@JSONField(name = "scam")
	public Boolean scam;
	@JSONField(name = "block")
	public Boolean block;
	@JSONField(name = "email")
	public String email;
	@JSONField(name = "phone")
	public String phone;
	/**
	 * 代理配置：[$proxyType,$server,$port,$enable,$username,$password]
	 * <p>
	 * <p>PROXY_TYPE_SOCKS4 = SOCKS4 = 1
	 * <p>PROXY_TYPE_SOCKS5 = SOCKS5 = 2
	 * <p>PROXY_TYPE_HTTP = HTTP = 3
	 */
	@JSONField(name = "proxy")
	public List<String> proxy;
	@JSONField(name = "twoFA")
	public String twoFA;
	@JSONField(name = "app_id")
	public Integer appId;
	@JSONField(name = "avatar")
	public String avatar;
	@JSONField(name = "device")
	public String device;
	@JSONField(name = "secret")
	public String secret;
	@JSONField(name = "user_id")
	public Long userId;
	@JSONField(name = "app_hash")
	public String appHash;
	@JSONField(name = "category")
	public String category;
	@JSONField(name = "email_id")
	public String emailId;
	@JSONField(name = "password")
	public String password;
	@JSONField(name = "perf_cat")
	public Integer perfCat;
	@JSONField(name = "username")
	public String username;
	@JSONField(name = "installer")
	public String installer;
	@JSONField(name = "lang_code")
	public String langCode;
	@JSONField(name = "lang_pack")
	public String langPack;
	@JSONField(name = "last_name")
	public String lastName;
	@JSONField(name = "tz_offset")
	public Integer tzOffset;
	@JSONField(name = "first_name")
	public String firstName;
	@JSONField(name = "is_blocked")
	public Boolean isBlocked;
	@JSONField(name = "package_id")
	public String packageId;
	@JSONField(name = "app_version")
	public String appVersion;
	@JSONField(name = "device_model")
	public String deviceModel;
	@JSONField(name = "device_token")
	public String deviceToken;
	@JSONField(name = "session_file")
	public String sessionFile;
	@JSONField(name = "register_time")
	public Integer registerTime;
	@JSONField(name = "last_check_time")
	public Integer lastCheckTime;
	@JSONField(name = "system_lang_code")
	public String systemLangCode;
	@JSONField(name = "system_lang_pack")
	public String systemLangPack;

	transient TdApi.ProxyType parsedProxyType;

	public TdApi.ProxyType proxyType() {
		if (parsedProxyType == null) {
			parsedProxyType = "2".equals(proxy.get(0)) ? new TdApi.ProxyTypeSocks5(proxy.get(4), proxy.get(5)) : new TdApi.ProxyTypeHttp(proxy.get(4), proxy.get(5), false);
		}
		return parsedProxyType;
	}

}
