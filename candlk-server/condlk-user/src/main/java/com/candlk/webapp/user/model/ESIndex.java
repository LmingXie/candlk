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
public enum ESIndex implements ValueProxyImpl<ESIndex, String> {
	/**
	 * 关键词索引
	 * <pre>
	 * 关于分词器：
	 *  中文分词：ik_max_word
	 * 	  ik_max_word: 会将文本做最细粒度的拆分，比如会将“中华人民共和国国歌”拆分为“中华人民共和国\中华人民\中华\华人\人民共和国\人民\人\民\共和国\共和\和\国国\国歌”，会穷尽各种可能的组合。
	 * 	  ik_smart: 会做最粗粒度的拆分，比如会将“中华人民共和国国歌”拆分为“中华人民共和国\国歌”。
	 *  英文分词：english（自带）
	 *  法语分词：french（自带）
	 *  西班牙语分词：spanish（自带）
	 * </pre>
	 */
	KEYWORDS_INDEX("关键词模糊匹配索引", TweetWord.class),
	KEYWORDS_ACCURATE_INDEX("关键词精确匹配索引", TweetWord.class),
	/** 停用词索引 */
	STOP_WORDS_INDEX("停用词索引", StopWord.class),
	;

	@EnumValue
	public final String value;
	public final Class<? extends BaseEntity> entity;
	public final String label;
	public final boolean open;
	final ValueProxy<ESIndex, String> proxy;

	ESIndex(String label, boolean open, Class<? extends BaseEntity> entity) {
		this.value = name().toLowerCase();
		this.label = label;
		this.entity = entity;
		this.open = open;
		this.proxy = new ValueProxy<>(this, value, label);
	}

	ESIndex(String label, Class<? extends BaseEntity> entity) {
		this(label, true, entity);
	}

	public static final ESIndex[] CACHE = ArrayUtil.filter(values(), ESIndex::isOpen);

}
