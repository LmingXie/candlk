package com.candlk.webapp.user.form;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TweetWordQuery {

	public String words;
	/** 关键词类型：0=热门词；1=二级词；2=普通词；3=停用词； */
	public Integer type;

}
