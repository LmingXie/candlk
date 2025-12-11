package com.bojiu.context.model;

public interface BaseI18nKey {

	/** sign不能为空！ */
	String SECURITY_SIGN_REQUIRED = "@security.sign.required";
	/** 非法请求 */
	String ILLEGAL_REQUEST = "@illegal.request";
	/** 非法请求，请刷新重试！ */
	String ILLEGAL_REQUEST_REFRESH_RETRY = "@illegal.request.refresh.retry";
	/** 你没有权限进行此操作! */
	String PERMISSION_DENIED = "@permission.denied";
	/** 你无法进行此操作! */
	String UNSUPPORTED_OPERATIONS = "unsupported.operations";
	/** 网络异常，请稍后重试 */
	String NETWORK_ABORT = "network.abort";
	/** 您的操作过于频繁，请稍后再试！ */
	String REQUEST_TOO_FAST = "request.too.fast";
	/** 参数错误，请确认! */
	String PARAMETER_ERROR = "parameter.error";
	/** 未找到页面! */
	String PAGE_NOT_FOUND = "page.not.found";
	/** 系统已记录故障。根据界面提示进行操作。 */
	String SYSTEM_DEFAULT_FAULT_TIP = "system.default.fault.tip";
	/** 系统错误，请稍后再试! */
	String SYSTEM_ERROR = "system.error";
	/** 数据已变更，请刷新再试! */
	String DATA_CHANGED = "data.changed";
	/** 不支持修改，请刷新! */
	String LIMIT_CHANGED = "limit.changed";
	/** 配置已变更，请刷新! */
	String CONFIG_CHANGED = "config.changed";
	/** 验证未通过或已超时，请重新操作！ */
	String VERIFY_TIME_OUT = "verify.time.out";
	/** 系统繁忙，请稍后再试! */
	String SYSTEM_BUSY = "system.busy";
	/** 您输入的内容过长！ */
	String INPUT_TOO_LONG = "input.too.long";
	/** 您输入的数值过大！ */
	String INPUT_OUT_OF_RANGE = "input.out.of.range";
	/** 您的输入包含不受支持的特殊字符，请删除后再试！ */
	String INPUT_UNSUPPORTED = "input.unsupported";
	/** 由于您{0}错误次数过多，一段时间内将无法再次操作！ */
	String LOGIN_ERROR_TIMES_LIMIT_INPUT = "login.error.times.limit.input";
	// operate
	/** 操作成功！ */
	String OPERATE_SUCCESS = "operate.success";
	/** 操作成功，次日生效！ */
	String OPERATE_DELAY_SUCCESS = "operate.delay.success";
	/** 设置成功！ */
	String SET_SUCCESS = "set.success";
	/** 操作失败！ */
	String OPERATE_FAIL = "operate.fail";
	/** 该操作被限制，请联系客服！ */
	String OPS_LIMITED = "ops.limited";
	/** 全部 */
	String ALL = "@all";
	// TimeType
	/** 今天 */
	String TIME_TYPE_TODAY = "time.type.today";
	/** 昨天 */
	String TIME_TYPE_YESTERDAY = "time.type.yesterday";
	/** 7天 */
	String TIME_TYPE_DAYS_7 = "time.type.days.7";
	/** 15天 */
	String TIME_TYPE_DAYS_15 = "time.type.days.15";
	/** 30天 */
	String TIME_TYPE_DAYS_30 = "time.type.days.30";
	/** 本周 */
	String TIME_TYPE_THIS_WEEK = "time.type.days.this_week";
	/** 上周 */
	String TIME_TYPE_LAST_WEEK = "time.type.days.last_week";
	/** 本月 */
	String TIME_TYPE_THIS_MONTH = "time.type.days.this_month";
	/** 上月 */
	String TIME_TYPE_LAST_MONTH = "time.type.days.last_month";
	/** 余额不足 */
	String NOT_SUFFICIENT_FUNDS = "not.sufficient.funds";
	/** 投诉经销商等待 */
	String DEALER_COMPLAIN_WAIT = "dealer.complain.wait";
	String DEALER_COMPLAIN_ACCEPT = "dealer.complain.accept";
	String DEALER_COMPLAIN_IGNORE = "dealer.complain.ignore";
	/** 已驳回 */
	String VERIFY_FALSE = "verify.false";
	/** 审核中 */
	String WAIT_VERIFY = "wait.verify";
	/** 已通过 */
	String VERIFY_TRUE = "verify.true";

	/** Google身份验证已成功启用 */
	String GOOGLE_SET_SUCCESS = "google.set.success";
	/** 请输入{0}的appid！ */
	String REGISTER_GOOGLE_APPID_REQUIRED = "register.google.appid.required";
	/** 请输入{0}的secret！ */
	String REGISTER_GOOGLE_SECRET_REQUIRED = "register.google.secret.required";
	/** 账号注册和手机号注册至少一个开启~ */
	String REGISTER_PHONE_OR_NORMAL_REQUIRED = "register.phone.or.normal.required";
	/** 账号登录和手机号登录至少一个开启~ */
	String LOGIN_PHONE_OR_NORMAL_REQUIRED = "login.phone.or.normal.required";
	/** 账号密码、手机验证码、手机号+密码只可选择一种 */
	String REGISTER_PHONE_OR_NORMAL_ONLY = "login.phone.or.normal.required";

	/** 不支持修改[RTP小于100]抽成 */
	String RTP_LESS_NOT_SUPPORT = "rtp.less.not.support";
	/** 不支持修改[RTP大于100]抽成 */
	String RTP_GREATER_NOT_SUPPORT = "rtp.greater.not.support";

	String NOW = "@now";

	/** 请输入有效的交易哈希！ */
	String CASH_TX_HASH_INVALID = "cash.tx.hash.invalid";
	/** 输入的转账记录与本次兑换申请无法匹配！ */
	String CASH_TX_HASH_MISMATCH = "cash.tx.hash.mismatch";
	/** 转账收款地址与本次兑换无法匹配！ */
	String CASH_TX_ADDRESS_MISMATCH = "cash.tx.address.mismatch";

	// 云厂商
	/** 亚马逊 */
	String SERVER_VENDOR_AWS = "server.vendor.aws";
	/** 微软云 */
	String SERVER_VENDOR_AZURE = "server.vendor.azure";

	// 商户VIP等级
	/** 1级 */
	String MERCHANT_VIP_LEVEL_1 = "merchant.vip.level.1";
	/** 2级 */
	String MERCHANT_VIP_LEVEL_2 = "merchant.vip.level.2";
	/** 3级 */
	String MERCHANT_VIP_LEVEL_3 = "merchant.vip.level.3";
	/** 4级 */
	String MERCHANT_VIP_LEVEL_4 = "merchant.vip.level.4";
	/** 5级 */
	String MERCHANT_VIP_LEVEL_5 = "merchant.vip.level.5";
	/** 6级 */
	String MERCHANT_VIP_LEVEL_6 = "merchant.vip.level.6";
	/** 7级 */
	String MERCHANT_VIP_LEVEL_7 = "merchant.vip.level.7";
	/** 8级 */
	String MERCHANT_VIP_LEVEL_8 = "merchant.vip.level.8";
	/** 9级 */
	String MERCHANT_VIP_LEVEL_9 = "merchant.vip.level.9";

	// 站点状态
	/** 注销 */
	String SITE_STATUS_CLOSE = "site.status.close";
	/** 冻结 */
	String SITE_STATUS_FROZEN = "site.status.frozen";
	/** 结账 */
	String SITE_STATUS_CLOSE_ACCOUNTS = "site.status.close.accounts";
	/** 维护 */
	String SITE_STATUS_MAINTAIN = "site.status.maintain";
	/** 建设中 */
	String SITE_STATUS_INIT = "site.status.init";
	/** 正常 */
	String SITE_STATUS_OK = "site.status.ok";

	// 奖励类型
	/** 次数 */
	String REWARD_TYPE_COUNT = "reward.type.count";
	/** 金币 */
	String REWARD_TYPE_GOLD = "reward.type.gold";
	/** 积分 */
	String REWARD_TYPE_INTEGRAL = "reward.type.integral";
	/** 额度 */
	String REWARD_TYPE_LIMIT = "reward.type.limit";

	// 稽核方式
	/** 稽核奖金 */
	String AUDIT_MODE_BONUS = "audit.mode.bonus";
	/** 稽核本金+奖金 */
	String AUDIT_MODE_CAPITAL_BONUS = "audit.mode.capital.bonus";
	/** 稽核余额+奖金 */
	String AUDIT_MODE_BALANCE_BONUS = "audit.mode.balance.bonus";

	// 奖金方式
	/** 固定 */
	String BONUS_TYPE_FIXED = "bonus.type.fixed";
	/** 随机 */
	String BONUS_TYPE_RANDOM = "bonus.type.random";
	/** 比例 */
	String BONUS_TYPE_RATIO = "bonus.type.ratio";
	/** 平均随机 */
	String BONUS_TYPE_AVG = "bonus.type.avg";

	// 循环方式
	/** 单次活动 */
	String CYCLE_MODE_SINGLE = "cycle.mode.single";
	/** 每日循环 */
	String CYCLE_MODE_DAILY_CYCLE = "cycle.mode.daily.cycle";
	/** 每周循环 */
	String CYCLE_MODE_WEEKLY_CYCLE = "cycle.mode.weekly.cycle";
	/** 每月循环 */
	String CYCLE_MODE_MONTHLY_CYCLE = "cycle.mode.monthly.cycle";
	/** 每季循环 */
	String CYCLE_MODE_QUARTERLY_CYCLE = "cycle.mode.quarterly.cycle";
	/** 每半年循环 */
	String CYCLE_MODE_SEMIYEARLY_CYCLE = "cycle.mode.semiyearly.cycle";
	/** 每小时循环 */
	String CYCLE_MODE_HOUR_CYCLE = "cycle.mode.hour.cycle";
	/** 每半年循环 */
	String CYCLE_MODE_YEARLY_CYCLE = "cycle.mode.yearly.cycle";
	/** 不重置(推荐) */
	String CYCLE_MODE_NOT_RESET = "cycle.mode.not.reset";
	// 派发方式
	/** 玩家自领-过期自动派发 */
	String DISPATCH_MODE_MANUAL_EXPIRED = "dispatch.mode.manual.expired";
	/** 玩家自领-过期作废 */
	String DISPATCH_MODE_MANUAL = "dispatch.mode.manual";
	/** 自动派发 */
	String DISPATCH_MODE_AUTO = "dispatch.mode.auto";
	/** 活动系统说明-奖励系统自动派发 */
	String REMARK_DISPATCH_MODE_AUTO = "remark.dispatch.mode.auto";
	/** 活动系统说明-奖励手动领取 */
	String REMARK_DISPATCH_MODE_MANUAL = "remark.dispatch.mode.manual";
	/** 玩家申请-人工派发 */
	String DISPATCH_MODE_REVIEW = "dispatch.mode.artificial";
	/** 次日领取 */
	String COLLECT_TIME_TYPE_NEXT_DAY = "collect.time.type.next.day";
	/** 实时领取（影响留存） */
	String COLLECT_TIME_TYPE_REAL_TIME = "collect.time.type.real.time";
	/** 每日 */
	String COLLECT_TIME_TYPE_EVERYDAY = "collect.time.type.everyday";
	/** 下周领取 */
	String COLLECT_TIME_TYPE_NEXT_WEEK = "collect.time.type.next.week";
	/** 每周领取 */
	String COLLECT_TIME_TYPE_WEEKLY = "collect.time.type.weekly";
	/** 下月领取 */
	String COLLECT_TIME_TYPE_NEXT_MONTH = "collect.time.type.next.month";
	/** 每月领取 */
	String COLLECT_TIME_TYPE_MONTHLY = "collect.time.type.monthly";
	// 活动状态
	/** 关闭 */
	String PROMOTION_STATUS_CLOSE = "promotion.status.close";
	/** 草稿 */
	String PROMOTION_STATUS_DRAFT = "promotion.status.draft";
	/** 待生效 */
	String PROMOTION_STATUS_TO_EFFECTIVE = "promotion.status.to.effective";
	/** 已生效 */
	String PROMOTION_STATUS_EFFECTIVE = "promotion.status.effective";
	/** 已结束 */
	String PROMOTION_STATUS_ENDED = "promotion.status.ended";

	// 风控状态
	/** 禁止 */
	String RISK_STATUS_PROHIBIT = "risk.status.prohibit";
	/** 限制 */
	String RISK_STATUS_LIMIT = "risk.status.limit";
	/** 预警 */
	String RISK_STATUS_WARN = "risk.status.warn";
	/** 正常 */
	String RISK_STATUS_OK = "risk.status.ok";
	// 风控提示词
	/** 当前站点余额不足，前台所有会员都无法进入三方游戏，请及时充U。 */
	String RISK_PROMPT_PROHIBIT_GAMES = "risk.prompt.prohibit.games";
	/** 当前站点余额不足，将无法导出会员资料和无法提现审核（含暂停自动出款）请及时充U。 */
	String RISK_PROMPT_BACKGROUND_LIMIT = "risk.prompt.background.limit";
	/** 当前站点余额不足，请及时充U。 */
	String RISK_PROMPT_WARN = "risk.prompt.warn";
	// 语言
	/** 英语 */
	String LANGUAGE_EN = "language.en";
	/** 中文 */
	String LANGUAGE_ZH = "language.zh";
	/** 葡萄牙语 */
	String LANGUAGE_MS = "language.ms";
	/** 马来语 */
	String LANGUAGE_PT = "language.pt";
	/** 印尼语 */
	String LANGUAGE_ID = "language.id";
	/** 菲律宾语 */
	String LANGUAGE_PH = "language.ph";
	/** 印地语 */
	String LANGUAGE_HI = "language.hi";
	/** 越南语 */
	String LANGUAGE_VI = "language.vi";
	/** 巴基斯坦-乌尔都语 */
	String LANGUAGE_UR = "language.ur";
	/** 孟加拉语 */
	String LANGUAGE_BN = "language.bn";
	/** 西班牙语 */
	String LANGUAGE_ES = "language.es";
	// 风控类型
	/** 商户余额 */
	String RISK_TYPE_MERCHANT_BALANCE = "risk.type.merchant.balance";
	/** 最大透支倍数 */
	String RISK_TYPE_MAX_OVERDRAFT = "risk.type.max.overdraft";
	/** 正常 */
	String RISK_TYPE_OK = "risk.type.ok";
	/** 预警 */
	String RISK_TYPE_WARN = "risk.type.warn";
	/** 限制 */
	String RISK_TYPE_BACKGROUND_LIMIT = "risk.type.background.limit";
	/** 禁止 */
	String RISK_TYPE_PROHIBIT_GAMES = "risk.type.prohibit.games";

	/** 小于{}设定值则对商户发送预警通知 */
	String RISK_TYPE_MERCHANT_BALANCE_DESC = "risk.type.merchant.balance.desc";
	/** 暂不使用 */
	String RISK_TYPE_MAX_OVERDRAFT_DESC = "risk.type.max.overdraft.desc";
	/** 额度已使用比例 ≤ {}%\n额度已使用比例 = (未结账单 + 未出账单) /(商户余额 + 授信额度) * 100% */
	String RISK_TYPE_OK_DESC = "risk.type.ok.desc";
	/** 额度已使用比例 > {}% 时，商户状态为预警 */
	String RISK_TYPE_WARN_DESC = "risk.type.warn.desc";
	/** 额度已使用比例 > {}% 时，商户状态为后台限制，将无法导出会员资料和无法提现审核（含暂停自动出款） */
	String RISK_TYPE_BACKGROUND_LIMIT_DESC = "risk.type.background.limit.desc";
	/** 额度已使用比例 > {}% 时，站点状态为禁止游戏，前台所有会员都无法进入三方游戏。 */
	String RISK_TYPE_PROHIBIT_GAMES_DESC = "risk.type.prohibit.games.desc";
	// 导出成功
	/** 导出任务提交成功，请前往导出列表下载结果！ */
	String EXPORT_SUCCESS = "export.success";

	// 投注状态
	/** 未结算 */
	String PLAY_STATUS_UNSETTLED = "play.status.unsettled";
	/** 已结算 */
	String PLAY_STATUS_SETTLED = "play.status.settled";
	/** 已撤单 */
	String PLAY_STATUS_CANCELLED = "play.status.cancelled";
	// 层级类型
	/** 自动层级 */
	String LEVEL_TYPE_AUTO = "level.type.auto";
	/** 固定层级 */
	String LEVEL_TYPE_FIXED = "level.type.fixed";

	// 经销商奖励类型
	/** 充值奖励 */
	String DEALER_REWARD_TYPE_RECHARGE = "dealer.reward.type.recharge";
	/** 额外充值奖励 */
	String DEALER_REWARD_TYPE_EXTRA_RECHARGE = "dealer.reward.type.extra.recharge";
	/** 阶梯奖励 */
	String DEALER_REWARD_TYPE_STEP = "dealer.reward.type.step";
	/** VIP晋级奖励 */
	String DEALER_REWARD_TYPE_VIP_PROMOTION = "dealer.reward.type.vip.promotion";
	// 经销商奖励状态
	/** 已发放 */
	String DEALER_REWARD_STATUS_RECEIVED = "dealer.reward.status.received";
	/** 待领取 */
	String DEALER_REWARD_STATUS_PENDING = "dealer.reward.status.pending";

	// 票券类型
	/** 现金兑换券 */
	String TICKET_TYPE_CASH_EXCHANGE = "ticket.type.cash.exchange";
	/** 幸运红包券 */
	String TICKET_TYPE_RED_PACKET = "ticket.type.red.packet";
	/** 砸金蛋抽奖券 */
	String TICKET_TYPE_GOLDEN_EGG = "ticket.type.golden.egg";
	/** 大转盘票券 */
	String TICKET_TYPE_TURNTABLE = "ticket.type.turntable";
	/** 充值票券 */
	String TICKET_TYPE_RECHARGE = "ticket.type.recharge";

	// 票券任务
	/** 存款金额 */
	String TICKET_TASK_RECHARGE_AMOUNT = "ticket.task.recharge.amount";
	/** 存款次数 */
	String TICKET_TASK_RECHARGE_COUNT = "ticket.task.recharge.count";
	/** 邀请注册 */
	String TICKET_TASK_INVITE_REGISTER = "ticket.task.invite.register";
	/** {0}游戏投注 */
	String TICKET_TASK_VALID_COIN = "ticket.task.valid.coin";
	/** 游戏总输金额 */
	String TICKET_TASK_LOSE_COIN = "ticket.task.lose.coin";
	/** 关联提款卡 */
	String TICKET_TASK_CASH_ACCOUNT = "ticket.task.cash.account";
	/** 绑定手机号 */
	String TICKET_TASK_BIND_PHONE = "ticket.task.bind.phone";

	// 票券来源类型
	/** APP下载 */
	String TICKET_ORIGIN_TYPE_APP = "ticket.origin.type.app";
	/** 活动奖励 */
	String TICKET_ORIGIN_TYPE_PROMOTION = "ticket.origin.type.promotion";
	/** 任务中心奖励 */
	String TICKET_ORIGIN_TYPE_TASK = "ticket.origin.type.task";
	/** 后台派发 */
	String TICKET_ORIGIN_TYPE_EMP = "ticket.origin.type.emp";

	// 票券状态
	/** 未生效 */
	String TICKET_STATUS_NOT_EFFECTIVE = "ticket.status.not.effective";
	/** 生效未使用 */
	String TICKET_STATUS_NOT_USED = "ticket.status.not.used";
	/** 已使用 */
	String TICKET_STATUS_USED = "ticket.status.used";
	/** 已过期 */
	String TICKET_STATUS_EXPIRED = "ticket.status.expired";
	/** 用户打码分享 */
	String SNS_SUM_COIN = "sns.template.sun.coin";
	/** 我在游戏中已经完成了{0}的打码，你也来加入吧！ */
	String SNS_CONTENT_SUM_COIN = "sns.template.content.sun.coin";
	/** 用户VIP分享 */
	String SNS_SUN_VIP = "sns.template.sun.vip";
	/** 我已经在平台中达到了VIP{0},享受超多福利！ */
	String SNS_CONTENT_VIP = "sns.template.content.vip";
	/** 用户登录分享 */
	String SNS_LOGIN_DAILY = "sns.template.login.daily";
	/** 我已经在平台中玩了{0}天！体验超棒，一起加入吧！ */
	String SNS_CONTENT_LOGIN_DAILY = "sns.template.content.login.daily";
	/** 用户大奖分享 */
	String SNS_MAX_BIG_WIN = "sns.template.max.big.win";
	/** 我在平台中一次爆到了{0}的奖金！赚翻了！你也来试试！ */
	String SNS_CONTENT_MAX_BIG_WIN = "sns.template.content.max.big.win";
	/** 用户优惠分享 */
	String SNS_SUM_REWARD = "sns.template.sum.reward";
	/** 我在平台中领取了高达{0}的优惠奖金！大家一起来领奖吧！ */
	String SNS_CONTENT_SUM_REWARD = "sns.template.content.sum.reward";
	/** 活动站点已使用 */
	String SITE_STATUS_USED = "site.status.used";
	/** 活动站点未使用 */
	String SITE_STATUS_NOT_USED = "site.status.not.used";
	/** 每邀请1人{0}%概率掉绑 */
	String BIND_BASED_ON_PROBABILITY = "bind.based.on.probability";
	/** 邀请第{0}人掉绑，之后每第{1}人掉绑 */
	String FIX_AND_REMOVE_THE_BINDING = "fix.and.remove.the.binding";
	/** 掉绑方式：概率 */
	String MISSING_BINDING_TYPE_ODDS = "missing.binding.type.odds";
	/** 掉绑方式：固定 */
	String MISSING_BINDING_TYPE_FIX = "missing.binding.type.fix";

	/** 正常 */
	String COMMON_STATUS_NORMAL = "common.status.normal";
	/** 冻结 */
	String COMMON_STATUS_FREEZE = "common.status.freeze";

	/** 游戏问题 */
	String FEEDBACK_TYPE_GAME = "feedback.type.game";
	/** 登录问题 */
	String FEEDBACK_TYPE_LOGIN = "feedback.type.login";
	/** 活动问题 */
	String FEEDBACK_TYPE_PROMOTION = "feedback.type.promotion";
	/** 代理问题 */
	String FEEDBACK_TYPE_AGENT = "feedback.type.agent";
	/** 充值问题 */
	String FEEDBACK_TYPE_RECHARGE = "feedback.type.recharge";
	/** 提现问题 */
	String FEEDBACK_TYPE_CASH = "feedback.type.cash";
	/** 优化建议 */
	String FEEDBACK_TYPE_OPTIMIZATION = "feedback.type.optimization";
	/** 其他建议 */
	String FEEDBACK_TYPE_OTHER = "feedback.type.other";

	/** 当前页面已存在配置 */
	String PAGE_CONFIG_EXIST = "page.config.exist";

	/** 您的账号尚未绑定{0}！ */
	String ACCOUNT_NOT_YET_BOUND = "account.not.yet.bound";
	/** 收款账户 */
	String FIELD_USER_RECHARGE_TO_ACCOUNT = "field.user.recharge.to.account";
	/** 手机号码 */
	String FIELD_USER_MOBILE_NUMBER = "field.user.mobile.number";
	/** 姓名 */
	String FIELD_USER_REAL_NAME = "field.user.real.name";
	/** 邮箱 */
	String FIELD_USER_EMAIL = "field.user.email";
	/** 快捷跳转 */
	String QUICK_JUMP = "@quick.jump";
	/** 牌照合规 */
	String LICENSE_COMPLIANCE = "@license.compliance";
	/** 联系我们 */
	String CONTACT_US = "@contact.us";
	/** 公司信息 */
	String COMPANY_INFO = "@company.info";
	/** 合作方信息 */
	String PARTNER_INFO = "@partner.info";
	/** 版权信息 */
	String COPYRIGHT_INFO = "@copyright.info";
	/** 官方频道 */
	String OFFICIAL_CHANNEL = "@official.channel";

	/** 请明天再来! */
	String PLEASE_COME_AGAIN_TOMORROW = "please.come.again.tomorrow";

	/** {0}不能为空！ */
	String REQUIRED = "@validate.required";
	/** {0}必须大于{1}！ */
	String RANGE_GT_MIN_INVALID = "@validate.range.gt.invalid";
	/** 当前幸运值不足，去赚幸运值 */
	String NOT_ENOUGH_EXPEND = "not.enough.expend";

	/** 每日 */
	String DAILY = "daily";
	/** 每周 */
	String WEEKLY = "weekly";
	/** 每月 */
	String MONTHLY = "monthly";
	/** 投注 */
	String PLAY = "play";
	/** 盈利 */
	String PROFIT = "profit";
	/** 充值 */
	String RECHARGE = "recharge";
	/** 累计充值 */
	String ACCUMULATED_RECHARGE = "accumulated.recharge";
	/** 有效投注 */
	String EFFECTIVE_BET_AMOUNT = "effective.bet.amount";
	/** 盈利额度 */
	String CUMULATIVE_PROFIT_AMOUNT = "cumulative.profit.amount";
	/** 充值额度 */
	String CUMULATIVE_RECHARGE_AMOUNT = "cumulative.recharge.amount";
	/** 次日 */
	String NEXT_DAY = "next.day";

	/** 排行榜-活动说明-系统模板 */
	String RANK_REMARK_SYSTEM_TEMPLATE = "rank.remark.system.template";
	/** ，最低充值金额需≥ */
	String RANK_REMARK_MIN_RECHARGE = "rank.remark.min.recharge";
	/** 每日{0}之间 */
	String RANK_REMARK_DAILY_BETWEEN = "rank.remark.daily.between";
	/** 自动派发到账 */
	String RANK_REMARK_AUTOMATIC_DELIVERY = "rank.remark.automatic.delivery";
	/** 作废 */
	String RANK_REMARK_INVALID = "rank.remark.invalid";

	/** 幸运转盘-活动说明-系统模板-幸运值获取方式 */
	String TURNTABLE_REMARK_SYSTEM_TEMPLATE_PRE = "turntable.remark.system.template.pre";
	/** 幸运转盘-活动说明-系统模板 */
	String TURNTABLE_REMARK_SYSTEM_TEMPLATE = "turntable.remark.system.template";
	/** 有效投注 */
	String TURNTABLE_REMARK_PLAY = "turntable.remark.play";
	/** 不限制游戏平台 */
	String TURNTABLE_REMARK_ALL_GAME = "turntable.remark.all.game";
	/** 不限制充值方式<br> */
	String TURNTABLE_REMARK_RECHARGE_LIMIT = "turntable.remark.recharge.limit";
	/** {0}抽奖一次需消耗{1}幸运值; */
	String TURNTABLE_REMARK_TURNTABLE_DESC = "turntable.remark.turntable.desc";
	/** ,同时需要已完成绑定{0}才可参与 */
	String TURNTABLE_REMARK_BIND_LIMIT = "turntable.remark.bind.limit";

}