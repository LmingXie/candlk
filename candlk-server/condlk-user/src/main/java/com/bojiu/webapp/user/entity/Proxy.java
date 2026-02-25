package com.bojiu.webapp.user.entity;

import com.bojiu.webapp.base.entity.TimeBasedEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 代理表
 *
 * @author
 * @since 2025-11-27
 */
@Setter
@Getter
public class Proxy extends TimeBasedEntity {

	/** 代理类型：0=HTTP；1=HTTPS；2=SOCKS5；3=SOCKS4 */
	Integer type;
	/** IP */
	String ip;
	/** 端口号 */
	Integer port;
	/** 登录用户名 */
	String username;
	/** 登录密码 */
	String password;
	/** 状态：0=无效；1=有效； */
	Integer status;
	/** 引用账号数 */
	Integer counter;
	/** 分组ID */
	Integer tagId;

	public static final String TYPE = "type";
	public static final String IP = "ip";
	public static final String PORT = "port";
	public static final String USERNAME = "username";
	public static final String PASSWORD = "password";
	public static final String STATUS = "status";
	public static final String COUNTER = "counter";
	public static final String TAG_ID = "tag_id";

}
