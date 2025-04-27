package com.candlk.webapp.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.candlk.webapp.base.entity.BaseEntity;

import java.util.Date;

import com.candlk.webapp.base.entity.TimeBasedEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 推特词库
 *
 * @since 2025-04-27
 */
@Setter
@Getter
public class TweetWord extends TimeBasedEntity {

	/** 词组 */
	String words;
	/** 事件类型：0=热门词；1=二级词；2=普通词； */
	Integer type;

	public static final String WORDS = "words";
	public static final String TYPE = "type";

}
