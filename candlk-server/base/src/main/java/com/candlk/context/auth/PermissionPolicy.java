package com.candlk.context.auth;

import java.lang.reflect.Method;
import javax.servlet.http.HttpServletRequest;

/**
 * 权限码生成策略接口
 *
 * @date 2015年2月2日
 */
public interface PermissionPolicy {

	/**
	 * 根据指定的参数生成对应的权限码
	 *
	 * @param method 当前请求对应的方法
	 * @return 权限定位符对象。如果返回null，则表示无需权限控制
	 */
	PermissionLocator parsePermission(HttpServletRequest request, Class<?> clazz, Method method);

	static Permission findPermission(Method method, Class<?> clazz) {
		Permission p = method.getAnnotation(Permission.class);
		if (p == null) {
			p = clazz.getAnnotation(Permission.class);
		}
		return p;
	}

}
