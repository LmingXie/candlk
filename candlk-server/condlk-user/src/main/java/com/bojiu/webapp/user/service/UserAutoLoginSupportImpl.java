package com.bojiu.webapp.user.service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.bojiu.common.model.Messager;
import com.bojiu.common.redis.RedisUtil;
import com.bojiu.context.auth.AutoLoginForm;
import com.bojiu.context.auth.AutoLoginHandler.AutoLoginSupport;
import com.bojiu.context.model.Member;
import com.bojiu.context.model.RedisKey;
import com.bojiu.context.web.Jsons;
import com.bojiu.context.web.RequestContextImpl;
import com.bojiu.webapp.user.entity.User;
import com.bojiu.webapp.user.form.MemberLoginForm;
import com.bojiu.webapp.user.model.UserRedisKey;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.StringUtil;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Slf4j
// @DubboService(group = AutoLoginSupport.GROUP_USER)
@Service
public class UserAutoLoginSupportImpl implements AutoLoginSupport<User> {

	@Resource
	UserService userService;

	@Nullable
	@Override
	public User load(Long id) {
		final User user = User.getUserById(id);
		if (user != null) {
			// User.password 是 transient，无法直接序列化，需要合并到 sessionId 中，再拆分出来
			user.setSessionId(StringUtil.toString(user.getSessionId()) + userPwdSep + user.getPassword());
		}
		return user;
	}

	// @Transactional TODO
	@Override
	public boolean autoLoginCallback(Member member, AutoLoginForm input) {
		MemberLoginForm form = MemberLoginForm.forAutoLogin(member, input);
		User user = (User) member;
		final RequestContextImpl req = RequestContextImpl.get();
		final HttpServletRequest request = req.getRequest();
		final Long userId = user.getId(), merchantId = user.getMerchantId();
		String sessionId = request == null ? req.getSessionId() : request.getSession().getId();
		if (RedisUtil.opsForHash().hasKey(RedisKey.LOGIN_DEVICE_SESSION, merchantId + RedisKey.dailySep + userId + RedisKey.dailySep + sessionId)) {
			return false;
		}
		Messager<User> messager = userService.loginAfter(form, Messager.hideData(user));
		return Messager.isOK(messager);
	}

	// @Transactional TODO
	@Override
	public int updateSessionId(String sessionId, Long userId) {
		// return userService.updateSessionId(sessionId, userId);
		final User user = User.getUserById(userId);
		if (user != null) {
			user.setSessionId(sessionId);
			RedisUtil.opsForHash().put(UserRedisKey.USER_INFO, user.getId().toString(), Jsons.encode(user));
			return 1;
		}
		return 0;
	}

}