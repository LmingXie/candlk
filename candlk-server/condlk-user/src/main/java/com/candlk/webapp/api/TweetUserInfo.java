package com.candlk.webapp.api;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class TweetUserInfo {

	/** 用户ID */
	public String id;
	/** 个人资料横幅网址 */
	public String profileBannerUrl;
	/** 用户名【账号】 */
	public String username;
	/** 昵称 */
	public String name;
	/** 注册时间 */
	public String createdAt;
	/** 描述 */
	public String description;
	/** 最近推文id */
	public String mostRecentTweetId;
	/** 置顶推文ID */
	public String pinnedTweetId;
	/** 公共指标数据 */
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
	public String profileImageUrl;
	/** 验证类型：用户的 X Blue 验证类型，例如：blue, government, business, none */
	public String verifiedType;
	/** 用户是否把自己的推文设为仅限已批准的关注者可见。 */
	public Boolean protectedX;
	/** 是否进行了身份验证 */
	public Boolean isIdentityVerified;
	/** 订阅你 */
	public Subscription subscription;
	/** 订阅类型 */
	public String subscriptionType;
	/** 个人资料中引用的数据 */
	public Entities entities;

	@NoArgsConstructor
	@Data
	public static class PublicMetrics {

		/** 关注该用户的用户数 */
		public Integer followersCount;
		/** 该用户关注的用户数 */
		public Integer followingCount;
		/** 该用户发布的帖子数（包括转推） */
		public Integer tweetCount;
		/** 包含该用户的列表数量 */
		public Integer listedCount;
		/** 该用户创建的赞数 */
		public Integer likeCount;
		/** 媒体算 */
		public Integer mediaCount;

	}

	@NoArgsConstructor
	@Data
	public static class Subscription {

		/** 订阅你 */
		public Boolean subscribesToYou;

	}

	@NoArgsConstructor
	@Data
	public static class Entities {

		/** 描述 */
		public Description description;

		@NoArgsConstructor
		@Data
		public static class Description {

			/** 附件URL */
			public List<Urls> urls;
			/** 提及的用户 */
			public List<Mentions> mentions;

			@NoArgsConstructor
			@Data
			public static class Urls {

				public Integer start;
				public Integer end;
				/** 短链接URL */
				public String url;
				/** 拓展URL */
				public String expandedUrl;
				/** 显示URL（超长...省略） */
				public String displayUrl;

			}

			@NoArgsConstructor
			@Data
			public static class Mentions {

				public Integer start;
				public Integer end;
				public String username;

			}

		}

	}

}
