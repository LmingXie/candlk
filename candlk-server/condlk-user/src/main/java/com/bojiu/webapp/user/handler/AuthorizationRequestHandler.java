package com.bojiu.webapp.user.handler;

import com.bojiu.context.web.Jsons;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class AuthorizationRequestHandler implements Client.ResultHandler {

	@Override
	public void onResult(TdApi.Object obj) {
		switch (obj.getConstructor()) {
			case TdApi.Error.CONSTRUCTOR:
				log.error("【授权】接收错误：ConstructorId={}，obj={}", obj.getConstructor(), Jsons.encode(obj));
				break;
			case TdApi.Ok.CONSTRUCTOR, TdApi.Proxy.CONSTRUCTOR:
				// 结果已经通过 UpdateAuthorizationState 收到，无需做任何事情
				break;
			default:
				log.info("【授权】从TDLib接收无法解析的响应：ConstructorId={}，obj={}", obj.getConstructor(), Jsons.encode(obj));
		}
	}

	private static AuthorizationRequestHandler authorizationRequestHandler;

	public static AuthorizationRequestHandler getInstance() {
		return authorizationRequestHandler == null ? authorizationRequestHandler = new AuthorizationRequestHandler() : authorizationRequestHandler;
	}

}
