-- 用户运动爱好、提醒状态与首次完成奖励规则
-- 适用数据库：MySQL 5.7

ALTER TABLE `blade_user`
  ADD COLUMN `interest_remind_disabled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否不再提醒完善运动爱好[0:提醒,1:不再提醒]' AFTER `address`,
  ADD COLUMN `interest_completed_at` datetime DEFAULT NULL COMMENT '首次完成运动爱好选择时间' AFTER `interest_remind_disabled`;

CREATE TABLE `ldqc_user_interest` (
  `id` bigint NOT NULL COMMENT '主键',
  `create_user` bigint DEFAULT NULL,
  `create_dept` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_user` bigint DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `status` tinyint DEFAULT 1,
  `is_deleted` tinyint DEFAULT 0,
  `tenant_id` varchar(12) DEFAULT '000000',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `category_id` bigint NOT NULL COMMENT '运动分类ID，对应t_category.id',
  `sort` int NOT NULL DEFAULT 1 COMMENT '展示排序',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_category_deleted` (`user_id`, `category_id`, `is_deleted`),
  KEY `idx_user_interest_user` (`user_id`, `is_deleted`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户运动爱好';

INSERT INTO `points_rule`
(`id`, `create_user`, `create_dept`, `create_time`, `update_user`, `update_time`, `is_deleted`, `rule_code`, `rule_name`, `scene_type`, `grant_points`, `daily_limit_count`, `daily_limit_points`, `lifecycle_limit_count`, `require_first_flag`, `status`, `ext_json`, `tenant_id`)
SELECT 910104, 1, 1, NOW(), 1, NOW(), 0, 'PROFILE_INTEREST_COMPLETED_BASIC', '首次完善运动爱好', 'PROFILE_INTEREST_COMPLETED', 20, 1, 20, 1, 1, 1, NULL, '000000'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `points_rule` WHERE `rule_code` = 'PROFILE_INTEREST_COMPLETED_BASIC' AND `is_deleted` = 0);
