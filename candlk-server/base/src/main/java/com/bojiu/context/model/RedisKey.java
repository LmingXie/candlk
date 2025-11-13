package com.bojiu.context.model;

import com.bojiu.common.redis.Permanent;
import com.bojiu.common.redis.RedisUtil;

/***
 * 全局共享的 Redis 键
 */
public interface RedisKey {

	/** APP审核版本 */
	@Permanent
	String APP_VERSION_PREFIX = "appVersion:";

	/** 用户操作分布式锁前缀 */
	String USER_OP_LOCK_PREFIX = "user:lock:";

	/** 代理邀请关系同步 */
	String AGENT_LOCK_PREFIX = "AGENT:lock:";

	/**
	 * 商户站点状态 Hash
	 * <p> < "merchantId",  SiteStatus. value | MaintainEntry（JSON） >    MaintainEntry 表示维护状态的JSON字符串
	 * <p> < "@merchantId",  SiteStatus. value | MaintainEntry（JSON） >  以 "@" 开头的表示平台控制的状态
	 * <p>
	 * 【注意】这里存的不是全量数据，只有变更为异常状态时，才必定有值
	 */
	String MERCHANT_SITE_STATUS = "merchant:site:status";

	/**
	 * 商户状态 Hash
	 * <p> < "merchantId",  groupStatus. value >  不是“@” 开头的是商户自行控制的状态
	 * <p> < "@merchantId",  groupStatus. value） >  以 "@" 开头的表示平台控制的状态
	 * <p>  @0 ：表示总台一键全站维护
	 * <p>
	 * 【注意】这里存的是全量数据，每次变更时，必定有值
	 */
	String MERCHANT_STATUS = "merchant:status";

	/**
	 * 商户站点域名 Hash
	 * <p> < "域名", 商户ID >
	 * 【注意】域名存入之前一定要先转小写
	 */
	String MERCHANT_DOMAINS = "merchantDomains";
	/**
	 * 商户后台域名 Hash
	 * <p> < "域名", 商户ID >
	 * 【注意】域名存入之前一定要先转小写
	 */
	String MERCHANT_BG_DOMAINS = "merchantBgDomains";
	/**
	 * 用户的游戏 有效投入 汇总 ZSet
	 * <p> $prefix + $merchantId =  < 用户ID, 打码金额（最后两位表示小数） >
	 * <p> 累计打码是自动层级的晋级条件，更新后一同出发层级刷新，不可更改为重量级任务！</p>
	 */
	String USER_STAT_PLAY_PREFIX = "userStat:play:";
	/**
	 * 用户的游戏 净输赢 汇总 ZSet
	 * <p> $prefix + $merchantId =  < 用户ID, 净输赢金额（负数表示输了，最后两位表示小数） >
	 */
	String USER_STAT_WIN_PREFIX = "userStat:win:";
	/**
	 * 用户的 【充值】汇总统计数据 ZSet
	 *
	 * <p> $prefix + $merchantId =  < $userId, 充值金额（最后两位表示小数）>
	 * <p> $prefix + $merchantId + "-count" =  < $userId, 充值次数>
	 */
	String USER_STAT_RECHARGE_PREFIX = "userStat:recharge:";

	/**
	 * 用户游戏投入按 天 | 周 | 月 汇总 ZSet （最后2位整数表示小数）
	 * <p>
	 * 按天 < "userPlayIns-$yyyyMMdd", "$userId", 投入 >
	 * <p>
	 * 按周 < "userPlayInsW-$yyyyMMdd", "$userId", 投入 >
	 * <p>
	 * 按月 < "userPlayInsM-$yyyyMM", "$userId", 投入 >
	 */
	String USER_PLAY_INS_PREFIX = "userPlayIns";

	/**
	 * 用户游戏净输赢按 天 汇总 ZSet （最后2位整数表示小数）
	 * <p>
	 * 按天 < "userWin-$yyyyMMdd", "$userId", 净输赢总额 >
	 * 按周 < "userWinW-$yyyyMMdd", "$userId", 净输赢总额 >
	 * 按月 < "userWinM-$yyyyMM", "$userId", 净输赢总额 >
	 */
	String USER_WIN_PREFIX = "userWin";
	/**
	 * 救援金活动，保存用户上一周期的净输赢
	 * <p>
	 * 按天 reliefPromotion:+活动id+“：”+$yyyyMMdd+“-”+$userId 净输赢总额
	 * 按周 reliefPromotion:+活动id+“：”+$yyyyMMdd+“W-”+$userId 净输赢总额
	 * 按月 reliefPromotion:+活动id+“：”+$yyyyMM+“M-”+$userId 净输赢总额
	 */
	String RELIEFP_ROMOTION_LAST_USER_WIN = "reliefPromotion:";

	String dailySep = "-";
	String weeklySep = "W-";
	String monthlySep = "M-";

	/**
	 * 用户游戏【最大单笔打码】按 天 | 周 （目前任务中心使用）
	 * <pre>
	 * ZSet < "userIns-$yyyyMMdd", "$userId", 投入 >
	 *      < "userInsW-$yyyyMMdd", "$userId", 投入 ></pre>
	 */
	String USER_PLAY_BIG_INS = "userPlayBigIns";

	/**
	 * 任务中心用户活跃度
	 * <pre>{@code ZSet $userTaskPoint-$merchantId-D/W-yyyyMMdd "< $userId, $point >" }</pre>
	 */
	String USER_TASK_POINT_PREFIX = "userTaskPoint:";
	/**
	 * 已完成新人任务记录
	 * <pre>{@code
	 * ZSet
	 * 写入任务是否完成【位运算】（目前只记录新人福利任务，最大值：3584）：
	 *      "< $userId, $bizFlag >"
	 * 添加已完成的任务：
	 *      "incrementScore($RewardCollectType.getBizFlag())"
	 * 删除已完成的任务：
	 *      "incrementScore(-$RewardCollectType.getBizFlag())"
	 *  }</pre>
	 */
	String USER_TASK_LOGS_PREFIX = "userTaskLogs:";

	/**
	 * 任务中心-新人福利任务-同IP/设备号限制 领取一次
	 * <pre>{@code Set userTaskLimit:$merchantId < "$ip/deviceId-$rewardType" > }
	 * 取消限制时，将删除历史记录：@see UserTaskConfigAdminService#addOrEditBase</pre>
	 */
	String USER_TASK_LIMIT_PREFIX = "userTaskLimit:";

	/**
	 * 用户充值 按 周 | 月 | 天 | 累计 汇总 ZSet
	 * <ul>
	 * <li> 按天 < "userRecharge-$yyyyMM", "$userId", 充值金额 >
	 * <li> 按周 < "userRechargeW-$yyyyMMdd", "$userId", 充值金额 >
	 * <li> 按月 < "userRechargeM-$yyyyMM", "$userId", 充值金额 >
	 * </ul> 充值金额不含赠送金额（最后2位整数表示小数）
	 */
	String USER_RECHARGE_PREFIX = "userRecharge";

	/** VIP升级候选人（用于依次检测用户是否可升级VIP） Set < "upgradeCandidates-$merchantId , $userId" > */
	String UPGRADE_CANDIDATES = "upgradeCandidates";
	/**
	 * VIP降级候选人 ZSet < "degradeCandidates, "$merchantId-$userId" , $nextMonthBeginTime >
	 * <p> 在指定时间检测这些用户是否需要降级并处理
	 */
	String DEGRADE_CANDIDATES = "degradeCandidates";

	/**
	 * VIP升级用户 累计充值金额、月充值、充值次数偏移值 ZSet （最后2位表示小数）
	 * <p> $prefix + 商户ID = < "$userId" , 充值总额偏移值（一般为负数） >
	 * <p> 用户VIP升级后，下个月会检测是否符合保级条件，如果不符合则会被降级，用于判断升级的指标也会重置到上一级
	 * <p> $prefix + 商户ID + $yyyyMM = < "$userId" , 月充值偏移值（一般为负数） >
	 * <p> $prefix + 商户ID + "-count" = < "$userId" , 充值次数偏移值 >
	 */
	String UPGRADE_OFFSET_RECHARGE_PREFIX = "upgradeOffset:recharge:";
	/**
	 * VIP升级用户 累计打码金额偏移值 ZSet （最后2位表示小数）
	 * <p> $prefix + 商户ID = < "$userId" , 打码总额偏移值（一般为负数） >
	 * <p> 用户VIP升级后，下个月会检测是否符合保级条件，如果不符合则会被降级，用于判断升级的指标也会重置到上一级
	 * <p> $prefix + 商户ID + $yyyyMM = < "$userId" , 月打码偏移值（一般为负数） >
	 */
	String UPGRADE_OFFSET_PLAY_PREFIX = "upgradeOffset:play:";
	/**
	 * 商户的游戏分类输赢汇总 ZSet 【最后2位整数表示小数】
	 * <p> $prefix + 商户ID =
	 * <pre>
	 *  < "$yyyyMM-$vendor.name()-$gameType下标", 输赢值（可能为负数） >
	 *  < "$yyyyMM-totalPay", 已出账单未结算总额 > （当账单生成后才有此值，同时会删除上面的同月数据）
	 *  < "$yyyyMM-$vendor.name()-$gameType下标-$rtpType", 打码量 >
	 *  $rtpType：0 = 大于100；1=小于100
	 * </pre>
	 */
	String MERCHANT_TYPE_WINS_PREFIX = "merchantTypeWin:";
	/**
	 * 待风控检查的站点ID Set 集合
	 */
	String RISK_MERCHANT_IDS = "riskMerchantIds";

	/**
	 * 商户黑名单（全部商家共用）
	 * <pre>
	 *     {@code Set <
	 *     黑名单对指定用户限制："$userId-$limitType",
	 *     黑名单/刷子监控对指定IP/设备号限制："$merchantId-$IP-$limitType"
	 *     游戏获利监控对用户限制（目前只限制领取优惠）："$userId-$limitType-spy",
	 *     刷子监控对用户限制（目前只限制领取优惠、禁止进入游戏）："$userId-$limitType-bot",
	 *     >
	 *     }
	 * </pre>
	 */
	String MERCHANT_BLACKLIST = "merchant:blacklist";

	/**
	 * 商户充值 汇总
	 * hash < "merchantRecharge", "merchantId", 充值金额（最后6位整数表示小数） >
	 */
	String MERCHANT_RECHARGE = "merchantRecharge";

	/** 商户是否晋级前缀 Set < merchantId > */
	String UPGRADE_MERCHANT = "upgrade:merchantId";

	/**
	 * 首充活动 ZSet < "firstRechargePromotion"+$promotionId, $merchantId+$type+$promotionId+$promotionCond+$cycleMode+$yyyyMMdd+$userId , rechargeAmount >
	 * $expireTime = yyyyMMdd（活动结束时间）
	 * $rechargeAmount 最后两位为小数
	 */
	String FIRST_RECHARGE_PROMOTION = "firstRechargePromotion";
	/**
	 * 每日首充活动 ZSet < "dailyFirstRechargePromotion"+$promotionId, $merchantId+$type+$promotionId+$promotionCond+$cycleMode+$yyyyMMdd+$userId , rechargeAmount >
	 * $expireTime = yyyyMMdd（活动结束时间）
	 * $rechargeAmount 最后两位为小数
	 */
	String DAILY_FIRST_RECHARGE_PROMOTION = "dailyFirstRechargePromotion";
	/**
	 * 单笔充值活动 List < "singleRechargePromotion"+$promotionId+$expireTime, $merchantId+$type+$promotionId+$promotionCond+$cycleMode+$yyyyMMdd+$userId + rechargeAmount >
	 * $expireTime = yyyyMMdd（周期结束时间）
	 * $rechargeAmount 最后两位为小数
	 */
	String SINGLE_RECHARGE_PROMOTION = "singleRechargePromotion";

	/**
	 * 充值方式活动，仅用于保存用户使用的充值方式 Hash<"rechargeWayPromotion+$promotionId", $userId, $rechargeWay>
	 */
	String RECHARGE_WAY_PROMOTION = "rechargeWayPromotion";

	/**
	 * 累计充值活动 ZSet < "totalRechargePromotion"+$promotionId+$expireTime, $merchantId+$type+$promotionId+$promotionCond+$cycleMode+$yyyyMMdd+$userId, rechargeAmount >
	 * $expireTime = yyyyMMdd（周期结束时间）
	 * $rechargeAmount 最后两位为小数
	 */
	String TOTAL_RECHARGE_PROMOTION = "totalRechargePromotion";
	/**
	 * 充值次数活动 ZSet < "timesRechargePromotion"+$promotionId+$expireTime, $merchantId+$type+$promotionId+$promotionCond+$cycleMode+$yyyyMMdd+$userId, amountTimes >
	 * $amountTimes 最后两位为次数，两位之前为充值金额；
	 * $expireTime = yyyyMMdd（周期结束时间）
	 */
	String TIMES_RECHARGE_PROMOTION = "timesRechargePromotion";
	/**
	 * 累计打码活动 ZSet < "totalPayPromotion"+$promotionId+$expireTime, $merchantId+$type+$promotionId+$cycleMode+$yyyyMMdd+$userId, payAmount >
	 * $expireTime = yyyyMMdd（周期结束时间）
	 * $payAmount 最后两位为小数
	 */
	String TOTAL_PAY_PROMOTION = "totalPayPromotion";
	/**
	 * 通用活动统计Key
	 * <p>
	 * < $prefix-$merchantId-$promotionId-D/W/M-$cycle >
	 * </p>
	 */
	String PROMOTION_STAT_PREFIX = "promotionStat";
	/**
	 * 任务统计Key
	 * <p>
	 * < $prefix-$merchantId-D/W/M-$cycle >
	 * </p>
	 */
	String TASK_STAT_PREFIX = "taskStat";
	/** 通用活动打码后缀标记 */
	String PLAY_SUFFIX = "-P";
	/** 通用活动累计充值标记 */
	String RECHARGE_SUFFIX = "-R";
	/** 通用活动单局最大打码标记 */
	String BIG_PLAY_SUFFIX = "-BR";

	/**
	 * 用户活动 Set < 用户参与活动的key >
	 * <p>通过$Task标记任务中心需要结算的用户</p>
	 */
	String USER_PROMOTION = "userPromotion";
	/** 任务中心标记用户参与过 */
	String TASK_PREFIX = "Task";

	/**
	 * 实时返水 ZSet <br/>
	 * 用户总投注：< $userId-$vendor_alias-$game_type-IN, $playCoin > <br/>
	 * 用户可申领奖金：< $userId-$vendor_alias-$game_type-IN, $amount > <br/>
	 * 定制版商户：rebateList-$merchantId-$yyyyMMdd <br/>
	 */
	String REBATE_LIST = "rebateList";
	/** 商户修改配置锁（避免修改时迁移数据导致遗留问题） */
	String REBATE_EDIT_OP = "rebateEditOp:";

	/**
	 * 用户【前一】 日 | 周 | 月 的VIP等级 Hash【注意$yyyyMMdd是当前周期！！】
	 * <li> < "userLevel-$yyyyMM", $userId, level >
	 * <li> < "userLevel-$yyyyMMdd", $userId, level >
	 * <li> < "userLevel", $userId, level > level 前两位为更新人工调整等级，后两位为实际升级等级
	 */
	String USER_LEVEL = "userLevel";

	/** 用户跑马灯公告 Hash< 商户ID, version > */
	String USER_MSG_MARQUEE_VERSION = "userMsgMarqueeVersion";

	/**
	 * 刷子监控去重名单
	 * <pre>
	 * {@code ZSet <  $typePrefix-$typeValue-$userId , $currentTimeMillis> }
	 * </pre>
	 */
	String BOT_SPY_PREFIX = "merchant:botSpy:";

	/**
	 * 刷子监控计数器
	 * <pre>
	 * {@code ZSet <  $typePrefix-$typeValue, $currentTimeMillis / 1000 * $CounterDefaultValue> }
	 * score：
	 *     $currentTimeMillis / 1000：秒级时间戳（用于进行批量删除，仅当incrementScore结果等于增量值时进行初始化设置）
	 *     $CounterDefaultValue：100000（目前触发值最大100）
	 * 删除清理程序：
	 *      {@code UserBotSpyLogService#clearBotSpyCache}
	 * </pre>
	 */
	String BOT_SPY_COUNTER_PREFIX = "merchant:botSpyCounter:";

	/** 用户签到 ZSet < "userSign"+$promotionId, $userId+$signMode+yyyyMMdd|次数|领取金额|领取积分 > */
	String USER_SIGN = "userSign";

	/** 大R登录提醒 ZSet $merchantId < $userId, $seconds+$level > */
	String BIG_R_POPUP_PREFIX = "bigRPopUp:";
	/** 优惠活动开始生效时间 ZSet < "promotionValid", $merchantId-$promotionId, 时间戳 > */
	String PROMOTION_VALID = "promotionValid";
	/** 优惠活动结束时间 ZSet < "promotionExpire", $promotionId, 时间戳 > */
	String PROMOTION_EXPIRE = "promotionExpire";

	/** 冻结商户名单 Set < $merchantId > */
	String FROZEN_MERCHANT_LIST = "frozen_merchant";

	/**
	 * 风控审核展示：用户游戏风控 Hash < $userId-"game",触发游戏获利监控的次数 >
	 * 风控审核展示：用户账户风控 Hash < $userId-"account",触发游戏获利监控的次数 >
	 */
	String USER_RISK = "user:risk";
	/** 用户游戏风控后缀 */
	String suffixGameRisk = "-game";
	/** 用户账户风控后缀 */
	String suffixAccountRisk = "-account";
	/** 总台操作 暂存的站点状态 Hash：$Prefix + SiteStatus.name() => < 商户ID, 变更前的 status > */
	String SITE_STATUS_PREFIX = "site:status:";

	/** 商户存钱宝开关 BitMap < 商户ID, 开关状态（1=开；0=关）  > */
	String MERCHANT_TOGGLE_INCOME = "merchant:toggle:income";

	/**
	 * 游戏厂家统计 周 | 月 | 总数 String
	 * <li> < "gameVendorStatW-$yyyyMMdd, 数量 >
	 * <li> < "gameVendorStatM-$yyyyMM, 数量 >
	 * <li> < "gameVendorStat, 数量 >
	 */
	String GAME_VENDOR_STAT = "gameVendorStat";
	/**
	 * 商户统计 周 | 月 | 总共数量 String
	 * <li> < "merchantStatW-$country-$yyyyMMdd, 数量 >
	 * <li> < "merchantStatM-$country-$yyyyMM, 数量 >
	 * <li> < "merchantStat-$country, 数量 >
	 */
	String MERCHANT_STAT = "merchantStat";
	/**
	 * 商户充值统计 周 | 月 | 总共数量 String
	 * <li> < "merchantRechargeStatW-$yyyyMMdd, 数量 >
	 * <li> < "merchantRechargeStatM-$yyyyMM, 数量 >
	 * <li> < "merchantRechargeStat, 数量 >
	 */
	String MERCHANT_RECHARGE_STAT = "merchantRechargeStat";
	/**
	 * 厂家游戏类型商户数统计 Hash < $vendor+"-typeMerchantStat", $gameType, $num >
	 * 厂家游戏类型国家商户数统计 Hash < $vendor-$country+"-typeMerchantStat", $gameType, $num >
	 */
	String suffixTypeMerchantStat = "-typeMerchantStat";
	/** 厂家游戏类型投注统计 Hash < $vendor+"-typePlayStat", $gameType, $num > */
	String suffixTypePlayStat = "-typePlayStat";
	/**
	 * 总台首页-商户充值按日 汇总
	 * value < "merchantDailyRecharge"-$country-$yyyyMMdd, 每日累计充值金额（最后6位整数表示小数，单位u） >
	 */
	String MERCHANT_DAILY_RECHARGE = "merchantDailyRecharge";
	/**
	 * 总台首页-商户充值按月按国家-每日充值的商户 汇总
	 * ZSet < "rechargeMerchantDetail"-$country-$yyyyMMdd, $merchantId, $amount >
	 */
	String RECHARGE_MERCHANT_DETAIL = "rechargeMerchantDetail";

	/**
	 * 存储需要回滚的尚未完成的游戏余额 转入/转出 记录ID
	 * Set < GameTransferLog.id >
	 * <p>
	 * 【注意】在将对应记录放入该集合的同时，还要将该记录的 update_time 改为等同于 add_time。即：
	 * <pre><code>
	 * UPDATE gs_game_transfer_log SET update_time = add_time WHERE id = $logId AND `status` = 0
	 * SADD game:trans:rollback $logId
	 * SET gameTransHandledMaxId $logId-1
	 * </code></pre>
	 */
	String GAME_TRANS_ROLLBACK = "game:trans:rollback";
	/**
	 * 渠道统计前缀
	 * ZSet < "channelStat"-$yyyyMMdd-$suffix-$channelId, $value >
	 * Hash < "channelStat"-$yyyyMMdd-$channelId, <$suffix, $value >>
	 */
	String CHANNEL_STAT_PREFIX = "channelStat";

	/** 自研游戏控制taskId计数器 String < "$merchantId",$counter >（单用户唯一，批量操作可以使用相同ID） */
	String GAME_CTRL_TASK_ID_COUNTER = "gameCtrlTaskIdCounter";

	/**
	 * <h2>优惠活动领取限制</h2>
	 * <h3>抢红包活动（ZSet promotionLimit:$promotionId）</h3>
	 * ->    < $userId, $limit > 用户活动期间领取的红包数<br/>
	 * ->    < $userId-$HM, $limit > 派发时段领取的红包数<br/>
	 * ->    < total, $total > 活动总共发放的红包数<br/>
	 * ->    < $total-$HM, $count > 派发时段已发放的红包数<br/>
	 * <h3>新人彩金活动（Set promotionLimit:$promotionId）</h3>
	 * ->    <$userId> 一人一码<br/><hr/><br/>
	 * <h3>自定义活动（Set promotionLimit:$promotionId（随活动过期）| Set promotionLimit:$promotionId-yyyyMMdd（3天过期））</h3>
	 * ->    < $userId, $count >    （当天）已申请次数<br/><hr/><br/>
	 */
	String PROMOTION_LIMIT_PREFIX = "promotionLimit:";
	/**
	 * 活动期间可发放的活动红包总金额
	 * <pre>
	 * 存储值：Long.Max_VALUE - 总量
	 * 剩余 = Long.Max_VALUE - 存储值
	 * 已发 = 总量-剩余
	 * 超出验证：{@link RedisUtil#incrementOverflow}(最后一个随机红包取差值)
	 * 后台修改：
	 *      增加总金额时 -> Long.Max_VALUE - 增量（old总量-new总量）
	 *      减少总金额时 -> Long.Max_VALUE + 增量 -> 捕获Redis溢出异常 -> 更改为 Long.Max_VALUE（停止发放）
	 * </pre>
	 */
	String RED_ENVELOPE_TOTAL_AMOUNT = "redEnvelopeUseTotalAmount:";
	/**
	 * 商户白名单 Hash < $merchantId,  $IPS >
	 */
	String MERCHANT_WHITELIST = "merchant:whitelist";
	/**
	 * 新人彩金兑换码 ZSet $promotionId < $code, $Status >  <br/>
	 * 通用型兑换码使用次数：< +$code, $Counter > <br/>
	 */
	String PROMOTION_REDEEM_CODE = "promotionRedeemCode:";
	/**
	 * 查询商户余额 Hash < "merchantBalance"-$merchantId, <$payChannel, $value >>
	 */
	String MERCHANT_BALANCE_PREFIX = "merchantBalance:";

	/** 用户正在玩的游戏厂商：Hash < $userId, $vendor > */
	String USER_IN_GAME_VENDOR = "userInGameVendor";

	/**
	 * 站点维护时使用
	 * Hash 商户客服 < 商户ID， "客服配置" >
	 */
	String MERCHANT_CUSTOMER_SERVICE = "merchantCustomerService";

	/**
	 * 经销商前缀
	 * Hash 经销商首充配置 dealerStat:recharge < 用户ID， "首充奖励，首充门槛奖励，累计充值奖励，邀请奖励，首充三级奖励, 累充三级奖励，首充人数，vip奖励" >
	 */
	String DEALER_STAT_PREFIX = "dealerStat:";
	/**
	 * 用户每日最大投注额
	 * ZSet < userDailyMaxCoin-$yyyyMMdd, $userId, $maxCoin >
	 */
	String USER_DAILY_MAX_COIN_PREFIX = "userDailyMaxCoin";
	/**
	 * 每个渠道充值笔数 成功笔数、总笔数 Hash
	 * < "channelRecharge", "$channel"-suc, 充值次数 >
	 * < "channelRecharge", "$channel"-total, 充值次数 >
	 */
	String CHANNEL_RECHARGE = "channelRecharge";
	/**
	 * 统计标记：领取记录、充值记录、提现记录
	 * < statMarker, rewardCollect, $marker >
	 * < statMarker, recharge, $marker >
	 * < statMarker, cash, $marker >
	 */
	String STAT_MARKER = "statMarker";
	/**
	 * VIP充值人数
	 * HASH < vipRechargeNum-$merchantId-$period-$level, $userId, $amount >
	 */
	String VIP_RECHARGE_NUM_PRE = "vipRechargeNum";
	/**
	 * VIP提现人数
	 * HASH < vipCashNum-$merchantId-$period-$level, $userId, $amount >
	 */
	String VIP_CASH_NUM_PRE = "vipCashNum";
	/**
	 * 商户VIP总人数
	 * HASH < vipNum-$merchantId, $level, $num >
	 */
	String VIP_NUM_PRE = "vipNum";
	/**
	 * 每个活动领取用户数
	 * Set < promotionUserStat-$promotion-$yyyyMMdd, $userId >
	 */
	String PROMOTION_USER_STAT = "promotionUserStat";
	/**
	 * 商户单日触发获利监控次数限制
	 * Set < $merchantId, $counter >
	 */
	String PROFIT_SPY_COUNTER = "profitSpyCounter";
	/**
	 * 商户下的推广账号
	 * Set < promoteAccount-$merchantId, $userId >
	 */
	String PROMOTE_ACCOUNT_PREFIX = "promoteAccount";
	/**
	 * 存在推广账号的商户
	 * Set < promoteAccountMerchant, promoteAccount-$merchantId >
	 */
	String PROMOTE_ACCOUNT_MERCHANT = "promoteAccountMerchant";
	/**
	 * 商户VIP统计标记，统计到哪一天了
	 * HASH < merchantVipStatMarker, $merchantId, $yyyyMMdd >
	 */
	String MERCHANT_VIP_STAT_MARKER = "merchantVipStatMarker";

	/**
	 * 自动层级时间分片（5min/批）<p>
	 * Set < $merchantId, $userId ><p>
	 * 按时间分片聚合数据，通过定时任务批量刷新用户层级
	 */
	String AUTO_LAYER_TIME_SHARD = "autoLayerTimeShard-";
	/**
	 * 用户层级缓存前缀<p>
	 * ZSet autoLayer:$merchantId < $userId, $layerId >
	 */
	String USER_LAYER_PREFIX = "autoLayer:";
	/**
	 * 活跃用户key<p>
	 * ZSet userActive:$merchantId < $userId, $active >
	 */
	String USER_ACTIVE_PREFIX = "userActive:";
	/**
	 * 提示音标记：HASH < promptSoundMarker, $groupId, $maxId >
	 * maxId：标记位（处理到哪个位置了）
	 */
	String PROMPT_SOUND_MARKER = "promptSoundMarker";
	/**
	 * PGC损益商户
	 */
	String PGC_PL_MERCHANT = "pgcPLMerchant";
	/**
	 * 商户月账单标记、商户旧分成比例
	 * Hash < "merchantOldShare", $merchantId, $shareConfig >
	 */
	String MERCHANT_BILL_MARK = "merchantBillMark";
	/** 代理管理配置的用户ID Set < merchantAgentManager, $topUserId > */
	String MERCHANT_AGENT_MANAGER = "merchantAgentManager";
	/**
	 * 统计标记：打码统计
	 * < statAgentPlayMarker, $merchantId, $yyyy-MM-dd HH >
	 */
	String STAT_AGENT_PLAY_MARKER = "statAgentPlayMarker";

	/**
	 * 代理商余额预警检测 Set < $merchantId-$agentId >
	 */
	String MERCHANT_AGENT_WARN = "agentWarn";
	/**
	 * 代理统计实时标记
	 * String
	 */
	String AGENT_STAT_REAL_MARKER = "agentStatRealMarker";
	/**
	 * 需要生成日账单的商户（ 限制 PGC ）
	 * Set< $merchantId >
	 */
	String MERCHANT_DAILY_BILL_LIST = "merchantDailyBillList";
	/**
	 * 闯关人
	 * ZSet< challengePromoter-$yyyyMMdd, $promotionId, $invitedUserId >
	 */
	String CHALLENGE_PROMOTER = "challengePromoter";
	/**
	 * 闯关邀请
	 * Set< challengeInvited, $merchantId-$promotionId-$invitedUserId-$yyyyMMdd > 邀请成功列表
	 * Hash< challengeInvited-$promotionId-$invitedUserId-$yyyyMMdd, $userId, $bizFlag > 被邀请人列表，只是临时的，未到达条件的也在其中
	 */
	String CHALLENGE_INVITED = "challengeInvited";
	/**
	 * 被邀请人IP，只是临时的，未到达条件的也在其中
	 * Hash< challengeInvitedIP-$promotionId-$invitedUserId-$yyyyMMdd，$userId, $IP >
	 */
	String CHALLENGE_INVITEE_IP = "challengeInviteeIP";
	/**
	 * 邀请人IP（含闯关人首次登录IP+邀请成功的人注册IP）
	 * Set< challengeInviterIp-$promotionId-$invitedUserId-$yyyyMMdd，$IP >
	 */
	String CHALLENGE_INVITER_IP = "challengeInviterIp";
	/**
	 * 闯关邀请成功用户列表
	 * Set< challengeInvited-$merchantId-$promotionId-$invitedUserId-$yyyyMMdd, $userId >
	 */
	String CHALLENGE_INVITED_SUCCESS = "challengeInvitedSuccess";
	/**
	 * 闯关打码
	 * Set< challengePlay, $promotionId-$invitedUserId-$yyyyMMdd > 闯关打码列表
	 */
	String CHALLENGE_PLAY = "challengePlay";
	/**
	 * 闯关活动同IP用户列表 Set< this-$yyyyMMdd， 用户ID >
	 */
	String CHALLENGE_SAME_IP_USER = "challengeSameIpUser";

	/**
	 * 救援金活动，用户总共领取的救援金额、总次数、每日次数
	 * Hash < reliefPromotion-$merchantId-$promotionId, $userId-TA, $totalAmount >
	 * Hash < reliefPromotion-$merchantId-$promotionId, $userId-TT, $totalTimes >
	 * Hash < reliefPromotion-$merchantId-$promotionId, $userId-$yyyyMMdd, $times >
	 */
	String RELIEF_PROMOTION = "reliefPromotion";

	/**
	 * 救援金活动，用户总共领取的救援金额、总次数、每日次数
	 * Hash < reliefPromotionDailyLimit-$merchantId-$promotionId, $userId, $times >
	 */
	String RELIEF_PROMOTION_DAILY_LIMIT = "reliefPromotionDailyLimit";
	/**
	 * 救援金活动偏移值，充值偏移（前一次总充值）
	 * Hash < reliefOffsetRecharge-$merchantId-$promotionId, $userId, -$offsetVal >
	 */
	String RELIEF_OFFSET_RECHARGE = "reliefOffsetRecharge";
	/**
	 * 救援金活动偏移值，打码偏移（前一次总打码）
	 * Hash < reliefOffsetPlay-$merchantId-$promotionId, $userId, -$offsetVal >
	 */
	String RELIEF_OFFSET_PLAY = "reliefOffsetPlay";
	/**
	 * 余额救援金活动-需要继续充值
	 * <p>Set $merchantId < $userId >
	 */
	String RELIEF_GAME_NEED_RECHARGE = "reliefNeedRecharge";
	/** （-un）用于标记余额救援金未充值用户 */
	String RELIEF_BALANCE_UN_SEP = "-un";
	/**
	 * 余额救援金活动-只可进PGC
	 * <p>Set $merchantId < $userId >
	 */
	String RELIEF_GAME_LIMIT_PGC = "reliefCanCtrlGame";

	/**
	 * 商户多扣的线路费
	 * Hash < merchantNetFee-$yyyyMM, $merchantId, $overcharge >
	 * 商户服务器费用首月到期日
	 * Hash < merchantNetFee, $merchantId, $expireDay>
	 */
	String MERCHANT_NET_FEE = "merchantNetFee";
	/**
	 * 参与救援金的用户
	 * ZSet< relief-$merchantId, $userId >
	 */
	String RELIEF = "relief";
	/** 新增稽核分布式锁前缀 */
	String AUDIT_OP_LOCK_PREFIX = "audit:lock:";
	/**
	 * 闯关游戏正
	 * key: challengePlayGame:$promotionId-$yyyyMMdd
	 * value: 正在进行的游戏信息
	 **/
	String CHALLENGE_PLAY_GAME = "challengePlayGame:";

	static String getDailyKey(String key, String ymd) {
		return key + dailySep + ymd;
	}

	/**
	 * VIP升级用户数 Hash < "UpgradeNum-$yyyyMMdd-$merchantId, "$userId" , $level >
	 * <p> 记录升级用户数
	 */
	String UPGRADE_NUM_PREFIX = "UpgradeNum";

	/**
	 * VIP日周月奖励条件为0的用户
	 * ZSet< "vipRewardZeroCond", "$sep$merchantId-$userId", 时间 >
	 * $sep=["-" | "W-" | "M-"]
	 */
	String VIP_REWARD_ZERO_COND = "vipRewardZeroCond";
	/**
	 * 印度JDB游戏列表
	 * Hash< "jdbInrGameIds", "$gid", "$gameId" >
	 */
	String JDB_INR_GAME_IDS = "jdbInrGameIds";
	/**
	 * 用户游戏总输金额汇总 ZSet
	 * <p> $prefix + $merchantId =  < 用户ID, 总输金额 >
	 */
	String USER_STAT_LOSE_COIN_PREFIX = "playLoseCoin:";
	/**
	 * 未结算服务器费用
	 * Hash < "unsettledServerFee", $merchantId, $amount >
	 */
	String UNSETTLED_SERVER_FEE = "unsettledServerFee";
	/**
	 * 服务器费用最后生成日期
	 * Hash < "serverFeeLastDate", $merchantId, $yyyyMMdd >
	 */
	String SERVER_FEE_LAST_DATE = "serverFeeLastDate";
	/**
	 * 经销商VIP统计
	 * Set < "dealer_v_"+$LEVEL + '-' + $dealer_id, $user_id >
	 * ZSet < "dealer_v_" + $LEVEL, $dealer_id, $totalRecharge >
	 */
	String DEALER_V_PREFIX = "dealer_v_";
	/**
	 * 临时服务器费用，到了结算日移到未结算服务器费用
	 * ZSet < "tempServerFee", $merchantId-$serverAmount, settlementTime >
	 */
	String TEMP_SERVER_FEE = "tempServerFee";
	/**
	 * ZSet < "dealerVipReward"+$merchantId, $userId+$isRecharge+$level+$dealerVipKey, $now >
	 */
	String DEALER_VIP_REWARD = "dealerVipReward";
	/**
	 * 社区活动-分组打码用户匹配池，活动关闭或过期后删除
	 * ZSet < $this + $merchantId + 活动ID, $userId, now >
	 */
	String SNS_TEAM_PLAY_MATCH = "snsTeamPlayMatch:";
	/**
	 * 社区活动-分组打码用户参与记录，当前正在进行中的用户，分组打码结束后删除$userId，活动关闭或过期后删除Key
	 * ZSet < $this + $merchantId, $userId, 活动ID.分组ID >
	 */
	String SNS_TEAM_PLAY_JOIN = "snsTeamPlayJoin:";
	/**
	 * 社区活动-分组打码用户发奖记录，用于查询分组打码活动的结算弹窗，查询后删除
	 * ZSet < $this + $merchantId, $userId, 活动ID.分组ID >
	 */
	String SNS_TEAM_PLAY_SETTLE = "snsTeamPlaySettle:";
	/**
	 * 拼团商品参与用户列表
	 * ZSet <"snsItemGroupBuy"+$snsItemId, $userId, $now >
	 * Set <"snsItemGroupBuy",$snsItemId >
	 * Set <"snsItemGroupBuy"+$promotionId, $userId >
	 */
	String SNS_ITEM_GROUP_BUY = "snsItemGroupBuy";
	/**
	 * 社区活动-砍价商品-用户业务数据是否已更新
	 * ZSet < $this + yyyyMMdd, $userId-$action, 最后更新的毫秒级时间戳 >
	 */
	String USER_SNS_ITEM_STAT_PREFIX = "UserSnsItemStat:";
	/** 充值行为后缀 */
	String snsActionSuffixRecharge = "-2";
	/** 打码行为后缀 */
	String snsActionSuffixPlay = "-3";

	/**
	 * 网红用户ID Set
	 */
	String BLOGGER_USER_ID = "bloggerUserId:";
	/**
	 * 营销风控检查-领取奖励判断IP是否重复
	 * Hash < "marketSameIp"+$merchantId+$cond, $ip, $userId >
	 */
	String MARKET_SAME_IP = "marketSameIp";
	/**
	 * 营销风控检查-领取奖励判断设备是否重复
	 * Hash < "marketSameDevice"+$merchantId+$cond, $device, $userId >
	 */
	String MARKET_SAME_DEVICE = "marketSameDevice";
	/**
	 * 需要重新发放奖励的用户
	 * Set < "reprocessRewardUser", $userId >
	 */
	String REPROCESS_REWARD_USER = "reprocessRewardUser";

	/**
	 * 奖金转盘活动缓存数据 ZSet  < $merchantId+ $promotionId+$period > <br/>
	 * 奖金数值（大于后可领取）: < $userId + Total, $total >     <br/>
	 * 剩余的抽奖次数: < $userId + Point, 剩余可用次数 >    <br/>
	 * 已完成任务次数: < $userId + Task, 完成次数 >    <br/>
	 * 是否已经完成: < $userId, 1 >    <br/>
	 */
	String REWARD_TURNTABLE_PREFIX = "rT";

	/**
	 * 奖金转盘活动:已完成的一次性任务 ZSet < $merchantId+$promotionId > <br/>
	 * : < $userId, 位运算TaskType >    <br/>
	 */
	String REWARD_TURNTABLE_TASK_PREFIX = "rTTask";
	/**
	 * 不验证IP白名单的商户ID
	 * Set < "whitelistNotCheck", $merchantId >
	 */
	String WHITELIST_NOT_CHECK = "whitelistNotCheck";
	/**
	 * 三级代理掉绑前缀
	 * Hash 经销商首充配置 thirdAgentStat:recharge < 用户ID， "充值奖励，邀请人数，掉绑人数，累计充值金额" >
	 */
	String THIRD_AGENT_STAT_PREFIX = "thirdAgentStat:";
	/**
	 * 商户存钱宝收益率
	 * Hash < "toggleRate", $merchantId, $rate >
	 */
	String TOGGLE_RATE = "toggleRate";
	/** 商户配置标识位修改锁 */
	String MERCHANT_CONFIG_FLAG = "merchant:configFlag:";
	/**
	 * 代理模式切换记录 Set
	 */
	String AGENT_MODEL_TOGGLE = "agentModelToggle:";
	/**
	 * 代理模式缓存数据 <br/>
	 * Hash <@see AgentModelCacheVO.AgentModelCacheType#getField, AgentModelCacheVO> <br/>
	 * $merchantId_0：代理配置的延时生效数据（到期自动删除） <br/>
	 * $merchantId_1：代理模式的延时生效数据（到期自动删除）<br/>
	 * $merchantId_2：当前商户正在运行的模式ID（不可删除，万一删除了也会重建） <br/>
	 * 每种类型只会存在一个最新的值，例如：1号修改 $merchantId_0，延时生效为2号，当2号再进行修改时，需要保证1号的缓存数据已经更新到数据库中了，否则1号的数据就会丢失
	 */
	String AGENT_EFFECTIVE_CONFIG = "agentEffectiveConfig";
	/**
	 * 商户代理顶部自动增长的奖金，仅对使用了指定主题的商户，定时每5分钟刷新 Hash<$merchantId+$agentModelId, reward>
	 */
	String AGENT_TOP_REWARD = "agentTopReward";

	/**
	 * 用户今日剩余转盘次数按天
	 * ZSet < "userSpinsPer+$promotionId+$yyyyMMdd, $userId+$turntableLevel, 今日已转次数 >
	 */
	String USER_SPINS_PER_PREFIX = "userSpinsPer:";

	/** 商户代理配置更新锁 */
	String AGENT_MODEL_CONFIG_LOCK = "agentModelConfigLock:";

	/** 拼团任务活动结束时间 ZSet < "promotionGroupTaskExpire", $merchantId-$groupTaskId-groupTaskType-$promotionId, 时间戳 > */
	String PROMOTION_GROUP_TASK_EXPIRE = "promotionGroupTaskExpire";

	/** 拼团任务加入机器人 ZSet < "promotionJoinRobotTaskExpire", $merchantId-$promotionId-$groupTaskId, 时间戳 > */
	String PROMOTION_JOIN_ROBOT_TASK_EXPIRE = "promotionJoinRobotTaskExpire";

	/** 团组成员列表 String  < promotion:groupTask:member:${taskId} 团成员信息 > see {@link com.bojiu.webapp.user.vo.PromotionGroupTaskMemberVO } */
	String PROMOTION_GROUP_TASK_MEMBER_PREFIX = "promotion:groupTask:member:";

	/** 奖池结束时间结束时间 ZSet < "promotionJackpotTaskEnd", $merchantId-$promotionId-$taskId, 时间戳 > */
	String PROMOTION_JACKPOT_TASK_END = "promotionJackpotTaskEnd";

	/**
	 * jackpot奖励弹窗
	 * Hash < "promotionJackpotPop", $userId, "类型,金额" >
	 */
	String PROMOTION_JACKPOT_POP = "promotionJackpotPop";
	/**
	 * 用户拼团任务奖励列表
	 * Hash < "userGroupTaskReward"+$merchantId, $userId, $taskIds >
	 */
	String USER_GROUP_TASK_REWARD = "userGroupTaskReward";

	/**
	 * Jackpot活动非真实活动的奖池金额
	 * Hash < "promotionJackpotAmount", $promotionId, $amount >
	 */
	String PROMOTION_JACKPOT_AMOUNT = "promotionJackpotAmount";
	/** 待处理的推广奖励 ZSet < merchantId+promotionId+agentId, 秒级时间戳 > */
	String AGENT_PROMOTION_AWARD_KEY = "agentPromotionAward:";
	/** 待处理的幸运转盘奖励 ZSet < merchantId+promotionId+agentId, 秒级时间戳 > */
	String TURNTABLE_PROMOTION_AWARD_KEY = "turntablePromotionAward:";

	/**
	 * 商户消息发送循环方式 Hash
	 * <p>
	 * 按天 < "msgSentKey", "$msgId-day", 1 >
	 * <p>
	 * 按周 < "msgSentKey", "$msgIdW-$周几", 1 >
	 * <p>
	 * 按月 < "msgSentKey", "$msgIdM-day", 1 >
	 */
	String MSG_SENT_KEY = "msgSentKey";

	/**
	 * 用户设置自动登录过期时间 ZSet < "userAutoLoginExpire", $merchantId-$userId, 时间戳 >
	 * 用户自动登录过期时间判断 ZSet < "userAutoLoginExpire", $merchantId-$userId-expire, 时间戳+最后一次登录时间戳 >
	 * 用户设置自动登录过期时间type Hash < "userAutoLoginExpire - $merchantId", $userId, $type >
	 */
	String USER_AUTO_LOGIN_EXPIRE = "userAutoLoginExpire";
	/**
	 * 公积金再次参与
	 * ZSet < "depositAgainParticipate"+$merchantId, $userId, $expireTime >
	 */
	String DEPOSIT_AGAIN_PARTICIPATE = "depositAgainParticipate";
	/**
	 * 排行榜循环周期 (当前轮) TODO 可以重设活动过期时间,所以要更新redis的过期时间
	 * ZSet < "rankCycle:"+$promotionId,  $cycle, $cycleEndTime >
	 */
	String RANK_CYCLE = "pRankCycle:";

	/**
	 * 弹窗只弹一次处理
	 * Hash < "promotionPopUp-$type-$promotionId", $userId, "1" >
	 */
	String PROMOTION_POP_UP = "promotionPopUp";

	/**
	 * 用户删除设备缓存
	 * Hash < "loginDeviceSession", $merchantId-$userId-$sessionId, "1" >
	 */
	String LOGIN_DEVICE_SESSION = "loginDeviceSession";
}