package com.candlk.context.auth;

import java.lang.reflect.Method;

import com.candlk.context.model.MemberType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PermissionLocator {

	/** 权限定位符数组在request中的KEY值 */
	public static final String PERMISSION_LOCATOR_KEY = "permissionLocator";

	protected Method method;
	public int menuSuffix;
	protected String methodCode;
	protected String permissionCode;
	protected MemberType memberType;

	public PermissionLocator() {
	}

	public PermissionLocator(Method method, int menuSuffix, String methodCode, String permissionCode, MemberType memberType) {
		this.method = method;
		this.menuSuffix = menuSuffix;
		this.methodCode = methodCode;
		this.permissionCode = permissionCode;
		this.memberType = memberType;
	}

}
