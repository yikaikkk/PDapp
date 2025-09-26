-- 创建点赞表
CREATE TABLE IF NOT EXISTS `pd_like` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `article_id` INT(11) NOT NULL COMMENT '博文ID',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '点赞状态（1:点赞, 0:取消点赞）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_article` (`user_id`, `article_id`) COMMENT '用户博文唯一索引',
  KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引',
  KEY `idx_article_id` (`article_id`) COMMENT '博文ID索引',
  KEY `idx_status` (`status`) COMMENT '状态索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表';

-- 为现有文章表添加点赞数字段（可选）
-- ALTER TABLE `pd_article` ADD COLUMN `like_count` INT(11) NOT NULL DEFAULT 0 COMMENT '点赞数量';

-- 创建索引优化查询性能
-- CREATE INDEX idx_article_like_count ON pd_article(like_count);
-- CREATE INDEX idx_like_create_time ON pd_like(create_time);
-- CREATE INDEX idx_like_user_status ON pd_like(user_id, status);
-- CREATE INDEX idx_like_article_status ON pd_like(article_id, status);