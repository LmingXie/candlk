package com.bojiu.webapp.user.action;

import java.util.Date;
import javax.annotation.Resource;

import com.alibaba.fastjson2.JSONObject;
import com.bojiu.common.context.I18N;
import com.bojiu.common.model.Messager;
import com.bojiu.common.redis.RedisUtil;
import com.bojiu.common.web.Ready;
import com.bojiu.context.auth.AutoLoginHandler;
import com.bojiu.context.auth.Permission;
import com.bojiu.context.i18n.UserI18nKey;
import com.bojiu.context.model.MessagerStatus;
import com.bojiu.context.web.*;
import com.bojiu.webapp.base.action.BaseAction;
import com.bojiu.webapp.user.entity.User;
import com.bojiu.webapp.user.form.MemberLoginForm;
import com.bojiu.webapp.user.model.UserRedisKey;
import com.bojiu.webapp.user.service.UserService;
import com.bojiu.webapp.user.utils.StringToLongUtil;
import com.google.common.base.CharMatcher;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.EasyDate;
import me.codeplayer.util.StringUtil;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserAction extends BaseAction {

	@Resource
	UserService userService;
	@Resource
	AutoLoginHandler autoLoginHandler;

	@Ready("用户登录")
	@PostMapping("/login")
	@Permission(Permission.NONE)
	public Messager<Object> login(ProxyRequest q, @Validated MemberLoginForm form) {
		// 不能存在中文
		if (!CharMatcher.ascii().matchesAllOf(form.username)) {
			return Messager.error(I18N.msg(UserI18nKey.USER_404));
		}
		final HashOperations<String, String, String> opsForHash = RedisUtil.opsForHash();
		long userId = StringToLongUtil.stringToLong(form.username);
		final String userKey = StringUtil.toString(userId);
		final String infoJson = opsForHash.get(UserRedisKey.USER_INFO, userKey);
		if (StringUtil.isEmpty(infoJson)) {
			return Messager.error(I18N.msg(UserI18nKey.USER_404));
		}
		final Long merchantId = q.getMerchantId();

		return RedisUtil.fastAttemptInLock(UserRedisKey.USER_OP_LOCK_PREFIX + merchantId + ":" + form.username.toLowerCase(), () -> {
			final User user = Jsons.parseObject(infoJson, User.class);
			user.setId(userId);
			if (!user.password.equals(form.password)) {
				return Messager.error(I18N.msg(UserI18nKey.PWD_ERROR));
			}
			if (!user.valid()) { // 冻结
				return Messager.error(I18N.msg(UserI18nKey.USER_FROZEN)).setStatus(MessagerStatus.FROZEN);
			}
			final Date now = q.now();
			user.setTtl(new EasyDate(now).addDay(1).getTime()); // 7天过期
			user.setLastLoginTime(now);

			user.setToken(autoLoginHandler.rememberMe(q.getRequest(), q.getResponse(), user));

			final RequestContextImpl req = RequestContextImpl.get();
			req.sessionUser(user);

			final Messager<User> ok = Messager.OK();
			ok.setData(user);
			userService.loginAfter(form, ok);

			opsForHash.put(UserRedisKey.USER_INFO, userKey, Jsons.encode(user));
			return Messager.exposeData(JSONObject.of(
					"username", user.getUsername(),
					"type", user.getType(),
					"token", user.getToken()
			));
		});
	}

	@Ready("用户信息")
	@GetMapping("/info")
	@Permission(Permission.USER)
	public Messager<Object> info(ProxyRequest q) {
		final Long id = q.getSessionUser().getId();
		final User user = User.getUserById(id);
		if (user == null) {
			return Messager.error(I18N.msg(UserI18nKey.USER_404));
		}
		return Messager.exposeData(JSONObject.of(
				"username", user.getUsername(),
				"type", user.getType()
		));
	}

}