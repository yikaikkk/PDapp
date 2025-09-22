-- 创建pd_user表的SQL语句
CREATE TABLE `pd_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `status` tinyint(4) DEFAULT 0 COMMENT '状态(0:正常, 1:禁用)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 摄影地点博文数据表
CREATE TABLE IF NOT EXISTS `pd_article` (
                                         `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '博文ID',
                                         `title` VARCHAR(255) NOT NULL COMMENT '博文标题',
    `name` VARCHAR(100) NOT NULL COMMENT '地点名称',
    `latitude` DECIMAL(10,7) NOT NULL COMMENT '纬度',
    `longitude` DECIMAL(10,7) NOT NULL COMMENT '经度',
    `type` VARCHAR(50) NOT NULL COMMENT '类型（architecture/nature/portrait/street/night）',
    `description` TEXT COMMENT '地点描述',
    `tips` TEXT COMMENT '摄影提示',
    `author_id` INT COMMENT '作者ID',
    `create_time` DATETIME NOT NULL COMMENT '创建时间',
    `update_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_type` (`type`),
    INDEX `idx_author_id` (`author_id`),
    INDEX `idx_create_time` (`create_time`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='摄影地点博文表';
