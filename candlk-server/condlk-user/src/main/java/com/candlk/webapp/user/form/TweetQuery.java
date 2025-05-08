package com.candlk.webapp.user.form;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TweetQuery {

	/** 类型：0=特殊关注推；1=热门评分推文；2=浏览猛增推文； */
	public Integer type;

	/** 搜索特定账号 */
	public String username;

	/** 业务状态（0=待创建；1=已创建） */
	public Integer status;

}
