package com.bojiu.webapp.user.entity;

import com.bojiu.webapp.base.entity.TimeBasedEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 管理员角色授权表
 *
 * @author LeeYd
 * @since 2023-08-31
 */
@Setter
@Getter
public class EmpRole extends TimeBasedEntity {

	/** 商户ID */
	Long merchantId;
	/** 成员ID */
	Long empId;
	/** 角色权限ID */
	Long roleId;
	/** 类型：1=总台；2=商户 */
	Integer type;

	public EmpRole init(Long merchantId, Long empId, Long roleId, Integer type) {
		this.merchantId = merchantId;
		this.empId = empId;
		this.roleId = roleId;
		this.type = type;
		return this;
	}

	public static Integer typeFor(boolean platformOrMerchant) {
		return platformOrMerchant ? 1 : 2;
	}

	public static final String MERCHANT_ID = "merchant_id";
	public static final String EMP_ID = "emp_id";
	public static final String ROLE_ID = "role_id";
	public static final String TYPE = "type";

}
