package com.bojiu.webapp.user.form;

import com.bojiu.common.validator.*;
import com.bojiu.context.auth.AutoLoginForm;
import com.bojiu.context.model.Member;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import static com.bojiu.context.i18n.UserModelI18nKey.*;

@Getter
@Setter
@Accessors(chain = true)
public class MemberLoginForm extends AutoLoginForm implements Form {

	/** 用户名/手机号/邮箱 */
	@Check(value = MEMBER_LOGIN_FORM_USERNAME)
	@Trim
	@NotEmpty
	public String username;

	/** 密码（与 短信验证码 二选一） */
	@Check(value = MEMBER_LOGIN_FORM_PASSWORD)
	@NotEmpty
	public String password;

	public static MemberLoginForm forAutoLogin(Member member, AutoLoginForm input) {
		final MemberLoginForm form = new MemberLoginForm();
		form.setUsername(member.getUsername());
		form.client = input.client;
		form.fromBackstage = input.fromBackstage;
		form.ip = input.ip;
		form.deviceId = input.deviceId;
		return form;
	}

}