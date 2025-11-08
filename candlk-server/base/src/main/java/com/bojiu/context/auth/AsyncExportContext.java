package com.bojiu.context.auth;

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
	/** 当前操作用户ID */
	public Long empId;
	/** 时区 */
	public TimeZone timeZone;
	/** 国际化后的导出文件名称 */
	public String fileName;

	// ============ after ============

	public Object result;

	public AsyncExportContext initConfig(Export.Config config, Long empId, TimeZone timeZone, String fileName) {
		this.config = config;
		this.empId = empId;
		this.timeZone = timeZone;
		this.fileName = fileName;
		return this;
	}

}