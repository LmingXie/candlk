package com.bojiu.webapp.user.entity;

import java.util.Date;

import com.bojiu.webapp.base.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 账号表
 */
@Setter
@Getter
public class User extends BaseEntity {

	/** 用户ID */
	Long userId;
	/** 手机号 */
	String phone;
	/** 用户账号名 */
	String username;
	/** 昵称 */
	String nickname;
	/** 头像 */
	String avatar;
	/** 导入时间 */
	Date addTime;
	/** 最后更新时间 */
	Date updateTime;
	/** .json内容文件内容（包括设备信息等） */
	String jsonInfo;
	/** 最后同步对话和未读消息的时间 */
	Date syncTime;
	/** 状态：0=冻结；1=正常；2=反垃圾限制（无法主动发消息 / 邀请陌生人） */
	Integer status;
	/** 账号类型：0=session协议号；1=直登号 */
	Integer type;
	/** 状态：0=失效；1=正常； */
	Integer proxyStatus;
	/** 冻结时间（永久为0000-00-00） */
	Date freezeTime;
	/** 分组ID */
	Integer tagId;
	/** 上次拉人的时间 */
	Date inviteTime;
	/** 风控原因 */
	String issue;

	public static final String USER_ID = "user_id";
	public static final String PHONE = "phone";
	public static final String USERNAME = "username";
	public static final String NICKNAME = "nickname";
	public static final String AVATAR = "avatar";
	public static final String ADD_TIME = "add_time";
	public static final String UPDATE_TIME = "update_time";
	public static final String JSON_INFO = "json_info";
	public static final String SYNC_TIME = "sync_time";
	public static final String STATUS = "status";
	public static final String TYPE = "type";
	public static final String PROXY_STATUS = "proxy_status";
	public static final String FREEZE_TIME = "freeze_time";
	public static final String TAG_ID = "tag_id";
	public static final String INVITE_TIME = "invite_time";
	public static final String ISSUE = "issue";

}
