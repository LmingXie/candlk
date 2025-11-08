package com.bojiu.context.auth;

import java.lang.annotation.*;

import com.bojiu.context.model.MemberType;

/**
 * 用于进行权限控制的注解，将该注解应用在对应的类型或方法上，即可在调用指定的类型或方法时，自动进行对应的权限判断<br>
 *
 * @since 2014-10-19
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Permission {

	/** 平台默认超管账号 ID */
	Long PLATFORM_SA_ID = 1L;
	/** 权限码：无需任何权限即可公开访问 */
	String NONE = "none";
	/** 权限码：普通用户(任意用户登录即可访问) */
	String USER = "user";
	/** 权限码：商户 + 平台(员工登录即可访问) */
	String EMP = "emp";
	/** 权限码：经销商(经销商用户登录即可访问) */
	String DEALER = "dealer";
	/** 权限码：代理(代理用户登录即可访问) */
	String AGENT = "agent";
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
	 * 设置限定访问当前请求方法的成员类型，
	 * <p> 如果是默认的 {@link MemberType#UNKNOWN } 则表示不限定。
	 * <p> 如果指定了其他类型，这表示在验证 {@link #value()} 权限码的基础上还会额外检查用户所属的成员类型
	 * <ul>
	 * <li> 如果为 {@link MemberType#EMP}， 则表示【只要是后台人员登录】都可访问</li>
	 * <li> 如果为 {@link MemberType#MERCHANT } 表示该方法只允许【商户】人员访问。
	 * <li> 如果为 {@link MemberType#AGENT}、{@link MemberType#DEALER}， 则表示自身类型 或<b>站点/商户员工登录</b>均可访问</li>
	 * <li> 如果为 {@link MemberType#ADMIN } 表示该方法只允许【总台】人员访问。
	 * </ul>
	 */
	MemberType type() default MemberType.UNKNOWN;

	/**
	 * 需要进行多权限管理的方法菜单数组<br>
	 * 该注解属性只能在方法上定义，否则无效
	 */
	Menu[] menus() default {};

	/** 是否为导出单独拆分权限 */
	boolean export() default false;

}