-- 【注意】记得去掉SQL语句中的 数据库名称限定，因为不同运行环境的数据库名称可能是不一样的。

-- 新增字段-消息配置
ALTER TABLE `gs_message_config`
    ADD COLUMN `type` tinyint NOT NULL COMMENT '类型：0=用户消息；1=商户消息' AFTER `id`;

ALTER TABLE `gs_message_config`
    ADD COLUMN `message_type` tinyint NOT NULL COMMENT '用户消息类型：0=消息、1=跑马灯；商户消息类型：0=消息、1=公告、2=风险预警状态、3=风险后台限制、4=风控禁止游戏；' AFTER `type`;

ALTER TABLE `gs_message_config`
    MODIFY `freight` tinyint UNSIGNED NOT NULL COMMENT '用户消息收件人：1=全部会员、2=自定义会员、3=会员层级、4=VIP等级；商户消息收件人：1=全部商户、2=自定义商户、3=商户等级、4=欠费商户；5=风险预警状态商户、6=风险后台限制商户、7=风控禁止游戏商户';

ALTER TABLE `gs_message_config`
    MODIFY `ranges` text CHARACTER SET ascii COLLATE ascii_general_ci COMMENT '范围值';

ALTER TABLE `gs_message_config`
    ADD COLUMN `tip` tinyint COMMENT '提示：0=是；1=否；' AFTER `pop_end_time`;

ALTER TABLE `gs_message_config`
    ADD COLUMN `end_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '结束时间' AFTER `send_time`;


-- 初始化历史数据
UPDATE `gs_recharge_log`
SET `local_amount` = `amount` * 100;

ALTER TABLE `gs_cash_log`
    CHANGE COLUMN `exchange_rate` `local_amount` bigint UNSIGNED NOT NULL COMMENT '兑换金额（转为商户本地货币）：最后2位表示小数' AFTER `currency`,
    MODIFY COLUMN `amount` decimal (14, 2) UNSIGNED NOT NULL COMMENT '提现金额' AFTER `account_id`,
    ADD COLUMN `exchange_rate` decimal (9, 4) NOT NULL COMMENT '兑换汇率：localAmount ≈ amount * exchangeRate' AFTER `local_amount`,
    MODIFY COLUMN `fee` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '服务费（本地货币金额）：最后2位表示小数' AFTER `trade_no`,
    MODIFY COLUMN `bad_debt` int UNSIGNED NOT NULL DEFAULT 0 COMMENT '扣除坏账（本地货币金额）：最后2位表示小数' AFTER `fee`,
    MODIFY COLUMN `real_amount` decimal (14, 2) UNSIGNED NOT NULL COMMENT '实到金额实到金额（兑换货币类型）：=申请金额-服务费-坏账金额（要预先做好汇率转换）' AFTER `bad_debt`;

-- 厂家表、游戏表取消必填字段
ALTER TABLE `gs_game`
    MODIFY COLUMN `image` varchar (128) CHARACTER SET ascii COLLATE ascii_general_ci NULL DEFAULT '' COMMENT '游戏图片' AFTER `game_id`;
ALTER TABLE `gs_vendor`
    MODIFY COLUMN `share_rate` decimal (4, 2) UNSIGNED NULL COMMENT '抽成比例：单位为百分比，计算注意时换算' AFTER `currency`,
    MODIFY COLUMN `image` varchar (128) CHARACTER SET ascii COLLATE ascii_general_ci NULL DEFAULT '' COMMENT '厂家图片' AFTER `share_rate`,
    MODIFY COLUMN `game_num` smallint UNSIGNED NULL COMMENT '游戏数量' AFTER `slogan_image`;

-- 人工出入款表
CREATE TABLE `gs_merchant_account_adjust_log`
(
    `id`             int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键Id',
    `merchant_id`    int     NOT NULL COMMENT '商户Id',
    `operation_type` tinyint NOT NULL COMMENT '操作类型：4=人工补单；5=人工扣除；6=赠送金额',
    `amount`         bigint  NOT NULL COMMENT '补偿金额：（补单，扣除，赠送）最后6位表示小数',
    `emp_id`         int     NOT NULL COMMENT '操作人',
    `add_time`       datetime(3) NOT NULL COMMENT '创建时间',
    `status`         tinyint NOT NULL COMMENT '业务状态',
    `remark`         varchar(100)     DEFAULT NULL COMMENT '备注',
    `state`          tinyint NOT NULL DEFAULT '3' COMMENT '可见性',
    `update_time`    datetime(3) NOT NULL COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY              `idx_merchantId` (`merchant_id`) USING BTREE,
    KEY              `idx_addTime` (`add_time`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 17
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='人工出入单';

ALTER TABLE `gs_merchant_account_log`
    MODIFY COLUMN `assoc_type` VARCHAR (30) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL DEFAULT '' COMMENT '关联的业务实体类型';


-- 调整商户游戏表，明确游戏ID归属
ALTER TABLE `gs_merchant_game`
    MODIFY COLUMN `game_id` int UNSIGNED NOT NULL COMMENT '游戏ID（第三方提供的ID，不同厂商可能会重复）' AFTER `vendor_id`;

-- 调整游戏表，增加厂家别名、游戏类型
ALTER TABLE `gs_game`
    ADD COLUMN `alias` char(2) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL COMMENT '厂商别名（内部固定标识）' AFTER `vendor_id`,
    ADD COLUMN `type`  tinyint UNSIGNED                                     NOT NULL COMMENT '游戏类型：1=棋牌；2=捕鱼；3=电子；4=电竞；5=体育；6=视讯' AFTER `alias`;

-- 调整商户游戏表，增加厂家别名、游戏类型
ALTER TABLE `gs_merchant_game`
    ADD COLUMN `alias` char(2) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL COMMENT '厂商别名（内部固定标识）' AFTER `vendor_id`,
    ADD COLUMN `type`  tinyint UNSIGNED                                     NOT NULL COMMENT '游戏类型：1=棋牌；2=捕鱼；3=电子；4=电竞；5=体育；6=视讯' AFTER `alias`;

-- 调整人工出入表，增加补偿后余额
ALTER TABLE `gs_merchant_account_adjust_log`
    ADD COLUMN `active_amount` bigint NOT NULL COMMENT '补偿后余额：最后6位表示小数' AFTER `amount`;

-- 调整用户充值记录表 增加 费率、操作员 列
ALTER TABLE `gs_recharge_log`
    ADD COLUMN `rate` decimal(10, 4) NULL COMMENT '兑换比例：USDT转本地货币费率' AFTER `state`,
    ADD COLUMN `emp_id` int UNSIGNED   NULL COMMENT '操作员ID' AFTER `rate`;


-- 商品表
CREATE TABLE `gs_goods`
(
    `id`                     int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `merchant_id`            int UNSIGNED NOT NULL COMMENT '商户ID：0 表示平台',
    `name`                   varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
    `subhead`                varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '副标题',
    `img_url`                varchar(150) CHARACTER SET ascii COLLATE ascii_general_ci    NOT NULL COMMENT '商品图片',
    `type`                   tinyint UNSIGNED NOT NULL COMMENT '商品类型',
    `price`                  bigint UNSIGNED NOT NULL DEFAULT '0' COMMENT '售价-货币，最后2位表示小数',
    `price_diamond`          bigint UNSIGNED NOT NULL COMMENT '售价-钻石，最后2位表示小数',
    `original_price`         bigint UNSIGNED NOT NULL COMMENT '原价-货币，最后2位表示小数',
    `original_price_diamond` bigint UNSIGNED NOT NULL COMMENT '原价-钻石，最后2位表示小数',
    `inventory`              bigint UNSIGNED NOT NULL COMMENT '库存',
    `detail`                 varchar(1500)                                                NOT NULL COMMENT '商品详情',
    `status`                 tinyint                                                      NOT NULL COMMENT '售卖状态：0=关闭；1=开启',
    `state`                  tinyint                                                      NOT NULL COMMENT '可见性',
    `add_time`               datetime(3) NOT NULL COMMENT '创建时间',
    `update_time`            datetime(3) NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='商品表';


-- 限时优惠表
CREATE TABLE `gs_goods_activity`
(
    `id`                      int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `merchant_id`             int UNSIGNED NOT NULL COMMENT '商户ID：0 表示平台',
    `goods_id`                int UNSIGNED NOT NULL COMMENT '商品id',
    `type`                    tinyint UNSIGNED NOT NULL COMMENT '活动类型：1=限时活动',
    `discounts_price`         bigint UNSIGNED NOT NULL COMMENT '优惠价-货币，最后2位表示小数',
    `discounts_price_diamond` bigint UNSIGNED NOT NULL COMMENT '优惠价-钻石，最后2位表示小数',
    `everyone_limit`          tinyint UNSIGNED DEFAULT NULL COMMENT '每人限制',
    `begin_time`              datetime NOT NULL COMMENT '活动开始时间',
    `end_time`                datetime NOT NULL COMMENT '活动结束时间',
    `remark`                  varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
    `status`                  tinyint  NOT NULL COMMENT '活动状态：0=关闭；1=开启',
    `state`                   tinyint  NOT NULL COMMENT '可见性',
    `add_time`                datetime(3) NOT NULL COMMENT '创建时间',
    `update_time`             datetime(3) NOT NULL COMMENT '更新时间',
    `sold_num`                bigint UNSIGNED DEFAULT NULL COMMENT '已售出',
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE KEY `idx_goods_id_type` (`goods_id`, `type`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='限时优惠表';

-- 修改商品，限时优惠表，部分必填字段
ALTER TABLE `gs_goods`
    MODIFY `original_price` bigint UNSIGNED NULL COMMENT '原价-货币，最后2位表示小数' AFTER `price_diamond`;
ALTER TABLE `gs_goods`
    MODIFY `original_price_diamond` bigint UNSIGNED NULL COMMENT '原价-钻石，最后2位表示小数' AFTER `original_price`;

ALTER TABLE `gs_goods_activity`
    MODIFY `discounts_price` bigint UNSIGNED NULL COMMENT '优惠价-货币，最后2位表示小数' AFTER `type`;
ALTER TABLE `gs_goods_activity`
    MODIFY `discounts_price_diamond` bigint UNSIGNED NULL COMMENT '优惠价-钻石，最后2位表示小数' AFTER `discounts_price`;

-- 商品订单表
CREATE TABLE `gs_goods_order`
(
    `id`            int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `merchant_id`   int UNSIGNED NOT NULL COMMENT '商户ID：0 表示平台',
    `goods_id`      int                                                      NOT NULL COMMENT '商品名称',
    `user_id`       int                                                      NOT NULL COMMENT '用户id',
    `address_id`    int                                                      NOT NULL COMMENT '收获地址id',
    `order_no`      varchar(30) CHARACTER SET ascii COLLATE ascii_general_ci NOT NULL COMMENT '订单编号',
    `logistics_no`  varchar(30) CHARACTER SET ascii COLLATE ascii_general_ci DEFAULT NULL COMMENT '物流单号',
    `price`         bigint UNSIGNED DEFAULT NULL COMMENT '售价-货币，最后2位表示小数',
    `price_diamond` bigint UNSIGNED DEFAULT NULL COMMENT '售价-钻石，最后2位表示小数',
    `num`           bigint UNSIGNED NOT NULL COMMENT '数量',
    `pay_time`      datetime                                                 DEFAULT NULL COMMENT '支付时间',
    `pay_end_time`  datetime                                                 NOT NULL COMMENT '支付截至时间',
    `deliver_time`  datetime                                                 DEFAULT NULL COMMENT '发货时间',
    `sign_for_time` datetime                                                 DEFAULT NULL COMMENT '签收时间',
    `emp_id`        int                                                      DEFAULT NULL COMMENT '操作人',
    `status`        tinyint                                                  NOT NULL COMMENT '状态：0=已关闭；1=待支付；2=待发货；3=待签收；4=已签收',
    `state`         tinyint                                                  NOT NULL COMMENT '可见性',
    `add_time`      datetime(3) NOT NULL COMMENT '创建时间（创建订单时间）',
    `update_time`   datetime(3) NOT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT ='商品订单表';

-- 调整 游戏记录明细表，增加 输赢金币数 列
ALTER TABLE `gs_game_play_log`
    ADD COLUMN `win_lose_coin` bigint DEFAULT NULL COMMENT '输赢金币数：最后两位表示小数' AFTER `out_coin`;

-- 优惠奖励领取记录合并到VIP奖励领取记录一起
ALTER TABLE `gs_reward_collect_log`
    MODIFY COLUMN `level` tinyint(0) UNSIGNED NULL COMMENT 'VIP等级' AFTER `type`,
    ADD COLUMN `promotion_id` int UNSIGNED NULL COMMENT '活动ID' AFTER `update_time`;

ALTER TABLE `gs_reward_collect_log`
    MODIFY COLUMN `type` tinyint(0) UNSIGNED NOT NULL COMMENT '奖励类型：1=每日免费转盘奖励；2=闯关游戏奖励；3=生日奖励；4=升级奖励；5=每周奖励；6=每月奖励；7=充值奖励；8=打码奖励；9=签到奖励；' AFTER `user_id`;

-- 新增优惠活动表
CREATE TABLE `gs_promotion`
(
    `id`              int(0) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `name`            varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NOT NULL COMMENT '活动名称',
    `type`            tinyint(0) UNSIGNED NOT NULL COMMENT '活动类型：1=充值；2=打码；3=签到',
    `sort`            int(0) UNSIGNED NULL DEFAULT NULL COMMENT '排序（降序）',
    `begin_time`      datetime(3) NULL DEFAULT NULL COMMENT '开始时间',
    `end_time`        datetime(3) NULL DEFAULT NULL COMMENT '结束时间',
    `show_begin_time` datetime(3) NULL DEFAULT NULL COMMENT '开始时间',
    `show_end_time`   datetime(3) NULL DEFAULT NULL COMMENT '结束时间',
    `bonus_type`      tinyint(0) UNSIGNED NULL DEFAULT NULL COMMENT '奖金方式：1=固定；2=随机；3=比例',
    `rules`           json                                                          NOT NULL COMMENT '奖励规则',
    `image`           varchar(128) CHARACTER SET ascii COLLATE ascii_general_ci NULL DEFAULT '' COMMENT '宣传图',
    `remark`          varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '说明',
    `dispatch_mode`   tinyint(0) UNSIGNED NULL DEFAULT NULL COMMENT '派发方式：1=玩家自领-过期作废；2=自动派发',
    `promotion_cond`  tinyint(0) UNSIGNED NULL DEFAULT NULL COMMENT '活动条件：1=账号首充；2=累计充值；3=单笔充值',
    `audit_mode`      tinyint(0) UNSIGNED NULL DEFAULT NULL COMMENT '稽核方式：1=稽核奖金；2=稽核本金+奖金',
    `audit_multiple`  decimal(4, 2) UNSIGNED NULL DEFAULT NULL COMMENT '稽核倍数',
    `cycle_mode`      tinyint(0) UNSIGNED NULL DEFAULT NULL COMMENT '循环方式：1=单次活动；2=每日循环；3=每周循环',
    `audit_vendors`   varchar(100) CHARACTER SET ascii COLLATE ascii_general_ci NULL DEFAULT '' COMMENT '稽核厂家，以逗号分割',
    `fix_layers`      varchar(30) CHARACTER SET ascii COLLATE ascii_general_ci NULL DEFAULT '' COMMENT '固定层级，以逗号分割',
    `auto_layers`     varchar(200) CHARACTER SET ascii COLLATE ascii_general_ci NULL DEFAULT '' COMMENT '自动层级，以逗号分割',
    `coding_vendors`  json NULL COMMENT '打码厂家',
    `sign_mode`       tinyint(0) UNSIGNED NULL DEFAULT NULL COMMENT '签到方式：1=连续签到；2=累计签到',
    `sign_cycle`      tinyint(0) UNSIGNED NULL DEFAULT NULL COMMENT '签到周期7,15,30',
    `status`          tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态开关：0=关闭；1=草稿；2=待生效；3=已生效；4=已结束；',
    `add_time`        datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT '添加时间',
    `update_time`     datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP (3) COMMENT '最后更新时间',
    `state`           tinyint(0) NOT NULL DEFAULT 3 COMMENT '可见状态：3=全部可见；0=全部不可见',
    `merchant_id`     int(0) UNSIGNED NOT NULL COMMENT '活动的商户ID',
    `emp_id`          int(0) UNSIGNED NOT NULL COMMENT '员工ID',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '优惠活动表' ROW_FORMAT = Dynamic;

-- 修改优惠活动打码名称
ALTER TABLE `gs_promotion`
    CHANGE COLUMN `coding_vendors` `play_vendors` json NULL COMMENT '打码厂家' AFTER `auto_layers`;

-- 活动奖励删除自动层级条件，增加VIP等级条件
ALTER TABLE `gs_promotion`
    CHANGE COLUMN `auto_layers` `levels` varchar(30) CHARACTER SET ascii COLLATE ascii_general_ci NULL DEFAULT '' COMMENT '用户VIP等级，以逗号分割' AFTER `fix_layers`;