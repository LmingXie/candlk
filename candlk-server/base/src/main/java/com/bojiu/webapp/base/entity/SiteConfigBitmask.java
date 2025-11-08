package com.bojiu.webapp.base.entity;

import com.bojiu.common.model.BizFlag;
import com.google.common.base.CaseFormat;
import lombok.Getter;

@Getter
public enum SiteConfigBitmask implements BizFlag {

	/** 快速指引关闭位 */
	QUICK_GUIDE,
	/** 站点配置->创建新站点标识 */
	SITE_NEW,
	/** 站点配置->前端模版 */
	SITE_TEMPLATE,
	/** 站点配置->前端域名配置 */
	SITE_WEB_DOMAIN,

	/** 基础功能配置->登录/注册 */
	SITE_BASE_LOGIN,
	/** 基础功能配置->Logo启动页 */
	SITE_BASE_BOOTSTRAP,
	/** 基础功能配置->首页 */
	SITE_BASE_HOME,
	/** 基础功能配置->消息 */
	SITE_BASE_MSG,
	/** 基础功能配置->我的 */
	SITE_BASE_ME,
	/** 基础功能配置->代理 */
	SITE_BASE_AGENT,
	/** 基础功能配置->支付通道 */
	SITE_BASE_PAY,
	/** 基础功能配置->底部导航 */
	SITE_BASE_BOTTOM_NAVIGATION,

	/** 运营配置->vip等级配置 */
	SITE_OPERATION_VIP,
	/** 运营配置->任务管理中心 */
	SITE_OPERATION_TASK,
	/** 运营配置->运营活动列表 */
	SITE_OPERATION_PROMOTION,

	/** 基础配置->消息->走马灯 */
	SITE_BASE_MSG_MARQUEE,
	/** 基础配置->消息->消息 */
	SITE_BASE_MSG_MSG,
	/** 基础配置->首页->Banner */
	SITE_BASE_HOME_BANNER,
	;

	private final long bizFlag;
	private final String configKey;

	SiteConfigBitmask() {
		this.bizFlag = 1L << ordinal();
		this.configKey = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name());
	}

}
