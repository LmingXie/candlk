package com.bojiu.webapp.base.dao;

import java.util.List;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.bojiu.webapp.base.dto.MerchantContext;
import com.bojiu.webapp.base.entity.Merchant;
import org.apache.ibatis.annotations.*;

/**
 * 商户表 Mapper 接口
 *
 * @author LeeYd
 * @since 2023-08-31
 */

public interface MerchantContextDao extends BaseDao<Merchant> {

	@Select("""
			SELECT m.id, m.name, m.currency, m.languages, m.country_codes, m.level, m.status, m.risk_status, g.status AS group_status
			, s.server_vendor, GROUP_CONCAT(d.name SEPARATOR ',') AS webDomains, m.group_id, m.commission_mode
			, g.name AS groupName, g.brand_id, m.ip_config_id
			FROM gs_merchant m
			LEFT JOIN gs_site s ON m.group_id = s.id
			LEFT JOIN gs_group g ON m.group_id = g.id
			LEFT JOIN gs_domain d ON m.id = d.merchant_id AND d.active = 1 AND d.status = 1 AND d.as_back = 0
			WHERE m.id = #{merchantId} GROUP BY d.merchant_id
			""")
	@InterceptorIgnore(tenantLine = "1")
	MerchantContext getContext(@Param("merchantId") Long merchantId);

	@Select("""
			SELECT m.id, m.name, m.currency, m.languages, m.country_codes, m.level, m.status, m.risk_status, g.status AS group_status
			, s.server_vendor, m.group_id, m.commission_mode, g.name AS groupName, g.brand_id, m.ip_config_id
			FROM gs_merchant m
			LEFT JOIN gs_group g ON m.group_id = g.id
			LEFT JOIN gs_site s ON m.id = s.id
			${ew.customSqlSegment}
			""")
	@InterceptorIgnore(tenantLine = "1")
	List<MerchantContext> findContexts(@Param("ew") Wrapper<?> wrapper);

	@Update("UPDATE gs_merchant SET risk_status=#{riskStatus} WHERE group_id=#{id}")
	@InterceptorIgnore(tenantLine = "1")
	int updateRiskStatus(@Param("riskStatus") Integer riskStatus, @Param("id") Long id);

	@Select("SELECT COUNT(*) FROM gs_user WHERE merchant_id = #{merchantId} AND state = 3")
	Long countMerchantUser(@Param("merchantId") Long merchantId);

}