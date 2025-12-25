package com.bojiu.context.i18n;

import com.bojiu.context.model.BaseI18nKey;

public interface TradeI18nKey extends BaseI18nKey {

	/** 该支付通道正在维护中，请更换其他支付通道或联系客服！ */
	String PAYMENT_STOP = "payment.stop";
	/** 您尚未完善资料，请先完善如有疑问请联系客服！ */
	String PAYMENT_FORCE_PROFILE = "payment.force.profile";
	/** 该支付渠道暂不支持兑换，请更换其他渠道！ */
	String PAYMENT_CLOSE = "payment.close";
	/** 支付通道不可用，请联系客服 */
	String PAYMENT_SUSPEND = "payment.suspend";
	/** 当前支付通道暂不支持该货币类型！ */
	String PAYMENT_CURRENCY_CLOSE = "payment.currency.close";
	/** 请输入有效的转出地址！ */
	String FROM_ACCOUNT_INVALID = "from.account.invalid";
	/** 请核实收款账号是否正确！ */
	String CASH_ACCOUNT_INVALID = "cash.account.invalid";
	/** 收款账号已经存在！ */
	String CASH_ACCOUNT_EXISTED = "cash.account.existed";
	/** 货币汇率或手续费率发生变化，请刷新页面确认无误后再试！ */
	String LOCAL_FEE_RATE_CHANGE = "local.fee.rate.change";
	/** 您当前的VIP等级每天最多可兑换{0}次，请提升VIP等级！ */
	String CASH_TIMES_LIMIT = "cash.times.limit";
	/** 您当前的VIP等级总共最多可兑换{0}次，请提升VIP等级！ */
	String CASH_TOTAL_TIMES_LIMIT = "cash.total.times.limit";
	/** 您当前的VIP等级每天最多可兑换{0}，请提升VIP等级！ */
	String CASH_AMOUNT_LIMIT = "cash.amount.limit";
	/** 您当前的VIP等级总共最多可兑换{0}，请提升VIP等级！ */
	String CASH_TOTAL_AMOUNT_LIMIT = "cash.total.amount.limit";
	/** 您正在进行中的兑换申请不可超过{0}笔！ */
	String CASH_APPLY_TIMES_LIMIT = "cash.apply.times.limit";
	/** 您的首次兑换金额不能低于{0} */
	String FIRST_CASH_AMOUNT_LIMIT = "first.cash.amount.limit";
	/** 您的兑换金额不能超过可提现额度！ */
	String CASH_AMOUNT_NOT_ENOUGH = "cash.amount.not.enough";
	/** 您的累计充值金额需超过{0}方可申请兑换！ */
	String TOTAL_RECHARGE_AMOUNT_LIMIT = "total.recharge.amount.limit";
	/** 兑换金额必须大于提现手续费！ */
	String CASH_AMOUNT_GT_FREE = "cash.amount.gt.free";
	/** 查询到该订单尚未支付成功！ */
	String QUERY_ORDER_NO_PAY = "query.order.no.pay";
	/** 查询订单状态时出错，请稍后再试！ */
	String QUERY_ORDER_ERROR = "query.order.error";
	/** 该功能已下线。如有疑问，请联系客服！ */
	String MODULE_CLOSED = "module.closed";
	/** 金额不得低于{0} */
	String AMOUNT_MIN_LIMIT = "amount.min.limit";
	/** 转出金额不能超过存入本金！ */
	String TRANSFER_AMOUNT_LE_AMOUNT = "transfer.amount.le.amount";
	/** 您暂无可提取的收益，请耐心等待收益结算！ */
	String INCOME_AMOUNT_NOT_ENOUGH = "income.amount.not.enough";
	/** 创建订单失败，请稍后再试或联系客服！ */
	String CREATE_ORDER_FAIL = "create.order.fail";
	/** 该支付通道最多只能绑定{0}个收款账号！！ */
	String CASH_ACCOUNT_MAX_BIND = "cash.account.max.bind";
	/** 您当前累积提现金额已超盈利上限！ */
	String CASH_AMOUNT_GT_PROFIT = "cash.amount.gt.profit";

	/*
	 * Model放置
	 */
	// CashLogForm
	/** 账号 */
	String CASH_LOG_FORM_ACCOUNT_ID = "cash.log.form.account.id";
	/** 金额 */
	String CASH_LOG_FORM_AMOUNT = "cash.log.form.amount";
	/** 支付平台 */
	String CASH_LOG_FORM_CHANNEL = "cash.log.form.channel";
	/** 维护费 */
	String CASH_LOG_FORM_FEE = "cash.log.form.fee";
	/** 兑换密码 */
	String CASH_LOG_FORM_PASSWORD = "cash.log.form.password";
	// HandleResult
	/** 处理成功 */
	String HANDLE_RESULT_OK = "handle.result.ok";
	/** 订单已关闭 */
	String HANDLE_RESULT_CLOSED = "handle.result.closed";
	/** 正在处理中 */
	String HANDLE_RESULT_PENDING = "handle.result.pending";
	/** 指定的订单不存在 */
	String HANDLE_RESULT_NOT_FOUND = "handle.result.not.found";
	/** 接口验签失败 */
	String HANDLE_RESULT_SIGN_ERROR = "handle.result.sign.error";
	/** 接口请求网络异常，请稍后再试！ */
	String HANDLE_RESULT_NETWORK_ERROR = "handle.result.network.error";
	/** 系统异常，请稍后再试！ */
	String HANDLE_RESULT_UNEXPECTED_ERROR = "handle.result.unexpected.error";
	/** 状态已变更 */
	String HANDLE_RESULT_STATE_CHANGED = "handle.result.state.changed";
	// CashStatus
	/** 兑换失败 */
	String CASH_STATUS_CLOSED = "cash.status.closed";
	/** 兑换失败（待退款） */
	String CASH_STATUS_FAILED = "cash.status.failed";
	/** 兑换失败（拒绝退款） */
	String CASH_STATUS_DENY = "cash.status.deny";
	/** 第三方退款 */
	String CASH_STATUS_BACK = "cash.status.back";
	/** 已取消 */
	String CASH_STATUS_CANCELED = "cash.status.canceled";
	/** 复审不通过 */
	String CASH_STATUS_REVIEW_FALSE = "cash.status.review.false";
	/** 初审不通过 */
	String CASH_STATUS_VERIFY_FALSE = "cash.status.verify.false";
	/** 待付款 */
	String CASH_STATUS_INIT = "cash.status.init";
	/** 待复审 */
	String CASH_STATUS_VERIFY_TRUE = "cash.status.verify.true";
	/** 复审通过 */
	String CASH_STATUS_REVIEW_TRUE = "cash.status.review.true";
	/** 已提交到第三方 */
	String CASH_STATUS_SUBMIT = "cash.status.submit";
	/** 支付通道处理中 */
	String CASH_STATUS_PENDING = "cash.status.pending";
	/** 已付款 */
	String CASH_STATUS_OK = "cash.status.ok";
	/** 待出款 */
	String CASH_STATUS_WAITING_AUDIT = "cash.status.waiting.audit";
	/** 已出款 */
	String CASH_STATUS_SUCCESS = "cash.status.success";
	/** 取消出款 */
	String CASH_STATUS_CANCEL = "cash.status.cancel";
	/** 拒绝 */
	String CASH_STATUS_REJECT = "cash.status.reject";
	/** 提现审核 */
	String CASH_STATUS_REVIEW = "cash.status.review";
	/** 提现成功 */
	String CASH_SUCCESS = "cash.success";
	/** 提现拒绝 */
	String CASH_REJECT = "cash.reject";
	/** 出款中 */
	String CASH_REVIEW = "cash.review";
	/** 提现取消 */
	String CASH_CANCELED = "cash.canceled";

	// RechargeStatus
	/** 存款取消 */
	String RECHARGE_STATUS_CLOSED = "recharge.status.closed";
	/** 存款失败 */
	String RECHARGE_STATUS_FAIL = "recharge.status.fail";
	/** 存款超时 */
	String RECHARGE_STATUS_TIMEOUT = "recharge.status.timeount";
	/** 等待付款 */
	String RECHARGE_STATUS_INIT = "recharge.status.init";
	/** 确认中 */
	String RECHARGE_STATUS_PENDING = "recharge.status.pending";
	/** 锁定 */
	String RECHARGE_STATUS_LOCK = "recharge.status.lock";
	/** 存款成功 */
	String RECHARGE_STATUS_OK = "recharge.status.ok";
	// CoinTradeType
	/** 充值 */
	String COIN_TRADE_TYPE_COIN_RECHARGE = "coin.trade.type.coin.recharge";
	/** 打码 */
	String COIN_TRADE_TYPE_COIN_PLAY = "coin.trade.type.coin.play";
	/** 转入游戏余额 */
	String COIN_TRADE_TYPE_TRANS_IN = "coin.trade.type.trans.in";
	/** 转出游戏余额 */
	String COIN_TRADE_TYPE_TRANS_OUT = "coin.trade.type.trans.out";
	/** 申请提现 */
	String COIN_TRADE_TYPE_CASH_APPLY = "coin.trade.type.cash.apply";
	/** 兑换成功 */
	String COIN_TRADE_TYPE_CASH_APPLY_TRUE = "coin.trade.type.cash.apply.true";
	/** 兑换失败 */
	String COIN_TRADE_TYPE_CASH_APPLY_FALSE = "coin.trade.type.cash.apply.false";
	/** 充值优惠 */
	String COIN_TRADE_TYPE_COIN_RECHARGE_REWARD = "coin.trade.type.coin.recharge.reward";
	/** 线下提现拒绝（不退还用户余额） */
	String COIN_TRADE_TYPE_CASH_OFFLINE_FALSE = "coin.trade.type.cash.offline.false";
	/** 兑换失败、到账后退回 */
	String COIN_TRADE_TYPE_CASH_APPLY_BACK = "coin.trade.type.cash.apply.back";
	/** VIP升级奖励 */
	String COIN_TRADE_TYPE_UPGRADE_REWARDS = "coin.trade.type.upgrade.rewards";
	/** VIP每日奖励 */
	String COIN_TRADE_TYPE_DAILY_REWARDS = "coin.trade.type.daily.rewards";
	/** VIP每周奖励 */
	String COIN_TRADE_TYPE_WEEKLY_REWARDS = "coin.trade.type.weekly.rewards";
	/** VIP每月奖励 */
	String COIN_TRADE_TYPE_MONTH_REWARDS = "coin.trade.type.month.rewards";
	/** 充值活动奖励 */
	String COIN_TRADE_TYPE_RECHARGE_REWARDS = "coin.trade.type.recharge.rewards";
	/** 打码活动奖励 */
	String COIN_TRADE_TYPE_PLAY_REWARDS = "coin.trade.type.play.rewards";
	/** 签到奖励 */
	String COIN_TRADE_TYPE_SIGN_REWARDS = "coin.trade.type.sign.rewards";
	/** 救援金奖励 */
	String COIN_TRADE_TYPE_RELIEF_REWARDS = "coin.trade.type.relief.rewards";
	/** 代理佣金 */
	String COIN_TRADE_TYPE_AGENT_COMMISSION = "coin.trade.type.agent.commission";
	/** 利息宝转入本金 */
	String COIN_TRADE_TYPE_INCOME_TRANS_IN = "coin.trade.type.income.trans.in";
	/** 利息宝转出本金 */
	String COIN_TRADE_TYPE_INCOME_TRANS_OUT = "coin.trade.type.income.trans.out";
	/** 利息宝收益 */
	String COIN_TRADE_TYPE_INCOME_EXTRACT = "coin.trade.type.income.extract";
	/** 实时返水 */
	String COIN_TRADE_TYPE_REBATE_REWARDS = "coin.trade.type.rebate.rewards";
	/** 余额修正-人工加款 */
	String COIN_TRADE_TYPE_BALANCE_REVISION_IN = "coin.trade.type.adjust.in";
	/** 余额修正-人工扣款 */
	String COIN_TRADE_TYPE_BALANCE_REVISION_OUT = "coin.trade.type.adjust.out";
	/** 余额修正-人工赠送 */
	String COIN_TRADE_TYPE_ADJUST_REWARD = "coin.trade.type.adjust.reward";
	/** 任务中心-领取任务奖励 */
	String COIN_TRADE_TYPE_TASK_REWARD_IN = "coin.trade.type.task.reward.in";
	/** 运营配置-有奖反馈奖励 */
	String COIN_TRADE_TYPE_FEEDBACK_IN = "coin.trade.type.task.feedback.in";
	/** 幸运转盘奖励 */
	String COIN_TRADE_TYPE_LOTTERY_REWARD = "coin.trade.type.lottery.reward";
	/** 红包奖励 */
	String COIN_TRADE_TYPE_RED_ENVELOPE_REWARD = "coin.trade.type.red.envelope.reward";
	/** 推广奖励 */
	String COIN_TRADE_TYPE_AGENT_REWARD = "coin.trade.type.agent.reward";
	/** 新人彩金 */
	String COIN_TRADE_TYPE_REDEEM_CODE_REWARD = "coin.trade.type.redeem.code.reward";
	/** 自定义活动 */
	String COIN_TRADE_TYPE_CUSTOM_REWARD = "coin.trade.type.custom.reward";
	/** 注册活动 */
	String COIN_TRADE_TYPE_REGISTER_REWARD = "coin.trade.type.register.reward";
	/** 数据校正 */
	String COIN_TRADE_TYPE_AUDIT_DATA_CORRECTION = "coin.trade.type.audit.data.correction";
	/** VIP稽核 */
	String COIN_TRADE_TYPE_VIP_UPGRADE_AUDIT = "coin.trade.type.vip.upgrade.audit";
	/** 闯关邀请奖励 */
	String COIN_TRADE_TYPE_CHALLENGE_INVITE_REWARD = "coin.trade.type.challenge.invite.reward";
	/** 闯关打码奖励 */
	String COIN_TRADE_TYPE_CHALLENGE_PLAY_REWARD = "coin.trade.type.challenge.play.reward";
	/** 救援金奖励 */
	String COIN_TRADE_TYPE_RELIEF_BALANCE_REWARD = "coin.trade.type.relief.balance.reward";
	/** 排行榜奖励 */
	String COIN_TRADE_TYPE_RANK_REWARD = "coin.trade.type.rank.reward";
	/** 现金兑换券奖励 */
	String COIN_TRADE_TYPE_TICKET_CASH_REWARD = "coin.trade.type.ticket.cash.reward";
	/** 幸运红包奖励 */
	String COIN_TRADE_TYPE_TICKET_RED_PACKET_REWARD = "coin.trade.type.ticket.red.packet.reward";
	/** 砸金蛋奖励 */
	String COIN_TRADE_TYPE_TICKET_GOLDEN_EGG_REWARD = "coin.trade.type.ticket.golden.egg.reward";
	/** 大转盘奖励 */
	String COIN_TRADE_TYPE_TICKET_TURNTABLE_REWARD = "coin.trade.type.ticket.turntable.reward";
	/** 网红佣金 */
	String COIN_TRADE_TYPE_RELIEF_BLOGGER_REWARD = "coin.trade.type.relief.blogger.reward";
	/** 网红奖励 */
	String COIN_TRADE_TYPE_BLOGGER_GIVE_REWARD = "coin.trade.type.blogger.give.reward";
	/** 代理邀请奖励 */
	String COIN_TRADE_TYPE_AGENT_INVITE_REWARD = "coin.trade.type.agent.invite.reward";
	/** 代理成就奖励 */
	String COIN_TRADE_TYPE_AGENT_MERIT_REWARD = "coin.trade.type.agent.merit.reward";
	/** 代理投注返佣奖励 */
	String COIN_TRADE_TYPE_AGENT_PLAY_REWARD = "coin.trade.type.agent.play.reward";
	/** 代理充值奖励 */
	String COIN_TRADE_TYPE_AGENT_RECHARGE_REWARD = "coin.trade.type.agent.recharge.reward";
	/** 资金调整 */
	String COIN_TRADE_TYPE_AMOUNT_ADJUST = "coin.trade.type.amount.adjust";
	/** 每日转盘奖励 */
	String COIN_TRADE_TYPE_DAILY_TURNTABLE_REWARD = "coin.trade.type.daily.turntable.reward";
	/** 拼团奖励 */
	String COIN_TRADE_TYPE_GROUP_REWARD = "coin.trade.type.group.reward";
	/** 充值优惠劵 */
	String COIN_TRADE_TYPE_TICKET_RECHARGE_REWARD = "coin.trade.type.ticket.recharge.reward";
	// IntegralTradeType
	/** 获得积分 */
	String INTEGRAL_TRADE_TYPE_ADD = "integral.trade.type.add";
	/** 公积金 */
	String PROMOTION_TYPE_DEPOSIT_POOL = "promotion.type.deposit.pool";
	/** 奖池 */
	String PROMOTION_TYPE_JACKPOT = "promotion.type.jackpot";
	/** 公积金奖励 */
	String PROMOTION_TYPE_DEPOSIT_POOL_REWARD = "promotion.type.deposit.pool.reward";
	/** 活动 */
	String REWARD_CATEGORY_PROMOTION = "@reward.category.promotion";
	/** 任务 */
	String REWARD_CATEGORY_TASK = "@reward.category.task";
	/** 返水 */
	String REWARD_CATEGORY_REBATE = "@reward.category.rebate";
	/** 待办的 */
	String REWARD_CATEGORY_PENDING = "@reward.category.pending";
	/** 返佣 */
	String REWARD_CATEGORY_AGENT = "@reward.category.agent";
	/** VIP奖励 */
	String REWARD_CATEGORY_VIP = "@reward.category.vip";
	/** 充值优惠 */
	String REWARD_CATEGORY_RECHARGE = "@reward.category.recharge";
	/** 利息宝 */
	String REWARD_CATEGORY_DEPOSIT = "@reward.category.deposit";
	/** 票券 */
	String REWARD_CATEGORY_TICKET = "@reward.category.ticket";
	/** 公积金 */
	String REWARD_CATEGORY_DEPOSIT_POOL = "@reward.category.deposit.pool";
	/** 奖励 */
	String REWARD_CATEGORY_REWARD = "@reward.category.reward";
	/** 本金不足 */
	String CAPITAL_INSUFFICIENT = "capital.insufficient";
	/** 请输入有效的{0}钱包地址！ */
	String VALID_ACCOUNT_ADDRESS = "valid.account.address";
	/** 请输入{0}位的有效账号地址！！ */
	String VALID_ACCOUNT_ADDRESS_COUNT = "valid.account.address.count";
	// 商户交易类型
	/** 充值 */
	String MERCHANT_TRADE_TYPE_RECHARGE = "merchant.trade.type.recharge";
	/** 运维费用 */
	String MERCHANT_TRADE_TYPE_OPS_FEE = "merchant.trade.type.ops-fee";
	/** 月度账单结算 */
	String MERCHANT_TRADE_TYPE_SETTLE = "merchant.trade.type.settle";
	/** 人工补单 */
	String MERCHANT_TRADE_TYPE_ADJUST_IN = "merchant.trade.type.adjust-in";
	/** 人工扣除 */
	String MERCHANT_TRADE_TYPE_ADJUST_OUT = "merchant.trade.type.adjust-out";
	/** 赠送奖励 */
	String MERCHANT_TRADE_TYPE_GIVE_AWARD = "merchant.trade.type.give-award";
	/** 开版费 + 维护费 */
	String MERCHANT_TRADE_TYPE_OPEN_LINE_FEE = "merchant.trade.type.open-line-fee";
	/** 维护费 */
	String MERCHANT_TRADE_TYPE_SERVER_FEE = "merchant.trade.type.server-fee";
	/** 增加授信额度 */
	String MERCHANT_TRADE_TYPE_OVERDRAFT_IN = "merchant.trade.type.overdraft-in";
	/** 减少授信额度 */
	String MERCHANT_TRADE_TYPE_OVERDRAFT_OUT = "merchant.trade.type.overdraft-out";
	/** 赠送奖励 */
	String MERCHANT_TRADE_TYPE_INVITE_REWARD = "merchant.trade.type.invite-reward";
	/** 商户转账 */
	String MERCHANT_TRADE_TYPE_TRANSFER = "merchant.trade.type.transfer";

	// 任务奖励限制
	/** 禁止同IP重复领取 */
	String TASK_REWARD_LIMIT_IP = "task.reward.limit.ip";
	/** 禁止同设备号重复领取 */
	String TASK_REWARD_LIMIT_DEVICE = "task.reward.limit.device";
	/** 已绑定收款方式 */
	String TASK_REWARD_LIMIT_BIND_CARD = "task.reward.limit.bind.card";
	/** 绑定手机号后领取 */
	String TASK_REWARD_LIMIT_BIND_PHONE = "task.reward.limit.bind.phone";
	/** 实名后领取 */
	String TASK_REWARD_LIMIT_REAL_NAME = "task.reward.limit.real.name";

	// 代理交易类型
	/** 代理分红 */
	String AGENT_TRADE_TYPE_AGENT_COMMISSION = "agent.trade.type.agent-commission";
	/** 人工加款 */
	String AGENT_TRADE_TYPE_ADJUST_IN = "agent.trade.type.adjust-in";
	/** 人工扣款 */
	String AGENT_TRADE_TYPE_ADJUST_OUT = "agent.trade.type.adjust-out";
	/** 申请兑换 */
	String AGENT_TRADE_TYPE_CASH_APPLY = "agent.trade.type.cash-apply";
	/** 兑换成功 */
	String AGENT_TRADE_TYPE_CASH_APPLY_TRUE = "agent.trade.type.cash-apply-true";
	/** 兑换失败 */
	String AGENT_TRADE_TYPE_CASH_APPLY_FALSE = "agent.trade.type.cash-apply-false";
	/** 兑换失败（已到账后退款） */
	String AGENT_TRADE_TYPE_CASH_APPLY_BACK = "agent.trade.type.cash-apply-back";

	// 提现渠道类型
	/** 地区货币 */
	String PAY_CATEGORY_WALLET = "pay.category.wallet";
	/** 数字货币 */
	String PAY_CATEGORY_USDT = "pay.category.usdt";
	/** 网银 */
	String PAY_CATEGORY_BANK = "pay.category.bank";
	/** 银行转账 */
	String PAY_CATEGORY_BANK_OFF = "pay.category.bank.off";

	/** 手动入账 */
	String ORDER_MANUAL = "order.manual";
	/** 强制取消 */
	String ORDER_CANCEL = "order.cancel";
	/** 您的次数已用完！ */
	String PROMOTION_USE_NOT_ENOUGH = "promotion.use.not.enough";
	// TradeCategory
	/** 资金切换 */
	String TRADE_CATEGORY_TRANS_GAME = "trade.category.trans.game";
	/** 会员充值 */
	String TRADE_CATEGORY_RECHARGE = "trade.category.recharge";
	/** 会员提现 */
	String TRADE_CATEGORY_CASH = "trade.category.cash";
	/** 银商结算 */
	String TRADE_CATEGORY_SILVER_MERCHANT_RECHARGE = "trade.category.silver.merchant.recharge";
	/** 资金修正 */
	String TRADE_CATEGORY_ADJUST_REWARD = "trade.category.adjust.reward";
	/** 活动 */
	String TRADE_CATEGORY_PROMOTION = "trade.category.promotion";
	/** 返水 */
	String TRADE_CATEGORY_REBATE = "trade.category.rebate";
	/** 返佣 */
	String TRADE_CATEGORY_COMMISSION = "trade.category.commission";
	/** 利息宝 */
	String TRADE_CATEGORY_INCOME = "trade.category.income";
	/** 任务 */
	String TRADE_CATEGORY_TASK = "trade.category.task";
	/** VIP奖励 */
	String TRADE_CATEGORY_VIP = "trade.category.vip";
	/** 充值优惠 */
	String TRADE_CATEGORY_RECHARGE_DISCOUNT = "trade.category.recharge.discount";
	/** 奖励 */
	String TRADE_CATEGORY_REWARD = "trade.category.reward";
	/** 担保理赔 */
	String TRADE_CATEGORY_GUARANTEED_CLAIMS = "trade.category.guaranteed.claims";
	/** 代理转账 */
	String TRADE_CATEGORY_AGENT_TRANSACTION = "trade.category.agent.transaction";
	/** 信用借款 */
	String TRADE_CATEGORY_CREDIT_LOAN = "trade.category.credit.loan";
	/** 保证金 */
	String TRADE_CATEGORY_MARGIN = "trade.category.margin";
	/** 俱乐部 */
	String TRADE_CATEGORY_CLUB = "trade.category.club";
	/** 幸运转盘 */
	String TRADE_CATEGORY_CIRCLE = "trade.category.circle";
	/** 公积金 */
	String TRADE_CATEGORY_DEPOSIT = "trade.category.deposit";
	/** 盲盒抽奖 */
	String TRADE_CATEGORY_BLIND_BOX = "trade.category.blind.box";
	/** 代理 */
	String TRADE_CATEGORY_AGENT = "trade.category.agent";
	/** 打赏 */
	String TRADE_CATEGORY_TIP = "trade.category.tip";
	/** Jackpot */
	String TRADE_CATEGORY_JACKPOT = "trade.category.jackpot";
	/** 有奖反馈 */
	String TRADE_CATEGORY_REWARD_FEEDBACK = "trade.category.reward.feedback";
	/** 网红博主 */
	String TRADE_CATEGORY_KOL = "trade.category.kol";
	/** 数据校正 */
	String TRADE_CATEGORY_DATA_CORRECTION = "trade.category.data.correction";
	// TradeTwoCategory
	/** 资金切换转入 */
	String TRADE_TWO_CATEGORY_1_TRANS_IN = "trade.category.1.trans.in";
	/** 资金切换转出 */
	String TRADE_TWO_CATEGORY_1_TRANS_OUT = "trade.category.1.trans.out";
	/** 支付方式 */
	String TRADE_TWO_CATEGORY_2_PAY_TYPE = "trade.category.2.pay.type";
	/** 提现解冻 */
	String TRADE_TWO_CATEGORY_3_CASH_UNFREEZE = "trade.category.3.cash.unfreeze";
	/** 提现拒绝 */
	String TRADE_TWO_CATEGORY_3_CASH_REJECT = "trade.category.3.cash.reject";
	/** 提现成功 */
	String TRADE_TWO_CATEGORY_3_CASH_SUCCESS = "trade.category.3.cash.success";
	/** 出款中 */
	String TRADE_TWO_CATEGORY_3_CASH_APPLY = "trade.category.3.cash.apply";
	/** 提现失败 */
	String TRADE_TWO_CATEGORY_3_CASH_FAILED = "trade.category.3.cash.failed";
	/** 提现失败-退回 */
	String TRADE_TWO_CATEGORY_3_CASH_FAILED_REFUND = "trade.category.3.cash.failed.refund";
	/** 银商充值 */
	String TRADE_TWO_CATEGORY_4_MERCHANT_RECHARGE = "trade.category.4.merchant.recharge";
	/** 银商加款 */
	String TRADE_TWO_CATEGORY_4_MERCHANT_ADDITION = "trade.category.4.merchant.addition";
	/** 银商扣款 */
	String TRADE_TWO_CATEGORY_4_MERCHANT_DEDUCTION = "trade.category.4.merchant.deduction";
	/** 转账给他人 */
	String TRADE_TWO_CATEGORY_4_MERCHANT_TRANSFER = "trade.category.4.merchant.transfer";
	/** 银商赠送会员余额 */
	String TRADE_TWO_CATEGORY_4_MERCHANT_GIFT_AMOUNT = "trade.category.4.merchant.gift.amount";
	/** 俱乐部保证金上分 */
	String TRADE_TWO_CATEGORY_5_CLUB_DEPOSIT_INCREASES = "trade.category.5.club.deposit.increases";
	/** 俱乐部保证金转出 */
	String TRADE_TWO_CATEGORY_5_CLUB_DEPOSIT_TRANSFER = "trade.category.5.club.deposit.transfer";
	/** 扣除超额盈利 */
	String TRADE_TWO_CATEGORY_5_DEDUCTION_PROFIT = "trade.category.5.deduction.profit";
	/** 奖励手动加款 */
	String TRADE_TWO_CATEGORY_5_REWARD_MANUAL_ADD = "trade.category.5.reward.manual.add";
	/** 修正负数余额 */
	String TRADE_TWO_CATEGORY_5_ADJUST_NEGATIVE_BALANCE = "trade.category.5.adjust.negative.balance";
	/** 扣除全部资产 */
	String TRADE_TWO_CATEGORY_5_DEDUCT_ALL_ASSETS = "trade.category.5.deduct.all.assets";
	/** 追缴扣除 */
	String TRADE_TWO_CATEGORY_5_RECOVER_DEDUCTION = "trade.category.5.recover.deduction";
	/** 手动补单 */
	String TRADE_TWO_CATEGORY_5_MANUAL_REFILL = "trade.category.5.manual.refill";
	/** 手动扣款 */
	String TRADE_TWO_CATEGORY_5_MANUAL_DEDUCTION = "trade.category.5.manual.deduction";
	/** 返还资金 */
	String TRADE_TWO_CATEGORY_5_RETURN_FUNDS = "trade.category.5.return.funds";
	/** 人工加款 */
	String TRADE_TWO_CATEGORY_5_MANUAL_ADDITION = "trade.category.5.manual.addition";
	/** 异常紧急修正 */
	String TRADE_TWO_CATEGORY_5_ABNORMAL_CORRECTION = "trade.category.5.abnormal.correction";
	/** 奖励异常紧急修正 */
	String TRADE_TWO_CATEGORY_5_REWARD_ABNORMAL_CORRECTION = "trade.category.5.reward.abnormal.correction";
	/** 奖励手动扣款 */
	String TRADE_TWO_CATEGORY_5_MANUAL_DEDUCTION_REWARDS = "trade.category.5.manual.deduction.rewards";
	/** 初始金额 */
	String TRADE_TWO_CATEGORY_5_INITIAL_AMOUNT = "trade.category.5.initial.amount";
	/** 更新金额 */
	String TRADE_TWO_CATEGORY_5_UPDATE_AMOUNT = "trade.category.5.update.amount";
	/** 排行榜活动 */
	String TRADE_TWO_CATEGORY_6_RANKING = "trade.category.6.ranking";
	/** 渠道奖励 */
	String TRADE_TWO_CATEGORY_6_CHANNEL_REWARD = "trade.category.6.channel.reward";
	/** 投资扣款 */
	String TRADE_TWO_CATEGORY_6_INVESTMENT_DEDUCTION = "trade.category.6.investment.deduction";
	/** 砍一刀 */
	String TRADE_TWO_CATEGORY_6_LUCKY_DRAW = "trade.category.6.lucky.draw";
	/** 集字活动 */
	String TRADE_TWO_CATEGORY_6_COLLECT_WORD = "trade.category.6.collect.word";
	/** 竞猜活动 */
	String TRADE_TWO_CATEGORY_6_PREDICTION = "trade.category.6.prediction";
	/** 新人彩金 */
	String TRADE_TWO_CATEGORY_6_NEW_PLAYER_BONUS = "trade.category.6.new.player.bonus";
	/** 签到活动 */
	String TRADE_TWO_CATEGORY_6_SIGN_IN = "trade.category.6.sign.in";
	/** 转盘活动 */
	String TRADE_TWO_CATEGORY_6_TURNTABLE = "trade.category.6.turntable";
	/** 红包活动 */
	String TRADE_TWO_CATEGORY_6_RED_ENVELOPE = "trade.category.6.red.envelope";
	/** 救援金活动 */
	String TRADE_TWO_CATEGORY_6_SUPPORT_BONUS = "trade.category.6.support.bonus";
	/** 充值活动 */
	String TRADE_TWO_CATEGORY_6_DEPOSIT = "trade.category.6.deposit";
	/** 代理活动 */
	String TRADE_TWO_CATEGORY_6_AGENCY = "trade.category.6.agency";
	/** 打码活动 */
	String TRADE_TWO_CATEGORY_6_BET = "trade.category.6.bet";
	/** 幸运注单活动 */
	String TRADE_TWO_CATEGORY_6_LUCKY_BET = "trade.category.6.lucky.bet";
	/** 推广活动 */
	String TRADE_TWO_CATEGORY_6_PROMOTION = "trade.category.6.promotion";
	/** 投资活动 */
	String TRADE_TWO_CATEGORY_6_INVESTMENT = "trade.category.6.investment";
	/** 自定义活动 */
	String TRADE_TWO_CATEGORY_6_CUSTOM = "trade.category.6.custom";
	/** 抽奖助力活动 */
	String TRADE_TWO_CATEGORY_6_LUCKY_DRAW_SUPPORT = "trade.category.6.lucky.draw.support";
	/** 新手救援金 */
	String TRADE_TWO_CATEGORY_6_NEWBIE_RELIEF_FUND = "trade.category.6.newbie.relief.fund";
	/** 新砍一刀 */
	String TRADE_TWO_CATEGORY_6_NEW_CUT = "trade.category.6.new.cut";
	/** 闯关邀请奖励 */
	String TRADE_TWO_CATEGORY_6_PASSING_LEVELS_BONUS = "trade.category.6.passing.levels.bonus";
	/** 闯关邀请额外奖励 */
	String TRADE_TWO_CATEGORY_6_PASSING_LEVELS_EXTRA_BONUS = "trade.category.6.passing.levels.extra.bonus";
	/** 每日转盘奖励 */
	String TRADE_TWO_CATEGORY_6_DAILY_TURNTABLE = "trade.category.6.daily.turntable";
	/** 拼团奖励 */
	String TRADE_TWO_CATEGORY_6_GROUP_BOOKING = "trade.category.6.group.booking";
	/** 返水领取 */
	String TRADE_TWO_CATEGORY_7_REBATE_RECEIVE = "trade.category.7.rebate.receive";
	/** 发放佣金 */
	String TRADE_TWO_CATEGORY_8_COMMISSION_ISSUE = "trade.category.8.commission.issue";
	/** 领取佣金 */
	String TRADE_TWO_CATEGORY_8_COMMISSION_RECEIVE = "trade.category.8.commission.receive";
	/** 手动拉回-利息宝 */
	String TRADE_TWO_CATEGORY_9_MANUAL_PULL_BACK = "trade.category.9.manual.pull.back";
	/** 大厅转入利息宝 */
	String TRADE_TWO_CATEGORY_9_INCOME_TRANS_IN = "trade.category.9.income.trans.in";
	/** 利息宝转到大厅 */
	String TRADE_TWO_CATEGORY_9_INCOME_TRANS_OUT = "trade.category.9.income.trans.out";
	/** 利息宝收益 */
	String TRADE_TWO_CATEGORY_9_INCOME_REWARD = "trade.category.9.income.reward";
	/** 新人福利 */
	String TRADE_TWO_CATEGORY_10_NEWPLAYER = "trade.category.10.newplayer";
	/** 每日任务 */
	String TRADE_TWO_CATEGORY_10_DAILY_TASK = "trade.category.10.daily.task";
	/** 每周任务 */
	String TRADE_TWO_CATEGORY_10_WEEKLY_TASK = "trade.category.10.weekly.task";
	/** 活跃度宝箱 */
	String TRADE_TWO_CATEGORY_10_ACTIVITY_BOX = "trade.category.10.activity.box";
	/** 神秘任务 */
	String TRADE_TWO_CATEGORY_10_MYSTERIOUS_TASK = "trade.category.10.mysterious.task";
	/** VIP晋级奖金 */
	String TRADE_TWO_CATEGORY_11_VIP_AFFILIATE_BONUS = "trade.category.11.vip.affiliate.bonus";
	/** VIP周奖金 */
	String TRADE_TWO_CATEGORY_11_VIP_WEEKLY_BONUS = "trade.category.11.vip.weekly.bonus";
	/** VIP月奖金 */
	String TRADE_TWO_CATEGORY_11_VIP_MONTHLY_BONUS = "trade.category.11.vip.monthly.bonus";
	/** VIP日奖励 */
	String TRADE_TWO_CATEGORY_11_VIP_DAILY_BONUS = "trade.category.11.vip.daily.bonus";
	/** 生日礼金 */
	String TRADE_TWO_CATEGORY_11_BIRTHDAY_GIFT = "trade.category.11.birthday.gift";
	/** 充值优惠 */
	String TRADE_TWO_CATEGORY_12_OFFERS = "trade.category.12.offers";
	/** 放弃奖励 */
	String TRADE_TWO_CATEGORY_13_GIVE_UP_REWARD = "trade.category.13.give.up.reward";
	/** 转出 */
	String TRADE_TWO_CATEGORY_13_TRANSFER_OUT = "trade.category.13.transfer.out";
	/** 转入 */
	String TRADE_TWO_CATEGORY_13_TRANSFER_IN = "trade.category.13.transfer.in";
	/** 理赔冻结 */
	String TRADE_TWO_CATEGORY_14_CLAIMS_FREEZE = "trade.category.14.claims.freeze";
	/** 理赔解冻 */
	String TRADE_TWO_CATEGORY_14_CLAIMS_UNFREEZING = "trade.category.14.claims.unfreezing";
	/** 理赔手续费 */
	String TRADE_TWO_CATEGORY_14_CLAIMS_FEE = "trade.category.14.claims.fee";
	/** 理赔上分 */
	String TRADE_TWO_CATEGORY_14_CLAIMS_SCORE_HIGHER = "trade.category.14.claims.score.higher";
	/** 理赔下分 */
	String TRADE_TWO_CATEGORY_14_CLAIMS_SCORE_LOWER = "trade.category.14.claims.score.lower";
	/** 理赔清空资产 */
	String TRADE_TWO_CATEGORY_14_CLAIMS_CLEAR = "trade.category.14.claims.clear";
	/** 退还代理佣金 */
	String TRADE_TWO_CATEGORY_15_RETURN_AGENT_DEPOSIT = "trade.category.15.return.agent.deposit";
	/** 转出给下级 */
	String TRADE_TWO_CATEGORY_15_TRANSFER_TO_SUBORDINATES = "trade.category.15.transfer.to.subordinates";
	/** 由上级转入 */
	String TRADE_TWO_CATEGORY_15_TRANSFERRED_FROM_SUPERIOR = "trade.category.15.transferred.from.superior";
	/** 信用借款-手动还款 */
	String TRADE_TWO_CATEGORY_16_MANUAL_REPAYMENT = "trade.category.16.manual.repayment";
	/** 信用借款-借款 */
	String TRADE_TWO_CATEGORY_16_BORROWING = "trade.category.16.borrowing";
	/** 信用借款-强制还款 */
	String TRADE_TWO_CATEGORY_16_MANDATORY_REPAYMENT = "trade.category.16.mandatory.repayment";
	/** 信用借款-到期扣款 */
	String TRADE_TWO_CATEGORY_16_DUE_PAYMENT = "trade.category.16.due.payment";
	/** 信用借款-系统追缴 */
	String TRADE_TWO_CATEGORY_16_SYSTEM_RECOVERY = "trade.category.16.system.recovery";
	/** 转入三方 */
	String TRADE_TWO_CATEGORY_17_TRANS_IN_MARGIN = "trade.category.17.trans.in.margin";
	/** 转出三方 */
	String TRADE_TWO_CATEGORY_17_TRANS_OUT_MARGIN = "trade.category.17.trans.out.margin";
	/** 账单占用 */
	String TRADE_TWO_CATEGORY_17_BILL_OCCUPANCY = "trade.category.17.bill.occupancy";
	/** 账单占用退还 */
	String TRADE_TWO_CATEGORY_17_BILL_OCCUPANCY_REFUND = "trade.category.17.bill.occupancy.refund";
	/** 保证金充值 */
	String TRADE_TWO_CATEGORY_17_FUND_DEPOSIT = "trade.category.17.fund.deposit";
	/** 保证金转出 */
	String TRADE_TWO_CATEGORY_17_FUND_TRANS_OUT = "trade.category.17.fund.trans.out";
	/** 取消转出 */
	String TRADE_TWO_CATEGORY_17_CANCEL_TRANS = "trade.category.17.cancel.trans";
	/** 转出被拒绝 */
	String TRADE_TWO_CATEGORY_17_TRANS_REJECTED = "trade.category.17.trans.rejected";
	/** 增加保证金 */
	String TRADE_TWO_CATEGORY_17_INCREASE_FUND = "trade.category.17.increase.fund";
	/** 扣除保证金 */
	String TRADE_TWO_CATEGORY_17_DEDUCT_FUND = "trade.category.17.deduct.fund";
	/** 强制结算 */
	String TRADE_TWO_CATEGORY_17_FORCED_SETTLE = "trade.category.17.forced.settle";
	/** 强制提前结算 */
	String TRADE_TWO_CATEGORY_17_FORCE_EARLY_SETTLE = "trade.category.17.force.early.settle";
	/** 提前结算 */
	String TRADE_TWO_CATEGORY_17_EARLY_SETTLE = "trade.category.17.early.settle";
	/** 俱乐部结算 */
	String TRADE_TWO_CATEGORY_17_CLUB_SETTLE = "trade.category.17.club.settle";
	/** 生成iOSAPP */
	String TRADE_TWO_CATEGORY_17_GENERATE_IOS_APP = "trade.category.17.generate.ios.app";
	/** AndroidAPP */
	String TRADE_TWO_CATEGORY_17_GENERATE_ANDROID_APP = "trade.category.17.generate.android.app";
	/** 保证金转入 */
	String TRADE_TWO_CATEGORY_17_MARGIN_TRANS = "trade.category.17.margin.trans";
	/** 转入保证金 */
	String TRADE_TWO_CATEGORY_18_TRANS_IN_FUND = "trade.category.18.trans.in.fund";
	/** 转出保证金 */
	String TRADE_TWO_CATEGORY_18_TRANS_OUT_FUND = "trade.category.18.trans.out.fund";
	/** 人工上分 */
	String TRADE_TWO_CATEGORY_18_MANUAL_SCORE_INCREASE = "trade.category.18.manual.score.increase";
	/** 人工下分 */
	String TRADE_TWO_CATEGORY_18_MANUAL_SCORE_DECREASE = "trade.category.18.manual.score.decrease";
	/** 成员借款 */
	String TRADE_TWO_CATEGORY_18_MEMBER_BORROW = "trade.category.18.member.borrow";
	/** 借款额度调整 */
	String TRADE_TWO_CATEGORY_18_REMAIN_LIMIT = "trade.category.18.remain.limit";
	/** 强制还款 */
	String TRADE_TWO_CATEGORY_18_FORCE_REPAYMENT = "trade.category.18.force.repayment";
	/** 主动还款 */
	String TRADE_TWO_CATEGORY_18_ACTIVE_REPAYMENT = "trade.category.18.active.repayment";
	/** 有奖反馈 */
	String TRADE_TWO_CATEGORY_18_REWARD_FEEDBACK = "trade.category.18.reward.feedback";
	/** 成员存款 */
	String TRADE_TWO_CATEGORY_18_MEMBER_DEPOSIT = "trade.category.18.member.deposit";
	/** 成员取款 */
	String TRADE_TWO_CATEGORY_18_MEMBER_WITHDRAWAL = "trade.category.18.member.withdrawal";
	/** 余额归零 */
	String TRADE_TWO_CATEGORY_18_BALANCE_RESET = "trade.category.18.balance.reset";
	/** 白银转盘 */
	String TRADE_TWO_CATEGORY_19_SILVER_TURNTABLE = "trade.category.19.silver.turntable";
	/** 黄金转盘 */
	String TRADE_TWO_CATEGORY_19_GOLDEN_TURNTABLE = "trade.category.19.golden.turntable";
	/** 钻石转盘 */
	String TRADE_TWO_CATEGORY_19_DIAMOND_TURNTABLE = "trade.category.19.diamond.turntable";
	/** 公积金领取 */
	String TRADE_TWO_CATEGORY_20_DEPOSIT_REWARD = "trade.category.20.deposit.reward";
	/** 盲盒抽奖扣除 */
	String TRADE_TWO_CATEGORY_21_BLIND_BOX_DEDUCTION = "trade.category.21.blind.box.deduction";
	/** 盲盒抽奖奖励 */
	String TRADE_TWO_CATEGORY_21_BLIND_BOX_REWARD = "trade.category.21.blind.box.reward";
	/** 缴纳代理押金 */
	String TRADE_TWO_CATEGORY_22_PAY_AGENT_DEPOSIT = "trade.category.22.pay.agent.deposit";
	/** 打赏消费 */
	String TRADE_TWO_CATEGORY_23_REWARD_CONSUMPTION = "trade.category.23.reward.consumption";
	/** 收到打赏 */
	String TRADE_TWO_CATEGORY_23_RECEIVED_REWARDS = "trade.category.23.received.rewards";

	/** 不限制 */
	String CASH_NOT_LIMIT = "cash.not.limit";

}