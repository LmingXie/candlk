package com.candlk.context.auth;

import java.lang.reflect.Method;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import com.candlk.common.context.Context;
import com.candlk.common.web.PackageResolveUtil;
import me.codeplayer.util.StringUtil;

/**
 * 默认的权限策略（用于配置权限码的生成策略等）
 */
public class DefaultPermissionPolicy implements PermissionPolicy {

	public static final String basePackage = "com.candlk.webapp";
	public static final String deletePackageNodeName = "action";
	public static final Set<String> basePackages = Set.of("com.candlk.webapp.admin.action", "com.candlk.webapp.merchant.action");

	@Override
	public PermissionLocator parsePermission(HttpServletRequest request, Class<?> clazz, Method method) {
		PermissionLocator locator = null;
		Permission p = method.getAnnotation(Permission.class);
		// 基于方法的 @Permission 解析
		if (p != null) {
			locator = parseMenusBasedMethod(clazz, method, request, p);
		}
		// 基于类的@Permission解析
		else if ((p = clazz.getAnnotation(Permission.class)) != null && p.value().isEmpty()) {
			String permissionCode = PackageResolveUtil.getMethodCode(clazz, (String) null, basePackage, deletePackageNodeName);
			locator = new PermissionLocator(method, 0, permissionCode, permissionCode, p.type());
		}
		// 有配置权限码，按照配置的方式解析
		if (p != null && locator == null) {
			locator = new PermissionLocator(method, 0, PackageResolveUtil.getMethodCode(clazz, method, basePackage, deletePackageNodeName), p.value(), p.type());
		}
		return locator;
	}

	protected PermissionLocator parseMenusBasedMethod(Class<?> clazz, Method method, HttpServletRequest request, Permission p) {
		Menu[] menus = p.menus();
		final PermissionLocator locator = new PermissionLocator(method, 0, null, null, p.type());
		final String code = PackageResolveUtil.getMethodCode(clazz, method, basePackage, deletePackageNodeName);
		final String permissionCode = !p.value().isEmpty() ? p.value() : code; // @Permission 如果有配置取配置，否则默认
		if (menus.length > 1) {
			// 如果有@Menus注解，并且有多个@Menu注解
			// 则该方法对应多个菜单、多个权限码：权限码=方法的默认权限码 + 数字后缀(索引)
			// 后缀为 0 时，不追加后缀
			for (int i = 0; i < menus.length; i++) {
				if (matchMethodMenu(request, menus[i])) {
					setTitle(request, menus[i]);
					locator.setMethodCode(i > 0 ? code + "-" + i : code);
					locator.setPermissionCode(i > 0 ? permissionCode + "-" + i : permissionCode);
					locator.setMenuSuffix(i);
					break;
				}
			}
			if (locator.getPermissionCode() == null) {
				throw new PermissionException('[' + method.toString() + "]权限码参数配置有误");
			}
		} else {
			locator.setPermissionCode(permissionCode);
			locator.setMethodCode(code);
		}
		return locator;
	}

	protected boolean matchMethodMenu(HttpServletRequest request, Menu menu) {
		final String[] args = menu.args(); // 菜单所需校验的额外请求参数的键值对数组
		if (args.length > 0) {
			if ((args.length & 1) != 0) { // 数组长度必须为偶数
				throw new PermissionException("The number of arguments passed in args() must be even!");
			}
			for (int j = 0; j < args.length; j++) {
				String value = request.getParameter(args[j++]);
				// 没有参数值，或不匹配返回 false
				if (StringUtil.isEmpty(value) || (!"*".equals(args[j]) && !value.equals(args[j]))) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 设置标题
	 */
	public static void setTitle(HttpServletRequest request, Menu currentMenu) {
		final String title = currentMenu.name();
		if (!title.isEmpty()) {
			request.setAttribute(Context.internal().getTitleAttr(), title);
		}
	}

}
