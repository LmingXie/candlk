package com.candlk.webapp.user.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.candlk.webapp.base.entity.TimeBasedEntity;
import lombok.*;

/**
 * 推特词库
 *
 * @since 2025-04-27
 */
@Setter
@Getter
@NoArgsConstructor
public class TweetWord extends TimeBasedEntity {

	/** 词组 */
	String words;
	/** 事件类型：0=热门词；1=二级词；2=普通词； */
	Integer type;
	/** 优先级 */
	Integer priority;
	/** ES 引用计数 */
	@TableField(exist = false)
	Long count;

	public static final int TYPE_HOT = 0, TYPE_SECOND = 1, TYPE_NORMAL = 2;

	public TweetWord(String words, Integer type, Date now) {
		this.words = words;
		this.type = type;
		this.initTime(now);
	}

	public TweetWord(String words, Integer type, Integer priority, Long count, Date now) {
		this.words = words;
		this.type = type;
		this.priority = priority;
		this.count = count;
		this.initTime(now);
	}

	public static final String WORDS = "words";
	public static final String COUNT = "count";
	public static final String PRIORITY = "priority";
	public static final String TYPE = "type";

}
