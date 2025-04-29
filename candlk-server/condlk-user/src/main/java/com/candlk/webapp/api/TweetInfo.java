package com.candlk.webapp.api;

import java.util.Date;
import java.util.List;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.*;

@Setter
@Getter
public class TweetInfo {

	/** 推文ID */
	public String id;
	/** 作者用户ID */
	@JSONField(name = "author_id")
	public String authorId;
	/** 内容文本（摘要 280） */
	public String text;
	/** 推文的全部内容，包括超过 280 个字符的文本。 */
	@JSONField(name = "note_tweet")
	public NoteTweet noteTweet;

	public String getText() {
		return noteTweet == null ? text : noteTweet.text;
	}

	/** 创建时间 */
	@JSONField(name = "created_at")
	public Date createdAt;
	/** 公共指标 */
	@JSONField(name = "public_metrics")
	public PublicMetrics publicMetrics;
	/** 附件实体 */
	public Entities entities;

	/** 回复目标用户ID */
	@JSONField(name = "in_reply_to_user_id")
	public String inReplyToUserId;
	/** 回复的帖子ID */
	@JSONField(name = "conversation_id")
	public String conversationId;
	/** 引用帖子信息 */
	@JSONField(name = "referenced_tweets")
	public List<ReferencedTweets> referencedTweets;

	/** 历史编辑的推文ID */
	@JSONField(name = "edit_history_tweet_ids")
	public List<String> editHistoryTweetIds;
	/** 敏感标识 */
	@JSONField(name = "possibly_sensitive")
	public Boolean possiblySensitive;
	/** 语言 */
	public String lang;
	/** 媒体的元数据 */
	@JSONField(name = "media_metadata")
	public List<MediaMetadata> mediaMetadata;
	/** 编辑空间 */
	@JSONField(name = "edit_controls")
	public EditControls editControls;
	/** 附件 */
	public Attachments attachments;
	/** 显示文本范围 */
	@JSONField(name = "display_text_range")
	public List<Integer> displayTextRange;
	/**
	 * 从推文文本推断出的注释：主要是用来提供推文内容的语义背景的
	 * <pre>
	 * 具体来说，context_annotations 里的信息通常包括两部分：
	 *
	 * domain：一个大的分类，比如「体育赛事」「人物」「电影」「品牌」等等。
	 * entity：更具体的对象，比如某个具体的运动员、电影名字、品牌名字等。
	 *
	 * 主要用途是：
	 *
	 * 让 Twitter 更好地理解一条推文是在讲什么；
	 * 用来改进推荐系统，比如向对某个主题感兴趣的用户推荐相关推文；
	 * 用来支持搜索、话题聚合（比如你搜某个人名、品牌，可以更精准地关联相关推文）；
	 * 给广告投放、趋势分析提供更多背景数据。
	 * </pre>
	 */
	@JSONField(name = "context_annotations")
	public List<ContextAnnotations> contextAnnotations;

	@Getter
	@Setter
	public static class EditControls {

		/** 编辑剩余次数 */
		@JSONField(name = "edits_remaining")
		public Integer editsRemaining;
		/** 编辑合格吗？ */
		@JSONField(name = "is_edit_eligible")
		public Boolean isEditEligible;
		/** 可编辑的,直到 */
		@JSONField(name = "editable_until")
		public String editableUntil;

	}

	@Getter
	@Setter
	public static class Attachments {

		@JSONField(name = "media_keys")
		public List<String> mediaKeys;

	}

	@Getter
	@Setter
	public static class PublicMetrics {

		/** 转发数 */
		@JSONField(name = "retweet_count")
		public Integer retweetCount;
		/** 回复数 */
		@JSONField(name = "reply_count")
		public Integer replyCount;
		/** 点赞 */
		@JSONField(name = "like_count")
		public Integer likeCount;
		/** 引用数 */
		@JSONField(name = "quote_count")
		public Integer quoteCount;
		/** 书签数 */
		@JSONField(name = "bookmark_count")
		public Integer bookmarkCount;
		/** 浏览数 */
		@JSONField(name = "impression_count")
		public Integer impressionCount;

	}

	@Getter
	@Setter
	public static class Entities {

		/** 附件URL */
		public List<Urls> urls;
		/** 提及的用户 */
		public List<Mentions> mentions;

		@Getter
		@Setter
		public static class Urls {

			public Integer start;
			public Integer end;
			/** 短链接URL */
			public String url;
			/** 完整URL */
			@JSONField(name = "expanded_url")
			public String expandedUrl;
			/** 显示URL */
			@JSONField(name = "display_url")
			public String displayUrl;
			/** 媒体Key */
			@JSONField(name = "media_key")
			public String mediaKey;

		}

		@Getter
		@Setter
		public static class Mentions {

			public Integer start;
			public Integer end;
			/** 提及用户名 */
			public String username;
			/** 用户ID */
			public String id;

		}

	}

	@Getter
	@Setter
	public static class NoteTweet {

		public String text;

	}

	@Getter
	@Setter
	public static class MediaMetadata {

		@JSONField(name = "media_key")
		public String mediaKey;

	}

	@Getter
	@Setter
	public static class ReferencedTweets {

		/** 引用类型：quoted=引用；replied_to=回复； */
		public String type;
		/** 应用的帖子ID */
		public String id;

	}

	@Getter
	@Setter
	public static class ContextAnnotations {

		/** 表示上下文注释域的数据 */
		public Domain domain;
		public Entity entity;

		@Getter
		@Setter
		public static class Domain {

			/** 上下文注释域的唯一 ID */
			public String id;
			/** 注释域名称 */
			public String name;
			/** 注释域描述 */
			public String description;

		}

		@Getter
		@Setter
		public static class Entity {

			public String id;
			public String name;

		}

	}

}
