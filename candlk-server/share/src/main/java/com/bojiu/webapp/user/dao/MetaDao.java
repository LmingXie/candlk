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

}