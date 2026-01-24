package com.bojiu.webapp.user.vo;

import com.bojiu.common.context.I18N;
import com.bojiu.common.model.Status;
import com.bojiu.context.i18n.UserI18nKey;
import com.bojiu.webapp.base.vo.AbstractVO;
import com.bojiu.webapp.user.entity.User;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserVO extends AbstractVO<User> {

	public Long id;
	public String username;
	public String password;
	public Integer status;

	public String getStatus_() {
		return I18N.msg(Status.YES.eq(status) ? UserI18nKey.SITE_STATUS_OK : UserI18nKey.USER_FROZEN);
	}

}
