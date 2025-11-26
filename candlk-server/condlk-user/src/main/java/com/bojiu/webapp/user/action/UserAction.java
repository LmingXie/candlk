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
	1、py提供导入账号接口，
	2、导入成功后，创建Client并通过 SetAuthenticationPhoneNumber 发送验证码，记录发送时间
	3、java定时查询正在导入的账号是否收到验证码，收到后通过 CheckAuthenticationCode、CheckAuthenticationPassword 进行验证录入
	 */
	// TODO: 2025/11/26 数据库迁移方案（考虑不进行迁移，java包存放到原有文件夹内，页面逻辑依旧只做静态路由）
}
