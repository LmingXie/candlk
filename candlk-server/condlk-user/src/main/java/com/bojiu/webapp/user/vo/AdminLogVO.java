package com.bojiu.webapp.user.vo;

import java.util.Date;

import com.bojiu.webapp.user.entity.AdminLog;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;

/**
 * 平台操作日志
 *
 * @author wsl
 * @since 2023-09-07
 */
@Setter
@Getter
public class AdminLogVO {

	/** 商户ID */
	Long merchantId;
	/** 站点ID */
	Long siteId;
	/** 日志ID */
	Long id;
	/** 模块名称 */
	String moduleName;
	/** 功能 */
	String func;
	/** 操作行为 */
	String action;
	/** 操作内容 */
	String content;
	/** 操作人 */
	String operatorName;
	/** IP */
	String ip;
	/** 操作时间 */
	Date operatorTime;
	transient String[] details;

	private String[] details() {
		if (details == null) {
			details = AdminLog.getDetail_(content);
		}
		return details;
	}

	public String getOlds() {
		return ArrayUtils.get(details(), AdminLog.FieldDiff.OLD_INDEX);
	}

	public String getCurrents() {
		return ArrayUtils.get(details(), AdminLog.FieldDiff.CURRENT_INDEX);
	}

}
