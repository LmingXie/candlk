CREATE TABLE `x_token_event` (
 `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
 `tweet_id` int(10) unsigned NOT NULL COMMENT '推文ID',
 `type` tinyint(3) unsigned NOT NULL COMMENT '事件类型：0=特殊关注账号；1=热门推文；2=浏览量猛增；',
 `coin` varchar(100) NOT NULL DEFAULT '' COMMENT '代币名称',
 `symbol` varchar(100) NOT NULL DEFAULT '' COMMENT '代币简称',
 `ca` varchar(100) CHARACTER SET latin1 NOT NULL DEFAULT '' COMMENT '代币地址',
 `desc` varchar(1000) NOT NULL DEFAULT '' COMMENT '代币简介',
 `status` tinyint(4) unsigned NOT NULL COMMENT '业务状态',
 `add_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
 `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代币事件表';

CREATE TABLE `x_tweet` (
   `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
   `tweet_id` varchar(20) CHARACTER SET latin1 NOT NULL COMMENT '推文ID',
   `type` tinyint(3) unsigned NOT NULL COMMENT '推文类型：0=发帖；1=回复；2=引用；3=转发',
   `provider_type` tinyint(3) unsigned NOT NULL COMMENT '推文来源厂商类型',
   `text` text NOT NULL COMMENT '推文内容',
   `entities` json DEFAULT NULL COMMENT '推文实体：urls=引用图片/视频；mentions=提及的人；hashtags=被识别的标签文本；',
   `org_msg` text NOT NULL COMMENT '原始推文数据',
   `retweet` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '此推文被转发的次数',
   `reply` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '此推文被回复的次数',
   `likes` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '此推文被点赞的次数',
   `quote` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '此推文被引用的次数',
   `bookmark` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '此推文被收藏的次数',
   `impression` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '此推文被浏览的次数',
   `biz_flag` smallint(5) unsigned NOT NULL DEFAULT '0' COMMENT '业务标识',
   `add_time` datetime NOT NULL COMMENT '推文发布时间',
   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
   `username` varchar(100) CHARACTER SET latin1 NOT NULL COMMENT '推特用户账号名',
   `images` json DEFAULT NULL COMMENT '图片',
   `videos` json DEFAULT NULL COMMENT '视频',
   `status` tinyint(4) unsigned NOT NULL DEFAULT '0' COMMENT '业务状态',
   `score` decimal(5,2) unsigned NOT NULL DEFAULT '0.00' COMMENT '分数',
   `words` json DEFAULT NULL COMMENT '命中的关键词',
   PRIMARY KEY (`id`),
   UNIQUE KEY `uk_tweetId` (`tweet_id`) USING BTREE,
   KEY `idx_addTime_status` (`add_time`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=111476 DEFAULT CHARSET=utf8mb4 COMMENT='推文信息表';

CREATE TABLE `x_tweet_user` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `provider_type` tinyint(3) unsigned NOT NULL COMMENT '推文来源厂商类型',
    `user_id` varchar(20) CHARACTER SET latin1 NOT NULL COMMENT '推特用户ID',
    `username` varchar(100) CHARACTER SET latin1 NOT NULL COMMENT '推特用户账号名',
    `nickname` varchar(255) DEFAULT NULL COMMENT '推特昵称',
    `avatar` varchar(255) CHARACTER SET latin1 DEFAULT NULL COMMENT '推特头像',
    `banner` varchar(255) CHARACTER SET latin1 DEFAULT NULL COMMENT '推特横幅',
    `pinned` varchar(255) CHARACTER SET latin1 DEFAULT NULL COMMENT '指定推特',
    `location` varchar(255) DEFAULT NULL COMMENT '地区',
    `description` json DEFAULT NULL COMMENT '推特简介',
    `followers` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '关注该用户的用户数',
    `tweets` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '该用户发布的帖子数（包括转推）',
    `following` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '该用户关注的用户数',
    `media` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '该用户发布的媒体数',
    `listed` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '包含该用户的列表数量',
    `likes` int(10) unsigned NOT NULL DEFAULT '0' COMMENT '该用户创建的赞数',
    `tweet_last_time` datetime DEFAULT NULL COMMENT '最后一次发帖时间',
    `type` tinyint(3) unsigned NOT NULL DEFAULT '1' COMMENT '账号类型：0=普通账号；1=特殊关注账号；2=二级账号；',
    `biz_flag` smallint(5) unsigned NOT NULL DEFAULT '0' COMMENT '业务标识',
    `add_time` datetime NOT NULL COMMENT '推特账号创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`) USING BTREE,
    UNIQUE KEY `uk_userId` (`user_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1097 DEFAULT CHARSET=utf8mb4 COMMENT='推特用户表';

CREATE TABLE `x_tweet_word` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `words` varchar(255) NOT NULL COMMENT '词组',
    `type` tinyint(3) unsigned NOT NULL COMMENT '事件类型：0=热门词；1=二级词；2=普通词；',
    `priority` int(8) unsigned NOT NULL DEFAULT '0' COMMENT '优先级',
    `add_time` datetime NOT NULL COMMENT '添加时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=306 DEFAULT CHARSET=utf8mb4 COMMENT='推特词库';

CREATE TABLE `x_stop_word` (
   `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
   `words` varchar(255) NOT NULL COMMENT '停用词组',
   `add_time` datetime NOT NULL COMMENT '添加时间',
   `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
   PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=306 DEFAULT CHARSET=utf8mb4 COMMENT='推特词库';