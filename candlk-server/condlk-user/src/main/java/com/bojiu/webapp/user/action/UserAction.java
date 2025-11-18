package com.bojiu.webapp.user.action;

import com.bojiu.webapp.base.action.BaseAction;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/* 账号表 控制器 */
@RestController
@RequestMapping("/user")
public class UserAction extends BaseAction {
	// TODO: 2025/11/18 导入
	/*
	1、py提供导入session+json账号接口，传递proxyId，仅解析来自Telegram的验证码消息（仅做临时存储，间隔10分钟检查并自动断开连接）
	2、导入成功后，创建Client并通过 SetAuthenticationPhoneNumber 发送验证码
	3、java定时查询正在导入的账号是否收到验证码，收到后通过 CheckAuthenticationCode、CheckAuthenticationPassword 进行验证录入
	 */
}
