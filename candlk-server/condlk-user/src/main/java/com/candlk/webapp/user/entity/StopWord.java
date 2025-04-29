package com.candlk.webapp.user.entity;

import java.util.Date;

import com.candlk.webapp.base.entity.TimeBasedEntity;
import lombok.*;

/**
 * 停用词库
 *
 * @since 2025-04-27
 */
@Setter
@Getter
@NoArgsConstructor
public class StopWord extends TimeBasedEntity {

	/** 词组 */
	public String words;

	public StopWord(String words, Date now) {
		this.words = words;
		this.initTime(now);
	}

	public static final String WORDS = "words";

}
