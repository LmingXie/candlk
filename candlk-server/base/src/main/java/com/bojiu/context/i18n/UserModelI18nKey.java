package com.bojiu.context.i18n;

import com.bojiu.context.model.BaseI18nKey;

public interface UserModelI18nKey extends BaseI18nKey {
	// Gender
	/** 女 */
	String GENDER_FEMALE = "gender.female";
	/** 男 */
	String GENDER_MALE = "gender.male";
	// BotSpyPunish
	/** 正常 */
	String BOT_SPY_PUNISH_NORMAL = "bot.spy.punish.normal";
	/** 禁止领取优惠 */
	String BOT_SPY_PUNISH_REWARD = "bot.spy.punish.reward";
	/** 冻结 */
	String BOT_SPY_PUNISH_FREEZE = "bot.spy.punish.freeze";
	/** 禁止进入游戏 */
	String BOT_SPY_PUNISH_OFF_GAME = "bot.spy.punish.off.game";
	/** 禁止注册 */
	String BOT_SPY_PUNISH_REGISTER = "bot.spy.punish.register";
	/** 禁止提现 */
	String BOT_SPY_PUNISH_CASH = "bot.spy.punish.cash";
	// BotSpyType
	/** 同IP */
	String BOT_SPY_TYPE_IP = "bot.spy.type.ip";
	/** 同设备号 */
	String BOT_SPY_TYPE_DEVICE = "bot.spy.type.device";
	/** 同提现名称 */
	String BOT_SPY_TYPE_CASH_NAME = "bot.spy.type.cash.name";
	// DownloadBarType
	/** 关闭按键 */
	String DOWNLOAD_BAR_TYPE_CLOSE_BUTTON = "download.bar.type.close.button";
	/** 游戏icon */
	String DOWNLOAD_BAR_TYPE_GAME_ICON = "download.bar.type.game.icon";
	/** 宣传图文 */
	String DOWNLOAD_BAR_TYPE_PROMOTIONAL = "download.bar.type.promotional";
	/** 下载按键 */
	String DOWNLOAD_BAR_TYPE_DOWN_BUTTON = "download.bar.type.down.button";
	/** 背景颜色 */
	String DOWNLOAD_BAR_TYPE_BACKGROUND_COLOR = "download.bar.type.background.color";
	/** 下载引导条ICON */
	String DOWNLOAD_BAR_TYPE_BANNER_ICON = "download.bar.type.banner_icon";
	/** 关闭按钮颜色 */
	String DOWNLOAD_BAR_TYPE_CLOSE_BUTTON_COLOR = "download.bar.type.close_button_color";
	/** 下载引导条背景颜色 */
	String DOWNLOAD_BAR_TYPE_BANNER_BG_COLOR = "download.bar.type.banner_bg_color";
	/** 下载引导条下载文案 */
	String DOWNLOAD_BAR_TYPE_DOWNLOAD_TEXT = "download.bar.type.download_text";
	/** 背景图片 */
	String DOWNLOAD_BAR_TYPE_BACK_PICTURE = "download.bar.type.back.picture";
	// MsgScope
	/** 全部登录会员 */
	String MSG_SCOPE_USER_ALL = "msg.scope.user.all";
	/** 自定义会员 */
	String MSG_SCOPE_USER_CUSTOM = "msg.scope.user.custom";
	/** 用户VIP等级 */
	String MSG_SCOPE_USER_LEVEL = "msg.scope.user.level";
	/** 会员层级 */
	String MSG_SCOPE_USER_AUTO_LAYER = "msg.scope.user.auto.layer";
	/** 会员层级 */
	String MSG_SCOPE_USER_FIXED_LAYER = "msg.scope.user.fixed.layer";
	/** 代理消息 */
	String MSG_SCOPE_AGENT = "msg.scope.agent";
	/** 全部商户 */
	String MSG_SCOPE_MERCHANT_ALL = "msg.scope.merchant.all";
	/** 自定义商户 */
	String MSG_SCOPE_MERCHANT_CUSTOM = "msg.scope.merchant.custom";
	/** 商户等级 */
	String MSG_SCOPE_MERCHANT_LEVEL = "msg.scope.merchant.level";
	// MsgType
	/** 消息 */
	String MSG_TYPE_MSG = "msg.type.msg";
	/** 公告 */
	String MSG_TYPE_NOTICE = "msg.type.notice";
	/** 跑马灯 */
	String MSG_TYPE_MARQUEE = "msg.type.marquee";
	/** 大厅弹窗 */
	String MSG_TYPE_POP_UP = "msg.type.pop.up";
	/** 维护公告 */
	String MSG_TYPE_MAINTENANCE_NOTICE = "msg.type.maintenance.notice";
	// PromotionType
	/** 充值 */
	String PROMOTION_TYPE_RECHARGE = "promotion.type.recharge";
	/** 打码 */
	String PROMOTION_TYPE_PLAY = "promotion.type.play";
	/** 签到 */
	String PROMOTION_TYPE_SIGN = "promotion.type.sign";
	/** 救援金 */
	String PROMOTION_TYPE_RELIEF = "promotion.type.relief";
	/** 幸运转盘 */
	String PROMOTION_TYPE_TURNTABLE = "promotion.type.turntable";
	/** 红包 */
	String PROMOTION_TYPE_RED_ENVELOPE = "promotion.type.red.envelope";
	/** 推广活动 */
	String PROMOTION_TYPE_AGENT = "promotion.type.agent";
	/** 推广注册 */
	String PROMOTION_TYPE_AGENT_REGISTER = "promotion.type.agent.register";
	/** 新人彩金 */
	String PROMOTION_TYPE_REDEEM_CODE = "promotion.type.redeem.code";
	/** 指定新人彩金（指定代理渠道） */
	String PROMOTION_TYPE_AGENT_REDEEM_CODE = "promotion.type.agent.redeem.code";
	/** 自定义 */
	String PROMOTION_TYPE_CUSTOM = "promotion.type.custom";
	/** 闯关邀请活动 */
	String PROMOTION_TYPE_CHALLENGE_INVITE = "promotion.type.challenge.invite";
	/** 闯关打码活动 */
	String PROMOTION_TYPE_CHALLENGE_PLAY = "promotion.type.challenge.play";
	/** 余额救援金 */
	String PROMOTION_TYPE_RELIEF_BALANCE = "promotion.type.relief.balance";
	/** 排行榜 */
	String PROMOTION_TYPE_RANK = "promotion.type.rank";
	/** 奖金转盘 */
	String PROMOTION_TYPE_REWARD_TURNTABLE = "promotion.type.reward.turntable";
	/** 攒金大转盘 */
	String PROMOTION_TYPE_GOLD_TURNTABLE = "promotion.type.gold.turntable";
	/** 公积金 */
	String PROMOTION_TYPE_DEPOSIT_POOL = "promotion.type.deposit.pool";
	/** 每日转盘 */
	String PROMOTION_TYPE_DAILY_TURNTABLE = "promotion.type.daily.turntable";
	/** 拼团 */
	String PROMOTION_TYPE_GROUP = "promotion.type.group";
	/** 奖池 */
	String PROMOTION_TYPE_JACKPOT = "promotion.type.jackpot";
	/** 首提返现 */
	String PROMOTION_TYPE_FIRST_CASH_REPAY = "promotion.type.first.cash.repay";
	/** 神秘彩金 */
	String PROMOTION_TYPE_MYSTERY_PRIZE = "promotion.type.mystery.prize";
	// PrivilegeType
	/** 每日转盘 */
	String PRIVILEGE_TYPE_TURNTABLE = "privilege.type.turntable";
	/** 每日闯关游戏 */
	String PRIVILEGE_TYPE_BREAKING_GAME = "privilege.type.breaking.game";
	/** 生日奖励 */
	String PRIVILEGE_TYPE_BIRTHDAY_REWARDS = "privilege.type.birthday.rewards";
	/** 升级奖励 */
	String PRIVILEGE_TYPE_UPGRADE_REWARDS = "privilege.type.upgrade.rewards";
	/** 每日奖励 */
	String PRIVILEGE_TYPE_DAILY_REWARDS = "privilege.type.daily.rewards";
	/** 每周奖励 */
	String PRIVILEGE_TYPE_WEEKLY_REWARDS = "privilege.type.weekly.rewards";
	/** 每月奖励 */
	String PRIVILEGE_TYPE_MONTH_REWARDS = "privilege.type.month.rewards";
	/** 每日提款次数限制 */
	String PRIVILEGE_TYPE_DAILY_CASH = "privilege.type.daily.cash";
	/** 每日提款总额限额 */
	String PRIVILEGE_TYPE_CASH_LIMIT = "privilege.type.cash.limit";
	/** 贵宾特权 */
	String PRIVILEGE_TYPE_VIP = "privilege.type.vip";
	/** VIP保级 */
	String PRIVILEGE_TYPE_RELEGATION = "privilege.type.relegation";
	/** 提款总次数限制 */
	String PRIVILEGE_TYPE_TOTAL_CASH_NUM = "privilege.type.total.cash.num";
	/** 提款总额限额 */
	String PRIVILEGE_TYPE_TOTAL_CASH_LIMIT = "privilege.type.total.cash.limit";
	/** 每日免手续费交易笔数 */
	String PRIVILEGE_TYPE_DAILY_FEE_NUM = "privilege.type.daily.fee.num";
	/** 特权限制 */
	String PRIVILEGE_TYPE_LIMIT = "privilege.type.limit";
	/** 特权笔数 */
	String PRIVILEGE_TYPE_NUM = "privilege.type.num";
	/** 每日提款次数 */
	String PRIVILEGE_TYPE_DAILY_CASH_TITLE = "privilege.type.daily.cash.title";
	/** 每日提款总额 */
	String PRIVILEGE_TYPE_CASH_TITLE = "privilege.type.cash.title";
	/** 提款总次数 */
	String PRIVILEGE_TYPE_TOTAL_CASH_NUM_TITLE = "privilege.type.total.cash.num.title";
	/** 提款总额 */
	String PRIVILEGE_TYPE_TOTAL_CASH_TITLE = "privilege.type.total.cash.title";
	/** 每日免手续费 */
	String PRIVILEGE_TYPE_DAILY_FEE_NUM_TITLE = "privilege.type.daily.fee.num.title";
	/** VIP限制提示 */
	String PRIVILEGE_TYPE_CASH_LIMIT_TIPS = "privilege.type.cash.limit.tips";
	// RewardStatus
	/** 未完成 */
	String REWARD_STATUS_UNFINISHED = "reward.status.unfinished";
	/** 领取 */
	String REWARD_STATUS_UNCLAIMED = "reward.status.unclaimed";
	/** 已领取 */
	String REWARD_STATUS_COLLECT = "reward.status.collect";
	/** 待审核 */
	String PENDING_STATUS_COLLECT = "reward.status.pending";
	/** 已通过 */
	String PASS_STATUS_COLLECT = "reward.status.pass";
	/** 已拒绝 */
	String REJECT_STATUS_COLLECT = "reward.status.reject";
	// SmsVendor
	/** 蚂蚁短信 */
	String SMS_VENDOR_ANT = "sms.vendor.ant";
	/** 颂量短信 */
	String SMS_VENDOR_ITNIO = "sms.vendor.itnio";
	// UserRechargeLevel
	/** 普通玩家 */
	String USER_RECHARGE_LEVEL_L0 = "user.recharge.level.l0";
	/** 小R玩家 */
	String USER_RECHARGE_LEVEL_L1 = "user.recharge.level.l1";
	/** 中R玩家 */
	String USER_RECHARGE_LEVEL_L2 = "user.recharge.level.l2";
	/** 大R玩家 */
	String USER_RECHARGE_LEVEL_L3 = "user.recharge.level.l3";
	// UserSource
	/** 官网 */
	String USER_SOURCE_H5 = "user.source.h5";
	/** 后台添加 */
	String USER_SOURCE_BACK = "user.source.back";
	/** 推广注册 */
	String USER_SOURCE_AGENT_INVITE = "user.source.agent.invite";
	/** 渠道注册 */
	String USER_SOURCE_CHANNEL_INVITE = "user.source.channel.invite";
	// LimitType
	/** 注册 */
	String LIMIT_TYPE_REGISTER = "limit.type.register";
	/** 登录 */
	String LIMIT_TYPE_LOGIN = "limit.type.login";
	/** 充值 */
	String LIMIT_TYPE_RECHARGE = "limit.type.recharge";
	/** 进入游戏 */
	String LIMIT_TYPE_GAMES = "limit.type.games";
	/** 领取优惠 */
	String LIMIT_TYPE_REWARD = "limit.type.reward";

	/*
	 * 以下放置 Form 相关的 key
	 */
	// MemberLoginForm
	/** 用户名 */
	String USERNAME = "username";
	/** 验证码 */
	String MEMBER_LOGIN_FORM_CAPTCHA = "member.login.form.captcha";
	/** Google身份验证码 */
	String MEMBER_LOGIN_FORM_GOOGLE_CODE = "member.login.form.google.code";
	/** 密码 */
	String MEMBER_LOGIN_FORM_PASSWORD = "member.login.form.password";
	/** 手机号码 */
	String MEMBER_LOGIN_FORM_PHONE = "member.login.form.phone";
	/** 用户名/手机号/邮箱 */
	String MEMBER_LOGIN_FORM_USERNAME = "member.login.form.username";
	/** 密码或短信验证码不能都为空！ */
	String MEMBER_LOGIN_FORM_CREDENTIAL_REQUIRED = "member.login.form.credential.required";
	// ChangePwdForm
	/** 新密码 */
	String CHANGE_PWD_FORM_NEW_PWD = "change.pwd.form.new.pwd";
	/** 旧密码 */
	String CHANGE_PWD_FORM_OLD_PWD = "change.pwd.form.old.pwd";
	// UserFeedBackForm
	/** 附件 */
	String USER_FEED_BACK_FORM_ATTACH = "user.feed.back.form.attach";
	/** 内容 */
	String USER_FEED_BACK_FORM_FEEDBACK_CONTENT = "user.feed.back.form.feedback.content";
	// UserEditForm
	/** {0}已存在 */
	String ALREADY_EXIST = "already.exist";
	/** {0}不存在 */
	String NOT_EXIST = "not.exist";
	/** 头像 */
	String USER_EDIT_FORM_AVATAR = "user.edit.form.avatar";
	/** 出生日期 */
	String USER_EDIT_FORM_BIRTHDAY = "user.edit.form.birthday";
	/** 电子邮箱 */
	String USER_EDIT_FORM_EMAIL = "user.edit.form.email";
	/** 性别 */
	String USER_EDIT_FORM_GENDER = "user.edit.form.gender";
	/** 昵称 */
	String USER_EDIT_FORM_NICKNAME = "user.edit.form.nickname";
	/** 姓名 */
	String USER_EDIT_FORM_REAL_NAME = "user.edit.form.real.name";

	/** 该手机号码已关联至其他账户。请检查并重新输入。 */
	String REGISTER_PHONE_ALREADY_EXIST = "register.phone.already.exist";
	// RewardCollectType
	/** 转盘奖励 */
	String REWARD_COLLECT_TYPE_TURNTABLE = "reward.collect.type.turntable";
	/** 闯关游戏奖励 */
	String REWARD_COLLECT_TYPE_CHALLENGE = "reward.collect.type.challenge";
	/** 生日奖励 */
	String REWARD_COLLECT_TYPE_BIRTHDAY = "reward.collect.type.birthday";
	/** 升级奖励 */
	String REWARD_COLLECT_TYPE_UPGRADE = "reward.collect.type.upgrade";
	/** 每日奖励 */
	String REWARD_COLLECT_TYPE_DAILY = "reward.collect.type.daily";
	/** 每周奖励 */
	String REWARD_COLLECT_TYPE_WEEKLY = "reward.collect.type.weekly";
	/** 每月奖励 */
	String REWARD_COLLECT_TYPE_MONTH = "reward.collect.type.month";
	/** 充值奖励 */
	String REWARD_COLLECT_TYPE_RECHARGE = "reward.collect.type.recharge";
	/** 打码奖励 */
	String REWARD_COLLECT_TYPE_PLAY = "reward.collect.type.play";
	/** 签到奖励 */
	String REWARD_COLLECT_TYPE_SIGN = "reward.collect.type.sign";
	/** 救援金奖励 */
	String REWARD_COLLECT_TYPE_RELIEF = "reward.collect.type.relief";
	/** 余额救赎金奖励 */
	String REWARD_COLLECT_TYPE_RELIEF_BALANCE = "reward.collect.type.relief.balance";
	/** 任务注册账号奖励 */
	String TASK_REWARD_REGISTER = "task.reward.register";
	/** 任务首笔充值/首充 */
	String TASK_REWARD_FIRST_RECHARGE = "task.reward.first.recharge";
	/** 任务保存桌面快捷方式奖励 */
	String TASK_REWARD_SAVE_SHORTCUT = "task.reward.save.shortcut";

	/** 任务每日累计充值奖励 */
	String TASK_REWARD_DAY_RECHARGE = "task.reward.day.recharge";
	/** 任务每日累计打码奖励 */
	String TASK_REWARD_DAY_PLAY = "task.reward.day.play";
	/** 任务每日单局大额打码奖励 */
	String TASK_REWARD_DAY_BIG_PLAY = "task.reward.day.big.play";

	/** 任务每周累计充值奖励 */
	String TASK_REWARD_WEEK_RECHARGE = "task.reward.week.recharge";
	/** 任务每周累计打码奖励 */
	String TASK_REWARD_WEEK_PLAY = "task.reward.week.play";
	/** 任务每周单局大额打码奖励 */
	String TASK_REWARD_WEEK_BIG_PLAY = "task.reward.week.big.play";

	/** 活跃度宝箱奖励 */
	String TASK_REWARD_ACTIVE_BOX = "task.reward.active";
	/** 任务中心通用（任务中心风控配置） */
	String TASK_RISK_CONFIG = "task.risk.config";
	/** 新人福利 */
	String TASK_NEWCOMER = "task.newcomer";
	/** 每日任务 */
	String TASK_DAY = "task.day";
	/** 每周任务 */
	String TASK_WEEK = "task.week";
	/** 活跃度宝箱 */
	String TASK_ACTIVE_BOX = "task.active";
	/** 设置提现密码 */
	String TASK_REWARD_CASH_PWD = "task.reward.cash.pwd";
	/** 设置生日 */
	String TASK_REWARD_BIND_BIRTHDAY = "task.reward.bind.birthday";
	/** 绑定邮箱 */
	String TASK_REWARD_BIND_EMAIL = "task.reward.bind.email";
	/** 绑定facebook */
	String TASK_REWARD_BIND_FACEBOOK = "task.reward.bind.facebook";
	/** 绑定telegram */
	String TASK_REWARD_BIND_TELEGRAM = "task.reward.bind.telegram";
	/** 绑定WhatsApp */
	String TASK_REWARD_BIND_WHATSAPP = "task.reward.bind.whatsapp";
	/** 绑定Google */
	String TASK_REWARD_BIND_GOOGLE = "task.reward.bind.google";
	/** 绑定Zalo */
	String TASK_REWARD_BIND_ZALO = "task.reward.bind.zalo";
	/** 绑定Instagram */
	String TASK_REWARD_BIND_INSTAGRAM = "task.reward.bind.instagram";
	/** 绑定Line */
	String TASK_REWARD_BIND_LINE = "task.reward.bind.line";
	/** 绑定Twitch */
	String TASK_REWARD_BIND_TWITCH = "task.reward.bind.twitch";
	/** 绑定theads */
	String TASK_REWARD_BIND_THEADS = "task.reward.bind.theads";
	/** 设置头像 */
	String TASK_REWARD_BIND_AVATAR = "task.reward.bind.avatar";
	/** 首次提现（成功才算） */
	String TASK_REWARD_FIRST_CASH = "task.reward.first.cash";
	/** 绑定提现账户 */
	String TASK_REWARD_BIND_CASH = "task.reward.bind.cash";
	/** 绑定手机号 */
	String TASK_REWARD_BIND_PHONE = "task.reward.bind.phone";
	/** 首次下载安装并登录APP任务 */
	String TASK_REWARD_LOGIN_APP = "task.reward.login.app";
	/** 绑定Google身份验证器 */
	String TASK_REWARD_BIND_GOOGLE_AUTH = "task.reward.bind.google_auth";
	// Country
	/** 巴西 */
	String COUNTRY_BR = "country.br";
	/** 马来西亚 */
	String COUNTRY_MY = "country.my";
	/** 菲律宾 */
	String COUNTRY_PH = "country.ph";
	/** 印尼 */
	String COUNTRY_ID = "country.id";
	/** 印度 */
	String COUNTRY_IN = "country.in";
	/** 越南 */
	String COUNTRY_VN = "country.vn";
	/** 综合 */
	String GAME_TYPE_ALL = "@game.type.all";
	// GameType
	/** 棋牌 */
	String GAME_TYPE_TABLE = "@game.type.table";
	/** 捕鱼 */
	String GAME_TYPE_FISH = "@game.type.fish";
	/** 电子 */
	String GAME_TYPE_DIGITAL = "@game.type.digital";
	/** 电竞 */
	String GAME_TYPE_E_SPORTS = "@game.type.e.sports";
	/** 体育 */
	String GAME_TYPE_SPORT = "@game.type.sport";
	/** 视讯 */
	String GAME_TYPE_LIVE = "@game.type.live";
	/** 彩票 */
	String GAME_TYPE_LOTTERY = "@game.type.lottery";
	/** 区块链 */
	String GAME_TYPE_BLOCKCHAIN = "@game.type.blockchain";
	/** 热门 */
	String GAME_HOT = "@game.hot";
	/** 最近游戏 */
	String GAME_LAST = "@game.last";
	/** 个人收藏 */
	String GAME_FAVORITE = "@game.favorite";
	// OauthStatus
	/** 已注销 */
	String OAUTH_STATUS_UNBIND = "oauth.status.unbind";
	/** 待绑定 */
	String OAUTH_STATUS_WAIT_BIND = "oauth.status.wait.bind";
	/** 已绑定 */
	String OAUTH_STATUS_BIND = "oauth.status.bind";
	/** 未开始 */
	String NOT_START = "not.start";
	/** 进行中 */
	String PROCEED = "proceed";
	/** 已完成 */
	String COMPLETED = "completed";
	/** 手动解除 */
	String MANUAL_RELEASE = "manual.release";
	/** 系统解除 */
	String SYSTEM_RELEASE = "system.release";
	/** 未知 */
	String UNKNOWN = "unknown";

	// PromotionCond
	/** 账号首充 */
	String PROMOTION_COND_FIRST_RECHARGE = "promotion.cond.first.recharge";
	/** 每日首充 */
	String PROMOTION_COND_DAILY_FIRST_RECHARGE = "promotion.cond.daily.first.recharge";
	/** 累计充值 */
	String PROMOTION_COND_TOTAL_RECHARGE = "promotion.cond.total.recharge";
	/** 单笔充值 */
	String PROMOTION_COND_SINGLE_RECHARGE = "promotion.cond.single.recharge";
	/** 二充 */
	String PROMOTION_COND_SECOND_RECHARGE = "promotion.cond.second.recharge";
	/** 三充 */
	String PROMOTION_COND_THIRD_RECHARGE = "promotion.cond.third.recharge";
	/** 四充 */
	String PROMOTION_COND_FOUR_RECHARGE = "promotion.cond.four.recharge";
	/** 五充 */
	String PROMOTION_COND_FIVE_RECHARGE = "promotion.cond.five.recharge";
	/** 闯关邀请奖励 */
	String REWARD_COLLECT_TYPE_CHALLENGE_INVITE = "reward.collect.type.challenge.invite";
	/** 闯关打码奖励 */
	String REWARD_COLLECT_TYPE_CHALLENGE_PLAY = "reward.collect.type.challenge.play";
	/** 灵动 */
	String TEMPLATE_TYPE_EUROPE = "template.type.europe";
	/** 经典 */
	String TEMPLATE_TYPE_CLASSIC = "template.type.classic";
	/** 亚太 */
	String TEMPLATE_TYPE_ASIA = "template.type.asia";
	/** WG新欧美(综合版2) */
	String TEMPLATE_TYPE_WG_EUROPE = "template.type.wg.europe";
	/** 定制版 */
	String TEMPLATE_TYPE_CUSTOMIZED = "template.type.customized";
	/** 欧美简约 */
	String TEMPLATE_TYPE_EUROPE_SIMPLIFIED = "template.type.europe.simplified";
	/** panda */
	String TEMPLATE_TYPE_PANDA = "template.type.panda";
	/** 综合版1 */
	String COMPREHENSIVE_VERSION_ONE = "comprehensive.version.one";
	/** 综合版3 */
	String COMPREHENSIVE_VERSION_THIRD = "comprehensive.version.third";
	/** 定制版1 */
	String TEMPLATE_TYPE_CUSTOMIZED_ONE = "template.type.customized.one";
	/** 综合版4 */
	String COMPREHENSIVE_VERSION_FOUR = "comprehensive.version.four";
	/** 综合版5 */
	String COMPREHENSIVE_VERSION_FIVE = "comprehensive.version.five";

	/** 皮肤蓝紫色 */
	String COLOR_TYPE_BLUE_PURPLE = "color.type.blue.purple";

	/** 活动 */
	String MARKET_TYPE_PROMOTION = "market.type.promotion";
	/** 任务 */
	String MARKET_TYPE_TASK = "market.type.task";
	/** 其它 */
	String MARKET_TYPE_OTHER = "market.type.other";
	/** 领奖设备-H5 */
	String COLLECT_LIMIT_H5 = "collect.limit.h5";
	/** 领奖设备-android */
	String COLLECT_LIMIT_ANDROID = "collect.limit.android";
	/** 领奖设备-ios */
	String COLLECT_LIMIT_IOS = "collect.limit.ios";
	/** 设备限制-禁止同设备号重复领取 */
	String COLLECT_LIMIT_SAME_IP = "collect.limit.same.ip";
	/** 设备限制-禁止同设备号重复领取 */
	String COLLECT_LIMIT_SAME_DEVICE = "collect.limit.same.device";
	/** 绑定限制-已绑定收款方 */
	String COLLECT_LIMIT_ACCOUNT_NUMBER = "collect.limit.account.number";
	/** 绑定限制-已绑定手机号码 */
	String COLLECT_LIMIT_PHONE = "collect.limit.phone";
	/** 绑定限制-设置姓名 */
	String COLLECT_LIMIT_REAL_NAME = "collect.limit.real.name";
	/** 绑定限制-邮箱 */
	String COLLECT_LIMIT_EMAIL = "collect.limit.email";
	/** 绑定限制-FACEBOOK */
	String COLLECT_LIMIT_FACEBOOK = "collect.limit.facebook";
	/** 绑定限制-TELEGRAM */
	String COLLECT_LIMIT_TELEGRAM = "collect.limit.telegram";
	/** 绑定限制-WHATSAPP */
	String COLLECT_LIMIT_WHATSAPP = "collect.limit.whatsapp";
	/** 绑定限制-Zalo */
	String COLLECT_LIMIT_ZALO = "collect.limit.zalo";
	/** 绑定限制-Instagram */
	String COLLECT_LIMIT_INSTAGRAM = "collect.limit.instagram";
	/** 绑定限制-Line */
	String COLLECT_LIMIT_LINE = "collect.limit.line";
	/** 绑定限制-Twitch */
	String COLLECT_LIMIT_TWITCH = "collect.limit.twitch";
	/** 绑定限制-Theads */
	String COLLECT_LIMIT_THEADS = "collect.limit.theads";
	/** 绑定限制-生日 */
	String COLLECT_LIMIT_BIRTHDAY = "collect.limit.birthday";
	/** 不在领取时间范围内 */
	String COLLECT_TIME_LIMIT = "collect.time.limit";
	// ChannelPosition
	/** 主域名 */
	String CHANNEL_POSITION_FRONT = "channel.position.front";
	/** 仿Google页 */
	String CHANNEL_POSITION_GOOGLE = "channel.position.google";
	/** H5页面 */
	String CHANNEL_POSITION_HOME = "channel.position.home";
	/** 短信营销 */
	String CHANNEL_POSITION_MOBILE_MARKETING = "channel.position.mobile.marketing";
	/** 右下角悬浮 */
	String MODULE_POSITION_RIGHT_BOTTOM = "module.position.right.bottom";
	/** 左下角悬浮 */
	String MODULE_POSITION_LEFT_BOTTOM = "module.position.left.bottom";
	/** 网页顶部下方 */
	String MODULE_POSITION_TOP_BOTTOM = "module.position.top.bottom";
	/** Banner图下方 */
	String MODULE_POSITION_BANNER_BOTTOM = "module.position.banner.bottom";
	/** 跑马灯下面 */
	String MODULE_POSITION_MARQUEE_BOTTOM = "module.position.marquee.bottom";
	/** 三方游戏最上方 */
	String MODULE_POSITION_THIRD_GAME_TOP = "module.position.third.game.top";
	/** 热门游戏上方 */
	String MODULE_POSITION_HOT_GAME_TOP = "module.position.hot.game.top";
	/** 热门游戏下方 */
	String MODULE_POSITION_HOT_GAME_BOTTOM = "module.position.hot.game.bottom";
	/** 三方游戏最下方 */
	String MODULE_POSITION_THIRD_GAME_BOTTOM = "module.position.third.game.bottom";
	/** 首页底部上方 */
	String MODULE_POSITION_HOME_BOTTOM_TOP = "module.position.home.bottom.top";
	/** 首页最底部 */
	String MODULE_POSITION_HOME_BOTTOM = "module.position.home.bottom";
	/** 定制模块1 */
	String MODULE_POSITION_HOME_CUSTOMIZED_MODULE_1 = "module.position.home.customized.module.1";
	/** 用户消息状态 */
	String USER_MSG_READ = "user.msg.read";
	String USER_MSG_UN_READ = "user.msg.unRead";

}