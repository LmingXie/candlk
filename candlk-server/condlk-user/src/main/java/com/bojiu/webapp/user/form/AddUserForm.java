package com.bojiu.webapp.user.form;

import com.bojiu.common.model.Status;
import com.bojiu.common.validator.Check;
import com.bojiu.webapp.base.form.BaseForm;
import com.bojiu.webapp.user.entity.User;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddUserForm extends BaseForm<User> {

	Long id;
	@Check(value = "账号")
	String username;
	@Check(value = "密码")
	String password;

	Integer status = Status.YES.value;

}
