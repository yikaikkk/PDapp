-- 创建pd_article表的SQL语句
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

-- 插入示例数据
INSERT INTO `pd_article` (`title`, `name`, `latitude`, `longitude`, `type`, `description`, `tips`, `author_id`, `create_time`) 
VALUES 
('北京故宫建筑之美', '故宫博物院', 39.9163447, 116.3972282, 'architecture', '紫禁城，明清两朝的皇家宫殿，建筑宏伟壮观', '早晨或傍晚光线最佳，注意人流量', 1, NOW()),
('香山红叶季', '香山公园', 39.9905660, 116.1900690, 'nature', '北京著名的赏红叶胜地，秋季景色迷人', '10-11月最佳拍摄时间，穿防滑鞋登山', 1, NOW()),
('南锣鼓巷夜景', '南锣鼓巷', 39.9368190, 116.4035730, 'street', '北京著名的胡同街区，夜晚灯火通明', '夜晚7-9点最佳，注意控制快门速度', 1, NOW());