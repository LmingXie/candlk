package com.bojiu.context.auth;

import com.bojiu.context.model.MemberType;

/**
 * 用户权限角色接口
 */
public interface MemberRole {

	/**
	 * 判断当前角色是否具备指定权限码所表示的权限
	 *
	 * @param code 指定的权限码
	 */
	boolean hasPermission(String code);

	/** 当前成员所属类型 */
	MemberType type();

}
