package com.bojiu.webapp.user.dao;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.bojiu.webapp.base.dao.BaseDao;
import com.bojiu.webapp.user.entity.Meta;
import org.apache.ibatis.annotations.*;

/**
 * 商户站点元数据配置表 Mapper 接口
 *
 * @author LeeYd
 * @since 2023-09-07
 */
public interface MetaDao extends BaseDao<Meta> {

	@Insert("""
			INSERT INTO gs_meta(merchant_id, type, name, value, label, ext, status, add_time, update_time)
			SELECT #{merchantId}, type, name, value, label, '', 1, NOW(3), NOW(3)
			FROM gs_meta
			WHERE type IN (${types}) AND merchant_id = 0 AND ext='init' ORDER BY id
			""")
	void initCopyFromGlobal(@Param("merchantId") Long merchantId, @Param("types") String types);

	@Insert("""
			INSERT INTO gs_meta(merchant_id, type, name, value, label, ext, status, add_time, update_time)
			SELECT #{merchantId}, type, name, value, label, '', 1, NOW(3), NOW(3)
			FROM gs_meta
			WHERE merchant_id = #{orgMerchantId} AND type ${types} ORDER BY id
			""")
	void initCopyFromMerchant(@Param("merchantId") Long merchantId, @Param("orgMerchantId") Long orgMerchantId, @Param("types") String types);

	@Insert("""
			INSERT INTO gs_meta(merchant_id, type, name, value, label, ext, status, add_time, update_time)
			SELECT #{merchantId}, type, name, value, label, '', 1, NOW(3), NOW(3)
			FROM gs_meta
			WHERE type = #{type} AND merchant_id = #{sourceMId} ${initSql} ORDER BY id
			""")
	Integer singleInitCopyGlobal(@Param("merchantId") Long merchantId, @Param("sourceMId") Long sourceMId, @Param("type") Integer type, @Param("initSql") String initSql);

	@Insert("""
			INSERT INTO gs_meta(merchant_id, type, name, value, label, ext, status, add_time, update_time)
			SELECT #{merchantId}, type, name, value, label, '', 1, NOW(3), NOW(3)
			FROM gs_meta
			WHERE merchant_id = 0 AND type = 2 AND name = 'merchant'
			""")
	void initSmsConfig(@Param("merchantId") Long merchantId);

	@Insert("""
			INSERT INTO gs_meta(merchant_id, type, name, value, label, ext, status, add_time, update_time)
			SELECT #{merchantId}, type, '1', value, label, '', 1, NOW(3), NOW(3)
			FROM gs_meta
			WHERE merchant_id = #{orgMerchantId} AND type = ${type} AND name = '0' ORDER BY id
			""")
	void initSiteCostConfig(@Param("merchantId") Long merchantId, @Param("orgMerchantId") Long orgMerchantId, @Param("type") Integer type);

	@Select("""
			SELECT DISTINCT merchant_id
			FROM gs_meta
			WHERE type = 18 AND `name` = 'cash_param' AND (`value` -> '$.riskReview') = 1 AND merchant_id ${merchantIds}
			""")
	List<Long> findRiskOpenMerchant(@Param("merchantIds") String merchantIds);

	@Select("SELECT COUNT(id)-2 AS name, merchant_id FROM gs_meta ${ew.customSqlSegment}")
	List<Meta> statVipConfig(@Param("ew") Wrapper<?> wrapper);

	@Select("SELECT DISTINCT name FROM gs_meta WHERE type = 11 AND merchant_id IN(${merchantIds}) AND name<>#{name}")
	List<Integer> findUserLevels(@Param("merchantIds") String merchantIds, @Param("name") String name);

}