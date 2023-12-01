package com.candlk.common.context;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import lombok.Getter;
import lombok.Setter;
import me.codeplayer.util.StringUtil;

@Getter
@Setter
public class Navigator {

	/** 是否是单体应用（默认为 false，即前后端分离） */
	protected boolean singleApp = false;
	/** 后端应用根路径 */
	protected String root = "";
	/** 站点首页URL */
	protected String siteURL = "https://candlk.com";
	/** 后端应用首页URL */
	protected String rootURL = "https://candlk.com";
	/** 前后端的用户登录页面路径（后端会自动加上 adminPath 前缀） */
	protected String loginPath = "/user/user/login";
	/** 前台用户登录的页面URL */
	protected String loginURL;
	/** CDN资源根URL */
	protected String cdnURL = "https://oss.candlk.com";
	/** 管理后台路径前缀 */
	protected String adminPath = "/admin";
	/** 管理后台路径前缀 */
	protected String adminRoot = adminPath;
	/** 管理后台首页URL */
	protected String adminSiteURL;
	/** 管理后台登录URL */
	protected String adminLoginURL;
	/** 静态资源路径前缀 */
	protected String[] staticResourcePaths = { "/public", "/static" };

	/**
	 * 跟随 ServletContext 进行部分常量的初始化工作
	 */
	public void initWithServletContext(ServletContext context) {
		root = context.getContextPath();
		adminRoot = StringUtil.concat(root, adminPath);
	}

	/**
	 * 跟随系统 ServletRequest 进行的常量初始化工作
	 */
	public void initWithRequest(HttpServletRequest request) {
	}

	public String getLoginURL(boolean forUserOrAdmin) {
		if (isSingleApp()) {
			return forUserOrAdmin ? getLoginURL() : getAdminLoginURL();
		}
		return getLoginPath();

	}

	public String getSiteURL(boolean forUserOrAdmin) {
		return forUserOrAdmin ? getSiteURL() : getAdminSiteURL();
	}

}
