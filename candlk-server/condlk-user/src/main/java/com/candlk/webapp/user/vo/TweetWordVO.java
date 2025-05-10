package com.candlk.webapp.user.vo;

import java.util.Date;

import com.candlk.webapp.base.vo.AbstractVO;
import com.candlk.webapp.user.entity.TweetWord;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TweetWordVO extends AbstractVO<TweetWord> {

	/** 词组 */
	String words;
	/** 关键词类型：0=热门词；1=二级词；2=普通词；3=停用词 */
	Integer type;
	/** 优先级 */
	Integer priority;
	/** ES 关键词命中计数【命中后自动更新】 */
	Long count = 0L;
	/** 业务状态：0=未启用；已启用 */
	Integer status;

	Date addTime;
	/** 最后更新时间 */
	Date updateTime;

	public Integer getType() {
		return type == null ? TweetWord.TYPE_STOP : type;
	}

	public Integer getPriority() {
		return priority == null ? 0 : priority;
	}

}