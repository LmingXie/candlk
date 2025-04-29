package com.candlk.webapp.api;

import java.util.List;

import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.*;

@Getter
@Setter
public class TweetUserInfo {

	/** 用户ID */
	public String id;
	/** 个人资料横幅网址 */
	@JSONField(name = "profile_banner_url")
	public String profileBannerUrl;
	/** 用户名【账号】 */
	public String username;
	/** 昵称 */
	public String name;
	/** 注册时间 */
	@JSONField(name = "created_at")
	public String createdAt;
	/** 描述 */
	public String description;
	/** 最近推文id */
	@JSONField(name = "most_recent_tweet_id")
	public String mostRecentTweetId;
	/** 置顶推文ID */
	@JSONField(name = "pinned_tweet_id")
	public String pinnedTweetId;
	/** 公共指标数据 */
	@JSONField(name = "public_metrics")
	public PublicMetrics publicMetrics;

	/** 展示地区 */
	public String location;
	/** 用户个人资料中指定的 URL */
	public String url;
	/** 是否是经过验证的 X 用户 */
	public Boolean verified;

	/** 这个账号是公开声明自己是“模仿”或“恶搞”某个人、品牌、机构的账号，而不是官方或真实的代表 */
	public Boolean parody;
	/** 个人资料图片url */
	@JSONField(name = "profile_image_url")
	public String profileImageUrl;
	/** 验证类型：用户的 X Blue 验证类型，例如：blue, government, business, none */
	@JSONField(name = "verified_type")
	public String verifiedType;
	/** 用户是否把自己的推文设为仅限已批准的关注者可见。 */
	@JSONField(name = "protected_x")
	public Boolean protectedX;
	/** 是否进行了身份验证 */
	@JSONField(name = "is_identity_verified")
	public Boolean isIdentityVerified;
	/** 订阅你 */
	public Subscription subscription;
	/** 订阅类型 */
	@JSONField(name = "subscription_type")
	public String subscriptionType;
	/** 个人资料中引用的数据 */
	public Entities entities;

	@Getter
	@Setter
	public static class PublicMetrics {

		/** 关注该用户的用户数 */
		@JSONField(name = "followers_count")
		public Integer followersCount;
		/** 该用户关注的用户数 */
		@JSONField(name = "following_count")
		public Integer followingCount;
		/** 该用户发布的帖子数（包括转推） */
		@JSONField(name = "tweet_count")
		public Integer tweetCount;
		/** 包含该用户的列表数量 */
		@JSONField(name = "listed_count")
		public Integer listedCount;
		/** 该用户创建的赞数 */
		@JSONField(name = "like_count")
		public Integer likeCount;
		/** 媒体数 */
		@JSONField(name = "media_count")
		public Integer mediaCount;

	}

	@Getter
	@Setter
	public static class Subscription {

		/** 订阅你 */
		@JSONField(name = "subscribes_to_you")
		public Boolean subscribesToYou;

	}

	public JSONObject toDescription() {
		JSONObject json = new JSONObject(4);
		json.put("text", this.description);
		if (this.entities != null && this.entities.description != null) {
			json.put("urls", this.entities.description.urls);
			json.put("mentions", this.entities.description.mentions);
		}
		return json;
	}

	@Getter
	@Setter
	public static class Entities {

		/** 描述 */
		public Description description;

		@Getter
		@Setter
		public static class Description {

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
				/** 拓展URL */
				@JSONField(name = "expanded_url")
				public String expandedUrl;
				/** 显示URL（超长...省略） */
				@JSONField(name = "display_url")
				public String displayUrl;

			}

			@Getter
			@Setter
			public static class Mentions {

				public Integer start;
				public Integer end;
				public String username;

			}

		}

	}

}
