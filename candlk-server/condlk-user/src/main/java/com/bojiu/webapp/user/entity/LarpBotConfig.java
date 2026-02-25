package com.bojiu.webapp.user.entity;

import com.bojiu.webapp.base.entity.TimeBasedEntity;
import lombok.Getter;
import lombok.Setter;

/**
* 剧本聊天机器人配置信息表
* @author
* @since 2025-11-27
*/
@Setter
@Getter
public class LarpBotConfig extends TimeBasedEntity {


	/** 群组链接 */
	String link;
	/** 剧本内容（数组JSON）：	1、需要发送图片要在末尾用{图片}标记	2、需要角色在末尾用{演员N}标记	3、需要回复的需要在末尾用{回复[行号]}标记	4、其他没有{演员N}标记的将随机获取一个账号发言（包含有角色的账号） */
	String script;
	/** 配置信息 */
	String config;
	/** 当前话术索引位 */
	Long offset;
	/** 业务状态：0=已暂停；1=运行中； */
	Integer status;
	/** 备注 */
	String remark;


	public static final String LINK = "link";
	public static final String SCRIPT = "script";
	public static final String CONFIG = "config";
	public static final String OFFSET = "offset";
	public static final String STATUS = "status";
	public static final String REMARK = "remark";
}
