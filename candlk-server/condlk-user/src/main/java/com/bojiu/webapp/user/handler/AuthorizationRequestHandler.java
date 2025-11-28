package com.bojiu.webapp.user.handler;

import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class AuthorizationRequestHandler implements Client.ResultHandler {

	@Override
	public void onResult(TdApi.Object object) {
		switch (object.getConstructor()) {
			case TdApi.Error.CONSTRUCTOR:
				log.error("【授权】接收错误:" + object);
				break;
			case TdApi.Ok.CONSTRUCTOR:
				// 结果已经通过 UpdateAuthorizationState 收到，无需做任何事情
				break;
			default:
				log.warn("【授权】从TDLib接收错误的响应:" + object);
		}
	}

	private static AuthorizationRequestHandler authorizationRequestHandler;

	public static AuthorizationRequestHandler getInstance() {
		return authorizationRequestHandler == null ? authorizationRequestHandler = new AuthorizationRequestHandler() : authorizationRequestHandler;
	}

}
