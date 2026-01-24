package com.bojiu.webapp.user.action;

import java.util.*;
import javax.annotation.Resource;

import com.alibaba.fastjson2.JSONObject;
import com.bojiu.common.context.I18N;
import com.bojiu.common.model.Messager;
import com.bojiu.common.redis.RedisUtil;
import com.bojiu.common.web.Page;
import com.bojiu.common.web.Ready;
import com.bojiu.context.auth.AutoLoginHandler;
import com.bojiu.context.auth.Permission;
import com.bojiu.context.i18n.UserI18nKey;
import com.bojiu.context.model.MessagerStatus;
import com.bojiu.context.web.*;
import com.bojiu.webapp.base.action.BaseAction;
import com.bojiu.webapp.user.entity.User;
import com.bojiu.webapp.user.form.AddUserForm;
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

import static com.bojiu.webapp.user.model.UserRedisKey.HEDGING_LIST_KEY;
import static com.bojiu.webapp.user.model.UserRedisKey.USER_INFO;

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
		final String infoJson = opsForHash.get(USER_INFO, userKey);
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

			opsForHash.put(USER_INFO, userKey, Jsons.encode(user));
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

	@Ready(value = "用户列表", merchantIdRequired = false)
	@GetMapping("/list")
	@Permission(Permission.USER)
	public Messager<Page<User>> list(ProxyRequest q) {
		User emp = q.getSessionUser();
		I18N.assertTrue(emp.asAdmin_(), UserI18nKey.PERMISSION_DENIED);
		final Page<User> page = q.getPage();
		Map<String, String> userMap = RedisUtil.opsForHash().entries(USER_INFO);
		int size = userMap.size();
		final List<User> vos = new ArrayList<>(size);
		for (String value : userMap.values()) {
			vos.add(Jsons.parseObject(value, User.class));
		}
		page.setList(vos);
		page.setTotal(size);
		return Messager.exposeData(page);
	}

	@Ready("添加用户")
	@PostMapping("/add")
	@Permission(Permission.USER)
	public Messager<Void> add(ProxyRequest q, @Validated AddUserForm form) {
		User emp = q.getSessionUser();
		I18N.assertTrue(emp.asAdmin_(), UserI18nKey.PERMISSION_DENIED);
		User input = form.copyTo(User::new);
		final String newUserId = input.getId().toString();
		final HashOperations<String, String, String> opsForHash = RedisUtil.opsForHash();
		I18N.assertTrue(!opsForHash.hasKey(USER_INFO, newUserId), "用户已存在");
		input.setBizFlag(0);
		opsForHash.put(USER_INFO, newUserId, Jsons.encodeRaw(input));
		return Messager.OK();
	}

	@Ready("删除用户")
	@PostMapping("/delete")
	@Permission(Permission.USER)
	public Messager<Void> delete(ProxyRequest q, String userId) {
		User emp = q.getSessionUser();
		I18N.assertTrue(emp.asAdmin_(), UserI18nKey.PERMISSION_DENIED);
		User opUser = Jsons.parseObject(I18N.assertNotNull(RedisUtil.opsForHash()
				.get(USER_INFO, userId), UserI18nKey.USER_404), User.class);
		RedisUtil.execInPipeline(redisOps -> {
			redisOps.opsForHash().delete(USER_INFO, userId);
			redisOps.delete(HEDGING_LIST_KEY + userId);
		});
		RequestContextImpl.removeSession(opUser.getSessionId());
		return Messager.OK();
	}

	@Ready("修改用户")
	@PostMapping("/edit")
	@Permission(Permission.USER)
	public Messager<Void> edit(ProxyRequest q, @Validated AddUserForm form) {
		final String opUserId = form.getId().toString();
		I18N.assertNotNull(opUserId);
		User emp = q.getSessionUser();
		I18N.assertTrue(emp.asAdmin_(), UserI18nKey.PERMISSION_DENIED);
		final HashOperations<String, String, String> opsForHash = RedisUtil.opsForHash();
		User opUser = Jsons.parseObject(I18N.assertNotNull(opsForHash.get(USER_INFO, opUserId), UserI18nKey.USER_404), User.class);
		opUser.setStatus(form.getStatus());
		opUser.setUsername(form.getUsername());
		opUser.setPassword(form.getPassword());
		opsForHash.put(USER_INFO, opUserId, Jsons.encodeRaw(opUser));
		RequestContextImpl.removeSession(opUser.getSessionId());
		return Messager.OK();
	}

}