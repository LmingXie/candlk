package com.candlk.webapp.user.model;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.candlk.common.model.ValueProxy;
import com.candlk.common.model.ValueProxyImpl;
import com.candlk.webapp.base.entity.BaseEntity;
import com.candlk.webapp.user.entity.StopWord;
import com.candlk.webapp.user.entity.TweetWord;
import lombok.Getter;
import me.codeplayer.util.ArrayUtil;

/** ES索引类型 */
@Getter
public enum ESIndexType implements ValueProxyImpl<ESIndexType, String> {
	/** 关键词索引 */
	KEYWORDS_INDEX("关键词", TweetWord.class),
	/** 停用词索引 */
	STOP_WORDS_INDEX("停用词", StopWord.class),
	;

	@EnumValue
	public final String value;
	public final Class<? extends BaseEntity> entity;
	public final String label;
	public final boolean open;
	final ValueProxy<ESIndexType, String> proxy;

	ESIndexType(String label, boolean open, Class<? extends BaseEntity> entity) {
		this.value = name().toLowerCase();
		this.label = label;
		this.entity = entity;
		this.open = open;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	ESIndexType(String label, Class<? extends BaseEntity> entity) {
		this(label, true, entity);
	}

	public static final ESIndexType[] CACHE = ArrayUtil.filter(values(), ESIndexType::isOpen);

}
