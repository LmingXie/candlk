package com.bojiu.context.i18n;

import com.bojiu.context.model.BaseI18nKey;

public interface UserI18nKey extends BaseI18nKey {

	/** Your verification code is: ${code}！ */
	String SMS_TEMPLATE = "sms.template";
	/** 用户名必须是4~16位字符，必须包含字母，且只允许字母、数字和下划线！ */
	String USERNAME_FORMAT = "username.format";
	/** 密码必须是6~20个字符，支持字母/数字/符号！ */
	String PASSWORD_FORMAT = "password.format";
	/** 兑换密码格式不正确！ */
	String EXCHANGE_PWD_FORMAT_ERROR = "exchange.pwd.format.error";
	/** 短信服务异常，请联系官方客服！ */
	String SMS_CLOSED = "sms.closed";
	/** 短信发送成功，请注意查收！ */
	String SMS_SEND_SUCCESS = "sms.send.success";
	/** 发送失败，请稍后尝试~ */
	String SMS_SEND_FAIL = "sms.send.fail";
	/** 短信验证已达上限，请{0}小时后再试~ */
	String SMS_SEND_MAX_LIMIT = "sms.send.max.limit";
	/** 手机号码格式不正确，请重新输入！ */
	String PHONE_FORMAT_ERROR = "phone.format.error";
	/** 该账户异常，无法进行相关操作！ */
	String USER_AUTO_LOGIN_FROZEN = "user.auto.login.frozen";
	/** 您尚未登录或登录已失效，请重新登录！ */
	String USER_AUTO_LOGIN_INVALID = "user.auto.login.invalid";
	/** 权限变更，请重新登录！ */
	String PERMISSION_CHANGE_LOGIN_INVALID = "permission.change.login.invalid";
	/** 验证码不正确，请重新获取验证码！ */
	String CAPTCHA_ERROR_REDO = "captcha.error.redo";
	/** Google身份验证码错误！ */
	String GOOGLE_CODE_ERROR_REDO = "google.code.error.redo";
	/** 验证码不正确，请重新输入！ */
	String CAPTCHA_ERROR = "captcha.error";
	/** 您输入有误，请重新输入！ */
	String INPUT_ERROR = "input.error";
	/** 格式不正确！ */
	String FORMAT_ERROR = "format.error";
	/** 新登录密码不能与当前登录密码相同！ */
	String CHANGE_PWD_SAME = "change.pwd.same";
	/** 密码不正确，请重新输入！ */
	String PWD_ERROR = "pwd.error";
	/** 该用户不存在！ */
	String USER_404 = "user.404";
	/** 账号未注册！ */
	String USER_NOT_REGISTERED = "user.not.registered";
	/** 账号未绑定手机号，请联系客服 */
	String USER_BING_PHONE_REQUIRED = "user.bing.phone.required";
	/** 手机号未验证，无法设置忘记密码，请联系客服 */
	String USER_PHONE_VALIDA_REQUIRED = "user.phone.valida.required";
	/** 账号与手机号不一致无法修改，请联系客服 */
	String USER_ACCOUNT_NE_PHONE_VALIDA = "user.account.ne.phone.valida";
	/** 检测到账户异常，登录失败，如有疑问请联系客服！ */
	String USER_FROZEN = "user.frozen";
	/** 您的账号涉嫌违规已被冻结，冻结期间无法登陆！ */
	String MERCHANT_FROZEN = "merchant.frozen";
	/** 标签不能超过{0}个 */
	String USER_TAG_LIMIT = "user.tag.limit";
	/** 您的账号已绑定手机号码！ */
	String CHANGE_PHONE_IS_PHONED = "change.phone.is.phoned";

	/** 无法识别的文件业务类型：{0}！ */
	String FILE_BIZ_TYPE_UNKNOWN = "file.biz.type.unknown";
	/** 奖励已领取 */
	String USER_REWARD_RECEIVED = "user.reward.received";
	/** 一个账号只能领取一次 */
	String ONLY_CLAIM_REWARD = "only.claim.reward";
	/** 您的可抢红包数量上限已达到了 */
	String USER_REWARD_NONE = "user.reward.none";
	/** 奖励未发放 */
	String REWARD_NOT_GRANT = "reward.not.grant";
	/** 兑换密码不正确 */
	String EXCHANGE_PWD_ERROR = "exchange.pwd.error";
	/** 您已设置过，请刷新核实 */
	String EXCHANGE_FLUSH_CHECK = "exchange.flush.check";
	/** 活动已关闭 */
	String PROMOTION_CLOSED = "promotion.closed";
	/** 活动不存在 */
	String PROMOTION_NOT_EXIST = "promotion.not.exist";
	/** 活动未开始 */
	String PROMOTION_NON_START = "promotion.non.start";
	/** 充值优惠活动固定奖励描述 */
	String PROMOTION_RECHARGE_FIX_REWARD = "promotion.recharge.fix.reward";
	/** 充值优惠活动按比例奖励描述 */
	String PROMOTION_RECHARGE_RATIO_REWARD = "promotion.recharge.ratio.reward";
	/** 打码优惠活动固定奖励描述 */
	String PROMOTION_PLAY_FIX_REWARD = "promotion.play.fix.reward";
	/** 打码优惠活动按比例奖励描述 */
	String PROMOTION_PLAY_RATIO_REWARD = "promotion.play.ratio.reward";
	/** 兑换码过期 */
	String PROMOTION_REDEEM_CODE_EXPIRE = "promotion.redeem.code.expire";
	/** 兑换码已使用 */
	String PROMOTION_REDEEM_CODE_USED = "promotion.redeem.code.used";
	/** 请先充值 */
	String PLEASE_RECHARGE_FIRST = "please.recharge.first";
	/** 奖励已过期 */
	String USER_REWARD_EXPIRE = "user.reward.expire";
	/** 无效签到 */
	String INVALID_SIGN = "invalid.sign";
	/** 站点维护中 */
	String SITE_MAINTAIN = "site.maintain";
	/** 站点{0}，请联系客服 */
	String SITE_STATUS = "site.status";
	/** 站点建设中 */
	String SITE_INIT = "site.init";
	/** 您存在异常行为操作 */
	String BLOCK_LIMIT_OPERATE = "block.limit.operate";
	/** 站点状态非正常运营访问 */
	String SITE_ABNORMAL = "site.abnormal";
	/** 系统短信接口配置有误，请联系客服！ */
	String SMS_CONFIG_ERROR = "sms.config.error";
	/** 该短信接口尚未启用，请联系客服！ */
	String SMS_CONFIG_CLOSED = "sms.config.closed";
	/*
	 * oauth授权
	 */
	/** 请关掉程序重新打开，再重新进行授权登录！ */
	String OAUTH_AUTH_LOGIN_EXIST_USER_ILLEGAL = "oauth.auth.login.exist.user.illegal";
	/** {0}授权登录尚未启用 */
	String OAUTH_AUTH_SERVICE_ENABLED = "oauth.auth.service.enabled";
	/** 授权登录失败，请重试~ */
	String OAUTH_AUTH_USER_INFO_IS_NULL = "oauth.auth.user.info.is.null";
	/** 当前账号已注销，请登录其他账号！ */
	String OAUTH_AUTH_PRE_BIND_INVALID = "oauth.auth.pre.bind.invalid";
	/** 该{0}账号已被绑定，无需重复操作！ */
	String OAUTH_AUTH_BIND_USER_BOUND = "oauth.auth.bind.user.bound";
	/** 该{0}账号已经被注销，请使用其他账号登录！ */
	String OAUTH_AUTH_BIND_USER_UN_BIND = "oauth.auth.bind.user.un.bind";
	/** 暂不支持一个用户同时绑定多个账号！ */
	String OAUTH_AUTH_BIND_USER_MORE_ACCOUNT = "oauth.auth.bind.user.more.account";
	/** 您的账号尚未绑定{0}！ */
	String OAUTH_AUTH_UNBIND_WAIT_BIND = "oauth.auth.unbind.wait.bind";

	/** 任务未完成 */
	String TASK_UNDONE = "task.undone";
	/** 任务未完成 */
	String TASK_POINT_INSUFFICIENT = "task.point.insufficient";
	/** 限制领取奖励 */
	String TASK_LIMIT_CLAIM = "task.limit.claim";
	/** 奖励领取成功 */
	String REWARD_RECEIVED_SUCCESS = "reward.received.success";
	/** 奖励领取失败 */
	String REWARD_RECEIVED_FAIL = "reward.received.fail";
	/** 您没有参与资格 */
	String NOT_ELIGIBLE_TO_PARTICIPATE = "not.eligible.to.participate";
	/** 没有可领取的奖励 */
	String NO_REWARDS_TO_COLLECT = "no.rewards.to.collect";
	/** 文件格式错误 */
	String FILE_UPLOAD_FORMAT_ERROR = "file.upload.format.error";
	/** 可申请次数不足 */
	String APPLY_COUNT_DEFICIENCY = "apply.count.deficiency";
	/** 已存在申请 */
	String APPLY_FOR_EXISTING = "apply.for.existing";
	/** 请回答全部问题 */
	String PLEASE_ANSWER_ALL_QUESTIONS = "please.answer.all.questions";
	/** 请先完成绑卡 */
	String TASK_UNCOMPLETED_BING_CARD = "task.uncompleted.bing.card";
	/** 请先绑定手机号 */
	String TASK_UNCOMPLETED_BING_PHONE = "task.uncompleted.bing.phone";
	/** 请先实名 */
	String TASK_UNCOMPLETED_REAL_NAME = "task.uncompleted.real.name";
	/** 任务状态{0} */
	String TASK_STATUS_ERROR = "task.status.error";

	// 消息状态
	/** 已拒绝 */
	String MSG_STATUS_REJECTED = "msg.status.rejected";
	/** 已撤回 */
	String MSG_STATUS_CANCELED = "msg.status.canceled";
	/** 待发送 */
	String MSG_STATUS_PENDING = "msg.status.pending";
	/** 已发送 */
	String MSG_STATUS_SENT = "msg.status.sent";
	/** 已结束 */
	String MSG_STATUS_ENDED = "msg.status.ended";
	/** 待审核 */
	String MSG_STATUS_AUDITING = "msg.status.auditing";
	/** 发送中 */
	String MSG_STATUS_SENDING = "msg.status.sending";

	// 签到方式
	/** 连续签到 */
	String SIGN_MODE_SUSTAIN = "sign.mode.sustain";
	/** 累计签到 */
	String SIGN_MODE_ACCUMULATE = "sign.mode.accumulate";

	// 活动 条件
	/** 无条件 */
	String PROMOTION_COND_NONE = "promotion.cond.none";
	/** 当日充值 */
	String PROMOTION_COND_DAILY_RECHARGE = "promotion.cond.daily.recharge";
	/** 当日打码 */
	String PROMOTION_COND_DAILY_PLAY = "promotion.cond.daily.play";
	/** 充值次数 */
	String PROMOTION_COND_RECHARGE_SUM = "promotion.cond.recharge.sum";
	/** 昨日累计充值 */
	String PROMOTION_COND_YESTERDAY_SUM_RECHARGE = "promotion.cond.yesterday.sum.recharge";
	/** 昨日累计打码 */
	String PROMOTION_COND_SUM_PLAY = "promotion.cond.sum.play";
	/** 昨日累计亏损 */
	String PROMOTION_COND_LOSS = "promotion.cond.loss";

	// 代理规则
	/** 仅限新用户下级 */
	String AGENT_RULE_TYPE_NEW = "agent.rule.type.new";
	/** 包含历史下级 */
	String AGENT_RULE_TYPE_HISTORY = "agent.rule.type.history";

	// 红包类型
	/** 抢红包 */
	String RED_ENVELOPE_TYPE_GRAB = "red.envelope.type.grab";
	/** 开红包 */
	String RED_ENVELOPE_TYPE_OPEN = "red.envelope.type.open";
	/** 送红包 */
	String RED_ENVELOPE_TYPE_SEND = "red.envelope.type.send";
	/** 按所有会员 */
	String RED_ENVELOPE_MODE_ALL = "red.envelope.mode.all";
	/** 按VIP大等级 */
	String RED_ENVELOPE_MODE_MAJOR = "red.envelope.mode.major";
	/** 按VIP小等级 */
	String RED_ENVELOPE_MODE_VIP = "red.envelope.mode.vip";
	// 监控处罚范围
	/** 全部处罚 */
	String BOT_SPY_PUNISH_RANGE_ALL = "bot.spy.punish.range.all";
	/** 只处罚超出范围的账号 */
	String BOT_SPY_PUNISH_RANGE_OUT_RANGE = "bot.spy.punish.range.out.range";

	// 跳转类型
	/** 无 */
	String SITE_BANNER_REDIRECT_NONE = "site.banner.redirect.none";
	/** 外部链接 */
	String SITE_BANNER_REDIRECT_EXT_LINK = "site.banner.redirect.ext.link";
	/** 活动 */
	String SITE_BANNER_REDIRECT_ACTIVITY = "site.banner.redirect.activity";
	/** 活动页 */
	String SITE_BANNER_REDIRECT_ACTIVITY_PAGE = "site.banner.redirect.activity.page";
	/** 任务 */
	String SITE_BANNER_REDIRECT_TASK = "site.banner.redirect.task";
	/** 返水 */
	String SITE_BANNER_REDIRECT_REFUND = "site.banner.redirect.refund";
	/** 返水 */
	String SITE_BANNER_REDIRECT_DEPOSIT_POOL = "site.banner.redirect.deposit.pool";
	/** 代理 */
	String SITE_BANNER_REDIRECT_AGENT = "site.banner.redirect.agent";
	/** VIP */
	String SITE_BANNER_REDIRECT_VIP = "site.banner.redirect.vip";
	/** 余额 */
	String SITE_BANNER_REDIRECT_BALANCE = "site.banner.redirect.balance";
	/** 票券 */
	String SITE_BANNER_REDIRECT_TICKET = "site.banner.redirect.ticket";
	/** 充值 */
	String SITE_BANNER_REDIRECT_RECHARGE = "site.banner.redirect.recharge";
	/** 钱包 */
	String SITE_BANNER_REDIRECT_WALLET = "site.banner.redirect.wallet";
	/** 系统内置模块 */
	String SITE_BANNER_REDIRECT_SYSTEM_MODULE = "site.banner.redirect.system.module";
	/** 待领取列表 */
	String PROMOTION_TYPE_PENDING_LIST = "site.banner.redirect.pending_list";
	/** 领取记录 */
	String PROMOTION_TYPE_CLAIM_LIST = "site.banner.redirect.claim_list";
	/** 提现 */
	String CASH = "site.banner.redirect.cash";
	/** 客服中心 */
	String PROMOTION_TYPE_CUSTOMER_SERVICE = "site.banner.redirect.customer_service";
	/** 找回余额 */
	String APP_LAYOUT_MENU_RETRIEVE_BALANCE = "site.banner.redirect.retrieve_balance";
	/** 安全中心 */
	String APP_LAYOUT_MENU_SECURITY_CENTER = "site.banner.redirect.security_center";

	// 新人彩金活动类型 兑换码类型
	/** 唯一型兑换码 */
	String REDEEM_CODE_TYPE_ONLY = "redeem.code.type.only";
	/** 可重复使用兑换码 */
	String REDEEM_CODE_TYPE_REPEAT = "redeem.code.type.repeat";

	// 自定义规则类型
	/** 内置界面 */
	String CUSTOM_RULE_TYPE_ACTIVITY = "custom.rule.type.activity";
	/** 外部跳转 */
	String CUSTOM_RULE_TYPE_LINK = "custom.rule.type.link";
	/** 内部跳转 */
	String CUSTOM_RULE_TYPE_INNER_LINK = "custom.rule.type.inner.link";

	// 自定义活动审核状态
	/** 待审核 */
	String PROMOTION_AUDIT_STATUS_PENDING = "promotion.audit.status.pending";
	/** 已通过 */
	String PROMOTION_AUDIT_STATUS_PASSED = "promotion.audit.status.passed";
	/** 待领取 */
	String PROMOTION_AUDIT_STATUS_UNCLAIMED = "promotion.audit.status.unclaimed";
	/** 已拒绝 */
	String PROMOTION_AUDIT_STATUS_DENIED = "promotion.audit.status.denied";
	/** 已领取 */
	String PROMOTION_AUDIT_STATUS_CLAIMED = "promotion.audit.status.claimed";
	/** 已派发 */
	String PROMOTION_AUDIT_STATUS_SEND = "promotion.audit.status.send";
	/** 已过期 */
	String PROMOTION_AUDIT_STATUS_EXPIRED = "promotion.audit.status.expired";

	// 弹窗方式
	/** 不弹窗 */
	String POP_MODE_NO_POP = "pop.mode.no.pop";
	/** 只弹一次 */
	String POP_MODE_POP_ONLY = "pop.mode.pop.only";
	/** 每日一次 */
	String POP_MODE_DAILY_POP = "pop.mode.daily.pop";
	/** 高频弹窗 */
	String POP_MODE_HIGH_FREQUENCY = "pop.mode.high.frequency";

	// 登录方式
	/** 密码 */
	String LOGIN_WAY_PASSWORD = "login.way.password";
	/** 密码错误 */
	String LOGIN_PASSWORD_FAIL = "login.password.fail";
	/** 记住密码 */
	String LOGIN_WAY_REMEMBER_PASSWORD = "login.way.remember.password";
	/** 验证码 */
	String LOGIN_WAY_CAPTCHA = "login.way.captcha";
	/** 邮箱验证码 */
	String LOGIN_WAY_MAIL_CAPTCHA = "login.way.mail.captcha";
	/** Google */
	String LOGIN_WAY_APPLE_AUTH = "login.way.apple.auth";
	/** Apple */
	String LOGIN_WAY_GOOGLE_AUTH = "login.way.google.auth";
	/** Facebook */
	String LOGIN_WAY_FACEBOOK_AUTH = "login.way.facebook.auth";

	// app 更新类型
	/** 非提示更新 */
	String APP_UPDATE_TYPE_NOT_TIPS = "app.update.type.not.tips";
	/** 提示更新 */
	String APP_UPDATE_TYPE_TIPS = "app.update.type.tips";
	/** 强制更新 */
	String APP_UPDATE_TYPE_MANDATORY = "app.update.type.mandatory";

	// app 更新状态
	/** 删除 */
	String APP_UPDATE_STATUS_DELETE = "app.update.status.delete";
	/** 待打包 */
	String APP_UPDATE_STATUS_WAIT = "app.update.status.wait";
	/** 打包中 */
	String APP_UPDATE_STATUS_PACKED = "app.update.status.packed";
	/** 打包成功 */
	String APP_UPDATE_STATUS_SUCCESS = "app.update.status.success";
	/** 已发布 */
	String APP_UPDATE_STATUS_RELEASE = "app.update.status.release";
	/** 待发布 */
	String APP_UPDATE_STATUS_WAIT_RELEASE = "app.update.status.wait.release";
	/** 打包失败，请重新打包 */
	String APP_UPDATE_STATUS_FAIL = "app.update.status.fail";

	/** 该时间段红包已经被抢完啦 */
	String RED_ENVELOPE_PERIOD_TOTAL = "red.envelope.period.total";
	/** 太慢啦，活动红包已派发完毕 */
	String RED_ENVELOPE_TOTAL_AMOUNT_TIP = "red.envelope.total.amount";
	/** 额度不足提醒 */
	String MSG_TITLE_QUOTA_NOT_ENOUGH = "msg.title.quota.not.enough";
	/** 授信额度修正 */
	String MSG_TITLE_QUOTA_FIXED = "msg.title.quota.fixed";
	/** 短信余额不足 */
	String MSG_TITLE_SMS_BALANCE_NOT_ENOUGH = "msg.title.sms.balance.not.enough";
	/** 站点余额不足 */
	String MSG_TITLE_WEBSITE_BALANCE_NOT_ENOUGH = "msg.title.website.balance.not.enough";
	/** 新增游戏厂家 */
	String MSG_TITLE_ADD_VENDOR = "msg.title.add.vendor";
	/** 月账单已生成 */
	String MSG_TITLE_MONTHLY_BILL_GENERATED = "msg.title.monthly.bill.generated";
	/** 月账单最晚核对时间 */
	String MSG_TITLE_MONTHLY_BILL_LAST_CHECK_TIME = "msg.title.monthly.bill.last.check.time";
	/** 商户推广佣金发放通知 */
	String MSG_TITLE_MONTHLY_BILL_LAST_PAY_TIME = "msg.title.monthly.bill.last.pay.time";
	/** 商户推广佣金 */
	String MSG_TITLE_MERCHANT_INVITE_REWARD = "msg.title.merchant.invite.reward";
	/** 服务器费用通知 */
	String MSG_TITLE_SERVER_FEE_NOTICE = "msg.title.server.fee.notice";
	/** 欠费通知 */
	String MSG_TITLE_OVERDUE_PAY_NOTICE = "msg.title.overdue.pay.notice";
	/** 领取失败，票券无效！ */
	String TICKET_RECEIVE_FAIL_INVALID = "ticket.receive.fail.invalid";
	/** 领取失败，任务未完成！ */
	String TICKET_RECEIVE_FAIL_TASK = "ticket.receive.fail.task";
	/** 领取失败，此票券只能在APP领取！ */
	String TICKET_RECEIVE_FAIL_SCOPE_APP = "ticket.receive.fail.scope.app";
	/** 会员层级受限，无法领取 */
	String MARKET_LIMIT_FIX_LAYER = "market.limit.fix.layer";
	/** 会员VIP等级受限，无法领取 */
	String MARKET_LIMIT_VIP_LEVEL = "market.limit.vip.level";
	/** 领奖设备限制，只能{}领取 */
	String MARKET_LIMIT_DEVICE = "market.limit.device";
	/** 相同IP不能重复领取 */
	String MARKET_LIMIT_SAME_IP = "market.limit.same.ip";
	/** 无法获取设备信息 */
	String MARKET_LIMIT_DEVICE_INFO = "market.limit.device.info";
	/** 相同设备不能重复领取 */
	String MARKET_LIMIT_SAME_DEVICE = "market.limit.same.device";
	/** 不属于指定代理下的用户不能领取 */
	String MARKET_LIMIT_AGENT_ID = "market.limit.agent.id";
	/** 不属于指定渠道下的用户不能领取 */
	String MARKET_LIMIT_CHANNEL_ID = "market.limit.channel.id";
	/** 当前网址不能领取 */
	String MARKET_LIMIT_DOMAIN = "market.limit.domain";

	/** 一次性（推荐） */
	String DISPOSABLE = "cycle.mode.disposable";
	/** 每小时 */
	String HOUR = "cycle.mode.hour";
	/** 每日 */
	String DAILY = "cycle.mode.daily";
	/** 每周 */
	String WEEKLY = "cycle.mode.weekly";
	/** 每月 */
	String MONTHLY = "cycle.mode.monthly";

	/** 推广有效下级 */
	String REWARD_TASK_AGENT = "reward.task.agent";
	/** 本人累计打码 */
	String REWARD_TASK_PLAY = "reward.task.play";
	/** 本人累计充值 */
	String REWARD_TASK_RECHARGE = "reward.task.recharge";
	/** 下载APP */
	String REWARD_TASK_DOWNLOAD_APP = "reward.task.download_app";
	/** 绑定手机号 */
	String REWARD_TASK_BIND_PHONE = "reward.task.bind_phone";
	/** 绑定提现方式 */
	String REWARD_TASK_BIND_CASH = "reward.task.bind_cash";
	/** {0}次数不足 */
	String REWARD_TURNTABLE_NO_POINT = "reward.turntable.no.point";
	/** 任务已完成 */
	String REWARD_TURNTABLE_TASK_COMPLETED = "reward.turntable.task.completed";

	/** 未达到领取条件 */
	String DEPOSIT_RECEIVE_LOG_UNDONE = "deposit.receive.log.undone";
	/** 已达到最大领取次数 */
	String DEPOSIT_RECEIVE_LOG_LIMIT = "deposit.receive.log.limit";
	/** 超出今日领取限制 */
	String DEPOSIT_RECEIVE_LOG_DAILY_LIMIT = "deposit.receive.log.daily.limit";
	/** 达到累计领取封顶限制 */
	String DEPOSIT_RECEIVE_LOG_TOTAL_LIMIT = "deposit.receive.log.total.limit";
	/** 公积金虚拟奖励列表 刚刚领取了 */
	String DEPOSIT_VIRTUAL_REWARD = "deposit.virtual.reward";
	/** 无限级差 */
	String AGENT_MODEL_UNLIMITED_LEVEL = "agent.model.unlimited.level";
	/** 自定义级差 */
	String AGENT_MODEL_CUSTOM_LEVEL = "agent.model.custom.level";
	/** 三级充值 */
	String AGENT_MODEL_THREE_RECHARGE = "agent.model.three.recharge";
	/** 1/2级充值 */
	String AGENT_MODEL_TWO_RECHARGE = "agent.model.two.recharge";
	/** 简易目标 */
	String AGENT_MODEL_SIMPLE = "agent.model.simple";
	/** 组合模式 */
	String AGENT_MODEL_GROUP = "agent.model.group";
	/** 有效投注模式 */
	String AGENT_BASIS_PLAY = "agent.basis.play";
	/** 累计充值模式 */
	String AGENT_BASIS_TOTAL_RECHARGE = "agent.basis.total.recharge";
	/** 首充模式 */
	String AGENT_BASIS_FIRST_RECHARGE = "agent.basis.first.recharge";
	/** 简易目标模式 */
	String AGENT_BASIS_SIMPLE = "agent.basis.simple";
	/** 充值返佣模式 */
	String AGENT_BASIS_RECHARGE = "agent.basis.recharge";
	/** 手动领取 */
	String AGENT_RECEIVE_MANUAL = "agent.receive.manual";
	/** 自动发放 */
	String AGENT_RECEIVE_AUTO = "agent.receive.auto";
	/** 达到阈值 */
	String AGENT_AUDIT_THRESHOLD = "agent.audit.threshold";
	/** 注册奖励（简易目标下才会有） */
	String AGENT_AUDIT_REGISTER_REWARD = "agent.audit.register.reward";
	/** 总佣金 */
	String AGENT_QUERY_TYPE_TOTAL = "agent.query.type.total";
	/** 有效投注佣金 */
	String AGENT_QUERY_TYPE_VALID_COIN = "agent.query.type.validCoin";
	/** 充值佣金 */
	String AGENT_QUERY_TYPE_RECHARGE = "agent.query.type.recharge";
	/** 有效投注业绩 */
	String AGENT_QUERY_TYPE_VALID_COIN_PERFORMANCE = "agent.query.type.validCoinPerformance";
	/** 充值金额 */
	String AGENT_QUERY_TYPE_RECHARGE_AMOUNT = "agent.query.type.rechargeAmount";
	/** 首充金额 */
	String AGENT_QUERY_TYPE_FIRST_RECHARGE_AMOUNT = "agent.query.type.firstRechargeAmount";
	/** 首充佣金 */
	String AGENT_QUERY_TYPE_FIRST_RECHARGE = "agent.query.type.firstRecharge";
	/** 注册佣金 */
	String AGENT_QUERY_TYPE_REGISTER = "agent.query.type.register";
	/** 团队成员 */
	String AGENT_QUERY_TYPE_USER_COUNT = "agent.query.type.userCount";
	/** 充值人数 */
	String AGENT_QUERY_TYPE_RECHARGE_COUNT = "agent.query.type.rechargeCount";
	/** 投注人数 */
	String AGENT_QUERY_TYPE_PLAY_COUNT = "agent.query.type.playCount";
	/** 额外成就 */
	String AGENT_QUERY_TYPE_ADDITIONAL = "agent.query.type.additional";
	/** 组员奖励 */
	String AGENT_QUERY_TYPE_SIMPLE_TEAM = "agent.query.type.simple.team";
	/** 首充成就 */
	String AGENT_QUERY_TYPE_FIRST_RECHARGE_ADDITIONAL = "agent.query.type.first.recharge.additional";
	/** 首充人数 */
	String AGENT_QUERY_TYPE_FIRST_RECHARGE_COUNT = "agent.query.type.firstRechargeCount";
	/** 邀请人数达标 */
	String AGENT_REWARD_INVITE_COUNT_VALID = "agent.reward.invite.count.valid";
	/** 首充人数达标 */
	String AGENT_REWARD_FIRST_RECHARGE_COUNT_VALID = "agent.reward.first.recharge.count.valid";
	/** 额外目标达成 */
	String AGENT_REWARD_ADDITIONAL_VALID = "agent.reward.additional.valid";

	/** 日奖励 */
	String SIGN_TYPE_DAILY_REWARD = "sign.type.daily.reward";
	/** VIP大等级-青铜 */
	String MAJOR_LEVEL_BRONZE = "major.level.bronze";
	/** VIP大等级-白银 */
	String MAJOR_LEVEL_SILVER = "major.level.silver";
	/** VIP大等级-黄金 */
	String MAJOR_LEVEL_GOLD = "major.level.gold";
	/** VIP大等级-铂金 */
	String MAJOR_LEVEL_PLATINUM = "major.level.platinum";
	/** VIP大等级-钻石 */
	String MAJOR_LEVEL_DIAMOND = "major.level.diamond";
	/** VIP大等级-大师 */
	String MAJOR_LEVEL_MASTER = "major.level.master";
	/** VIP大等级-传说 */
	String MAJOR_LEVEL_LEGEND = "major.level.legend";
	/** 神启 */
	String MAJOR_LEVEL_CELESTIAL = "major.level.celestial";
	/** 圣境 */
	String MAJOR_LEVEL_EMPYREAN = "major.level.empyrean";
	/** 永恒 */
	String MAJOR_LEVEL_AEON = "major.level.aeon";
	/** 全知 */
	String MAJOR_LEVEL_OMNISCIENT = "major.level.omniscient";
	/** 钱包 充值奖金 */
	String WALLET_RECHARGE_BONUS = "wallet.recharge.bonus";
	/** 钱包 打码奖金 */
	String WALLET_PLAY_BONUS = "wallet.play.bonus";
	/** 钱包 攒金转盘奖金 */
	String WALLET_GOLD_TURNTABLE_BONUS = "wallet.gold.turntable.bonus";
	/** 钱包 代理奖金 */
	String WALLET_AGENT_BONUS = "wallet.agent.bonus";
	/** 钱包 任务奖金 */
	String WALLET_TASK_BONUS = "wallet.task.bonus";
	/** 钱包 VIP奖金 */
	String WALLET_VIP_BONUS = "wallet.vip.bonus";
	/** 钱包 公积金奖金 */
	String WALLET_DEPOSIT_POOL_BONUS = "wallet.deposit.pool.bonus";
	/** 钱包 利息宝奖金 */
	String WALLET_INCOME_BONUS = "wallet.income.bonus";
	/** 钱包 签到奖金 */
	String WALLET_SIGN_BONUS = "wallet.sign.bonus";
	/** 钱包 红包雨奖金 */
	String WALLET_RED_ENVELOPE_BONUS = "wallet.red.envelope.bonus";
	/** 钱包 每日转盘奖金 */
	String WALLET_DAILY_TURNTABLE_BONUS = "wallet.daily.turntable.bonus";
	/** 钱包 幸运转盘奖金 */
	String WALLET_TURNTABLE_BONUS = "wallet.turntable.bonus";

	/** 已参加jackpot */
	String JOINED_JACKPOT = "joined.jackpot";
	/** 当余额<{0} 时，系统会自动解除稽核任务，投注统计完成后方可触发自动解除 */
	String AUTO_RELEASE_AUDIT = "auto.release.audit";
	/* 在线客服 */
	String ONLINE_CUSTOMER_SERVICE = "online.customer.service";

	/** 系统派发 */
	String REWARD_SOURCE_SYS = "reward.source.sys";
	/** VIP奖励 */
	String REWARD_SOURCE_VIP = "reward.source.vip";
	/** 任务奖励 */
	String REWARD_SOURCE_TASK = "reward.source.task";
	/** 活动奖励 */
	String REWARD_SOURCE_PROMOTION = "reward.source.promotion";
	/** 返水奖励 */
	String REWARD_SOURCE_REBATE = "reward.source.rebate";
	/** 公积金奖励 */
	String REWARD_SOURCE_DEPOSIT_POOL = "reward.source.deposit.pool";
	/** 幸运转盘 */
	String REWARD_SOURCE_TURNTABLE = "reward.source.turntable";
	/** 签到活动说明 1.每日充值、有效投注满足活动条件，才可进行签到，签到成功可领取对应奖金，最高{2}（最高金额）；<br/>2.签到投注不限制平台，奖励预计10分钟后更新，请等待奖励发放；<br/> */
	String SIGN_REMARK_ONE_TWO = "sign.remark.one.two";
	/** 签到活动说明 3.此活动属{0}{1} */
	String SIGN_REMARK_THIRD_FIRST_HALF = "sign.remark.third.first.half";
	/** 签到活动说明 3.，若期间中断，将从第1天开始；<br/> */
	String SIGN_REMARK_THIRD_LATTER_HALF = "sign.remark.third.latter.half";
	/** 签到活动说明 4.奖励{0}手动领取,未领取作废；<br/> */
	String SIGN_REMARK_FOUR = "sign.remark.four";
	/** 仅限在{0}端 */
	String SIGN_REMARK_FOUR_LIMIT = "sign.remark.four.limit";
	/** {0}.本活动所赠送的奖金（不含本金）需{1}倍有效投注才能提现{3} {4}；<br/>{5}.本活动仅限账号本人进行正常的人为操作，禁止租借、使用外挂、机器人、不同账号对赌、互刷、套利、接口、协议、利用漏洞、群控或其他技术手段参与，否则将取消或扣除奖励、冻结、甚至拉入黑名单；<br/>{6}.为避免文字理解差异，平台将保留本活动最终解释权。 */
	String SIGN_REMARK_FIVE = "sign.remark.five";
	/** 仅限：{0} */
	String SIGN_REMARK_FIVE_GAME_LIMIT = "sign.remark.five.game.limit";
	/** 投注仅限<br/> {0} */
	String DEPOSIT_REMARK_FIVE_GAME_LIMIT = "deposit.remark.five.game.limit";
	/** 投注不限游戏平台 */
	String DEPOSIT_REMARK_FIVE_ALL_GAME = "deposit.remark.five.all.game";
	/** 不限游戏平台 */
	String SIGN_REMARK_FIVE_ALL_GAME = "sign.remark.five.all.game";
	/** 公积金活动说明 1、赠送方式： 每次充值≥{0}，会赠送充值金额{1}%的奖金，该奖金存放至公积金账户，充值越多，奖励越多。<br/>2、取出条件： 每次充值都会积累新的公积金，所以每次都要完成该充值本金的{2}倍有效投注，才能取出公积金；若公积金无赠送或已封顶的，则需要完成该充值的 {3}倍有效投注；<br/> */
	String DEPOSIT_REMARK_ONE_TWO = "deposit.remark.one.two";
	/** 公积金活动说明 不限赠送次数 */
	String DEPOSIT_REMARK_THIRD_MID_FIRST = "deposit.remark.third.mid.first";
	/** 公积金活动说明 累计赠送{0}次， */
	String DEPOSIT_REMARK_THIRD_MID_LATTER = "deposit.remark.third.mid.latter";
	/** 公积金活动说明 金额无上限。 */
	String DEPOSIT_REMARK_THIRD_RIGHT_FIRST = "deposit.remark.third.right.first";
	/** 公积金活动说明 封顶{0}。 */
	String DEPOSIT_REMARK_THIRD_RIGHT_LATTER = "deposit.remark.third.right.latter";
	/** 3、赠送封顶： 活动周期内，{0}公积金累计{1} */
	String DEPOSIT_REMARK_THIRD_RIGHT = "deposit.remark.third.right";
	/** 4、重置周期： 公积金有效期为 每月，周期结束7天后未取出，公积金奖励自动作废 */
	String DEPOSIT_REMARK_CYCLE_FOUR = "deposit.remark.cycle.four";
	/** 公积金奖励自动派发 */
	String DEPOSIT_REMARK_DISPATCH_MODE_AUTO = "deposit.remark.dispatch.mode.auto";
	/** 公积金奖励自动作废 */
	String DEPOSIT_REMARK_DISPATCH_MODE_MANUAL = "deposit.remark.dispatch.mode.manual";
	/** {0}、稽核倍数：取出的公积金奖金需{1}倍有效投注，才能申请提现，{2} */
	String DEPOSIT_REMARK_FOUR = "deposit.remark.four";
	/** {0}手动领取， */
	String DEPOSIT_REMARK_FIVE_MID = "deposit.remark.five.mid";
	/** 5、领取限制： {0}活动期间内，{1} */
	String DEPOSIT_REMARK_FIVE = "deposit.remark.five";
	/** 可多次领取奖励。 */
	String DEPOSIT_REMARK_FIVE_RIGHT_FIRST = "deposit.remark.five.right.first";
	/** 仅可领取1次奖励。 */
	String DEPOSIT_REMARK_FIVE_RIGHT_LATTER = "deposit.remark.five.right.latter";
	/** <br/>6、违规声明： 仅限账号本人进行正常的人为操作，禁止租借、使用外挂、机器人、不同账号对赌、互刷、套利、接口、协议、利用漏洞、群控或其他技术手段参与，否则将取消或扣除奖励、冻结、甚至拉入黑名单。<br/>7、最终解释： 为避免文字理解差异，平台将保留本活动最终解释权。 */
	String DEPOSIT_REMARK_SIX = "deposit.remark.six";
	/** 充值获得幸运值 */
	String TURNTABLE_TRADE_TYPE_RECHARGE = "turntable.trade.type.recharge";
	/** 投注获得幸运值 */
	String TURNTABLE_TRADE_TYPE_PLAY = "turntable.trade.type.play";
	/** 白银转盘消耗 */
	String TURNTABLE_TRADE_TYPE_SILVER_OUT = "turntable.trade.type.silver.out";
	/** 黄金转盘消耗 */
	String TURNTABLE_TRADE_TYPE_GOLD_OUT = "turntable.trade.type.gold.out";
	/** 钻石转盘消耗 */
	String TURNTABLE_TRADE_TYPE_DIAMOND_OUT = "turntable.trade.type.diamond.out";
	/** 幸运值过期 */
	String TURNTABLE_TRADE_TYPE_EXPIRED = "turntable.trade.type.expired";
	/** 人工增加 */
	String TURNTABLE_TRADE_TYPE_ADJUST_IN = "turntable.trade.type.adjust.in";
	/** 白银转盘 */
	String TURNTABLE_TYPE_SILVER = "turntable.type.silver";
	/** 黄金转盘 */
	String TURNTABLE_TYPE_GOLD = "turntable.type.gold";
	/** 钻石转盘 */
	String TURNTABLE_TYPE_DIAMOND = "turntable.type.diamond";

	/** 仿Google商店 */
	String APP_STORE_CONFIG_GOOGLE = "app.store.config.google";
	/** 仿APP商店 */
	String APP_STORE_CONFIG_APP = "app.store.config.app";
	/** .此活动属于"人道救援,江湖救急",当您{0}在游戏中亏损{1}或以上,即可获得对应金额的救援金,奖金高低由亏损金额决定,亏损越多,救援金额越高,最高{2};<br/> */
	String RELIEF_REMARK_1 = "relief.remark.one";
	/** .亏损不限制平台,奖励预计次日01:00左右更新,请等特奖励发放;<br/> */
	String RELIEF_REMARK_2 = "relief.remark.two";
	/** .计算公式:赠送金额=周期内亏损金额(扣除领取的优惠)×返奖比例;<br/> */
	String RELIEF_REMARK_3 = "relief.remark.three";
	/** .{0},{1};<br/> */
	String RELIEF_REMARK_4 = "relief.remark.four";
	/** .每个周期结束{0}天后,获得的奖励过期,过期自动发放<br/> */
	String RELIEF_REMARK_5_1 = "relief.remark.five.one";
	/** .每个周期结束{0}天后,获得的奖励过期,过期直接作废<br/> */
	String RELIEF_REMARK_5_2 = "relief.remark.five.two";
	/** .本活动所赠送的奖金(不含本金)需{0}倍有效投注才能提现, */
	String RELIEF_REMARK_6 = "relief.remark.six";
	/**
	 * 每月几号的描述
	 * 使用： I18N.msg(UserI18nKey.DAY_OF_MONTH_NAME + this.allotDay)
	 */
	String DAY_OF_MONTH_NAME = "day.of.month.name.";
	/** 获得的奖励次日:{0}之后才可领取 */
	String CLAIM_LIMIT_NEXT_DAY = "claim.limit.next.day";
	/** 获得的奖励每日:{0}之间才可领取 */
	String CLAIM_LIMIT_EVERYDAY = "claim.limit.everyday";
	/** 获得的奖励下周{0}:{1}之后才可领取 */
	String CLAIM_LIMIT_NEXT_WEEK = "claim.limit.next.week";
	/** 获得的奖励每周{0}:{1}之间才可领取 */
	String CLAIM_LIMIT_WEEKLY = "claim.limit.weekly";
	/** 获得的奖励下月{0}:{1}之后才可领取 */
	String CLAIM_LIMIT_NEXT_MONTH = "claim.limit.next.month";
	/** 获得的奖励每月{0}:{1}之间才可领取 */
	String CLAIM_LIMIT_MONTHLY = "claim.limit.monthly";
	/** 仅限在{0}端手动领取 */
	String DEVICE_CLAIM_LIMIT = "device.claim.limit";
	/** 同时需要绑定{0} */
	String BIND_CLAIM_LIMIT = "bind.claim.limit";
	/** 昨日 */
	String RELIEF_REMARK_CYCLE_DAY = "relief.remark.cycle.day";
	/** 上周 */
	String RELIEF_REMARK_CYCLE_WEEK = "relief.remark.cycle.week";
	/** 上月 */
	String RELIEF_REMARK_CYCLE_MONTH = "relief.remark.cycle.month";
	/** 余额救援金活动说明 1.当您余额过低时，系统会随机为您赠送一笔神秘奖金，最高奖励可达{0}.<br/> */
	String RELIEF_BALANCE_REMARK_ONE = "relief.balance.remark.one";
	/** 余额救援金活动说明 {0}仅限在{1}端 */
	String RELIEF_BALANCE_REMARK_TWO_FIRST_HALF = "relief.balance.remark.two.first.half";
	/** 余额救援金活动说明 2.奖励{0}自动派发。<br/> */
	String RELIEF_BALANCE_REMARK_TWO_LATTER_HALF = "relief.balance.remark.two.latter.half";
	/** 余额救援金活动说明 3.本活动所赠送的奖金(不含本金)需{0}倍有效投注才能提现,投注不限游戏平台;<br/> */
	String RELIEF_BALANCE_REMARK_THIRD = "relief.balance.remark.third";
	/** 余额救援金活动说明 4.本活动仅限账号本人进行正常的人为操作,禁止租借、使用外挂、机器人、不同账号对赌、互刷、套利、接口、协议、利用漏洞、群控或其他技术手段参与,否则将取消或扣除奖励、冻结、甚至拉入黑名单;<br/> */
	String RELIEF_BALANCE_REMARK_FOUR = "relief.balance.remark.four";
	/** 余额救援金活动说明 5.为避免文字理解差异,平台将保留本活动最终解释权。 */
	String RELIEF_BALANCE_REMARK_FIVE = "relief.balance.remark.five";
	/** 1.活动期间，{0}累计投注将获得不同程度的奖励，投注越多，奖励越多，最高奖励 {1}； */
	String PLAY_REMARK_ONE = "play.remark.one";
	/** 2.投注不限制平台，奖励预计10分钟后更新，请等待奖励发放； */
	String PLAY_REMARK_TWO = "play.remark.two";
	/** 3.获得的奖励{0}领取，{1}{2}； */
	String PLAY_REMARK_THIRD = "play.remark.third";
	/** 仅限在{0} */
	String PLAY_REMARK_THIRD_FIRST_HALF = "play.remark.third.first.half";
	/** {0}.每个周期结束{1}后，获得的奖励过期{2}； */
	String PLAY_REMARK_FOUR = "play.remark.four";
	/** 过期天数 0天 */
	String PLAY_REMARK_FOUR_ZERO_DAY = "play.remark.four.zero.day";
	/** 过期天数 1天 */
	String PLAY_REMARK_FOUR_GT_DAY = "play.remark.four.gt.day";
	/** {0}.本活动所赠送的奖金{1}需{2}倍有效投注才能提现，投注{3} */
	String PLAY_REMARK_FIVE = "play.remark.five";
	/** 稽核本金+奖金 */
	String REMARK_AUDIT_MODE_CAPITAL_BONUS = "remark.audit.mode.capital.bonus";
	/** 稽核余额+奖金 */
	String REMARK_AUDIT_MODE_BALANCE_BONUS = "remark.audit.mode.balance.bonus";
	/** 天 */
	String PLAY_REMARK_DAY = "play.remark.day";
	/** 天 */
	String PLAY_REMARK_DAYS = "play.remark.days";
	/** {0} */
	String PLAY_REMARK_EXPIRE_DAYS = "play.remark.expire.days";
	/** 不含本金 */
	String PLAY_REMARK_BONUS = "play.remark.bonus";
	/** 累计 */
	String RECHARGE_REMARK_CYCLE_CUMULATIVE = "recharge.remark.cycle.cumulative";
	/** 单次 */
	String RECHARGE_REMARK_CYCLE_SINGLE = "recharge.remark.cycle.single";
	/** 每日 */
	String PLAY_REMARK_EVERYDAY = "play.remark.everyday";
	/** 每周 */
	String PLAY_REMARK_WEEKLY = "play.remark.weekly";
	/** 每月 */
	String PLAY_REMARK_MONTHLY = "play.remark.monthly";
	/** 次日{0}后才可 */
	String PLAY_REMARK_NEXT_DAY = "play.remark.next.day";
	/** 每日{0}之间才可 */
	String PLAY_REMARK_EVERYDAY_PERIOD = "play.remark.everyday.period";
	/** 周一 */
	String REMARK_NEXT_MONDAY = "remark.next.monday";
	/** 周二 */
	String REMARK_NEXT_TUESDAY = "remark.next.tuesday";
	/** 周三 */
	String REMARK_NEXT_WEDNESDAY = "remark.next.wednesday";
	/** 周四 */
	String REMARK_NEXT_THURSDAY = "remark.next.thursday";
	/** 周五 */
	String REMARK_NEXT_FRIDAY = "remark.next.friday";
	/** 周六 */
	String REMARK_NEXT_SATURDAY = "remark.next.saturday";
	/** 周日 */
	String REMARK_NEXT_SUNDAY = "remark.next.sunday";
	/** 下周{0}后才可 */
	String PLAY_REMARK_NEXT_WEEK = "play.remark.next.week";
	/** 每周{0}之间才可 */
	String PLAY_REMARK_WEEKLY_PERIOD = "play.remark.weekly.period";
	/** 下月{0}日{1}后才可 */
	String PLAY_REMARK_NEXT_MONTH = "play.remark.next.month";
	/** 每月{0}日{1}之间才可 */
	String PLAY_REMARK_MONTHLY_PERIOD = "play.remark.monthly.period";
	/** 直接作废 */
	String PLAY_REMARK_EXPIRED = "play.remark.expired";
	/** 自动派发到账 */
	String REMARK_EXPIRED_AUTO = "remark.expired.auto";

	/** 1.账号首次充值专享，只有一次机会，充值越多，奖励越多，最高奖励{0}； */
	String RECHARGE_REMARK_FIRST = "recharge.remark.first";
	/** 1.活动期间，每日首次充值将获得额外奖励，充值越多，奖励越多，最高奖励{0}； */
	String RECHARGE_REMARK_DAILY_FIRST = "recharge.remark.daily.first";
	/** 1.活动期间，{0}累计充值将获得不同程度的奖励，充值越多，奖励越多，最高奖励{1}； */
	String RECHARGE_REMARK_TOTAL = "recharge.remark.total";
	/** 1.活动期间，{0}单笔充值将获得不同程度的奖励，充值越多，奖励越多，最高奖励{1}； */
	String RECHARGE_REMARK_SINGLE = "recharge.remark.single";
	/** 1.活动期间内充值次数以及充值金额满足活动要求，可获得对应奖励，最高奖励{0}； */
	String RECHARGE_REMARK_TIMES = "recharge.remark.times";
	/** 2.充值不限充值方式，若充值成功后，未获得奖励，可尝试刷新奖励或等待几分钟后再查看； */
	String RECHARGE_REMARK_TWO = "recharge.remark.two";
	/** 1.发展好友开{0},完成不同人数,可获得对应{0},金额最高{1},发展越多,奖励越高;<br/>2.此活动属平台额外赠送,同时享有其他代理奖励和佣金,即直接享受多倍快乐,每个周期内{2},满足活动条件才算有效; */
	String AGENT_REMARK_FIRST = "agent.remark.first";
	/** 1.发展好友绑定手机,完成不同人数,可获得对应奖励展越多,奖励越高;<br/>2.此活动属平台额外赠送,同时享有其他代理奖励和佣金,即直接享受多倍快乐,满足活动条件才算有效; */
	String AGENT_REMARK_REG_FIRST = "agent.remark.reg.first";
	/** 宝箱 */
	String BOX = "box";
	/** 红包 */
	String RED_POCKET = "red.pocket";
	/** 历史下级 */
	String AGENT_REMARK_HISTORY_USER = "agent.remark.history.user";
	/** 仅限周期内新用户下级 */
	String AGENT_REMARK_REGISTER_USER = "agent.remark.register.user";
	/** 3.下级会员的有效投注{0} */
	String AGENT_REMARK_SECOND = "agent.remark.second";
	/** {0}.奖励仅限在{1}端{2}; */
	String AGENT_REMARK_FOUR = "agent.remark.four";
	/** 1.晋级标准：满足VIP晋级要求（即充值或有效投注都满足条件），即可晋级相应VIP等级，获得相应晋级奖金，如连续晋级多级，可获得全部等级晋级奖金，晋级奖金没有过期时间； */
	String VIP_REMARK_ONE = "vip.remark.one";
	/** 2.日俸禄：每日充值及有效投注满足当前等级日俸禄要求，可获得对应日俸禄奖金，如连续晋级多级，仅可获得当前等级日俸禄奖金，{0}可领取，周期结束{1}后过期； */
	String VIP_REMARK_TWO = "vip.remark.two";
	/** 3.周俸禄：每周充值及有效投注满足当前等级周俸禄要求，可获得对应周俸禄奖金，如连续晋级多级，仅可获得当前等级周俸禄奖金，{0}可领取，周期结束{1}后过期； */
	String VIP_REMARK_THIRD = "vip.remark.third";
	/** 4.月俸禄：每月充值及有效投注满足当前等级月俸禄要求，可获得对应月俸禄奖金，如连续晋级多级，仅可获得当前等级月俸禄奖金，{0}可领取；周期结束{1}后过期； */
	String VIP_REMARK_FOUR = "vip.remark.four";
	/** 奖金发放后即 */
	String VIP_REMARK_TWO_MIDDLE = "vip.remark.two.middle";
	/** 今日00:00:00以后才 */
	String VIP_REMARK_TWO_LATTER_DAY = "vip.remark.two.latter.day";
	/** 每周周一00:00:00以后才 */
	String VIP_REMARK_TWO_LATTER_WEEK = "vip.remark.two.latter.week";
	/** 每月1日00:00:00以后才 */
	String VIP_REMARK_TWO_LATTER_MONTH = "vip.remark.two.latter.month";
	/** 5.奖励过期说明：奖励过期{0}，可在领取记录进行查看 */
	String VIP_REMARK_FIVE = "vip.remark.five";
	/** 自动作废 */
	String VIP_REMARK_FIVE_NULLIFY = "vip.remark.five.nullify";
	/** 自动派发到账 */
	String VIP_REMARK_FIVE_AUTO = "vip.remark.five.auto";
	/** {0}.保级说明：会员在达到某VIP等级后，若上个月充值及有效投注未达到保级要求，则自动降一个等级； */
	String VIP_REMARK_SIX = "vip.remark.six";
	/** {0}.稽核说明：VIP所赠送的奖金需{1}倍流水（即稽核或有效投注）才能提现； */
	String VIP_REMARK_SEVEN = "vip.remark.seven";
	/** {0}.活动声明：本功能仅限账号本人进行正常游戏投注，禁止租借账号、无风险投注(对赌、对刷、低赔刷水)、恶意套利、使用外挂程式、机器人、利用协议、漏洞、接口、群控或其他技术手段参与，一经查核属实，本平台有权终止会员登录、暂停会员使用网站、及没收奖金和不当盈利的权利，无需特别通知； */
	String VIP_REMARK_EIGHT = "vip.remark.eight";
	/** {0}.解释说明：会员领取VIP奖励时，本平台将默认会员同意且遵守对应条件等相关规定，为避免文字理解歧义，本平台保有本活动最终解释权。 */
	String VIP_REMARK_NINE = "vip.remark.nine";
	/**
	 * {@code
	 * <br/>仅限于:<br/>{0}
	 * }
	 */
	String REMARK_AUDIT_LIMIT = "remark.audit.limit";
	/** 投注不限游戏平台 */
	String REMARK_PLAY_UNRESTRICTED_GAME_PLATFORMS = "remark.play.unrestricted.game.platforms";
	/** 所有游戏 */
	String REMARK_ALL_GAME = "remark.all.game";

	/** 1.每日每个时间段单个最高0,10，全凭运气，先到先得，抢完为止！ */
	String RED_ENVELOPE_REMARK_1 = "redEnvelope.remark_1";
	String RED_ENVELOPE_REMARK_2 = "redEnvelope.remark_2";
	String RED_ENVELOPE_REMARK_3_1 = "redEnvelope.remark_3_1";
	String RED_ENVELOPE_REMARK_3_2 = "redEnvelope.remark_3_2";
	String RED_ENVELOPE_REMARK_4 = "redEnvelope.remark_4";
	String RED_ENVELOPE_REMARK_4_1 = "redEnvelope.remark_4_1";
	String RED_ENVELOPE_REMARK_4_2 = "redEnvelope.remark_4_2";
	String RED_ENVELOPE_REMARK_4_3 = "redEnvelope.remark_4_3";
	String RED_ENVELOPE_REMARK_4_3_1 = "redEnvelope.remark_4_3_1";
	String RED_ENVELOPE_REMARK_4_3_2 = "redEnvelope.remark_4_3_2";
	String RED_ENVELOPE_REMARK_4_3_3 = "redEnvelope.remark_4_3_3";
	String RED_ENVELOPE_REMARK_4_4_1 = "redEnvelope.remark_4_4_1";
	String RED_ENVELOPE_REMARK_4_4_2 = "redEnvelope.remark_4_4_2";
	String RED_ENVELOPE_REMARK_4_5_1 = "redEnvelope.remark_4_5_1";
	String RED_ENVELOPE_REMARK_4_5_2 = "redEnvelope.remark_4_5_2";
	String RED_ENVELOPE_REMARK_4_6_1 = "redEnvelope.remark_4_6_1";
	String RED_ENVELOPE_REMARK_4_6_2 = "redEnvelope.remark_4_6_2";
	String RED_ENVELOPE_REMARK_4_7_1 = "redEnvelope.remark_4_7_1";
	String RED_ENVELOPE_REMARK_4_7_2 = "redEnvelope.remark_4_7_2";
	String RED_ENVELOPE_REMARK_4_8 = "redEnvelope.remark_4_8";
	String RED_ENVELOPE_REMARK_4_9 = "redEnvelope.remark_4_9";
	String RED_ENVELOPE_REMARK_5 = "redEnvelope.remark_5";
	String RED_ENVELOPE_REMARK_5_1 = "redEnvelope.remark_5_1";
	String RED_ENVELOPE_REMARK_5_2 = "redEnvelope.remark_5_2";
	String RED_ENVELOPE_REMARK_5_3 = "redEnvelope.remark_5_3";
	String RED_ENVELOPE_REMARK_6_1 = "redEnvelope.remark_6_1";
	String RED_ENVELOPE_REMARK_6_2 = "redEnvelope.remark_6_2";
	String RED_ENVELOPE_REMARK_7_1 = "redEnvelope.remark_7_1";
	String RED_ENVELOPE_REMARK_7_2 = "redEnvelope.remark_7_2";
	String RED_ENVELOPE_REMARK_8 = "redEnvelope.remark_8";
	String RED_ENVELOPE_REMARK_9 = "redEnvelope.remark_9";
	String RED_ENVELOPE_REMARK_10 = "redEnvelope.remark_10";
	String RED_ENVELOPE_REMARK_11 = "redEnvelope.remark_11";
	String RED_ENVELOPE_REMARK_12 = "redEnvelope.remark_12";
	String RED_ENVELOPE_REMARK_13 = "redEnvelope.remark_13";
	String RED_ENVELOPE_REMARK_14 = "redEnvelope.remark_14";
	String RED_ENVELOPE_REMARK_15 = "redEnvelope.remark_15";

	/** 1.发展好友,进入游戏投注,发展越多,奖励越高;2.此活动属平台额外赠送,同时享有其他代理奖励和佣金,即直接享受多倍快乐。 */
	String CHALLENGE_INVITE_REMARK_FIRST = "challenge.invite.remark.first";

	/** 利息宝结算周期分钟 */
	String USER_INCOME_PERIOD_MINUTES = "user.income.period.minutes";
	/** 利息宝结算周期小时 */
	String USER_INCOME_PERIOD_HOURS = "user.income.period.hours";
	/** 利息宝结算周期无限制 */
	String USER_INCOME_MAX_PERIOD_UNLIMITED = "user.income.max.period.unlimited";

	/** <strong>1、</strong><strong>收益介绍：</strong>存入利息宝的金额，至少满足一个完整周期才能产生利息，若中途提前转出则该周期不计算收益，例如：当前结算周期为{0}分钟，则2025/01/01 00:00:01转入的金额，将在{1}产生第一个周期利息;<br/> */
	String USER_INCOME_REMARK_1 = "userIncome.remark_1";
	/** <strong>2、</strong><strong>结算周期：</strong>当前利息的结算周期为{0}分钟;<br/> */
	String USER_INCOME_REMARK_2 = "userIncome.remark_2";
	/** <strong>3、</strong><strong>年利率：</strong>当前年利率为{0}%;<br/><strong>4、</strong><strong>计算公式：</strong>利息收益=存入金额 * 年利率 / 结算周期;<br/> */
	String USER_INCOME_REMARK_3 = "userIncome.remark_3";
	/** <strong>5、</strong><strong>举例说明：</strong>A于2025/01/01 00:00:01存入10.000，年利率为{0}%，结算周期为{1}分钟，则于{2}获得首次利息收益，计算方式如下:首次利息 =10.000*{3}%/365天/24小时/60分钟*{4}= {5}，最低领取金额需≥{6}，即当利息≥{7}后，客户端才能领取;<br/> */
	String USER_INCOME_REMARK_4 = "userIncome.remark_4";
	/** <strong>6、</strong><strong>转入门槛：</strong>每次转入金额必须大于等于{0}（即≥{1}），转入金额无上限，越多收益越大;<br/> */
	String USER_INCOME_REMARK_5 = "userIncome.remark_5";
	/** <strong>7、</strong><strong>利息封顶：</strong>{0};<br/> */
	String USER_INCOME_REMARK_6 = "userIncome.remark_6";
	/** 当前利息封顶最多只能结算{0}个周期，超出则不再产生利息，记得定期或经常来领取收益，以免错过更多收益哦 */
	String USER_INCOME_REMARK_6_1 = "userIncome.remark_6_1";
	/** 当前没有利息封顶限制 */
	String USER_INCOME_REMARK_6_2 = "userIncome.remark_6_2";
	/** <strong>8、</strong><strong>领取时间：</strong>{0};<br/> */
	String USER_INCOME_REMARK_7 = "userIncome.remark_7";
	/** 当前为隔天领取，即当天产生的利息，要等到第二天0点后才能领取 */
	String USER_INCOME_REMARK_7_1 = "userIncome.remark.7_1";
	/** 实时领取：当前为实时领取 */
	String USER_INCOME_REMARK_7_2 = "userIncome.remark.7_2";
	/** <strong>9、</strong><strong>稽核倍数：</strong>当前稽核倍数为{0}倍（投注流水要求），即所领取的利息，需要进行投注后，才能提现，投注不限有效平台，仅限于所领取的利息需要稽核，所转入转出的本金则无稽核要求;<br/> */
	String USER_INCOME_REMARK_8 = "userIncome.remark_8";
	/** <strong>10、</strong>本功能仅限账号本人进行正常游戏投注，禁止租借账号、无风险投注(对赌、对刷、低赔刷水)、恶意套利、使用外挂程式、机器人、利用协议、漏洞、接口、群控或其他技术手段参与，一经查核属实，本平台有权终止会员登录、暂停会员使用网站、及没收奖金和不当盈利的权利，无需特别通知;<br/> */
	String USER_INCOME_REMARK_9 = "userIncome.remark_9";
	/** <strong>11、</strong><strong>解释说明：</strong>会员领取利息宝奖励时，本平台将默认会员同意且遵守对应条件等相关规定，为避免文字理解歧义，本平台保有本活动最终解释权。<br/> */
	String USER_INCOME_REMARK_10 = "userIncome.remark_10";

	/** 建设中的站点只能注册不超过3个用户 */
	String MERCHANT_INIT_OVER_USER_NUM = "merchant.init.over.user.num";

	/** 攒金转盘 系统规则国际化 */
	String GOLD_REMARK_1 = "gold.remark_1";
	String GOLD_REMARK_1_1 = "gold.remark_1_1";
	String GOLD_REMARK_1_2 = "gold.remark_1_2";
	String GOLD_REMARK_1_3 = "gold.remark_1_3";
	String GOLD_REMARK_2 = "gold.remark_2";
	String GOLD_REMARK_2_1 = "gold.remark_2_1";
	String GOLD_REMARK_2_2 = "gold.remark_2_2";
	String GOLD_REMARK_3_1 = "gold.remark_3_1";
	String GOLD_REMARK_3_2 = "gold.remark_3_2";
	String GOLD_REMARK_3_3 = "gold.remark_3_3";
	String GOLD_REMARK_3_4 = "gold.remark_3_4";
	String GOLD_REMARK_3_5 = "gold.remark_3_5";
	String GOLD_REMARK_3_6 = "gold.remark_3_6";
	String GOLD_REMARK_3_7 = "gold.remark_3_7";
	String GOLD_REMARK_3_8 = "gold.remark_3_8";
	String GOLD_REMARK_4_1 = "gold.remark_4_1";
	String GOLD_REMARK_4_2 = "gold.remark_4_2";
	String GOLD_REMARK_4_3 = "gold.remark_4_3";
	String GOLD_REMARK_4_4 = "gold.remark_4_4";
	String GOLD_REMARK_5 = "gold.remark_5";
	String GOLD_REMARK_6 = "gold.remark_6";

	/** 任务中心-新人福利任务 系统规则国际化 */
	String TASK_REMARK_1 = "task.remark_1";
	String TASK_REMARK_2 = "task.remark_2";
	String TASK_REMARK_3 = "task.remark_3";
	String TASK_REMARK_3_1 = "task.remark_3_1";
	String TASK_REMARK_4 = "task.remark_4";
	String TASK_REMARK_5 = "task.remark_5";
	String TASK_REMARK_6 = "task.remark_6";
	String TASK_REMARK_6_1 = "task.remark_6_1";
	String TASK_REMARK_6_2 = "task.remark_6_2";
	String TASK_REMARK_7 = "task.remark_7";

	String TASK_REMARK_DAY_1 = "task.remark_day_1";
	String TASK_REMARK_DAY_2 = "task.remark_day_2";
	String TASK_REMARK_DAY_3 = "task.remark_day_3";
	String TASK_REMARK_DAY_3_1 = "task.remark_day_3_1";
	String TASK_REMARK_DAY_3_2 = "task.remark_day_3_2";
	String TASK_REMARK_DAY_3_3 = "task.remark_day_3_3";
	String TASK_REMARK_DAY_4 = "task.remark_day_4";
	String TASK_REMARK_DAY_5 = "task.remark_day_5";
	String TASK_REMARK_DAY_5_1 = "task.remark_day_5_1";
	String TASK_REMARK_DAY_6 = "task.remark_day_6";
	String TASK_REMARK_DAY_7 = "task.remark_day_7";
	/** 任务中心 */
	String SYS_INTERNAL_MODULE_TASK = "sys.internal.module.task";
	/** 找到我们 */
	String SYS_INTERNAL_MODULE_FIND_US = "sys.internal.module.find.us";
	/** 复制推广链接 */
	String SYS_INTERNAL_MODULE_COPY_AGENT_LINK = "sys.internal.module.copy.agent.link";
	/** 用户修改 */
	String USER_EDIT_WITHOUT_ANY_MODIFICATION = "user.edit.without.any.modification";
	String USER_EDIT_SUCCESSFUL = "user.edit.success";
	/** 1.输入正确的兑换码可领取彩金，领取成功彩金直接到账<br/> */
	String REDEEM_CODE_REMARK_1 = "redeem.code.remark.1";
	/** 2.一个兑换码仅能兑换一次，兑换后即作废<br/> */
	String REDEEM_CODE_REMARK_2 = "redeem.code.remark.2";
	/** 3.发放的兑换码请在有效期内使用，过期使用该兑换码将会失效，且活动期间仅可兑换一次<br/> */
	String REDEEM_CODE_REMARK_3 = "redeem.code.remark.3";
	/** 4.本活动所赠送的奖金（稽核奖金）需{0}倍有效投注才能提现， */
	String REDEEM_CODE_REMARK_4 = "redeem.code.remark.4";
	/** 1.首提成功立返，提现更划算，最高返现比例{0}% */
	String FIRST_CASH_REPAY_REMARK_1 = "first.cash.repay.remark.1";
	/** 2.提现后再次充值获得加倍奖励，充值越多加倍越多 */
	String FIRST_CASH_REPAY_REMARK_2 = "first.cash.repay.remark.2";

	/** 神秘彩金活动 国际化 */
	String MYSTERY_REMARK_1 = "mystery.remark_1";
	String MYSTERY_REMARK_1_1 = "mystery.remark_1_1";
	String MYSTERY_REMARK_1_2 = "mystery.remark_1_2";
	String MYSTERY_REMARK_2 = "mystery.remark_2";
	String MYSTERY_REMARK_2_1 = "mystery.remark_2_1";
	String MYSTERY_REMARK_2_2 = "mystery.remark_2_2";
	String MYSTERY_REMARK_3 = "mystery.remark_3";
	String MYSTERY_REMARK_4 = "mystery.remark_4";
	String MYSTERY_REMARK_5 = "mystery.remark_5";
	String MYSTERY_REMARK_6 = "mystery.remark_6";

}