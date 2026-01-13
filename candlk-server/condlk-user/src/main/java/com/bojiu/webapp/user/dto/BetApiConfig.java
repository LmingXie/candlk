package com.bojiu.webapp.user.dto;

import com.bojiu.common.context.I18N;
import com.bojiu.webapp.base.entity.MetaValue;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class BetApiConfig implements MetaValue<BaseRateConifg> {

	/** 域名 */
	public String domain;
	/** 用户名 */
	public String username;
	/** 密码 */
	public String password;
	/** 代理配置（格式：proxy://username:password@host:port） */
	public String proxy;
	/** API接口地址（根据域名自动生成） */
	public String endPoint;
	/** 登录Token */
	public String token;

	/** 获取赛果的URL */
	public String scoreResultUrl;

	@Override
	public void validate() {
		I18N.assertNotNull(domain, "域名不能为空");
	}

}