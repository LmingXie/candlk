package com.bojiu.context.i18n;

import com.bojiu.context.model.BaseI18nKey;

public interface AdminI18nKey extends BaseI18nKey {

	/** 用户不存在 */
	String USER_NOT_EXIST = "user.not.exist";
	/** 删除成功！ */
	String DELETE_SUCCESS = "delete.success";
	/** 删除失败！ */
	String DELETE_FAIL = "delete.fail";
	/** 更新失败！ */
	String UPDATE_FAIL = "update.fail";
	/** 更新成功！ */
	String UPDATE_SUCCESS = "update.success";
	/** 编辑成功 */
	String EDIT_SUCCESS = "edit.success";
	/** 编辑失败！ */
	String EDIT_FAIL = "edit.fail";
	/** 添加成功! */
	String ADD_SUCCESS = "add.success";
	/** 批量添加失败！ */
	String BATCH_ADD_FAIL = "batch.add.fail";
	/** 请选择删除的数据！ */
	String DELETE_NOT_SELECTED = "delete.not.selected";
	/** 该名称已存在，请重新编辑！ */
	String NAME_EXIST = "name.exist";
	/** 配置不能为空! */
	String CONFIG_NOT_NULL = "config.not.null";
	/** 已存在相同的配置 */
	String CONFIG_DUPLICATE = "config.duplicate";
	/** 必须先进行基础配置 */
	String CONFIG_NOT_FIRST = "config.not.first";
	/** 代理账户不存在~ */
	String AGENT_ACCOUNT_NOT_EXIST = "agent.account.not.exist";
	/** 该类型已存在，无需重复添加！ */
	String TYPE_EXIST = "type.exist";
	/** 正在处理中，请稍后重试。 */
	String PROCESSING = "processing";
	/** 该功能暂不可用！ */
	String FEATURE_NOT_AVAILABLE = "feature.not.available";
	/** 请输入用户ID */
	String UID_VALID = "uid.valid";
	/** 正在刷新中，请稍后重试。 */
	String REFRESHING = "refreshing";
	/** 审核意见不能为空！ */
	String REVIEW_REMARK_NOT_NULL = "review.remark.not.null";
	/** 跨地域发放商户推广佣金失败 */
	String TRANS_REGIONAL_GRANT_INVITE_REWARD = "trans.regional.grant.invite.reward";
	/** 该序号已存在，请核实！ */
	String SEQ_EXIST = "seq.exist";
	/** 投注业绩 */
	String BET_PERFORMANCE = "bet.performance";
	/** 有效人数 */
	String VALID_USER_COUNT = "valid.user.count";
	/** 每万返佣 */
	String BET_REBATE = "bet.rebate";
	/** 净盈利 */
	String NET_PROFIT = "net.profit";

	// ============================= MERCHANT ========================================

	/** 该会员不存在！ */
	String ACCOUNT_VALID_USER = "account.not.exist";
	/** PIX账号与CPF必须相等~ */
	String ACCOUNT_EDIT_VALID_PIX = "account.edit.valid.pix";
	/** 请上传要修正的会员信息 */
	String ACCOUNT_ADJUST_VALID_USER = "account.adjust.valid.user";
	/** 该支付通道最多只能绑定{0}个收款账号 */
	String ACCOUNT_EDIT_VALID_MAX_COUNT = "account.edit.valid.max.count";
	/** 账号已存在 */
	String ACCOUNT_EDIT_VALID_EXIST = "account.edit.valid.exist";
	/** 只能添加一个CPF！ */
	String ACCOUNT_EDIT_VALID_CPF_ONE = "account.edit.valid.cpf.one";
	/** 请输入有效的收款账号! */
	String ACCOUNT_EDIT_VALID_ACCOUNT = "account.edit.valid.account";
	/** 收款CPF账号已经存在 */
	String ACCOUNT_EDIT_VALID_CPF_EXIST = "account.edit.valid.cpf.exist";
	/** 账号名已存在，请更换账号！ */
	String ACCOUNT_NAME_EXIST = "account.name.exist";
	/** 会员ID已存在账号，无需重复配置！ */
	String ACCOUNT_UID_EXIST = "account.uid.exist";
	/** 非管辖会员 */
	String ACCOUNT_IMPORT_UID_OUT_OF_SCOPE = "account.import.uid.out.of.scope";
	/** 交易类型输入错误； */
	String ACCOUNT_IMPORT_TYPE_ERROR = "account.import.type.error";
	/** 修正金额不能超过限定额度：{0} */
	String ACCOUNT_IMPORT_AMOUNT_ERROR = "account.import.amount.error";
	/** 修正金额范围：{0}-{1} */
	String ACCOUNT_IMPORT_AMOUNT_ERROR_FORMAT = "account.import.amount.error.format";
	/** 稽核倍数范围：1~100 */
	String ACCOUNT_IMPORT_FACTOR_ERROR = "account.import.factor.error";
	/** 备注不超过{0}个字符 */
	String ACCOUNT_IMPORT_REMARK_ERROR = "account.import.remark.error";
	/** 不能设置代理号为主播~ */
	String ACCOUNT_SET_HOST_FORBIDDEN = "account.set.host.forbidden";
	/** 充值金额不能大于{0} */
	String FORM_RECHARGE_AMOUNT_MAX = "form.recharge.amount.max";
	/** 赠送金额不能大于{0} */
	String FORM_GIFT_AMOUNT_MAX = "form.gift.amount.max";

	/** 每次仅支持导出{0}天的数据！ */
	String EXPORT_ONLY_DAYS = "export.only.days";
	/** 仅支持查询{0}天的数据！ */
	String QUERY_DAYS_LIMIT = "query.days.limit";
	/** 代理不存在~ */
	String AGENT_NOT_EXIST = "agent.not.exist";
	/** 上级代理不存在~ */
	String AGENT_PARENT_NOT_EXIST = "agent.parent.not.exist";
	/** 修改用户下级结果与修改代理下级结果不一致 */
	String AGENT_USER_MAP_CHANGE_FAILURE = "agent.user.map.change.failure";
	/** 只能配置顶级代理！ */
	String AGENT_ONLY_TOP = "agent.only.top";
	/** 该代理无法进行此操作！ */
	String AGENT_OPERATE_FORBIDDEN = "agent.operate.forbidden";
	/** 请输入分成比例 */
	String AGENT_SHARE_NOT_RATE = "agent.share.not.rate";
	/** 请先添加后台域名再新增代理商 */
	String AGENT_DOMAIN_NOT_EXIST = "agent.domain.not.exist";
	/** 请先添加后台域名 */
	String DOMAIN_NOT_EXIST = "domain.not.exist";
	/** 会员不存在，请核实输入是否正确！ */
	String AGENT_USER_NOT_EXIST = "agent.user.not.exist";
	/** 会员输入错误，请核实是否正确! */
	String AGENT_USER_INPUT_ERROR = "agent.user.input.error";
	/** 只能修改同商户的非主播号的会员！ */
	String AGENT_USER_MERCHANT_ERROR = "agent.user.merchant.error";
	/** 不允许修改顶级代理商的关系！ */
	String AGENT_USER_MAP_TOP_ERROR = "agent.user.map.top.error";
	/** 同顶级代理不能进行变更！ */
	String AGENT_USER_MAP_TOP_CHANGE_ERROR = "agent.user.map.top.change.error";
	/** 该用户已经是顶级代理！ */
	String AGENT_USER_MAP_TOP_EXIST = "agent.user.map.top.exist";
	/** 结束时间必须大于开始时间！ */
	String AGENT_SETTLE_TIME_ERROR = "agent.settle.time.error";
	/** 预警额度不能小于0！ */
	String AGENT_WARN_AMOUNT_ERROR = "agent.warn.amount.error";
	/** 预警内容 > 0 并且 <= 500 */
	String AGENT_WARN_MSG_ERROR = "agent.warn.msg.error";

	/** 请设置模板 */
	String APP_TEMPLATE_NOT_SET = "app.template.not.set";
	/** 请选择模板 */
	String APP_TEMPLATE_NOT_SELECT = "app.template.not.select";
	/** 请选择颜色 */
	String APP_COLOR_NOT_SELECT = "app.color.not.select";
	/** 你无权限设置该模板 */
	String APP_TEMPLATE_NOT_ROLE = "app.template.not.role";
	/** 配置缺失，请检查后重新提交 */
	String APP_CONFIG_NOT_COMPLETE = "app.config.not.complete";
	/** 状态设置异常 */
	String APP_STATUS_ERROR = "app.status.error";
	/** 跳过客服中心配置错误 */
	String APP_CS_ERROR = "app.cs.error";
	/** 跳转客服中心 */
	String APP_TO_CS = "app.to.cs";
	/** {0}配置错误 */
	String CONFIG_ERROR = "config.error";
	/** 在线客服开关配置错误 */
	String APP_CS_ONLINE_ERROR = "app.cs.online.error";
	/** 外部链接配置错误 */
	String APP_CS_EXTERNAL_LINK_ERROR = "app.cs.external.link.error";
	/** 内部链接配置错误 */
	String APP_CS_INTERNAL_ERROR = "app.cs.internal.error";
	/** 展示位置配置有误 */
	String APP_AP_POSITION_ERROR = "app.ap.position.error";
	/** 最大展示金额 ≥ 0 且只能有两位小数 */
	String APP_AP_MAX_AMOUNT_ERROR = "app.ap.max.amount.error";
	/** 最小展示金额 ≥ 0 且只能有两位小数 */
	String APP_AP_MIN_AMOUNT_ERROR = "app.ap.min.amount.error";
	/** 最大展示金额不能小于最小展示金额 */
	String APP_AP_MAX_MIN_ERROR = "app.ap.max.min.error";
	/** 只能开启一个 */
	String APP_AP_ONLY_ONE_ERROR = "app.ap.only.one.error";
	/** 域名不存在，请重新选择 */
	String APP_DL_DOMAIN_NOT_EXIST = "app.dl.domain.not.exist";
	/** {0} 域名已被使用，请更换其他域名 */
	String APP_DL_DOMAIN_USED = "app.dl.domain.used";
	/** {0} 节点未开启！ */
	String APP_DL_DOMAIN_NOT_OPEN = "app.dl.domain.not.open";
	/** 域名已被删除! */
	String APP_DL_DOMAIN_DELETED = "app.dl.domain.deleted";
	/** 域名尚未验证! */
	String APP_DL_DOMAIN_NOT_VERIFIED = "app.dl.domain.not.verified";
	/** 验证失败，请核实！ */
	String APP_DL_DOMAIN_VERIFY_ERROR = "app.dl.domain.verify.error";
	/** 域名已被删除! */
	String APP_DL_DOMAIN_DELETED_ERROR = "app.dl.domain.deleted.error";
	/** 删除主域名失败! */
	String APP_DL_DOMAIN_DELETE_ERROR = "app.dl.domain.delete.error";
	/** 域名已删除，无法进行此操作！ */
	String APP_DL_DOMAIN_DELETED_FORBIDDEN = "app.dl.domain.deleted.forbidden";
	/** 域名已通过验证! */
	String APP_DL_DOMAIN_ACTIVE_ERROR = "app.dl.domain.active.error";
	/** 渠道不存在,请核实！ */
	String APP_INFO_CHANNEL_NOT_EXIST = "app.info.channel.not.exist";
	/** 请先添加app配置！ */
	String APP_INFO_CONFIG_NEED_ADD = "app.info.config.need.add";
	/** 该渠道尚未启用,请核实！ */
	String APP_INFO_CHANNEL_NOT_ENABLE = "app.info.channel.not.enable";
	/** APP路径不能为空！ */
	String APP_INFO_PATH_EMPTY = "app.info.path.empty";
	/** 包名已存在，请重新编辑！ */
	String APP_INFO_PACKAGE_EXIST = "app.info.package.exist";
	/** 正在打包中，请耐心等待！ */
	String APP_INFO_PACKAGE_WAIT = "app.info.package.wait";
	/** 发布失败 */
	String APP_INFO_PUB_FAIL = "app.info.pub.fail";
	/** 打包失败 */
	String APP_INFO_PACK_FAIL = "app.info.pack.fail";
	/** 向APP打包发送请求失败，请稍后重试！ */
	String APP_INFO_PACK_REQUEST_FAIL = "app.info.pack.request.fail";
	/** 向APP打包发送请求失败，请联系客服！ */
	String APP_INFO_PACK_REQUEST_FAIL_CONTACT = "app.info.pack.request.fail.contact";
	/** APP信息不存在 */
	String APP_INFO_NOT_EXIST = "app.info.not.exist";
	/** 已打包成功，无需重复打包！ */
	String APP_INFO_PACKED = "app.info.packed";
	/** 状态已变更，请刷新核实！ */
	String DATA_STATUS_CHANGED = "data.status.changed";
	/** 打包中或状态已变更，请刷新核实！ */
	String APP_INFO_STATUS_PACKING = "app.info.status.packing";
	/** 系统模板不能进行编辑！ */
	String APP_AWARD_TEMPLATE_EDIT_LIMIT = "app.award.template.edit.limit";
	/** 请选择样式！ */
	String APP_AWARD_STYLE_ID_REQUIRED = "app.award.style.id.required";
	/** 样式无效，请重新选择！ */
	String APP_AWARD_STYLE_INVALID = "app.award.style.invalid";

	/** 请选择支付类型！ */
	String CASH_TYPE_NOT_SELECT = "cash.type.not.select";
	/** 备注长度最多：{0} */
	String CASH_REMARK_MAX_LENGTH = "cash.remark.max.length";

	/** 请核实代理账号！ */
	String CHANNEL_AGENT_INVALID = "channel.agent.invalid";
	/** 不能设置主播号为代理！ */
	String CHANNEL_AGENT_LIMIT_TEAM = "channel.agent.limit.team";

	/** 请填写需要添加的用户数量！ */
	String USER_ADD_NUM_NULL = "user.add.num.null";
	/** 生成推广账号失败，请重试~ */
	String USER_AGENT_GEN_ERROR = "user.agent.gen.error";

	/** 反馈信息已处理 */
	String FEEDBACK_HANDLED = "feedback.handled";

	/** 操作成功，系统已自动从账户完成扣款，感谢您的支持！ */
	String BILL_CONFIRM_SUCCESS = "bill.confirm.success";
	/** 核对成功，但您的账户余额不足，自动扣款失败，为避免影响您站点的正常运行，请注意及时充值！ */
	String BILL_CONFIRM_ERROR = "bill.confirm.error";
	/** 请选择账单时间 */
	String BILL_TIME_NOT_SELECT = "bill.time.not.select";
	/** 账单开始时间和结束时间必选 */
	String BILL_TIME_NOT_COMPLETE = "bill.time.not.complete";
	/** 提交成功，商户已经可以核对该订单！ */
	String BILL_SUBMIT_SUCCESS = "bill.submit.success";
	/** 商户账单不存在 */
	String BILL_NOT_EXIST = "bill.not.exist";
	/** 该账单状态已变更，无法进行修改操作！ */
	String BILL_STATUS_CHANGED = "bill.status.changed";
	/** 修改后的商家实际应付款金额不能小于0！ */
	String BILL_REAL_PAY_ERROR = "bill.real.pay.error";
	/** 该账单状态已发生变更，请刷新确认无误后再操作！如逾期系统会自动划扣余额。 */
	String BILL_STATUS_CHANGED_ERROR = "bill.status.changed.error";
	/** 该账单已发生变更，请刷新确认无误后再操作！ */
	String BILL_CHANGED_ERROR = "bill.changed.error";
	/** 请上传游戏厂商的对账单文件！ */
	String BILL_FILE_NOT_UPLOAD = "bill.file.not.upload";
	/** 交易汇率的调整幅度不能超过 ±10% ！ */
	String BILL_RATE_CHANGE_ERROR = "bill.rate.change.error";

	/** 没有游戏需要更新！ */
	String GAME_UPDATE_NONE = "game.update.none";
	/** 没有厂家需要更新！ */
	String VENDOR_UPDATE_NONE = "vendor.update.none";
	/** 热门游戏（包含厂商）最多可添加{0}个 */
	String GAME_HOT_MAX_SIZE = "game.hot.max.size";
	/** 批量更新厂商信息失败！ */
	String VENDOR_BATCH_UPDATE_FAIL = "vendor.batch.update.fail";
	/** 游戏同步更新成功，本次更新如下： */
	String GAME_SYNC_DETAIL = "game.sync.detail";
	/** 新增{0}款游戏 */
	String GAME_SYNC_ADD = "game.sync.add";
	/** 厂家：{0}，游戏类型：{1} */
	String GAME_SYNC_ADD_DETAIL = "game.sync.add.detail";

	/** 配置的层级数量不能超过 {0} */
	String LAYER_MAX_SIZE = "layer.max.size";
	/** 层级名称不能为空且最长{0}字符 */
	String LAYER_NAME_INVALID = "layer.name.invalid";
	/** 层级描述不能为空且最长{0}字符 */
	String LAYER_DESC_INVALID = "layer.desc.invalid";
	/** 充值次数错误 */
	String LAYER_RECHARGE_COUNT_ERROR = "layer.recharge.count.error";
	/** 层级配置中不能存在相同的盈利额度 */
	String LAYER_PROFIT_DUPLICATE = "layer.profit.duplicate";
	/** 固定层级不能包含累计充值金额 */
	String LAYER_FIXED_ERROR = "layer.fixed.error";
	/** 固定层级不能包含用户累计充值成功次数 */
	String LAYER_FIXED_RECHARGE_COUNT_ERROR = "layer.fixed.recharge.count.error";
	/** 固定层级不能包含盈利额度 */
	String LAYER_FIXED_PROFIT_ERROR = "layer.fixed.profit.error";
	/** 累计充值金额应大于等于{0} */
	String LAYER_RECHARGE_SUM_ERROR = "layer.recharge.sum.error";
	/** 必须存在默认层级 */
	String LAYER_DEFAULT_NOT_EXIST = "layer.default.not.exist";
	/** 不能存在相同名称的层级 */
	String LAYER_NAME_DUPLICATE = "layer.name.duplicate";
	/** 不能存在相同累计充值金额的自动层级 */
	String LAYER_SUM_DUPLICATE = "layer.sum.duplicate";
	/** 已存在名为【{0}】的层级 */
	String LAYER_NAME_DUPLICATE_ERROR = "layer.name.duplicate.error";
	/** 请核实会员ID是否正确~ */
	String LAYER_UID_ERROR = "layer.uid.error";
	/** 层级配置已发生变化请刷新页面~ */
	String LAYER_CONFIG_CHANGED = "layer.config.changed";

	/** 充值档位已发生变化，请刷新页面确认后再重试！ */
	String RECHARGE_PRODUCT_CHANGED = "recharge.product.changed";
	/** 大R玩家类型不能为空！ */
	String RECHARGE_TYPE_INVALID = "recharge.type.invalid";
	/** 累计充值金额必须大于0！ */
	String RECHARGE_AMOUNT_INVALID = "recharge.amount.invalid";
	/** 大R玩家配置不能为空！ */
	String RECHARGE_CONFIG_INVALID = "recharge.config.invalid";
	/** 玩家类型设置错误！ */
	String RECHARGE_TYPE_ERROR = "recharge.type.error";
	/** 累计充值金额只能递增！ */
	String RECHARGE_AMOUNT_INCREASE_ERROR = "recharge.amount.increase.error";

	/** 活动已存在 */
	String PROMOTION_EXIST = "promotion.exist";
	/** 活动不存在 */
	String PROMOTION_NOT_EXIST = "promotion.not.exist";
	/** 已存在相同规则的活动，不允许新增 */
	String PROMOTION_RULE_DUPLICATE_ADD = "promotion.rule.duplicate.add";
	/** 已存在相同规则的活动，不允许修改 */
	String PROMOTION_RULE_DUPLICATE_CHANGE = "promotion.rule.duplicate.change";
	/** 活动当前状态，不允许编辑 */
	String PROMOTION_STATUS_EDIT = "promotion.status.edit";
	/** 已删除的活动不允许修改状态 */
	String PROMOTION_STATUS_DELETE = "promotion.status.delete";
	/** 已关闭的活动不允许修改状态 */
	String PROMOTION_STATUS_CLOSE = "promotion.status.close";
	/** 只能是新人彩金活动 */
	String PROMOTION_TYPE_REDEEM = "promotion.type.redeem";
	/** 活动暂未发布 */
	String PROMOTION_STATUS_UNPUBLISHED = "promotion.status.unpublished";
	/** 已生成兑换码数量超过最大可生成数量 */
	String PROMOTION_REDEEM_CODE_OVER = "promotion.redeem.code.over";
	/** 请核实站点ID是否输入正确 */
	String SITE_ID_INVALID = "site.id.invalid";
	/** 请选择要同步的活动 */
	String PROMOTION_SYNC_NOT_SELECT = "promotion.sync.not.select";
	/** 只能操作自定义活动 */
	String PROMOTION_ONLY_CUSTOM = "promotion.only.custom";
	/** 有效投注 */
	String PROMOTION_RANK_VALID_COIN = "promotion.rank.valid.coin";
	/** 盈利额度 */
	String PROMOTION_RANK_PROFIT_AMOUNT = "promotion.rank.profit.amount";
	/** 充值额度 */
	String PROMOTION_RANK_RECHARGE_AMOUNT = "promotion.rank.recharge.amount";
	/** 活动名称 */
	String PROMOTION_ACTIVITY_NAME = "export.promotionStat.name";
	/** 活动类型 */
	String PROMOTION_ACTIVITY_TYPE = "export.promotionStat.type_";

	/** 时长：天 */
	String DURATION_DAY = "duration.day";
	/** 时长：周 */
	String DURATION_WEEK = "duration.week";
	/** 时长：月 */
	String DURATION_MONTH = "duration.month";
	/** 时长：总 */
	String DURATION_ALL = "duration.all";
	/** 结算周期：每天 */
	String DURATION_DAY_SETTLE = "duration.day.settle";
	/** 结算周期：每周 */
	String DURATION_WEEK_SETTLE = "duration.week.settle";
	/** 结算周期：每月 */
	String DURATION_MONTH_SETTLE = "duration.month.settle";

	/** 手机注册 */
	String LOGIN_WAY_CONFIG_SMS_CAPTCHA = "login.way.config.sms.captcha";
	/** 账号注册 */
	String LOGIN_WAY_CONFIG_REGISTER = "login.way.config.register";
	/** 手机号+密码 */
	String LOGIN_WAY_CONFIG_PHONE_PWD = "login.way.config.phone.password";
	/** Google登录 */
	String LOGIN_WAY_CONFIG_GOOGLE_LOGIN = "login.way.config.google.login";
	/** Facebook登录 */
	String LOGIN_WAY_CONFIG_FACEBOOK = "login.way.config.facebook";

	/** 请上传白名单信息 */
	String WHITELIST_NOT_NULL = "whitelist.not.null";
	/** 第{0}行，会员ID为空 */
	String WHITELIST_IMPORT_UID_EMPTY = "whitelist.import.uid.empty";
	/** 第{0}行，备注长度不能超过500 */
	String WHITELIST_IMPORT_REMARK_LENGTH = "whitelist.import.remark.length";
	/** 会员{0}已存在白名单 */
	String WHITELIST_MEMBER_EXIST = "whitelist.member.exist";
	/** 层级ID不能为空 */
	String LAYER_ID_NOT_NULL = "layer.id.not.null";
	/** 请选择状态 */
	String STATUS_NOT_NULL = "status.not.null";

	/** 请上传厂商账变信息 */
	String ACCOUNT_LIST_NOT_NULL = "account.list.not.null";
	/** 第{0}行，厂商ID为空 */
	String ACCOUNT_LIST_IMPORT_VENDOR_ID_EMPTY = "account.list.import.vendor.id.empty";
	/** 第{0}行，类型为空 */
	String ACCOUNT_LIST_IMPORT_TYPE_EMPTY = "account.list.import.type.empty";
	/** 第{0}行，金额（单位“U”）为空 */
	String ACCOUNT_LIST_IMPORT_AMOUNT_EMPTY = "account.list.import.amount.empty";
	/** 第{0}行，金额错误 */
	String ACCOUNT_LIST_IMPORT_AMOUNT_ERROR = "account.list.import.amount.error";
	/** 第{0}行，备注长度不能超过500 */
	String ACCOUNT_LIST_IMPORT_REMARK_LENGTH = "account.list.import.remark.length";

	/** 第{0}行，导入失败，原因：{1} */
	String IMPORT_FAIL = "import.fail";
	/** 导入成功{0}条 */
	String IMPORT_SUCCESS = "import.success";

	/** 请先进行配置 */
	String REBATE_CONFIG_NOT_SET = "rebate.config.not.set";
	/** 暂未配置可用厂家 */
	String REBATE_VENDOR_NOT_SET = "rebate.vendor.not.set";
	/** 不存在的VIP等级配置 */
	String REBATE_LEVEL_NOT_EXIST = "rebate.config.level.not.exist";
	/** 前一VIP等级不存在 */
	String REBATE_LEVEL_PREV_NOT_EXIST = "rebate.config.level.prev.not.exist";
	/** 请先配置对应VIP等级返水信息 */
	String REBATE_LEVEL_NOT_CONFIG = "rebate.config.level.not.config";
	/** 请先配置前一VIP等级返水信息 */
	String REBATE_LEVEL_PREV_NOT_CONFIG = "rebate.config.level.prev.not.config";
	/** 有效投注总额不能为空 */
	String REBATE_TOTAL_NOT_NULL = "rebate.total.not.null";
	/** 返水比例配置不能为空 */
	String REBATE_RATE_NOT_NULL = "rebate.rate.not.null";
	/** 返水比例配置不正确 */
	String REBATE_RATE_INVALID = "rebate.rate.invalid";
	/** 配置有效投注总额重复 */
	String REBATE_TOTAL_DUPLICATE = "rebate.total.duplicate";
	/** 暂不支持的游戏类型:{0} */
	String REBATE_GAME_TYPE_NOT_SUPPORT = "rebate.game.type.not.support";
	/** 请先开启厂家{0}的游戏开关！ */
	String REBATE_GAME_TYPE_NOT_OPEN = "rebate.game.type.not.open";

	/** 操作成功，充值已到账！ */
	String RECHARGE_SUCCESS = "recharge.success";
	/** 该订单已付款成功，充值已到账！ */
	String RECHARGE_PAID = "recharge.paid";
	/** 充值订单不存在 */
	String RECHARGE_NOT_EXIST = "recharge.not.exist";

	/** 国家或地区不存在 */
	String REGION_NOT_EXIST = "region.not.exist";
	/** 该国家或地区限制已存在 */
	String REGION_LIMIT_EXIST = "region.limit.exist";

	/** 播放时间只能设置在{0}~{1}秒内 */
	String BANNER_PLAY_TIME_INVALID = "banner.play.time.invalid";

	/** 站点设置有误，请刷新重试！ */
	String SITE_SETTING_ERROR = "site.setting.error";
	/** 不可使用该支付渠道代付，请联系客服。 */
	String PAYMENT_CHANNEL_NOT_SUPPORT = "payment.channel.not.support";

	/** 可用余额必须大于零才能新增稽核任务 */
	String AUDIT_BALANCE_INVALID = "audit.balance.invalid";
	/** 该稽核任务已解除，无需重复操作！ */
	String AUDIT_REMOVED = "audit.removed";
	/** 该稽核任务属性已发生变化，请确认后再重试！ */
	String AUDIT_CHANGED = "audit.changed";

	/** 等级未变化，请重新选择 */
	String LEVEL_NOT_CHANGED = "level.not.changed";
	/** 该会员有申请提现中，不可修改vip等级 */
	String LEVEL_CHANGE_NOT_ALLOWED = "level.change.not.allowed";
	/** 该vip等级存在会员，请调整会员至其他等级，再进行删除！ */
	String LEVEL_DELETE_NOT_ALLOWED = "level.delete.not.allowed";
	/** 尚未找到可添加的会员，请核实是否满足列表添加条件或该会员是否已加入！ */
	String MEMBER_ADD_NOT_FOUND = "member.add.not.found";
	/** 会员已加入，无需重复添加！ */
	String MEMBER_ADD_DUPLICATE = "member.add.duplicate";
	/** 请核实会员ID是否错误:{0} */
	String MEMBER_ID_INVALID = "member.id.invalid";
	/** 删除失败，尚未找到可删除的会员！ */
	String MEMBER_DELETE_NOT_FOUND = "member.delete.not.found";
	/** 删除失败，请核实重试！ */
	String MEMBER_DELETE_FAIL = "member.delete.fail";
	/** 会员ID输入有误! */
	String MEMBER_ID_INVALID_FORMAT = "member.id.invalid.format";
	/** 最大输入{0}个{1} */
	String MSG_RANGES_LIMIT = "msg.ranges.limit";
	/** 推广账号不能创建在线订单！ */
	String MEMBER_ORDER_NOT_ALLOWED = "member.order.not.allowed";
	/** 请配置全部VIP等级 */
	String MEMBER_LEVEL_NOT_CONFIG = "member.level.not.config";
	/** 余额限制必须大于等于0 */
	String BALANCE_LIMIT_INVALID = "balance.limit.invalid";
	/** 默认配置不可使用此返奖率 */
	String REBATE_BPG_NOT_ALLOWED = "rebate.bpg.not.allowed";
	/** 不存在的返奖率 */
	String REBATE_NOT_EXIST = "rebate.not.exist";

	/** 非推广用户不可选用此返奖率 */
	String REBATE_NOT_ALLOWED = "rebate.not.allowed";
	/** 不可使用此返奖率 */
	String REBATE_NOT_USABLE = "rebate.not.usable";

	/** 层级已发生变化，请刷新页面。 */
	String LAYER_CHANGED = "layer.changed";
	/** 这些代理人已存在规则：{0} */
	String CTRL_RULE_EXIST = "ctrl.rule.exist";
	/** 不存在的代理人用户：{0} */
	String CTRL_USER_NOT_EXIST = "ctrl.user.not.exist";
	/** VIP配置发生变化，请刷新页面后重新配置 */
	String VIP_CHANGED = "vip.changed";
	/** 缺少{0}的配置 */
	String CTRL_MISSING_CONFIG = "ctrl.missing.config";
	/** 请配置满{0}的全部阶梯，并检查是否重复，配置0的池子将被忽略 */
	String CTRL_MISSING_TIER = "ctrl.missing.tier";
	/** 请进行阶梯配置 */
	String CTRL_TIER_NOT_CONFIG = "ctrl.tier.not.config";
	/** 阶梯 {0} 人数比例的值应该 >= 0，小于等于100% */
	String CTRL_TIER_PERCENT_INVALID = "ctrl.tier.percent.invalid";
	/** 阶梯 {0} 人数比例总和应该 > 0，小于等于100% */
	String CTRL_TIER_PERCENT_TOTAL_INVALID = "ctrl.tier.percent.total.invalid";
	/** 推广账号最多创建{0}个，已经存在{1}个 */
	String CTRL_TEAM_MAX_COUNT = "ctrl.team.max.count";
	/** 最多同时创建{0}个 */
	String BATCH_ADD_MAX_COUNT = "batch.add.max.count";

	/** 未找到提现限制配置 */
	String PRIVILEGE_CASH_LIMIT_NOT_FOUND = "privilege.cash.limit.not.found";
	/** 特权图片不能为空 */
	String PRIVILEGE_IMAGE_NOT_NULL = "privilege.image.not.null";
	/** 祝福内容不能为空！ */
	String PRIVILEGE_CONTENT_NOT_NULL = "privilege.content.not.null";
	/** 0级不能设置奖励！" */
	String PRIVILEGE_LEVEL_ZERO_ALLOWED = "privilege.level.zero.not.allowed";
	/** 每日次数配置有误 */
	String PRIVILEGE_DAILY_COUNT_INVALID = "privilege.daily.count.invalid";

	/** 每日次数 */
	String PRIVILEGE_DAILY_COUNT = "privilege.daily.count";
	/** 金币奖品 */
	String PRIVILEGE_GOLD = "privilege.gold";
	/** 额度 */
	String PRIVILEGE_LIMIT = "privilege.limit";
	/** {0}等级配置有误 */
	String PRIVILEGE_LEVEL_INVALID = "privilege.level.invalid";
	/** {0}数量配置有误 */
	String PRIVILEGE_COUNT_INVALID = "privilege.count.invalid";
	/** {0}不能小于零 */
	String PRIVILEGE_COUNT_INVALID_NEGATIVE = "privilege.count.invalid.negative";
	/** {0}等级不能为空 */
	String PRIVILEGE_LEVEL_NOT_NULL = "privilege.level.not.null";
	/** {0}数量不能为空 */
	String PRIVILEGE_COUNT_NOT_NULL = "privilege.count.not.null";
	/** {0}中奖概率不能为空 */
	String PRIVILEGE_ODDS_NOT_NULL = "privilege.odds.not.null";
	/** 每个VIP等级的中奖概率总和应为100% */
	String PRIVILEGE_ODDS_SUM_INVALID = "privilege.odds.sum.invalid";

	/** 请先配置存钱宝收益规则，首次配置完成后将自动启用！ */
	String INCOME_RULE_NOT_CONFIG = "income.rule.not.config";
	/** 请输入有效的收款地址！ */
	String ADDRESS_INVALID = "address.invalid";

	/** 不能同时选择用户和代理进行查询 */
	String HOME_QUERY_USER_AND_AGENT = "home.query.user.and.agent";

	//================================= ADMIN ===========================================
	/** 管理员才能进行此操作！ */
	String ADMIN_ONLY = "admin.only";
	/** 账号名重复，请更换账号名！ */
	String USERNAME_DUPLICATE = "username.duplicate";
	/** 角色无需配置站点~ */
	String ROLE_NO_SITE_CONFIG = "role.no.site.config";
	/** 不支持对此账号操作！ */
	String EMP_NOT_SUPPORT = "emp.not.support";
	/** 您不能修改此角色权限！ */
	String ROLE_NOT_MODIFY_PERMISSION = "role.not.modify.permission";
	/** 暂不支持对此账号操作！ */
	String EMP_NOT_SUPPORT_OPERATION = "emp.not.support.operation";
	/** 您不能修改此角色！ */
	String ROLE_NOT_MODIFY = "role.not.modify";
	/** 此角色不在您的管辖范围内！ */
	String ROLE_NOT_IN_SCOPE = "role.not.in.scope";
	/** 绑定成功 */
	String BIND_SUCCESS = "bind.success";
	/** 单笔加款限额：{0} */
	String SINGLE_ADD_LIMIT = "single.add.limit";
	/** 请输出Google验证码 */
	String GOOGLE_AUTH_CODE_REQUIRED = "google.auth.code.required";
	/** Google验证码错误 */
	String GOOGLE_AUTH_CODE_INVALID = "google.auth.code.invalid";
	/** 该账号已绑定谷歌身份验证器 */
	String GOOGLE_AUTH_BIND = "google.auth.bind";
	/** 绑定Google身份验证器失败！ */
	String GOOGLE_AUTH_BIND_FAIL = "google.auth.bind.fail";
	/** 重置Google身份验证器失败！ */
	String GOOGLE_AUTH_RESET_FAIL = "google.auth.reset.fail";
	/** 不允许修改Google配置~ */
	String GOOGLE_CONFIG_RESET_NOT_SUPPORT = "google.config.reset.not.support";
	/** 员工ID必选 */
	String EMP_ID_REQUIRED = "emp.id.required";
	/** 员工不存在 */
	String EMP_NOT_EXIST = "emp.not.exist";
	/** 该角色名称已存在，请重新输入！ */
	String ROLE_NAME_DUPLICATE = "role.name.duplicate";
	/** 不支持创建站点角色！ */
	String ROLE_CREATE_SITE_NOT_SUPPORT = "role.create.site.not.support";
	/** 站点角色已存在，只能设置一个！ */
	String ROLE_SITE_EXIST = "role.site.exist";
	/** 不允许删除站点角色~ */
	String ROLE_SITE_NOT_DELETE = "role.site.not.delete";
	/** 角色无需配置站点~ */
	String ROLE_NO_SITE = "role.no.site";
	/** 菜单不属于该角色! */
	String ROLE_MENU_NOT_BELONG = "role.menu.not.belong";
	/** 获取新菜单名称失败 */
	String MENU_NAME_FAIL = "menu.name.fail";
	/** 菜单名称不可重复 */
	String MENU_NAME_DUPLICATE = "menu.name.duplicate";
	/** 流量报表字段-全部 */
	String TRAFFIC_STAT_ALL = "traffic.chart.all";

	/** 文件已过期！ */
	String FILE_EXPIRED = "file.expired";
	/** 文件状态不支持下载！ */
	String FILE_STATUS_FAIL = "file.status.fail";

	/** 批量更新游戏信息失败！ */
	String GAME_BATCH_UPDATE_FAIL = "game.batch.update.fail";
	/** 游戏ID输入有误 */
	String GAME_ID_INVALID = "game.id.invalid";
	/** 厂商已被关闭，无法修改游戏状态 */
	String GAME_VENDOR_CLOSED = "game.vendor.closed";
	/** 当前站点余额不足，请注意充U。 */
	String SITE_BALANCE_INSUFFICIENT = "site.balance.insufficient";
	/** 额度不足，扣费失败！ */
	String MERCHANT_BALANCE_NOT_ENOUGH = "merchant.balance.not.enough";
	/** 商户正在建设中，不允许创建站点！ */
	String MERCHANT_UNDER_CONSTRUCTION = "merchant.under.construction";
	/** 商户正在建设中！不允许修改状态 */
	String MERCHANT_STATUS_INIT_NOT_MODIFY = "merchant.status.init.not.modify";
	/** 子站点数量达到最大限制，请联系客服 */
	String LIMIT_CREATE_SUB_SITE = "limit.create.sub.site";
	/** 商户不存在 */
	String MERCHANT_NOT_EXIST = "merchant.not.exist";
	/** 只有正常或建设中的状态才能维护 */
	String MERCHANT_STATUS_NOT_MAINTAIN = "merchant.status.not.maintain";
	/** 只有维护或冻结状态才能恢复 */
	String MERCHANT_STATUS_NOT_RECOVERY = "merchant.status.not.recovery";
	/** 未找到要恢复的状态 */
	String MERCHANT_STATUS_NOT_FOUND = "merchant.status.not.found";
	/** 已经注销的商户不能编辑状态 */
	String MERCHANT_STATUS_NOT_EDIT = "merchant.status.not.edit";
	/** 默认主站点不能注销 */
	String MAIN_SITE_STATUS_NOT_CLOSE = "main.status.not.close";
	/** 还有未结账单，不能注销 */
	String MERCHANT_STATUS_NOT_CANCEL = "merchant.status.not.cancel";
	/** 已经冻结的商户不能改为维护状态 */
	String MERCHANT_STATUS_NOT_MAINTAIN_FROZEN = "merchant.status.not.maintain.frozen";
	/** 站点状态不能改为正常 */
	String SITE_STATUS_NOT_OK = "site.status.not.ok";
	/** 建设中的站点不能设置为维护状态！ */
	String SITE_STATUS_NOT_MAINTAIN_UNDER = "site.status.not.maintain.under";
	/** 建设中商户不允许修改为维护或冻结状态！ */
	String SITE_STATUS_NOT_MAINTAIN_FROZEN_UNDER = "site.status.not.maintain.frozen.under";
	/** {0}未配置，站点不能运营！ */
	String SITE_STATUS_NO_CONFIG = "site.status.no.config";
	/** 备注不能为空且长度不能超过{0} */
	String REMARK_LENGTH_INVALID = "remark.length.invalid";
	/** 请输入商户ID */
	String MERCHANT_ID_REQUIRED = "merchant.id.required";
	/** 商户名重复 */
	String MERCHANT_NAME_DUPLICATE = "merchant.name.duplicate";
	/** 账户余额不够支付开版费 + 线路费，无法开站，开版费：{0}U，服务器费用：{1}U */
	String MERCHANT_NOT_ENOUGH_BALANCE = "merchant.not.enough.balance";
	/** 商户抽成不能大于厂家抽成 */
	String MERCHANT_NOT_ALLOW_GREATER = "merchant.not.allow.greater";
	/** 充币额度只能递增~ */
	String MERCHANT_RECHARGE_LIMIT_INVALID = "merchant.recharge.limit.invalid";
	/** 赠送比例只能递增~ */
	String MERCHANT_GIVE_RATE_INVALID = "merchant.give.rate.invalid";
	/** 赠送额度只能递增~ */
	String MERCHANT_GIVE_LIMIT_INVALID = "merchant.give.limit.invalid";

	/** 提前提醒的天数不能超过 {0} 与 {1} 的天数差值 */
	String AHEAD_DAY_INVALID = "ahead.day.invalid";
	/** 抽成模式未改变 */
	String DRAW_MODE_NOT_CHANGE = "draw.mode.not.change";
	/** 站点状态发生变更，请确认后重试！ */
	String SITE_STATUS_CHANGE = "site.status.change";
	/** 请先配置厂家分成比例 */
	String DRAW_MODE_NOT_CONFIG = "draw.mode.not.config";

	/** {0}设定值必填 */
	String LEVEL_VALUE_REQUIRED = "level.value.required";
	/** {0}设定值必须为大于零的正整数 */
	String LEVEL_VALUE_INVALID = "level.value.invalid";
	/** {0}设定值 ≥ 0 且只能有一位小数 */
	String LEVEL_VALUE_INVALID_DECIMAL = "level.value.invalid.decimal";
	/** {0}设定值不能大于下一个 */
	String LEVEL_VALUE_INVALID_GREATER = "level.value.invalid.greater";
	/** {0}设定值只能小于下一个 */
	String LEVEL_VALUE_INVALID_LESS = "level.value.invalid.less";

	/** 当前等级的累计充值U不能大于下一级 */
	String LEVEL_INVALID_GREATER_ACCOUNT = "level.invalid.greater.account";
	/** 当前等级的累计充值U不能小于上一级 */
	String LEVEL_INVALID_LESS_ACCOUNT = "level.invalid.less.account";
	/** 账单发出之前不允许修改商户分成比例 */
	String LEVEL_NOT_ALLOW_EDIT = "level.not.allow.edit";
	/** 商户分成比例不能小于厂家分成比例 */
	String LEVEL_NOT_ALLOW_LESS = "level.not.allow.less";

	/** 已存在相同的支付通道配置，请核实 */
	String PAY_CHANNEL_DUPLICATE = "pay.channel.duplicate";
	/** 提现支付配置有误，请刷新重试！ */
	String CASH_PAY_CONFIG_INVALID = "cash.pay.config.invalid";
	// 支付配置有误，请刷新重试！
	String PAY_CONFIG_INVALID = "pay.config.invalid";
	/** 总商户才能进行此操作！ */
	String ROLE_NOT_ALLOW = "role.not.allow";
	/** 站点设置有误，请刷新重试！ */
	String SITE_SETTING_INVALID = "site.setting.invalid";
	/** 不可使用该支付渠道代收，请联系客服。 */
	String PAY_CHANNEL_NOT_ALLOW = "pay.channel.not.allow";
	/** 三方支付渠道配置不存在 */
	String PAY_CHANNEL_NOT_EXIST = "pay.channel.not.exist";
	/** 存在30分钟内创建的进行中充值订单，暂不可删除此通道 */
	String PAY_CHANNEL_RECHARGE_ALLOW = "pay.channel.recharge.allow";
	/** 存在30分钟内创建的进行中提现订单，暂不可删除此通道 */
	String PAY_CHANNEL_CASH_ALLOW = "pay.channel.cash.allow";
	/** 笔笔送比例设置不能小于0~ */
	String BBS_RATE_INVALID = "bbs.rate.invalid";
	/** 配置来源必填 */
	String CONFIG_SOURCE_REQUIRED = "config.source.required";
	/** 配置来源有误 */
	String CONFIG_SOURCE_INVALID = "config.source.invalid";
	/** IP已经存在 */
	String IP_DUPLICATE = "ip.duplicate";
	/** 商户白名单配置不存在 */
	String WHITELIST_NOT_EXIST = "whitelist.not.exist";

	/** 联系方式配置有误 */
	String CONTACT_INVALID = "contact.invalid";
	/** 联系信息不能为空 */
	String CONTACT_REQUIRED = "contact.required";
	/** 联系信息超长 */
	String CONTACT_LENGTH_INVALID = "contact.length.invalid";
	/** 顶部显示配置有误 */
	String LAYER_TOP_INVALID = "layer.top.invalid";
	/** 演示站不能为空 */
	String SITE_URL_REQUIRED = "site.url.required";
	/** 请选择要处理的申请记录 */
	String APPLY_REQUIRED = "apply.required";
	/** 推广设置不能为空 */
	String PROMOTION_CONFIG_REQUIRED = "promotion.config.required";
	/** 推广商户申请记录不存在 */
	String PROMOTION_APPLY_NOT_EXIST = "promotion.apply.not.exist";

	/** Account不存在 */
	String CPF_NOT_EXIST = "cpf.not.exist";
	/** 上级代理账号不存在 */
	String SUP_AGENT_NOT_EXIST = "sup.agent.not.exist";
	/** 累计佣金 */
	String AGENT_COMMISSION = "agent.commission";
	/** 会员已存在 */
	String MEMBER_EXIST = "member.exist";
	/** 存在无效的用户ID！ */
	String MEMBER_INVALID = "member.invalid";
	/** 该会员兑换密码尚未被限制，无需解除！ */
	String MEMBER_NOT_LOCKED = "member.not.locked";
	/** 会员不存在 */
	String MEMBER_NOT_EXIST = "member.not.exist";
	/** 包含不存在的用户 */
	String MEMBER_NOT_EXISTS = "member.not.exists";
	/** 请选择搜索的会员账号类型 */
	String MEMBER_SEARCH_TYPE_REQUIRED = "member.search.type.required";
	/** 搜索的会员账号类型不正确 */
	String MEMBER_SEARCH_TYPE_INVALID = "member.search.type.invalid";
	/** 单次只允许搜索最多{0}个账号 */
	String MEMBER_SEARCH_SIZE_INVALID = "member.search.size.invalid";

	/** VIP开关配置错误 */
	String VIP_CONFIG_INVALID = "vip.config.invalid";
	/** 等级配置范围最小{0}级，最大{1}级 */
	String VIP_LEVEL_INVALID = "vip.level.invalid";
	/** VIP开关配置错误 */
	String VIP_CONFIG_ERROR = "vip.config.error";
	/** 开关配置有误 */
	String VIP_CONFIG_SWITCH_INVALID = "vip.config.switch.invalid";
	/** VIP等级 */
	String VIP_LEVEL = "vip.level";

	/** {0}：VIP等级配置有误 */
	String VIP_LEVEL_CONFIG_INVALID = "vip.level.config.invalid";
	/** {0}：图标不能为空 */
	String VIP_CONFIG_ICON_REQUIRED = "vip.config.icon.required";
	/** {0}：头像框不能为空 */
	String VIP_CONFIG_HEADER_REQUIRED = "vip.config.header.required";
	/** {0}：晋级充值不能为空 */
	String VIP_CONFIG_RECHARGE_REQUIRED = "vip.config.recharge.required";
	/** {0}：晋级充值配置不能大于下一级配置 */
	String VIP_CONFIG_RECHARGE_INVALID = "vip.config.recharge.invalid";
	/** {0}：晋级打码不能为空 */
	String VIP_CONFIG_BET_REQUIRED = "vip.config.bet.required";
	/** {0}：晋级打码配置不能大于下一级配置 */
	String VIP_CONFIG_BET_INVALID = "vip.config.bet.invalid";
	/** {0}：晋级奖励不能为空 */
	String VIP_CONFIG_AWARD_REQUIRED = "vip.config.award.required";
	/** {0}：晋级奖励不能大于下一级配置 */
	String VIP_CONFIG_AWARD_INVALID = "vip.config.award.invalid";
	/** {0}：保级-上个月充值不能为空 */
	String VIP_CONFIG_RECHARGE_LAST_REQUIRED = "vip.config.recharge.last.required";
	/** {0}：保级-上个月充值配置不能大于下一级配置 */
	String VIP_CONFIG_RECHARGE_LAST_INVALID = "vip.config.recharge.last.invalid";
	/** {0}：保级-上个月打码不能为空 */
	String VIP_CONFIG_BET_LAST_REQUIRED = "vip.config.bet.last.required";
	/** {0}：保级-上个月打码配置不能大于下一级配置 */
	String VIP_CONFIG_BET_LAST_INVALID = "vip.config.bet.last.invalid";
	/** {0}：日奖励充值不能为空 */
	String VIP_CONFIG_RECHARGE_DAY_REQUIRED = "vip.config.recharge.day.required";
	/** {0}：日奖励充值配置不能大于下一级配置 */
	String VIP_CONFIG_RECHARGE_DAY_INVALID = "vip.config.recharge.day.invalid";
	/** {0}：日奖励打码不能为空 */
	String VIP_CONFIG_AWARD_DAY_BET_REQUIRED = "vip.config.award.day.bet.required";
	/** {0}：日奖励打码配置不能大于下一级配置 */
	String VIP_CONFIG_AWARD_DAY_BET_INVALID = "vip.config.award.day.bet.invalid";
	/** {0}：日奖励不能为空 */
	String VIP_CONFIG_AWARD_DAY_REQUIRED = "vip.config.award.day.required";
	/** {0}：日奖励配置不能大于下一级配置 */
	String VIP_CONFIG_AWARD_DAY_INVALID = "vip.config.award.day.invalid";
	/** {0}：周奖励充值不能为空 */
	String VIP_CONFIG_RECHARGE_WEEK_REQUIRED = "vip.config.recharge.week.required";
	/** {0}：周奖励充值配置不能大于下一级配置 */
	String VIP_CONFIG_RECHARGE_WEEK_INVALID = "vip.config.recharge.week.invalid";
	/** {0}：周奖励打码不能为空 */
	String VIP_CONFIG_AWARD_WEEK_BET_REQUIRED = "vip.config.award.week.bet.required";
	/** {0}：周奖励打码配置不能大于下一级配置 */
	String VIP_CONFIG_AWARD_WEEK_BET_INVALID = "vip.config.award.week.bet.invalid";
	/** {0}：周奖励不能为空 */
	String VIP_CONFIG_AWARD_WEEK_REQUIRED = "vip.config.award.week.required";
	/** {0}：周奖励配置不能大于下一级配置 */
	String VIP_CONFIG_AWARD_WEEK_INVALID = "vip.config.award.week.invalid";
	/** {0}：月奖励充值不能为空 */
	String VIP_CONFIG_RECHARGE_MONTH_REQUIRED = "vip.config.recharge.month.required";
	/** {0}：月奖励充值配置不能大于下一级配置 */
	String VIP_CONFIG_RECHARGE_MONTH_INVALID = "vip.config.recharge.month.invalid";
	/** {0}：月奖励打码不能为空 */
	String VIP_CONFIG_AWARD_MONTH_BET_REQUIRED = "vip.config.award.month.bet.required";
	/** {0}：月奖励打码配置不能大于下一级配置 */
	String VIP_CONFIG_AWARD_MONTH_BET_INVALID = "vip.config.award.month.bet.invalid";
	/** {0}：月奖励不能为空 */
	String VIP_CONFIG_AWARD_MONTH_REQUIRED = "vip.config.award.month.required";
	/** {0}：月奖励配置不能大于下一级配置 */
	String VIP_CONFIG_AWARD_MONTH_INVALID = "vip.config.award.month.invalid";
	/** {0}：上个月充值保级条件不能大于晋级充值条件 */
	String VIP_CONFIG_PROMOTE_LAST_INVALID = "vip.config.promote.last.invalid";
	/** {0}：上个月打码保级条件不能大于晋级打码条件 */
	String VIP_CONFIG_PROMOTE_BET_LAST_INVALID = "vip.config.promote.bet.last.invalid";
	/** {0}：打码方式配置有误 */
	String VIP_CONFIG_BET_TYPE_INVALID = "vip.config.bet.type.invalid";
	/** {0}：打码倍数配置有误 */
	String VIP_CONFIG_BET_MULTIPLE_INVALID = "vip.config.bet.multiple.invalid";
	/** {0}：升级打码稽核配置不能大于下一级配置 */
	String VIP_AUDIT_CONFIG_BET_REQUIRED = "vip.audit.config.bet.required";

	/** 查询不到对应的转账记录！ */
	String TRANSFER_NOT_EXIST = "transfer.not.exist";
	/** 查询不到有效的转账记录，请确认您输入的交易哈希无误！ */
	String TRANSFER_INVALID = "transfer.invalid";

	/** 无可修改配置！ */
	String VENDOR_NOT_CONFIG_EDIT = "vendor.not.config.edit";
	/** 未查找到该厂商此游戏类型下该币种的配置！ */
	String VENDOR_CURRENCY_NOT_FOUND = "vendor.currency.not.found";
	/** 该厂商还没有{0}类型的游戏，不能修改分成阶梯 */
	String VENDOR_NOT_CONFIG_SHARE = "vendor.not.config.share";
	/** 商户厂家已存在，已经在其他地方开放给商户使用 */
	String VENDOR_APPLY_VENDOR_EXISTS = "vendor.apply.vendor.exists";
	/** 分成额度配置只能递增~ */
	String VENDOR_SHARE_INVALID = "vendor.share.invalid";
	/** 游戏类型不能为空 */
	String VENDOR_GAME_TYPE_REQUIRED = "vendor.game.type.required";
	/** 抽成比例不能为空 */
	String VENDOR_SHARE_REQUIRED = "vendor.share.required";
	/** 游戏类型配置有误 */
	String VENDOR_GAME_TYPE_INVALID = "vendor.game.type.invalid";
	/** 抽成比例配置只能递增 */
	String VENDOR_SHARE_INVALID_INCREASE = "vendor.share.invalid.increase";
	/** 抽成比例不能低于厂家固定比例配置:{0}% */
	String VENDOR_SHARE_INVALID_LESS = "vendor.share.invalid.less";
	/** 游戏厂商不能为空 */
	String VENDOR_ALIAS_REQUIRED = "vendor.alias.required";
	/** 请配置全部规则信息 */
	String BS_ALL_RULE_REQUIRED = "bs.all.rule.required";
	/** 请配置监控类型 */
	String BS_TYPE_REQUIRED = "bs.type.required";
	/** 请配置触发值，且大于1小于等于{0} */
	String BS_VALUE_REQUIRED = "bs.value.required";
	/** 请正确配置处罚范围 */
	String BS_PUNISH_RANGE_REQUIRED = "bs.punish.range.required";
	/** 请配置处罚方式 */
	String BS_PUNISH_TYPE_REQUIRED = "bs.punish.type.required";

	/** 相同的导出任务已存在，请勿重复导出！ */
	String EXPORT_TASK_EXISTS = "export.task.exists";

	/** 该类型下已存在相同的名称，请核实！ */
	String TAG_NAME_EXISTS = "tag.name.exists";

	/** 当前黑名单规则已存在 */
	String BLACKLIST_RULE_EXISTS = "blacklist.rule.exists";
	/** 当前黑名单规则不存在 */
	String BLACKLIST_RULE_NOT_EXISTS = "blacklist.rule.not.exists";
	/** 名单内包含其他商户数据，请刷新重试！ */
	String BLACKLIST_OTHER_NOT_EXISTS = "blacklist.other.not.exists";

	/** 已存在此三方支付通道 */
	String MERCHANT_PAY_CHANNEL_EXISTS = "merchant.pay.channel.exists";

	/** 未配置首页弹窗 */
	String TASK_HOME_POPUP_REQUIRED = "task.home.popup.required";
	/** 任务时长不得超过{0}天 */
	String TASK_DURATION_INVALID = "task.duration.invalid";
	/** 同IP人数错误 */
	String TASK_LIMIT_IP_NUM_INVALID = "task.limit.ip.num.invalid";
	/** 同设备人数错误 */
	String TASK_LIMIT_DEVICE_NUM_INVALID = "task.limit.device.num.invalid";
	/** 任务类型错误 */
	String TASK_TYPE_INVALID = "task.type.invalid";
	/** 保证阶梯数量在10个以内 */
	String TASK_TIER_COUNT_INVALID = "task.tier.count.invalid";
	/** 存在重复的阶梯金额 */
	String TASK_TIER_AMOUNT_DUPLICATE = "task.tier.amount.duplicate";
	/** 阶梯金额必须大于0且小于等于100,000,000 */
	String TASK_TIER_AMOUNT_INVALID = "task.tier.amount.invalid";
	/** 奖励金额必须大于0且小于等于100,000,000 */
	String TASK_REWARD_AMOUNT_INVALID = "task.reward.amount.invalid";
	/** 奖励金额与票券至少配置一个 */
	String TASK_REWARD_TICKET_INVALID = "task.reward.ticket.invalid";
	/** 无效的票券 */
	String TASK_TICKET_INVALID = "ticket.invalid";
	/** 奖励积分必须大于0且小于等于1000 */
	String TASK_REWARD_POINT_INVALID = "task.reward.point.invalid";
	/** 任务介绍/标题长度不能超过100 */
	String TASK_TITLE_LENGTH_INVALID = "task.title.length.invalid";
	/** 请至少配置一种类型的任务阶梯 */
	String TASK_TIER_REQUIRED = "task.tier.required";
	/** 应该配置4个宝箱 */
	String TASK_BOX_COUNT_INVALID = "task.box.count.invalid";
	/** 存在重复的活跃度阶梯 */
	String TASK_TIER_DUPLICATE = "task.tier.duplicate";
	/** 名称错误或超过100长度 */
	String TASK_NAME_INVALID = "task.name.invalid";
	/** 所需活跃度必须大于0且小于等于1000 */
	String TASK_POINT_INVALID = "task.point.invalid";
	/** 必须指定奖励类型 */
	String TASK_REWARD_TYPE_REQUIRED = "task.reward.type.required";
	/** 奖励类型为随机时，必须指定奖励金额的范围 */
	String TASK_REWARD_RANDOM_AMOUNT_INVALID = "task.reward.random.amount.invalid";
	/** 最小奖励金额必须大于0且小于等于100,000,000 */
	String TASK_REWARD_MIN_AMOUNT_INVALID = "task.reward.min.amount.invalid";
	/** 最大奖励金额必须大于最小金额且小于等于100,000,000 */
	String TASK_REWARD_MAX_AMOUNT_INVALID = "task.reward.max.amount.invalid";
	/** 一种任务类型只能有一个配置 */
	String TASK_REWARD_TYPE_DUPLICATE = "task.reward.type.duplicate";
	/** 排序序号必须大于0且小于等于{0} */
	String TASK_SORT_INVALID = "task.sort.invalid";
	/** 至少配置一种类型的任务 */
	String TASK_TYPE_REQUIRED = "task.type.required";
	/** 图标配置错误或长度超过100 */
	String TASK_ICON_INVALID = "task.icon.invalid";
	/** 任务介绍长度不能超过150 */
	String TASK_DESCRIPTION_INVALID = "task.description.invalid";
	/** 是否开启必须填写 */
	String TASK_ENABLE_REQUIRED = "task.enable.required";
	/** 提示气泡开关必须填写 */
	String TASK_TIP_REQUIRED = "task.tip.required";
	/** 注册任务缺少额外配置信息 */
	String TASK_REGISTER_CONFIG_REQUIRED = "task.register.config.required";
	/** 循环方式错误 */
	String TASK_LOOP_MODE_INVALID = "task.loop.mode.invalid";
	/** 稽核倍数错误 */
	String TASK_AUDIT_FACTOR_INVALID = "task.audit.factor.invalid";
	/** 是否允许重复循环开宝箱必须填写 */
	String TASK_EXTEND_REQUIRED = "task.extend.required";

	/** 重置时间不能为空！ */
	String RESET_TIME_REQUIRED = "reset.time.required";
	/** 重置时间只能小于当天！ */
	String RESET_TIME_INVALID = "reset.time.invalid";

	/** 该消息状态已变更，无法撤回！ */
	String MSG_STATUS_CHANGED_WITHDRAW = "msg.status.changed.withdraw";
	/** 该消息状态已变更，无法进行此操作！ */
	String MSG_STATUS_CHANGED_REVOKE = "msg.status.changed.revoke";
	/** 已发送的消息必须先撤销才能修改发送范围 */
	String MSG_STATUS_CHANGED_REVOKE_SENT = "msg.status.changed.revoke.sent";

	/** 导出账单总计 */
	String EXPORT_BILL_TOTAL = "export.bill.total";
	/** 列表合计 */
	String LIST_TOTAL = "list.total";
	// ================================================From=======================================================
	/** 类型 */
	String FIELD_TYPE = "field.type";
	/** 名称 */
	String FIELD_NAME = "field.name";
	/** 状态 */
	String FIELD_STATUS = "field.status";
	/** 图标 */
	String FIELD_ICON = "field.icon";
	/** 角标 */
	String FIELD_BADGE = "field.badge";
	/** 排序 */
	String FIELD_SORT = "field.sort";
	/** 标题 */
	String FIELD_TITLE = "field.title";
	/** 厂商 */
	String FIELD_VENDOR = "field.vendor";
	/** 商户 */
	String FIELD_MERCHANT = "field.merchant";
	/** 分成比例 */
	String FIELD_SHARE_RATE = "field.share.rate";
	/** 原因 */
	String FIELD_REASON = "field.reason";
	/** 用户ID */
	String FIELD_USER_ID = "field.user.id";
	/** 用户数量 */
	String FIELD_USER_COUNT = "field.user.count";
	/** 稽核金额 */
	String FIELD_AUDIT_AMOUNT = "field.audit.amount";
	/** 配置 */
	String FIELD_CONFIG = "field.config";
	/** 维护开关 */
	String FIELD_MAINTAIN_SWITCH = "field.maintain.switch";
	/** 游戏图 */
	String FIELD_GAME_IMG = "field.game.img";
	/** 遮罩图 */
	String FIELD_MASK_IMG = "field.mask.img";
	/** 厂家图片 */
	String FIELD_VENDOR_IMAGE = "field.vendor.image";
	/** 厂商别名 */
	String FIELD_VENDOR_ALIAS = "field.vendor.alias";
	/** 货币 */
	String FIELD_CURRENCY = "field.currency";
	/** 游戏类型 */
	String FIELD_GAME_TYPE = "field.game.type";
	/** 提示内容 */
	String FIELD_PROMPT = "field.prompt";
	/** 联系电话 */
	String FIELD_PHONE = "field.phone";
	/** 子域名 */
	String FIELD_SUB_DOMAIN = "field.sub.domain";
	/** 域名用途 */
	String FIELD_DOMAIN_POSITION = "field.domain.position";
	/** CDN节点 */
	String FIELD_CDN_PROVIDER = "field.cdn.provider";
	/** 用户名/后台账号 */
	String FIELD_ACCOUNT_USERNAME = "field.account.username";
	/** 昵称 */
	String FIELD_NICKNAME = "field.nickname";
	/** 账号密码 */
	String FIELD_ACCOUNT_PASSWORD = "field.account.password";
	/** 角色ID */
	String FIELD_ROLE_ID = "field.role.id";
	/** 人工单笔加款限额 */
	String FROM_LIMIT_ADD_REQUIRED = "from.limit.add.required";
	/** 人工单笔出款限额 */
	String FROM_LIMIT_WITHDRAW_REQUIRED = "from.limit.withdraw.required";
	/** 站点 */
	String FIELD_SITE = "field.site";
	/** 是否热门 */
	String FIELD_HOT = "field.hot";
	/** 是否推荐 */
	String FIELD_RECOMMEND = "field.recommend";
	/** 语言 */
	String FIELD_LANGUAGE = "field.language";
	/** 运营地区 */
	String FIELD_OPERATION_AREA = "field.operation.area";
	/** 商户名称 */
	String FIELD_MERCHANT_NAME = "field.merchant.name";
	/** 站点名称 */
	String FIELD_SITE_NAME = "field.site.name";
	/** 账号 */
	String FIELD_ACCOUNT = "field.account";
	/** 密码 */
	String FIELD_PASSWORD = "field.password";
	/** 代理模式 */
	String FIELD_AGENT_MODE = "field.agent.mode";
	/** 开版费 */
	String FIELD_OPEN_FEE = "field.open.fee";
	/** 线路费用 */
	String FIELD_LINE_COST = "field.line.cost";
	/** 游戏月保底 */
	String FIELD_GAME_MONTH_MIN = "field.game.month.min";
	/** 游戏厂家 */
	String FIELD_GAME_VENDOR = "field.game.vendor";
	/** 商户ID */
	String FIELD_MERCHANT_ID = "field.merchant.id";
	/** 金额 */
	String FIELD_AMOUNT = "field.amount";
	/** 备注 */
	String FIELD_REMARK = "field.remark";
	/** 日期 */
	String FIELD_DATE = "field.date";
	/** 描述 */
	String FIELD_DESCRIPTION = "field.description";
	/** 通知内容 */
	String FIELD_NOTIFY_CONTENT = "field.notify.content";
	/** 通知状态 */
	String FIELD_NOTIFY_STATUS = "field.notify.status";
	/** 提前天数 */
	String FIELD_AHEAD_DAY = "field.ahead.day";
	/** 提醒类型 */
	String FIELD_NOTIFY_TYPE = "field.notify.type";
	/** 最大站点数量 */
	String FIELD_MAX_SITE_NUM = "field.max.site.num";
	/** 链接 */
	String FIELD_LINK = "field.link";
	/** 商户等级 */
	String FIELD_MERCHANT_LEVEL = "field.merchant.level";
	/** 累计充值 */
	String FIELD_TOTAL_RECHARGE = "field.total.recharge";
	/** 启用站点 */
	String FIELD_ENABLE_SITE = "field.enable.site";
	/** 厂家 */
	String FIELD_VENDOR_NAME = "field.vendor.name";
	/** 设置比例 */
	String FIELD_SETTING_RATE = "field.setting.rate";
	/** RTP>100抽成 */
	String FIELD_RTP_UP_RATE = "field.rtp.up.rate";
	/** RTP<100抽成 */
	String FIELD_RTP_DN_RATE = "field.rtp.dn.rate";
	/** 维护开始类型 */
	String FIELD_STAND_BEGIN_TYPE = "field.stand.begin.type";
	/** 维护结束类型 */
	String FIELD_STAND_END_TYPE = "field.stand.end.type";
	/** 短信供应商 */
	String FIELD_SMS_PROVIDER = "field.sms.provider";
	/** 余额 */
	String FIELD_BALANCE = "field.balance";
	/** 提醒内容 */
	String FIELD_REMIND_CONTENT = "field.remind.content";
	/** 配置来源 */
	String FIELD_CONFIG_SOURCE = "field.config.source";
	/** 说明 */
	String FIELD_REMARK_DESCRIPTION = "field.remark.description";
	/** 所属类型 */
	String FIELD_TYPE_BELONG = "field.type.belong";
	/** 别名 */
	String FIELD_ALIAS = "field.alias";
	/** 值 */
	String FIELD_VALUE = "field.value";
	/** 规则配置 */
	String FIELD_RULE_CONFIG = "field.rule.config";
	/** 作用域 */
	String FIELD_SCOPE = "field.scope";
	/** 发送循环方式 */
	String FIELD_SEND_CYCLE = "field.send.cycle";
	/** 内容 */
	String FIELD_CONTENT = "field.content";
	/** 开始时间 */
	String FIELD_BEGIN_TIME = "field.begin.time";
	/** 结束时间 */
	String FIELD_END_TIME = "field.end.time";
	/** 等级 */
	String FIELD_LEVEL = "field.level";
	/** 风控配置 */
	String FIELD_RISK_CONFIG = "field.risk.config";
	/** 授信额度 */
	String FIELD_OVERDRAFT = "field.overdraft";
	/** 角色名称 */
	String FIELD_ROLE_NAME = "field.role.name";
	/** 角色简介 */
	String FIELD_ROLE_DESCRIPTION = "field.role.description";
	/** 菜单 */
	String FIELD_MENU = "field.menu";
	/** 跳转类型 */
	String FIELD_REDIRECT_TYPE = "field.redirect.type";
	/** 跳转地址 */
	String FIELD_REDIRECT_URL = "field.redirect.url";
	/** 宣传简介 */
	String FIELD_PROMOTION_DESCRIPTION = "field.promotion.description";
	/** 所在位置 */
	String FIELD_POSITION = "field.position";
	/** 开关 */
	String FIELD_SWITCH = "field.switch";
	/** 子类型 */
	String FIELD_SUB_TYPE = "field.sub.type";
	/** 群ID */
	String FIELD_CHAT_ID = "field.chat.id";
	/** 机器人token */
	String FIELD_BOT_TOKEN = "field.bot.token";
	/** 开关配置 */
	String FIELD_SWITCH_CONFIG = "field.switch.config";
	/** 姓名 */
	String FIELD_REAL_NAME = "field.real.name";
	/** 生日 */
	String FIELD_BIRTHDAY = "field.birthday";
	/** 性别 */
	String FIELD_GENDER = "field.gender";
	/** 邮箱 */
	String FIELD_EMAIL = "field.email";
	/** 手机号码 */
	String FIELD_MOBILE = "field.mobile";
	/** 标签ID */
	String FIELD_USER_TAG_ID = "field.user.tag.id";
	/** 高倍爆奖 */
	String FIELD_HIGH_MULTIPLE_AWARD = "field.high.multiple.award";
	/** 中奖金额 */
	String FIELD_AWARD_AMOUNT = "field.award.amount";
	/** 大额中奖 */
	String FIELD_LARGE_AWARD = "field.large.award";
	/** 当日会员获利比 */
	String FIELD_PROFIT_RATE = "field.profit.rate";
	/** 当日获利比触发额度值 */
	String FIELD_PROFIT_RATE_TRIGGER_VALUE = "field.profit.rate.trigger.value";
	/** 账单ID */
	String FIELD_BILL_ID = "field.bill.id";
	/** 宣传图 */
	String FIELD_PROMOTION_IMG = "field.promotion.img";
	/** 热门图 */
	String FIELD_HOT_IMG = "field.hot.img";
	/** 厂商ID */
	String FIELD_VENDOR_ID = "field.vendor.id";
	/** 分成配置 */
	String FIELD_SHARE_CONFIG = "field.share.config";
	/** 站点ID */
	String FIELD_SITE_ID = "field.site.id";
	/** 支付渠道 */
	String FIELD_PAYMENT_CHANNEL = "field.payment.channel";
	/** 支付钱包编码 */
	String FIELD_PAYMENT_WALLET_CODE = "field.payment.wallet.code";
	/** 控制类型 */
	String FIELD_CTRL_TYPE = "field.ctrl.type";
	/** 控制池 */
	String FIELD_CTRL_POOL = "field.ctrl.pool";
	/** 代理ID */
	String FIELD_AGENT_ID = "field.agent.id";
	/** 交易金额 */
	String FIELD_TRADE_AMOUNT = "field.trade.amount";
	/** 推广状态 */
	String FIELD_AGENT_OPEN = "field.agent.open";
	/** 提佣状态 */
	String FIELD_CASH_OPEN = "field.cash.open";
	/** 代理等级 */
	String FIELD_AGENT_LEVEL = "field.agent.level";
	/** 累计业绩 */
	String FIELD_TOTAL_PERFORMANCE = "field.total.performance";
	/** 会员ID */
	String FIELD_MEMBER_ID = "field.member.id";
	/** 游戏抽成% */
	String FIELD_PLAY_RATE = "field.play.rate";
	/** pgc游戏rtp>100抽成% */
	String FIELD_PGC_RTP_UP_RATE = "field.pgc.rtp.up.rate";
	/** pgc游戏rtp<100抽成% */
	String FIELD_PGC_RTP_DN_RATE = "field.pgc.rtp.dn.rate";
	/** 充值支付通道费率% */
	String FIELD_PAY_CHANNEL_RATE = "field.pay.channel.rate";
	/** 包名 */
	String FIELD_PACKAGE_NAME = "field.package.name";
	/** 启动图 */
	String FIELD_START_IMAGE = "field.start.image";
	/** 客户端 */
	String FIELD_CLIENT = "field.client";
	/** 路径 */
	String FIELD_PAGE_URL = "field.page.url";
	/** H5路径 */
	String FIELD_H5_URL = "field.h5.url";
	/** 渠道ID */
	String FIELD_CHANNEL_ID = "field.channel.id";
	/** 渠道成本 */
	String FIELD_CHANNEL_COST = "field.channel.cost";
	/** 移除像素 */
	String FIELD_IGNORE_TRACKER = "field.ignore.tracker";
	/** SDK事件上报ID */
	String FIELD_APP_EVENT_ID = "field.app.event.id";
	/** 包类型 */
	String FIELD_PACKAGE_TYPE = "field.package.type";
	/** 版本描述 */
	String FIELD_VERSION_DESC = "field.version.desc";
	/** 更新类型 */
	String FIELD_UPDATE_TYPE = "field.update.type";
	/** 最小余额限制 */
	String FIELD_MIN_BALANCE = "field.min.balance";
	/** 最大余额限制 */
	String FIELD_MAX_BALANCE = "field.max.balance";
	/** 余额限制 */
	String FIELD_BALANCE_LIMIT = "field.balance.limit";
	/** 链接直达位置 */
	String FIELD_LINK_POSITION = "field.link.position";
	/** 域名 */
	String FIELD_DOMAIN = "field.domain";
	/** 下载页模版 */
	String FIELD_DOWNLOAD_TEMPLATE = "field.download.template";
	/** 代理账号 */
	String FIELD_AGENT_ACCOUNT = "field.agent.account";
	/** 下载提示 */
	String FIELD_DOWNLOAD_TIP = "field.download.tip";
	/** 包大小 */
	String FIELD_PACKAGE_SIZE = "field.package.size";
	/** 投放渠道 */
	String FIELD_AD_CHANNEL = "field.ad.channel";
	/** 投放渠道像素 */
	String FIELD_AD_CHANNEL_TRACKER = "field.ad.channel.tracker";
	/** 投放渠道Token */
	String FIELD_AD_CHANNEL_TOKEN = "field.ad.channel.token";
	/** 返奖率 */
	String FIELD_CTRL_RATE = "from.ctrl.rate";
	/** 年化利率 */
	String FIELD_YEAR_RATE = "from.year.rate";
	/** 结算收益周期 */
	String FIELD_SETTLE_PERIOD = "from.settle.period";
	/** 每次最低存入金额 */
	String FIELD_MIN_DEPOSIT = "from.min.deposit";
	/** 每次最低领取金额 */
	String FIELD_MIN_RECEIVE = "from.min.receive";
	/** 利息封顶 */
	String FIELD_INTEREST_CAP = "from.interest.cap";
	/** 利息稽核倍数 */
	String FIELD_AUDIT_TIMES = "from.audit.times";
	/** 申请人备注 */
	String FIELD_APPLY_REMARK = "field.apply.remark";
	/** 账单月份 */
	String FIELD_BILL_MONTH = "field.bill.month";
	/** 网络流量线路费用 */
	String FIELD_NET_FEE = "field.net.fee";
	/** 其他费用 */
	String FIELD_OTHER_FEE = "field.other.fee";
	/** 优惠减免额度 */
	String FIELD_DISCOUNT_AMOUNT = "field.discount.amount";
	/** 最低限额 */
	String FIELD_MIN_LIMIT = "field.min.limit";
	/** 最大限额 */
	String FIELD_MAX_LIMIT = "field.max.limit";
	/** 手续费 */
	String FIELD_HANDLING_FEE = "field.handling.fee";
	/** 启用站点 */
	String FIELD_SITE_OPEN = "field.site.open";
	/** 支付方式 */
	String FIELD_PAYMENT_WAY = "field.payment.way";
	/** 提现笔数 */
	String FIELD_CASH_COUNT = "field.cash.count";
	/** 风控审核 */
	String FIELD_RISK_REVIEW = "field.risk.review";
	/** 热门类型 */
	String FIELD_HOT_TYPE = "field.hot.type";
	/** 热门设置 */
	String FIELD_HOT_SETTING = "field.hot.setting";
	/** 充值金额 */
	String FIELD_RECHARGE_AMOUNT = "field.recharge.amount";
	/** 赠送比例 */
	String FIELD_GIVE_RATE = "field.give.rate";
	/** 赠送金额 */
	String FIELD_GIVE_AMOUNT = "field.give.amount";
	/** 最低准入金额 */
	String FIELD_MIN_ADMIT_AMOUNT = "field.min.admit.amount";
	/** 奖励金额 */
	String FIELD_REWARD_AMOUNT = "field.reward.amount";
	/** 稽核倍数 */
	String FIELD_AUDIT_MULTIPLE = "field.audit.multiple";
	/** 文件名 */
	String FIELD_FILE_NAME = "field.file.name";
	/** 时长（ms） */
	String FIELD_LEN = "field.len";
	/** 大小 */
	String FIELD_SIZE = "field.size";
	/** 提现限制 */
	String FIELD_CASH_LIMIT = "field.cash.limit";
	/** 后端备注 */
	String FIELD_SERVICE_REMARK = "field.service.remark";
	/** 前端备注 */
	String FIELD_CLIENT_REMARK = "field.client.remark";
	/** 文件名不能为空 */
	String FIELD_FILE_NAME_REQUIRED = "field.file.name.required";
	/** 兑换密码 */
	String FIELD_EXCHANGE_PASSWORD = "field.exchange.password";
	/** 颜色 */
	String FIELD_COLOR = "field.color";
	/** 任务类型 */
	String FIELD_TASK_TYPE = "field.task.type";
	/** 状态不能为空 */
	String FIELD_STATUS_REQUIRED = "field.status.required";
	/** 推广标签的控制类型 */
	String FIELD_PROMOTION_CTRL_TYPE = "field.promotion.ctrl.type";
	/** 最小余额限制的RTP */
	String FIELD_MIN_BALANCE_RTP = "field.min.balance.rtp";
	/** 最大余额限制的RTP */
	String FIELD_MAX_BALANCE_RTP = "field.max.balance.rtp";
	/** 方式 */
	String FIELD_WAY = "field.way";
	/** 代理模式 */
	String FIELD_AGENT_MODE_NAME = "field.agent.mode.name";
	/** 佣金提取方式 */
	String FIELD_RECEIVE_WAY = "field.receive.way";
	/** 用户类型 */
	String FIELD_USER_TYPE = "field.user.type";
	/** 站点名称 */
	String AGENT_MODEL_SITE_NAME = "export.agentModel.siteName";
	/** 站点ID */
	String AGENT_MODEL_SITE_ID = "export.agentModel.siteId";
	/** 代理模式 */
	String AGENT_MODEL_AGENT_MODEL = "export.agentModel.agentModel_";
	/** 代理模式名称 */
	String AGENT_MODEL_AGENT_MODEL_NAME = "export.agentModel.agentModelName";
	/** 代理账号 */
	String AGENT_MODEL_AGENT_ACCOUNT = "export.agentModel.agentAccount";
	/** 代理ID */
	String AGENT_MODEL_AGENT_ID = "export.agentModel.agentUserId";
	/** 组别 */
	String AGENT_MODEL_AGENT_GROUP = "export.agentModel.agentGroup";
	/** 上级代理账号 */
	String AGENT_MODEL_INVITE_ACCOUNT = "export.agentModel.inviteAccount";
	/** 上级代理ID */
	String AGENT_MODEL_INVITE_USER_ID = "export.agentModel.inviteUserId";
	/** 注册来源 */
	String AGENT_MODEL_REGISTER_SOURCE = "export.agentModel.userSource_";
	/** 直属团队成员 */
	String AGENT_MODEL_NEXT_USER_COUNT = "export.agentModel.nextUserCount";
	/** 新增直属团队成员 */
	String AGENT_MODEL_NEXT_ADD_USER_COUNT = "export.agentModel.nextAddUserCount";
	/** 直属首充成员/金额 */
	String AGENT_MODEL_NEXT_FIRST_RECHARGE_ = "export.agentModel.nextFirstRecharge_";
	/** 直属注册首充成员/金额 */
	String AGENT_MODEL_NEXT_REGISTER_FIRST_ = "export.agentModel.nextRegisterFirst_";
	/** 直属充值成员/金额 */
	String AGENT_MODEL_NEXT_RECHARGE_ = "export.agentModel.nextRecharge_";
	/** 新增直属充值成员/金额 */
	String AGENT_MODEL_NEXT_ADD_RECHARGE_ = "export.agentModel.nextAddRecharge_";
	/** 直属提现成员/金额 */
	String AGENT_MODEL_NEXT_CASH_ = "export.agentModel.nextCash_";
	/** 直属投注成员/金额 */
	String AGENT_MODEL_NEXT_PLAY_COIN_ = "export.agentModel.nextPlayCoin_";
	/** 直属业绩 */
	String AGENT_MODEL_NEXT_PERFORMANCE = "export.agentModel.nextPerformance";
	/** 其他团队成员 */
	String AGENT_MODEL_OTHER_USER_COUNT = "export.agentModel.otherUserCount";
	/** 新增其他团队成员 */
	String AGENT_MODEL_OTHER_ADD_USER_COUNT = "export.agentModel.otherAddUserCount";
	/** 其他首充成员/金额 */
	String AGENT_MODEL_OTHER_FIRST_RECHARGE_ = "export.agentModel.otherFirstRecharge_";
	/** 其他注册首充成员/金额 */
	String AGENT_MODEL_OTHER_REGISTER_FIRST_ = "export.agentModel.otherRegisterFirst_";
	/** 其他充值成员/金额 */
	String AGENT_MODEL_OTHER_RECHARGE_ = "export.agentModel.otherRecharge_";
	/** 新增其他充值成员/金额 */
	String AGENT_MODEL_OTHER_ADD_RECHARGE_ = "export.agentModel.otherAddRecharge_";
	/** 其他提现成员/金额 */
	String AGENT_MODEL_OTHER_CASH_ = "export.agentModel.otherCash_";
	/** 其他投注成员/金额 */
	String AGENT_MODEL_OTHER_PLAY_COIN_ = "export.agentModel.otherPlayCoin_";
	/** 其他业绩 */
	String AGENT_MODEL_OTHER_PERFORMANCE = "export.agentModel.otherPerformance";
	/** 总团队成员 */
	String AGENT_MODEL_ALL_USER_COUNT = "export.agentModel.allUserCount";
	/** 新增总团队成员 */
	String AGENT_MODEL_ALL_ADD_USER_COUNT = "export.agentModel.allAddUserCount";
	/** 总首充成员/金额 */
	String AGENT_MODEL_ALL_FIRST_RECHARGE_ = "export.agentModel.allFirstRecharge_";
	/** 总注册首充成员/金额 */
	String AGENT_MODEL_ALL_REGISTER_FIRST_ = "export.agentModel.allRegisterFirst_";
	/** 总充值成员/金额 */
	String AGENT_MODEL_ALL_RECHARGE_ = "export.agentModel.allRecharge_";
	/** 新增总充值成员/金额 */
	String AGENT_MODEL_ALL_ADD_RECHARGE_ = "export.agentModel.allAddRecharge_";
	/** 总提现成员/金额 */
	String AGENT_MODEL_ALL_CASH_ = "export.agentModel.allCash_";
	/** 总投注成员/金额 */
	String AGENT_MODEL_ALL_PLAY_COIN_ = "export.agentModel.allPlayCoin_";
	/** 总业绩 */
	String AGENT_MODEL_ALL_PERFORMANCE = "export.agentModel.allPerformance";
	/** 个人充值 */
	String AGENT_MODEL_USER_RECHARGE = "export.agentModel.userRecharge";
	/** 个人提现 */
	String AGENT_MODEL_USER_CASH = "export.agentModel.userCash";
	/** 个人有效投注 */
	String AGENT_MODEL_USER_PLAY_COIN = "export.agentModel.userPlayCoin";
	/** 个人输赢 */
	String AGENT_MODEL_USER_WIN_COIN = "export.agentModel.userWinCoin";
	/** 累计佣金 */
	String AGENT_MODEL_TOTAL_COMMISSION = "export.agentModel.totalCommission";
	/** 累计领取 */
	String AGENT_MODEL_RECEIVE_AMOUNT = "export.agentModel.receiveAmount";
	/** 未领取 */
	String AGENT_MODEL_UN_RECEIVE_AMOUNT = "export.agentModel.unReceiveAmount";
	/** 提佣方式 */
	String AGENT_MODEL_RECEIVE_WAY = "export.agentModel.receiveWay_";
	/** 成为代理时间 */
	String AGENT_MODEL_ADD_TIME = "export.agentModel.addTime";
	/** 推广链接 */
	String AGENT_MODEL_TRACK_URL = "export.agentModel.trackUrl";
	/** 会员状态 */
	String AGENT_MODEL_USER_STATUS = "export.agentModel.userStatus_";
	/** 结算日期 */
	String AGENT_MODEL_SETTLE_DATE = "export.agentModel.settleDate_";
	/** 直属有效投注返佣 */
	String NEXT_VALID_COIN_COMMISSION = "export.agentModel.nextValidCoinCommission";
	/** 直属充值返佣 */
	String NEXT_RECHARGE_COMMISSION = "export.agentModel.nextRechargeCommission";
	/** 直属注册返佣 */
	String NEXT_REG_COMMISSION = "export.agentModel.nextRegCommission";
	/** 其他有效投注返佣 */
	String OTHER_VALID_COIN_COMMISSION = "export.agentModel.otherValidCoinCommission";
	/** 其他充值返佣 */
	String OTHER_RECHARGE_COMMISSION = "export.agentModel.otherRechargeCommission";
	/** 其他注册返佣 */
	String OTHER_REG_COMMISSION = "export.agentModel.otherRegCommission";
	/** 额外成就返佣 */
	String ADDITIONAL_COMMISSION = "export.agentModel.additionalCommission";
	/** 直属有效投注金额/业绩 */
	String NEXT_VALID_COIN = "export.agentModel.nextValidCoin_";
	/** 直属充值/首充金额 */
	String NEXT_RECHARGE_AND_FIRST = "export.agentModel.nextRechargeAndFirst_";
	/** 直属注册数 */
	String NEXT_REG_COUNT = "export.agentModel.nextRegCount";
	/** 其他有效投注数/业绩 */
	String OTHER_VALID_COIN = "export.agentModel.otherValidCoin_";
	/** 其他充值/首充金额 */
	String OTHER_RECHARGE_AND_FIRST = "export.agentModel.otherRechargeAndFirst_";
	/** 其他注册数 */
	String OTHER_REG_COUNT = "export.agentModel.otherRegCount";

	/** 商户分成比例必填 */
	String FROM_SHARE_RATE_REQUIRED = "from.share.rate.required";
	/** 商户分成比例只能有两位小数 */
	String FROM_SHARE_RATE_DECIMAL = "from.share.rate.decimal";
	/** 状态开关必填 */
	String FROM_STATUS_REQUIRED = "from.status.required";
	/** 维护开关必填 */
	String FROM_MAINTAIN_SWITCH_REQUIRED = "from.maintain.switch.required";
	/** 请输入子域名！ */
	String FROM_SUB_DOMAIN_REQUIRED = "from.sub.domain.required";
	/** CDN节点不可用，请更换节点！ */
	String FROM_CDN_PROVIDER_INVALID = "from.cdn.provider.invalid";
	/** 同样节点下子域名不能相同！ */
	String FROM_SUB_DOMAIN_DUPLICATE = "from.sub.domain.duplicate";
	/** 请输入域名！ */
	String FROM_DOMAIN_REQUIRED = "from.domain.required";
	/** 最多批量添加{0}个域名！ */
	String FROM_DOMAIN_MAX = "from.domain.max";
	/** 只支持配置顶级域名，请核实：{0} */
	String FROM_DOMAIN_TOP = "from.domain.top";
	/** 请选择皮肤配置 */
	String FORM_MERCHANT_SKIN_REQUIRED = "form.merchant.skin.required";
	/** 请选择支付通道配置 */
	String FORM_MERCHANT_PAY_REQUIRED = "form.merchant.pay.required";
	/** 请选择登录方式 */
	String FORM_MERCHANT_LOGIN_WAYS_REQUIRED = "form.merchant.login.ways.required";
	/** 请选择活动配置 */
	String FORM_MERCHANT_PROMOTION_REQUIRED = "form.merchant.promotion.required";
	/** 请填写商务联系昵称 */
	String FORM_MERCHANT_BUSINESS_NAME_REQUIRED = "form.merchant.business.name.required";
	/** 请填写商务联系方式 */
	String FORM_MERCHANT_BUSINESS_TG_REQUIRED = "form.merchant.business.tg.required";
	/** 商务telegram输入有误 */
	String FORM_MERCHANT_BUSINESS_TG_INVALID = "form.merchant.business.tg.invalid";
	/** 商户telegram输入有误 */
	String FORM_MERCHANT_TG_INVALID = "form.merchant.tg.invalid";
	/** 请填写商户联系方式 */
	String FORM_MERCHANT_TG_REQUIRED = "form.merchant.tg.required";
	/** 月贡献返佣比例不能超过100% */
	String FORM_MERCHANT_MONTH_REBATE_INVALID = "form.merchant.month.rebate.invalid";
	/** VIP配置不能为空 */
	String FORM_MERCHANT_VIP_REQUIRED = "form.merchant.vip.required";
	/** 短信登录或账号注册登录至少勾选1个 */
	String FORM_MERCHANT_LOGIN_WAY_INVALID = "form.merchant.login.way.invalid";
	/** 仅损益模式才能选择控赢 */
	String FORM_MERCHANT_CTRL_WIN_REQUIRED = "form.merchant.ctrl.win.required";
	/** 商户{0}由于欠费自动维护，不能进行操作 */
	String MERCHANT_AUTO_MAINTAIN_STATUS_ERROR = "merchant.auto.maintain.status.error";
	/** 必须选择至少一种语言 */
	String FROM_LANGUAGE_REQUIRED = "from.language.required";
	/** 必须包含英语语言 */
	String FROM_LANGUAGE_ENGLISH = "from.language.english";
	/** 该商户必须包含所选国家的默认语言：{0} */
	String FROM_LANGUAGE_DEFAULT = "from.language.default";
	/** {0}不需要配置提醒参数! */
	String FROM_NOTIFY_PARAM_REQUIRED = "from.notify.param.required";
	/** 月账单生成不需要配置提醒参数 */
	String FROM_NOTIFY_PARAM_MONTH_REQUIRED = "from.notify.param.month.required";
	/** 请选择修改模式 */
	String FROM_MODE_REQUIRED = "from.mode.required";
	/** 商户ID不能为空 */
	String FROM_MERCHANT_ID_REQUIRED = "from.merchant.id.required";
	/** 请配置打码模式RTP>100分成比例 */
	String FROM_RTP_UP_REQUIRED = "from.rtp.up.required";
	/** 请配置打码模式RTP<100分成比例 */
	String FROM_RTP_DN_REQUIRED = "from.rtp.dn.required";
	/** 请配置损益模式分成比例 */
	String FROM_SHARE_RATE_PLAY_REQUIRED = "from.share.rate.play.required";
	/** 设置比例不能为空 */
	String FROM_SETTING_RATE_REQUIRED = "from.setting.rate.required";
	/** 维护开始时间不能为空 */
	String FROM_STAND_BEGIN_TIME_REQUIRED = "from.stand.begin.time.required";
	/** 维护结束时间必须大于开始时间 */
	String FROM_STAND_END_TIME_REQUIRED = "from.stand.end.time.required";
	/** IP输入有误 */
	String FROM_IP_INVALID = "from.ip.invalid";
	/** 尚不支持配置此类型！ */
	String FROM_TYPE_NOT_SUPPORT = "from.type.not.support";
	/** 值不能为空！ */
	String FROM_VALUE_REQUIRED = "from.value.required";
	/** 公告必须配置结束时间 */
	String FROM_NOTICE_END_TIME_REQUIRED = "from.notice.end.time.required";
	/** 商户{0}不存在! */
	String FROM_MERCHANT_NOT_EXIST = "from.merchant.not.exist";
	/** 商户ID不能存在重复 */
	String FROM_MERCHANT_ID_DUPLICATE = "from.merchant.id.duplicate";
	/** 会员黑名单规则无法限制注册 */
	String FROM_LIMIT_TYPE_REGISTER_REQUIRED = "from.limit.type.register.required";
	/** 高倍爆奖与高倍爆奖中奖金额必须同时配置 */
	String FROM_HIGH_MULTIPLE_AWARD_REQUIRED = "from.high.multiple.award.required";
	/** 会员获利比与获利比触发额度值必须同时配置 */
	String FROM_PROFIT_RATE_REQUIRED = "from.profit.rate.required";
	/** 开启风控预警才可设置间隔时间 */
	String FROM_WARN_INTERVAL_REQUIRED = "from.warn.interval.required";
	/** 最小限额不能低于通道起提金额:{0} */
	String FROM_MIN_AMOUNT_REQUIRED = "from.min.amount.required";
	/** 最大限额不能超过通道限高金额:{0} */
	String FROM_MAX_AMOUNT_REQUIRED = "from.max.amount.required";
	/** 最低推荐金额不能低于：{0} */
	String FROM_MIN_REFERRAL_AMOUNT_REQUIRED = "from.min.referral.amount.required";
	/** 最大推荐金额不能超过：{0} */
	String FROM_MAX_REFERRAL_AMOUNT_REQUIRED = "from.max.referral.amount.required";
	/** 固定层级不能为空 */
	String FROM_FIXED_LAYER_REQUIRED = "from.fixed.layer.required";
	/** 自动层级不能为空 */
	String FROM_AUTO_LAYER_REQUIRED = "from.auto.layer.required";
	/** 不支持的控制类型 */
	String FROM_CTRL_TYPE_NOT_SUPPORT = "from.ctrl.type.not.support";
	/** 不可使用此返奖率 */
	String FROM_SHARE_RATE_NOT_SUPPORT = "from.share.rate.not.support";
	/** 交易金额不能为0 */
	String FROM_TRADE_AMOUNT_ZERO = "from.trade.amount.zero";
	/** 请填写周期设置 */
	String FROM_INTERVAL_REQUIRED = "from.interval.required";
	/** 请选择游戏类型！ */
	String FROM_GAME_TYPE_REQUIRED = "from.game.type.required";
	/** 返佣不能超过投注业绩！ */
	String FROM_SHARE_RATE_INVALID = "from.share.rate.invalid";
	/** 角色ID错误 */
	String FROM_ROLE_ID_INVALID = "from.role.id.invalid";
	/** 请核实包名格式！ */
	String FROM_PACKAGE_NAME_INVALID = "from.package.name.invalid";
	/** APP路径不能为空！ */
	String FROM_APP_PATH_REQUIRED = "from.app.path.required";
	/** 最小余额限制不能设置超过1000000的限制额度 */
	String FROM_MIN_BALANCE_REQUIRED = "from.min.balance.required";
	/** 最小余额限制不能大于最大余额限制 */
	String FROM_MIN_BALANCE_INVALID = "from.min.balance.invalid";
	/** 最大余额限制不能设置超过1000000的限制额度 */
	String FROM_MAX_BALANCE_REQUIRED = "from.max.balance.required";
	/** 最小余额限制不能超过最大余额限制的80% */
	String FROM_MAX_BALANCE_INVALID = "from.max.balance.invalid";
	/** 采纳才能赠送金额 */
	String FROM_ACCEPT_GIVE_AMOUNT = "from.accept.give.amount";
	/** {0}通道尚未开通~ */
	String FROM_CHANNEL_NOT_OPEN = "from.channel.not.open";
	/** 最大兑换金额必须大于最小兑换金额 */
	String FROM_EXCHANGE_AMOUNT_INVALID = "from.exchange.exchange.amount.invalid";
	/** 最小兑换金额不能低于:{0} */
	String FROM_MIN_EXCHANGE_AMOUNT_INVALID = "from.min.exchange.amount.invalid";
	/** 最大兑换金额不能超过:{0} */
	String FROM_MAX_EXCHANGE_AMOUNT_INVALID = "from.max.exchange.amount.invalid";
	/** 风控项不能为空 */
	String FROM_RISK_ITEM_REQUIRED = "from.risk.item.required";
	/** 提现次数必须大于等于0 */
	String FROM_CASH_COUNT_INVALID = "from.cash.count.invalid";
	/** 提现金额必须大于0 */
	String FROM_CASH_AMOUNT_INVALID = "from.cash.amount.invalid";
	/** 累计充值金额必须大于等于0 */
	String FROM_TOTAL_RECHARGE_INVALID = "from.total.recharge.invalid";
	/** 热门开关必填 */
	String FROM_HOT_SWITCH_REQUIRED = "from.hot.switch.required";
	/** 游戏最低准入必填 */
	String FROM_GAME_MIN_REQUIRED = "from.game.min.required";
	/** 非{0}不允许操作 */
	String FROM_NOT_SUPPORT_OPERATE = "from.not.support.operate";
	/** 审核状态错误 */
	String FROM_AUDIT_STATUS_INVALID = "from.audit.status.invalid";
	/** 领取方式错误 */
	String FROM_RECEIVE_TYPE_INVALID = "from.receive.type.invalid";
	/** 请先选择要派奖的用户，且最多只能选择1000条记录 */
	String FROM_RECEIVE_USER_REQUIRED = "from.receive.user.required";
	/** 活动状态不允许修改 */
	String FROM_PROMOTION_STATUS_NOT_ALLOWED = "from.promotion.status.not.allowed";
	/** 是否基于输入的VIP等级同步全部其他等级的配置 */
	String FROM_SYNC_ALL_REQUIRED = "from.sync.all.required";
	/** 余额最大值返奖率必须是小于100% */
	String FROM_BALANCE_MAX_RATE_INVALID = "from.balance.max.rate.invalid";
	/** 不能同时设置比例与额度~ */
	String FROM_RATE_AND_AMOUNT_INVALID = "from.rate.and.amount.invalid";
	/** 稽核厂家不能为空 */
	String FROM_AUDIT_FACTORY_NOT_NULL = "from.audit.factory.not.null";
	/** 账单日期不能为空 */
	String FROM_PERIOD_REQUIRED = "from.period.required";
	/** 请传入有效的计算层级 */
	String FROM_LEVEL_INVALID = "from.level.invalid";
	/** 请传入有效的返佣计算依据 */
	String FROM_BASIS_INVALID = "from.basis.invalid";
	/** 请选择有效会员条件 */
	String FROM_USER_TYPE_COND_INVALID = "from.user.type.cond.invalid";
	/** 请选择主题 */
	String FROM_THEME_REQUIRED = "from.theme.required";
	/** 请选择推广快捷方式 */
	String FROM_IM_LIST_REQUIRED = "from.im.list.required";
	/** 请上传其他方式LOGO */
	String FROM_OTHER_IMG_REQUIRED = "from.other.img.required";
	/** 返佣比例配置有误 */
	String FROM_RATE_CONFIG_INVALID = "from.rate.config.invalid";
	/** 请配置返佣比例 */
	String FROM_RATE_REQUIRED = "from.rate.required";
	/** 代理模式不支持修改上级 */
	String AGENT_MODEL_SIMPLE_ERROR = "agent.model.simple.error";
	/** 顶部奖金配置有误 */
	String FROM_TOP_REWARD_REQUIRED = "from.top.reward.required";
	/** 月度前三配置有误 */
	String FROM_MONTH_REWARD_REQUIRED = "from.month.reward.required";
	/** 当前选择的代理模式不支持实时结算 */
	String FROM_AGENT_MODEL_NOT_REAL = "from.agent.model.not.real";
	/** 请完善返佣依据配置 */
	String FROM_SIMPLE_BASIS_REQUIRED = "from.simple.basis.required";

	// =====================================活动From====================================================================================
	/** 奖励类型错误 */
	String PROMOTION_AWARD_TYPE_INVALID = "promotion.award.type.invalid";
	/** 请选择活动方式~ */
	String PROMOTION_TYPE_REQUIRED = "promotion.type.required";
	/** 同注册IP上限不能小于0~ */
	String PROMOTION_MAX_REGISTER_AMOUNT_REQUIRED = "promotion.max.register.amount.required";
	/** 同注册设备上限不能小于0~ */
	String PROMOTION_MAX_REGISTER_DEVICE_REQUIRED = "promotion.max.register.device.required";
	/** 推广充值条件错误 */
	String PROMOTION_COND_INVALID = "promotion.cond.invalid";
	/** 推广打码条件错误 */
	String PROMOTION_CODE_INVALID = "promotion.code.invalid";
	/** 请选择是否需要手机验证~ */
	String PROMOTION_MOBILE_VERIFY_REQUIRED = "promotion.mobile.verify.required";
	/** 累计达到人数可提现必须大于0！ */
	String PROMOTION_CASH_COUNT_REQUIRED = "promotion.cash.count.required";
	/** 未知的推广类型 */
	String PROMOTION_TYPE_UNKNOWN = "promotion.type.unknown";
	/** 下级会员限制类型错误 */
	String PROMOTION_LIMIT_TYPE_INVALID = "promotion.limit.type.invalid";
	/** 最多可设置{0}个奖励阶梯 */
	String PROMOTION_MAX_TIERS_INVALID = "promotion.max.tiers.invalid";
	/** 请配置阶梯金额 */
	String PROMOTION_TIER_AMOUNT_REQUIRED = "promotion.tier.amount.required";
	/** 固定金额必须大于0 */
	String PROMOTION_FIX_AMOUNT_INVALID = "promotion.fix.amount.invalid";
	/** 最小金额必须大于0 */
	String PROMOTION_MIN_AMOUNT_INVALID = "promotion.min.amount.invalid";
	/** 最大金额必须大于0 */
	String PROMOTION_MAX_AMOUNT_INVALID = "promotion.max.amount.invalid";
	/** 有效推广配置应该顺序递增 */
	String PROMOTION_TIER_NUMBER_INVALID = "promotion.tier.number.invalid";
	/** 邀请人数限制条件不能为空 */
	String PROMOTION_INVITE_NUMBER_LIMIT_REQUIRED = "promotion.invite.number.limit.required";
	/** 邀请人数限制配置缺失 */
	String PROMOTION_INVITE_NUMBER_LIMIT_INVALID = "promotion.invite.number.limit.invalid";
	/** 同ip人数不可计入统计配置有误 */
	String PROMOTION_SAME_IP_NUMBER_INVALID = "promotion.same.ip.number.invalid";
	/** 绑定手机号码配置有误 */
	String PROMOTION_BIND_MOBILE_INVALID = "promotion.bind.mobile.invalid";
	/** 达到充值金额配置有误 */
	String PROMOTION_REACH_AMOUNT_INVALID = "promotion.reach.amount.invalid";
	/** 达到打码金额配置有误 */
	String PROMOTION_REACH_CODE_INVALID = "promotion.reach.code.invalid";
	/** 稽核倍数不能为空 */
	String PROMOTION_AUDIT_MULTIPLE_REQUIRED = "promotion.audit.multiple.required";
	/** 选取平台数据有误 */
	String PROMOTION_PLATFORM_DATA_INVALID = "promotion.platform.data.invalid";
	/** 选取游戏类型有误 */
	String PROMOTION_GAME_TYPE_INVALID = "promotion.game.type.invalid";
	/** 邀请人数不能为空 */
	String PROMOTION_INVITE_NUMBER_REQUIRED = "promotion.invite.number.required";
	/** 邀请奖励金额不能为空 */
	String PROMOTION_INVITE_AMOUNT_REQUIRED = "promotion.invite.amount.required";
	/** 闯关总打码不能为空 */
	String PROMOTION_GAME_CODE_REQUIRED = "promotion.game.code.required";
	/** 闯关额外奖金不能为空 */
	String PROMOTION_GAME_BONUS_REQUIRED = "promotion.game.bonus.required";
	/** 邀请人数配置有误 */
	String PROMOTION_INVITE_NUMBER_INVALID = "promotion.invite.number.invalid";
	/** 邀请奖励金额配置有误 */
	String PROMOTION_INVITE_AMOUNT_INVALID = "promotion.invite.amount.invalid";
	/** 闯关总打码配置有误 */
	String PROMOTION_GAME_CODE_INVALID = "promotion.game.code.invalid";
	/** 闯关额外奖金配置有误 */
	String PROMOTION_GAME_BONUS_INVALID = "promotion.game.bonus.invalid";
	/** 展示方式错误 */
	String PROMOTION_SHOW_TYPE_INVALID = "promotion.show.type.invalid";
	/** 优惠申请开关不能为空 */
	String PROMOTION_APPLY_SWITCH_REQUIRED = "promotion.apply.switch.required";
	/** 申请条件错误 */
	String PROMOTION_APPLY_COND_INVALID = "promotion.apply.cond.invalid";
	/** 是否按固定次数不能为空 */
	String PROMOTION_FIX_SWITCH_REQUIRED = "promotion.fix.switch.required";
	/** 条件金额错误 */
	String PROMOTION_COND_AMOUNT_INVALID = "promotion.cond.amount.invalid";
	/** 固定申请次数应该大于0且小于100 */
	String PROMOTION_FIX_COUNT_INVALID = "promotion.fix.count.invalid";
	/** 阶梯最多10个，至少2个 */
	String PROMOTION_TIERS_INVALID = "promotion.tiers.invalid";
	/** 阶梯的申请次数应该大于0且小于100！ */
	String PROMOTION_TIER_COUNT_INVALID = "promotion.tier.count.invalid";
	/** 金额应该大于前一阶梯 */
	String PROMOTION_TIER_AMOUNT_INVALID = "promotion.tier.amount.invalid";
	/** 申请次数应该大于前一阶梯！ */
	String PROMOTION_TIER_COUNT_LINE_INVALID = "promotion.tier.count.line.invalid";
	/** 每日申请次数限制 应该大于0且小于100 */
	String PROMOTION_DAILY_NUM_INVALID = "promotion.daily.num.invalid";
	/** 每日申请次数 应该大于0且小于100！ */
	String PROMOTION_DAILY_NUM_LINE_INVALID = "promotion.daily.num.line.invalid";
	/** 活动期间申请次数上限应该大于 每日申请次数 且小于500 */
	String PROMOTION_APPLY_NUM_INVALID = "promotion.apply.num.invalid";
	/** 扣除方式错误 */
	String PROMOTION_DEDUCT_TYPE_INVALID = "promotion.deduct.type.invalid";
	/** 申请需要回答的问题列表不能为空，且最多10条 */
	String PROMOTION_QUESTION_REQUIRED = "promotion.question.required";
	/** 申请问题长度不能超过400字符 */
	String PROMOTION_QUESTION_INVALID = "promotion.question.invalid";

	/** 奖金方式 */
	String FIELD_AWARD_TYPE = "field.award.type";
	/** 奖金 */
	String FIELD_AWARD_BONUS = "field.award.bonus";
	/** 奖励最小金额 */
	String FIELD_AWARD_MIN_AMOUNT = "field.award.min.amount";
	/** 奖励最大金额 */
	String FIELD_AWARD_MAX_AMOUNT = "field.award.max.amount";
	/** 奖金比例 */
	String FIELD_AWARD_RATIO = "field.award.ratio";

	/** 结束时间应该大于当前时间 */
	String PROMOTION_END_TIME_INVALID = "promotion.end.time.invalid";
	/** 活动开始时间必须在结束时间之前 */
	String PROMOTION_BEGIN_TIME_INVALID = "promotion.end.time.line.invalid";
	/** 活动展示开始时间必须在展示结束时间之前 */
	String PROMOTION_SHOW_BEGIN_TIME_INVALID = "promotion.show.begin.time.invalid";
	/** 活动时间必须在展示时间之内 */
	String PROMOTION_TIME_INVALID = "promotion.time.invalid";
	/** 宣传图不能为空 */
	String PROMOTION_IMAGE_REQUIRED = "promotion.image.required";
	/** 用户VIP等级数据有误 */
	String PROMOTION_VIP_LEVEL_INVALID = "promotion.vip.level.invalid";
	/** 稽核厂家数据有误 */
	String PROMOTION_AUDIT_VENDOR_INVALID = "promotion.audit.vendor.invalid";
	/** 循环方式不能为空 */
	String PROMOTION_CYCLE_MODE_REQUIRED = "promotion.cycle.mode.required";
	/** 请配置阶梯 */
	String PROMOTION_TIERS_RULES_REQUIRED = "promotion.tiers.rules.required";

	/** 兑换类型错误 */
	String PROMOTION_EXCHANGE_TYPE_INVALID = "promotion.exchange.type.invalid";
	/** 通用型兑换码不能为空 */
	String PROMOTION_EXCHANGE_CODE_REQUIRED = "promotion.exchange.code.required";
	/** 兑换码数量应该 > 0 */
	String PROMOTION_EXCHANGE_CODE_NUM_INVALID = "promotion.exchange.code.num.invalid";
	/** 有效期开始时间不能为空 */
	String PROMOTION_EXCHANGE_CODE_BEGIN_TIME_REQUIRED = "promotion.exchange.code.begin.time.required";
	/** 有效期截止时间应该大于当前时间，且小于活动结束时间 */
	String PROMOTION_EXCHANGE_CODE_END_TIME_INVALID = "promotion.exchange.code.end.time.invalid";
	/** 充值限制不能为空 */
	String PROMOTION_RECHARGE_LIMIT_REQUIRED = "promotion.recharge.limit.required";
	/** 展示彩金错误 */
	String PROMOTION_AWARD_SHOW_INVALID = "promotion.award.show.invalid";
	/** 固定金额错误 */
	String PROMOTION_AWARD_FIX_INVALID = "promotion.award.fix.invalid";
	/** 最小金额错误 */
	String PROMOTION_AWARD_MIN_INVALID = "promotion.award.min.invalid";
	/** 最大金额错误 */
	String PROMOTION_AWARD_MAX_INVALID = "promotion.award.max.invalid";
	/** 平均金额错误 */
	String PROMOTION_AWARD_AVG_INVALID = "promotion.award.avg.invalid";
	/** 期望金额错误 */
	String PROMOTION_AWARD_EXPECT_INVALID = "promotion.award.expect.invalid";
	/** 前端展示金额范围填写错误 */
	String PROMOTION_AWARD_SHOW_AMOUNT_INVALID = "promotion.award.show.amount.invalid";
	/** 请至少配置两个概率阶梯 */
	String PROMOTION_AWARD_RATIO_TIERS_REQUIRED = "promotion.award.ratio.tiers.required";
	/** 最多10个额度比例 */
	String PROMOTION_AWARD_RATIO_TIERS_INVALID = "promotion.award.ratio.tiers.invalid";
	/** 第一阶梯应该大于最低奖励额：{0} */
	String PROMOTION_RATIO_TIERS_FIRST_INVALID = "promotion.ratio.tiers.first.invalid";
	/** 最后阶梯应该等于最大奖励额：{0} */
	String PROMOTION_RATIO_TIERS_LAST_INVALID = "promotion.ratio.tiers.last.invalid";
	/** 概率总和必须等于100 */
	String PROMOTION_RATIO_TIERS_SUM_INVALID = "promotion.ratio.tiers.sum.invalid";
	/** 请保证阶梯金额是递增的，问题金额在 {0} 附近 */
	String PROMOTION_TIERS_AMOUNT_INVALID = "promotion.tiers.amount.invalid";

	/** 红包类型错误 */
	String PROMOTION_REWARD_TYPE_INVALID = "promotion.reward.type.invalid";
	/** 请配置派发时间 */
	String PROMOTION_DISPATCH_TIME_REQUIRED = "promotion.dispatch.time.required";
	/** 最多可设置24个时间区间 */
	String PROMOTION_DISPATCH_TIME_INVALID = "promotion.dispatch.time.invalid";
	/** 派发时间错误 */
	String PROMOTION_DISPATCH_TIME_ERROR = "promotion.dispatch.time.error";
	/** 请保证时段的有序，下一时段开始时间必须大于上一时段结束时间 */
	String PROMOTION_DISPATCH_TIME_LINE_INVALID = "promotion.dispatch.time.line.invalid";
	/** 领取条件类型错误 */
	String PROMOTION_CONDITION_TYPE_INVALID = "promotion.condition.type.invalid";
	/** 条件金额错误 */
	String PROMOTION_CONDITION_AMOUNT_INVALID = "promotion.condition.amount.invalid";
	/** 限抢次数错误 */
	String PROMOTION_CONDITION_LIMIT_INVALID = "promotion.condition.limit.invalid";
	/** 活动红包总数错误 */
	String PROMOTION_RE_TOTAL_INVALID = "promotion.re.total.invalid";
	/** 单用户活动周期内最大领取次数错误 */
	String PROMOTION_RE_LIMIT_INVALID = "promotion.re.limit.invalid";
	/** 首页是否展示图标 */
	String PROMOTION_RE_SHOW_INVALID = "promotion.re.show.invalid";
	/** 时段红包总数不可大于活动红包总数 */
	String PROMOTION_RE_PERIOD_LIMIT_INVALID = "promotion.re.period.limit.invalid";
	/** 红包总金额不可超过10亿 */
	String PROMOTION_RE_TOTAL_AMOUNT_INVALID = "promotion.re.total.amount.invalid";
	/** 红包总金额不可小于单个红包 */
	String PROMOTION_RE_AMOUNT_MIN_INVALID = "promotion.re.amount.min.invalid";
	/** 红包总金额不可小于单个时段的发放总金额 */
	String PROMOTION_RE_AMOUNT_PERIOD_INVALID = "promotion.re.amount.period.invalid";
	/** 红包总金额不可小于单个红包的金额最大值 */
	String PROMOTION_RE_AMOUNT_SINGLE_INVALID = "promotion.re.amount.single.invalid";
	/** 红包总金额不可小于单个时段的最小随机发放总金额 */
	String PROMOTION_RE_AMOUNT_INVALID = "promotion.re.amount.invalid";

	/** 规则不能为空 */
	String PROMOTION_RULES_REQUIRED = "promotion.rules.required";
	/** 余额限制必须大于0 */
	String PROMOTION_BALANCE_GT_INVALID = "promotion.balance.gt.invalid";
	/** 只能选择一种奖励类型 */
	String PROMOTION_SELECT_TYPE_INVALID = "promotion.select.type.invalid";
	/** 奖励比例必须大于0 */
	String PROMOTION_BONUS_RATIO_INVALID = "promotion.bonus.ratio.invalid";
	/** 固定金额必须大于0 */
	String PROMOTION_BONUS_FIX_INVALID = "promotion.bonus.fix.invalid";
	/** 打码金额限制必须大于0 */
	String PROMOTION_REACH_PLAY_INVALID = "promotion.reach.play.invalid";
	/** 当日奖励发放次数必须大于0 */
	String PROMOTION_DAY_LIMIT_INVALID = "promotion.day.limit.invalid";
	/** 总奖励发放次数必须大于0 */
	String PROMOTION_TOTAL_LIMIT_INVALID = "promotion.total.limit.invalid";
	/** 总奖励发放次数不可小于当日奖励发放次数 */
	String PROMOTION_TOTAL_DAY_LIMIT_INVALID = "promotion.total.limit.invalid.2";
	/** 最高赠送金额必须大于0 */
	String PROMOTION_MAX_BONUS_INVALID = "promotion.max.bonus.invalid";
	/** 再次进入游戏需要充值不能为空 */
	String PROMOTION_GAME_RECHARGE_INVALID = "promotion.game.recharge.invalid";
	/** 只可进控制游戏不能为空 */
	String PROMOTION_GAME_CONTROL_INVALID = "promotion.game.control.invalid";
	/** 控输返奖率必须 < 100 */
	String PROMOTION_CONTROL_RATE_DN_INVALID = "promotion.control.rate.dn.invalid";
	/** 控赢返奖率必须 > 100 */
	String PROMOTION_CONTROL_RATE_UP_INVALID = "promotion.control.rate.up.invalid";
	/** 选择控输时请配置概率，且小于等于100 */
	String PROMOTION_CONTROL_RATE_DN = "promotion.control.rate.dn";
	/** 选择控赢时请配置概率，且小于等于100 */
	String PROMOTION_CONTROL_RATE_UP = "promotion.control.rate.up";
	/** 概率之和不可超过100 */
	String PROMOTION_RATE_SUM_INVALID = "promotion.rate.sum.invalid";
	/** 不可选用此控制类型 */
	String PROMOTION_CONTROL_TYPE_INVALID = "promotion.control.type.invalid";
	/** 当日奖励发放次数必须大于等于原值 */
	String PROMOTION_DAY_LIMIT_GT_INVALID = "promotion.day.limit.gt.invalid";
	/** 总奖励发放次数必须大于等于原值 */
	String PROMOTION_TOTAL_LIMIT_GT_INVALID = "promotion.total.limit.gt.invalid";
	/** 奖励上限金额必须大于等于原值 */
	String PROMOTION_MAX_BONUS_GT_INVALID = "promotion.max.bonus.gt.invalid";

	/** 应该配置损失额度阶梯 */
	String PROMOTION_LOSS_TIERS_REQUIRED = "promotion.loss.tiers.required";

	/** 存在重复的充值损失阶梯 */
	String PROMOTION_LOSS_TIERS_DUPLICATE = "promotion.loss.tiers.duplicate";
	/** 昨日负盈利不能为空 */
	String PROMOTION_LOSS_AMOUNT_REQUIRED = "promotion.loss.tiers.amount.required";
	/** 今日充值不能为空 */
	String PROMOTION_TODAY_RECHARGE_REQUIRED = "promotion.today.recharge.required";
	/** 额外奖励比例错误 */
	String PROMOTION_ADDITION_RATIO_INVALID = "promotion.addition.ratio.invalid";
	/** 签到方式不能为空 */
	String PROMOTION_SIGN_MODE_REQUIRED = "promotion.sign.mode.required";
	/** 签到类型不能为空 */
	String PROMOTION_SIGN_TYPE_REQUIRED = "promotion.sign.type.required";
	/** 签到周期不能为空 */
	String PROMOTION_SIGN_CYCLE_REQUIRED = "promotion.sign.cycle.required";
	/** 奖励金额不能为空 */
	String PROMOTION_BONUS_AMOUNT_REQUIRED = "promotion.bonus.amount.required";
	/** 奖励金额范围不能为空 */
	String PROMOTION_BONUS_AMOUNT_RANGE_REQUIRED = "promotion.bonus.amount.range.required";
	/** 规则配置错误 */
	String PROMOTION_RULE_INVALID = "promotion.rule.invalid";
	/** 充值要求不能为空 */
	String PROMOTION_RECHARGE_REQUIRED = "promotion.recharge.required";
	/** 打码要求不能为空 */
	String PROMOTION_PLAY_REQUIRED = "promotion.play.required";
	/** 已签图片不能为空 */
	String PROMOTION_SIGN_IMAGE_REQUIRED = "promotion.sign.image.required";
	/** 未签图片不能为空 */
	String PROMOTION_UNSIGN_IMAGE_REQUIRED = "promotion.unsign.image.required";
	/** 充值要求配置有误 */
	String PROMOTION_RECHARGE_INVALID = "promotion.recharge.invalid";
	/** 打码要求配置有误 */
	String PROMOTION_PLAY_REQUIRE_INVALID = "promotion.play.require.invalid";

	/** 兑换幸运值额度不能小于0！ */
	String PROMOTION_EXCHANGE_AMOUNT_INVALID = "promotion.exchange.amount.invalid";
	/** 转盘配置不能为空！ */
	String PROMOTION_TURNTABLE_REQUIRED = "promotion.turntable.required";
	/** 消耗幸运值不能小于0！ */
	String PROMOTION_EXCHANGE_LUCKY_INVALID = "promotion.exchange.lucky.invalid";
	/** 转盘配置必须为{0}个~ */
	String PROMOTION_TURNTABLE_SIZE_INVALID = "promotion.turntable.size.invalid";
	/** 奖励金额不能小于0 */
	String PROMOTION_BONUS_AMOUNT_GT_INVALID = "promotion.bonus.amount.gt.invalid";
	/** 中奖率不能小于0 */
	String PROMOTION_RATE_GT_INVALID = "promotion.rate.gt.invalid";
	/** 图标不能为空！ */
	String PROMOTION_ICON_REQUIRED = "promotion.icon.required";
	/** 中奖率总和 > 0 且 <= 100 */
	String PROMOTION_FACTOR_SUM_INVALID = "promotion.factor.sum.invalid";
	/** 只能编辑今日未进行的游戏! */
	String PROMOTION_CHALLENGE_GAME_CHANGED = "promotion.challenge.game.changed";

	/** 每日转盘 转盘配置不能为空！ */
	String PROMOTION_DAILY_TURNTABLE_REQUIRED = "promotion.daily.turntable.required";
	/** 虚拟奖励开启 虚拟奖励不能为空 */
	String PROMOTION_DAILY_TURNTABLE_VIRTUAL_REWARD_REQUIRED = "promotion.daily.turntable.reward.required";
	/** 虚拟奖励开启 虚拟最大奖励应该比最小奖励大 */
	String PROMOTION_DAILY_TURNTABLE_VIRTUAL_REWARD_INVALID = "promotion.daily.turntable.reward.invalid";
	/** 每日转盘 白银转盘配置必须为6个~ */
	String PROMOTION_DAILY_TURNTABLE_SILVER_SIZE_INVALID = "promotion.daily.turntable.silver.size.invalid";
	/** 每日转盘 黄金转盘配置必须为8个~ */
	String PROMOTION_DAILY_TURNTABLE_GOLD_SIZE_INVALID = "promotion.daily.turntable.gold.size.invalid";
	/** 每日转盘 砖石转盘配置必须为10个~ */
	String PROMOTION_DAILY_TURNTABLE_DIAMOND_SIZE_INVALID = "promotion.daily.turntable.diamond.size.invalid";
	/** 每日转盘 vip等级不能为空 */
	String PROMOTION_DAILY_TURNTABLE_VIP_LEVEL_REQUIRED = "promotion.daily.turntable.vip.level.required";
	/** 每日转盘 vip等级配置错误 */
	String PROMOTION_DAILY_TURNTABLE_VIP_LEVEL_INVALID = "promotion.daily.turntable.vip.level.invalid";
	/** 每日转盘 每日旋转次数不能小于0 */
	String PROMOTION_DAILY_TURNTABLE_SPINS_PER_DAY_GT_INVALID = "promotion.daily.turntable.spins.per.day.gt.invalid";

	/** 拼团 原价金额不能为空！ */
	String PROMOTION_JOIN_GROUP_ORIGINAL_AMOUNT_REQUIRED = "promotion.join.group.original.amount.required";
	/** 拼团 拼团金额不能为空！ */
	String PROMOTION_JOIN_GROUP_AMOUNT_REQUIRED = "promotion.join.group.amount.required";
	/** 拼团 拼团价格不能高于原价价格 */
	String PROMOTION_JOIN_GROUP_AMOUNT_GT_ORIGINAL_AMOUNT = "promotion.join.group.amount.gt.original.amount";
	/** 拼团 金额配置错误！ */
	String PROMOTION_JOIN_GROUP_AMOUNT_ERROR = "promotion.join.group.amount.error";
	/** 拼团 拼团人数不能为空！ */
	String PROMOTION_JOIN_GROUP_NUM_REQUIRED = "promotion.join.group.num.required";
	/** 拼团 拼团人数配置错误 至少2人成团，最高设为10人！ */
	String PROMOTION_JOIN_GROUP_NUM_INVALID = "promotion.join.group.num.invalid";
	/** 拼团 请填写团周期！ */
	String PROMOTION_JOIN_GROUP_CYCLE_REQUIRED = "promotion.join.group.cycle.required";
	/** 拼团 团周期配置错误！ */
	String PROMOTION_JOIN_GROUP_CYCLE_INVALID = "promotion.join.group.cycle.invalid";
	/** 拼团 自动成团条件 团周期倒计时要小于团周期 */
	String PROMOTION_JOIN_GROUP_AUTO_GROUP_COUNTDOWN_GT_GROUP_CYCLE = "promotion.join.group.auto.group.countdown.gt.group.cycle";
	/** 拼团 自动成团条件 团周期倒计时不能小于1h */
	String PROMOTION_JOIN_GROUP_AUTO_GROUP_COUNTDOWN_INVALID = "promotion.join.group.auto.group.countdown.invalid";
	/** 拼团 自动成团条件 团人数要小于团人数 */
	String PROMOTION_JOIN_GROUP_AUTO_GROUP_NUM_GT_GROUP_NUM = "promotion.join.group.auto.group.num.gt.group.num";
	/** 拼团 自动成团条件 团人数不能小于1人 */
	String PROMOTION_JOIN_GROUP_AUTO_GROUP_NUM_INVALID = "promotion.join.group.auto.group.num.invalid";
	/** 拼团 参团/发团条件不能为空！ */
	String PROMOTION_JOIN_GROUP_CONDITIONS_CFG_REQUIRED = "promotion.join.group.conditions.cfg.required";
	/** 拼团 新人参与条件 注册天数≥1天内 */
	String PROMOTION_JOIN_GROUP_NEWCOMER_REGISTER_DAY_INVALID = "promotion.join.group.newcomer.register.day.invalid";
	/** 拼团 折扣时效不能为空！ */
	String PROMOTION_JOIN_GROUP_DISCOUNT_TIMELINESS_REQUIRED = "promotion.join.group.discount.timeliness.required";
	/** 拼团 折扣时效 配置错误 */
	String PROMOTION_JOIN_GROUP_DISCOUNT_TIMELINESS_INVALID = "promotion.join.group.discount.timeliness.invalid";
	/** 拼团 拼团记录不能为空！ */
	String PROMOTION_JOIN_GROUP_RECORD_REQUIRED = "promotion.join.group.record.required";
	/** 拼团 拼团记录 2.人工：设置条数（不低于10条,校对提示：≥10条数据信息） */
	String PROMOTION_JOIN_GROUP_RECORD_NUM_INVALID = "promotion.join.group.record.num.invalid";
	/** 拼团 新人参团再次减免配置错误！ */
	String PROMOTION_JOIN_GROUP_NEWCOMER_INVALID = "promotion.join.group.newcomer.invalid";

	/** 排行时长不能为空 */
	String PROMOTION_RANK_DURATION_INVALID = "promotion.rank.duration.invalid";
	/** 排行榜类型不能为空 */
	String PROMOTION_RANK_TYPE_INVALID = "promotion.rank.type.invalid";
	/** 排行榜最低存款时长不能为空 */
	String PROMOTION_RANK_MIN_DURATION_INVALID = "promotion.rank.min.duration.invalid";
	/** 排行榜最低存款时长只能选择{0}或累计 */
	String PROMOTION_RANK_MIN_DURATION_TYPE_INVALID = "promotion.rank.min.duration.type.invalid";
	/** 排行榜奖励配置不能为空 */
	String PROMOTION_RANK_CONFIG_INVALID = "promotion.rank.config.invalid";
	/** 排行榜名次不能为空 */
	String PROMOTION_RANK_CONFIG_MIN_INVALID = "promotion.rank.config.min.invalid";
	/** 排行榜名次不能小于1 */
	String PROMOTION_RANK_CONFIG_ZERO_INVALID = "promotion.rank.config.zero.invalid";
	/** 排行榜奖励不能为空 */
	String PROMOTION_RANK_CONFIG_REWARD_INVALID = "promotion.rank.config.reward.invalid";
	/** 排行榜奖励不能为0 */
	String PROMOTION_RANK_CONFIG_REWARD_ZERO_INVALID = "promotion.rank.config.reward.zero.invalid";
	/** 排行榜名次不是连续的 */
	String PROMOTION_RANK_CONFIG_NOT_CONTINUOUS = "promotion.rank.config.not.continuous";
	/** 排行榜名次最大不能超过1000 */
	String PROMOTION_RANK_CONFIG_MAX_INVALID = "merchant.rank.config.max.invalid";
	/** 机器人排名最大不能超过{0} */
	String PROMOTION_RANK_ROBOT_CONFIG_MAX_INVALID = "merchant.rank.robot.config.max.invalid";
	/** 下一档排名要大于前一档 {0} */
	String PROMOTION_RANK_ROBOT_RANK_INVALID = "merchant.rank.robot.rank.invalid";
	/** 机器人排名不能小于1 */
	String PROMOTION_RANK_ROBOT_CONFIG_ZERO_INVALID = "merchant.rank.robot.config.zero.invalid";
	/** 排行榜领取时间不能为空 */
	String PROMOTION_RANK_RECEIVE_TIME_START_INVALID = "promotion.rank.receive.time.invalid";
	/** 奖励过期天数 */
	String PROMOTION_EXPIRE_TIME = "promotion.expire.time";

	/** 目标站点没有相应的配置 */
	String TARGET_SITE_NOT_FOUND_CONFIG = "target.site.not.found.config";
	/** 代理模式类型不匹配，不能进行复制 */
	String AGENT_MODEL_TYPE_MISMATCH = "agent.model.type.mismatch";
	/** 本日已更改过一次，次日才可更改 */
	String AGENT_MODEL_UPDATE_LIMIT = "agent.model.update.limit";

	// ==================================枚举国际化=======================================
	// 商户账单状态
	/** 待平台核对 */
	String MERCHANT_BILL_STATUS_INIT = "merchant.bill.status.init";
	/** 待商户核对 */
	String MERCHANT_BILL_STATUS_WAIT_AUDIT = "merchant.bill.status.wait.audit";
	/** 未支付 */
	String MERCHANT_BILL_STATUS_WAIT_PAY = "merchant.bill.status.wait.pay";
	/** 已结清 */
	String MERCHANT_BILL_STATUS_OK = "merchant.bill.status.ok";

	// 游戏厂商账单状态
	/** 待核对 */
	String VENDOR_BILL_STATUS_INIT = "vendor.bill.status.init";
	/** 待确认 */
	String VENDOR_BILL_STATUS_WAIT_CONFIRM = "vendor.bill.status.wait.confirm";
	/** 待支付 */
	String VENDOR_BILL_STATUS_WAIT_PAY = "vendor.bill.status.wait.pay";
	/** 已结清 */
	String VENDOR_BILL_STATUS_OK = "vendor.bill.status.ok";

	// 游戏获利监控状态
	/** 待处理 */
	String USER_PROFIT_SPY_STATUS_PENDING = "user.profit.spy.status.pending";
	/** 已忽略 */
	String USER_PROFIT_SPY_STATUS_IGNORE = "user.profit.spy.status.ignore";
	/** 已冻结 */
	String USER_PROFIT_SPY_STATUS_FREEZE = "user.profit.spy.status.freeze";
	/** 禁止领取奖励 */
	String USER_PROFIT_SPY_STATUS_NO_PRIZES = "user.profit.spy.status.no.prizes";

	// 签到周期
	/** 7天 */
	String SIGN_CYCLE_DAYS_7 = "sign.cycle.days.7";
	/** 1天 */
	String SIGN_CYCLE_DAYS_1 = "sign.cycle.days.1";
	/** 2天 */
	String SIGN_CYCLE_DAYS_2 = "sign.cycle.days.2";
	/** 3天 */
	String SIGN_CYCLE_DAYS_3 = "sign.cycle.days.3";
	/** 10天 */
	String SIGN_CYCLE_DAYS_10 = "sign.cycle.days.10";
	/** 15天 */
	String SIGN_CYCLE_DAYS_15 = "sign.cycle.days.15";
	/** 30天 */
	String SIGN_CYCLE_DAYS_30 = "sign.cycle.days.30";

	// 宣传类型
	/** 弹窗 */
	String BANNER_TYPE_POP = "banner.type.pop";
	/** 悬浮 */
	String BANNER_TYPE_MODAL = "banner.type.modal";
	/** 首页banner */
	String BANNER_TYPE_HOME_BANNER = "banner.type.home.banner";
	/** 首页中部中尺寸banner */
	String BANNER_TYPE_HOME_MID_SIZE = "banner.type.home.mid.size";
	/** 首页中部小尺寸banner */
	String BANNER_TYPE_HOME_SMALL_SIZE = "banner.type.home.small.size";
	// collect Status
	/** 已归集 */
	String COLLECT_STATUS_COLLECTED = "collect.status.collected";
	/** 未归集 */
	String COLLECT_STATUS_WAIT_COLLECT = "collect.status.wait.collect";

	// 代理模式
	/** 打码模式 */
	String AGENT_MODE_PLAY_MODE = "agent.mode.play.mode";
	/** 盈亏模式 */
	String AGENT_MODE_WIN_LOSE_MODE = "agent.mode.win.lose.mode";

	// 余额修正 类型
	/** 人工加款 */
	String BALANCE_ADJUST_IN = "balance.adjust.in";
	/** 人工扣款 */
	String BALANCE_ADJUST_OUT = "balance.adjust.out";
	/** 人工赠送 */
	String BALANCE_ADJUST_REWARD = "coin.trade.type.adjust.reward";

	// 申请状态
	/** 待申请 */
	String APPLY_STATUS_INIT = "apply.status.init";
	/** 待审核 */
	String APPLY_STATUS_REVIEW = "apply.status.review";
	/** 审核通过 */
	String APPLY_STATUS_PASS = "apply.status.pass";
	/** 审核拒绝 */
	String APPLY_STATUS_REJECT = "apply.status.reject";

	// 网红奖励赠送类型
	/** 单人赠送 */
	String BLOGGER_REWARD_SINGLE = "blogger.reward.single";
	/** 批量赠送 */
	String BLOGGER_REWARD_BATCH = "blogger.reward.batch";
	/** 群体手动赠送 */
	String BLOGGER_REWARD_RANGE_MANUAL = "blogger.reward.range.manual";
	/** 群体自动赠送 */
	String BLOGGER_REWARD_RANGE_AUTO = "blogger.reward.range.auto";

	// 网红排行榜类型
	/** 直属首充人数排名 */
	String BLOGGER_RANK_RECHARGE_COUNT = "blogger.rank.recharge.count";
	/** 直属佣金排名 */
	String BLOGGER_RANK_COMMISSION = "blogger.rank.commission";

	// 虚拟彩金池展示位置
	/** 热门 */
	String DISPLAY_POSITION_HOT = "display.position.hot";
	/** banner上方 */
	String DISPLAY_POSITION_BANNER = "display.position.banner";
	/** 跑马灯上方 */
	String DISPLAY_POSITION_MARQUEE = "display.position.marquee";
	/** 棋牌 */
	String DISPLAY_POSITION_TABLE = "display.position.table";
	/** 捕鱼 */
	String DISPLAY_POSITION_FISH = "display.position.fish";
	/** 电子 */
	String DISPLAY_POSITION_DIGITAL = "display.position.digital";
	/** 体育 */
	String DISPLAY_POSITION_SPORT = "display.position.sport";
	/** 视讯 */
	String DISPLAY_POSITION_LIVE = "display.position.live";

	// 用户操作类型
	/** 冻结 */
	String USER_HANDLE_TYPE_FREEZE = "user.handle.type.freeze";
	/** 恢复正常 */
	String USER_HANDLE_TYPE_RECOVER_FROZEN = "user.handle.type.recover.frozen";
	/** 游戏踢下线 */
	String USER_HANDLE_TYPE_GAME_OFFLINE = "user.handle.type.game.offline";
	/** 禁止进入游戏 */
	String USER_HANDLE_TYPE_GAME_LIMIT = "user.handle.type.game.limit";
	/** 解除游戏禁用 */
	String USER_HANDLE_TYPE_RECOVER_GAME = "user.handle.type.recover.game";
	/** 禁止提现 */
	String USER_HANDLE_TYPE_CASH_LIMIT = "user.handle.type.cash.limit";
	/** 解除提现禁用 */
	String USER_HANDLE_TYPE_RECOVER_CASH = "user.handle.type.recover.cash";
	/** 禁止优惠 */
	String USER_HANDLE_TYPE_GIFT_LIMIT = "user.handle.type.gift.limit";
	/** 解除优惠限制 */
	String USER_HANDLE_TYPE_RECOVER_GIFT = "user.handle.type.recover.gift";
	/** 修改层级 */
	String USER_HANDLE_TYPE_LAYER = "user.handle.type.layer";
	/** 修改标签 */
	String USER_HANDLE_TYPE_CHANGE_TAG = "user.handle.type.change.tag";

	// 菜单
	/** 首页 */
	String APP_LAYOUT_MENU_INDEX = "@app.layout.menu.index";
	/** 优惠 */
	String APP_LAYOUT_MENU_PROMOTION = "@app.layout.menu.promotion";
	/** VIP */
	String APP_LAYOUT_MENU_VIP = "@app.layout.menu.vip";
	/** 充值 */
	String APP_LAYOUT_MENU_RECHARGE = "@app.layout.menu.recharge";
	/** 我的 */
	String APP_LAYOUT_MENU_ME = "@app.layout.menu.me";
	/** 兑换 */
	String APP_LAYOUT_MENU_EXCHANGE = "@app.layout.menu.exchange";
	/** 代理 */
	String APP_LAYOUT_MENU_AGENT = "@app.layout.menu.agent";
	/** 定制版皮肤-启动页 */
	String APP_LAYOUT_MENU_LOADING_PAGE = "@app.layout.menu.loadingPage";
	/** 游戏 */
	String APP_LAYOUT_MENU_GAME_MODULE = "@app.layout.menu.game.module";
	/** 钱包 */
	String APP_LAYOUT_MENU_WALLET = "@app.layout.menu.wallet";
	/** 左侧入口 */
	String APP_LAYOUT_MENU_LEFT = "@app.layout.menu.left";
	/** 投注记录 */
	String APP_LAYOUT_MENU_RECORD = "@app.layout.menu.record";
	/** 领取记录 */
	String APP_LAYOUT_MENU_REWARD = "@app.layout.menu.reward";
	/** 邀请分享 */
	String APP_LAYOUT_MENU_SHARE = "@app.layout.menu.share";
	/** 服务器 */
	String APP_LAYOUT_MENU_SERVER = "@app.layout.menu.server";
	/** 下载 */
	String APP_LAYOUT_MENU_DOWNLOAD = "@app.layout.menu.download";
	/** 客服 */
	String APP_LAYOUT_MENU_SERVICE = "@app.layout.menu.service";
	/** FAQ帮助 */
	String APP_LAYOUT_MENU_FAQ = "@app.layout.menu.faq";
	/** 关于我们 */
	String APP_LAYOUT_MENU_ABOUT = "@app.layout.menu.about";
	/** 语言 */
	String APP_LAYOUT_MENU_LANGUAGE = "@app.layout.menu.language";
	/** 音乐 */
	String APP_LAYOUT_MENU_MUSIC = "@app.layout.menu.music";
	/** 找回余额 */
	String APP_LAYOUT_MENU_RETRIEVE_BALANCE = "@app.layout.menu.retrieveBalance";
	/** 交易报表 */
	String APP_LAYOUT_MENU_TRADE_REPORT = "@app.layout.menu.tradeReport";
	/** 投注记录 */
	String APP_LAYOUT_MENU_PLAY_RECORD = "@app.layout.menu.playRecord";
	/** 数据报表 */
	String APP_LAYOUT_MENU_DATA_REPORT = "@app.layout.menu.dataReport";
	/** 账号绑定 */
	String APP_LAYOUT_MENU_ACCOUNT_BIND = "@app.layout.menu.accountBind";
	/** 安全中心 */
	String APP_LAYOUT_MENU_SECURITY_CENTER = "@app.layout.menu.securityCenter";
	/** 消息 */
	String APP_LAYOUT_MENU_MESSAGE = "@app.layout.menu.message";
	/** 存款 */
	String APP_LAYOUT_MENU_DEPOSIT = "@app.layout.menu.deposit";
	/** 提现 */
	String APP_LAYOUT_MENU_CASH = "@app.layout.menu.cash";
	/** 游戏类型 */
	String APP_LAYOUT_MENU_GAME_TYPE = "@app.layout.menu.game.type";
	/** 兑换码 */
	String APP_LAYOUT_MENU_REDEEM_CODE = "@app.layout.menu.redeemCode";
	/** 登录设备 */
	String APP_LAYOUT_MENU_LOGIN_DEVICE = "@app.layout.menu.loginDevice";
	/** 有奖反馈 */
	String APP_LAYOUT_MENU_REWARDED_FEEDBACK = "@app.layout.menu.rewardedFeedback";
	/** 常见问题 */
	String APP_LAYOUT_MENU_COMMON_PROBLEMS = "@app.layout.menu.commonProblems";

	/** 票券 */
	String APP_LAYOUT_MENU_TICKET = "@app.layout.menu.ticket";
	/** 公积金 */
	String APP_LAYOUT_MENU_DEPOSIT_POOL = "@app.layout.menu.deposit.pool";
	/** 注册 */
	String APP_LAYOUT_MENU_REGISTER = "@app.layout.menu.register";
	/** 登录 */
	String APP_LAYOUT_MENU_LOGIN = "@app.layout.menu.login";
	/** 弹框 */
	String APP_LAYOUT_MENU_POPUP = "@app.layout.menu.popup";
	/** 轮播图 */
	String APP_LAYOUT_MENU_BANNER = "@app.layout.menu.banner";
	/** 广播 */
	String APP_LAYOUT_MENU_BROADCAST = "@app.layout.menu.broadcast";
	/** 下载栏 */
	String APP_LAYOUT_MENU_DOWNLOAD_BAR = "@app.layout.menu.download.bar";
	/** APP下载 */
	String APP_LAYOUT_MENU_APP_DOWNLOAD = "@app.layout.menu.app.download";
	/** 活动轮播图 */
	String APP_LAYOUT_MENU_ACTIVITY_BANNER = "@app.layout.menu.activity.banner";
	/** APP加载页 */
	String APP_LAYOUT_MENU_LOADING = "@app.layout.menu.loading";
	/** 游戏入口 */
	String APP_LAYOUT_MENU_GAME = "@app.layout.menu.game";
	/** 关于我们 */
	String APP_LAYOUT_MENU_ABOUT_US = "@app.layout.menu.about.us";
	/** 用户协议 */
	String APP_LAYOUT_MENU_USER_AGREEMENT = "@app.layout.menu.user.agreement";
	/** 桌面logo */
	String APP_LAYOUT_MENU_DESKTOP_LOGO = "@app.layout.menu.desktop.logo";
	/** 活动倒计时 */
	String APP_LAYOUT_MENU_ACTIVITY_COUNTDOWN = "@app.layout.menu.activity.countdown";
	/** 攒金大转盘 */
	String APP_LAYOUT_MENU_GOLD_TURNTABLE = "@app.layout.menu.gold.turntable";

	// 代理角色
	/** 高级代理（可出款） */
	String AGENT_ROLE_ADVANCED = "agent.role.advanced";
	/** 高级代理（不可出款） */
	String AGENT_ROLE_ADVANCED_NO_OUT = "agent.role.advanced.no-out";
	/** 普通代理（不可出款） */
	String AGENT_ROLE_ORDINARY = "agent.role.ordinary";

	// DNS分组
	/** Web大厅 */
	String DNS_POSITION_WEB_HALL = "dns.position.web.hall";
	/** App大厅 */
	String DNS_POSITION_APP_HALL = "dns.position.app.hall";
	/** 下载站域名 */
	String DNS_POSITION_DOWNLOAD = "dns.position.download";
	/** 支付域名 */
	String DNS_POSITION_PAY = "dns.position.pay";
	/** 代理商后台 */
	String DNS_POSITION_AGENT = "dns.position.agent";

	// 用户状态
	/** 正常 */
	String USER_STATUS_NORMAL = "user.status.normal";
	/** 冻结 */
	String USER_STATUS_FREEZE = "user.status.freeze";
	/** 禁止提现 */
	String USER_STATUS_CASH_LIMIT = "user.status.cash.limit";
	/** 禁止优惠 */
	String USER_STATUS_GIFT_LIMIT = "user.status.gift.limit";
	/** 禁止游戏 */
	String USER_STATUS_GAME_LIMIT = "user.status.game.limit";

	// 标签 状态
	/** 是 */
	String FLAG_YES = "flag.yes";
	/** 否 */
	String FLAG_NO = "flag.no";

	// 会员提现
	/** 正常提现 */
	String ONLINE = "cash.log.online";
	/** 加密货币 */
	String USDT = "cash.log.usdt";
	/** 转账提现 */
	String BANK_OFFLINE = "cash.log.bankOffline";

	// 域名活跃状态
	/** 正常 */
	String DOMAIN_STATUS_NORMAL = "domain.status.normal";
	/** 待验证 */
	String DOMAIN_STATUS_WAIT_VERIFY = "domain.status.wait.verify";

	// 宣传配置状态
	/** 待生效 */
	String SITE_BANNER_STATUS_WAIT_EFFECTIVE = "site.banner.status.wait.effective";
	/** 生效中 */
	String SITE_BANNER_STATUS_EFFECTIVE = "site.banner.status.effective";
	/** 已失效 */
	String SITE_BANNER_STATUS_EXPIRED = "site.banner.status.expired";
	/** 未知 */
	String SITE_BANNER_STATUS_UNKNOWN = "site.banner.status.unknown";

	// 充值标签
	/** 已存款 */
	String RECHARGE_DEPOSITED = "recharge.deposited";
	/** 未存款 */
	String RECHARGE_NOT_DEPOSITED = "recharge.not.deposited";

	// 用户类型
	/** 推广 */
	String USER_TYPE_PROMOTE = "user.type.promote";
	/** 代理 */
	String USER_TYPE_AGENT = "user.type.agent";
	/** 会员 */
	String USER_TYPE_MEMBER = "user.type.member";

	// 出款开关
	/** 已开启 */
	String OUT_SWITCH_ON = "out.switch.on";
	/** 关闭 */
	String OUT_SWITCH_OFF = "out.switch.off";

	// 热门类型
	/** 厂商 */
	String HOT_TYPE_PLATFORM = "hot.type.platform";
	/** 子游戏 */
	String HOT_TYPE_SUB_GAME = "hot.type.sub.game";

	// 商户参数校验
	/** 商户号 */
	String MERCHANT_PARAM_NO = "merchant.param.no";
	/** 商户私钥 */
	String MERCHANT_PARAM_PRIVATE_KEY = "merchant.param.private.key";
	/** 平台公钥 */
	String MERCHANT_PARAM_PLATFORM_PUBLIC_KEY = "merchant.param.platform.public.key";
	/** MD5密钥 */
	String MERCHANT_PARAM_MD5_KEY = "merchant.param.md5.key";
	/** 商户公钥 */
	String MERCHANT_PARAM_PUBLIC_KEY = "merchant.param.public.key";
	/** USDT收款地址 */
	String MERCHANT_PARAM_USDT_ADDR = "merchant.param.usdt.addr";
	/** 二维码图片 */
	String MERCHANT_PARAM_QR_CODE = "merchant.param.qr.code";
	/** 签名秘钥 */
	String MERCHANT_PARAM_SIGN_KEY = "merchant.param.sign.key";
	/** 商户密钥 */
	String MERCHANT_PARAM_SECRET_KEY = "merchant.param.secret.key";
	/** HamcSHA256密匙 */
	String MERCHANT_PARAM_HAMC_SHA256_KEY = "merchant.param.hamc.sha256.key";
	/** RSA公钥 */
	String MERCHANT_PARAM_RSA_PUBLIC_KEY = "merchant.param.rsa.public.key";
	/** RSA私钥 */
	String MERCHANT_PARAM_RSA_PRIVATE_KEY = "merchant.param.rsa.private.key";
	/** 代收应用ID */
	String MERCHANT_PARAM_APP_ID = "merchant.param.app.id";
	/** 代收秘钥 */
	String MERCHANT_PARAM_APP_SECRET = "merchant.param.app.secret";
	/** 代付应用ID */
	String MERCHANT_PARAM_PAY_APP_ID = "merchant.param.pay.app.id";
	/** 代付秘钥 */
	String MERCHANT_PARAM_PAY_APP_SECRET = "merchant.param.pay.app.secret";
	/** 通道ID */
	String MERCHANT_PARAM_CHANNEL_ID = "merchant.param.channel.id";
	/** 交易通道 */
	String MERCHANT_PARAM_CHANNEL_TYPE = "merchant.param.channel.type";
	/** 代收支付地址 */
	String MERCHANT_PARAM_CREATE_ORDER_KEY = "merchant.param.create.order.key";
	/** 代付支付地址 */
	String MERCHANT_PARAM_WITHDRAW_KEY = "merchant.param.withdraw.key";
	/** 代收查询地址 */
	String MERCHANT_PARAM_QUERY_ORDER_KEY = "merchant.param.query.order.key";
	/** 代付查询地址 */
	String MERCHANT_PARAM_QUERY_WITHDRAW_KEY = "merchant.param.query.withdraw.key";
	/** 余额查询地址 */
	String MERCHANT_PARAM_QUERY_BALANCE_KEY = "merchant.param.query.balance.key";

	// 用户操作类型
	/** 登录 */
	String USER_OPERATION_TYPE_LOGIN = "user.operation.type.login";

	// 消息状态
	/** 已读 */
	String MSG_STATUS_READ = "msg.status.read";
	/** 未读 */
	String MSG_STATUS_UNREAD = "msg.status.unread";

	// 提示音类型
	/** 消息 */
	String PROMPT_SOUND_MSG = "prompt.sound.msg";
	/** 获利监控 */
	String PROMPT_SOUND_PROFIT_SPY = "prompt.sound.profit.spy";
	/** 提现 */
	String PROMPT_SOUND_CASH = "prompt.sound.cash";

	// 领取方式
	/** 手工入账 */
	String RECEIVE_TYPE_HAND = "receive.type.hand";
	/** 自动入账 */
	String RECEIVE_TYPE_AUTO = "receive.type.auto";
	// 白名单类型
	/** 商户添加 */
	String WHITELIST_TYPE_MERCHANT = "whitelist.type.merchant";
	/** 平台添加 */
	String WHITELIST_TYPE_ADMIN = "whitelist.type.admin";
	/** 导入指定的商户不存在 */
	String ORIGINAL_MERCHANT_NOT_EXIST = "original.merchant.not.exist";
	/** 新商户经营地与导入指定的商户经营地不一致 */
	String MERCHANT_COUNTRY_INCONSISTENT = "merchant.country.inconsistent";
	/** 总台VIP余额限制未初始化 */
	String VIP_LIMIT_NONE_INIT = "merchant.country.vip.limit";
	// 客服模块
	/** 引导配置 */
	String CUSTOMER_SERVICE_GUIDE = "customer.service.guide";
	/** 支付配置 */
	String CUSTOMER_SERVICE_PAY = "customer.service.pay";
	/** 短信配置 */
	String CUSTOMER_SERVICE_SMS = "customer.service.sms";
	/** 未开放页面配置 */
	String CUSTOMER_SERVICE_UNOPENED_PAGE = "customer.service.unopened.page";
	/** 引导配置提示内容 */
	String GUIDE_CONFIG_PROMPT_CONTENT = "guide.config.prompt.content";
	/** 支付配置提示内容 */
	String PAY_CONFIG_PROMPT_CONTENT = "pay.config.prompt.content";
	/** 短信配置提示内容 */
	String SMS_CONFIG_PROMPT_CONTENT = "sms.config.prompt.content";
	/** 未开放页面配置提示内容 */
	String UNOPENED_PAGE_CONFIG_PROMPT_CONTENT = "unopened.page.config.prompt.content";
	/** 全部站点 */
	String SITE_ALL = "site.all";
	/** 默认短信提现内容 */
	String DEFAULT_SMS_REMIND_CONTENT = "default.sms.remind.content";
	/** 默认层级 */
	String LAYER_DEFAULT = "layer.default";

	/** 区域前缀 */
	String REGION_PREFIX = "region.";

	/** 活动全部层级 */
	String PROMOTION_LEVEL_ALL = "promotion.level.all";
	/** 活动全部等级 */
	String PROMOTION_LAYER_ALL = "promotion.layer.all";
	/** 最多只能选择{0}个代理 */
	String CUSTOMER_LINK_AGENT_LIMIT = "customer.link.agent.limit";
	/** 最多只能选择{0}个渠道 */
	String CUSTOMER_LINK_CHANNEL_LIMIT = "customer.link.channel.limit";
	/** 推广域名类型-代理域名 */
	String MARKETING_DOMAIN_TYPE_AGENT = "marketing.domain.type.agent";
	/** 推广域名类型-自定义 */
	String MARKETING_DOMAIN_TYPE_CUSTOM = "marketing.domain.type.custom";
	/** 仅能选择推广域名 */
	String MARKETING_DOMAIN_SELECT_RANGE = "marketing.domain.select.range";
	/** 仅能选择自己的推广域名 */
	String MARKETING_DOMAIN_SELECT_SELF = "marketing.domain.select.self";
	/** 该域名已经存在，请勿重复添加 */
	String MARKETING_DOMAIN_REPEAT = "marketing.domain.repeat";
	/** 代理用户 {0} 不存在，请重新输入 */
	String MARKETING_DOMAIN_AGENT_NOTFOUND = "marketing.domain.agent.notfound";
	/** 推广域名 {0} 无效，请重新选择 */
	String MARKETING_DOMAIN_INVALID = "marketing.domain.invalid";

	/** 实时 */
	String DATE_UNIT_NOW = "date.unit.now";
	/** 天 */
	String DATE_UNIT_DAY = "date.unit.day";
	/** 周 */
	String DATE_UNIT_WEEK = "date.unit.week";
	/** 月 */
	String DATE_UNIT_MONTH = "date.unit.month";
	/** 小时 */
	String DATE_UNIT_HOUR = "date.unit.hour";

	/** 奖励配置 */
	String FORM_REWARD_CONFIG = "form.reward.config";
	/** 过期配置 */
	String FORM_EXPIRE_CONFIG = "form.expire.config";
	/** 过期小时数 */
	String FORM_EXPIRE_HOUR = "form.expire.hour";
	/** 领取限制 */
	String FORM_RECEIVE_SCOPE = "form.receive.scope";
	/** 领取票券后 */
	String TICKET_MIN_RULE_RECEIVE_AFTER = "ticket.min.rule.receive.after";
	/** 历史累计 */
	String TICKET_MIN_RULE_TOTAL = "ticket.min.rule.total";
	/** 长期 */
	String TICKET_EXPIRE_TYPE_LONG = "ticket.expire.type.long";
	/** 长期 */
	String TICKET_EXPIRE_TYPE_RECEIVE_AFTER = "ticket.expire.type.receive.after";
	/** 仅限APP领取 */
	String TICKET_RECEIVE_SCOPE_APP = "ticket.receive.scope.app";
	/** 无限制领取设备 */
	String TICKET_RECEIVE_SCOPE_ALL = "ticket.receive.scope.all";
	/** 领取后有效时间必须大于0 */
	String TICKET_RECEIVE_AFTER_TIME_INVALID = "ticket.receive.after.time.invalid";
	/** 总存款金额配置不能为空 */
	String TICKET_RECHARGE_AMOUNT_CONFIG_INVALID = "ticket.recharge.amount.config.invalid";
	/** 总存款次数配置不能为空 */
	String TICKET_RECHARGE_COUNT_CONFIG_INVALID = "ticket.recharge.count.config.invalid";
	/** 总存款金额类型不能为空 */
	String TICKET_RECHARGE_AMOUNT_TYPE_INVALID = "ticket.recharge.amount.type.invalid";
	/** 总存款金额不能为空 */
	String TICKET_RECHARGE_AMOUNT_INVALID = "ticket.recharge.amount.invalid";
	/** 总存款次数类型不能为空 */
	String TICKET_RECHARGE_COUNT_TYPE_INVALID = "ticket.recharge.count.type.invalid";
	/** 总存款次数不能为空 */
	String TICKET_RECHARGE_COUNT_INVALID = "ticket.recharge.count.invalid";
	/** 有效投注配置不能为空 */
	String TICKET_VALID_COIN_CONFIG_INVALID = "ticket.valid.coin.config.invalid";
	/** 有效投注类型不能为空 */
	String TICKET_VALID_COIN_TYPE_INVALID = "ticket.valid.coin.type.invalid";
	/** 有效投注数不能为空 */
	String TICKET_VALID_COIN_INVALID = "ticket.valid.coin.invalid";
	/** 总输金额不能为空且必须大于0 */
	String TICKET_LOSE_COIN_INVALID = "ticket.lose.coin.invalid";
	/** 直属注册人数不能为空且必须大于0 */
	String TICKET_DIRECT_COUNT_INVALID = "ticket.direct.count.invalid";
	/** 中奖概率最多只能配置{0}个 */
	String TICKET_REWARD_RATE_MAX_INVALID = "ticket.reward.rate.max.invalid";
	/** 中奖概率最少需要配置{0}个 */
	String TICKET_REWARD_RATE_MIN_INVALID = "ticket.reward.rate.min.invalid";
	/** 奖励不能为空 */
	String TICKET_REWARD_AMOUNT_INVALID = "ticket.reward.amount.invalid";
	/** 最小金额不能大于最大金额 */
	String TICKET_REWARD_AMOUNT_MIN_GT_MAX = "ticket.reward.amount.min.gt.max";
	/** 概率不能为空 */
	String TICKET_REWARD_RATE_INVALID = "ticket.reward.rate.invalid";
	/** 图标不能为空 */
	String TICKET_REWARD_ICON_INVALID = "ticket.reward.icon.invalid";
	/** 总概率相加必须等于100 */
	String TICKET_TOTAL_REWARD_RATE_INVALID = "ticket.total.reward.rate.invalid";
	/** 总奖励相加必须大于0 */
	String TICKET_TOTAL_REWARD_INVALID = "ticket.total.reward.invalid";
	/** 该游戏厂商不支持替换 */
	String VENDOR_NOT_SUPPORT_REPLACEMENT = "vendor.not.support.replacement";
	/** 原厂商游戏编辑失败！ */
	String ORG_GAME_EDIT_FAIL = "org.game.edit.fail";
	/** 归集收款地址 */
	String COLLECT_TO_ADDR = "field.collectToAddr";
	/** 归集收款地址格式错误 */
	String COLLECT_TO_ADDR_FORMATTING_ERROR = "field.collectToAddr.invalid";
	/** 介绍人商户不存在 */
	String INVITE_MERCHANT_NOT_EXISTS = "invite.merchant.not.exists";
	/** 请选择介绍人所属运营地区 */
	String INVITE_REGION_REQUIRED = "invite.region.required";
	/** 开版费返现金额应该大于0 */
	String OPEN_REBATE_REQUIRED = "open.rebate.required";
	/** 月贡献返佣比例应该大于等于0小于等于100 */
	String REBATE_RATE_REQUIRED = "rebate.rate.required";
	/** 时间间隔不能超过一个月 */
	String TIME_INTERVAL_EXCEED_MONTH = "time.interval.exceed.month";
	/** 运营地区：巴西 */
	String BR = "region.br";
	/** 运营地区：巴西 */
	String ASIA = "region.asia";
	/** 有效 */
	String CHANNEL_TOKEN_VALID = "channel.token.valid";
	/** 已失效 */
	String CHANNEL_TOKEN_EXPIRED = "channel.token.expired";
	/** 首月 */
	String SERVER_FEE_CYCLE_FIRST_MONTH = "server.fee.cycle.first.month";
	/** 1-10 */
	String SERVER_FEE_CYCLE_EARLY_MONTH = "server.fee.cycle.early.month";
	/** 11-20 */
	String SERVER_FEE_CYCLE_MID_MONTH = "server.fee.cycle.mid.month";
	/** 21-月底 */
	String SERVER_FEE_CYCLE_LATE_MARCH = "server.fee.cycle.late.march";
	/** 欠费 */
	String SITE_STATUS_OVERDRAFT = "site.status.overdraft";
	/** 录入的日期 */
	String FIELD_ENTER_PERIOD = "from.enter.period";
	/** 录入的费用 */
	String FIELD_ENTER_FEE = "from.enter.fee";
	/** 没有找到有效记录/或记录不存在 */
	String RECORD_NOT_FOUND = "record.not.found";
	/** 广告费用 */
	String OTHER_FEE_TYPE_AD = "other.fee.type.ad";
	/** 人力费用 */
	String OTHER_FEE_TYPE_MANPOWER = "other.fee.type.manpower";
	/** 其他费用 */
	String OTHER_FEE_TYPE_OTHER = "other.fee.type.other";
	/** 费用类型 */
	String FIELD_FEE_TYPE = "field.fee.type";
	/** 不支持直接录入广告费用 */
	String OTHER_FEE_TYPE_AD_NOT_SUPPORT = "other.fee.type.ad.not.support";

	/** 修正金额和票券不能都为空 */
	String ADJUST_AMOUNT_OR_TICKET_INVALID = "adjust.amount.or.ticket.invalid";

	/** 分卡出款功能未开放 */
	String FORM_SPLIT_CASH_NOT_OPEN = "form.split.cash.not.open";
	/** USDT不支持分卡出款 */
	String FORM_SPLIT_CASH_NOT_SUPPORT = "form.split.cash.not.support";
	/** 订单不支持分卡出款 */
	String FORM_SPLIT_CASH_ORDER_INVALID = "form.split.cash.order.invalid";
	/** 分卡出款总金额与总出款金额不相等 */
	String FORM_SPLIT_CASH_AMOUNT_INVALID = "form.split.cash.amount.invalid";
	/** 分卡出款数量限制[2~5] */
	String FORM_SPLIT_CASH_NUM_INVALID = "form.split.cash.num.invalid";
	/** 不支持操作分卡出款后的提现订单 */
	String FORM_SPLIT_CASH_OPERATE_NOT_SUPPORT = "form.split.cash.operate.not.support";
	/** 分卡订单创建失败 */
	String FORM_SPLIT_CASH_CREATE_FAIL = "form.split.cash.create.fail";
	/** 请选择支付类型 */
	String FORM_CASH_PAY_CHANNEL_INVALID = "form.cash.pay.channel.invalid";
	/** 请输入提现额度 */
	String FORM_CASH_AMOUNT_INVALID = "form.cash.amount.invalid";
	// 返佣方式
	/** 直属 */
	String REBATE_MODEL_DIRECTLY = "rebate.model.directly";
	/** 所有 */
	String REBATE_MODEL_ALL = "rebate.model.all";

	/** 底部下载浮窗配置：描述文本不可超过150字符 */
	String BOTTOM_POPUP_ENABLED = "bottom.popup.enabled";
	/** 底部下载浮窗配置：APP按钮角标文本不可超过30字符 */
	String BOTTOM_POPUP_FLAG_ENABLED = "bottom.popup.flag.enabled";
	/** 快速入口最多只能6个 */
	String LAYOUT_ME_QUICK_ACCESS_ABOVE = "layout.me.quick.access.above";
	/** 请勾选需要展示的活动 */
	String PROMOTION_HELP_ME_TYPES_REQUIRED = "promotion.help.me.types.required";
	/** 未开放的活动类型 */
	String PROMOTION_NOT_OPEN_TYPE = "promotion.not.open.type";
	/** 注册方式 */
	String CONFIG_TYPE_REG_MODE = "config.type.reg.mode";
	/** 客服配置 */
	String CONFIG_TYPE_CUSTOMER_CONFIG = "config.type.customer.config";
	/** 开放厂家 */
	String CONFIG_TYPE_OPEN_VENDOR = "config.type.open.vendor";
	/** banner */
	String CONFIG_TYPE_BANNER = "config.type.banner";
	/** 活动 */
	String CONFIG_TYPE_PROMOTION = "config.type.promotion";
	/** 代理 */
	String CONFIG_TYPE_AGENT = "config.type.agent";
	/** 会员标签 */
	String CONFIG_TYPE_MEMBER_TAG = "config.type.member.tag";
	/** 会员调控 */
	String CONFIG_TYPE_MEMBER_CTRL = "config.type.member.ctrl";
	/** 任务 */
	String CONFIG_TYPE_TASK = "config.type.task";
	/** 游戏返水 */
	String CONFIG_TYPE_GAME_REBATE = "config.type.game.rebate";
	/** 票券 */
	String CONFIG_TYPE_TICKET = "config.type.ticket";
	/** 获利监控 */
	String CONFIG_TYPE_PROFIT_MONITOR = "config.type.profit.monitor";
	/** 刷子监控 */
	String CONFIG_TYPE_BOT_SPY = "config.type.bot.spy";
	/** 支付通道 */
	String CONFIG_TYPE_PAY_CHANNEL = "config.type.pay.channel";
	/** 前端模版 */
	String CONFIG_TYPE_FRONT_TEMPLATE = "config.type.front.template";
	/** logo */
	String CONFIG_TYPE_LOG = "config.type.log";
	/** 站点已经存在配置，不可替换 */
	String SITE_CONFIG_ALREADY_EXISTS = "site.config.already.exists";
	/** 会员层级 */
	String CONFIG_TYPE_MEMBER_LAYER = "config.type.member.layer";
	/** 员工ID */
	String FIELD_EMP_ID = "field.empId";
	/** 薪资 */
	String FIELD_SALARY = "field.salary";
	/** 薪资-发放 */
	String FIELD_SALARY_FRONT = "field.salary.front";
	/** 薪资-拖欠 */
	String FIELD_SALARY_BEHIND = "field.salary.behind";
	/** 薪资-积极进取 */
	String FIELD_SALARY_BE_PROACTIVE = "field.salary.be.proactive";
	/** 薪资-警告 */
	String FIELD_SALARY_WARN = "field.salary.warn";
	/** 归集中 */
	String COLLECT_STATUS_PENDING = "collect.status.pending";
	/** 归集失败 */
	String COLLECT_STATUS_FILA = "collect.status.fila";
	/** 数量范围1~100 */
	String BLOGGER_ACCOUNT_NUM_INVALID = "blogger.account.num.invalid";
	/** 请选择适用站点 */
	String BLOGGER_ACCOUNT_MERCHANT_INVALID = "blogger.account.merchant.invalid";
	/** 佣金 */
	String EXPORT_BLOGGER_COMMISSION = "export.blogger.commission";
	/** 范围配置不能为空 */
	String BLOGGER_REWARD_RANG_INVALID = "blogger.reward.rang.invalid";
	/** 排名范围1~1000 */
	String BLOGGER_REWARD_RANK_RANGE_INVALID = "blogger.reward.rank.range.invalid";
	/** 人数范围1~90000 */
	String BLOGGER_REWARD_NUM_RANGE_INVALID = "blogger.reward.num.range.invalid";
	/** 佣金范围1~9000000 */
	String BLOGGER_REWARD_COMMISSION_RANGE_INVALID = "blogger.reward.commission.range.invalid";
	/** 不可选择当天或之后的日期 */
	String BLOGGER_REWARD_START_TIME_INVALID = "blogger.reward.start.time.invalid";
	/** 该时段已存在配置 */
	String BLOGGER_REWARD_TIME_INVALID = "blogger.reward.time.invalid";
	/** 不能修改已结束的配置 */
	String BLOGGER_REWARD_END_EDIT_INVALID = "blogger.reward.end.edit.invalid";
	/** 总计 */
	String STAT_TOTAL = "export.gameStat.total";
	/** 会员账号 */
	String FIELD_MEMBER_ACCOUNT = "export.userList.username";
	/** 注册来源 */
	String FIELD_REGISTER_SOURCE = "export.userList.source_";
	/** 首充时间 */
	String FIELD_FIRST_RECHARGE_TIME = "export.userList.firstRechargeTime_";
	/** 上级代理ID */
	String FIELD_PARENT_AGENT_ID = "export.userList.agent.id";
	/** 上级代理账号 */
	String FIELD_PARENT_AGENT_ACCOUNT = "export.userList.agentUsername";
	/** 注册时间 */
	String FIELD_REGISTER_TIME = "export.userList.addTime";
	/** VIP等级 */
	String FIELD_VIP_LEVEL = "export.userList.level_";
	/** 会员层级 */
	String FIELD_MEMBER_LAYER = "export.userList.layerName";
	/** 所有余额 */
	String FIELD_ALL_BALANCE = "export.userList.activeAmount";
	/** 总提现金额 */
	String FIELD_TOTAL_WITHDRAW = "export.userList.cashSum";
	/** 总充提差额 */
	String FIELD_TOTAL_DIFFERENCE = "export.userList.rechargeCashMinus";
	/** 充值次数 */
	String FIELD_RECHARGE_COUNT = "export.userList.rechargeNum";
	/** 提现次数 */
	String FIELD_WITHDRAW_COUNT = "export.userList.cashNum";
	/** 有效投注 */
	String FIELD_VALID_COIN_IN = "export.userList.validCoin";
	/** 验证方式 */
	String FIELD_VALID_BET_METHOD = "export.userList.loginWay_";
	/** 最后登录时间 */
	String FIELD_LAST_LOGIN_TIME = "export.userList.lastLoginTime";
	/** 最后登录IP */
	String FIELD_LAST_LOGIN_IP = "export.userList.lastLoginIp";
	/** 总充值金额 */
	String FIELD_RECHARGE_SUM = "export.userList.rechargeSum";
	/** 姓名 */
	String FIELD_MEMBER_NAME = "export.userList.realName";
	/** 会员输赢 */
	String FIELD_MEMBER_WIN_LOSE_COIN = "export.userList.winLoseCoin";
	/** 佣金 */
	String FIELD_UNLIMITED_COMMISSION = "export.userList.commission";
	/** 优惠金额 */
	String FIELD_MEMBER_DISCOUNT_AMOUNT = "export.userList.discountAmount";
	/** VIP */
	String CONFIG_TYPE_VIP = "config.type.vip";

	/** VIP等级+会员返利 */
	String CONFIG_TYPE_VIP_REBATE = "config.type.vip.rebate";

	/** 订单锁定状态不能操作 */
	String TRANSFER_ORDER_LOCKED = "transfer.order.locked";

	/** 当前账号与锁定订单账号不符，请联系管理员解锁订单后操作 */
	String CASH_LOCK_FAIL = "cash.lock.fail";
	/** 仅支持操作线下兑换订单 */
	String CASH_OPERATE_OFFLINE = "cash.operate.offline";
	/** 订单[111]不是锁定状态 */
	String CASH_LOCK_STATUS_CHECK = "cash.lock.status.check";
	/** 当前状态不支持锁定操作 */
	String CASH_LOCK_STATUS_FAIL = "cash.lock.status.fail";
	/** 该订单已被锁定，请勿重复操作 */
	String CASH_REPEAT_LOCK_FAIL = "cash.repeat.lock.fail";
	/** 该订单未锁定，无需解锁 */
	String CASH_NON_LOCK_FAIL = "cash.non.lock.fail";

	/** 任务验证错误信息 - 周期模式不能为空 */
	String CYCLE_MODE_REQUIRED = "user.validation.cycle_mode_required";
	/** 任务验证错误信息 - 奖励金额错误 */
	String REWARD_REQUIRED = "user.validation.reward_required";
	/** 任务验证错误信息 - 平均抽奖次数错误 */
	String TASK_AVG_INVALID = "user.validation.task_avg_invalid";
	/** 任务验证错误信息 - 至少需要提供一个任务 */
	String TASK_REQUIRED = "user.validation.task_required";
	/** 任务验证错误信息 - 任务类型不能为空 */
	String REWARD_TASK_TYPE_REQUIRED = "user.validation.task_type_required";
	/** 任务验证错误信息 - 缺少代理任务 */
	String REWARD_TASK_AGENT_TYPE_REQUIRED = "user.validation.task_agent_type_required";
	/** 任务验证错误信息 - 任务数量必须大于零 */
	String TASK_NUM_REQUIRED = "user.validation.task_num_required";
	/** 任务验证错误信息 - 任务权重必须大于零 */
	String TASK_WEIGHT_REQUIRED = "user.validation.task_weight_required";
	/** 任务验证错误信息 - 如果指定，任务值必须大于零 */
	String TASK_VALUE_REQUIRED = "user.validation.task_value_required";
	/** 任务验证错误信息 - 完成任务的最小数量必须大于{0} */
	String TASK_MIN_REQUIRED = "user.validation.task_min_required";
	/** 任务验证错误信息 - 至少包含一个非一次性任务 */
	String TASK_DISPOSABLE_NUM_FAIL = "user.validation.task_disposable_num_fail";
	/** 任务验证错误信息 - 完成任务的最大数量必须大于零 */
	String TASK_MAX_REQUIRED = "user.validation.task_max_required";
	/** 任务验证错误信息 - 最小任务数不能大于最大任务数 */
	String TASK_MIN_MAX_REQUIRED = "user.validation.task_min_max_required";
	/** 任务验证错误信息 - 至少需要提供一个奖项 */
	String ITEMS_REQUIRED = "user.validation.items_required";
	/** 任务验证错误信息 - 奖项类型无效 */
	String ITEM_TYPE_REQUIRED = "user.validation.item_type_required";
	/** 任务验证错误信息 - 初次中奖范围最大值错误，最大 {0} */
	String ITEM_TYPE_TASK_MAX_LIMIT = "user.validation.task_max_limit";
	/** 任务验证错误信息 - 初始奖金数值错误，最小 {0} */
	String ITEM_TYPE_TASK_MIN_LIMIT = "user.validation.task_min_limit";
	/** 任务验证错误信息 - 最小初次中奖范围必须大于零 */
	String FIRST_WIN_MIN_REQUIRED = "user.validation.first_win_min_required";
	/** 任务验证错误信息 - 最大初次中奖范围必须大于零 */
	String FIRST_WIN_MAX_REQUIRED = "user.validation.first_win_max_required";
	/** 任务验证错误信息 - 最小初次中奖范围不能大于最大初次中奖范围 */
	String FIRST_WIN_MIN_MAX_REQUIRED = "user.validation.first_win_min_max_required";
	/** 公积金存入比例不能为空 */
	String DEPOSIT_POOL_RATIO_REQUIRED = "deposit.pool.ratio.required";
	/** 累计赠送封顶 */
	String DEPOSIT_POOL_REWARD_AMOUNT_REQUIRED = "deposit.pool.reward.amount.required";
	/** 累计赠送次数 */
	String DEPOSIT_POOL_REWARD_NUM_REQUIRED = "deposit.pool.reward.num.required";
	/** 有赠送公积金的充值要求投注倍数 */
	String DEPOSIT_POOL_BET_MULTIPLE_REQUIRED = "deposit.pool.bet.multiple.required";
	/** 发奖方式 */
	String DEPOSIT_POOL_GRANT_MODE_REQUIRED = "deposit.pool.grant.mode.required";
	/** 充值弹窗提醒 */
	String DEPOSIT_POOL_POP_REMIND_REQUIRED = "deposit.pool.pop.remind.required";
	/** 公积金开关 */
	String DEPOSIT_POOL_ON_OFF_REQUIRED = "deposit.pool.on.off.required";
	/** 不封顶 */
	String DEPOSIT_POOL_NO_CAP = "deposit.pool.no.cap";
	/** 已重置 */
	String DEPOSIT_POOL_STATUS_RESET = "deposit.pool.status.reset";
	/** 未完成 */
	String DEPOSIT_POOL_STATUS_INIT = "deposit.pool.status.init";
	/** 已完成 */
	String DEPOSIT_POOL_STATUS_COMPLETED = "deposit.pool.status.completed";
	/** 已解除 */
	String DEPOSIT_POOL_STATUS_RELEASED = "deposit.pool.status.released";
	/** 已领奖 */
	String DEPOSIT_POOL_STATUS_AWARDED = "deposit.pool.status.awarded";
	/** 交易ID */
	String FIELD_TX_ID = "export.merchantRechargeList.txId";

	/** 站点ID */
	String FIELD_RECHARGE_MERCHANT_ID = "export.rechargeList.merchantId";
	/** 会员ID */
	String FIELD_RECHARGE_USER_ID = "export.rechargeList.userId";
	/** 上级代理ID */
	String FIELD_RECHARGE_AGENT_USER_ID = "export.rechargeList.agentUserId";
	/** 上级代理账号 */
	String FIELD_RECHARGE_AGENT_USERNAME = "export.rechargeList.agentUsername";
	/** 站点名称 */
	String FIELD_RECHARGE_MERCHANT_NAME = "export.rechargeList.merchantName";
	/** 订单号 */
	String FIELD_RECHARGE_ORDER_NO = "export.rechargeList.orderNo";
	/** 会员账号 */
	String FIELD_RECHARGE_USERNAME = "export.rechargeList.username";
	/** 创建时间 */
	String FIELD_RECHARGE_ADD_TIME = "export.rechargeList.addTime";
	/** 成功时间 */
	String FIELD_RECHARGE_UPDATE_TIME = "export.rechargeList.updateTime";
	/** 支付方式 */
	String FIELD_RECHARGE_PAY_CODE = "export.rechargeList.payCode";
	/** 支付通道 */
	String FIELD_RECHARGE_CHANNEL = "export.rechargeList.channel_";
	/** 通道目前成功率(%) */
	String FIELD_RECHARGE_CHANNEL_SUCCESS_RATE = "export.rechargeList.channelSuccessRate";
	/** VIP等级 */
	String FIELD_RECHARGE_LEVEL = "export.rechargeList.level_";
	/** 会员层级 */
	String FIELD_RECHARGE_LAYER_NAME = "export.rechargeList.layerName";
	/** 汇率 */
	String FIELD_RECHARGE_RATE = "export.rechargeList.rate";
	/** 原始数量(汇率) */
	String FIELD_RECHARGE_NUM_RATE = "export.rechargeList.numRate";
	/** 充值币种 */
	String FIELD_RECHARGE_CURRENCY = "export.rechargeList.currency";
	/** 订单金额 */
	String FIELD_RECHARGE_ORDER_AMOUNT = "export.rechargeList.amount";
	/** 赠送金额 */
	String FIELD_RECHARGE_GIFT_AMOUNT = "export.rechargeList.giftAmount";
	/** 总金额 */
	String FIELD_RECHARGE_TOTAL_AMOUNT = "export.rechargeList.totalAmount";
	/** 银行名称 */
	String FIELD_RECHARGE_BANK_NAME = "export.rechargeList.bankName";
	/** 付款人 */
	String FIELD_RECHARGE_FROM_NAME = "export.rechargeList.fromName";
	/** 付款账户 */
	String FIELD_RECHARGE_FROM_ACCOUNT = "export.rechargeList.fromAccount";
	/** 付款信息 */
	String FIELD_RECHARGE_FROM_INFO = "export.rechargeList.fromInfo";
	/** 收款人 */
	String FIELD_RECHARGE_TO_NAME = "export.rechargeList.toName";
	/** 收款账户 */
	String FIELD_RECHARGE_TO_ACCOUNT = "export.rechargeList.toAccount";
	/** 转账备注 */
	String FIELD_RECHARGE_FRONT_REMARK = "export.rechargeList.frontRemark";
	/** 前端拒绝备注 */
	String FIELD_RECHARGE_BACK_REMARK = "export.rechargeList.backRemark";
	/** 订单状态 */
	String FIELD_RECHARGE_STATUS = "export.rechargeList.status_";
	/** 操作人 */
	String FIELD_RECHARGE_EMP_NAME = "export.rechargeList.empName";
	/** 订单金额（本地） */
	String FIELD_RECHARGE_LOCAL_AMOUNT = "export.rechargeList.localAmount";
	/** 转账凭证 */
	String FIELD_RECHARGE_TRANSFER_VOUCHER = "export.rechargeList.transferVoucher";
	/** 第三方订单号 */
	String FIELD_TRADE_NO = "export.rechargeList.tradeNo";
	/** 充值列表 */
	String FILENAME_RECHARGE_LIST = "filename.rechargeList";
	/** 转账订单列表 */
	String FILENAME_TRANSFER_REVIEW_LIST = "filename.transferReviewList";

	/** 商户ID */
	String FIELD_CASH_LOG_MERCHANT_ID = "export.cashLogList.merchantId";
	/** 商户名称 */
	String FIELD_CASH_LOG_MERCHANT_NAME = "export.cashLogList.merchantName";
	/** 订单号 */
	String FIELD_CASH_LOG_ORDER_NO = "export.cashLogList.orderNo";
	/** 会员ID */
	String FIELD_CASH_LOG_USER_ID = "export.cashLogList.userId";
	/** 会员账号 */
	String FIELD_CASH_LOG_USERNAME = "export.cashLogList.userName";
	/** 会员等级 */
	String FIELD_CASH_LOG_LEVEL = "export.cashLogList.level_";
	/** 申请时间 */
	String FIELD_CASH_LOG_ADD_TIME = "export.cashLogList.addTime";
	/** 操作时间 */
	String FIELD_CASH_LOG_UPDATE_TIME = "export.cashLogList.updateTime";
	/** 当前余额 */
	String FIELD_CASH_LOG_ACTIVE_AMOUNT = "export.cashLogList.activeAmount";
	/** 到账币种 */
	String FIELD_CASH_LOG_CURRENCY = "export.cashLogList.currency";
	/** 汇率 */
	String FIELD_CASH_LOG_EXCHANGE_RATE = "export.cashLogList.exchangeRate";
	/** 预计到账 */
	String FIELD_CASH_LOG_AMOUNT = "export.cashLogList.amount";
	/** 手续费 */
	String FIELD_CASH_LOG_REAL_FEE = "export.cashLogList.realFee";
	/** 实际到账 */
	String FIELD_CASH_LOG_REAL_AMOUNT = "export.cashLogList.realAmount";
	/** 充值次数 */
	String FIELD_CASH_LOG_RECHARGE_NUM = "export.cashLogList.rechargeNum";
	/** 提现次数 */
	String FIELD_CASH_LOG_CASH_NUM = "export.cashLogList.cashNum";
	/** 累计充值金额 */
	String FIELD_CASH_LOG_RECHARGE_SUM = "export.cashLogList.rechargeSum";
	/** 充提差额 */
	String FIELD_CASH_LOG_DIFF_AMOUNT = "export.cashLogList.diffAmount";
	/** 重复IP人数 */
	String FIELD_CASH_LOG_IP_NUMS = "export.cashLogList.ipNums";
	/** 收款人 */
	String FIELD_CASH_LOG_REAL_NAME = "export.cashLogList.realName";
	/** PIX类型 */
	String FIELD_CASH_LOG_SUBTYPE = "export.cashLogList.subtype";
	/** PIX账号 */
	String FIELD_CASH_LOG_ACCOUNT = "export.cashLogList.account";
	/** CPF号 */
	String FIELD_CASH_LOG_CPF = "export.cashLogList.cpf";
	/** 付款账号 */
	String FIELD_CASH_LOG_PAY_ACCOUNT = "export.cashLogList.payAccount";
	/** 付款账号类型 */
	String FIELD_CASH_LOG_PAY_ACCOUNT_TYPE = "export.cashLogList.payAccountType";
	/** 付款账号哈希值 */
	String FIELD_CASH_LOG_PAY_ACCOUNT_HASH = "export.cashLogList.payAccountHash";
	/** 订单状态 */
	String FIELD_CASH_LOG_STATUS = "export.cashLogList.status_";
	/** 备注 */
	String FIELD_CASH_LOG_REMARK = "export.cashLogList.remark";
	/** 复审备注 */
	String FIELD_REVIEW_REMARK = "export.cashLogList.reviewRemark";
	/** 支付通道 */
	String FIELD_CASH_LOG_CHANNEL = "export.cashLogList.channel_";
	/** 流水号 */
	String FIELD_CASH_LOG_TRADE_NO = "export.cashLogList.tradeNo";
	/** 提现本地币 */
	String FIELD_CASH_LOG_LOCAL_AMOUNT = "export.cashLogList.localAmount";
	/** 提现方式 */
	String FIELD_CASH_LOG_BANK_CODE = "export.cashLogList.bankCode";
	/** 操作人 */
	String FIELD_CASH_LOG_EMP_NAME = "export.cashLogList.empName";
	/** 后端备注 */
	String FIELD_CASH_LOG_BACK_REMARK = "export.cashLogList.backRemark";
	/** 前端备注 */
	String FIELD_CASH_LOG_FRONT_REMARK = "export.cashLogList.frontRemark";
	/** 收款人银行账号 */
	String FIELD_CASH_LOG_BANK_ACCOUNT = "export.cashLogList.bankAccount";
	/** 订单状态（线下） */
	String FIELD_CASH_LOG_OFFLINE_STATUS = "export.cashLogList.offlineStatus_";
	/** 提现金额 */
	String FIELD_CASH_LOG_CASH_AMOUNT = "export.cashLogList.cashAmount";
	/** 掉绑概率不能超{0}% */
	String BINDING_PROBABILITY_INVALID = "binding.probability.invalid";
	/** 存款分润比例配置有误 */
	String REWARD_SHARE_INVALID = "reward.share.invalid";
	/** 掉绑概率必须大于0 */
	String BINDING_PROBABILITY_LIMIT = "binding.probability.limit";
	/** 固定掉绑值必须大于0 */
	String FIX_BINDING_LIMIT = "fix.binding.limit";
	/** 邀请几人后开始掉绑 */
	String MISSING_BINDING_INIT_INVALID = "missing.binding.init.invalid";
	/** 官方频道不能超过{0}个 */
	String OFFICIAL_CHANNEL_LIMIT = "official.channel.limit";
	/** 有效头像上限80个 */
	String USER_AVATAR_LIMIT = "user.avatar.limit";
	/** 用户头像-启用 */
	String USER_AVATAR_ENABLE = "user.avatar.enable";
	/** 用户头像-停用 */
	String USER_AVATAR_STOP = "user.avatar.stop";
	/** 用户头像-系统 */
	String USER_AVATAR_SYSTEM = "user.avatar.system";
	/** 用户头像-自定义 */
	String USER_AVATAR_CUSTOM = "user.avatar.custom";
	/** 活动标签最多{0}个 */
	String PROMOTION_TAG_LIMIT = "promotion.tag.limit";
	/** 标签重复 */
	String PROMOTION_TAG_REPEAT = "promotion.tag.repeat";
	/** VIP系统默认规则 */
	String VIP_DEFAULT_RULE_DESC = "vip.default.rule.desc";
	String COMMON_STATUS_EXECUTED = "common.status.executed";   // 已执行
	String COMMON_STATUS_CANCELED = "common.status.canceled";   // 已取消
	String USER_BIZ_FLAG_GAME_LIMIT = "user.biz.flag.game_limit";     // 禁止进入游戏
	String USER_BIZ_FLAG_CASH_LIMIT = "user.biz.flag.cash_limit";     // 禁止提现
	String USER_BIZ_FLAG_GIFT_LIMIT = "user.biz.flag.gift_limit";     // 禁止优惠

	/** 热门排序必须在 1~3 之间 */
	String HOT_INDEX_REQUIRED = "hot.index.required";

	/** 热门图片不能为空 */
	String HOT_IMG_REQUIRED = "hot.img.required";

	/** 弹窗图片不能为空 */
	String POP_IMG_REQUIRED = "pop.img.required";

	/** 模态弹窗图片不能为空 */
	String MODAL_IMG_REQUIRED = "modal.img.required";
	/** 热门位已被占用 */
	String PROMOTION_HOT_OCCUPANCY = "promotion.hot.occupancy";
	/** VIP等级已存在 */
	String PROMOTION_VIP_REQUIRED = "promotion.vip.required";
	/** 跑马灯条数限制：数量应为 1 ~ 100 */
	String MSG_MARQUEE_COUNT_LIMIT = "msg.marquee.count.limit";
	/** 最小金额必须大于 0 */
	String MSG_MARQUEE_MIN_LIMIT = "msg.marquee.min.limit";
	/** 请配置公告，最多10条 */
	String MSG_MARQUEE_PLACARDS = "msg.marquee.placards";
	/** 最大金额必须大于最小金额 */
	String MSG_MARQUEE_MAX_LIMIT = "msg.marquee.max.limit";
	/** 未登录用户 */
	String MSG_SCOPE_USER_NOT_LOGGED_IN = "msg.scope.user.not.logged.in";
	/** 登录用户 */
	String MSG_SCOPE_USER_LOGGED = "msg.scope.user.logged";
	/** 指定上级ID（直属） */
	String MSG_SCOPE_SPECIFY_AGENT_ID = "msg.scope.specify.agent.id";
	/** 指定顶级ID（全部下级） */
	String MSG_SCOPE_SPECIFY_TOP_AGENT_ID = "msg.scope.specify.top.agent.id";

	/** 结算周期 */
	String FIELD_AGENT_SETTLE_CYCLE = "field.agent.settle.cycle";
	/** 计算层级 */
	String FIELD_AGENT_CALC_LEVEL = "field.agent.calc.level";
	/** 稽核倍数 */
	String FIELD_AGENT_AUDIT_MULTIPLE = "field.agent.audit.multiple";
	/** 计算用户类型 */
	String FIELD_AGENT_USER_TYPE = "field.agent.user.type";
	/** 结算周期 */
	String FIELD_AGENT_TYPE = "field.agent.type";
	/** 前端展示配置 */
	String FIELD_AGENT_SHOW_CONFIG = "field.agent.show.config";
	/** 投注返佣配置 */
	String FIELD_PLAY_RATE_CONFIG = "field.play.rate.config";
	/** 投注返佣配置 */
	String FIELD_RECHARGE_RATE_CONFIG = "field.recharge.rate.config";
	/** 有效用户配置 */
	String FIELD_AGENT_VALID_USER_CONFIG = "field.agent.valid.user.config";
	/** 代理模式状态 */
	String FIELD_FIELD_AGENT_STATUS = "field.agent.status";

	/* 拼团枚举 */
	/** 团员普通用户 */
	String GROUP_USER_TYPE_NORMAL_USER = "group.user.type.normal.user";
	/** 团长 */
	String GROUP_USER_TYPE_HEAD = "group.user.type.head";
	/** 新用户 */
	String GROUP_USER_TYPE_NEW_USER = "group.user.type.new.user";
	/** 机器人 */
	String GROUP_USER_TYPE_ROBOT = "group.user.type.normal.robot";

	/** 初始状态 */
	String GROUP_RECHARGE_STATUS_INIT = "group.recharge.status.init";
	/** 未充值 */
	String GROUP_RECHARGE_STATUS_WAITING = "group.recharge.status.waiting";
	/** 已充值 */
	String GROUP_RECHARGE_STATUS_SUCCESS = "group.recharge.status.success";
	/** 已过期 */
	String GROUP_RECHARGE_STATUS_EXPIRED = "group.recharge.status.expired";
	/** 使用中待成功 */
	String GROUP_RECHARGE_STATUS_IN_USE = "group.recharge.status.in.use";

	/** 领取失败 */
	String GROUP_CLAIM_REWARDS_FAIL = "group.claim.rewards.fail";
	/** 初始状态 */
	String GROUP_CLAIM_REWARDS_INIT = "group.claim.rewards.init";
	/** 等待领取 */
	String GROUP_CLAIM_REWARDS_WAITING = "group.claim.rewards.waiting";
	/** 领取成功 */
	String GROUP_CLAIM_REWARDS_SUCCESS = "group.claim.rewards.success";
	/** 你不满足参团拼团条件，请满足条件后参与 */
	String GROUP_JOIN_UNSATISFIED = "group.join.unsatisfied";
	/** 你不满足发起拼团条件，请满足条件后参与 */
	String GROUP_LAUNCH_UNSATISFIED = "group.launch.unsatisfied";
	/** 拼团活动已过期，请参与其他拼团 */
	String GROUP_JOIN_STATUS_END = "group.join.status.end";
	/** 该拼团已满员，请选择其他拼团参与 */
	String GROUP_JOIN_STATUS_IS_FULL = "group.join.status.isFull";
	/** 当前未成团数量已达上限，请成团后再参与新拼团 */
	String GROUP_JOIN_MAX_SIZE_LIMIT = "group.join.max.size.limit";
	/** 您已参与该拼团 */
	String GROUP_JOINED = "group.joined";
	/** 无待领取奖励记录 */
	String GROUP_CLAIM_NO_RECORD = "group.claim.noRecord";
	/** 优惠券已过期 */
	String GROUP_RECHARGE_REWARD_EXPIRED = "group.recharge.reward.expired";
	/** 优惠券已使用 */
	String GROUP_RECHARGE_REWARD_USED = "group.recharge.reward.used";
	/** 进行中 */
	String GROUP_TASK_STATUS_PENDING = "group.task.status.pending";
	/** 已完成 */
	String GROUP_TASK_STATUS_FINISH = "group.task.status.finish";
	/** 已过期 */
	String GROUP_TASK_STATUS_EXPIRED = "group.task.status.expired";
	/** 已关闭 */
	String GROUP_TASK_STATUS_CLOSED = "group.task.status.closed";
	/** 支付类目名 */
	String PAY_CATEGORY_MOCK_TYPE_LABEL = "pay.category.mock.type.label.";
	/** 支付渠道类型名(支付方式名) */
	String PAY_CHANNEL_TYPE_LABEL = "pay.channel.type.label.";
	/** 该商户状态为结账/注销，不可接收转账 */
	String MERCHANT_TRANSFER_ERROR = "merchant.transfer.error";
	/** 不可向自己商户转账 */
	String MERCHANT_TRANSFER_NOT_FOR_SELF = "merchant.transfer.not.for.self";
	/** 支付平台别名 */
	String PAY_CHANNEL_LABEL = "pay.channel.label.";

	/** 充值问题 */
	String FAQ_TYPE_RECHARGE = "faq.type.recharge";
	/** 提现问题 */
	String FAQ_TYPE_CASH = "faq.type.cash";
	/** 游戏问题 */
	String FAQ_TYPE_GAME = "faq.type.game";
	/** 活动问题 */
	String FAQ_TYPE_PROMOTION = "faq.type.promotion";
	/** 代理问题 */
	String FAQ_TYPE_AGENT = "faq.type.agent";
	/** 其他问题 */
	String FAQ_TYPE_OTHER = "faq.type.other";
	/** 待发布 */
	String FAQ_STATUS_UNPUBLISHED = "faq.status.unpublished";
	/** 已发布 */
	String FAQ_STATUS_PUBLISHED = "faq.status.published";
	/** 印度地区不能开启PP */
	String MERCHANT_VENDOR_PP_NOT_OPEN = "merchant.vendor.pp.not.open";
	/** 巴西地区不能开启JL */
	String MERCHANT_VENDOR_JL_NOT_OPEN = "merchant.vendor.jl.not.open";
	/** 公告 */
	String MARQUEE_PLACARD = "marquee.placard";
	/** 中奖 */
	String MARQUEE_PRIZE = "marquee.prize";
	/** 提现 */
	String MARQUEE_CASH = "marquee.cash";
	/** 代理ID输入有误 */
	String FIELD_AGENT_ID_INVALID = "field.agentId.invalid";
	/** 渠道ID格式有误 */
	String FIELD_CHANNEL_ID_INVALID = "field.channelId.invalid";
	/** 指定代理ID和指定渠道二选一配置 */
	String COND_ONE_IN_TWO_INVALID = "cond.one.in.two.invalid";

	/** 发放失败的奖励已超时 */
	String FAIL_REWARD_STATUS_TIMEOUT = "fail.reward.status.timeout";
	/** 发放失败的奖励待处理 */
	String FAIL_REWARD_STATUS_PENDING = "fail.reward.status.pending";
	/** 发放失败的奖励已处理 */
	String FAIL_REWARD_STATUS_PROCESSED = "fail.reward.status.processed";
	/** 进行中 */
	String JACKPOT_TASK_STATUS_PENDING = "jackpot.task.status.pending";
	/** 已结束 */
	String JACKPOT_TASK_STATUS_END = "jackpot.task.status.end";
	/** 已关闭 */
	String JACKPOT_TASK_STATUS_CLOSED = "jackpot.task.status.closed";
	/** 进行中 */
	String JACKPOT_TASK_MEMBER_STATUS_PENDING = "jackpot.task.member.status.pending";
	/** 已完成 */
	String JACKPOT_TASK_MEMBER_STATUS_FINISH = "jackpot.task.member.status.finish";
	/** 已中奖 */
	String JACKPOT_TASK_MEMBER_STATUS_SUCCESS = "jackpot.task.member.status.success";
	/** 该厂商被上级关闭或维护，请联系客服！ */
	String MERCHANT_VENDOR_NOT_ALLOW_MODIFY = "merchant.vendor.not.allow.modify";
	/** 请先开启对应厂商才可开启子游戏 */
	String VENDOR_CLOSED_NOT_ALLOW_MODIFY_GAME = "vendor.closed.not.allow.modify.game";
	/** 该游戏被上级关闭，请联系客服 */
	String PLATFORM_GAME_CLOSED_NOT_ALLOW_MODIFY = "platform.game.closed.not.allow.modify";
	/** 子站点数量达到上限，请联系客服！ */
	String MERCHANT_SITE_MAX_LIMIT = "merchant.site.max.limit";

	/** 与原有配置冲突，请删除旧层级后重试修改 */
	String LAYER_CONFIG_DUPLICATE = "layer.config.duplicate";
	/** 系统默认 */
	String SYS = "sys";
	/** 自定义 */
	String CUSTOM = "custom";
	/** 该分类下有活动，暂不能操作！ */
	String PROMOTION_TAG_DEL = "promotion.tag.del";
	/** 投注任务 */
	String PLAY_TASK = "play.task";
	/** 该站点已存在找到我们配置，无需重复添加！ */
	String FIND_US_CONFIG_ADD_DUPLICATE = "find.us.config.add.duplicate";
	/** 默认右下角轮播悬浮图 */
	String DEFAULT_RIGHT_BOTTOM_CAROUSEL_FLOAT_IMG = "default.right.bottom.carousel.float.img";
	/** 默认左下角轮播悬浮图 */
	String DEFAULT_LEFT_BOTTOM_CAROUSEL_FLOAT_IMG = "default.left.bottom.carousel.float.img";
	/** 默认右下角展开悬浮图 */
	String DEFAULT_RIGHT_BOTTOM_EXPAND_FLOAT_IMG = "default.right.bottom.expand.float.img";
	/** 默认右下角展开悬浮图 */
	String DEFAULT_LEFT_BOTTOM_EXPAND_FLOAT_IMG = "default.left.bottom.expand.float.img";

	/** 利息宝结算周期分钟 */
	String USER_INCOME_PERIOD_MINUTES = "user.income.period.minutes";
	/** 利息宝结算周期小时 */
	String USER_INCOME_PERIOD_HOURS = "user.income.period.hours";
	/** 利息宝结算周期无限制 */
	String USER_INCOME_MAX_PERIOD_UNLIMITED = "user.income.max.period.unlimited";
	/** 存在展示中的数据，不允许删除！ */
	String MODULE_MANAGE_NOT_ALLOW_DELETE = "module.manage.not.allow.delete";

	/** 白银转盘 */
	String TURNTABLE_TYPE_SILVER = "turntable.type.silver";
	/** 黄金转盘 */
	String TURNTABLE_TYPE_GOLD = "turntable.type.gold";
	/** 钻石转盘 */
	String TURNTABLE_TYPE_DIAMOND = "turntable.type.diamond";
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
	/** 手动增加 */
	String TURNTABLE_TRADE_TYPE_ADJUST_IN = "turntable.trade.type.adjust.in";
	/** 固定奖金 */
	String TURNTABLE_RATE_TYPE_FIXED_BONUS = "turntable.rate.type.fixed.bonus";
	/** 随机奖金 */
	String TURNTABLE_RATE_TYPE_RANDOM_BONUS = "turntable.rate.type.random.bonus";
	/** 实物 */
	String TURNTABLE_RATE_TYPE_ARTICLE = "turntable.rate.type.article";
	/** 0概率奖品 */
	String TURNTABLE_RATE_TYPE_NO_PROBABILITY = "turntable.rate.type.no.probability";
	/** 谢谢惠顾 */
	String TURNTABLE_RATE_TYPE_THANK = "turntable.rate.type.thank";
	/** 成本范围%s ~ %s，计算总概率为 %.2f%%，无法变更，请调整成本或奖金 */
	String REWARD_ITEM_LIMIT_MSG = "reward.item.limit.msg";


}