package com.bojiu.webapp.user.dto;

import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class BetApiConfig {

	/** 域名 */
	public String domain;
	/** 用户名 */
	public String username;
	/** 密码 */
	public String password;
	/** 代理配置（格式：proxy://username:password@host:port） */
	public String proxy;
	/** API接口地址 */
	public String endPoint;

	public BetApiConfig() {
		this.endPoint = "https://" + domain;
	}

}