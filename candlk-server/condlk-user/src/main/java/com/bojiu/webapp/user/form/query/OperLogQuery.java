package com.bojiu.webapp.user.form.query;

import com.bojiu.webapp.base.form.MerchantForm;
import com.bojiu.webapp.user.entity.AdminLog;
import lombok.Getter;
import lombok.Setter;

/** 操作日志查询参数 */
@Getter
@Setter
public class OperLogQuery extends MerchantForm<AdminLog> {

	/** 操作人 */
	public String operatorName;
	/** 模块名 */
	public String moduleName;
	/** 功能名 */
	public String func;
	/** 站点ID */
	public Long siteId;
	/** 日志类型：1=总台日志，2=商户日志 */
	public Integer type = 2;

	public boolean isAdmin() {
		return type == 1;
	}

}
