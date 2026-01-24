package com.bojiu.webapp.user.form.query;

import com.bojiu.webapp.base.form.MerchantForm;
import com.bojiu.webapp.user.entity.User;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserQuery extends MerchantForm<User> {

	String username;
	String password;

	Integer status;

}
