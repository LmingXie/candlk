package com.candlk.webapp.user.form;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TweetQuery {

	/** 类型：0=特殊关注推；1=热门评分推文；2=浏览猛增推文； */
	public Integer type;

}
