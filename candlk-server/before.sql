ALTER TABLE `x_tweet_word`
    ADD COLUMN `status` tinyint UNSIGNED NOT NULL DEFAULT 1 COMMENT '业务状态：0=未启用；1=已启用';