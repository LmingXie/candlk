package com.candlk.webapp.user.vo;

import java.math.BigDecimal;
import java.util.Date;

import com.candlk.webapp.base.vo.AbstractVO;
import com.candlk.webapp.user.entity.TweetUser;
import com.candlk.webapp.user.model.TweetProvider;
import com.candlk.webapp.user.model.TweetUserType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TweetUserVO extends AbstractVO<TweetUser> {

	/** 推文来源厂商类型 */
	TweetProvider providerType;
	/** 推特用户ID */
	String userId;
	/** 推特用户账号名 */
	String username;
	/** 推特昵称 */
	String nickname;
	/** 推特头像 */
	String avatar;
	/** 推特横幅 */
	String banner;
	/** 置顶推文（JSON） */
	String pinned;
	/** 地区 */
	String location;
	/** 推特简介 */
	String description;
	/** 关注该用户的用户数 */
	Integer followers;
	/** 该用户发布的帖子数（包括转推） */
	Integer tweets;
	/** 该用户关注的用户数 */
	Integer following;
	/** 该用户发布的媒体数 */
	Integer media;
	/** 包含该用户的列表数量 */
	Integer listed;
	/** 该用户创建的赞数 */
	Integer likes;
	/** 最后一次发帖时间 */
	Date tweetLastTime;
	/** 账号类型：0=普通账号；1=特殊关注账号；2=二级账号； */
	TweetUserType type;
	/** 推文分数 */
	BigDecimal score;

}