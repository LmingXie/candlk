package com.bojiu.webapp.user.dao;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.bojiu.common.web.Page;
import com.bojiu.webapp.base.dao.BaseDao;
import com.bojiu.webapp.user.entity.AdminLog;
import com.bojiu.webapp.user.vo.AdminLogVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface AdminLogDao extends BaseDao<AdminLog> {

	@Select("""
			SELECT t.merchant_id AS siteId,t.type, e.username AS operatorName, t.module_name AS moduleName,t.action, t.detail AS content, t.add_ip AS ip,
			t.add_time AS operatorTime,t.func AS func
			FROM gs_admin_log t
			LEFT JOIN gs_emp e ON t.emp_id = e.id
			${ew.customSqlSegment}
			""")
	@InterceptorIgnore(tenantLine = "1")
	Page<AdminLogVO> findPlatformPageByQuery(Page<AdminLogVO> page, @Param("ew") Wrapper<?> wrapper);

}
