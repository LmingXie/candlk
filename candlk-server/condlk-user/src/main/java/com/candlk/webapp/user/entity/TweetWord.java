package com.candlk.webapp.user.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.candlk.common.model.Status;
import com.candlk.webapp.base.entity.TimeBasedEntity;
import com.candlk.webapp.user.model.TrendProvider;
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

	/**
	 * 来源厂商类型
	 *
	 * @see com.candlk.webapp.user.model.TrendProvider
	 */
	Integer providerType = TrendProvider.CUSTOM.value;
	/** 词组 */
	String words;
	/** 关键词类型：0=热门词；1=二级词；2=普通词；3=停用词 */
	Integer type;
	/** 优先级 */
	Integer priority;
	/** 业务状态：0=未启用；已启用 */
	Integer status = Status.YES.value;
	/** ES 关键词命中计数【命中后自动更新】 */
	@TableField(exist = false)
	Long count = 0L;

	/** 关键词类型：0=热门词；1=二级词；2=普通词；3=停用词 */
	public static final int TYPE_HOT = 0, TYPE_SECOND = 1, TYPE_NORMAL = 2, TYPE_STOP = 3;

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
	public static final String STATUS = "status";

}
