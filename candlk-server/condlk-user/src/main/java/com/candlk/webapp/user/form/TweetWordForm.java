package com.candlk.webapp.user.form;

import java.util.List;

import com.candlk.common.validator.Check;
import com.candlk.common.validator.Form;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TweetWordForm implements Form {

	public List<Long> ids;

	@Check(value = "关键词")
	public List<String> words;
	/** 关键词类型：0=热门词；1=二级词；2=普通词；3=停用词； */
	@Check(value = "关键词类型")
	public Integer type;
	/** 优先级 */
	public Integer priority;

	@Override
	public void postHandle() {
		if (priority == null || priority < 0) {
			priority = 0;
		}
	}

}
