package com.candlk.webapp.user.vo;

import java.util.Date;
import java.util.List;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GmgnTweetEventVO {

	/** 订阅频道 */
	public String channel;

	public List<Data> data;

	@Getter
	@Setter
	public static class Data {

		/** Gmgn 推文ID */
		@JSONField(name = "i")
		public String id;
		/** 推文ID */
		@JSONField(name = "ti")
		public String tweetId;

		/** 推文类型：tweet=发推；reply=回复；repost=转帖；quote=引用；description=简介更新； */
		@JSONField(name = "tw")
		public String twType;

		@JSONField(name = "ts")
		public Date twTimestamp;

		@JSONField(name = "cp")
		public Integer complete;

		@JSONField(name = "u")
		public TwitterUser user;

		/** 推文内容 */
		@JSONField(name = "c")
		public TweetContent content;

		/** 引用的推文ID */
		@JSONField(name = "si")
		public String sourceId;
		/** 引用的用户信息 */
		@JSONField(name = "su")
		public TwitterUser sourceUser;
		/** 引用的推文信息 */
		@JSONField(name = "sc")
		public TweetContent sourceContent;

		/** 用户标签：kol=KOL；master=牛人榜；trader=交易员； */
		@JSONField(name = "ut")
		public List<String> userTags;

		/** 简介更新事件：用户资料 */
		@JSONField(name = "p")
		public Profile profile;

	}

	@Getter
	@Setter
	public static class TwitterUser {

		/** 用户名/账户名 */
		@JSONField(name = "s")
		public String username;
		/** 昵称 */
		@JSONField(name = "n")
		public String name;
		/** 头像 */
		@JSONField(name = "a")
		public String avatar;

	}

	@Getter
	@Setter
	public static class TweetContent {

		/** 推文完整内容 */
		@JSONField(name = "t")
		public String text;
		/** 媒体 */
		@JSONField(name = "m")
		public List<Media> media;

	}

	@Getter
	@Setter
	public static class Media {

		/** 媒体类型：image=图片；video=视频；thumbnail=缩略图（通常是图片）； */
		@JSONField(name = "t")
		public String type;
		/** 媒体资源完整路径 */
		@JSONField(name = "u")
		public String url;

	}

	@Getter
	@Setter
	public static class Profile {

		/** 最新简介 */
		@JSONField(name = "d")
		public String description;

		@JSONField(name = "u")
		public List<ProfileUrl> url;

	}

	@Getter
	@Setter
	public static class ProfileUrl {

		/** 简介中引用的链接摘要 */
		@JSONField(name = "n")
		public String name;
		/** 简介中引用的原始链接 */
		@JSONField(name = "u")
		public String url;

	}

}
