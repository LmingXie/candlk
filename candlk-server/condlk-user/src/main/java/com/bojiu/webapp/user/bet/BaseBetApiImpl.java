package com.bojiu.webapp.user.bet;

import java.net.http.HttpClient;
import javax.annotation.Nullable;
import javax.annotation.Resource;

import com.bojiu.common.util.BaseHttpUtil;
import com.bojiu.webapp.base.entity.Merchant;
import com.bojiu.webapp.user.dto.BetApiConfig;
import com.bojiu.webapp.user.model.BetProvider;
import com.bojiu.webapp.user.model.MetaType;
import com.bojiu.webapp.user.service.MetaService;

public abstract class BaseBetApiImpl extends BaseHttpUtil implements BetApi {

	@Resource
	MetaService metaService;

	protected HttpClient currentClient() {
		return defaultClient();
	}

	@Nullable
	protected static volatile HttpClient proxyClient;

	/** 初始化代理客户端 */
	protected void initProxyClient() {
		if (proxyClient == null) {
			synchronized (BetApi.class) {
				if (proxyClient == null) {
					BetApiConfig config = metaService.getCachedParsedValue(Merchant.PLATFORM_ID, MetaType.bet_config, BetProvider.HG.name(), BetApiConfig.class);
					proxyClient = BaseHttpUtil.getProxyOrDefaultClient(config.proxy);
				}
			}
		}
	}

}
