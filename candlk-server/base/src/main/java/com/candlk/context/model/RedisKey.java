package com.candlk.context.model;

import javax.annotation.Nullable;

/***
 * 全局共享的 Redis 键
 */
public interface RedisKey {

	/** APP审核版本 */
	String APP_VERSION = "app_version:";

	/** 用户操作分布式锁前缀 */
	String USER_OP_LOCK_PREFIX = "user:lock:";

	/** 用户是否晋级前缀 Set < "$merchantId-$userId" > */
	String UPGRADE_USER = "upgrade:user";

	static String upgradeUserKey(Long merchantId, Long userId) {
		return merchantId + "-" + userId;
	}

	/** 商户游戏类型等级 hash < vendor+month, merchantId+currency+type, level > */
	String MERCHANT_GAME_LEVEL_PREFIX = "merchant:game:level:";

	/** 商户站点状态 ZSet < merchantId,status > */
	String MERCHANT_SITE_STATUS = "merchant:site:status";

	/** 用户的游戏 投入/产出 汇总 ZSet < $userId-IN/OUT, 金额（最后两位表示小数）> 【投入用负数表示，便于后续区分排行】 */
	String USER_PLAY_IN_OUTS = "userPlayInOuts";

	/** 用整数表示的投入金额（最后两位整数表示小数） */
	static long userPlayIn(@Nullable Double score) {
		if (score == null) {
			return 0L;
		}
		return Math.abs(score.longValue());
	}

	/** 【真实的】投入金额 */
	static double unboxUserPlayIn(@Nullable Double score) {
		if (score == null) {
			return 0D;
		}
		return Math.abs(score.longValue()) / 100D;
	}

	/** 将最后两位表示小数的整数转为对应的 double 真实小数 */
	static double unboxInt(@Nullable Double score) {
		if (score == null) {
			return 0D;
		}
		return score.longValue() / 100D;
	}

	/**
	 * 用户游戏投入按 周 | 月 汇总
	 * ZSet < "userIns-$yyyyMMdd", "$userId", 投入 >
	 * < "userIns-$yyyyMM", "$userId", 投入 >
	 */
	String USER_PLAY_INS = "userPlayIns";

	/**
	 * 用户充值 按 周 | 月 | 累计 汇总
	 * ZSet < "userRecharge-$yyyyMMdd", "$userId", 充值金额 >
	 * < "userRecharge-$yyyyMM", "$userId", 充值金额 >
	 * < "userRecharge", "$userId", 充值金额 >
	 */
	String USER_RECHARGE = "userRecharge";

	/** 用户VIP升级集合 Set < "userUpgrade-$level", "$userId" > */
	String USER_UPGRADE = "userUpgrade";

	/** 用户的游戏 投入 后缀 */
	String USER_PLAY_IN_SUFFIX = "-IN";

	/** 用户的游戏 产出 后缀 */
	String USER_PLAY_OUT_SUFFIX = "-OUT";

	/** 用户游戏 累计提现总额 后缀 */
	String USER_PLAY_CASH_SUFFIX = "-CASH";

	/**
	 * 用户游戏 累计充值总额/层级 后缀
	 * <pre>
	 * Max累计充值额：10 000 000 000 000（13位，整数最后2位表示小数）
	 * Max层级ID：99
	 *
	 * 写充值总额：value * 100
	 * 写层级：value + 100
	 *
	 * 注意：抹除小数位！
	 * 取充值总额：value / 100
	 * 取层级：value % 100（层级ID从0递增）
	 * <pre>
	 */
	String USER_PLAY_RECHARGE_SUFFIX = "-RECHARGE";

	/** 用户游戏 累计活动福利总额 后缀 */
	String USER_PLAY_ACTIVITY_SUFFIX = "-ACTIVITY";

	/** 用户的固定层级后缀 */
	String USER_FIXED_SUFFIX = "-FIXED";

	/**
	 * 商户游戏列表 ZSet < "merchant:game"+$merchantId, $game, score[type,vendor,gameId] >
	 * score：前3位代表游戏类型ID；4-6位代表厂商ID，最后9位代表游戏ID
	 */
	String MERCHANT_GAME_PREFIX = "merchant:game:";

	static long scoreForTypeId_vendorId_gameId(int type, int vendorId, int gameId) {
		return type * 1000_000_000_000L + vendorId * 1000_000_000L + gameId;
	}

	/** 商户热门游戏列表 ZSet < "merchant:hot:game:"+$merchantId, $game, score[type,gameId] > */
	String MERCHANT_HOT_GAME_PREFIX = "merchant:hot:game:";

	static long scoreForTypeId_gameId(int type, int gameId) {
		return type * 1000_000_000L + gameId;
	}

	/** 用户最近玩的游戏列表 ZSet < "user:last:game:"+$userId, $id > */
	String USER_LAST_GAME_PREFIX = "user:last:game:";

	/** 商户阶段性（尚未结算的部分）的游戏分赢汇总 ZSet <  "$merchantId-$yyyyMM", 输赢值 > */
	String MERCHANT_STAGE_TOTAL_WINS = "merchantStageTotalWins";

	/**
	 * 商户黑名单（全部商家共用）
	 * {@code Set <  "$userId-$limitType","$merchantId-$IP-$limitType" > }
	 */
	String MERCHANT_BLACKLIST = "merchant:blacklist";

	/** 商户-自研游戏-控制数据实时统计 */
	String MERCHANT_CONTROL_STAT = "merchant:control_stat:";

	/**
	 * 商户充值 汇总
	 * hash < "merchantRecharge", "merchantId", 充值金额 >
	 */
	String MERCHANT_RECHARGE = "merchantRecharge";

	/** 商户是否晋级前缀 Set < merchantId > */
	String UPGRADE_MERCHANT = "upgrade:merchantId";

	/** 商品订单操作分布式锁前缀 */
	String GOODS_ORDER_OP_LOCK_PREFIX = "goods_order:lock:";

	/**
	 * ZSet < "userLayers", userId , score[auto,fix]
	 * score 前两位为自动层级，后两位为固定层级
	 * 写入自动层级auto=auto*100，读取auto=auto/100
	 */
	String USER_LAYERS = "userLayers";
	/** 首充活动 ZSet < "firstRechargePromotion", $promotionId+$promotionCond+$yyyyMMDD+$userId , rechargeAmount > */
	String FIRST_RECHARGE_PROMOTION = "firstRechargePromotion";
	/** 单笔充值活动 List < "singleRechargePromotion", $$promotionId+$promotionCond+$yyyyMMDD+$userId , rechargeAmount > */
	String SINGLE_RECHARGE_PROMOTION = "singleRechargePromotion";
	/** 累计充值活动 ZSet < "totalRechargePromotion", $promotionId+$promotionCond+$cycleMode+$yyyyMMDD+$userId, rechargeAmount > */
	String TOTAL_RECHARGE_PROMOTION = "totalRechargePromotion";
	/** 累计打码活动 ZSet < "totalPayPromotion", $promotionId+$cycleMode+$yyyyMMDD+$userId, payAmount > */
	String TOTAL_PAY_PROMOTION = "totalPayPromotion";
	/**
	 * 用户本周初 | 本月初 VIP等级
	 * Hash < "userLevel"-yyyyMM, $userId, level >
	 * Hash < "userLevel"-yyyyMMdd, $userId, level >
	 * Hash < "userLevel", $userId, level >
	 */
	String USER_LEVEL = "userLevel";

	/** 用户跑马灯公告 Hash< 商户ID, version > */
	String USER_MSG_MARQUEE_VERSION = "userMsgMarqueeVersion";

}
