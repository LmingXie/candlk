ALTER TABLE `x_tweet_word`
    ADD COLUMN `status` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '业务状态：0=未启用；1=已启用',
    ADD COLUMN `provider_type` tinyint UNSIGNED NOT NULL DEFAULT 0 COMMENT '来源厂商类型';

ALTER TABLE `x_tweet_word`
    MODIFY COLUMN `words` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs NOT NULL;