package com.bojiu.webapp.user.service;

import java.util.Date;
import javax.servlet.http.HttpServletRequest;

import com.bojiu.common.context.I18N;
import com.bojiu.common.model.Messager;
import com.bojiu.context.i18n.UserI18nKey;
import com.bojiu.context.model.MessagerStatus;
import com.bojiu.context.web.RequestContextImpl;
import com.bojiu.webapp.base.service.BaseServiceImpl;
import com.bojiu.webapp.user.dao.AdminLogDao;
import com.bojiu.webapp.user.entity.AdminLog;
import com.bojiu.webapp.user.entity.User;
import com.bojiu.webapp.user.form.MemberLoginForm;
import me.codeplayer.util.StringUtil;
import org.springframework.stereotype.Service;

@Service
public class UserService extends BaseServiceImpl<AdminLog, AdminLogDao, Long> {

	public Messager<User> loginAfter(MemberLoginForm form, Messager<User> msger) {
		final User user = msger.data();
		if (!user.valid()) { // 冻结
			return msger.setStatus(MessagerStatus.FROZEN).setMsg(I18N.msg(UserI18nKey.USER_FROZEN));
		}

		// 保存 user 到 session
		final RequestContextImpl req = RequestContextImpl.get();
		final Date now = req.now();
		if (StringUtil.isEmpty(form.ip)) {
			form.ip = req.clientIP();
		}
		// 最后一次登录时间
		user.setLastLoginTime(now);

		// 最后一次登录时间
		final HttpServletRequest request = req.getRequest();
		final String newSessionId = request == null ? req.getSessionId() : request.getSession().getId();
		if (newSessionId != null) {
			user.setSessionId(newSessionId);
		}

		req.sessionUser(user);

		return msger;
	}

}
