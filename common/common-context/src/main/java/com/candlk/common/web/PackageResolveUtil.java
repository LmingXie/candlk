package com.candlk.common.web;

import java.lang.reflect.Method;
import javax.annotation.Nonnull;

import com.candlk.common.context.Context;
import com.candlk.common.context.InternalConfig.ActionInfo;
import me.codeplayer.util.CharConverter.CharCase;
import me.codeplayer.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

public class PackageResolveUtil {

	public static String parseHandlerMethodUriPrefix(String prefix, final Class<?> handlerClass, final @Nullable String methodName) {
		// e.g. "com.domain.webapp.action.admin.sub.UserAccountAction.hello()"
		// 去除默认的包前缀 -> "admin.sub.UserAccountAction"
		ActionInfo info = Context.internal().getActionInfo();
		String path = handlerClass.getName().substring(info.getBasePackage().length() + 1);
		// 去掉末尾的Action、把'.'替换为'/' -> "admin/sub/UserAccount"
		path = path.substring(0, path.length() - info.getActionNameSuffix().length()).replace('.', '/');
		// 斜杠后面首字母小写
		int index = path.lastIndexOf('/');
		path = StringUtil.concat(
				prefix,
				index == -1 ? null : path.substring(0, index + 1),
				Words.from(index == -1 ? path : path.substring(index + 1)).to(Words.KEBAB_CASE, CharCase.LOWER)
		);
		if (StringUtil.notEmpty(methodName)) {
			// index方法URI简写
			if (info.getDefaultMethodName().equals(methodName)) {
				// DefaultAction类的URI可继续简写
				if (info.getDefaultActionName().equals(handlerClass.getSimpleName())) {
					path = path.substring(0, path.length() - (info.getDefaultActionName().length() - info.getActionNameSuffix().length()));
				} else {
					path = path + '/';
				}
			} else {
				path = path + '/' + methodName;
			}
		}
		return path;
	}

	public static String buildUriPath(String prefix, ActionInfo info, Class<?> clazz, Method method, String basePackage, @Nullable String deletePackageNodeName) {
		// e.g. "com.domain.webapp.action.admin.sub.UserAccountAction.hello()"
		// 去除默认的包前缀 -> "admin.sub.UserAccountAction"、"admin.action.sub.UserAccountAction"
		String pkg = normalizePackageName(clazz, basePackage, deletePackageNodeName).replace('.', '/');
		String className = clazz.getSimpleName(), methodName = method.getName();
		final String sep = pkg.isEmpty() ? "" : "/";
		// index方法URI简写
		/*
		if (methodName.equals(info.getDefaultMethodName())) {
			// DefaultAction类的URI可继续简写
			if (className.equals(info.getDefaultActionName())) {
				return StringUtil.concat(prefix, pkg, sep);
			}
			methodName = "";
		}
		*/
		return prefix + pkg
				+ sep + Words.from(StringUtils.removeEnd(className, info.getActionNameSuffix())).to(Words.KEBAB_CASE, CharCase.LOWER)
				+ "/" + methodName;
	}

	public static String buildUriPath(String prefix, Class<?> clazz, Method method, String basePackage, @Nullable String deletePackageNodeName) {
		return buildUriPath(prefix, Context.internal().getActionInfo(), clazz, method, basePackage, deletePackageNodeName);
	}

	public static String buildUriPath(String prefix, Class<?> clazz, Method method) {
		ActionInfo info = Context.internal().getActionInfo();
		return buildUriPath(prefix, info, clazz, method, info.getBasePackage(), info.getDeleteNodeName());
	}

	/**
	 * 获取方法码如：com.domain.webapp.action.admin.UserAction.list => admin.UserAction.list
	 */
	public static String getMethodCode(Class<?> clazz, Method method, String basePackage, @Nullable String deletePackageNodeName) {
		return getMethodCode(clazz, method.getName(), basePackage, deletePackageNodeName);
	}

	/**
	 * 获取方法码如：com.domain.webapp.action.admin.UserAction.list => admin.UserAction.list
	 */
	public static String getMethodCode(Class<?> clazz, @Nullable String methodName, String basePackage, @Nullable String deletePackageNodeName) {
		final String pkg = normalizePackageName(clazz, basePackage, deletePackageNodeName);
		if (StringUtil.isEmpty(methodName)) {
			return pkg.isEmpty() ? clazz.getSimpleName() : pkg + '.' + clazz.getSimpleName();
		}
		return pkg.isEmpty() ? clazz.getSimpleName() + '.' + methodName
				: pkg + '.' + clazz.getSimpleName() + '.' + methodName;
	}

	/**
	 * 获取方法码如：com.domain.webapp.action.admin.UserAction.list => admin.UserAction.list
	 */
	public static String getMethodCode(Class<?> clazz, Method method) {
		ActionInfo actionInfo = Context.internal().getActionInfo();
		return getMethodCode(clazz, method, actionInfo.getBasePackage(), actionInfo.getDeleteNodeName());
	}

	@Nonnull
	public static String normalizePackageName(Class<?> clazz, String basePackage, @Nullable String deletePackageNodeName) {
		final String packageName = clazz.getPackageName();
		String pkg = packageName.length() > basePackage.length() ? packageName.substring(basePackage.length() + 1) : "";
		final int length = X.size(deletePackageNodeName);
		if (length > 0) {
			int fromIndex = 0, beginIndex, endIndex;
			while ((beginIndex = pkg.indexOf(deletePackageNodeName, fromIndex)) != -1) {
				endIndex = beginIndex + length;
				if ((beginIndex == 0 || pkg.charAt(beginIndex - 1) == '.') && (endIndex == pkg.length() || pkg.charAt(endIndex) == '.')) {
					if (beginIndex == 0 && endIndex == pkg.length()) {
						pkg = "";
						break;
					} else {
						pkg = beginIndex == 0 ? pkg.substring(endIndex + 1) : pkg.substring(0, beginIndex - 1) + pkg.substring(endIndex);
						fromIndex = beginIndex;
					}
				} else {
					fromIndex = endIndex;
				}
			}
		}
		return pkg;
	}

}
