package com.candlk.common.context;

import java.math.RoundingMode;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;
import me.codeplayer.util.StringUtil;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * 存储应用中可能存在的动态变量
 */
@Getter
@Setter
public class InternalConfig {

	/** 首页入口方法。默认为 "com.candlk.www.action.DefaultAction#index" */
	protected String homepageEntryMethod = "com.candlk.webapp.base.action.DefaultAction#index";
	/** 控制器相关信息（默认根据 homepageEntryMethod 自动生成） */
	protected ActionInfo actionInfo;
	/** 站点名称 */
	protected String siteName = "SiteName";
	protected int scale = 2;
	protected RoundingMode roundingMode = RoundingMode.HALF_UP;
	/** 平台超级管理员 id */
	protected long systemSuperAdminId = 1L;
	/** 页面标题的属性名 */
	protected String titleAttr = "__title";
	/** 分页对象的属性名 */
	protected String pageAttr = "page";
	/** 当前登录用户的 session 属性名 */
	protected String userSessionAttr = RequestContext.SESSION_USER;
	/** 用户类 */
	@SuppressWarnings("rawtypes")
	protected Class userClass;
	/** 当前图片验证码的 session 属性名 */
	protected String imageCaptchaSessionAttr = "imageCaptcha";
	/** 当前手机验证码的 session 属性名 */
	protected String phoneCaptchaSessionAttr = "phoneCaptcha";
	/** 邀请码的参数名 */
	protected String inviteCodeParamName = "inviteCode";
	/** 邀请码的 session 属性名 */
	protected String inviteCodeSessionAttr = "inviteCode";
	/** 邮箱 */
	protected String email = "codeplayer@foxmail.com";
	/** 客户端请求需要携带的交互请求头名称 */
	protected String appIdHeaderName = "App-Id";
	/** 管理后台请求的App-Id中所包含的关键字（以区分后台请求） */
	protected String adminAppIdKeyword = "admin";
	/** APP ID 值集合 */
	protected Set<String> appIdSet = Set.of("webapp_admin", "webapp_front");
	/** 存储当前客户端的 session 属性名（ 默认情况下，如该值为空，则表示不需要缓存到 session ） */
	protected String userClientSessionAttr;

	public void init() {
		if (StringUtil.notEmpty(homepageEntryMethod) && actionInfo == null) {
			actionInfo = new ActionInfo().init(homepageEntryMethod);
		} else {
			actionInfo.initActionNameSuffix();
		}
	}

	public void setUserSessionAttr(String userSessionAttr) {
		this.userSessionAttr = userSessionAttr;
		if (StringUtil.notEmpty(userSessionAttr)) {
			RequestContext.SESSION_USER = userSessionAttr;
		}
	}

	@Getter
	@Setter
	public static class ActionInfo {

		/** 放置 所有控制器的 base package */
		protected String basePackage;
		/** 放置 所有后台控制器的 base package */
		protected String adminBasePackage;
		protected String defaultActionName;
		protected String defaultMethodName;
		protected String actionNameSuffix = "Action";
		protected String deleteNodeName = "action";

		public ActionInfo init(String homepageEntryMethod) {
			String[] parts = StringUtils.split(homepageEntryMethod, "#", 2);
			this.defaultMethodName = parts[1];
			final String defaultAction = parts[0];
			Class<?> clazz;
			try {
				clazz = ClassUtils.getClass(defaultAction, false);
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("homepageEntryMethod 指定的默认控制器类不存在：" + defaultAction, e);
			}
			this.basePackage = clazz.getPackageName();
			if (StringUtil.isEmpty(adminBasePackage)) {
				this.adminBasePackage = basePackage.replace("base", "admin");
			}
			this.defaultActionName = clazz.getSimpleName();
			initActionNameSuffix();
			return this;
		}

		public void initActionNameSuffix() {
			if (StringUtil.isEmpty(actionNameSuffix) && StringUtil.notEmpty(defaultActionName)) {
				String[] supportsSuffixes = { "Action", "Controller" };
				for (String suffix : supportsSuffixes) {
					if (defaultActionName.endsWith(suffix)) {
						actionNameSuffix = suffix;
					}
				}
			}
		}

	}

}
