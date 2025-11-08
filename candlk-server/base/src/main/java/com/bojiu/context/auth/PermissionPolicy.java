package com.bojiu.context.auth;

import java.lang.reflect.Method;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

/**
 * 权限码生成策略接口
 *
 * @since 2015-2-2
 */
public interface PermissionPolicy {

	/**
	 * 根据指定的参数生成对应的权限码
	 *
	 * @param method 当前请求对应的方法
	 * @return 权限定位符对象。如果返回null，则表示无需权限控制
	 */
	@Nullable
	PermissionLocator parse(HttpServletRequest request, Class<?> clazz, Method method);

}