package com.candlk.context.auth;

import java.lang.annotation.*;

import com.candlk.context.model.MemberType;

/**
 * 用于进行权限控制的注解，将该注解应用在对应的类型或方法上，即可在调用指定的类型或方法时，自动进行对应的权限判断<br>
 * 该注解只对作为Struts2的Action的类或方法生效
 *
 * @date 2014-10-19
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Permission {

	/** 权限码：普通用户(任意用户登录即可访问) */
	String USER = "user";
	/** 权限码：商户 + 平台(员工登录即可访问) */
	String EMP = "emp";
	/** 权限码：商户(商户用户登录即可访问) */
	String MERCHANT = "merchant";
	/** 权限码：平台(任意员工登录即可访问) */
	String ADMIN = "admin";
	/** 权限码：系统顶级权限(仅限开发人员使用，一般是ID=1的用户) */
	String SYSTEM = "system";

	/**
	 * 权限码
	 */
	String value() default "";

	/**
	 * 成员类型限定，如果是 NONE 则相当于不限定
	 */
	MemberType type() default MemberType.NONE;

	/**
	 * 需要进行多权限管理的方法菜单数组<br>
	 * 该注解属性只能在方法上定义，否则无效
	 */
	Menu[] menus() default {};

}
