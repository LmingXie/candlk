package com.candlk.webapp.user.form;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TweetUserQuery {

	public String username;
	/** 账号类型：0=普通账号；1=二级账号；2=特殊关注账号； */
	public Integer type;

}
