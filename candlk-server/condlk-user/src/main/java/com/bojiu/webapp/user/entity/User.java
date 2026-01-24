package com.bojiu.webapp.user.entity;

import java.util.*;

import com.baomidou.mybatisplus.annotation.TableField;
import com.bojiu.common.model.Status;
import com.bojiu.common.redis.RedisUtil;
import com.bojiu.context.auth.Permission;
import com.bojiu.context.model.Gender;
import com.bojiu.context.model.MemberType;
import com.bojiu.context.web.Jsons;
import com.bojiu.webapp.base.entity.BaseMember;
import com.bojiu.webapp.user.model.UserRedisKey;
import com.bojiu.webapp.user.utils.StringToLongUtil;
import lombok.Getter;
import lombok.Setter;
import me.codeplayer.util.Cmp;
import me.codeplayer.util.StringUtil;

@Setter
@Getter
public class User extends BaseMember {

	public static List<String> accounts;
	public static List<Long> accountIds;

	public static void initLoad() {
		final Map<String, String> userMap = RedisUtil.opsForHash().entries(UserRedisKey.USER_INFO);
		if (!userMap.isEmpty()) {
			accounts = new ArrayList<>(userMap.size());
			accountIds = new ArrayList<>(userMap.size());
			for (String json : userMap.values()) {
				final User user = Jsons.parseObject(json, User.class);
				accounts.add(user.getUsername());
				accountIds.add(user.getId());
			}
		}
	}

	public Long id;
	public String token;
	public String username;
	public String password;
	public Long ttl;
	/** 账号类型：1=1类；2=2类 */
	public Integer type;
	public Integer status;
	/** 最后一次登录时间 */
	protected Date lastLoginTime;
	@TableField(exist = false)
	protected String sessionId;

	@Override
	public boolean valid() {
		return Status.YES.eq(status);
	}

	@Override
	public Gender getGender() {
		return Gender.MALE;
	}

	public Long getId() {
		return this.id = StringToLongUtil.stringToLong(username);
	}

	@Override
	public Long getTopUserId() {
		return 0L;
	}

	@Override
	public Long getDealerId() {
		return 0L;
	}

	@Override
	public boolean hasPermission(String code) {
		// 权限码为"user"的方法允许所有登录用户访问
		return Permission.USER.equals(code);
	}

	@Override
	public MemberType type() {
		return MemberType.USER;
	}

	@Override
	public Long getMerchantId() {
		return 0L;
	}

	public static User getUserByUsername(String username) {
		return getUserById(StringToLongUtil.stringToLong(username));
	}

	public static User getUserById(Long id) {
		final String infoJson = RedisUtil.opsForHash().get(UserRedisKey.USER_INFO, StringUtil.toString(id));
		return infoJson == null ? null : Jsons.parseObject(infoJson, User.class);
	}

	/** 是否可查看全部对冲对子 */
	public boolean asAdmin_() {
		return Cmp.eq(type, 1) || "admin_".equals(username);
	}

}
