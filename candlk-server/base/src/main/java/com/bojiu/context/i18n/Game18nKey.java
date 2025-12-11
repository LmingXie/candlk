package com.bojiu.context.i18n;

import com.bojiu.context.model.BaseI18nKey;

public interface Game18nKey extends BaseI18nKey {

	/** 您无法进行提佣操作！ */
	String CASH_COMMISSION_NOT_ALLOWED = "cash.commission.not.allowed";
	/** 佣金可领取时间：{0} - {1} */
	String CASH_COMMISSION_RECEIVE_TIME = "cash.commission.receive.time";
	/** 暂无提取佣金！ */
	String NOT_CASH_COMMISSION = "not.cash.commission";
	/** 打码额度需达到{0}才可提取~ */
	String CASH_COMMISSION_PLAY_LIMIT = "cash.commission.play.limit";
	/** 佣金存在差异，请联系客服！ */
	String CASH_COMMISSION_RECEIVE_ERROR = "cash.commission.receive.error";
	/** 该游戏正在维护中！ */
	String GAME_UPDATE_STOP = "game.update.stop";
	/** 请先进行充值后可进行游戏。！ */
	String GAME_RECHARGE_LIMITED = "game.recharge.limited";
	/** 每日最多领取{0}次 */
	String DAILY_TIMES_LIMIT = "daily.times.limit";
	/** 最低领取金额：{0} */
	String LEAST_RECEIVE_AMOUNT_LIMIT = "least.receive.amount.limit";
	/** 账户余额不足{0}，无法进入该游戏！ */
	String PLAY_COIN_NOT_ENOUGH = "play.coin.not.enough";
	/** 进入游戏时出现网络异常，请稍后再试！ */
	String JOIN_GAME_NETWORK_ERROR = "join.game.network.error";
	/** 第三方游戏网络异常，请稍后再试！ */
	String GAME_VENDOR_NETWORK_ERROR = "game.vendor.network.error";
	/** 业务状态异常，请联系客服！ */
	String BIZ_STATUS_ERROR = "biz.status.error";
	/** 不支持查询超过{0}天的数据 */
	String MULTIPLE_TABLE_QUERY_LIMIT = "multiple.table.query.limit";

	// SpyType
	/** 高倍爆奖 */
	String SPY_TYPE_HIGH_RTP = "spy.type.high.rtp";
	/** 大额中奖 */
	String SPY_TYPE_BIG_REWARD = "spy.type.big.reward";
	/** 会员当天获利比 */
	String SPY_TYPE_SUN_PROFIT_RATE = "spy.type.sun.profit.rate";

	// 控制类型
	/** 固定层级 */
	String CONTROL_TYPE_FIXED_LAYER = "control.type.fixed.layer";
	/** 自动层级 */
	String CONTROL_TYPE_AUTO_LAYER = "control.type.auto.layer";
	/** 代理 */
	String CONTROL_TYPE_AGENT = "control.type.agent";
	/** 推广 */
	String CONTROL_TYPE_TEAM = "control.type.team";

	// PGC抽成模式
	/** 损益模式 */
	String COMMISSION_MODE_PL = "commission.mode.pl";
	/** 打码模式 */
	String COMMISSION_MODE_PLAY = "commission.mode.play";

	/** 领取成功 */
	String REWARD_RECEIVED_SUCCESS = "game.reward.received.success";

}