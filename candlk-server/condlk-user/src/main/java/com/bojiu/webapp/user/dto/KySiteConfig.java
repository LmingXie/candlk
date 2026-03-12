package com.bojiu.webapp.user.dto;

import java.util.*;

import com.bojiu.webapp.user.model.KySiteType;
import lombok.Getter;
import lombok.Setter;

/** 银行卡绑卡验证 */
@Setter
@Getter
public class KySiteConfig extends BetApiConfig {

	/** 站点名称 */
	public String name;

	/** 站点类型 */
	public KySiteType type;

	/** 站点ID（开云才有） */
	public String siteId;

	public static KySiteConfig ofT0(String name, String domain, String username, String password, String siteId) {
		final KySiteConfig config = new KySiteConfig();
		config.setName(name);
		config.setDomain(domain);
		config.setUsername(username);
		config.setPassword(password);
		config.setType(KySiteType.T0);
		config.setSiteId(siteId);
		return config;
	}

	public static KySiteConfig of(String name, String domain, String username, String password, KySiteType type) {
		final KySiteConfig config = new KySiteConfig();
		config.setName(name);
		config.setDomain(domain);
		config.setUsername(username);
		config.setPassword(password);
		config.setType(type);
		return config;
	}

}
