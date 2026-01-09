/*
 Navicat Premium Dump SQL

 Source Server         : 127.0.0.1【TG】
 Source Server Type    : MySQL
 Source Server Version : 80037 (8.0.37)
 Source Host           : 127.0.0.1:3607
 Source Schema         : bet_local

 Target Server Type    : MySQL
 Target Server Version : 80037 (8.0.37)
 File Encoding         : 65001

 Date: 03/01/2026 18:57:41
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for gs_admin_log
-- ----------------------------
DROP TABLE IF EXISTS `gs_admin_log`;
CREATE TABLE `gs_admin_log`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type` varchar(20) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL COMMENT '操作类型',
  `merchant_id` int UNSIGNED NULL DEFAULT NULL COMMENT '商户ID：0 表示平台',
  `emp_id` int UNSIGNED NOT NULL COMMENT '操作员ID',
  `assoc_type` varchar(32) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL COMMENT '操作目标类型',
  `assoc_id` bigint UNSIGNED NOT NULL COMMENT '操作目标ID',
  `detail` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '操作详情（JSON字符串）',
  `audit_result` tinyint NOT NULL DEFAULT 0 COMMENT '审核结果：0=默认；1=通过；-1=不通过',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '备注信息',
  `status` tinyint NOT NULL COMMENT '业务状态',
  `state` tinyint NOT NULL COMMENT '可见状态：3=全部可见；2=自己可见；1=平台可见；0=全部不可见',
  `add_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '操作时间',
  `update_time` timestamp(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
  `add_ip` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' COMMENT '操作IP',
  `module_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '模块名',
  `func` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '功能名',
  `action` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '操作行为',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_empId`(`emp_id` ASC) USING BTREE,
  INDEX `idx_type`(`type`(10) ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 43502 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '管理员操作日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gs_admin_log
-- ----------------------------
INSERT INTO `gs_admin_log` VALUES (906, '1', 106, 103, 'Meta', 1467, '[{\"field\":\"c_\",\"label\":\"晋级条件-打码\",\"now\":\"开启\",\"old\":\"关闭\"},{\"field\":\"r_\",\"label\":\"保级条件-充值\",\"now\":\"开启\",\"old\":\"关闭\"},{\"field\":\"c_\",\"label\":\"保级条件-打码\",\"now\":\"开启\",\"old\":\"关闭\"},{\"field\":\"r_\",\"label\":\"特权条件-充值\",\"now\":\"开启\",\"old\":\"关闭\"},{\"field\":\"c_\",\"label\":\"特权条件-打码\",\"now\":\"开启\",\"old\":\"关闭\"},{\"field\":\"qr\",\"label\":\"晋级再充值\",\"now\":1501,\"old\":1500},{\"field\":\"lmr\",\"label\":\"保级-上个月充值\",\"now\":1301,\"old\":1300},{\"field\":\"dr\",\"label\":\"日奖励再充值\",\"now\":101,\"old\":100}]', 0, '', 1, 3, '2024-12-21 10:05:14.254', '2024-12-21 10:05:14.254', '31.223.184.140', '用户管理', 'VIP等级', '升级配置');
INSERT INTO `gs_admin_log` VALUES (907, '0', 10, 6, 'Meta', 2691, '', 0, '', 1, 3, '2024-12-21 10:14:08.108', '2024-12-21 10:14:08.108', '38.107.236.182', '财务中心', '提现管理-提现配置', '新增提现设置');
INSERT INTO `gs_admin_log` VALUES (909, '0', 10, 6, 'Meta', 2695, '[{\"field\":\"limit\",\"label\":\"Wallet限制创建的提现账号数量\",\"now\":5},{\"field\":\"del\",\"label\":\"Wallet是否允许删除\",\"now\":\"否\"},{\"field\":\"allowAdd.CPF\",\"label\":\"前端提现附加选项:CPF\",\"now\":\"是\",\"old\":\"\"},{\"field\":\"allowAdd.PHONE\",\"label\":\"前端提现附加选项:PHONE\",\"now\":\"是\",\"old\":\"\"},{\"field\":\"allowAdd.EMAIL\",\"label\":\"前端提现附加选项:EMAIL\",\"now\":\"是\",\"old\":\"\"},{\"field\":\"allowAdd.EVP\",\"label\":\"前端提现附加选项:EVP\",\"now\":\"是\",\"old\":\"\"}]', 0, '', 1, 3, '2024-12-21 10:27:53.052', '2024-12-21 10:27:53.052', '38.107.236.182', '财务中心', '提现管理-提现设置', '提现类型设置');
INSERT INTO `gs_admin_log` VALUES (910, '1', 122, 103, 'Meta', 2620, '[{\"field\":\"multiple\",\"label\":\"VIP-奖励稽核-稽核倍数\",\"now\":11}]', 0, '', 1, 3, '2024-12-21 10:28:37.107', '2024-12-21 10:28:37.107', '31.223.184.140', '稽核配置', 'VIP-奖励稽核', '稽核配置编辑');

-- ----------------------------
-- Table structure for gs_meta
-- ----------------------------
DROP TABLE IF EXISTS `gs_meta`;
CREATE TABLE `gs_meta`  (
  `id` int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `merchant_id` int UNSIGNED NOT NULL COMMENT '商户ID',
  `type` tinyint UNSIGNED NOT NULL COMMENT '配置类型：1=机器人配置',
  `name` varchar(20) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL COMMENT '元数据名称',
  `value` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '元数据值',
  `label` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '配置标签',
  `ext` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '拓展配置',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '业务状态：1=有效；0=无效',
  `add_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '添加时间',
  `update_time` datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '最后更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_type_name`(`type` ASC, `name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 78 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '全局元数据配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gs_meta
-- ----------------------------
INSERT INTO `gs_meta` VALUES (73, 0, 3, '1068', '1068', '数据库升级标记', '', 1, '2025-11-28 19:52:26.808', '2025-11-28 19:52:26.808');
INSERT INTO `gs_meta` VALUES (74, 0, 1, 'HG', '{\"domain\":\"hga038.com\",\"username\":\"CntBet1203\",\"password\":\"123456789Cr\",\"proxy\":\"proxy://127.0.0.1:10809\",\"scoreResultUrl\":\"https://125.252.69.119\"}', '游戏供应商配置', '', 1, '2025-07-24 00:04:44.208', '2025-07-24 00:04:44.208');
INSERT INTO `gs_meta` VALUES (75, 0, 1, 'D1CE', '{\"domain\":\"cncrownbet-api.d1ce.com\",\"username\":\"1611826811@qq.com\",\"password\":\"123456789Cr\",\"proxy\":\"proxy://127.0.0.1:10809\",\"scoreResultUrl\":\"https://125.252.69.119\"}', '游戏供应商配置', '', 1, '2025-07-24 00:04:44.208', '2025-07-24 00:04:44.208');
INSERT INTO `gs_meta` VALUES (76, 0, 1, 'KY', '{\"domain\":\"api.q7stajv.com\",\"token\":\"482945626a459b14155d27e16e6ace15e60bdcac\",\"proxy\":\"proxy://127.0.0.1:10809\"}', '游戏供应商配置', '', 1, '2025-07-24 00:04:44.208', '2025-07-24 00:04:44.208');
INSERT INTO `gs_meta` VALUES (77, 0, 2, 'base_rate_config', '{\"aPrincipal\":1000.0,\"aRebate\":0.02,\"aRechargeRate\":0.0,\"bRebate\":0.025}', '基础返点配置', '', 1, '2025-07-24 00:04:44.208', '2025-12-29 11:46:54.573');

SET FOREIGN_KEY_CHECKS = 1;
