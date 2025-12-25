package com.bojiu.context.auth;

import java.lang.reflect.Method;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import com.bojiu.common.context.Context;
import com.bojiu.common.web.PackageResolveUtil;
import com.bojiu.context.model.MemberType;
import me.codeplayer.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * 默认的权限策略（用于配置权限码的生成策略等）
 */
public class DefaultPermissionPolicy implements PermissionPolicy {

	public static final String basePackage = "com.bojiu.webapp";
	public static final String deletePackageNodeName = "action";
	public static final String suffixSep = "-";
	public static final Set<String> basePackages = Set.of("com.bojiu.webapp.admin.action", "com.bojiu.webapp.merchant.action");

	/** 是否独立拆分数据导出权限，如果是，则将自动为添加了 @Export 注解的请求生成对应后缀为 "-export" 的导出专属权限码 */
	public static boolean exportStandalone = true;

	@Override
	public PermissionLocator parse(HttpServletRequest request, Class<?> clazz, Method method) {
		Permission p = method.getAnnotation(Permission.class);  // 基于方法的 @Permission 解析
		final boolean fromMethod = p != null || (p = clazz.getAnnotation(Permission.class)) == null;
		final PermissionLocator locator;
		if (p == null) {
			locator = parseFromMethod(clazz, method, request, MemberType.fromBackstage() ? "" : Permission.USER, MemberType.UNKNOWN, null);
		} else if (Permission.NONE.equals(p.value())) {
			return null;
		} else if (fromMethod) {
			locator = parseFromMethod(clazz, method, request, p);
		} else {
			locator = parseFromClass(clazz, method, p);
		}
		if (exportStandalone && p != null && p.export() && MemberType.fromBackstage()
				&& ExportInterceptor.isExport(request) && method.getAnnotation(com.bojiu.webapp.base.util.Export.class) != null) {
			locator.permissionCode = patchForExport(locator.permissionCode, method);
		}
		return locator;
	}

	protected static String mergeCode(String baseCode, int index, @NonNull String suffix) {
		return switch (suffix) {
			case Menu.DEFAULT_SUFFIX -> index > 0 ? baseCode + suffixSep + index : baseCode;
			case "" -> baseCode;
			default -> baseCode + suffixSep + suffix;
		};
	}

	public static String buildCodeForMenu(String permissionCode, int index, @NonNull Menu menu) {
		String code = menu.value();
		if (StringUtil.isEmpty(code)) {
			code = mergeCode(permissionCode, index, menu.suffix());
		}
		return normalizeCode(code);
	}

	public static String normalizeCode(String code) {
		// 由于历史原因，部分 Action 类名中带有 Admin 前缀或后缀，因此需要统一去掉 "Admin" 字样
		// return code.replace("Admin", "");
		return code; // 历史数据已全部替换，直接返回原参数即可
	}

	protected PermissionLocator parseFromMethod(Class<?> clazz, Method method, HttpServletRequest request, @NonNull String value, MemberType type, @Nullable Menu[] menus) {
		final PermissionLocator locator = new PermissionLocator(method, -1, null, type);
		if (menus != null && menus.length > 1) {
			/*
			如果有 @Menus 注解，并且有多个 @Menu 注解，则该方法对应多个菜单、多个权限码：
			1、如果指定了 Menu.value() 则以此作为最终权限码，忽略其他规则（包括后缀）
			2、没有 value() 时，权限码=默认权限码 + "-" + 后缀(或默认索引)
			3、如果索引为 0 或 后缀为 "" 时，则直接采用默认权限码，不追加后缀
			*/
			for (int i = 0; i < menus.length; i++) {
				final Menu menu = menus[i];
				if (matchMethodMenu(request, menu)) {
					setTitle(request, menu.name());
					if (StringUtil.isEmpty(value) && menu.value().isEmpty()) {
						value = resolveBaseCode(value, clazz, method);
					}
					locator.setPermissionCode(buildCodeForMenu(value, i, menu));
					locator.setMenuIndex(i);
					break;
				}
			}
			if (locator.getPermissionCode() == null) {
				// 这里采用嵌套异常，是为了不对外输出方法细节，也不必打印堆栈
				throw new PermissionException("Permission config error, please retry after refresh!", new PermissionException(method.toString()));
			}
		} else {
			locator.setPermissionCode(normalizeCode(resolveBaseCode(value, clazz, method)));
		}
		return locator;
	}

	public static boolean isPreDefined(String code) {
		return switch (code) {
			case Permission.NONE -> true;
			case Permission.USER -> checkScope(false);
			case Permission.ADMIN,
			     Permission.MERCHANT,
			     Permission.EMP,
			     Permission.SYSTEM,
			     Permission.AGENT -> checkScope(true);
			default -> false;
		};
	}

	private static boolean checkScope(final boolean acceptBackOrFrontOnly) {
		if (acceptBackOrFrontOnly == MemberType.fromBackstage()) {
			return true;
		}
		throw new UnsupportedOperationException();
	}

	public static String patchForExport(String code, Method method) {
		if (!code.endsWith(ExportInterceptor.EXPORT_PARAM)) {
			if (isPreDefined(code)) {
				code = PackageResolveUtil.getMethodCode(method.getDeclaringClass(), method, basePackage, deletePackageNodeName);
			} else {
				code = StringUtils.substringBeforeLast(code, suffixSep);
			}
			return code + suffixSep + ExportInterceptor.EXPORT_PARAM;
		}
		return code;
	}

	protected PermissionLocator parseFromMethod(Class<?> clazz, Method method, HttpServletRequest request, Permission p) {
		return parseFromMethod(clazz, method, request, p.value(), p.type(), p.menus());
	}

	static String resolveBaseCode(String menuValue, Class<?> clazz, Method method) {
		/*
		1、如果指定了 Permission.value() 则以此作为权限码
		2、没有 value() 时，默认权限码="子报名.类名.方法名"
		*/
		return menuValue.isEmpty() ? PackageResolveUtil.getMethodCode(clazz, method, basePackage, deletePackageNodeName) : menuValue;
	}

	@NonNull
	private static PermissionLocator parseFromClass(Class<?> clazz, Method method, Permission p) {
		final MemberType type = p.type();
		String code = p.value();
		return code.isEmpty()
				? new PermissionLocator(method, -1, PackageResolveUtil.getMethodCode(clazz, null, basePackage, deletePackageNodeName), type)
				: new PermissionLocator(method, -1, code, type);
	}

	protected boolean matchMethodMenu(HttpServletRequest request, Menu menu) {
		final String[] args = menu.args(); // 菜单所需校验的额外请求参数的键值对数组
		if (args.length > 0) {
			if ((args.length & 1) != 0) { // 数组长度必须为偶数
				throw new PermissionException("The number of arguments passed in args() must be even!");
			}
			for (int j = 0; j < args.length; j++) {
				String value = request.getParameter(args[j++]);
				// 如果 expect 为 * 则表示该参数必须是不为空的任意值
				// 如果 expect 不为 * 则表示该参数必须是指定的值
				if (!matchMenu(args[j], value)) {
					return false;
				}
			}
		}
		return true;
	}

	static boolean matchMenu(String expect, @Nullable String value) {
		return switch (expect) {
			case "" -> StringUtil.isBlank(value); // 必须为空
			case "*" -> StringUtil.notBlank(value); // 必须非空
			default -> expect.equals(value);
		};
	}

	/**
	 * 设置标题
	 */
	public static void setTitle(HttpServletRequest request, @Nullable String title) {
		if (title != null && !title.isEmpty()) {
			request.setAttribute(Context.internal().getTitleAttr(), title);
		}
	}

}