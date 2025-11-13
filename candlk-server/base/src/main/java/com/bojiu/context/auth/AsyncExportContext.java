package com.bojiu.context.auth;

import java.util.Locale;
import java.util.TimeZone;

import com.bojiu.webapp.base.util.Export;

/** 异步导出上下文对象 */
public class AsyncExportContext {

	// ============ init ============

	/** 触发导出的方法注解 */
	public Export export;
	public String permissionCode;
	/** 请求导出的前端页面路径，与 permissionCode 组合用于获取菜单ID */
	public String frontPath;

	// ============ before ============

	/** 导出配置 */
	public Export.Config config;
	/** 时区 */
	public TimeZone timeZone;
	/** 国际化后的导出文件名称 */
	public Locale locale;
	/** 国际化后的导出文件名称 */
	public String fileName;

	/** 当前所属商户ID */
	public Long merchantId;
	/** 当前操作用户ID */
	public Long empId;

	// ============ after ============

	public Object result;

	public AsyncExportContext initConfig(Export.Config config, Long merchantId, Long empId, TimeZone timeZone, Locale locale, String fileName) {
		this.config = config;
		this.merchantId = merchantId;
		this.empId = empId;
		this.timeZone = timeZone;
		this.locale = locale;
		this.fileName = fileName;
		return this;
	}

}